package eu.brain.iot.warehouse.sensinact.api;

import eu.brain.iot.eventing.api.BrainIoTEvent;

public class PickingPointUpdateNotice extends BrainIoTEvent {
    
   public String pickID;
   public boolean isAssigned; //Sensinact must update the isAssigned column after an iteration starts or finished
}