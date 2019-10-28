package org.lib.availability;

public class Robot_status {

	 boolean emergency_stop =true;
	 Battery battery =new Battery();
	public boolean isEmergency_stop() {
		return emergency_stop;
	}
	public void setEmergency_stop(boolean emergency_stop) {
		this.emergency_stop = emergency_stop;
	}
	public Battery getBattery() {
		return battery;
	}
	public void setBattery(Battery battery) {
		this.battery = battery;
	}
	 
	 
}
