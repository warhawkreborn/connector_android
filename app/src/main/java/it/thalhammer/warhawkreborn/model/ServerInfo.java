package it.thalhammer.warhawkreborn.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class ServerInfo {
    @SerializedName("msg")
    private String msg;
    @SerializedName("state")
    private String state;
    @SerializedName("ping")
    private String ping;
    @SerializedName("name")
    private String name;
}
