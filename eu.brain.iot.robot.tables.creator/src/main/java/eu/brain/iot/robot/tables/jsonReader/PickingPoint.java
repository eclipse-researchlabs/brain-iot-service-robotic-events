/*******************************************************************************
 * Copyright (C) 2021 LINKS Foundation
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
