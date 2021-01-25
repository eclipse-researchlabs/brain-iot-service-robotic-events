
package eu.brain.iot.robot.tables.jsonReader;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class StoragePoint {

    @SerializedName("STid")
    @Expose
    private String sTid;
    @SerializedName("storageAUX")
    @Expose
    private StorageAUX storageAUX;
    @SerializedName("storagePose")
    @Expose
    private StoragePose storagePose;

    public String getSTid() {
        return sTid;
    }

    public void setSTid(String sTid) {
        this.sTid = sTid;
    }

    public StorageAUX getStorageAUX() {
        return storageAUX;
    }

    public void setStorageAUX(StorageAUX storageAUX) {
        this.storageAUX = storageAUX;
    }

    public StoragePose getStoragePose() {
        return storagePose;
    }

    public void setStoragePose(StoragePose storagePose) {
        this.storagePose = storagePose;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("sTid", sTid).append("storageAUX", storageAUX).append("storagePose", storagePose).toString();
    }

}
