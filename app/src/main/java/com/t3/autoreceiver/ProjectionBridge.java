package com.t3.autoreceiver;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import java.util.List;

/**
 * Bridge to the head unit's factory projection engine.
 *
 * The NWD/Nuoweida T3 ships <b>ZLink</b> ({@code com.zjinnova.zlink}) which is
 * the real Android Auto / CarPlay receiver: it talks the genuine Android Auto
 * Protocol to the phone and renders the Google UI seen in the reference photo
 * (Google Maps, media cards, message cards).
 *
 * This app therefore does NOT try to reimplement projection (a sideloaded APK
 * cannot: the head-unit side of the protocol is proprietary and the phone only
 * enters AA mode for a licensed, Google-signed receiver). Instead it:
 *
 *  - detects the phone connection (USB / Bluetooth),
 *  - opens ZLink (or another projection engine) on the big screen,
 *  - and gets out of the way — it never performs an AOA handshake, never opens
 *    the USB device and never claims the accessory. Competing for the USB
 *    device is what stopped the phone from entering Android Auto.
 */
public final class ProjectionBridge {

    private static final String TAG = "T3Projection";

    /** Projection engines seen on Chinese head units, in preference order. */
    private static final String[][] ENGINES = {
            // package, activity/direct component (null = resolve launcher activity)
            {"com.zjinnova.zlink", null},
            {"com.xyauto.zlink", null},
            {"com.zjinnova.zlink5", null},
            {"com.carlink.app", null},
            {"com.carplay.auto", null},
            {"net.easyconn", null},
            {"com.syu.carplay", null},
    };

    /** ZLink accepts an explicit page/feature extra; LINK = Android Auto. */
    private static final String ZLINK_ACTION = "zjinnova.android.intent.action.ZLINK_MAIN";

    private ProjectionBridge() {}

    /** True when at least one projection engine is installed. */
    public static boolean isEngineInstalled(Context ctx) {
        return findEngine(ctx) != null;
    }

    /** Package name of the best available engine, or null. */
    public static String enginePackage(Context ctx) {
        return findEngine(ctx);
    }

    /** Human-readable engine name for the UI, or null when none is installed. */
    public static String engineLabel(Context ctx) {
        String pkg = findEngine(ctx);
        if (pkg == null) return null;
        if (pkg.startsWith("com.zjinnova")) return "ZLink";
        if (pkg.contains("carlink")) return "CarLink";
        if (pkg.contains("easyconn")) return "EasyConnection";
        if (pkg.contains("carplay")) return "CarPlay";
        return pkg;
    }

    /**
     * Opens the projection engine full-screen, preferring Android Auto mode.
     * Returns true when an activity was successfully started.
     */
    public static boolean open(Context ctx) {
        String pkg = findEngine(ctx);
        if (pkg == null) {
            Log.w(TAG, "No projection engine installed");
            return false;
        }

        // 1) ZLink's documented entry point: an explicit action with page=LINK.
        if (pkg.startsWith("com.zjinnova") && startZlink(ctx, pkg)) {
            Log.i(TAG, "Opened " + pkg + " via " + ZLINK_ACTION);
            return true;
        }

        // 2) Plain launcher intent for the package.
        try {
            Intent i = ctx.getPackageManager().getLaunchIntentForPackage(pkg);
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                ctx.startActivity(i);
                Log.i(TAG, "Opened " + pkg + " via launcher intent");
                return true;
            }
        } catch (Exception e) {
            Log.w(TAG, "Launcher intent failed for " + pkg, e);
        }

        // 3) Resolve MAIN/LAUNCHER or MAIN/CAR_LAUNCHER activity explicitly.
        ComponentName cn = resolveMainActivity(ctx, pkg);
        if (cn != null) {
            try {
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setComponent(cn);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(i);
                Log.i(TAG, "Opened " + cn.flattenToShortString());
                return true;
            } catch (Exception e) {
                Log.w(TAG, "Explicit component failed: " + cn, e);
            }
        }

        // 4) Last resort: open the app's store/settings page so the user can tap it.
        try {
            Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + pkg));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(i);
        } catch (Exception ignored) {}
        return false;
    }

    private static boolean startZlink(Context ctx, String pkg) {
        try {
            Intent i = new Intent(ZLINK_ACTION);
            i.setPackage(pkg);
            i.putExtra("page", "LINK");
            i.putExtra("feature", "link_mode");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            ctx.startActivity(i);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "ZLink action intent failed", e);
            return false;
        }
    }

    private static String findEngine(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        for (String[] engine : ENGINES) {
            String pkg = engine[0];
            try {
                pm.getPackageInfo(pkg, 0);
                return pkg;
            } catch (Exception ignored) {
                // not installed — try the next candidate
            }
        }
        return null;
    }

    private static ComponentName resolveMainActivity(Context ctx, String pkg) {
        PackageManager pm = ctx.getPackageManager();
        String[] actions = {Intent.ACTION_MAIN};
        String[] categories = {"android.intent.category.CAR_LAUNCHER",
                Intent.CATEGORY_LAUNCHER, "android.intent.category.CAR_APP"};
        for (String action : actions) {
            for (String category : categories) {
                Intent probe = new Intent(action).addCategory(category).setPackage(pkg);
                List<ResolveInfo> list = pm.queryIntentActivities(probe, 0);
                if (list != null && !list.isEmpty()) {
                    ResolveInfo ri = list.get(0);
                    return new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
                }
            }
        }
        // Known ZLink component as a documented fallback.
        if (pkg.startsWith("com.zjinnova")) {
            return new ComponentName(pkg, "com.zjinnova.android.zlink.features.main.MainActivity");
        }
        return null;
    }
}
