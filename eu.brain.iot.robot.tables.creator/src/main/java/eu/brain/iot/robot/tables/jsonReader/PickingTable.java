
package eu.brain.iot.robot.tables.jsonReader;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class PickingTable {

    @SerializedName("Picking_Points")
    @Expose
    private List<PickingPoint> pickingPoints = null;

    public List<PickingPoint> getPickingPoints() {
        return pickingPoints;
    }

    public void setPickingPoints(List<PickingPoint> pickingPoints) {
        this.pickingPoints = pickingPoints;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("pickingPoints", pickingPoints).toString();
    }

}
