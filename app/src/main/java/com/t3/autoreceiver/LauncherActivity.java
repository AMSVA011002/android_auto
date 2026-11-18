package com.t3.autoreceiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Dashboard for the 1024x600 head unit.
 *
 * This is NOT a HOME launcher: it opens only when a phone connects
 * (USB/Bluetooth) and returns to the head unit's default launcher when
 * the phone disconnects. The built-in launcher always stays the default.
 */
public class LauncherActivity extends Activity {

    private TextView clockText, dateText, connText;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable clockTick = new Runnable() {
        @Override
        public void run() {
            updateClock();
            handler.postDelayed(this, 1000);
        }
    };

    private final BroadcastReceiver connReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctx, Intent intent) {
            if (intent == null) return;
            String a = intent.getAction();
            if (ConnectionMonitor.ACTION_PHONE_CONNECTED.equals(a)) {
                String type = intent.getStringExtra(ConnectionMonitor.EXTRA_TYPE);
                String dev = intent.getStringExtra(ConnectionMonitor.EXTRA_DEVICE);
                connText.setText("Phone: " + (dev == null || dev.isEmpty() ? type : dev));
            } else if (ConnectionMonitor.ACTION_PHONE_DISCONNECTED.equals(a)) {
                connText.setText("No phone");
                // Dashboard only runs while connected — hand back to the
                // head unit's default launcher.
                if (ConnectionMonitor.isReturnHomeEnabled(ctx)) {
                    goHome();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        clockText = findViewById(R.id.clockText);
        dateText = findViewById(R.id.dateText);
        connText = findViewById(R.id.connText);

        findViewById(R.id.tileReceiver).setOnClickListener(v -> openReceiver());
        findViewById(R.id.tileNav).setOnClickListener(v -> openMaps());
        findViewById(R.id.tileMusic).setOnClickListener(v -> openMusic());
        findViewById(R.id.tilePhone).setOnClickListener(v -> openDialer());
        findViewById(R.id.tileRadio).setOnClickListener(v -> openRadio());
        findViewById(R.id.tileApps).setOnClickListener(
                v -> startActivity(new Intent(this, AppDrawerActivity.class)));

        findViewById(R.id.toggleWifi).setOnClickListener(v -> Toggles.toggleWifi(this));
        findViewById(R.id.toggleBt).setOnClickListener(v -> Toggles.toggleBluetooth(this));
        findViewById(R.id.toggleHotspot).setOnClickListener(v -> Toggles.openHotspot(this));
        findViewById(R.id.toggleBrightness).setOnClickListener(v -> Toggles.cycleBrightness(this));

        updateClock();
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(clockTick);
        IntentFilter f = new IntentFilter();
        f.addAction(ConnectionMonitor.ACTION_PHONE_CONNECTED);
        f.addAction(ConnectionMonitor.ACTION_PHONE_DISCONNECTED);
        registerReceiver(connReceiver, f);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(clockTick);
        try { unregisterReceiver(connReceiver); } catch (Exception ignored) {}
        super.onPause();
    }

    private void updateClock() {
        Date now = new Date();
        clockText.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now));
        dateText.setText(new SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(now));
    }

    private void openReceiver() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void goHome() {
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception ignored) {}
    }

    private void openMaps() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com")));
        } catch (Exception e) {
            Toast.makeText(this, "Maps not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openMusic() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("content://media/external/audio/media")));
        } catch (Exception e) {
            Toast.makeText(this, "No music app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openDialer() {
        try {
            startActivity(new Intent(Intent.ACTION_DIAL));
        } catch (Exception e) {
            Toast.makeText(this, "Dialer not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openRadio() {
        // T3 head units ship a proprietary radio app with a vendor package name.
        // Try the common ones; fall back to the app drawer.
        String[] candidates = {
                "com.mstar.android.fmradio",
                "com.allwinner.fmradioservice",
                "com.topway.radio",
                "com.ts.radio",
        };
        for (String pkg : candidates) {
            try {
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setPackage(pkg);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return;
            } catch (Exception ignored) {}
        }
        Toast.makeText(this, "Radio app not found — open it from All apps", Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, AppDrawerActivity.class));
    }
}
