package com.t3.carlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Ensures the launcher is ready after boot. The HOME intent-filter does the
 * real work (Android shows the launcher as home screen); this receiver just
 * logs boot completion for diagnostics.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "T3LauncherBoot";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Log.i(TAG, "Boot completed — launcher ready as HOME");
    }
}
