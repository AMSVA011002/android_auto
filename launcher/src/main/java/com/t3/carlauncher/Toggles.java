package com.t3.carlauncher;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.widget.Toast;

/**
 * Quick-toggle helpers for the launcher dashboard.
 * All toggles are best-effort: T3 firmwares vary, so every action falls
 * back to opening the matching system settings screen.
 */
public final class Toggles {

    private Toggles() {}

    public static void toggleWifi(Context ctx) {
        try {
            WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                boolean on = wm.isWifiEnabled();
                wm.setWifiEnabled(!on);
                Toast.makeText(ctx, on ? "Wi-Fi off" : "Wi-Fi on", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception ignored) {}
        openSettings(ctx, Settings.ACTION_WIFI_SETTINGS);
    }

    @SuppressWarnings("deprecation")
    public static void toggleBluetooth(Context ctx) {
        try {
            BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
            if (bt != null) {
                boolean on = bt.isEnabled();
                if (on) bt.disable();
                else bt.enable();
                Toast.makeText(ctx, on ? "Bluetooth off" : "Bluetooth on", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception ignored) {}
        openSettings(ctx, Settings.ACTION_BLUETOOTH_SETTINGS);
    }

    public static void openHotspot(Context ctx) {
        try {
            Intent i = new Intent();
            i.setAction("android.settings.TETHER_SETTINGS");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(i);
        } catch (Exception e) {
            openSettings(ctx, Settings.ACTION_WIRELESS_SETTINGS);
        }
    }

    /** Cycles screen brightness between low / medium / high. */
    public static void cycleBrightness(Context ctx) {
        try {
            int mode = Settings.System.getInt(ctx.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(ctx.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                Settings.System.putInt(ctx.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS, 120);
                Toast.makeText(ctx, "Brightness: low", Toast.LENGTH_SHORT).show();
                return;
            }
            int b = Settings.System.getInt(ctx.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
            int next = b < 100 ? 120 : (b < 200 ? 255 : 60);
            Settings.System.putInt(ctx.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, next);
            String label = next <= 120 ? "low" : (next < 255 ? "medium" : "high");
            Toast.makeText(ctx, "Brightness: " + label, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            openSettings(ctx, Settings.ACTION_DISPLAY_SETTINGS);
        }
    }

    private static void openSettings(Context ctx, String action) {
        try {
            Intent i = new Intent(action);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(i);
        } catch (Exception ignored) {}
    }
}
