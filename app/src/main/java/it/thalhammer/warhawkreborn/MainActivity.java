package it.thalhammer.warhawkreborn;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import it.thalhammer.warhawkreborn.fragment.*;
import it.thalhammer.warhawkreborn.model.UpdateInfo;

import java.io.File;

public class MainActivity extends AppCompatActivity implements LogFragment.OnFragmentInteractionListener,
        HostFragment.OnFragmentInteractionListener, MainFragment.OnFragmentInteractionListener, ServerListFragment.OnFragmentInteractionListener,
        HostServerFragment.OnFragmentInteractionListener, UpdateFragment.OnFragmentInteractionListener {
    private static final String LOG_TAG = MainActivity.class.getName();
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fix for missing sax parser in upnp lib
        System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
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
                    /*case R.id.nav_host_server:
                        setFragment(new HostFragment());
                        break;*/
                    case R.id.nav_app_log:
                        setFragment(new LogFragment());
                        break;
                }
                return true;
            }
        });

        setFragment(new MainFragment());

        AppLog.getInstance().addEntry(getResources().getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
        AppLog.getInstance().addEntry(getResources().getString(R.string.app_copyright_message));

        checkVersion();
    }

    private void checkVersion() {
        AsyncTask<Void, Void, UpdateInfo> task = new AsyncTask<Void, Void, UpdateInfo>() {
            @Override
            protected UpdateInfo doInBackground(Void... voids) {
                File targetFile = new File(MainActivity.this.getFilesDir(), "update.apk");
                if(targetFile.exists()) targetFile.delete();

                UpdateInfo info = API.getUpdateInfo();
                if(info.getVersion().equals(BuildConfig.VERSION_NAME)) return null;
                return info;
            }

            @Override
            protected void onPostExecute(final UpdateInfo v) {
                super.onPostExecute(v);
                if(v == null) return;
                setFragment(UpdateFragment.newInstance(v));
            }
        };
        task.execute();
    }

    public void setFragment(Fragment f) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, f);
        transaction.commit();
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
        ServerSearchResponder.getInstance().start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause");
        ServerSearchResponder.getInstance().stop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
