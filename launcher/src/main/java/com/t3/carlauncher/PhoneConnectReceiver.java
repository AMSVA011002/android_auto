package com.t3.carlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Receives the receiver app's phone-connected broadcast and auto-opens
 * the receiver. This is the launcher <-> receiver handoff:
 * phone plugs in / pairs -> receiver broadcasts -> launcher opens receiver.
 */
public class PhoneConnectReceiver extends BroadcastReceiver {

    private static final String TAG = "T3LauncherConn";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        if ("com.t3.autoreceiver.PHONE_CONNECTED".equals(intent.getAction())) {
            Log.i(TAG, "Phone connected — opening receiver");
            try {
                Toast.makeText(ctx, "Phone connected — opening receiver…", Toast.LENGTH_SHORT).show();
            } catch (Exception ignored) {}
            try {
                Intent i = new Intent("com.t3.autoreceiver.LAUNCH_RECEIVER");
                i.setPackage("com.t3.autoreceiver");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(i);
            } catch (Exception e) {
                Log.w(TAG, "Receiver not installed or cannot launch", e);
            }
        }
    }
}
