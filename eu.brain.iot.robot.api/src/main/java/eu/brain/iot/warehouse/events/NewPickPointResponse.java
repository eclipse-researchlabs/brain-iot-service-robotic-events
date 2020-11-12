package eu.brain.iot.warehouse.events;

import eu.brain.iot.robot.api.Cooridinate;

/*
 * Response of warehouse backend with the new picking point info to Robot Behavior
*/

public class NewPickPointResponse extends WarehouseCommand{
	
	public boolean hasNewPoint = false;
	
	public Cooridinate pickPoint;
	
	

}
