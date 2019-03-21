package it.thalhammer.warhawkreborn.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class AddHostResponse {

    @SerializedName("state")
    private String state;
    @SerializedName("info")
    private ServerInfo info;

    public boolean isOk() { return state != null && state.equals("ok"); }
}
