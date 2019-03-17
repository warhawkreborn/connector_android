package it.thalhammer.warhawkreborn;

import android.net.Uri;
import android.os.Bundle;
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
import it.thalhammer.warhawkreborn.fragment.HostFragment;
import it.thalhammer.warhawkreborn.fragment.LogFragment;
import it.thalhammer.warhawkreborn.fragment.MainFragment;

public class MainActivity extends AppCompatActivity implements LogFragment.OnFragmentInteractionListener, HostFragment.OnFragmentInteractionListener, MainFragment.OnFragmentInteractionListener {
    private static final String LOG_TAG = MainActivity.class.getName();
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    private void setFragment(Fragment f) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, f);
        transaction.commit();
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
