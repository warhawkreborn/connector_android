package it.thalhammer.warhawkreborn.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;

public class ServerList extends ArrayList<ServerList.Entry> {
    @Data
    public class Entry {
        @SerializedName("id")
        private int id;
        @SerializedName("name")
        private String name;
        @SerializedName("ping")
        private int ping;
        @SerializedName("response")
        private String response;
        @SerializedName("state")
        private String state;
        @SerializedName("hostname")
        private String hostname;
        @SerializedName("created")
        private Date created;

        public boolean isOnline() {
            return state != null && state.equals("online");
        }
    }
}
