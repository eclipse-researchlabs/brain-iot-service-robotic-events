
package eu.brain.iot.robot.tables.jsonReader;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class PickingPoint {

    @SerializedName("PPid")
    @Expose
    private String pPid;
    @SerializedName("pose")
    @Expose
    private Pose pose;
    @SerializedName("isAssigned")
    @Expose
    private Boolean isAssigned;

    public String getPPid() {
        return pPid;
    }

    public void setPPid(String pPid) {
        this.pPid = pPid;
    }

    public Pose getPose() {
        return pose;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
    }

    public Boolean getIsAssigned() {
        return isAssigned;
    }

    public void setIsAssigned(Boolean isAssigned) {
        this.isAssigned = isAssigned;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("pPid", pPid).append("pose", pose).append("isAssigned", isAssigned).toString();
    }

}
