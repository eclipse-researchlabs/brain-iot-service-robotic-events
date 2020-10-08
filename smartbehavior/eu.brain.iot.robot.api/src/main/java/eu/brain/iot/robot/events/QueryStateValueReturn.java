package eu.brain.iot.robot.events;

import eu.brain.iot.robot.api.Mission;

/*
   This event will be automatically created by ROS Edge Node when it receives the five events,
   (only 'GoTo', 'PickCart', 'PlaceCart', 'Cancel', 'QueryState') because ROS Edge Node will continuously query the execution status of the commands.
   So Robot Behavior just need to wait for the 'QueryStateValueReturn' event to check the execution status.
   
  if a 'QueryState' command is explicitly sent from Robot Behavior, after receiving it, ROS Edge Node will also execute and  reply with 'QueryStateValueReturn'.
*/

public class QueryStateValueReturn extends RobotCommand {
	
	public Mission mission;
	public CurrentState currentState;
	
	public static enum CurrentState {
		unknown, finished;
	}
}
