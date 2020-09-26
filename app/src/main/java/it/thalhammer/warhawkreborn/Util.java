package it.thalhammer.warhawkreborn;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.arch.core.util.Function;
import android.content.Context;
import androidx.core.util.Consumer;
import android.util.Log;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Util {
    private static final String LOG_TAG = Util.class.getName();

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String byteArrayToHexString(byte[] s, int len) {
        String res="";
        for (int i = 0; i < len; i++) {
            res += Character.forDigit((s[i] >> 4)&0x0f, 16);
            res += Character.forDigit(s[i] & 0x0f, 16);
        }
        return res;
    }

    public static String byteArrayToHexString(byte[] s) {
        return byteArrayToHexString(s, s.length);
    }

    public static String downloadString(String uri) {
        return downloadString(uri, false, false);
    }

    public static String downloadString(String uri, boolean forceipv4, boolean addFcmID) {
        HttpURLConnection connection = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.getAppContext());
        String fcmId = null;
        if(prefs != null && addFcmID) {
            fcmId = prefs.getString(MainActivity.getAppContext().getString(R.string.pref_fcm_id), null);
        }
        try
        {
            URL url = new URL(uri);
            connection = (HttpURLConnection)url.openConnection();
            if(fcmId != null) connection.setRequestProperty("X-Warhawk-FCMID", fcmId);
            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str;
            String res = "";
            while ((str = in.readLine()) != null)
            {
                res += str + "\n";
            }
            in.close();
            return res;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(connection != null) connection.disconnect();
        }
        return null;
    }

    public static Inet4Address resolveIPV4(String host) throws UnknownHostException {
        for(InetAddress addr : InetAddress.getAllByName(host)) {
            if(addr instanceof Inet4Address) return (Inet4Address)addr;
        }
        return null;
    }

    public static List<InetAddress> getLocalIpAddress() {
        ArrayList<InetAddress> addresses = new ArrayList<>();
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        addresses.add(inetAddress);
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e(LOG_TAG, ex.toString());
        }
        return addresses;
    }

    public static String uploadString(String url, String data) {
        return uploadString(url, data, false, false);
    }

    public static String uploadString(String uri, String data, boolean forceipv4, boolean addFcmID) {
        HttpURLConnection connection = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.getAppContext());
        String fcmId = null;
        if(prefs != null && addFcmID) {
            fcmId = prefs.getString(MainActivity.getAppContext().getString(R.string.pref_fcm_id), null);
        }
        try {
            //Create connection
            URL url = new URL(uri);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            if(fcmId != null) connection.setRequestProperty("X-Warhawk-FCMID", fcmId);

            connection.setRequestProperty("Content-Length", "" + Integer.toString(data.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches (false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
            wr.writeBytes(data);
            wr.flush ();
            wr.close ();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str;
            String res = "";
            while ((str = in.readLine()) != null)
            {
                res += str + "\n";
            }
            in.close();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    public static void copyStream(InputStream is, OutputStream os, Consumer<Integer> cb) {
        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        int done = 0;
        try {
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
                done += bytesRead;
                cb.accept(done);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch(IOException e) {}
            try {
                os.close();
            } catch(IOException e) {}
        }
    }

    public static String readAssetFile(String fileName, Context context) {
        StringBuilder returnString = new StringBuilder();
        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        try {
            fIn = context.getResources().getAssets().open(fileName, Context.MODE_PRIVATE);
            isr = new InputStreamReader(fIn);
            input = new BufferedReader(isr);
            String line = "";
            while ((line = input.readLine()) != null) {
                returnString.append(line);
            }
        } catch (Exception e) {
            e.getMessage();
        } finally {
            try {
                if (isr != null)
                    isr.close();
                if (fIn != null)
                    fIn.close();
                if (input != null)
                    input.close();
            } catch (Exception e2) {
                e2.getMessage();
            }
        }
        return returnString.toString();
    }
}
