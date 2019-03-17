package it.thalhammer.warhawkreborn;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import it.thalhammer.warhawkreborn.model.ServerList;
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
                byte[] res = hexStringToByteArray(e.getResponse());
                byte[] addrBytes = Inet4Address.getByName(e.getHostname()).getAddress();
                res[112] = addrBytes[0];
                res[113] = addrBytes[1];
                res[114] = addrBytes[2];
                res[115] = addrBytes[3];
                res[176] = addrBytes[0];
                res[177] = addrBytes[1];
                res[178] = addrBytes[2];
                res[179] = addrBytes[3];
                e.setResponse(byteArrayToHexString(res, res.length));
            } catch (UnknownHostException ex) {
                Log.e(LOG_TAG, "Failed to download server list", ex);
            }
        }
        return result;
    }
}
