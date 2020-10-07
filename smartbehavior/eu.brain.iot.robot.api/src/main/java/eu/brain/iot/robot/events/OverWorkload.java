package eu.brain.iot.robot.events;

import eu.brain.iot.robot.api.CurrentState;
import eu.brain.iot.robot.api.LastEvent;
import eu.brain.iot.robot.api.Result;

public class OverWorkload extends RobotCommand {
	
	/*
	   This event will only be automatically sent by ROS Edge Node when it receives the three action events,
	   (only 'GoTo', 'PickCart', 'PlaceCart') but this command will not be executed by robot, because there is already one mission running,
	   in such case, ROS Edge Node gets a response from the ROS Service and it finds the robot is busy now (indicated by the response result = error),
	   it will report using this 'OverWorkload' event to Robot Behavior, as well as the reason message. 
	   
	   Of course, if a 'QueryState' command is explicitly sent from Robot Behavior, after receiving it, ROS Edge Node will also execute and  reply with 'QueryStateValueReturn'.
	*/

	public Result result; 	  //   OK | ERROR
	public CurrentState currentState;  
	public LastEvent lastEvent;       //   internal event received by the robot, which is the reason causing the "CurrentState" changes.
	public String message;
}






/* ---------Example response got by ROS Edge Node from ROS Service, meaning the 'GoTo' command has been sent to robot successfully.

{
    "_format": "ros",
    "state": {
        "header": {
            "priority": 0,
            "stamp": {
                "secs": 0,
                "nsecs": 0
            },
            "id": 4,
            "name": ""
        },
        "current_state": "queued",
        "last_event": "added"
    },
    "result": {
        "message": "",
        "result": "ok"
    }
}*/

/* ---------Example response got by ROS Edge Node from ROS Service, meaning the 'GoTo' command will not be executed by robot,
 *  because the robot is running a mission
{
    "_format": "ros",
    "state": {
        "header": {
            "priority": 0,
            "stamp": {
                "secs": 417,
                "nsecs": 500000000
            },
            "id": -1,
            "name": ""
        },
        "current_state": "unknown",
        "last_event": "abort"
    },
    "result": {
        "message": "Could not add procedure to \"GoToComponent\" because component: \"GoToComponent\" is already running a procedure",
        "result": "error"
    }
}  */


