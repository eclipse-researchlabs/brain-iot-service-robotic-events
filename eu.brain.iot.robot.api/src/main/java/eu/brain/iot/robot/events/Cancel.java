package eu.brain.iot.robot.events;

import eu.brain.iot.robot.api.Command;
import eu.brain.iot.robot.api.RobotCommand;

public class Cancel extends RobotCommand {
	
	// sent by Robot Behavior, ROS Edge Node will cancel the current action
	
	public Command command;
}
