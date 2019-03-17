package it.thalhammer.warhawkreborn.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public abstract class BackgroundService extends Service {
    private Thread th;
    private volatile boolean exit = false;

    public void start(Runnable task) {
        if (th == null || !th.isAlive()) {
            exit = false;
            th = new Thread(task);
            th.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(BackgroundService.class.getName(), "onDestroy");
        if(th != null) {
            exit = true;
            try {
                th.join();
            } catch (InterruptedException e) {
                Log.e(BackgroundService.class.getName(), "Failed to stop serverthread", e);
            }
        }
    }

    protected boolean shouldExit() {
        return this.exit;
    }
}
