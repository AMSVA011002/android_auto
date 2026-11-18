package com.t3.autoreceiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

/**
 * Foreground service that keeps the receiver alive while driving.
 * Registers the USB + Bluetooth receivers at runtime as a backup to the
 * manifest receivers (some T3 firmwares drop manifest receivers after reboot).
 */
public class ReceiverService extends Service {

    private static final String TAG = "T3ReceiverSvc";
    private static final int NOTIF_ID = 1001;
    private static final String CHANNEL = "t3_receiver";

    private UsbReceiver usbReceiver;
    private BluetoothReceiver btReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        usbReceiver = new UsbReceiver();
        btReceiver = new BluetoothReceiver();
        register(usbReceiver, new IntentFilterCompat().usbFilter());
        register(btReceiver, new IntentFilterCompat().btFilter());
        Log.i(TAG, "ReceiverService started");
    }

    /** Android 13+ needs an explicit export flag for context-registered receivers. */
    private void register(android.content.BroadcastReceiver r, android.content.IntentFilter f) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(r, f, Context.RECEIVER_EXPORTED);
            } else {
                registerReceiver(r, f);
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not register " + r.getClass().getSimpleName(), e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIF_ID, buildNotification());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try { unregisterReceiver(usbReceiver); } catch (Exception ignored) {}
        try { unregisterReceiver(btReceiver); } catch (Exception ignored) {}
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification buildNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL, "T3 Auto Receiver", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(ch);
            return new Notification.Builder(this, CHANNEL)
                    .setContentTitle("T3 Auto Receiver")
                    .setContentText("Listening for phone (USB / Bluetooth)…")
                    .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                    .build();
        }
        return new Notification.Builder(this)
                .setContentTitle("T3 Auto Receiver")
                .setContentText("Listening for phone (USB / Bluetooth)…")
                .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
                .getNotification();
    }

    /** Small helper so the filter code stays in one place. */
    static final class IntentFilterCompat {
        android.content.IntentFilter usbFilter() {
            android.content.IntentFilter f = new android.content.IntentFilter();
            f.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
            f.addAction("android.hardware.usb.action.USB_DEVICE_DETACHED");
            return f;
        }
        android.content.IntentFilter btFilter() {
            android.content.IntentFilter f = new android.content.IntentFilter();
            f.addAction("android.bluetooth.device.action.ACL_CONNECTED");
            f.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
            return f;
        }
    }
}
