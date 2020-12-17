package eu.brain.iot.warehouse.events;

/*
 * Robot behaviour Notices the warehouse backend the cart at the picking point has been moved. 
The backend will update the table property 'isAssigned' from True to False 
 * */

public class CartMovedNotice extends WarehouseCommand{

	public String pickPoint; // 8.0,-3.6,-3.14   presenting x,y,z
}
