package org.lib.request.gotolocation;

import org.lib.availability.Header;
import org.lib.availability.Robot_Pose;

public class Goal {

	 Header header =new Header();
	 Robot_Pose pose =new Robot_Pose();
	public Header getHeader() {
		return header;
	}
	public void setHeader(Header header) {
		this.header = header;
	}
	public Robot_Pose getPose() {
		return pose;
	}
	public void setPose(Robot_Pose pose) {
		this.pose = pose;
	}
	
	
}
