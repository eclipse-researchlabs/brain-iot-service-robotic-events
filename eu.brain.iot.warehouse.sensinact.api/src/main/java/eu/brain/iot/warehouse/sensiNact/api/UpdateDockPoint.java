package eu.brain.iot.warehouse.sensiNact.api;

/*
 * used to update the Dock_Points table
 * */

public class UpdateDockPoint extends SensiNactCommand {
    
   public String robotIP;
   public String  dockAUX;   // dock Auxiliary Coordinate
   public String dockPoint;  // Coordinate
   
}