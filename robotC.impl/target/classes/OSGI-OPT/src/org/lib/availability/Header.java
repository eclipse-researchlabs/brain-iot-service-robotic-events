package org.lib.availability;

public class Header{
	 
	 Stamp stamp =new Stamp();
	 String frame_id ="rb1_base_a_map";
	 int seq =0;
	public Stamp getStamp() {
		return stamp;
	}
	public void setStamp(Stamp stamp) {
		this.stamp = stamp;
	}
	public String getFrame_id() {
		return frame_id;
	}
	public void setFrame_id(String frame_id) {
		this.frame_id = frame_id;
	}
	public int getSeq() {
		return seq;
	}
	public void setSeq(int seq) {
		this.seq = seq;
	}
	@Override
	public String toString() {
		return "Header [stamp=" + stamp.toString() + ", frame_id=" + frame_id + ", seq="
				+ seq + "]";
	}
	
	

}