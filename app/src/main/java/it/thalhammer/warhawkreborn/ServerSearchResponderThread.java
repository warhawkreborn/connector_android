package it.thalhammer.warhawkreborn;

import android.util.Log;
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
    private boolean shouldUpdateServers = false;

    @Override
    public void run() {
        try {
            serverSocket = new DatagramSocket(10029);
            serverSocket.setSoTimeout(1000);

            doUpdateServers();

            List<InetAddress> localIps = Util.getLocalIpAddress();
            for(InetAddress addr: localIps) {
                appendLog("Local address; " + addr.getHostAddress());
            }

            if(!serverSocket.isBound()) {
                appendLog("Serversocket is not bound :(");
            }

            if(handler!= null) handler.onServerStart();
            while (!this.isInterrupted()) {
                if(shouldUpdateServers) {
                    doUpdateServers();
                    shouldUpdateServers = false;
                }
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    serverSocket.receive(receivePacket);
                } catch(SocketTimeoutException e) {
                    continue;
                }
                // Search packets are always 188 bytes
                if(receivePacket.getLength() != 188) continue;
                InetAddress src = receivePacket.getAddress();
                boolean found = false;
                for(InetAddress addr : localIps) {
                    if(addr.equals(src)) { found = true; break; }
                }
                if(!found)
                    this.handlePacket(receivePacket.getData(), receivePacket.getLength(), src);
            }
        } catch (SocketException e) {
            appendLog("Responderthread crashed:");
            appendLog(e.getMessage());
            appendLog(e.toString());
        } catch (IOException e) {
            appendLog("Responderthread crashed:");
            appendLog(e.getMessage());
            appendLog(e.toString());
        } finally {
            if(serverSocket!=null) serverSocket.close();
        }
        appendLog("Responder exited");
        if(handler!= null) handler.onServerStop();
    }

    @Override
    public void interrupt() {
        super.interrupt();
        if(serverSocket != null) serverSocket.close();
    }
    
    private void doUpdateServers() {
        appendLog("Downloading server list...");
        for(int i=0; i<10; i++) {
            ServerList list = API.getServerList();
            if(list == null) continue;
            serverList = list;
            break;
        }
        int n_online = 0;
        for(ServerList.Entry e : serverList) if(e.isOnline()) n_online++;
        appendLog("Found " + serverList.size() + " servers (" + n_online + " online)");
        if(n_online>0) {
            statusText = "Broadcasting " + serverList.size() + " servers (" + n_online + " online)\nGo to your PS3 and search for local games!";
        } else {
            statusText = "Startup ok, but no servers are online :(";
        }
        if(handler != null) handler.onServerListUpdated(serverList);
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
        if(len > 3) {
            if(data[0] == -61 && data[1] == -127) {
                appendLog("Received server request from " + src.toString());
                for(ServerList.Entry e: serverList) {
                    if(!e.isOnline()) continue;
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
