#!/usr/bin/env bash
# Build the trimmed ElecPilot rclone binary (librclone.so) for Android.
#
# Usage:
#   build-librclone.sh [--version vX.Y.Z] [--out <dir>] [--skip-upx] [--smoke-only <file>]
#                      [--src <existing-rclone-checkout>]
#
# Defaults: version from pinned-version.txt, out = app/src/main/jniLibs.
# With --smoke-only <librclone-bin> only the contract smoke test runs.
#
# Requires: git, go (>= toolchain.lock GO_VERSION), Android NDK at
# $ANDROID_HOME/ndk/<NDK_VERSION> (or $ANDROID_NDK_HOME), UPX for release ABI.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
PINNED_VERSION="$(tr -d '[:space:]' < "$SCRIPT_DIR/pinned-version.txt")"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/toolchain.lock" 2>/dev/null || true

VERSION="$PINNED_VERSION"
OUT_DIR="$REPO_ROOT/app/src/main/jniLibs"
SKIP_UPX=0
SMOKE_ONLY=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --version) VERSION="$2"; shift 2 ;;
    --out) OUT_DIR="$2"; shift 2 ;;
    --skip-upx) SKIP_UPX=1; shift ;;
    --smoke-only) SMOKE_ONLY="$2"; shift 2 ;;
    --src) SRC_DIR="$2"; shift 2 ;;
    *) echo "Unknown arg: $1" >&2; exit 1 ;;
  esac
done

smoke_test() {
  local bin="$1"
  echo "== smoke: version"
  "$bin" --version | head -3
  echo "== smoke: backends (want: drive, local, onedrive only)"
  local backends
  backends="$("$bin" --config /dev/null help backends 2>&1 || true)"
  echo "$backends" | grep -qE '(^|\s)drive(\s|:)' || { echo "SMOKE FAIL: backends (drive missing)"; echo "$backends"; return 1; }
  echo "$backends" | grep -qE '(^|\s)local(\s|:)' || { echo "SMOKE FAIL: backends (local missing)"; echo "$backends"; return 1; }
  echo "$backends" | grep -qE '(^|\s)onedrive(\s|:)' || { echo "SMOKE FAIL: backends (onedrive missing)"; echo "$backends"; return 1; }
  if echo "$backends" | grep -qE '(^|\s)(s3|dropbox|mega)(\s|:)'; then echo "SMOKE FAIL: untrimmed backend present"; echo "$backends"; return 1; fi
  echo "== smoke: lsf flags"
  # NOTE: capture first, grep second — `grep -q` on a live pipe SIGPIPEs the
  # writer and trips `set -o pipefail` even when the flag is present.
  local help_out; help_out="$(mktemp)"
  "$bin" --config /dev/null lsf --help >"$help_out" 2>&1
  grep -q -- "--format" "$help_out" || { echo "SMOKE FAIL: lsf --format"; rm -f "$help_out"; return 1; }
  grep -q -- "--separator" "$help_out" || { echo "SMOKE FAIL: lsf --separator"; rm -f "$help_out"; return 1; }
  rm -f "$help_out"
  for c in authorize config copyto delete deletefile listremotes mkdir; do
    "$bin" --config /dev/null "$c" --help >/dev/null 2>&1 || { echo "SMOKE FAIL: command $c"; return 1; }
  done
  echo "== smoke: copyto local->local round trip"
  local tmp; tmp="$(mktemp -d)"
  trap 'rm -rf "$tmp"' RETURN
  echo "elecpilot-smoke" > "$tmp/src.txt"
  (cd "$tmp" && "$bin" --config /dev/null copyto src.txt dst.txt) || { echo "SMOKE FAIL: copyto"; return 1; }
  grep -q elecpilot-smoke "$tmp/dst.txt" || { echo "SMOKE FAIL: copyto content"; return 1; }
  echo "SMOKE OK"
}

if [[ -n "$SMOKE_ONLY" ]]; then
  smoke_test "$SMOKE_ONLY"
  exit 0
fi

: "${ANDROID_NDK_HOME:=${ANDROID_HOME:-$HOME/Android/Sdk}/ndk/${NDK_VERSION:?set NDK_VERSION in toolchain.lock}}"
LLVM="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin"
API="${ANDROID_API_LEVEL:?set ANDROID_API_LEVEL in toolchain.lock}"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

echo "== clone rclone $VERSION"
if [[ -n "${SRC_DIR:-}" ]]; then
  cp -r "$SRC_DIR" "$WORK/rclone"
  (cd "$WORK/rclone" && git checkout -q "$VERSION" 2>/dev/null || true)
else
  git clone --depth 1 --branch "$VERSION" https://github.com/rclone/rclone.git "$WORK/rclone"
fi
echo "== stage trimmed module"
mkdir -p "$WORK/trim"
cp "$SCRIPT_DIR/main.go" "$WORK/trim/main.go"
cat > "$WORK/trim/go.mod" <<EOF
module elecpilot-rclone

go ${GO_VERSION:?set GO_VERSION in toolchain.lock}
EOF
export GOFLAGS=-mod=mod
export CGO_ENABLED=1
(
  cd "$WORK/trim"
  go mod edit -require "github.com/rclone/rclone@$VERSION" -replace "github.com/rclone/rclone=$WORK/rclone"
  go mod tidy
)

build_abi() { # <goarch> <clang-prefix> <out-file>
  local goarch="$1" prefix="$2" out="$3"
  echo "== build $goarch"
  (cd "$WORK/trim" &&
    GOOS=android GOARCH="$goarch" CC="$LLVM/${prefix}-clang" CXX="$LLVM/${prefix}-clang++" \
      go build -ldflags="-s -w" -trimpath -o "$out" .)
}

build_abi arm64 "aarch64-linux-android$API" "$WORK/librclone_arm64.so"
# x86_64 is debug/emulator only: build it uncompressed like today.
build_abi amd64 "x86_64-linux-android$API" "$WORK/librclone_x86_64.so"

if [[ "$SKIP_UPX" -eq 0 ]]; then
  command -v upx >/dev/null || { echo "upx not found (want $UPX_VERSION)" >&2; exit 1; }
  upx --version | head -1 | grep -q "$UPX_VERSION" || echo "WARN: upx version != $UPX_VERSION"
  echo "== upx arm64 (release ABI)"
  upx --best "$WORK/librclone_arm64.so"
fi

echo "== smoke test (host binaries before packaging)"
# Host smoke binary: no C compiler guaranteed on the runner, and the smoke
# test exercises CLI surface only, so build it without cgo.
(cd "$WORK/trim" && GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build -o "$WORK/librclone_host" .)
smoke_test "$WORK/librclone_host"

mkdir -p "$OUT_DIR/arm64-v8a" "$OUT_DIR/x86_64"
cp "$WORK/librclone_arm64.so" "$OUT_DIR/arm64-v8a/librclone.so"
cp "$WORK/librclone_x86_64.so" "$OUT_DIR/x86_64/librclone.so"
ls -lh "$OUT_DIR/arm64-v8a/librclone.so" "$OUT_DIR/x86_64/librclone.so"

echo "== sync version records"
printf '%s\n' "$VERSION" > "$SCRIPT_DIR/pinned-version.txt"
plain_version="${VERSION#v}"
sed -i "s|^\(- \*\*Version:\*\* \).*|\1$plain_version|" "$REPO_ROOT/THIRD_PARTY.md"
echo "Done: $VERSION"
