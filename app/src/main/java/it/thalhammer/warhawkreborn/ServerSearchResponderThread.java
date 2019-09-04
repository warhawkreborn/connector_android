package it.thalhammer.warhawkreborn;

import android.util.Log;
import com.crashlytics.android.Crashlytics;
import it.thalhammer.warhawkreborn.model.ServerList;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.*;
import java.util.List;

public class ServerSearchResponderThread extends Thread {
    public interface OnStateChangeHandler {
        void onServerListUpdated(ServerList list);

        void onServerStart();

        void onServerStop();
    }

    private static final String LOG_TAG = ServerSearchResponderThread.class.getName();
    private DatagramSocket serverSocket = null;
    private volatile boolean shouldUpdateServers = false;
    private volatile boolean shouldExit = false;

    @Override
    public void run() {
        try {
            serverSocket = new DatagramSocket(10029);
            serverSocket.setSoTimeout(1000);

            doUpdateServers();

            List<InetAddress> localIps = Util.getLocalIpAddress();
            for (InetAddress addr : localIps) {
                appendLog(MainActivity.getAppContext().getResources().getString(R.string.responder_thread_local_ip, addr.getHostAddress()));
            }

            if (!serverSocket.isBound()) {
                appendLog(MainActivity.getAppContext().getResources().getString(R.string.responder_thread_socket_not_bound));
            }

            if (handler != null) handler.onServerStart();

            long time = System.currentTimeMillis();
            while (!this.isInterrupted()) {
                if (System.currentTimeMillis() - time > 120 * 1000) {
                    time = System.currentTimeMillis();
                    shouldUpdateServers = true;
                }
                if (shouldUpdateServers) {
                    doUpdateServers();
                    shouldUpdateServers = false;
                }
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    serverSocket.receive(receivePacket);
                } catch (SocketTimeoutException e) {
                    continue;
                }
                // Search packets are always 188 bytes
                if (receivePacket.getLength() != 188) continue;
                InetAddress src = receivePacket.getAddress();
                boolean found = false;
                for (InetAddress addr : localIps) {
                    if (addr.equals(src)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    this.handlePacket(receivePacket.getData(), receivePacket.getLength(), src);
            }
        } catch (SocketException e) {
            if(!shouldExit) {
                appendLog(MainActivity.getAppContext().getResources().getString(R.string.responder_thread_thread_crashed));
                appendLog(e.getMessage());
                appendLog(e.toString());
                Crashlytics.logException(e);
            }
        } catch (IOException e) {
            appendLog(MainActivity.getAppContext().getResources().getString(R.string.responder_thread_thread_crashed));
            appendLog(e.getMessage());
            appendLog(e.toString());
            Crashlytics.logException(e);
        } finally {
            if (serverSocket != null) serverSocket.close();
        }
        appendLog(MainActivity.getAppContext().getResources().getString(R.string.responder_exited));
        if (handler != null) handler.onServerStop();
    }

    @Override
    public void interrupt() {
        this.shouldExit = true;
        super.interrupt();
        if (serverSocket != null) serverSocket.close();
    }

    public boolean isOK() {
        return isAlive() && serverList != null && !serverList.isEmpty();
    }

    private void doUpdateServers() {
        appendLog(MainActivity.getAppContext().getResources().getString(R.string.responder_downloading_list));
        for (int i = 0; i < 10; i++) {
            ServerList list = API.getServerList();
            if (list == null) continue;
            serverList = list;
            break;
        }
        if(serverList == null) {
            statusText = MainActivity.getAppContext().getResources().getString(R.string.responder_could_not_download);
            appendLog(statusText);
            return;
        }
        appendLog(MainActivity.getAppContext().getResources().getString(R.string.responder_found_n_servers, serverList.size()));
        if (serverList.size() > 0) {
            statusText = MainActivity.getAppContext().getResources().getString(R.string.responder_broadcasting_n_servers, serverList.size());
        } else {
            statusText = MainActivity.getAppContext().getResources().getString(R.string.responder_ok_no_servers);
        }
        if (handler != null) handler.onServerListUpdated(serverList);
    }

    public void updateServers() {
        shouldUpdateServers = true;
    }

    @Getter
    private ServerList serverList = null;

    @Getter
    @Setter
    private OnStateChangeHandler handler = null;

    @Getter
    private String statusText = "";

    private void handlePacket(byte[] data, int len, InetAddress src) {
        if (len > 3) {
            if (data[0] == -61 && data[1] == -127) {
                appendLog(MainActivity.getAppContext().getString(R.string.responder_received_server_request, src.getHostAddress()));
                if(serverList == null) {
                    appendLog(MainActivity.getAppContext().getString(R.string.responder_no_serverlist));
                    return;
                }
                for (ServerList.Entry e : serverList) {
                    if (!e.isOnline()) continue;
                    byte[] frame = Util.hexStringToByteArray(e.getResponse());
                    try {
                        DatagramPacket pkt = new DatagramPacket(frame, frame.length, src, 10029);
                        this.serverSocket.send(pkt);
                    } catch (IOException ex) {
                        Log.e(LOG_TAG, "Failed to send discovery response", ex);
                    }
                }

            }
        }
    }

    private void appendLog(String str) {
        Log.i(LOG_TAG, str);
        AppLog.getInstance().addEntry(str);
    }
}
