package eu.brain.iot.robot.events;

import eu.brain.iot.robot.api.RobotCommand;

public class RobotReadyBroadcast extends RobotCommand {
	
	public String robotIP;
	
	public boolean isReady;
}
