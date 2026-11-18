# T3 Auto — Android Auto Receiver + Car Launcher (Allwinner T3 head unit)

Two lightweight apps built for your head unit (QUAD-CORE T3 k2001-nwd,
Allwinner sun8iw11p1, Cortex-A7 @ 1.2 GHz, Mali-400 MP, 1024×600 @ 160 dpi,
1 GB RAM, rooted). See [docs/DEVICE.md](docs/DEVICE.md) for the full spec
and [docs/PROTOCOL-NOTES.md](docs/PROTOCOL-NOTES.md) for what Android
Auto / CarPlay can and cannot do as a sideloaded app.

## What you get

| App | Package | What it does |
|---|---|---|
| **T3 Auto Receiver** (`receiver/`) | `com.t3.autoreceiver` | Auto-opens when a phone connects via **USB cable** or **Bluetooth**. Android-Auto-style UI (Home / Navigate / Music / Phone / Settings), AOA handshake, Bluetooth audio + calls, media controls, hotspot/mirror helpers. Foreground service keeps it alive while driving. |
| **T3 Car Launcher** (`launcher/`) | `com.t3.carlauncher` | Your home screen. Clock, big app tiles (Receiver, Navigate, Music, Phone, Radio, All apps), quick toggles (Wi-Fi, Bluetooth, hotspot, brightness). Hands over to the receiver automatically when a phone connects. |

**How the auto-open works:** plug in a phone (USB) or pair Bluetooth →
`UsbReceiver` / `BluetoothReceiver` fires → receiver opens and broadcasts
`PHONE_CONNECTED` → launcher receives it and opens the receiver. On boot,
`BootReceiver` starts the receiver service so this works immediately.

> **iPhone note:** true CarPlay needs Apple's MFi licence — it can't be a
> plain APK. Use a Carlinkit-style USB dongle for CarPlay (the receiver
> auto-opens when it's plugged in). Without a dongle, iPhones work for
> Bluetooth music/calls + USB charging.

## Build

### Option A — Android Studio (easiest)

1. Open this folder in Android Studio.
2. Let it sync (it generates the Gradle wrapper for you).
3. Run **receiver** and **launcher** on your head unit, or
   Build → Build APK(s).

### Option B — Command line

Requires JDK 17 + Android SDK (platform 34, build-tools 34).

```bash
# Generate the wrapper once (jar is not committed):
gradle wrapper --gradle-version 8.5

# Build both debug APKs:
./gradlew assembleDebug
# → receiver/build/outputs/apk/debug/receiver-debug.apk
# → launcher/build/outputs/apk/debug/launcher-debug.apk
```

### Option C — GitHub Actions (no local setup)

Push to this repo (or run the workflow manually) — the
`Build T3 Auto APKs` workflow builds both APKs and uploads them as
artifacts you can download and sideload.

## Install on the T3 head unit

```bash
adb install receiver/build/outputs/apk/debug/receiver-debug.apk
adb install launcher/build/outputs/apk/debug/launcher-debug.apk
```

1. Open **T3 Auto Receiver** once, grant Bluetooth/location permissions
   when asked. Leave **Auto-open** and **Start on boot** enabled.
2. Open **T3 Car Launcher**, then set it as default:
   Settings → Apps → T3 Car Launcher → *Set as default* (or press Home
   and choose it when prompted).
3. Pair your phone once in Bluetooth settings (for music + calls).
4. Plug in via USB or connect Bluetooth — the receiver opens by itself.

### Optional: install as system apps (rooted — survives factory reset)

```bash
adb root
adb remount
adb push receiver-debug.apk /system/app/T3AutoReceiver.apk
adb push launcher-debug.apk /system/app/T3CarLauncher.apk
adb reboot
```

## Usage

- **Android phone, USB:** plug in → receiver opens. For on-screen Maps,
  use *Screen mirror help* (Home tab) — hotspot + mirroring app, or
  scrcpy from a PC.
- **Any phone, Bluetooth:** pair once → music and hands-free calls route
  to the head unit; receiver opens on connect.
- **iPhone + Carlinkit dongle:** plug the dongle into USB → receiver
  opens → CarPlay runs through the dongle.
- **Music tab:** ▶/❚❚, ◀◀, ▶▶ send media keys to your phone.

## Troubleshooting (1 GB RAM head unit)

- **Receiver doesn't auto-open:** check Receiver → Settings → *Auto-open*
  is on, and that the service notification is showing. Some T3 firmwares
  kill background apps — the system-app install above fixes this.
- **Phone not detected over USB:** try a different cable/port (charge-only
  cables are the #1 cause). Add your phone's USB VID to
  `receiver/src/main/res/xml/device_filter.xml` if needed.
- **Out of memory / slow:** close unused apps; both apps are designed to
  run in a few MB. Avoid running a heavy Maps app *and* mirroring at once.
- **Bluetooth name missing:** grant location permission (required for
  Bluetooth on Android 13).

## Project layout

```
receiver/   T3 Auto Receiver app (USB/BT auto-open, AOA, Auto-style UI)
launcher/   T3 Car Launcher app (HOME dashboard, toggles, handoff)
docs/       Device spec + protocol notes
.github/workflows/build-apk.yml   CI: builds both APKs on every push
```

## Private use

Built for your own vehicle and personal use. Not an official Google
Android Auto or Apple CarPlay product.
