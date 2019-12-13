package it.thalhammer.warhawkreborn.model;

import android.os.Bundle;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class UpdateInfo {
    @SerializedName("version")
    private String version;
    @SerializedName("version_code")
    private long versionCode;
    @SerializedName("changes")
    private String changes;
    @SerializedName("uri")
    private String uri;

    public static UpdateInfo fromBundle(Bundle info) {
        if(info == null) return null;
        UpdateInfo res = new UpdateInfo();
        res.setVersion(info.getString("version"));
        res.setVersionCode(info.getLong("version_code"));
        res.setChanges(info.getString("changes"));
        res.setUri(info.getString("uri"));
        return res;
    }

    public Bundle toBundle() {
        Bundle res = new Bundle();
        res.putString("version", getVersion());
        res.putLong("version_code", getVersionCode());
        res.putString("changes", getChanges());
        res.putString("uri", getUri());
        return res;
    }
}
