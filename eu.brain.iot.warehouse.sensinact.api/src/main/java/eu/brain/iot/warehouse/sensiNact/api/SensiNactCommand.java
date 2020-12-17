package eu.brain.iot.warehouse.sensiNact.api;

import eu.brain.iot.eventing.api.BrainIoTEvent;

public class SensiNactCommand extends BrainIoTEvent {
	
	public static final boolean SENSINACT_CMD = true;
	
	public boolean isSensiNactCMD = SENSINACT_CMD;  // to be used by warehouse backend for filtering the events

}
