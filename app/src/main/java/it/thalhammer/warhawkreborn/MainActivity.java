package it.thalhammer.warhawkreborn;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getName();
    private Thread responder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((TextView)findViewById(R.id.log_view)).setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");
    }

    @Override
    protected  void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
        responder = new ServerSearchResponderThread() {
             @Override
             protected void appendLog(String str) {
                 Log.i(LOG_TAG, str);
                 ((TextView)findViewById(R.id.log_view)).append(str + "\n");
             }
        };
        responder.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause");
        if(responder != null && responder.isAlive()) {
            responder.interrupt();
            try {
                responder.join();
            } catch(InterruptedException e){
                Log.d(LOG_TAG, "Failed to join responder thread", e);
            }
            Log.d(LOG_TAG, "Responder exited");
        }
    }
}
