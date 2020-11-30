package eu.brain.iot.warehouse.events;

import eu.brain.iot.robot.api.RobotCommand;

public class NewStoragePointRequest extends RobotCommand{

	public int markerID;  // cart marker ID, is used to identify the Place position.
	
}