package org.lib.availability;

public class Stamp{
	 
	 double secs =3398;
	 double nsecs =334000000;
	public double getSecs() {
		return secs;
	}
	public void setSecs(double secs) {
		this.secs = secs;
	}
	public double getNsecs() {
		return nsecs;
	}
	public void setNsecs(double nsecs) {
		this.nsecs = nsecs;
	}
	@Override
	public String toString() {
		return "Stamp [secs=" + secs + ", nsecs=" + nsecs + "]";
	}
	public Stamp(double secs, double nsecs) {
		super();
		this.secs = secs;
		this.nsecs = nsecs;
	}
	 
	public Stamp() {

	}	
	 
}