package it.thalhammer.warhawkreborn;

import android.app.Application;
import android.util.Log;
import it.thalhammer.warhawkreborn.model.ServerList;

import java.util.HashSet;
import java.util.Set;

public class ServerSearchResponder implements ServerSearchResponderThread.OnStateChangeHandler {
    private static final String LOG_TAG = ServerSearchResponder.class.getName();
    private static final ServerSearchResponder instance = new ServerSearchResponder();
    public static ServerSearchResponder getInstance() { return instance; }

    private ServerSearchResponderThread worker;

    private ServerSearchResponder() {
    }

    public void start() {
        if(worker != null) return;
        worker = new ServerSearchResponderThread();
        worker.setHandler(this);
        worker.start();
    }

    public void stop() {
        if (worker != null && worker.isAlive()) {
            worker.interrupt();
            try {
                worker.join();
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Failed to join responder thread", e);
            }
            Log.d(LOG_TAG, "Responder exited");
            worker = null;
        }
    }

    public boolean isActive() {
        return worker != null && worker.isAlive();
    }

    public ServerList getServerList() {
        return worker.getServerList();
    }

    public void updateServers() {
        worker.updateServers();
    }

    public String getStatusText() {
        if(worker != null) return worker.getStatusText();
        return "Not started";
    }

    public interface OnStateChangeListener {
        void onServerListUpdated(ServerList list);
        void onServerStart();
        void onServerStop();
    }

    private Set<OnStateChangeListener> listeners = new HashSet<>();

    public void addListener(OnStateChangeListener l) {
        listeners.add(l);
    }

    public void removeListener(OnStateChangeListener l) {
        listeners.remove(l);
    }

    @Override
    public void onServerListUpdated(ServerList list) {
        for(OnStateChangeListener l : listeners) {
            l.onServerListUpdated(list);
        }
    }

    @Override
    public void onServerStart() {
        for(OnStateChangeListener l : listeners) {
            l.onServerStart();
        }
    }

    @Override
    public void onServerStop() {
        for(OnStateChangeListener l : listeners) {
            l.onServerStop();
        }
    }
}
