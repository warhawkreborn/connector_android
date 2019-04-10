package it.thalhammer.warhawkreborn.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class AddHostRequest {
    // Or null to use the requesting IP address
    @SerializedName("hostname")
    private String hostname;
    // True to keep the server even when offline
    // False server will get removed once offline
    @SerializedName("persistent")
    private boolean persistent;
    // FCM Token used for notifications
    @SerializedName("fcm_id")
    private String fcmId;
}
