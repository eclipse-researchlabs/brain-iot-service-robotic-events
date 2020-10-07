package eu.brain.iot.warehouse.events;

import eu.brain.iot.robot.api.Cooridinate;

public class TaskInfo extends WarehouseCommand{
	
	public Cooridinate pickPoint;
	
	public Cooridinate storagePoint;
	
	// fixed point in the picking side in front of Door where robot will stop here for checking the door is open or not, on the way to storage area  
	public Cooridinate storageAuxliaryPoint;

}
