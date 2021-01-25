
package eu.brain.iot.robot.tables.jsonReader;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class DockingPoint {

    @SerializedName("IPid")
    @Expose
    private String iPid;
    @SerializedName("dockAUX")
    @Expose
    private DockAUX dockAUX;
    @SerializedName("dockPose")
    @Expose
    private DockPose dockPose;

    public String getIPid() {
        return iPid;
    }

    public void setIPid(String iPid) {
        this.iPid = iPid;
    }

    public DockAUX getDockAUX() {
        return dockAUX;
    }

    public void setDockAUX(DockAUX dockAUX) {
        this.dockAUX = dockAUX;
    }

    public DockPose getDockPose() {
        return dockPose;
    }

    public void setDockPose(DockPose dockPose) {
        this.dockPose = dockPose;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("iPid", iPid).append("dockAUX", dockAUX).append("dockPose", dockPose).toString();
    }

}
