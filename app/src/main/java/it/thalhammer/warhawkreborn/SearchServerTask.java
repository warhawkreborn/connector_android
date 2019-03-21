package it.thalhammer.warhawkreborn;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import it.thalhammer.warhawkreborn.networking.DiscoveryPacket;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public abstract class SearchServerTask extends AsyncTask<Void, List<Pair<DiscoveryPacket, Inet4Address>>, List<Pair<DiscoveryPacket, Inet4Address>>> {
    private static final byte[] discoverPacket = Util.hexStringToByteArray("c381b800001900b6018094004654000005000000010000000000020307000000c0a814ac000000002d27000000000000010000005761726861776b000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002801800ffffffff00000000000000004503d7e0000000000000005a");
    private static final String LOG_TAG = SearchServerTask.class.getName();

    protected List<Pair<DiscoveryPacket, Inet4Address>> doInBackground(Void... voids) {
        List<Pair<DiscoveryPacket, Inet4Address>> result = new ArrayList<>();
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(1000);
            socket.setBroadcast(true);

            DatagramPacket pkt = new DatagramPacket(discoverPacket, discoverPacket.length, Inet4Address.getByName("255.255.255.255"), 10029);
            socket.send(pkt);

            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                result.add(new Pair<DiscoveryPacket, Inet4Address>(new DiscoveryPacket(receiveData), (Inet4Address)receivePacket.getAddress()));
            }
        } catch (SocketTimeoutException e) {
            // No frame for 1 second => done
            return result;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to search for servers", e);
            return null;
        }
    }
}
