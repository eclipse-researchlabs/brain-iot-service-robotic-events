package com.paremus.brain.iot.example.door.api;

import eu.brain.iot.eventing.api.BrainIoTEvent;

public class DoorStatus extends BrainIoTEvent {

	public static final String ALL_DOORS = "ALL_DOORS";
	
	public String doorId;
	
	public State targetState;
	
	public static enum State {
		OPEN, CLOSED, QUERY;
	}
}
