package com.t3.carlauncher;

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
 * Car launcher dashboard for the 1024x600 head unit.
 * Set as the default HOME app (see README) — it becomes the home screen
 * after every boot, and hands over to the receiver when a phone connects.
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
            if ("com.t3.autoreceiver.PHONE_CONNECTED".equals(a)) {
                String type = intent.getStringExtra("type");
                String dev = intent.getStringExtra("device");
                connText.setText("Phone: " + (dev == null || dev.isEmpty() ? type : dev));
                // Hand over to the receiver automatically.
                openReceiver();
            } else if ("com.t3.autoreceiver.PHONE_DISCONNECTED".equals(a)) {
                connText.setText("No phone");
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
        f.addAction("com.t3.autoreceiver.PHONE_CONNECTED");
        f.addAction("com.t3.autoreceiver.PHONE_DISCONNECTED");
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
        try {
            Intent i = new Intent("com.t3.autoreceiver.LAUNCH_RECEIVER");
            i.setPackage("com.t3.autoreceiver");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Receiver not installed — install T3 Auto Receiver first",
                    Toast.LENGTH_LONG).show();
        }
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
