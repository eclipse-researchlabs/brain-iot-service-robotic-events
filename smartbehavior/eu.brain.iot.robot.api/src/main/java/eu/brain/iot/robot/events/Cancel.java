package eu.brain.iot.robot.events;

import eu.brain.iot.robot.api.Command;

public class Cancel extends RobotCommand {
	
	// sent by Robot Behavior, ROS Edge Node will cancel the current mission
	
	public Command mission;
}
