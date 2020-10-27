package eu.brain.iot.robot.events;

import eu.brain.iot.robot.api.Command;

/*
 * Check current mission is finished or not if Robot Behavior wants to explicitly check the status
 */
public class QueryState extends RobotCommand {

	 
	public Command mission;
}
