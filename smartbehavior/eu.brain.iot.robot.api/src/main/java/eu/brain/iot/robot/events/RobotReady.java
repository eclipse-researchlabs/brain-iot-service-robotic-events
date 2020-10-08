package eu.brain.iot.robot.events;

/* 
 * ROS Edge Node automatically report its availability afterit’s initialized for connecting with robot 
 * After receiving this event, Robot Behaviour can start to ask for a new Task for the first iteration 
 */
public class RobotReady extends RobotCommand {
	
	public boolean isReady;
}
