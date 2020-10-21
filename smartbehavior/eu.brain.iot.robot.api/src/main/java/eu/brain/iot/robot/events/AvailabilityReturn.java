package eu.brain.iot.robot.events;

import eu.brain.iot.robot.api.Cooridinate;

public class AvailabilityReturn extends RobotCommand {
	
	public OperationState operationState;
	public String navigationType;              // GoToComponent | PickComponent |  PlaceComponent | None
	public Cooridinate currentPosition;

	public static enum OperationState {
		idle, moving;
	}
}


/* Availability: check the overall availability of robot, including the current location
 * possible values in ROS service:

when mission is being executed by the robot:
{
    "operation_state": "moving",
    "robot_state": "standby",
    "navigation_status": {
        "state": "",
        "type": "GoToComponent,"
    },
    "localization_status": {
        .......
            "pose": {
                "y": -5.4574489731562705,
                "x": 6.533845601173178,
                "theta": 1.7921496156071322
            }
        .......
            
 */

/* when  GoTo mission almost done.
 * {
    "operation_state": "idle",
    "robot_state": "standby",
    "navigation_status": {
        "state": "",
        "type": "GoToComponent,"
    },
    "localization_status": {
    	
    	*/

/* when mission is  completely done
{
    "operation_state": "idle",
    "robot_state": "standby",
    "navigation_status": {
        "state": "",
        "type": "None"
    },
    "localization_status": {
        "node": "-1",
        "reliable": true,
*/

