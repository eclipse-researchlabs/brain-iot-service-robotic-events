package eu.brain.iot.warehouse.sensinact.api;

import eu.brain.iot.eventing.api.BrainIoTEvent;

/*
 * used to update the Storage_Points table
 * */

public class UpdateStoragePoint extends BrainIoTEvent {
    
   public String storageID;
   public Coordinate  storageAUX;   // Storage Auxiliary Point
   public Coordinate storagePoint;
   
}