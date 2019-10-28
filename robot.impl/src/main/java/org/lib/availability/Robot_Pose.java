package org.lib.availability;

public class Robot_Pose {
	double y = -3.6808252039538503;
	double x = 0.23644128868579684;
	double theta = -3.117151068232479;
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getTheta() {
		return theta;
	}
	public void setTheta(double theta) {
		this.theta = theta;
	}
	@Override
	public String toString() {
		return "Robot_Pose [y=" + y + ", x=" + x + ", theta=" + theta + "]";
	}
	public Robot_Pose(double y, double x, double theta) {
		super();
		this.y = y;
		this.x = x;
		this.theta = theta;
	}
	public Robot_Pose() {
		super();
	}
	
	
	
	
}
