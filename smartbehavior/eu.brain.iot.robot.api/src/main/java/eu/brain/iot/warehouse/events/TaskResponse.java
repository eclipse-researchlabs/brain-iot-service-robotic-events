package eu.brain.iot.warehouse.events;

import eu.brain.iot.robot.api.Cooridinate;

/*
 * Response of warehouse manager with the new task info to Robot Behavior
*/

public class TaskResponse extends WarehouseCommand{
	
	public boolean hasNewTask = false;
	
	public Cooridinate pickPoint;
	
	public Cooridinate storagePoint;
	
	// fixed point in the picking side in front of Door where robot will stop here for checking the door is open or not, on the way to storage area  
	public Cooridinate storageAuxliaryPoint;

}