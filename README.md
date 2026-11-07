# Text Recognition (Android)

An Android app that takes a photo with the camera and pulls the text out of it
using Google ML Kit's on-device text recognition. It works offline because the
recognition model is bundled into the app.

## How it works

1. **Home screen** – tap **Capture** to open the scanner.
2. **Scanner screen**
   - **Snap** asks for camera permission (only the first time), then opens the camera.
     If you said no before, it explains why the camera is needed and asks again; if
     you chose "Don't ask again", it offers a shortcut to the app's Settings page.
   - **Detect** runs ML Kit text recognition on the photo and shows the text,
     which you can select and copy.

## Project layout

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/textrecognition/
│   ├── MainActivity.java      # welcome screen
│   └── ScannerActivity.java   # camera capture + ML Kit OCR
└── res/
    ├── layout/activity_main.xml
    ├── layout/activity_scanner.xml
    ├── drawable/              # button background, logo
    └── values/                # colors, strings, theme
```

## Build

You need JDK 17+ and the Android SDK (platform 34). The easiest way is to open
the folder in Android Studio and press **Run**. From the command line:

```bash
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. To install it
on a connected device:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Tech

- Java, AndroidX AppCompat, Material Components
- `com.google.mlkit:text-recognition` (Latin script, bundled model)
- minSdk 21, targetSdk 34
