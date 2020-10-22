package eu.brain.iot.robot.events;

/* 
 * ROS Edge Node report if it has initialized for connecting with robot 
 * After receiving this event, Robot Behaviour can start to ask warehouse backend for a new pickup Task for the first iteration 
 */
public class RobotReadyResponse extends RobotCommand {
	
	public static final int ALL_ROBOT_BEHAVIOURS = -1;
	
	public int robotBehaviorID;
	
	public boolean isReady;
}
