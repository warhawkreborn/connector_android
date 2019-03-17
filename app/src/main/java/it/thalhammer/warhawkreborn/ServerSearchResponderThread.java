package it.thalhammer.warhawkreborn;

import android.util.Log;
import it.thalhammer.warhawkreborn.model.ServerList;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public abstract class ServerSearchResponderThread extends Thread {
    private static final String LOG_TAG = ServerSearchResponderThread.class.getName();
    private DatagramSocket serverSocket = null;

    @Override
    public void run() {
        try {
            serverSocket = new DatagramSocket(10029);

            updateServers();

            while (!this.isInterrupted()) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                InetAddress src = receivePacket.getAddress();
                this.handlePacket(receivePacket.getData(), receivePacket.getLength(), src);
            }
        } catch (SocketException e) {
            appendLog(e.getMessage());
        } catch (IOException e) {
            if(serverSocket != null && serverSocket.isClosed()) return;
            appendLog(e.getMessage());
        } finally {
            if(serverSocket!=null) serverSocket.close();
        }
        appendLog("Responder exited");
    }

    @Override
    public void interrupt() {
        super.interrupt();
        if(serverSocket != null) serverSocket.close();
    }

    private void updateServers() {
        appendLog("Downloading server list...");
        serverList = API.getServerList();
        int n_online = 0;
        for(ServerList.Entry e : serverList) if(e.isOnline()) n_online++;
        appendLog("Found " + serverList.size() + " servers (" + n_online + " online)");
    }

    private ServerList serverList = null;

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

    protected abstract void appendLog(String str);
}
