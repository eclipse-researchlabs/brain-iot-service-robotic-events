package eu.brain.iot.warehouse.sensinact.api;

import eu.brain.iot.eventing.api.BrainIoTEvent;

/*
 * used to update the Picking_Points table
 * */

public class UpdatePickPoint extends BrainIoTEvent {
    
   public String pickID;
   public Coordinate  pickPoint;
   public boolean isAssigned;
}
