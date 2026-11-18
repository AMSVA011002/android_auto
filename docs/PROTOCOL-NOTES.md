# How this app delivers real Android Auto on the NWD / Nuoweida T3

## The key fact about this head unit

Your unit (NUOWEIDA-QUAD-CORE T3 k2001-nwd, firmware
`V5.3.49#834FD030_WLA`, Zjinnova "Ultimate") ships **ZLink**
(`com.zjinnova.zlink`) pre-installed. ZLink *is* the Android Auto / CarPlay
receiver on this platform: it holds the Google-signed credentials, speaks the
real Android Auto Protocol to the phone, and draws the Google UI you see in
the reference screenshot — Maps, media card, message card, notification dots,
the left-hand rail with nav/music/phone buttons.

A sideloaded APK cannot reproduce that screen. The head-unit half of the
protocol is proprietary and the phone only enters Android Auto mode for a
licensed receiver. Any "Android Auto APK" that claims otherwise is either
wrapping ZLink or mirroring the phone's screen.

## What T3 Auto does instead

**It drives ZLink for you, and stays out of its way.**

1. Detects the phone connection (USB attach, or Bluetooth ACL connect).
2. Opens **ZLink** full-screen — Android Auto appears on the 1024×600 screen.
3. Never touches the USB device.

That last point is the important fix. The previous build performed an Android
Open Accessory (AOA) handshake and called `openAccessory()` on attach. Claiming
the device makes the phone think it is talking to an *unlicensed* accessory, so
it leaves Android Auto mode — and ZLink then has nothing to display. That is
exactly the "app opens but doesn't work" symptom: our app and ZLink were both
competing for the same USB endpoint, and ours won.

The AOA helper has been deleted, the `USB_DEVICE_ATTACHED` intent-filter has
been removed from our manifest (so Android never shows an app chooser or hands
us the device), and the app now only listens to the attach broadcast.

## Launch path

`ProjectionBridge` tries, in order:

1. ZLink's documented action intent:
   `zjinnova.android.intent.action.ZLINK_MAIN` with `page=LINK`,
   `feature=link_mode` (Android Auto mode).
2. `getLaunchIntentForPackage("com.zjinnova.zlink")`.
3. An explicitly resolved `MAIN`/`CAR_LAUNCHER` (then `LAUNCHER`) activity,
   falling back to `com.zjinnova.android.zlink.features.main.MainActivity`.
4. App details page, so the app is one tap away.

Other engines are probed too (`com.xyauto.zlink`, `com.carlink.app`,
`net.easyconn`, Carlinkit-style apps) in case a unit has a different factory
engine.

## Apple CarPlay

Same mechanism: ZLink handles CarPlay on units that are licensed for it, which
this "Ultimate" firmware advertises. No extra dongle is needed if your ZLink
build has CarPlay enabled — plug in the iPhone and tap **Android Auto** (the
tile opens ZLink, which picks the right mode). A Carlinkit dongle is only
required if your ZLink licence is Android-Auto-only.

## If ZLink does not open

1. Confirm ZLink is installed and enabled:
   `adb shell pm list packages | grep zjinnova` → `package:com.zjinnova.zlink`
2. Test it by hand: `adb shell am start -a zjinnova.android.intent.action.ZLINK_MAIN --es page LINK`
3. If that fails, open ZLink from the head unit's own app grid and check that
   the "Ultimate" licence is still activated (ZLink keeps its activation in
   `/data/data/com.zjinnova.zlink/` — back that up before any factory reset).
4. Wired Android Auto needs a **data** cable. Charge-only cables are the most
   common cause of "nothing happens".

## If you remove ZLink — read this first

Uninstalling ZLink does **not** make T3 Auto draw Android Auto by itself. A
sideloaded APK cannot: the head-unit side needs a licensed, Google-signed
receiver. Removing ZLink therefore changes *nothing* about projection — it only
removes the thing that currently works.

Two things you lose with it:

- **CarPlay.** No open-source alternative exists (Apple MFi). CarPlay is gone
  unless you add a Carlinkit-class dongle.
- **Your paid activation.** ZLink keeps its licence in
  `/data/data/com.zjinnova.zlink/`. Some NWD/MCU firmware even depends on it for
  phone-link entry points. Back that folder up before you touch anything.

### The legitimate replacement: Open Headunit

[Open Headunit](https://github.com/andreknieriem/headunit-revived)
(`com.andrerinas.headunitrevived`) is a genuine Android Auto *receiver* — it
speaks the real protocol to the phone and renders Google's car UI, the same
thing ZLink does. It is open source, runs on Android 4.1+, supports USB-OTG and
ships an `armeabi-v7a` build, which suits this Cortex-A7 T3.

T3 Auto already probes for it: install Open Headunit and the same auto-open
handoff launches it instead of ZLink. Wireless (in addition to USB) on Android
Auto 17.4+ needs its "Headunit Server" option or the root workaround described
in its README.

Caveats: it is a community project, so expect to grant USB permission and to
tune settings for a 1024×600 screen. It cannot do CarPlay.

**Recommended order:** keep ZLink, use T3 Auto as the auto-opener (it works
today, CarPlay included). Only move to Open Headunit if ZLink fails or its
licence ends.

## Private use

Built for your own vehicle and personal use. ZLink, Android Auto, CarPlay and
the related marks belong to their owners.
