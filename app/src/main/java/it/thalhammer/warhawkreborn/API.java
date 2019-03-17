package it.thalhammer.warhawkreborn;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.thalhammer.warhawkreborn.model.ServerList;
import it.thalhammer.warhawkreborn.networking.DiscoveryPacket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import static it.thalhammer.warhawkreborn.Util.*;

public class API {
    public static final String LOG_TAG = API.class.getName();

    public static ServerList getServerList() {
        String jsonStr = downloadString("https://warhawk.thalhammer.it/api/server/");
        JsonParser parser = new JsonParser();
        JsonElement mJson =  parser.parse(jsonStr);
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        ServerList result = gson.fromJson(mJson, ServerList.class);
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
}
