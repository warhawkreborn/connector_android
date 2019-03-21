package it.thalhammer.warhawkreborn.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class CheckForwardingResponse {
    @SerializedName("info")
    private ServerInfo info;
    @SerializedName("ip")
    private String ip;
}
