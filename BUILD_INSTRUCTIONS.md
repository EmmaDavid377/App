# Building EmmaDavid Nexus into an installable APK

This project is a native Android app (Kotlin + Jetpack Compose). It needs the
Android SDK + Gradle to compile, which isn't available in this chat's sandbox
(no access to Google's Android servers). Below are two ways to get a real,
installable `.apk` — pick whichever is easier for you.

## Option A — GitHub Actions (no Android Studio needed)

A workflow file is already included at `.github/workflows/android-build.yml`.
It builds a debug APK on GitHub's own servers (which have the Android SDK
preinstalled) every time you push, and uploads it as a downloadable artifact.

1. Create a new repository on GitHub (public or private, either works).
2. Push this project folder to it:
   ```bash
   cd emmadavid-nexus
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/<your-username>/<your-repo>.git
   git push -u origin main
   ```
3. (Optional, for AI features) Add your Gemini API key as a repo secret:
   - Go to **Settings → Secrets and variables → Actions → New repository secret**
   - Name: `GEMINI_API_KEY`, Value: your key from https://aistudio.google.com/apikey
   - If you skip this, the app still builds and runs — the AI guest-communication
     feature just won't be able to call Gemini until a key is provided.
4. Go to the **Actions** tab of your repo. The "Build Android APK" workflow
   runs automatically on push (or click **Run workflow** to trigger it manually).
5. When it finishes (green check), open the workflow run and download the
   `emmadavid-nexus-debug-apk` artifact — that's a zip containing your `.apk`.
6. Transfer the `.apk` to your Android phone (email, cloud drive, USB) and
   install it. You'll need to allow "install from unknown sources" for debug
   APKs not from the Play Store.

This produces a **debug** build. It's fully installable and functional, just
not signed for Play Store distribution.

## Option B — Android Studio (local build, release-ready)

1. Install [Android Studio](https://developer.android.com/studio).
2. **File → Open**, select this project folder.
3. Let Android Studio sync/download the SDK components it needs.
4. Create a `.env` file in the project root (see `.env.example`) with:
   ```
   GEMINI_API_KEY=your_actual_key_here
   ```
5. In `app/build.gradle.kts`, under `buildTypes { release { ... } }`, remove or
   comment out the line `signingConfig = signingConfigs.getByName("debugConfig")`
   if you want to produce a real release build signed with your own keystore
   (see the `signingConfigs { release { ... } }` block — set `KEYSTORE_PATH`,
   `STORE_PASSWORD`, `KEY_PASSWORD` env vars, or point `storeFile` at your own
   `.jks` file).
6. **Build → Build Bundle(s) / APK(s) → Build APK(s)**, or just click Run ▶
   to install directly onto a connected device/emulator.

## What this app does

EmmaDavid Nexus is a luxury event-coordination and live inventory dashboard
with an AI-assisted guest-communication screen (dashboard, logistics hub,
customer portal, add/edit flows) backed by a local Room database and the
Gemini API for AI responses.
