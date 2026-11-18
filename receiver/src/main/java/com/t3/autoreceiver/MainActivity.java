package com.t3.autoreceiver;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Receiver UI — Android-Auto-style tabs (Home / Navigate / Music / Phone / Settings)
 * sized for the 1024x600 head unit. Auto-opens on USB/Bluetooth connect via
 * {@link UsbReceiver} / {@link BluetoothReceiver}.
 */
public class MainActivity extends Activity {

    private TextView statusText;
    private TextView deviceText;
    private View statusDot;
    private View pageHome, pageNav, pageMusic, pagePhone, pageSettings;
    private CheckBox prefAutoLaunch, prefKeepScreenOn, prefStartService;

    private final BroadcastReceiver connReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            if (intent == null) return;
            String a = intent.getAction();
            if (ConnectionMonitor.ACTION_PHONE_CONNECTED.equals(a)) {
                String type = intent.getStringExtra(ConnectionMonitor.EXTRA_TYPE);
                String dev = intent.getStringExtra(ConnectionMonitor.EXTRA_DEVICE);
                showConnected(type, dev);
            } else if (ConnectionMonitor.ACTION_PHONE_DISCONNECTED.equals(a)) {
                showDisconnected();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        deviceText = findViewById(R.id.deviceText);
        statusDot = findViewById(R.id.statusDot);
        pageHome = findViewById(R.id.pageHome);
        pageNav = findViewById(R.id.pageNav);
        pageMusic = findViewById(R.id.pageMusic);
        pagePhone = findViewById(R.id.pagePhone);
        pageSettings = findViewById(R.id.pageSettings);

        // Tabs
        findViewById(R.id.tabHome).setOnClickListener(v -> showPage(pageHome));
        findViewById(R.id.tabNav).setOnClickListener(v -> showPage(pageNav));
        findViewById(R.id.tabMusic).setOnClickListener(v -> showPage(pageMusic));
        findViewById(R.id.tabPhone).setOnClickListener(v -> showPage(pagePhone));
        findViewById(R.id.tabSettings).setOnClickListener(v -> showPage(pageSettings));

        // Home actions
        findViewById(R.id.btnMirror).setOnClickListener(v -> MirrorHelper.showMirrorHelp(this));
        findViewById(R.id.btnHotspot).setOnClickListener(v -> {
            if (!MirrorHelper.tryEnableHotspot(this)) {
                MirrorHelper.openHotspotSettings(this);
            }
        });
        findViewById(R.id.btnLauncher).setOnClickListener(v -> MirrorHelper.openLauncher(this));

        // Nav / Phone actions
        findViewById(R.id.btnOpenMaps).setOnClickListener(v -> MirrorHelper.openMaps(this));
        findViewById(R.id.btnDialer).setOnClickListener(v -> MirrorHelper.openDialer(this));

        // Music transport (sends media keys to the connected phone)
        findViewById(R.id.btnPrev).setOnClickListener(
                v -> MirrorHelper.sendMediaKey(this, KeyEvent.KEYCODE_MEDIA_PREVIOUS));
        findViewById(R.id.btnPlayPause).setOnClickListener(
                v -> MirrorHelper.sendMediaKey(this, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
        findViewById(R.id.btnNext).setOnClickListener(
                v -> MirrorHelper.sendMediaKey(this, KeyEvent.KEYCODE_MEDIA_NEXT));

        // Settings
        prefAutoLaunch = findViewById(R.id.prefAutoLaunch);
        prefKeepScreenOn = findViewById(R.id.prefKeepScreenOn);
        prefStartService = findViewById(R.id.prefStartService);
        prefAutoLaunch.setChecked(ConnectionMonitor.isAutoLaunchEnabled(this));
        prefKeepScreenOn.setChecked(ConnectionMonitor.isKeepScreenOn(this));
        prefStartService.setChecked(ConnectionMonitor.isStartServiceEnabled(this));
        prefAutoLaunch.setOnCheckedChangeListener((b, on) -> ConnectionMonitor.setAutoLaunchEnabled(this, on));
        prefKeepScreenOn.setOnCheckedChangeListener((b, on) -> {
            ConnectionMonitor.setKeepScreenOn(this, on);
            MirrorHelper.keepScreenOn(this, on);
        });
        prefStartService.setOnCheckedChangeListener((b, on) -> ConnectionMonitor.setStartServiceEnabled(this, on));

        MirrorHelper.keepScreenOn(this, ConnectionMonitor.isKeepScreenOn(this));

        // Start the foreground service so auto-open keeps working.
        Intent svc = new Intent(this, ReceiverService.class);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(svc);
            else startService(svc);
        } catch (Exception ignored) {}

        // If Bluetooth is off, nudge the user (don't force it).
        try {
            BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
            if (bt != null && !bt.isEnabled()) {
                deviceText.setText("Bluetooth is OFF — turn it on to connect your phone");
            }
        } catch (Exception ignored) {}

        // Handle the USB-attach intent that launched us.
        handleLaunchIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleLaunchIntent(intent);
    }

    private void handleLaunchIntent(Intent intent) {
        if (intent == null) return;
        if ("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(intent.getAction())) {
            showConnected(ConnectionMonitor.TYPE_USB, "USB phone");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter f = new IntentFilter();
        f.addAction(ConnectionMonitor.ACTION_PHONE_CONNECTED);
        f.addAction(ConnectionMonitor.ACTION_PHONE_DISCONNECTED);
        registerReceiver(connReceiver, f);
    }

    @Override
    protected void onPause() {
        try { unregisterReceiver(connReceiver); } catch (Exception ignored) {}
        super.onPause();
    }

    private void showPage(View page) {
        pageHome.setVisibility(View.GONE);
        pageNav.setVisibility(View.GONE);
        pageMusic.setVisibility(View.GONE);
        pagePhone.setVisibility(View.GONE);
        pageSettings.setVisibility(View.GONE);
        page.setVisibility(View.VISIBLE);
    }

    private void showConnected(String type, String device) {
        statusDot.setBackgroundResource(R.drawable.dot_green);
        statusText.setText(ConnectionMonitor.TYPE_USB.equals(type)
                ? R.string.status_usb : R.string.status_bt);
        deviceText.setText(device == null ? "" : device);
    }

    private void showDisconnected() {
        statusDot.setBackgroundResource(R.drawable.dot_gray);
        statusText.setText(R.string.status_disconnected);
        deviceText.setText("");
    }
}
