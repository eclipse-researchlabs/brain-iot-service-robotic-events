package org.lib.markersinsight;

import org.lib.availability.Header;

public class Marker_position_s {

	Header header =new Header();
	
	Marker_position pose =new Marker_position();

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public Marker_position getPose() {
		return pose;
	}

	public void setPose(Marker_position pose) {
		this.pose = pose;
	}
	
	
	
	
}
