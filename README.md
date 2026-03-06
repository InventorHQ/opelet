# opelet

A self-updating APK manager for Android that pulls releases directly from GitHub. No Play Store. No middlemen.

App zero: the thing that installs everything else.

## Install

Download the latest APK from [Releases](https://github.com/InventorHQ/opelet/releases/latest) and sideload it. That's the only manual step — opelet manages its own updates after that.

## What it does

Paste a GitHub repo URL. opelet fetches all releases, defaults to the latest stable, downloads the APK, and hands it to Android's package installer. It tracks what you have installed, what's available, and lets you pick any version — not just the latest.

- **Add apps** by pasting `owner/repo` or a full GitHub URL
- **Browse releases** with release notes for any tracked app
- **Install any version**, including downgrades and pre-releases
- **Pin versions** to opt out of update notifications
- **Background checks** every 6 hours via WorkManager
- **Self-updating** — opelet's own repo is tracked automatically

## Design

Black and white. Monospace. No decoration. Every pixel is content or structure.

- Monochrome palette with light and dark mode
- JetBrains Mono throughout
- No Material Design defaults — no Roboto, no ripples, no FABs, no elevation
- Thin 1–2px borders, outlined buttons, instant transitions

## Technical

- Kotlin + Jetpack Compose, single-activity MVVM
- Room for persistence, WorkManager for background checks
- `HttpURLConnection` for networking — no Retrofit, no Ktor
- `org.json` for parsing — ships with Android, zero added dependency
- minSdk 29, targetSdk 35

### Dependencies

7 total, all from AndroidX:

| Dependency         | Why                               |
| ------------------ | --------------------------------- |
| core-ktx           | Kotlin extensions for Android SDK |
| lifecycle-\*       | ViewModel + StateFlow             |
| activity-compose   | setContent, enableEdgeToEdge      |
| compose (BOM)      | UI framework                      |
| navigation-compose | Screen routing                    |
| room               | Local database                    |
| work-runtime-ktx   | Background update checks          |

## Build

```
./gradlew assembleDebug
```

The debug APK lands in `app/build/outputs/apk/debug/`. Release builds are signed in CI and published to GitHub Releases on every push to `main`.

## License

MIT
