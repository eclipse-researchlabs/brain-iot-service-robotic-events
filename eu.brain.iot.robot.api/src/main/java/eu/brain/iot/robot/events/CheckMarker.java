package eu.brain.iot.robot.events;

import eu.brain.iot.robot.api.RobotCommand;

public class CheckMarker extends RobotCommand {

	/* 
	 * after received this command, robot will scan the QR Code its camera can see, 
	 * ROS Edge Node will reply with the 'CheckValueReturn' with the scanned marker ID.
	 * 
	 */
}
