package eu.brain.iot.robot.events;

import eu.brain.iot.robot.api.CurrentState;
import eu.brain.iot.robot.api.Mission;
import eu.brain.iot.robot.api.Result;

/*
   This event will be automatically created by ROS Edge Node when it receives the three action events,
   (only 'GoTo', 'PickCart', 'PlaceCart') because ROS Edge Node will continuously query the execution status of the commands.
   So Robot Behavior just need to wait for the 'QueryStateValueReturn' event to check the execution status.
   
   Of course, if a 'QueryState' command is explicitly sent from Robot Behavior, after receiving it, ROS Edge Node will also execute and  reply with 'QueryStateValueReturn'.
*/

public class QueryStateValueReturn extends RobotCommand {
	
	public Mission mission;
	public Result result;        //   OK | ERROR
	public CurrentState currentState;
}
