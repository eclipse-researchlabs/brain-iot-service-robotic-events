package eu.brain.iot.robot.events;

/*
 * After robot behaviour is linitialized, it needs to check if the ROS Edge Node initiialization is done or not.
 * */
public class CheckRobotReadyRequest extends RobotCommand {

	public static final int ALL_ROBOT_BEHAVIOURS = -1;
	
	public int robotBehaviorID;
}
