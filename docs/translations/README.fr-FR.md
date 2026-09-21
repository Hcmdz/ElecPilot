<!-- Sync with EN rev a360a66 (2026-09-19) -->
# ElecPilot

[![ElecPilot](../../docs/assets/feature-graphic.png)](https://github.com/Hcmdz/ElecPilot/releases/latest)

**Lire ceci dans d'autres langues**

🇺🇸 [English](../../README.md) | 🇫🇷 [Français](README.fr-FR.md) | 🇸🇦 [العربية](README.ar-SA.md)
<!-- Localized app: values-fr/values-ar (+ldrtl) mirror README langs; supportsRtl verified. -->

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?logo=android)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?logo=kotlin)](https://kotlinlang.org)
[![MinSDK](https://img.shields.io/badge/MinSDK-29-orange.svg)](#)
[![TargetSDK](https://img.shields.io/badge/TargetSDK-36-blue.svg)](#)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Release](https://img.shields.io/github/v/release/Hcmdz/ElecPilot)](https://github.com/Hcmdz/ElecPilot/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/Hcmdz/ElecPilot/total)](https://github.com/Hcmdz/ElecPilot/releases)
[![Stars](https://img.shields.io/github/stars/Hcmdz/ElecPilot)](https://github.com/Hcmdz/ElecPilot/stargazers)
[![Forks](https://img.shields.io/github/forks/Hcmdz/ElecPilot)](https://github.com/Hcmdz/ElecPilot/network/members)
[![Compose M3](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

Application Android de gestion des démarreurs de moteurs électriques et des modules d'E/S d'automates (PLC) en environnement industriel.

Sur le terrain, les données moteurs vivent sur papier ou dans des tableurs éparpillés — perdues, obsolètes, introuvables. ElecPilot est le carnet de terrain offline-first : chaque démarreur et chaque module PLC enregistré une fois, retrouvé en quelques secondes, sauvegardé chiffré.

- **Package**: `com.HcmDz.ElecPilot`
- **Version**: 6.5.1 (versionCode 31)
- **Author**: HcmDZ &lt;[REDACTED]&gt;

---

## 📦 Téléchargements

Obtenez ElecPilot sur GitHub : **[Dernière release](https://github.com/Hcmdz/ElecPilot/releases/latest)** (`ElecPilot-release.apk`, ~21 MB, `arm64-v8a`).

[![Download](https://img.shields.io/badge/Download-Latest_Release-green.svg?logo=android)](https://github.com/Hcmdz/ElecPilot/releases/latest)

- Nécessite Android 9+ (API 29) avec **arm64-v8a** ; autorisez *Installer des applications inconnues* pour votre navigateur si demandé.
- Vérifiez l'intégrité : `sha256sum -c ElecPilot-release.apk.sha256` (fichier sidecar à côté de l'APK).
- ⚠️ Si vous venez d'une version ≤ 6.5 : réinstallation manuelle requise (nouvelle clé de signature depuis 6.5.1 — sauvegardez, désinstallez, installez, restaurez).

---

## Captures d'écran

Retrouvez n'importe quel démarreur en quelques secondes, en clair ou en sombre — listes, détails et réglages en un coup d'œil.

### Démarreurs de moteurs

| Mode clair | Mode sombre |
|---|---|
| [![Motor list — table view](../../screenshots/motor-list-table-view-light.png)](../../screenshots/motor-list-table-view-light.png) | [![Motor list — table view](../../screenshots/motor-list-table-view-dark.png)](../../screenshots/motor-list-table-view-dark.png) |
| [![Motor list — card view](../../screenshots/motor-list-card-view-light.png)](../../screenshots/motor-list-card-view-light.png) | |
| [![Motor detail](../../screenshots/motor-detail-dark.png)](../../screenshots/motor-detail-dark.png) | |

### Modules d'E/S PLC

Chaque module est enregistré — vue tableau ou cartes, avec le détail complet par module.

| Mode clair | Mode sombre |
|---|---|
| [![PLC list — table view](../../screenshots/plc-list-table-view-light.png)](../../screenshots/plc-list-table-view-light.png) | [![PLC list — card view](../../screenshots/plc-list-card-view-dark.png)](../../screenshots/plc-list-card-view-dark.png) |
| [![PLC list — card view](../../screenshots/plc-list-card-view-light.png)](../../screenshots/plc-list-card-view-light.png) | |
| [![PLC detail](../../screenshots/plc-detail-dark.png)](../../screenshots/plc-detail-dark.png) | |

### Réglages

Sauvegarde, langue et mises à jour — tout est configurable au même endroit.

| Mode clair | Mode sombre |
|---|---|
| [![Settings](../../screenshots/settings-light.png)](../../screenshots/settings-light.png) | [![Settings](../../screenshots/settings-dark.png)](../../screenshots/settings-dark.png) |

---

## Fonctionnalités clés

### Démarreurs de moteurs
- **Chaque démarreur est enregistré.** CRUD complet, recherche, édition par lot, statistiques.
- **Recherche mains-libres.** Parlez pour filtrer les démarreurs avec la recherche vocale.
- **Partez d'une feuille propre.** Modèles Excel prêts à remplir pour les démarreurs et les E/S PLC.

### Modules d'E/S PLC
- **Chaque module est enregistré.** Base de données et vues dédiées aux modules d'E/S PLC.

### Sauvegarde et synchronisation
- **Vos données survivent au téléphone.** Sauvegarde/restauration cloud via Google Drive et OneDrive avec rclone (config chiffrée AES-256-GCM).
- **Trace écrite à la demande.** Sauvegarde locale avec export/import Excel et CSV et sauvegardes planifiées.

### Application
- **Toujours à jour.** Mise à jour intégrée avec vérification auto et téléchargement depuis GitHub Releases.
- **Comme à la maison.** Thème Material You avec couleur dynamique, edge-to-edge.
- **Parle votre langue.** Localisation Système/EN/FR/AR avec détection auto de la langue de l'appareil.
- **Durcie par défaut.** FLAG_SECURE, config rclone chiffrée, allowlist d'URL WebView, suppression des logs ProGuard.

---

## 🛠️ Stack technique et architecture

### Architecture

- Application **monomodule** avec `MainActivity` + navigation Jetpack Compose
- Couche données : bases Room + pattern Repository
- ViewModels avec StateFlow
- WorkManager pour les sauvegardes planifiées (locales + cloud)

### Bibliothèques et outils principaux

| Catégorie | Bibliothèque | Version |
|---|---|---|
| **UI** | Jetpack Compose + Material 3 | BOM 2026.09.00 |
| **Activity** | Activity Compose | 1.13.0 |
| **Lifecycle** | Lifecycle Runtime Compose | 2.11.0 |
| **Async** | Kotlin Coroutines & Flow | 1.11.0 |
| **Database** | Room | 2.8.5 |
| **Networking** | OkHttp | 5.5.0 |
| **Cloud** | rclone (binaire natif, compressé UPX) | custom build |
| **Excel** | Apache POI (shadow jar de centic9/poi-on-android) | 5.2.5 |
| **Scheduling** | WorkManager | 2.11.2 |
| **File Access** | DocumentFile (SAF) | 1.1.0 |
| **Browser** | AndroidX Custom Tabs | 1.10.0 |
| **Security** | AES-256-GCM (Android KeyStore), ProGuard, NSC | — |
| **Build** | AGP 9.4.0, Kotlin 2.4.10, KSP 2.3.12 | — |
| **Lint** | Android Security Lint | 1.0.4 |
| **Quality** | Detekt CLI | 1.23.8 |
| **Testing** | JUnit4, Room Testing, Coroutines Test | 4.13.2 / 2.8.5 / 1.11.0 |

### Fonctionnalités de sécurité (v6.0)

| Contrôle | Implémentation |
|---|---|
| **Chiffrement au repos** | AES-256-GCM via Android KeyStore (StrongBox+TEE en repli) pour la config rclone et le cache cloud |
| **Protection captures d'écran** | `FLAG_SECURE` sur MainActivity |
| **Durcissement WebView** | Allowlist d'URL (fournisseurs OAuth uniquement), `allowFileAccess=false`, `mixedContent=NEVER_ALLOW` |
| **Suppression des logs** | ProGuard supprime `Log.d/v/i/e/w` (y compris les stack traces d'exceptions) en release |
| **Sécurité réseau** | Cleartext bloqué (NSC), exception localhost uniquement pour rclone OAuth |
| **Sauvegarde désactivée** | `allowBackup="false"` |
| **MTE** | `memtagMode="sync"` activé dans le manifest |

### Permissions

- `INTERNET`, `ACCESS_NETWORK_STATE` — sauvegarde cloud (rclone) et vérification des mises à jour
- `POST_NOTIFICATIONS` — progression des sauvegardes et statut des mises à jour
- `REQUEST_INSTALL_PACKAGES` — installation des mises à jour intégrées

Points d'entrée : `MainActivity` (launcher), `RcloneAuthActivity` (OAuth), `FileProvider` (`${applicationId}.fileprovider`).

### CI et qualité

- GitHub Actions : `.github/workflows/ci.yml` (`testDebugUnitTest` + `lintDebug`), `codeql.yml`, Dependabot
- Detekt : `./gradlew detekt` (config `config/detekt/detekt.yml`)

---

## 🚀 Démarrage

### Prérequis

- **Android Studio** : dernière stable (Meerkat ou plus récent)
- **JDK** : 17
- **Android SDK** : compileSdk 37
- **NDK** : 26.1.10909125
- **Go** : 1.22+ (pour le build rclone personnalisé)

### Build APK

```bash
cd "ElecPilot"
./gradlew assembleRelease
```

Sortie : `app/build/outputs/apk/release/ElecPilot-release.apk`

### Lancer sur appareil

1. Ouvrez le projet dans **Android Studio**.
2. Attendez la fin de la synchronisation Gradle.
3. Sélectionnez un appareil ou un émulateur sous **API 29+**.
4. Appuyez sur **Run** (Shift + F10) ou :
   ```bash
   ./gradlew installDebug
   ```

---

## 🧪 Tests

```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## 📁 Structure du projet

```
app/src/main/java/com/HcmDz/ElecPilot/
├── MainActivity.kt              # Single activity, edge-to-edge, FLAG_SECURE
├── data/
│   ├── db/                      # Room databases (Motor + PLC)
│   ├── repository/              # Data repositories
│   ├── BackupPreferences.kt     # Local backup settings models
│   ├── CloudBackupPreferences.kt # Cloud backup settings models
│   └── CloudBackupFileInfo.kt   # Cloud file metadata
├── ui/
│   ├── screens/                 # Compose screens (Main, MotorDetail, dialogs)
│   ├── viewmodel/               # ViewModels
│   ├── components/              # Reusable UI components
│   ├── theme/                   # Material 3 theming
│   └── views/plc/               # PLC-specific views
├── util/
│   ├── BackupManager.kt         # Local backup (Excel/CSV)
│   ├── CloudBackupManager.kt    # Cloud backup orchestration
│   ├── RcloneDriveService.kt    # rclone CLI wrapper
│   ├── RcloneAuthActivity.kt    # OAuth WebView flow
│   ├── CryptoManager.kt         # AES-256-GCM encryption
│   ├── ExcelUtil.kt             # Apache POI export/import + templates
│   ├── UpdateManager.kt         # In-app update (GitHub Releases)
│   ├── ContextUtils.kt          # Locale-aware context helpers
│   ├── NotificationHelper.kt    # Backup notifications
│   ├── BackupScheduler.kt       # WorkManager: local backup schedule
│   └── CloudBackupScheduler.kt  # WorkManager: cloud backup schedule
└── worker/
    ├── BackupWorker.kt          # WorkManager: local backup
    └── CloudBackupWorker.kt     # WorkManager: cloud backup
```

---

## Apache POI Shadow Jar

Apache POI est fourni comme shadow jar précompilé de [centic9/poi-on-android](https://github.com/centic9/poi-on-android) plutôt que comme dépendance Maven standard.

### Pourquoi un shadow jar ?

Le POI Maven brut + R8 (le minifieur Android) est fondamentalement cassé. L'obfuscation R8 casse le parsing des noms de classes `SchemaTypeSystemImpl` de xmlbeans, et le shrinking R8 génère des classes bridge synthétiques cassées. Le shadow jar résout cela en :

- Relocalisant `javax.xml.stream` → `org.apache.poi.javax.xml.stream` (Android n'a pas l'API StAX complète)
- Relocalisant `javax.xml.namespace` → `org.apache.poi.javax.xml.namespace`
- Remplaçant les classes `java.awt.*` manquantes par des stubs
- Embarquant l'implémentation StAX [aalto-xml](https://github.com/FasterXML/aalto-xml)
- Fusionnant toutes les dépendances POI en un seul jar

Le shadow jar se trouve à `app/libs/poishadow-all.jar` (~19.5 MB brut). R8 supprime les classes POI inutilisées dans les builds release.

### Propriétés système

Les parseurs StAX relocalisés nécessitent des propriétés système définies avant tout appel POI. C'est géré dans `ExcelUtil.kt` :

```kotlin
System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl")
System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl")
System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl")
System.setProperty("org.apache.poi.ss.ignoreMissingFontSystem", "true")
```

### Mettre à jour le shadow jar

Pour construire une version plus récente :

```bash
git clone https://github.com/centic9/poi-on-android.git
cd poi-on-android
./gradlew :poishadow:shadowJar
cp poishadow/build/libs/poishadow-all.jar <path-to-ElecPilot>/app/libs/
```

La branche master de centic9/poi-on-android utilise POI 5.5.1. La release précompilée (5.2.5-4) utilise POI 5.2.5. Les deux conviennent pour la lecture/écriture xlsx de base.

---

## Binaire Rclone personnalisé

L'app embarque un binaire rclone sur mesure (`librclone.so`) avec uniquement les backends et commandes nécessaires à la sauvegarde cloud. Cela garde le binaire petit.

### Backends inclus

- `local` — système de fichiers local (requis pour upload/download)
- `drive` — Google Drive
- `onedrive` — OneDrive

### Commandes incluses

- `authorize`, `config`, `copyto`, `delete`, `deletefile`, `listremotes`, `lsf`, `mkdir`

### Reconstruire rclone

**Important** : après avoir reconstruit le binaire rclone, vous DEVEZ le compresser avec UPX avant de le copier dans le projet. Sans UPX, l'APK fera ~17 MB de plus.

Les rebuilds sont scriptés — ne lancez pas de `go build` manuel (cela produirait un binaire complet non allégé). Le point d'entrée unique est :

```bash
./tools/rclone/build-librclone.sh [--version vX.Y.Z]
```

Le script clone le tag épinglé (voir `tools/rclone/pinned-version.txt`), construit le module allégé (`tools/rclone/main.go` : backends `local`/`drive`/`onedrive`, commandes du contrat dans `tools/rclone/contract.md`) pour `arm64-v8a` + `x86_64` (clang `android29`, voir `tools/rclone/toolchain.lock`), compresse avec UPX l'ABI release, lance le smoke test du contrat, copie les binaires dans `app/src/main/jniLibs/`, et synchronise la version dans `THIRD_PARTY.md`.

Prérequis : Go (voir `toolchain.lock`), Android NDK, UPX. En CI le workflow `.github/workflows/rclone.yml` fait de même chaque mois et ouvre une PR de bump — la fusionner exige la validation OAuth sur appareil décrite dans `contract.md`.

> **Note** : les builds release ne ciblent que `arm64-v8a`. Le build debug inclut `arm64-v8a` et `x86_64` pour les tests sur émulateur. Le binaire `x86_64` n'est volontairement PAS compressé avec UPX.

---

## Sauvegarde Cloud

La sauvegarde cloud utilise rclone comme exécutable CLI (via `ProcessBuilder`). Les tokens OAuth sont stockés chiffrés (AES-256-GCM via Android KeyStore) dans `rclone.conf.enc` dans le stockage interne de l'app. L'app communique avec rclone via stdout/stderr du processus.

### Flux OAuth

1. `RcloneAuthActivity` démarre rclone `authorize` en arrière-plan
2. rclone affiche une URL d'auth locale sur stderr
3. La WebView charge l'URL (hôtes allowlistés uniquement)
4. L'utilisateur s'authentifie auprès de Google/Microsoft
5. Le token est capturé depuis stdout rclone et sauvegardé chiffré

### Sécurité

- Fichier de config chiffré au repos avec AES-256-GCM (Android KeyStore)
- Fichiers de config temporaires lisibles par le propriétaire uniquement (`setReadable(true, true)`)
- Allowlist d'URL WebView restreignant le flux OAuth aux fournisseurs connus
- Le cache disque de la sauvegarde cloud est chiffré avant écriture

---

## Signature

L'APK release est signé avec un keystore. Pour construire un APK release :

1. Créez un fichier `gradle.properties` à la racine du projet (déjà gitignoré)
2. Ajoutez les propriétés suivantes :

```properties
RELEASE_STORE_FILE=/path/to/your/release.keystore
RELEASE_KEY_ALIAS=ALIAS
RELEASE_STORE_[REDACTED:password]
RELEASE_KEY_[REDACTED:password]
```

3. Lancez `./gradlew assembleRelease`

---

## Taille de l'APK

Taille actuelle de l'APK release : **~20 MB** (arm64 uniquement, R8 activé).

| Composant | Taille |
|---|---|
| `librclone.so` (compressé UPX) | ~6.5 MB |
| Classes POI shadow jar (après R8) | ~3 MB |
| Compose + AndroidX | ~5 MB |
| Autres (Room, OkHttp, etc.) | ~5.5 MB |

Techniques de réduction appliquées :
- Compression UPX sur le binaire natif rclone
- POI shadow jar + R8 pour supprimer les classes Apache POI/xmlbeans inutilisées
- `abiFilters` restreint à `arm64-v8a` pour les builds release
- `isMinifyEnabled = true` + `isShrinkResources = true` avec règles ProGuard de centic9/poi-on-android

---

## Changelog (v5.7 → v6.5)

### v6.5

- **Tâches de fond annulables sans erreur** — `CancellationException` relancée dans les chemins sauvegarde cloud/locale, vérification de mise à jour et import (plus de fausse notification d'échec ni de retry après annulation)
- **Écritures snapshot sur Main** — les dialogues de sauvegarde réassignent l'état UI sur le thread principal
- **Noms de fichiers de sauvegarde ASCII** — horodatages export/cloud en `Locale.US` (plus de chiffres non latins sous locale arabe)
- **Polish cartes PLC** — taille de l'étoile favori alignée sur les cartes moteurs (18.dp)
- **Build** — AGP 9.4.0, OkHttp 5.5.0, WorkManager 2.11.2

### v6.3

- **Plus d'écran blanc au démarrage à froid** — l'écran complet est conditionné à `hasLoadedOnce` pour ne jamais dessiner le shell vide (header avec compteur « 0 ») avant le premier chargement
- **Dernier module persistant** — l'app rouvre sur le dernier module actif (Départs / PLC I/O) après un redémarrage à froid

### v6.2

- **Mise à jour intégrée** — vérification auto des GitHub Releases, téléchargement et installation avec progression
- **Génération de modèles vierges** — modèles Excel prêts à remplir (headers EN/FR)
- **Option langue système** — détection auto de la langue de l'appareil à la première installation
- **Corrections** — gestion CancellationException, assainissement des noms de fichiers, seuils de validation des headers, garde de file d'export
- **Audit sécurité** — fonctionnalités mise à jour et modèles durcies (recommandation SHA-256, prévention path traversal)

### v6.0 → v6.1

Correctifs d'audit sécurité (OWASP MASVS 2.1) :

| # | Sévérité | Correctif |
|---|---|---|
| 1 | CRITIQUE | Fichier temp de config rclone : permissions lisibles par le propriétaire uniquement |
| 2 | HAUTE | Allowlist d'URL WebView OAuth contre le phishing |
| 4 | MOYENNE | Cache disque sauvegarde cloud chiffré (AES-256-GCM) |
| 5 | MOYENNE | `FLAG_SECURE` sur MainActivity (protection capture/enregistrement) |
| 7 | BASSE | Repli env HOME utilise `filesDir` au lieu d'un chemin en dur |
| 8 | BASSE | Usage explicite de SharedPreferences `MODE_PRIVATE` |
| 9 | BASSE | Logs d'erreur expurgés des chemins de fichiers et détails d'opérations |
| 10 | BASSE | ProGuard supprime `Log.e/w` avec objets d'exception en release |

---

## Mentions légales

- [Conditions d'utilisation](https://hcmdz.github.io/ElecPilot/terms/)
- [Politique de confidentialité](https://hcmdz.github.io/ElecPilot/privacy/)

---

## Licence

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

Ce projet est sous **GNU General Public License v3.0** — voir le fichier [LICENSE](../../LICENSE) pour les détails.

## Docs associées

- [Contributing](../../CONTRIBUTING.md) · [Composants tiers](../../THIRD_PARTY.md) · [Sécurité](../../SECURITY.md)
- [Politique de confidentialité](../../docs/privacy/) · [Conditions](../../docs/terms/)

---

Made with ❤️ by HcmDZ
