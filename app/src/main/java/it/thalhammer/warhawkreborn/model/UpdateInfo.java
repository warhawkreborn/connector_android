package it.thalhammer.warhawkreborn.model;

import android.os.Bundle;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class UpdateInfo {
    @SerializedName("version")
    private String version;
    @SerializedName("changes")
    private String changes;
    @SerializedName("filename")
    private String filename;

    public static UpdateInfo fromBundle(Bundle info) {
        if(info == null) return null;
        UpdateInfo res = new UpdateInfo();
        res.setVersion(info.getString("version"));
        res.setChanges(info.getString("changes"));
        res.setFilename(info.getString("filename"));
        return res;
    }

    public Bundle toBundle() {
        Bundle res = new Bundle();
        res.putString("version", getVersion());
        res.putString("changes", getChanges());
        res.putString("filename", getFilename());
        return res;
    }
}
