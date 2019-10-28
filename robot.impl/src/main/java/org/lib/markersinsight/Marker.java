package org.lib.markersinsight;

import org.lib.availability.Header;

public class Marker {

	Header header =new Header();
	
	double confidence = 0;
	
	Marker_position_s pose =new Marker_position_s();
	
	double id =4;

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public Marker_position_s getPose() {
		return pose;
	}

	public void setPose(Marker_position_s pose) {
		this.pose = pose;
	}

	public double getId() {
		return id;
	}

	public void setId(double id) {
		this.id = id;
	}
	
	
}
