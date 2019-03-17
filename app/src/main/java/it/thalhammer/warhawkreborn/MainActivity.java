package it.thalhammer.warhawkreborn;

import android.content.*;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import it.thalhammer.warhawkreborn.service.ServerSearchResponder;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((Button)findViewById(R.id.btn_start_service)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStartService(v);
            }
        });
        ((Button)findViewById(R.id.btn_stop_service)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleStopService(v);
            }
        });
        ((TextView)findViewById(R.id.log_view)).setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(MainActivity.class.getName(), "onStart");
    }

    @Override
    protected  void onStop() {
        super.onStop();
        Log.i(MainActivity.class.getName(), "onStop");
        if(binder != null) {
            unbindService(mConnection);
            binder = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(MainActivity.class.getName(), "onResume");
        registerReceiver(receiver, new IntentFilter(ServerSearchResponder.NOTIFICATION));
        if(binder != null && binder.isStarted()) {
             findViewById(R.id.btn_stop_service).setVisibility(View.VISIBLE);
             findViewById(R.id.btn_start_service).setVisibility(View.GONE);
        } else {
            findViewById(R.id.btn_stop_service).setVisibility(View.GONE);
            findViewById(R.id.btn_start_service).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(MainActivity.class.getName(), "onPause");
        unregisterReceiver(receiver);
    }

    private void handleStartService(View v) {
        if(null != binder && binder.isStarted()) return;
        Intent intent = new Intent(this, ServerSearchResponder.class);
        this.getApplicationContext().startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void handleStopService(View v) {
        if(binder == null) return;
        Intent intent = new Intent(this, ServerSearchResponder.class);
        unbindService(mConnection);
        stopService(intent);
        binder = null;
    }

    private ServerSearchResponder.Binder binder = null;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            binder = (ServerSearchResponder.Binder) service;
            if(binder.isStarted()) {
                findViewById(R.id.btn_stop_service).setVisibility(View.VISIBLE);
                findViewById(R.id.btn_start_service).setVisibility(View.GONE);
            } else {
                findViewById(R.id.btn_stop_service).setVisibility(View.GONE);
                findViewById(R.id.btn_start_service).setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            binder = null;
        }
    };
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(MainActivity.class.getName(), "onReceive");
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                ServerSearchResponder.Command command = (ServerSearchResponder.Command)bundle.getSerializable(ServerSearchResponder.BUNDLE_COMMAND);
                if(command == ServerSearchResponder.Command.UpdateLog){
                    TextView log_view = (TextView)findViewById(R.id.log_view);
                    log_view.setText(bundle.getString(ServerSearchResponder.BUNDLE_LOG));
                    final int scrollAmount = log_view.getLayout().getLineTop(log_view.getLineCount()) - log_view.getHeight();
                    // if there is no need to scroll, scrollAmount will be <=0
                    if (scrollAmount > 0) log_view.scrollTo(0, scrollAmount);
                    else log_view.scrollTo(0, 0);
                } else if(command == ServerSearchResponder.Command.Started) {
                    findViewById(R.id.btn_stop_service).setVisibility(View.VISIBLE);
                    findViewById(R.id.btn_start_service).setVisibility(View.GONE);
                } else if(command == ServerSearchResponder.Command.Stopped) {
                    findViewById(R.id.btn_stop_service).setVisibility(View.GONE);
                    findViewById(R.id.btn_start_service).setVisibility(View.VISIBLE);
                }
            }
        }
    };
}
