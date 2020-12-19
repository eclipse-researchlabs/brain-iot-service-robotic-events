package com.paremus.brain.iot.example.door.api;

import com.paremus.brain.iot.example.door.api.DoorStatus.State;

import eu.brain.iot.eventing.api.BrainIoTEvent;

public class DoorStatusResponse extends BrainIoTEvent {

	public String doorId;
	
	public State state;
	
}