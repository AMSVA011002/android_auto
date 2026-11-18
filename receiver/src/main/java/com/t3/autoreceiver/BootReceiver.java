package com.t3.autoreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

/**
 * Starts the receiver service on boot so USB/Bluetooth auto-open works
 * immediately after the head unit powers on.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "T3BootReceiver";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (!ConnectionMonitor.isStartServiceEnabled(ctx)) {
            Log.i(TAG, "Start-on-boot disabled in settings — skipping");
            return;
        }
        Intent svc = new Intent(ctx, ReceiverService.class);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(svc);
            } else {
                ctx.startService(svc);
            }
            Log.i(TAG, "ReceiverService started on boot");
        } catch (Exception e) {
            Log.w(TAG, "Could not start ReceiverService on boot", e);
        }
    }
}
