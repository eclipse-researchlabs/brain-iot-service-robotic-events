package com.paremus.brain.iot.example.door.impl;

import org.apache.felix.service.command.CommandProcessor;
import org.osgi.service.component.annotations.Component;

import com.paremus.brain.iot.example.orch.api.DoorClose;
import com.paremus.brain.iot.example.orch.api.DoorOpen;
import com.paremus.brain.iot.example.orch.api.InetRobot;

import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.BrainIoTEvent;
import eu.brain.iot.eventing.api.SmartBehaviour;

@Component(
		property = {
				CommandProcessor.COMMAND_SCOPE + "=DOOR", //
				CommandProcessor.COMMAND_FUNCTION + "=door" //
		},service= {SmartBehaviour.class, ComponentImpl.class}
	)

@SmartBehaviourDefinition(consumed = {DoorOpen.class, DoorClose.class,InetRobot.class} ,    
author = "UGA", name = "Smart Door",
description = "Implements a remote Smart Door.")

public class ComponentImpl implements SmartBehaviour<BrainIoTEvent>{

	public static DoorClose close ;	
	
	public static DoorOpen open ;
	
	public static InetRobot inetrobot;
	
	private Command cmd =new Command();
	
	  String IP ;
	
	  String PORT ;
	
	
	public void notify(BrainIoTEvent event) {
		if (event instanceof DoorOpen) {
			 open = (DoorOpen) event;
			synchronized (this) {
				
				cmd.writeOpenDoor(IP,PORT);

			}
			
		}else {
			if (event instanceof DoorClose) {
				 close = (DoorClose) event;
				synchronized (this) {

					cmd.writeCloseDoor(IP,PORT);
				}
				
			}else {
				if (event instanceof InetRobot) {
					inetrobot = (InetRobot) event;
					synchronized (this) {
						IP= inetrobot.ip;
						PORT=inetrobot.port;
						
					}
					
				}else {
					
				
				
				System.out.println("Argh! Received an unknown event type " + event.getClass());
				
				}
			}
		}
		
	}
	

    
}
