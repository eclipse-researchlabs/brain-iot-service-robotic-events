package org.lib.availability;

public class Localization_status{
	 
	 String node ="";
		
	 boolean reliable =true;
	 
	 Pose_s pose =new Pose_s();
	 
	 String state ="READY";
		
	 String type ="";

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public boolean isReliable() {
		return reliable;
	}

	public void setReliable(boolean reliable) {
		this.reliable = reliable;
	}

	public Pose_s getPose() {
		return pose;
	}

	public void setPose(Pose_s pose) {
		this.pose = pose;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	 
	 
}
