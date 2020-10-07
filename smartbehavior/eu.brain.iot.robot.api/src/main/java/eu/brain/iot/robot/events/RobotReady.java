package eu.brain.iot.robot.events;

public class RobotReady extends RobotCommand {
	
	/* 
	 * ROS Edge Node automatically publish this event to be received by Robot Behaviour,
	 * then Robot Behaviour can start to search a new Task
	 */
	public boolean isReady;
}
