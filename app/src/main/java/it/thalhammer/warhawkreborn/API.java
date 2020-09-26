package it.thalhammer.warhawkreborn;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.thalhammer.warhawkreborn.model.*;
import it.thalhammer.warhawkreborn.networking.DiscoveryPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class API {
    private static final String LOG_TAG = API.class.getName();
    private static final String API_BASE = "https://warhawk.thalhammer.it/api/";

    private static <T> T getObject(String endpoint, Class<T> tClass, boolean forceipv4) {
        String jsonStr = Util.downloadString(API_BASE + endpoint, forceipv4, true);
        if(jsonStr==null) return null;
        JsonParser parser = new JsonParser();
        JsonElement mJson =  parser.parse(jsonStr);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        return gson.fromJson(mJson, tClass);
    }

    private static <T> T postObject(String endpoint, Object param,  Class<T> tClassRet, boolean forceipv4) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        String jsonStr = gson.toJson(param);
        jsonStr = Util.uploadString(API_BASE + endpoint, jsonStr, forceipv4, true);
        if(jsonStr==null) return null;
        JsonParser parser = new JsonParser();
        JsonElement mJson =  parser.parse(jsonStr);
        return gson.fromJson(mJson, tClassRet);
    }

    static ServerList getServerList() {
        ServerList result = getObject("server/", ServerList.class, false);
        if(result == null) return null;
        ServerList checked = new ServerList();
        for(ServerList.Entry e : result) {
            if(!e.isOnline()) continue;
            if(e.getResponse().length() != 744) continue;
            try {
                DiscoveryPacket packet = new DiscoveryPacket(e.getResponse());
                packet.setIP(Util.resolveIPV4(e.getHostname()));
                e.setResponse(packet.getHexString());
                checked.add(e);
            } catch (UnknownHostException ex) {
                Log.e(LOG_TAG, "Failed to download server list", ex);
            }
        }
        return checked;
    }

    public static CheckForwardingResponse checkForwarding() {
        // We need to make sure we access this endpoint via ipv4 or warhawk discovery wont work.
        return getObject("server/checkForwarding", CheckForwardingResponse.class, true);
    }

    public static AddHostResponse addHost(boolean persistent, String fcmid) {
        AddHostRequest req = new AddHostRequest();
        req.setPersistent(persistent);
        if(fcmid != null) req.setFcmId(fcmid);
        return postObject("server/", req, AddHostResponse.class, true);
    }

    public static UpdateInfo getUpdateInfo() {
        return getObject("app/versionInfo", UpdateInfo.class, false);
    }
}
