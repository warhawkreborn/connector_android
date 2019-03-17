package it.thalhammer.warhawkreborn.service;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import it.thalhammer.warhawkreborn.MainActivity;
import it.thalhammer.warhawkreborn.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ServerSearchResponder extends BackgroundService implements Runnable {
    public static final String BUNDLE_COMMAND = "command";
    public static final String BUNDLE_LOG = "log";
    public static final String NOTIFICATION = "it.thalhammer.warhawkreborn.service.receiver";
    public enum Command {
        UpdateLog,
        Started,
        Stopped
    }

    private String log="";
    private DatagramSocket serverSocket;
    private Binder binder = new Binder(this);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(ServerSearchResponder.class.getName(), "onStartCommand");

        String channelId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel(this.getClass().getName(), "Warhawk reborn Background Service");
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Warhawk reborn server list")
                .setContentText("Server running")
                .setSmallIcon(R.drawable.warhawk_logo_small)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        startForeground(101, notification);
        this.start(this);

        return Service.START_NOT_STICKY;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        if(serverSocket != null) {
            serverSocket.close();
        }
        Log.i(ServerSearchResponder.class.getName(), "onDestroy");
    }

    @Override
    public void run() {
        try {
            serverSocket = new DatagramSocket(10029);
            byte receiveData[] = new byte[1024];

            Intent intent = new Intent(NOTIFICATION);
            intent.putExtra(BUNDLE_COMMAND, Command.Started);
            sendBroadcast(intent);

            updateServers();

            while(!this.shouldExit()) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                InetAddress src = receivePacket.getAddress();
                this.handlePacket(receivePacket.getData(), receivePacket.getLength(), src);
                receiveData = new byte[1024];
            }
        } catch (SocketException e) {
            appendLog(e.getMessage());
        } catch (IOException e) {
            if(serverSocket.isClosed()) return;
            appendLog(e.getMessage());
        }
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(BUNDLE_COMMAND, Command.Stopped);
        sendBroadcast(intent);
    }

    private void appendLog(String line) {
        this.log += line + "\n";
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(BUNDLE_COMMAND, Command.UpdateLog);
        intent.putExtra(BUNDLE_LOG, this.log);
        sendBroadcast(intent);
        Log.i(ServerSearchResponder.class.getName(), line);
    }

    public static String byteArrayToHexString(byte[] s, int len) {
        String res="";
        for (int i = 0; i < len; i++) {
            res += Character.forDigit((s[i] >> 4)&0x0f, 16);
            res += Character.forDigit(s[i] & 0x0f, 16);
        }
        return res;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static class Binder extends android.os.Binder {
        private ServerSearchResponder service;

        private Binder(ServerSearchResponder s) {
            this.service = s;
        }

        public boolean isStarted() {
            return this.service.serverSocket != null && !this.service.serverSocket.isClosed();
        }
    }


    /* =========== Packet handling ============= */

    private void updateServers() {
        appendLog("Downloading server list...");
        String jsonStr = downloadString("https://warhawk.thalhammer.it/api/server/");
        try {
            JSONArray arr = new JSONArray(jsonStr);
            for(int i=0; i<arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                byte[] res = hexStringToByteArray(obj.getString("response"));
                byte[] addrBytes = Inet4Address.getByName(obj.getString("hostname")).getAddress();
                res[112] = addrBytes[0];
                res[113] = addrBytes[1];
                res[114] = addrBytes[2];
                res[115] = addrBytes[3];
                res[176] = addrBytes[0];
                res[177] = addrBytes[1];
                res[178] = addrBytes[2];
                res[179] = addrBytes[3];
                packetList.add(res);
            }
        } catch (UnknownHostException | JSONException e) {
            appendLog("Failed to download server list: " + e.getMessage());
        }
        appendLog("Added " + packetList.size() + " servers");
    }

    private String downloadString(String uri) {
        try
        {
            URL url = new URL(uri);
            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            String res = "";
            while ((str = in.readLine()) != null)
            {
                res += str + "\n";
            }
            in.close();
            return res;
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        return null;
    }

    private List<byte[]> packetList = new ArrayList<>();

    public void handlePacket(byte[] data, int len, InetAddress src) {
        if(len > 3) {
            if(data[0] == -61 && data[1] == -127) {
                appendLog("Received server request from " + src.toString());
                for(byte[] frame : packetList) {
                    try {
                        DatagramPacket pkt = new DatagramPacket(frame, frame.length, src, 10029);
                        this.serverSocket.send(pkt);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }
}
