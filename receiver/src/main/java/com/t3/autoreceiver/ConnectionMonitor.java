package com.t3.autoreceiver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Tracks the current phone connection (USB or Bluetooth) and notifies
 * both the receiver UI and the car launcher.
 *
 * The launcher listens for {@link #ACTION_PHONE_CONNECTED} so it can
 * hand over to the receiver automatically.
 */
public final class ConnectionMonitor {

    public static final String ACTION_PHONE_CONNECTED = "com.t3.autoreceiver.PHONE_CONNECTED";
    public static final String ACTION_PHONE_DISCONNECTED = "com.t3.autoreceiver.PHONE_DISCONNECTED";
    public static final String EXTRA_TYPE = "type";        // "usb" or "bt"
    public static final String EXTRA_DEVICE = "device";    // device name

    public static final String TYPE_USB = "usb";
    public static final String TYPE_BT = "bt";

    private static final String PREFS = "t3_receiver_prefs";
    private static final String KEY_AUTO_LAUNCH = "auto_launch";
    private static final String KEY_KEEP_SCREEN_ON = "keep_screen_on";
    private static final String KEY_START_SERVICE = "start_service";

    private ConnectionMonitor() {}

    public static void onPhoneConnected(Context ctx, String type, String deviceName) {
        Intent i = new Intent(ACTION_PHONE_CONNECTED);
        i.putExtra(EXTRA_TYPE, type);
        i.putExtra(EXTRA_DEVICE, deviceName == null ? "" : deviceName);
        // Explicit package so the launcher receives it without a global broadcast leak.
        i.setPackage("com.t3.carlauncher");
        ctx.sendBroadcast(i);
    }

    public static void onPhoneDisconnected(Context ctx) {
        Intent i = new Intent(ACTION_PHONE_DISCONNECTED);
        i.setPackage("com.t3.carlauncher");
        ctx.sendBroadcast(i);
    }

    public static boolean isAutoLaunchEnabled(Context ctx) {
        return prefs(ctx).getBoolean(KEY_AUTO_LAUNCH, true);
    }

    public static void setAutoLaunchEnabled(Context ctx, boolean v) {
        prefs(ctx).edit().putBoolean(KEY_AUTO_LAUNCH, v).apply();
    }

    public static boolean isKeepScreenOn(Context ctx) {
        return prefs(ctx).getBoolean(KEY_KEEP_SCREEN_ON, false);
    }

    public static void setKeepScreenOn(Context ctx, boolean v) {
        prefs(ctx).edit().putBoolean(KEY_KEEP_SCREEN_ON, v).apply();
    }

    public static boolean isStartServiceEnabled(Context ctx) {
        return prefs(ctx).getBoolean(KEY_START_SERVICE, true);
    }

    public static void setStartServiceEnabled(Context ctx, boolean v) {
        prefs(ctx).edit().putBoolean(KEY_START_SERVICE, v).apply();
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
