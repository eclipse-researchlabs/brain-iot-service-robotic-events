package eu.brain.iot.robot.api;

public class Cooridinate {
	double y = 0;
	double x = 0;
	double z = 0;
	
	public Cooridinate() {

	}
	
	public Cooridinate(double y, double x, double z) {
		this.y = y;
		this.x = x;
		this.z = z;
	}
	
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
	public double getZ() {
		return z;
	}
	public void setZ(double z) {
		this.z = z;
	}
	@Override
	public String toString() {
		return "Cooridinate [y=" + y + ", x=" + x + ", z=" + z + "]";
	}

	
}
