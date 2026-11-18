package com.t3.autoreceiver;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Simple app drawer listing every installed launcher app (alphabetical).
 * Kept dependency-free and light for the 1 GB RAM head unit.
 */
public class AppDrawerActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_drawer);

        ListView list = findViewById(R.id.appList);
        final List<Intent> launchIntents = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        PackageManager pm = getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<android.content.pm.ResolveInfo> infos = pm.queryIntentActivities(main, 0);
        Collections.sort(infos, new Comparator<android.content.pm.ResolveInfo>() {
            @Override
            public int compare(android.content.pm.ResolveInfo a, android.content.pm.ResolveInfo b) {
                return a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString());
            }
        });
        for (android.content.pm.ResolveInfo ri : infos) {
            labels.add(ri.loadLabel(pm).toString());
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.setClassName(ri.activityInfo.packageName, ri.activityInfo.name);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntents.add(i);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, labels);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, pos, id) -> {
            try {
                startActivity(launchIntents.get(pos));
            } catch (Exception ignored) {}
        });
    }
}
