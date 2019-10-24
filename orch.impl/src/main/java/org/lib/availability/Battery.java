package org.lib.availability;

public class Battery {

	double time_remaining= 0;
	double time_charging= 0;
	boolean is_charging =false;
	double voltage= 0;
	double level= 0;
	public double getTime_remaining() {
		return time_remaining;
	}
	public void setTime_remaining(double time_remaining) {
		this.time_remaining = time_remaining;
	}
	public double getTime_charging() {
		return time_charging;
	}
	public void setTime_charging(double time_charging) {
		this.time_charging = time_charging;
	}
	public boolean isIs_charging() {
		return is_charging;
	}
	public void setIs_charging(boolean is_charging) {
		this.is_charging = is_charging;
	}
	public double getVoltage() {
		return voltage;
	}
	public void setVoltage(double voltage) {
		this.voltage = voltage;
	}
	public double getLevel() {
		return level;
	}
	public void setLevel(double level) {
		this.level = level;
	}
	
	
	
}
