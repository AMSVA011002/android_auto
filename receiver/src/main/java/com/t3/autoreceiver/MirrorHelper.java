package com.t3.autoreceiver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.widget.Toast;

import java.lang.reflect.Method;

/**
 * Screen-mirror fallback helpers.
 *
 * On a rooted T3 the most reliable way to get the phone's screen (Maps,
 * music apps, CarPlay via dongle, etc.) onto the 1024x600 display is:
 *  1. USB: ADB / scrcpy-style mirroring from a PC, or a phone mirroring app.
 *  2. Wi-Fi: phone hotspot + a mirroring app (Miracast/Chromecast-style).
 *
 * This helper opens the relevant system screens and shows step-by-step
 * guidance so the driver is not stuck guessing.
 */
public final class MirrorHelper {

    private MirrorHelper() {}

    public static void showMirrorHelp(Context ctx) {
        String msg = "Screen mirror:\n"
                + "1) Android phone: enable USB debugging, connect USB, "
                + "then use scrcpy (PC) or a mirroring app over Wi-Fi.\n"
                + "2) iPhone: use a Carlinkit USB dongle for CarPlay, "
                + "or Bluetooth audio + USB charging.\n"
                + "3) Or turn on the hotspot (next button) and connect "
                + "the phone to this head unit's Wi-Fi.";
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
    }

    /** Opens the hotspot / tethering settings screen. */
    public static void openHotspotSettings(Context ctx) {
        try {
            Intent i = new Intent();
            i.setAction("android.settings.TETHER_SETTINGS");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(ctx, "Open Settings > Tethering & hotspot manually", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Best-effort hotspot enable via reflection (works on many T3 firmwares
     * where the settings screen is hard to reach while driving).
     * Returns true if the call was accepted.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean tryEnableHotspot(Context ctx) {
        try {
            WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            if (wm == null) return false;
            Method m = wm.getClass().getDeclaredMethod("startLocalOnlyHotspot",
                    Class.forName("android.net.wifi.WifiManager$LocalOnlyHotspotCallback"),
                    android.os.Handler.class);
            // Fire-and-forget: callback intentionally empty (API 26+ path).
            m.invoke(wm, null, null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void openMaps(Context ctx) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com"));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(ctx, "Maps not available on head unit", Toast.LENGTH_SHORT).show();
        }
    }

    public static void openDialer(Context ctx) {
        try {
            Intent i = new Intent(Intent.ACTION_DIAL);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(ctx, "Dialer not available", Toast.LENGTH_SHORT).show();
        }
    }

    /** Opens the companion car launcher (if installed). */
    public static void openLauncher(Context ctx) {
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setPackage("com.t3.carlauncher");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(i);
        } catch (Exception e) {
            Toast.makeText(ctx, "Car launcher not installed", Toast.LENGTH_SHORT).show();
        }
    }

    /** Sends Bluetooth media key events (play/pause/next/prev) to the phone. */
    public static void sendMediaKey(Context ctx, int keyCode) {
        try {
            Intent down = new Intent(Intent.ACTION_MEDIA_BUTTON);
            down.putExtra(Intent.EXTRA_KEY_EVENT,
                    new android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, keyCode));
            ctx.sendOrderedBroadcast(down, null);
            Intent up = new Intent(Intent.ACTION_MEDIA_BUTTON);
            up.putExtra(Intent.EXTRA_KEY_EVENT,
                    new android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, keyCode));
            ctx.sendOrderedBroadcast(up, null);
        } catch (Exception ignored) {}
    }

    public static boolean isAndroid13OrNewer() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    public static void keepScreenOn(Activity a, boolean on) {
        if (a != null && a.getWindow() != null) {
            if (on) a.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            else a.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
}
