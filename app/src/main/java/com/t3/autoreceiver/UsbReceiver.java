package com.t3.autoreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

/**
 * Detects a plugged-in phone and hands the screen to the head unit's
 * projection engine (ZLink → real Android Auto).
 *
 * IMPORTANT: this receiver deliberately does NOT open the USB device and does
 * NOT perform an AOA handshake. Claiming the accessory makes the phone think it
 * is talking to an unlicensed accessory, which drops it out of Android Auto and
 * leaves ZLink with nothing to show — that was the "app opens but nothing works"
 * symptom. ZLink owns the USB device; we only react to the attach event.
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

            ConnectionMonitor.onPhoneConnected(ctx, ConnectionMonitor.TYPE_USB, name);

            if (ConnectionMonitor.isAutoLaunchEnabled(ctx)) {
                launchProjection(ctx);
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

    /** Opens ZLink (Android Auto) directly; falls back to our dashboard. */
    private void launchProjection(Context ctx) {
        try {
            ConnectionMonitor.openProjection(ctx);
        } catch (Exception e) {
            Log.w(TAG, "Could not auto-launch projection", e);
        }
    }
}
