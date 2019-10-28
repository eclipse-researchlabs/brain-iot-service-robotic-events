package org.lib.availability;

public class Pose_s{
	 
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
	@Override
	public String toString() {
		return "Pose_s [header=" + header.toString() + ", pose=" + pose + "]";
	}	 


}