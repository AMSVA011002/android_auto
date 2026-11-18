package com.t3.autoreceiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Auto-opens the receiver when a phone connects over Bluetooth
 * (A2DP music / HFP hands-free). This is how Android phones and iPhones
 * connect for audio + calls without any dongle.
 */
public class BluetoothReceiver extends BroadcastReceiver {

    private static final String TAG = "T3BtReceiver";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        String action = intent.getAction();

        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String name = "Bluetooth phone";
            try {
                if (device != null && device.getName() != null) name = device.getName();
            } catch (SecurityException ignored) {
                // BLUETOOTH_CONNECT not granted yet — use generic name.
            }
            Log.i(TAG, "Bluetooth connected: " + name);
            ConnectionMonitor.onPhoneConnected(ctx, ConnectionMonitor.TYPE_BT, name);
            if (ConnectionMonitor.isAutoLaunchEnabled(ctx)) {
                launchReceiver(ctx);
            }
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            Log.i(TAG, "Bluetooth disconnected");
            ConnectionMonitor.onPhoneDisconnected(ctx);
        } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            Log.i(TAG, "Bluetooth adapter state: " + state);
        }
    }

    private void launchReceiver(Context ctx) {
        try {
            Intent i = new Intent(ctx, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            ctx.startActivity(i);
        } catch (Exception e) {
            Log.w(TAG, "Could not auto-launch receiver", e);
        }
    }
}
