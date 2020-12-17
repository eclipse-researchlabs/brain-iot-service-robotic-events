package eu.brain.iot.warehouse.sensiNact.api;

/*
 * this event is sent from warehouse backend
 * */

public class PickingPointUpdateNotice extends SensiNactCommand {
    
   public String pickID;

   //after receiving this event from warehouse backend, Sensinact must update the isAssigned column after an iteration starts or finished
   public boolean isAssigned; 
}