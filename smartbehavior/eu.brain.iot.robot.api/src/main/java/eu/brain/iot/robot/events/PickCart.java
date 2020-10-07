package eu.brain.iot.robot.events;

import eu.brain.iot.robot.api.Mission;

public class PickCart extends RobotCommand {
	
	/* 
	 * Used for matching with the mission of QueryStateValueReturn received by Robot Behaviour 
	 */
	public Mission mission = Mission.PICK;
	
	public int markerID;
}
