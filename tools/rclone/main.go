// Trimmed rclone entry point for ElecPilot.
//
// Mirrors upstream rclone.go but imports only the backends and commands the
// app uses (see contract.md). Everything else (all other backends/commands)
// is left out to keep librclone.so small.
//
// The build script (build-librclone.sh) places this file in a temp module
// with a `replace github.com/rclone/rclone => <rclone checkout>` directive
// pinned to the version in pinned-version.txt, then `go mod tidy` + build.
package main

import (
	"github.com/rclone/rclone/cmd"
	_ "github.com/rclone/rclone/cmd/authorize"
	_ "github.com/rclone/rclone/cmd/config"
	_ "github.com/rclone/rclone/cmd/copyto"
	_ "github.com/rclone/rclone/cmd/delete"
	_ "github.com/rclone/rclone/cmd/deletefile"
	_ "github.com/rclone/rclone/cmd/listremotes"
	_ "github.com/rclone/rclone/cmd/lsf"
	_ "github.com/rclone/rclone/cmd/mkdir"

	_ "github.com/rclone/rclone/backend/drive"
	_ "github.com/rclone/rclone/backend/local"
	_ "github.com/rclone/rclone/backend/onedrive"
)

func main() {
	cmd.Main()
}
