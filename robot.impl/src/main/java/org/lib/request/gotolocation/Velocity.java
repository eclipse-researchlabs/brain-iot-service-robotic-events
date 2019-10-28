package org.lib.request.gotolocation;

public class Velocity {

	double linear_x = 0.0;
	double linear_y = 0.0;
	double angular_z = 0.0;
	public double getLinear_x() {
		return linear_x;
	}
	public void setLinear_x(double linear_x) {
		this.linear_x = linear_x;
	}
	public double getLinear_y() {
		return linear_y;
	}
	public void setLinear_y(double linear_y) {
		this.linear_y = linear_y;
	}
	public double getAngular_z() {
		return angular_z;
	}
	public void setAngular_z(double angular_z) {
		this.angular_z = angular_z;
	}
	public Velocity(double linear_x, double linear_y, double angular_z) {
		super();
		this.linear_x = linear_x;
		this.linear_y = linear_y;
		this.angular_z = angular_z;
	}
	public Velocity() {
		super();
	}
	
	
	
}
