package eu.brain.iot.warehouse.sensiNact.api;

/*
 * used to update the Picking_Points table
 * */

public class UpdatePickPoint extends SensiNactCommand {
    
   public String pickID;
   public String  pickPoint;  // coordinate: 8.0,-3.6,-3.14   presenting x,y,z
   public boolean isAssigned;
}
