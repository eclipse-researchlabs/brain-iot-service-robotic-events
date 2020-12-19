package org.lib.availability;

public class Sensor {

	
	
	String state=  "READY";
	String type = "robot_local_control_components/LaserComponent";
	String name = "LaserComponent";
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
	
}
