package eu.brain.iot.warehouse.sensiNact.api;

/*
 * used to update the Storage_Points table
 * */

public class UpdateStoragePoint extends SensiNactCommand {
    
   public String storageID;
   public String  storageAUX;   // Storage Auxiliary Coordinate
   public String storagePoint;   // STORAGE Coordinate
   
}