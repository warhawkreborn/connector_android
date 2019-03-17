package it.thalhammer.warhawkreborn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class Util {
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
}
