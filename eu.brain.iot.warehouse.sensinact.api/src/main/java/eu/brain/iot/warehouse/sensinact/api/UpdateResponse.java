package eu.brain.iot.warehouse.sensinact.api;

import eu.brain.iot.eventing.api.BrainIoTEvent;

public class UpdateResponse extends BrainIoTEvent {

	public UpdateStatus updateStatus;
	
	public static enum UpdateStatus {
		OK, ERROR;
	}
	
}
