package eu.brain.iot.warehouse.sensinact.api;

import eu.brain.iot.eventing.api.BrainIoTEvent;

/*
 * used to update the Cart_Storage table
 * */

public class UpdateCartStorage extends BrainIoTEvent {
    
   public int cartID;
   public String storageID;  // corresponses to the storageID in Storage_Points table
   

}