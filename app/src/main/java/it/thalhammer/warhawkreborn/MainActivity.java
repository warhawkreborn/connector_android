package it.thalhammer.warhawkreborn;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import it.thalhammer.warhawkreborn.fragment.*;

public class MainActivity extends AppCompatActivity implements FragmentBase.OnFragmentInteractionListener {
    private static final String LOG_TAG = MainActivity.class.getName();
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                }
                return true;
            }
        });

        setFragment(new MainFragment());

        AppLog.getInstance().addEntry(getResources().getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
        AppLog.getInstance().addEntry(getResources().getString(R.string.app_copyright_message));
    }

    public void setFragment(Fragment f) {
        setFragment(f, false);
    }

    public void setFragment(Fragment f, boolean addtobackstack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, f);
        if(addtobackstack) transaction.addToBackStack(null);
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
        if(item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
