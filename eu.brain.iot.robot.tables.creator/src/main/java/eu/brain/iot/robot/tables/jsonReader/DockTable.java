
package eu.brain.iot.robot.tables.jsonReader;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class DockTable {

    @SerializedName("Docking_Points")
    @Expose
    private List<DockingPoint> dockingPoints = null;

    public List<DockingPoint> getDockingPoints() {
        return dockingPoints;
    }

    public void setDockingPoints(List<DockingPoint> dockingPoints) {
        this.dockingPoints = dockingPoints;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("dockingPoints", dockingPoints).toString();
    }

}
