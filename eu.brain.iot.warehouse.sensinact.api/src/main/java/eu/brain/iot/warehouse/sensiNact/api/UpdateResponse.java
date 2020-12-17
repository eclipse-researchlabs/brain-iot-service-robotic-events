package eu.brain.iot.warehouse.sensiNact.api;

/*
 * this event is sent from warehouse backend
 * */

public class UpdateResponse extends SensiNactCommand {

	public UpdateStatus updateStatus; // Acknowledge response from warehouse backend
	
	public static enum UpdateStatus {
		OK, ERROR;
	}
	
}
