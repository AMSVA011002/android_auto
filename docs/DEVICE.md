# Target device (from CPU-Z photos)

| Item | Value |
|---|---|
| Model | QUAD-CORE T3 k2001-nwd (K2001N_NWD_S212909) |
| Manufacturer | Allwinner |
| Board | exdroid |
| Hardware | sun8iw11p1 (Allwinner T3) |
| CPU | ARM Cortex-A7 quad-core, 1.20 GHz (480 MHz – 1.20 GHz) |
| GPU | ARM Mali-400 MP |
| Kernel | 3.10.65 (20231123), armv7l |
| Android | 13 (reported) |
| Display | 1024 × 600, 160 dpi |
| RAM | 1024 MB total (~29 MB free in photo — very tight) |
| Storage | 32 GB (19.65 GB free) |
| Root | Yes |

## Design consequences

- **1 GB RAM** → pure-framework UI, no Compose, no heavy libraries, no
  background bloat. Both APKs are small and start fast.
- **1024×600 landscape, 160 dpi** → landscape-only activities, 64dp+ touch
  targets, large text (readable while driving).
- **Rooted** → optional system-app install (`/system/app`) so the apps
  survive factory resets. See README § Install.
- **minSdk 21** → the apps still install if the firmware base is older than
  the reported Android 13 (common on T3 clones).
