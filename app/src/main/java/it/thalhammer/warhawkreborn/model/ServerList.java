package it.thalhammer.warhawkreborn.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;

public class ServerList extends ArrayList<ServerList.Entry> {
    @Data
    public class Entry {
        private int id;
        private String name;
        private int ping;
        private String response;
        private String state;
        private String hostname;
        private Date created;

        public boolean isOnline() {
            return getState().equals("online");
        }
    }
}
