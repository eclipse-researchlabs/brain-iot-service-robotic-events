package eu.brain.iot.warehouse.sensinact.api;

import eu.brain.iot.eventing.api.BrainIoTEvent;

/*
 * used to update the Dock_Points table
 * */

public class UpdateDockPoint extends BrainIoTEvent {
    
   public int robotID;
   public Coordinate  dockAUX;   // dock Auxiliary Point
   public Coordinate dockPoint;
   
}