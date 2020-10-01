package it.thalhammer.warhawkreborn;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

import it.thalhammer.warhawkreborn.fragment.*;
import it.thalhammer.warhawkreborn.model.UpdateInfo;
import lombok.SneakyThrows;

public class MainActivity extends AppCompatActivity implements FragmentBase.OnFragmentInteractionListener, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String LOG_TAG = MainActivity.class.getName();

    private static final String SIDELOAD_APK_NAME = "com.android.packageinstaller";
    private DrawerLayout drawerLayout;

    private static Context appContext;

    public static Context getAppContext() { return appContext; }

    private void updateNightModeSetting() {
        String value = PreferenceManager.getDefaultSharedPreferences(this).getString("night_mode", "os");
        switch(value) {
            default:
            case "os":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "on":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "off":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("night_mode")) {
            String value = sharedPreferences.getString("night_mode", "os");
            Log.i(LOG_TAG, "Changed preference " + key + " to " + value);
            updateNightModeSetting();
        }
    }

    class UpdateTask extends AsyncTask<Void, Void, UpdateInfo> {
        @Override
        protected UpdateInfo doInBackground(Void... voids) {
            try {
                return API.getUpdateInfo();
            } catch(Exception e) {
                Log.e(LOG_TAG, "Failed to check for updates:", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(final UpdateInfo result) {
            if(result == null) return;
            if(result.getVersionCode() <= BuildConfig.VERSION_CODE) return;
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(result.getChanges())
                    .setTitle("New version " + result.getVersion() + " is available")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(result.getUri()));
                            startActivity(browserIntent);
                        }
                    })
                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton(android.R.string.cancel, null)
                    .setIcon(android.R.drawable.ic_menu_upload)
                    .show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateNightModeSetting();

        appContext = this.getApplicationContext();

        // Fix for missing sax parser in upnp lib
        System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if(actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // set item as selected to persist highlight
                menuItem.setChecked(true);
                // close drawer when item is tapped
                drawerLayout.closeDrawers();

                switch(menuItem.getItemId()) {
                    case R.id.nav_start_playing:
                        setFragment(new MainFragment());
                        break;
                    case R.id.nav_server_list:
                        setFragment(new ServerListFragment());
                        break;
                    case R.id.nav_host_server:
                        setFragment(new HostFragment());
                        break;
                    case R.id.nav_app_log:
                        setFragment(new LogFragment());
                        break;
                    case R.id.nav_privacy_policy:
                        setFragment(new PrivacyPolicyFragment());
                        break;
                    case R.id.nav_preferences:
                        setFragment(new SettingsFragment());
                        break;

                }
                return true;
            }
        });
        navigationView.setCheckedItem(R.id.nav_start_playing);

        createNotificationChannel();

        setFragment(new MainFragment(), false);

        AppLog.getInstance().addEntry(getResources().getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
        AppLog.getInstance().addEntry(getResources().getString(R.string.app_copyright_message));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs != null) {
            String fcmId = prefs.getString(getString(R.string.pref_fcm_id), null);
            if(fcmId != null)
                Log.i(LOG_TAG, "FCMID: " + fcmId);
        }
        final String installer = getPackageManager().getInstallerPackageName(getPackageName());
        Log.d(LOG_TAG, "Installer:" + installer);
        if(installer == null || SIDELOAD_APK_NAME.equals(installer)) {
            new UpdateTask().execute();
        }

        try {
            Date launchDate = new SimpleDateFormat("yyyy-MM-dd").parse("2020-10-01");
            Date now = new Date();
            if(now.compareTo(launchDate) >= 0) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.shutdown_popup_header)
                        .setMessage(R.string.shutdown_popup_description)
                        .setPositiveButton(R.string.shutdown_popup_showweb, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://psone.online/game/warhawk/reborn-shutdown"));
                                startActivity(browserIntent);
                            }
                        })
                        .setNegativeButton(R.string.shutdown_popup_later, null)
                        .setIcon(R.drawable.warhawk_logo)
                        .show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public void setFragment(Fragment f) {
        setFragment(f, true);
    }

    public void setFragment(Fragment f, boolean addtobackstack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, f);
        if(addtobackstack) transaction.addToBackStack(null);
        transaction.commit();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            CharSequence server_name = getString(R.string.server_channel_name);
            String server_description = getString(R.string.server_channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("serverinfo", server_name, importance);
            channel.setDescription(server_description);
            notificationManager.createNotificationChannel(channel);


            CharSequence default_name = getString(R.string.default_channel_name);
            String default_description = getString(R.string.default_channel_description);
            importance = NotificationManager.IMPORTANCE_DEFAULT;
            channel = new NotificationChannel("default", default_name, importance);
            channel.setDescription(default_description);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void setDrawerVisible(boolean v) {
        if(v) {
            MainActivity.this.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            findViewById(R.id.toolbar).setVisibility(View.VISIBLE);
        } else {
            MainActivity.this.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            findViewById(R.id.toolbar).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        ServerSearchResponder.getInstance().start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause");
        ServerSearchResponder.getInstance().stop();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
