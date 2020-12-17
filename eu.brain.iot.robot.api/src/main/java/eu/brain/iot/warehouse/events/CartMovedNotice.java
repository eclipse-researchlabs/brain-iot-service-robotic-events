package eu.brain.iot.warehouse.events;

import eu.brain.iot.robot.api.RobotCommand;

/*
 * Robot behaviour Notices the warehouse backend the cart at the picking point has been moved. 
The backend will update the table property 'isAssigned' from True to False 
 * */

public class CartMovedNotice extends RobotCommand{

	public String pickPoint; // 8.0,-3.6,-3.14   presenting x,y,z
}
