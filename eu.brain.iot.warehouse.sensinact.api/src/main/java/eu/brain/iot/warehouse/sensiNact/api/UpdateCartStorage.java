package eu.brain.iot.warehouse.sensiNact.api;

/*
 * used to update the Cart_Storage table
 * */

public class UpdateCartStorage extends SensiNactCommand {
    
   public int cartID;
   public String storageID;  // corresponses to the storageID in Storage_Points table
   

}