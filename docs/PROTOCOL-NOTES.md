# What "Android Auto receiver" can and cannot do as a sideloaded app

## The honest picture

- **Android Auto (head-unit side)** speaks the Android Auto Protocol (AAP)
  over USB/Bluetooth/Wi-Fi. Google does not publish a head-unit SDK for
  third-party sideloaded apps — a full AAP receiver is a large proprietary
  implementation, not a weekend project. This repo implements the parts
  that *are* open and practical:
  - USB auto-detection + Android Open Accessory (AOA) handshake
    (`AoaHelper`) so the phone can identify the head unit.
  - Bluetooth A2DP (music) + HFP (hands-free calls) — the standard audio
    paths every Android phone and iPhone already supports.
  - An Android-Auto-style UI (Navigate / Music / Phone) with media-key
    transport controls sent to the phone.
  - Screen-mirror fallback (hotspot + mirroring guidance) for on-screen
    Maps/apps.

- **Apple CarPlay** requires Apple's MFi licensing and their proprietary
  protocol. It cannot be implemented as a plain sideloaded APK. For true
  CarPlay on this head unit, use a Carlinkit-style USB dongle (the receiver
  auto-opens when the dongle is plugged in — see `device_filter.xml`).
  Without a dongle, iPhones still work for Bluetooth audio/calls + USB
  charging.

## What you get out of the box

1. Plug in an Android phone via USB → receiver auto-opens.
2. Pair any phone (Android or iPhone) via Bluetooth → receiver auto-opens,
   music + calls route to the head unit.
3. The launcher is your home screen: clock, app tiles, Wi-Fi/Bluetooth/
   hotspot/brightness toggles, and automatic handoff to the receiver.

## Private use

Built for your own vehicle and personal use. Do not redistribute as an
official "Android Auto" / "CarPlay" product — those are Google/Apple
trademarks and protocols.
