package org.lib.markersinsight;

public class Marker_position {

	Position_XYZ position =new Position_XYZ();
	
	Orientation orientation =new Orientation();

	public Position_XYZ getPosition() {
		return position;
	}

	public void setPosition(Position_XYZ position) {
		this.position = position;
	}

	public Orientation getOrientation() {
		return orientation;
	}

	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}
	
	
	
}
