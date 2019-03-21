package it.thalhammer.warhawkreborn;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.thalhammer.warhawkreborn.model.*;
import it.thalhammer.warhawkreborn.networking.DiscoveryPacket;
import java.net.Inet4Address;
import java.net.UnknownHostException;

public class API {
    private static final String LOG_TAG = API.class.getName();
    private static final String API_BASE = "https://warhawk.thalhammer.it/api/";

    private static <T> T getObject(String endpoint, Class<T> tClass) {
        String jsonStr = Util.downloadString(API_BASE + endpoint);
        if(jsonStr==null) return null;
        JsonParser parser = new JsonParser();
        JsonElement mJson =  parser.parse(jsonStr);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        return gson.fromJson(mJson, tClass);
    }

    private static <T> T postObject(String endpoint, Object param,  Class<T> tClassRet) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        String jsonStr = gson.toJson(param);
        jsonStr = Util.uploadString(API_BASE + endpoint, jsonStr);
        if(jsonStr==null) return null;
        JsonParser parser = new JsonParser();
        JsonElement mJson =  parser.parse(jsonStr);
        return gson.fromJson(mJson, tClassRet);
    }

    static ServerList getServerList() {
        ServerList result = getObject("server/", ServerList.class);
        if(result == null) return null;
        for(ServerList.Entry e : result) {
            if(!e.isOnline()) continue;
            try {
                DiscoveryPacket packet = new DiscoveryPacket(e.getResponse());
                packet.setIP(Inet4Address.getByName(e.getHostname()));
                e.setResponse(packet.getHexString());
            } catch (UnknownHostException ex) {
                Log.e(LOG_TAG, "Failed to download server list", ex);
            }
        }
        return result;
    }

    public static CheckForwardingResponse checkForwarding() {
        return getObject("server/checkForwarding", CheckForwardingResponse.class);
    }

    public static AddHostResponse addHost(boolean persistent) {
        AddHostRequest req = new AddHostRequest();
        req.setPersistent(persistent);
        return postObject("server/", req, AddHostResponse.class);
    }

    public static UpdateInfo getUpdateInfo() {
        return getObject("app/versionInfo", UpdateInfo.class);
    }
}
