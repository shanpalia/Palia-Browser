# Palia Browser

A modern Android browser with a built-in download manager featuring real-time download speed, progress, pause/resume where supported, background downloads and download history.

**App:** Palia Browser  
**Developer:** Shanpalia  
**Package:** `com.shanpalia.paliabrowser`  
**Copyright:** © Shanpalia

## Included

- Light theme by default
- Palia Browser launcher icon
- Palia Browser splash screen
- Browser tabs, history and bookmarks
- Download manager with progress and speed
- Pause/resume support where the server supports HTTP Range requests
- Background download service and notifications
- Codemagic release workflow (`codemagic.yaml`)

## GitHub

Upload the complete project to a GitHub repository. Do not upload keystores, passwords, API keys or `.env` files.

## Codemagic signing

Create a Codemagic Android keystore named `palia_browser_keystore` and configure the workflow's signing group as required by your Codemagic account. The Gradle release signing configuration accepts Codemagic variables (`CM_KEYSTORE_PATH`, `CM_KEYSTORE_PASSWORD`, `CM_KEY_ALIAS`, `CM_KEY_PASSWORD`).

The workflow builds:

- `app/build/outputs/apk/release/*.apk`
- `app/build/outputs/bundle/release/*.aab`

If you use Google Play App Signing, keep the upload keystore and Play signing key management inside Google Play Console/Codemagic rather than committing credentials to GitHub.

## Local development

Open the project in Android Studio and allow Gradle to sync. For a local debug build, use the Android Studio Run action or the Gradle `assembleDebug` task.

## Important

Release signing requires a real keystore and secure credentials. These are intentionally not included in this ZIP.
