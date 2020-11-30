package eu.brain.iot.robot.api;

import eu.brain.iot.eventing.api.BrainIoTEvent;

public abstract class RobotCommand extends BrainIoTEvent {

	public static final int ALL_ROBOTS = -1;
	
	public int robotID;
}
