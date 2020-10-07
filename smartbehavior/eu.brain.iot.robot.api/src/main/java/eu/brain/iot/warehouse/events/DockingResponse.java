package eu.brain.iot.warehouse.events;

import eu.brain.iot.robot.api.Cooridinate;

public class DockingResponse extends WarehouseCommand{
	
	public Cooridinate dockingPoint;

	// fixed point in the storage side in front of Door where robot will stop here for checking the door is open or not, on the way to docking area 
	public Cooridinate dockAuxliaryPoint;
}
