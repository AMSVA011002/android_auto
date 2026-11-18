# T3 Auto — one app for your Allwinner T3 head unit

A single app built for your head unit (QUAD-CORE T3 k2001-nwd,
Allwinner sun8iw11p1, Cortex-A7 @ 1.2 GHz, Mali-400 MP, 1024×600 @ 160 dpi,
1 GB RAM, rooted). See [docs/DEVICE.md](docs/DEVICE.md) for the full spec
and [docs/PROTOCOL-NOTES.md](docs/PROTOCOL-NOTES.md) for what Android
Auto / CarPlay can and cannot do as a sideloaded app.

## What you get — one app, package `com.t3.autoreceiver`

| Screen | When it shows |
|---|---|
| **Android Auto (ZLink)** | Opens **automatically when a phone connects**. This is the real Android Auto UI — Maps, media, messages — because your unit ships ZLink (`com.zjinnova.zlink`) as its licensed projection engine. |
| **Dashboard** (clock, big tiles, quick toggles) | Fallback when no projection engine is installed. |
| **Controls** (Auto-style Home / Navigate / Music / Phone / Settings) | Manual fallback from the dashboard's *My Dashboard* tile. |

**Read [docs/PROTOCOL-NOTES.md](docs/PROTOCOL-NOTES.md) first.** It explains why
this app opens ZLink rather than trying to draw Android Auto itself — and why
the previous build's USB handshake was stopping Android Auto from working.

**Your head unit's own launcher stays the default home screen.** This app
never registers as `CATEGORY_HOME`, so it is not a replacement home screen:

- Phone connects (USB or Bluetooth) → dashboard opens automatically.
- Phone disconnects → the app hands the screen back to the built-in
  launcher (Settings → *Return to default launcher when phone disconnects*,
  on by default).
- Nothing connected → the built-in launcher is what you see, as normal.

> **iPhone note:** CarPlay is handled by ZLink too, on units whose ZLink licence
> includes it (this "Ultimate" firmware does). Plug in the iPhone and the same
> handoff opens ZLink, which switches to CarPlay. If your ZLink build is
> Android-Auto-only, a Carlinkit-style dongle provides CarPlay.

## Build

### Option A — Android Studio (easiest)

1. Open this folder in Android Studio.
2. Let it sync (it generates the Gradle wrapper for you).
3. Run **app** on your head unit, or Build → Build APK(s).

### Option B — Command line

Requires JDK 17 + Android SDK (platform 34, build-tools 34).

```bash
# Generate the wrapper once (jar is not committed):
gradle wrapper --gradle-version 8.5

./gradlew assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```

### Option C — Prebuilt APK (no setup at all)

The latest build is checked in at [`build-output/t3-auto-debug.apk`](build-output/t3-auto-debug.apk)
and mirrored to Google Drive:
<https://drive.google.com/file/d/1hog9geodUu-oQds_t9FbQwslyqd8AIJZ/view>

It is also produced on every push by the `Build T3 Auto APK` workflow
(`t3-auto` artifact).

## Install on the T3 head unit

```bash
# download build-output/t3-auto-debug.apk (or the Drive link above), then:
adb install t3-auto-debug.apk
```

1. Open **T3 Auto** once and grant Bluetooth/location permissions.
   Leave **Auto-open** and **Start on boot** enabled.
2. Pair your phone once in the head unit's Bluetooth settings
   (for music + calls).
3. Plug in via USB or connect Bluetooth — the dashboard opens by itself.
4. Unplug / disconnect — you're returned to your normal head unit launcher.

### Optional: install as a system app (rooted — survives factory reset)

```bash
adb root
adb remount
adb push app-debug.apk /system/app/T3Auto.apk
adb reboot
```

## Usage

- **Android phone, USB:** plug in with a *data* cable → Android Auto opens by
  itself (via ZLink). No mirroring, no phone screen copying — the phone renders
  the car UI and the head unit shows it, exactly like the reference screenshot.
- **Any phone, Bluetooth:** pair once → music and hands-free calls route
  to the head unit; dashboard opens on connect.
- **iPhone + Carlinkit dongle:** plug the dongle into USB → dashboard
  opens → CarPlay runs through the dongle.
- **Receiver → Music tab:** ▶/❚❚, ◀◀, ▶▶ send media keys to your phone.

## Troubleshooting (1 GB RAM head unit)

- **Nothing opens on connect:** check Controls → Settings → *Open Android Auto
  (ZLink) when a phone connects* is on, and that ZLink itself launches by hand
  (`adb shell am start -a zjinnova.android.intent.action.ZLINK_MAIN --es page LINK`).
- **ZLink opens but shows "no device":** the phone has not entered Android Auto
  mode — use a data cable, unlock the phone, and accept the Android Auto prompt.
- **Dashboard doesn't auto-open:** verify the service notification is showing.
  Some T3 firmwares kill background apps — the system-app install above fixes this.
- **Phone not detected over USB:** try a different cable/port (charge-only
  cables are the #1 cause). Add your phone's USB VID to
  `app/src/main/res/xml/device_filter.xml` if needed.
- **Out of memory / slow:** close unused apps; the app is designed to run
  in a few MB. Avoid running a heavy Maps app *and* mirroring at once.
- **Bluetooth name missing:** grant location permission (required for
  Bluetooth on Android 13).
- **Don't want it to leave on disconnect:** uncheck *Return to default
  launcher when phone disconnects* in Receiver → Settings.

## Project layout

```
app/        The single T3 Auto app (dashboard, receiver UI, USB/BT detection)
build-output/  Built APK, refreshed by CI on every push
docs/       Device spec + protocol notes
.github/workflows/build-apk.yml   CI: builds the APK on every push
```

## Private use

Built for your own vehicle and personal use. Not an official Google
Android Auto or Apple CarPlay product.
