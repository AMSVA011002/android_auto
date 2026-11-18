package com.t3.autoreceiver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Tracks the current phone connection (USB or Bluetooth) and notifies
 * the dashboard + receiver UI inside this single app.
 *
 * A phone connection opens the projection engine (ZLink) when available, so
 * the driver gets genuine Android Auto; the in-app dashboard is the fallback.
 * On disconnect we return to the head unit's default launcher
 * (see {@link #isReturnHomeEnabled}).
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
    private static final String KEY_RETURN_HOME = "return_home";
    private static final String KEY_PREFER_ENGINE = "prefer_engine";

    private ConnectionMonitor() {}

    public static void onPhoneConnected(Context ctx, String type, String deviceName) {
        Intent i = new Intent(ACTION_PHONE_CONNECTED);
        i.putExtra(EXTRA_TYPE, type);
        i.putExtra(EXTRA_DEVICE, deviceName == null ? "" : deviceName);
        // Same app now — deliver to our own package only.
        i.setPackage(ctx.getPackageName());
        ctx.sendBroadcast(i);
    }

    public static void onPhoneDisconnected(Context ctx) {
        Intent i = new Intent(ACTION_PHONE_DISCONNECTED);
        i.setPackage(ctx.getPackageName());
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

    /**
     * When true (default), a phone connection opens the head unit's projection
     * engine (ZLink → genuine Android Auto) instead of our own dashboard.
     */
    public static boolean isPreferEngine(Context ctx) {
        return prefs(ctx).getBoolean(KEY_PREFER_ENGINE, true);
    }

    public static void setPreferEngine(Context ctx, boolean v) {
        prefs(ctx).edit().putBoolean(KEY_PREFER_ENGINE, v).apply();
    }

    /**
     * Single entry point used by every trigger: prefer the real projection
     * engine, fall back to our dashboard when none is installed or the user
     * turned the preference off.
     */
    public static void openProjection(Context ctx) {
        if (isPreferEngine(ctx) && ProjectionBridge.open(ctx)) return;
        try {
            Intent i = new Intent(ctx, LauncherActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            ctx.startActivity(i);
        } catch (Exception ignored) {}
    }

    /** When true (default), disconnect returns the user to the default launcher. */
    public static boolean isReturnHomeEnabled(Context ctx) {
        return prefs(ctx).getBoolean(KEY_RETURN_HOME, true);
    }

    public static void setReturnHomeEnabled(Context ctx, boolean v) {
        prefs(ctx).edit().putBoolean(KEY_RETURN_HOME, v).apply();
    }

    private static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
