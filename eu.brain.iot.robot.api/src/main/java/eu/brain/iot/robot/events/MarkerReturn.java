package eu.brain.iot.robot.events;

import eu.brain.iot.robot.api.RobotCommand;

public class MarkerReturn extends RobotCommand {
	
	/* 
	 * marker ID contained in QR Code of any objects(e.g. Cart, Door) scanned by robot camera.
	 * 
	 */
	public int markerID;
}
