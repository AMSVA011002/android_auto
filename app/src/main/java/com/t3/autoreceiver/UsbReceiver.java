package com.t3.autoreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

/**
 * Auto-opens the dashboard when a phone (or Carlinkit dongle) is plugged in.
 * Also handles the AOA handshake so the phone can identify this head unit.
 * The dashboard only runs while connected — on detach the activities
 * return to the head unit's default launcher.
 */
public class UsbReceiver extends BroadcastReceiver {

    private static final String TAG = "T3UsbReceiver";
    public static final String ACTION_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        String action = intent.getAction();

        if (ACTION_ATTACHED.equals(action)) {
            UsbDevice device = getDevice(intent);
            String name = device != null ? device.getDeviceName() : "USB device";
            Log.i(TAG, "USB attached: " + name);

            // AOA handshake (best effort) so the phone sees us as a head unit.
            try {
                AoaHelper aoa = new AoaHelper(ctx);
                if (aoa.open(device)) {
                    Log.i(TAG, "AOA accessory opened");
                }
            } catch (Exception e) {
                Log.w(TAG, "AOA handshake failed (expected on non-AOA phones)", e);
            }

            ConnectionMonitor.onPhoneConnected(ctx, ConnectionMonitor.TYPE_USB, name);

            if (ConnectionMonitor.isAutoLaunchEnabled(ctx)) {
                launchDashboard(ctx);
            }
        } else if (ACTION_DETACHED.equals(action)) {
            Log.i(TAG, "USB detached");
            ConnectionMonitor.onPhoneDisconnected(ctx);
        }
    }

    private UsbDevice getDevice(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice.class);
        }
        return intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
    }

    private void launchDashboard(Context ctx) {
        try {
            Intent i = new Intent(ctx, LauncherActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            ctx.startActivity(i);
        } catch (Exception e) {
            Log.w(TAG, "Could not auto-launch dashboard", e);
        }
    }
}
