/*******************************************************************************
 * Copyright (C) 2021 LINKS Foundation
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package eu.brain.iot.robot.behaviour;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.sensinact.gateway.brainiot.door.api.CloseDoorResponse;
import org.eclipse.sensinact.gateway.brainiot.door.api.CommandDoorStatus;
import org.eclipse.sensinact.gateway.brainiot.door.api.DoorStatus;
import org.eclipse.sensinact.gateway.brainiot.door.api.DoorStatusRequest;
import org.eclipse.sensinact.gateway.brainiot.door.api.DoorStatusResponse;
import org.eclipse.sensinact.gateway.brainiot.door.api.OpenDoorRequest;
import org.eclipse.sensinact.gateway.brainiot.door.api.OpenDoorResponse;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.component.annotations.Reference;
import java.util.function.Predicate;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.BrainIoTEvent;
import eu.brain.iot.eventing.api.EventBus;
import eu.brain.iot.eventing.api.SmartBehaviour;
import eu.brain.iot.robot.api.Command;
import eu.brain.iot.robot.events.*;
import eu.brain.iot.robot.events.QueryStateValueReturn.CurrentState;
import eu.brain.iot.robot.api.RobotCommand;
import eu.brain.iot.warehouse.events.CartMovedNotice;
import eu.brain.iot.warehouse.events.CartNoticeResponse;
import eu.brain.iot.warehouse.events.DockingRequest;
import eu.brain.iot.warehouse.events.DockingResponse;
import eu.brain.iot.warehouse.events.NewPickPointRequest;
import eu.brain.iot.warehouse.events.NewPickPointResponse;
import eu.brain.iot.warehouse.events.NewStoragePointRequest;
import eu.brain.iot.warehouse.events.NewStoragePointResponse;
import eu.brain.iot.warehouse.events.NoCartNotice;

@Component(
		immediate=true,
		configurationPid = "eu.brain.iot.example.robot.RobotBehavior", 
		configurationPolicy = ConfigurationPolicy.OPTIONAL,
		scope=ServiceScope.SINGLETON,
		service = {SmartBehaviour.class})

@SmartBehaviourDefinition(consumed = { NewPickPointResponse.class, NewStoragePointResponse.class, DockingResponse.class, 
		CartNoticeResponse.class, MarkerReturn.class, QueryStateValueReturn.class, RobotReadyBroadcast.class,
		DoorStatusResponse.class, OpenDoorResponse.class, CloseDoorResponse.class, AvailabilityReturn.class, BroadcastACK.class},
		author = "LINKS", name = "Robot Behavior", 
		filter = "(timestamp=*)",
		description = "Implements a Robot Behavior.")

public class RobotBehaviour implements SmartBehaviour<BrainIoTEvent> {

	private static volatile int robotID;
	private static volatile String robotIP;
	private static volatile boolean robotReady = false;
	private static volatile QueryStateValueReturn queryReturn;
	private static volatile int markerID = 0;
	private static volatile int newMarkerCounter = 0;
	private static volatile int currentMarkerCounter = 0;
	private boolean isDoorOpen = false;
	private static volatile NewPickPointResponse pickResponse = null;
	private static NewStoragePointResponse storageResponse = null;
	private static DockingResponse dockingResponse = null;
	private static CartNoticeResponse cartNoticeResponse = null;
	private ConfigurationAdmin cm;
	private static volatile String UUID;
	private static volatile boolean receivedBroadcast = false;
	private static volatile boolean broadcastACK = false;
	private static volatile DoorStatus doorStatus = null;
	private static volatile OpenDoorResponse openDoorResponse = null;
	private static volatile CloseDoorResponse closeDoorResponse = null;
	private static StateMachineMonitoring sm;
	

	@ObjectClassDefinition
	public static @interface Config {
		String logPath() default "/opt/fabric/resources/logback.xml";
	}
	
	private  Logger logger;
	
	private ExecutorService worker;
//	private ServiceRegistration<?> reg;

	@Reference
	private EventBus eventBus;
	
	@Reference
    void setConfigurationAdmin(ConfigurationAdmin cm) {
        this.cm = cm;
    }

	@Activate
	void activate(BundleContext context, Config config, Map<String, Object> props) {
		
		System.setProperty("logback.configurationFile", config.logPath());
		
		logger = (Logger) LoggerFactory.getLogger(RobotBehaviour.class.getSimpleName());
		
		UUID = context.getProperty("org.osgi.framework.uuid");
		
		logger.info("\n Hello!  I am Robot Behavior for the demo : " + robotID + ",  UUID = "+UUID);
		System.out.println("\n Hello!  I am Robot Behavior : " + robotID + ",  UUID = "+UUID);
		
		//sm = new StateMachineMonitoring("192.168.52.120", 4445);
               sm = new StateMachineMonitoring("192.168.2.167", 4445);

        sm.startMonitorning();
        logger.info("--SM: The monitoring is started, server host name:"+ sm.getServerHostName() +" port"+sm.getServerPort());

		worker = Executors.newFixedThreadPool(10);

		 onStart();
	}

	

	public void onStart() {
		
		worker.execute(() -> {

			boolean nextIteration = true;
			int pickCounter = 1;
			int storageCounter = 1;

			while (nextIteration) {

				if (broadcastACK && robotReady) {

					boolean query = true;
					String pickPoint = null;
					pickCounter = 1;
					storageCounter = 1;
					
					logger.info("--------------------------- Start New Iteration --------------------------------------");
					
					// --------------------------- Query Pick point --------------------------------------
					logger.info("--------------------------- Query Pick point --------------------------------------");
					while (query) {

						NewPickPointRequest pickRequest = new NewPickPointRequest();
						pickRequest.robotID = this.robotID;
						RobotBehaviour.pickResponse = null;
						eventBus.deliver(pickRequest);

						waitPickResponse();

						if (RobotBehaviour.pickResponse.hasNewPoint) {	
							logger.info("----------- has new Pick Point = true-------------");
							pickPoint = getPickResponse().pickPoint;
							if(pickPoint == null) {
								logger.info("-->no pick point, RB exit!");
								stop();
							}
							logger.info("-->RB" + robotID + " get new Pick Point: " + pickPoint);
							System.out.println("-->RB" + robotID + " get new Pick Point: " + pickPoint);
							break;
							
						} else {

							if (pickCounter > 0) { // just ask for 2 times
								logger.info("-->RB" + robotID + " doesn't get any Pick Point, continue to query after 10s");
								// TODO continue to query new pick point
								try {
									TimeUnit.SECONDS.sleep(10);
								} catch (InterruptedException e) {
									logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
								}
								pickCounter--;
								continue;
							} else {
								nextIteration = false;
								logger.info("-->RB" + robotID + " doesn't get any Pick Point, all carts have been moved, exit! ");
								break;
							}

						}
					} // while

					if (nextIteration) { // it means a new pick point is found, the break is not because no pick point is found
						
						// --------------------------- Go to Picking point --------------------------------------
						logger.info("--------------------------- Go to Picking point --------------------------------------");

						if (!executeGoTo(pickPoint, "Picking point")) {
							sm.UDPSend("GoToPickPointFailed");
							logger.info("-->SM: send UDP message GoToPickPointFailed" );
							break; // execution failed
							
						}
						sm.UDPSend("GoToPickPointSuccess");
						logger.info("-->SM: send UDP message GoToPickPointSuccess" );
						
						// --------------------------- check Cart Marker --------------------------------------
						logger.info("--------------------------- check Cart Marker --------------------------------------");

						CheckMarker checkMarker = createCheckMarker(); // CheckMarker
						eventBus.deliver(checkMarker);
						logger.info("-->RB" + robotID + " sending CheckMarker");

						int newMarkerID = waitMarker();
						logger.info("-->RB" + robotID + " got new MarkerID = " + newMarkerID);
						System.out.println("-->RB" + robotID + " got new MarkerID = " + newMarkerID);

						// ---------------------------TODO: No Cart Notice----------THEN Cancel current
						// mission? how?--how to handle no marker found on topic in ros node.---can't
						// always wait.-----------------------

						// 1. how to know no cart here, then run:

						/*
						 * NoCartNotice noCartNotice = createNoCartNotice(); cartNoticeResponse = null;
						 * eventBus.deliver(noCartNotice); System.out.println("-->RB" + robotID +
						 * " sending NoCartNotice");
						 * 
						 * waitCartNoticeResponse(); // noticeStatus = "OK" continue;
						 */
						// ---------------------------TODO: Cancel current action after detecting  Anomaly--------------------------------------

						// --------------------------- Pick Cart --------------------------------------
						
						sm.UDPSend("CartMarker");
						logger.info("-->SM: send UDP message CartMarker");
						logger.info("--------------------------- Pick Cart --------------------------------------");
						
						PickCart pickCart = createPickCart(newMarkerID); // PickCart
						queryReturn = null;
						eventBus.deliver(pickCart);
						logger.info("-->RB" + robotID + " is sending PickCart with robotID = "+pickCart.robotID);

						if (waitQueryReturn(pickCart.command)) { // always true.
							CurrentState currentState = queryReturn.currentState;

							if (currentState.equals(CurrentState.unknown)) {
								robotReady = false;
								logger.info("-->RB" + robotID + " execute PickCart action failed, Robot Behavior stops !!!!");
								sm.UDPSend("PickCartFailed");
								logger.info("-->SM: send UDP message PickCartFailed");
								break;
							} else { // FINISHED
								
								logger.info("-->RB " + robotID + " Pick Cart successfully");
								sm.UDPSend("PickCartSuccess");
								logger.info("-->SM: send UDP message PickCartSuccess");
							}
						}
						

						// --------------------------- Query Storage point --------------------------------------
						logger.info("--------------------------- Query Storage point --------------------------------------");
						while (query) {

							NewStoragePointRequest storageRequest = new NewStoragePointRequest();
							storageRequest.robotID = this.robotID;
							storageRequest.markerID = markerID;
							RobotBehaviour.storageResponse = null;
							eventBus.deliver(storageRequest);

							waitStorageResponse();

							if (RobotBehaviour.storageResponse.hasNewPoint) {
								logger.info("-----------has new Storage Point-------------");
								sm.UDPSend("NewStoragePointResponse");
								logger.info("-->SM: send UDP message NewStoragePointResponse");
								break;
							} else {
								if (storageCounter > 0) { // just ask for 2 times
									logger.info("-->RB" + robotID + " doesn't get any Storage Point, continue to query after 10s");
									try {
										TimeUnit.SECONDS.sleep(10);
									} catch (InterruptedException e) {
										logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
									}
									storageCounter--;
									continue;
								} else {
									nextIteration = false;
									logger.info("-->RB" + robotID + " doesn't get any Storage Point for this cart after querying for 2 times, exit! ");
									break;
								}
							}
						} // while
						if (nextIteration) {

							// --------------------------- Go to Storage AUX -------------------------------------
							logger.info("--------------------------- Go to Storage AUX --------------------------------------");
							
							if (!executeGoTo(RobotBehaviour.storageResponse.storageAuxliaryPoint, "storage AUX")) {
								sm.UDPSend("GoToStorageFailed");
								logger.info("-->SM: send UDP message GoToStorageFailed");
								break; // execution failed
							}
							sm.UDPSend("ObstacleOnStoragePath");
							logger.info("-->SM: send UDP message ObstacleOnStoragePath");
							try {
								TimeUnit.SECONDS.sleep(3);
							} catch (InterruptedException e) {
								logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
							}
							
							// --------------------------- check Door Marker  --------------------------------------
					/*	*/	logger.info("--------------------------- Check Door Marker --------------------------------------");
							
							CheckMarker checkDoorMarker = createCheckMarker(); // CheckMarker
							eventBus.deliver(checkDoorMarker);
							logger.info("-->RB" + robotID + " is sending cmd to check Door Marker");
							System.out.println("-->RB" + robotID + " is sending cmd to check Door Marker");

							int DoorID = waitMarker();
							logger.info("-->RB" + robotID + " got   Door ID = " + DoorID);
							sm.UDPSend("DoorMarker");
							logger.info("-->SM: send UDP message DoorMarker");
							System.out.println("-->RB" + robotID + " got   Door ID = " + DoorID);
							
							int counter = 2;
							int flag = 0;
							boolean isDoorOpened = false;
							
							while(true) {
								sm.UDPSend("DoorMarker");
								logger.info("--SM: send UDP message DoorMaker");
								counter = 2;
								flag = 0;
							
							while(counter>0) {
								if(checkDoorStatus()) {  // check for max 2 times ==>  6s
									flag = 1;
									break;
								} else {
									counter --;
								}
							}
							if(flag == 0) {
								isDoorOpened = false;
								break;  // no status response received
							} else {
									if(doorStatus == DoorStatus.CLOSED) {
										sm.UDPSend("DoorClosed");
										logger.info("--SM: send UDP message DoorClosed");
										counter = 2;
										flag = 0;
										while(counter>0) {
											if(commandToOpenDoor()) {  // if  OpenDoorResponse is received and the response is SUCCESS
												flag = 1;
												sm.UDPSend("OpenDoorResponseSuccessStorage");
												logger.info("--SM: send UDP message OpenDoorResponseSuccessStorage");
												break;
											}
										}
										if(flag == 0) {
											isDoorOpened = false;
											sm.UDPSend("OpenDoorResponseFailed");
											break;  // failed to send open door request 
										} else {
											continue; // continue to check the door status
										}
										
										
									} else if (doorStatus == DoorStatus.OPENED) {
										logger.info("-->RB" + robotID + " found Door is OPENED, continue moving");
										System.out.println("-->RB" + robotID + " found Door is OPENED, continue moving");
										isDoorOpened = true;
										sm.UDPSend("DoorOpenStorage");
										logger.info("--SM: send UDP message DoorOpenStorage");
										break;
									} else if (doorStatus == DoorStatus.CLOSING || doorStatus == DoorStatus.OPENING) {
										logger.info("-->RB" + robotID + " found Door is" + doorStatus + ", waiting");
										System.out.println("-->RB" + robotID + " found Door is" + doorStatus + ", waiting");
										try {
											TimeUnit.SECONDS.sleep(3);
										} catch (InterruptedException e) {
											logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
										}
										continue;
									} else if (doorStatus == DoorStatus.PREVENTED) {
										logger.info("-->RB" + robotID + " found Door is" + doorStatus + ", waiting");
										System.out.println("-->RB" + robotID + " found Door is" + doorStatus + ", waiting");
										try {
											TimeUnit.SECONDS.sleep(3);
										} catch (InterruptedException e) {
											logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
										}
										continue;
									}
								}
							
							}
							
							if(!isDoorOpened) {
								logger.info("-->RB" + robotID + " found Door can not be OPENED in Storage area, stop moving");
								System.out.println("-->RB" + robotID + " found Door can not be OPENED in Storage area, stop moving");
								break;
							} else {
							
							// --------------------------- Go to Storage Point --------------------------------------
							logger.info("--------------------------- Go to Storage Point --------------------------------------");
							
							if (!executeGoTo(RobotBehaviour.storageResponse.storagePoint, "storage Point")) {
								sm.UDPSend("GoToStorageFailed");
								logger.info("-->SM: send UDP message GoToStorageFailed");
								break; // execution failed
							}
							sm.UDPSend("GoToStorageSuccess");
							logger.info("-->SM: send UDP message GoToStorageSuccess ");

							// --------------------------- Place Cart --------------------------------------
							logger.info("--------------------------- Place Cart --------------------------------------");
							
							PlaceCart placeCart = createPlaceCart(); // PickCart
							queryReturn = null;
							eventBus.deliver(placeCart);
							logger.info("-->RB" + robotID + " sending placeCart");

							if (waitQueryReturn(placeCart.command)) { // always true. otherwise it always query
								CurrentState currentState = queryReturn.currentState;

								if (currentState.equals(CurrentState.unknown)) {
									robotReady = false;
									logger.info("-->RB" + robotID + " execute PickCart action failed, Robot Behavior stops !!!!");
									sm.UDPSend("PlaceCartFailed");
									logger.info("-->SM: send UDP message PlaceCartFailed");
									
									break;
								} else { // FINISHED
									
									logger.info("-->RB " + robotID + " Place Cart successfully");
									sm.UDPSend("PlaceCartSuccess");
									logger.info("-->SM: send UDP message PlaceCartSuccess");
								}
							}

							// --------------------------- Cart Moved Notice --------------------------------------
							logger.info("--------------------------- Cart Moved Notice --------------------------------------");
							
							CartMovedNotice cartMovedNotice = createCartMovedNotice();
							cartNoticeResponse = null;
							eventBus.deliver(cartMovedNotice);
							logger.info("-->RB" + robotID + " is sending CartMovedNotice");

							waitCartNoticeResponse(); // noticeStatus = "OK"
							sm.UDPSend("CartNoticeResponse");
							logger.info("-->SM: send UDP message CartNoticeResponse");

						//	logger.info("-->RB" + robotID + " got CartNoticeResponse");

							// --------------------------- Docking Request--------------------------------------
							logger.info("--------------------------- Docking Request --------------------------------------");
							
							DockingRequest dockingRequest = createDockingRequest();
							dockingResponse = null;
							eventBus.deliver(dockingRequest);
							logger.info("-->RB" + robotID + " is sending DockingRequest with robotIP = "+ robotID); // TODO change to robotIP

							if (waitDockingResponse()) {
								if (dockingResponse.hasNewPoint) {
									sm.UDPSend("DockingResponse");
									logger.info("-->SM: send UDP message DockingResponse");

									// --------------------------- Go to Docking AUX -------------------------------------
									logger.info("--------------------------- Go to Docking AUX --------------------------------------");

									
									if (!executeGoTo(dockingResponse.dockAuxliaryPoint, "docking AUX")) {
										sm.UDPSend("GoToDockFailed");
										logger.info("-->SM: send UDP message GoToDockFailed");
										break; // execution failed
									}
									
									
									 sm.UDPSend("ObstacleOnDockingPath");
									 logger.info("-->SM: send UDP message ObstacleOnDockingPath");
									
									// --------------------------- check Door Marker to Docking Area --------------------------------------
									logger.info("--------------------------- Check Door Marker to Docking Area --------------------------------------");

									isDoorOpened = false;
									
									while(true) {
										sm.UDPSend("DoorMarker");
										logger.info("--SM: send UDP message DoorMarker");
										counter = 2;
										flag = 0;
									
									while(counter>0) {
										if(checkDoorStatus()) {  // check for max 2 times
											flag = 1;
											break;
										} else {
											counter --;
										}
									}
									if(flag == 0) {
										isDoorOpened = false;
										break;  // no status response received
									} else {
											if(doorStatus == DoorStatus.CLOSED) {
												sm.UDPSend("DoorClosed");
												logger.info("--SM: send UDP message DoorClosed");
												counter = 2;
												flag = 0;
												while(counter>0) {
													if(commandToOpenDoor()) {  // if  OpenDoorResponse is received and the response is SUCCESS
														flag = 1;
														sm.UDPSend("DoorOpenResponseSuccessDock");
														logger.info("--SM: send UDP message DoorOpenResponseSuccessDock");
														break;
													}
												}
												if(flag == 0) {
													isDoorOpened = false;
													sm.UDPSend("OpenDoorResponseFailed");
													logger.info("--SM: send UDP message DoorOpenResponseFailed");
													break;  // failed to send open door request 
												} else {
													continue; // continue to check the door status
												}
												
												
											} else if (doorStatus == DoorStatus.OPENED) {
												logger.info("-->RB" + robotID + " found Door is OPENED, continue moving");
												System.out.println("-->RB" + robotID + " found Door is OPENED, continue moving");
												sm.UDPSend("DoorOpenDock");
												logger.info("--SM: send UDP message DoorOpenDock");
												isDoorOpened = true;
												break;
											} else if (doorStatus == DoorStatus.CLOSING || doorStatus == DoorStatus.OPENING) {
												logger.info("-->RB" + robotID + " found Door is" + doorStatus + ", waiting");
												System.out.println("-->RB" + robotID + " found Door is" + doorStatus + ", waiting");
												try {
													TimeUnit.SECONDS.sleep(3);
												} catch (InterruptedException e) {
													logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
												}
												continue;
											} else if (doorStatus == DoorStatus.PREVENTED) {
												logger.info("-->RB" + robotID + " found Door is" + doorStatus + ", waiting");
												System.out.println("-->RB" + robotID + " found Door is" + doorStatus + ", waiting");
												try {
													TimeUnit.SECONDS.sleep(3);
												} catch (InterruptedException e) {
													logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
												}
												continue;
											}
										}
									
									}

									if(!isDoorOpened) {
										logger.info("-->RB" + robotID + " found Door can not be OPENED in Docking area, stop moving");
										break;
									} else {
									// --------------------------- Go to Docking Point --------------------------------------
									logger.info("--------------------------- Go to Docking Point --------------------------------------");

									if (!executeGoTo(dockingResponse.dockingPoint, "dock Point")) {
										sm.UDPSend("GoToDockFailed");
										logger.info("-->SM: send UDP message GoToDockFailed");
										break; // execution failed
									} else {
										sm.UDPSend("GoToDockSuccess");
										logger.info("-->SM: send UDP message GoToDockSuccess");
										logger.info("--------------------------- End of the Iteration --------------------------------------");
									}
									
								  }   // end of if(isDoorOpened)
								} else {
									logger.info("-->RB" + robotID + " exit because NO Docking point found ");
									nextIteration = false;
									break;
								}
							}
						 }  // end of if(isDoorOpened)
						} // end of if(nextIteration), new storage point is found
						else { // nextIteration = false
							break; // no storage are found for this cart
						}

					} // end of if(nextIteration), new pick point is found
					else {
						break; // Tasks are done
					}

				} else { // !broadcastACK && robotReady = false
					try {
						TimeUnit.SECONDS.sleep(1);
					} catch (InterruptedException e) {
						logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
					}
				}

			} // end of ==> while (nextIteration)

			if (nextIteration == false) { // only when normal exit(no pick, no storage after querying for a long time)
				logger.info("-->Tasks are done. Robot Behavior " + robotID + " exit !!!");
			} else { // when the WriteGoTo action fail with the last_event=abort, robot behavior will exit.
				logger.info("-->RB " + robotID + "  exit because of failure in robot!!!");
			}
			stop();
		}

		);
		
	

	}
	
	
	public boolean commandToOpenDoor() {
		RobotBehaviour.openDoorResponse = null;
		OpenDoorRequest open = new OpenDoorRequest();
		open.robotID = robotID;
		eventBus.deliver(open);
		logger.info("-->RB" + robotID + " found Door is CLOSED, is sending OpenDoorRequest");
		System.out.println("-->RB" + robotID + " found Door is CLOSED, is sending OpenDoorRequest");
		
		if(!waitOpenDoorResponse()) {
			logger.info("-->RB" + robotID + " didn't receive the OpenDoorResponse in 3s");
			System.out.println("-->RB" + robotID + " didn't receive the OpenDoorResponse in 3s");
			return false;
		} else {
			
			if(openDoorResponse.response == CommandDoorStatus.SUCCESS) {
				logger.info("-->RB" + robotID + " received the OpenDoorResponse = "+openDoorResponse.response+", continue to query....");
				System.out.println("-->RB" + robotID + " received the OpenDoorResponse = "+openDoorResponse.response+", continue to query....");
				
			} else { // CommandDoorStatus.FAILURE
				logger.info("-->RB" + robotID + " received the OpenDoorResponse = "+openDoorResponse.response+", failed to send open door request....");
				System.out.println("-->RB" + robotID + " received the OpenDoorResponse = "+openDoorResponse.response+", failed to send open door request....");
				return false;
			}
		}
		return true;
	}
	
	public boolean checkDoorStatus() {
			doorStatus = null;
			
			DoorStatusRequest dsr = new DoorStatusRequest();
			dsr.robotID = robotID;
			eventBus.deliver(dsr);
			logger.info("-->RB" + robotID + " is asking for Door Status");
			System.out.println("-->RB" + robotID + " is asking for Door Status");
			
			if(!waitDoorStatusResponse()) {
				logger.info("-->RB" + robotID + " didn't receive the DoorStatusResponse in 5s");
				System.out.println("-->RB" + robotID + " didn't receive the DoorStatusResponse in 5s");
				return false;
			}
			return true;
	}
	
	public boolean waitDoorStatusResponse() {
		logger.info("-->RB" + robotID + " is waiting DoorStatusResponse");
		System.out.println("-->RB" + robotID + " is waiting DoorStatusResponse");
		int counter = 10;
		while (counter>0) {
	//		RobotBehaviour.doorStatus = DoorStatus.CLOSED; // todo1
			
			
			if (RobotBehaviour.doorStatus != null) {
				return true;
			} else {
				try {
					logger.info("-->RB" + robotID + " is waiting for 1s");
					System.out.println("-->RB" + robotID + " is waiting for 1s");
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
				}
				counter --;
			}
		}
		return false;
	}
	
	public boolean waitOpenDoorResponse() {
		logger.info("-->RB" + robotID + " is waiting OpenDoorResponse");
		int counter = 3;
		while (counter>0) {
	//		RobotBehaviour.openDoorResponse.response = CommandDoorStatus.SUCCESS; // todo2
			
			if (RobotBehaviour.openDoorResponse != null) {
				return true;
			} else {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
				}
				counter --;
			}
		}
		return false;
	}
	
	
	@Modified
    void modified(Map<String, Object> properties) {
		logger.info("\n\n --> RB " + robotID + "  has osgi service properties :" + properties+ ", UUID="+UUID+"\n");
		System.out.println("\n\n --> RB " + robotID + "  has osgi service properties :" + properties+ ", UUID="+UUID+"\n");

    }

	@Override
	public void notify(BrainIoTEvent event) {
		if (event instanceof RobotCommand) {
			logger.info("-->RB " + robotID + " received an event: "+event.getClass().getSimpleName()+ ", with robotID="+event);
			System.out.println("-->RB " + robotID + " received an event: "+event.getClass().getSimpleName()+ ", with robotID="+event);
			
		
		}/* else {
			logger.info("-->RB " + robotID + " received an Door event: "+event.getClass().getSimpleName());
			System.out.println("-->RB " + robotID + " received an Door event: "+event.getClass().getSimpleName());
		}*/
		
		if (event instanceof RobotReadyBroadcast) {
			if(!receivedBroadcast) {
			RobotReadyBroadcast rbc = (RobotReadyBroadcast) event;
			logger.info("-->RB " + robotID + " received an RobotReadyBroadcast event with robotID="+rbc.robotID+ " and UUID="+rbc.UUID+ "==>  RB.UUID="+UUID);
			System.out.println("-->RB " + robotID + " received an RobotReadyBroadcast event with robotID="+rbc.robotID+ " and UUID="+rbc.UUID+ "==>  RB.UUID="+UUID);
                       
			if(rbc.UUID.equals(UUID)) {
			worker.execute(() -> {
				
				robotIP = rbc.robotIP;
				robotID = rbc.robotID;

				
				Bundle adminBundle = FrameworkUtil.getBundle(RobotBehaviour.class);
				String location = adminBundle.getLocation();
				
				Configuration config;
				try {
					config = cm.getConfiguration("eu.brain.iot.example.robot.RobotBehavior", location);

					Hashtable<String, Object> props = new Hashtable<>();
					props.put(SmartBehaviourDefinition.PREFIX_ + "filter", // only receive some sepecific events with robotID
							String.format("(|(robotID=%s)(robotID=%s))", robotID, RobotCommand.ALL_ROBOTS));
					config.update(props); // the modified() method will be called. it will receive only the events with the robotID.
					logger.info("-->RB " + robotID + " update properties = "+props+ ", UUID="+UUID);
					System.out.println("-->RB " + robotID + " update properties = "+props+ ", UUID="+UUID);
					
				} catch (IOException e) {
					logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
				}
				
	
				receivedBroadcast = true;
                                sm.UDPSend("RobotReadyBroadcast");
	                       logger.info("-->SM: send UDP message RobotReadyBroadcast");
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
					e.printStackTrace();
				}
				
				BroadcastResponse bcr = new BroadcastResponse();
				bcr.robotID = robotID;
				bcr.UUID = UUID;
				eventBus.deliver(bcr);
		//		receivedBroadcast = true;
				
				robotReady = rbc.isReady;  // then RB start to ask for a pick point
				logger.info("-->RB " + robotID + " got robot "+robotID+" is Ready -- "+robotReady+", sent BroadcastResponse to robot="+robotID+ ", UUID="+UUID);
				System.out.println("-->RB " + robotID + " got robot "+robotID+" is Ready -- "+robotReady+", sent BroadcastResponse to robot="+robotID+ ", UUID="+UUID);
			});
		}
		}

		} else if (event instanceof BroadcastACK) {
			BroadcastACK ack = (BroadcastACK) event;

			if(!receivedBroadcast || ack.robotID!=robotID) {
				logger.info("-->RB " + robotID + " got broadcastACK with robotID="+ack.robotID+", but to be ignored.......... ");
				System.out.println("-->RB " + robotID + " got broadcastACK with robotID="+ack.robotID+", but to be ignored.......... ");
			} else {
				logger.info("-->RB " + robotID + " got broadcastACK with robotID="+ack.robotID+", start moving.......... "+ ", UUID="+UUID);
				System.out.println("-->RB " + robotID + " got broadcastACK with robotID="+ack.robotID+", start moving.......... "+ ", UUID="+UUID);
			
				broadcastACK = true;
				sm.UDPSend("BroadcastACK");
		                logger.info("-->SM: send UDP message BroadcastACK");
			}

		}
		
		else if (event instanceof NewPickPointResponse) {
			NewPickPointResponse resp = (NewPickPointResponse) event;

			if(resp.robotID == robotID) {
				RobotBehaviour.pickResponse = resp;
			
				logger.info("-->RB" + robotID + " get new Pick Point: " + RobotBehaviour.pickResponse.pickPoint +" with robotID="+RobotBehaviour.pickResponse.robotID);
				System.out.println("-->RB" + robotID + " get new Pick Point: " + RobotBehaviour.pickResponse.pickPoint+" with robotID="+RobotBehaviour.pickResponse.robotID);
				sm.UDPSend("NewPickupPointResponse");
		        logger.info("-->SM: send UDP message NewPickupPointResponse");
			} else {
				logger.info("-->RB" + robotID + " get new Pick Point: " + RobotBehaviour.pickResponse.pickPoint +" with robotID="+RobotBehaviour.pickResponse.robotID+", but is ignored....");
				System.out.println("-->RB" + robotID + " get new Pick Point: " + RobotBehaviour.pickResponse.pickPoint+" with robotID="+RobotBehaviour.pickResponse.robotID+", but is ignored....");
			}
		} else if (event instanceof NewStoragePointResponse) {
			
			NewStoragePointResponse resp = (NewStoragePointResponse) event;
			
			if(resp.robotID == robotID) {
				RobotBehaviour.storageResponse = resp;
			
				logger.info("-->RB" + robotID + " get new storageResponse AUX: " + RobotBehaviour.storageResponse.storageAuxliaryPoint +" with robotID="+RobotBehaviour.storageResponse.robotID);
				System.out.println("-->RB" + robotID + " get new storageResponse AUX: " + RobotBehaviour.storageResponse.storageAuxliaryPoint +" with robotID="+RobotBehaviour.storageResponse.robotID);
			} else {
				logger.info("-->RB" + robotID + " get new storageResponse AUX: " + RobotBehaviour.storageResponse.storageAuxliaryPoint +" with robotID="+RobotBehaviour.storageResponse.robotID+", but is ignored....");
				System.out.println("-->RB" + robotID + " get new storageResponse AUX: " + RobotBehaviour.storageResponse.storageAuxliaryPoint +" with robotID="+RobotBehaviour.storageResponse.robotID+", but is ignored....");
			}
		} else if (event instanceof DockingResponse) {
			
			DockingResponse resp = (DockingResponse) event;
			
			if(resp.robotID == robotID) {
				RobotBehaviour.dockingResponse = resp;
				
				logger.info("-->RB" + robotID + " get new dockingResponse AUX: " + RobotBehaviour.dockingResponse.dockAuxliaryPoint+" with robotID="+RobotBehaviour.dockingResponse.robotID);
				System.out.println("-->RB" + robotID + " get new dockingResponse AUX: " + RobotBehaviour.dockingResponse.dockAuxliaryPoint+" with robotID="+RobotBehaviour.dockingResponse.robotID);
			} else {
				logger.info("-->RB" + robotID + " get new dockingResponse AUX: " + RobotBehaviour.dockingResponse.dockAuxliaryPoint+" with robotID="+RobotBehaviour.dockingResponse.robotID+", but is ignored....");
				System.out.println("-->RB" + robotID + " get new dockingResponse AUX: " + RobotBehaviour.dockingResponse.dockAuxliaryPoint+" with robotID="+RobotBehaviour.dockingResponse.robotID+", but is ignored....");
			}

		} else if (event instanceof CartNoticeResponse) {
			RobotBehaviour.cartNoticeResponse = (CartNoticeResponse) event;

		} else if (event instanceof QueryStateValueReturn) {
			QueryStateValueReturn qs = (QueryStateValueReturn) event;
			worker.execute(() -> {
				if(qs.robotID == robotID) {
				
					logger.info("-->RB" + robotID + " receive QueryStateValueReturn = " + qs.currentState +" with robotID="+qs.robotID);
					System.out.println("-->RB" + robotID + " receive QueryStateValueReturn = " + qs.currentState +" with robotID="+qs.robotID);
					RobotBehaviour.queryReturn = qs;
				} else {
					logger.info("-->RB" + robotID + " receive QueryStateValueReturn = " + qs.currentState +" with robotID="+qs.robotID+", but is ignored....");
					System.out.println("-->RB" + robotID + " receive QueryStateValueReturn = " + qs.currentState +" with robotID="+qs.robotID+", but is ignored....");
				}
			});

		} else if (event instanceof MarkerReturn) {
			MarkerReturn cvr = (MarkerReturn) event;
			worker.execute(() -> {
				if(cvr.robotID == robotID) {
					logger.info("-->RB" + robotID + " receive Check Marker return, marker ID = " + cvr.markerID);
					System.out.println("-->RB" + robotID + " receive Check Marker return, marker ID = " + cvr.markerID);
					RobotBehaviour.markerID = cvr.markerID;
					RobotBehaviour.newMarkerCounter += 1;
				} else {
					logger.info("-->RB" + robotID + " receive Check Marker return, marker ID = " + cvr.markerID+" with robotID="+cvr.robotID+", but is ignored....");
					System.out.println("-->RB" + robotID + " receive Check Marker return, marker ID = " + cvr.markerID+" with robotID="+cvr.robotID+", but is ignored....");
				}
			});

		} else if (event instanceof DoorStatusResponse) {
			DoorStatusResponse response = (DoorStatusResponse) event;
			worker.execute(() -> {
				
				System.out.println("-->    RB" + robotID + " receive DoorStatusResponse = " + response.status);
				doorStatus = response.status;
			});
		} else if (event instanceof OpenDoorResponse) {
			RobotBehaviour.openDoorResponse = (OpenDoorResponse) event;

		} else if (event instanceof CloseDoorResponse) {
			RobotBehaviour.closeDoorResponse = (CloseDoorResponse) event;
			worker.execute(() -> {
				System.out.println("-->    RB" + robotID + " receive closeDoorResponse = " + closeDoorResponse.response);
			});
		}

	}
	
	private static synchronized void setPickResponse(NewPickPointResponse event) {
		pickResponse = event;
	}
	private static synchronized NewPickPointResponse getPickResponse() {
		return pickResponse;
	}

	public boolean executeGoTo(String coordinate, String targetPoint) {
		WriteGoTo writeGoTo = createWriteGoTo(coordinate); // writeGOTO
		queryReturn = null;
		eventBus.deliver(writeGoTo);
		logger.info("-->RB" + robotID + " is sending WriteGoTo: "+ coordinate +" with robotID = "+writeGoTo.robotID);
		System.out.println("-->RB" + robotID + " is sending WriteGoTo: "+ coordinate +" with robotID = "+writeGoTo.robotID);

		if (waitQueryReturn(writeGoTo.command)) { // always true.
			CurrentState currentState = queryReturn.currentState;

			if (currentState.equals(CurrentState.unknown)) {
				robotReady = false;
				logger.info("-->RB" + robotID + " execute GoTo " + targetPoint + " action failed, Robot Behavior stops !!!!");
				System.out.println("-->RB" + robotID + " execute GoTo " + targetPoint + " action failed, Robot Behavior stops !!!!");
				// break;
				return false;
			} else { // FINISHED
				logger.info("-->RB " + robotID + " GoTo " + targetPoint + " successfully");
				System.out.println("-->RB " + robotID + " GoTo " + targetPoint + " successfully");
			}
		}
		return true;
	}

	public boolean waitQueryReturn(Command command) {
		logger.info("-->RB" + robotID + " is waiting QueryStateValueReturn");
		while (true) {
			if (queryReturn != null) {
				if (queryReturn.command.equals(command)/* && queryReturn.currentState */) {
					return true;
				} else {
					logger.info(
							"-->RB" + robotID + " get QueryStateValueReturn, but the command is not the same, wait......");
					try {
						TimeUnit.SECONDS.sleep(2);
					} catch (InterruptedException e) {
						logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
					}
				}
			} else {
				try {
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException e) {
					logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
				}
			}
		}
	}

	public boolean waitPickResponse() {
		logger.info("-->RB" + robotID + " is waiting PickResponse");
		while (true) {
			if (RobotBehaviour.pickResponse != null) {
				return true;
			} else {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
				}
			}
		}
	}

	public boolean waitStorageResponse() {
		logger.info("-->RB" + robotID + " is waiting storageResponse");
		while (true) {
			if (RobotBehaviour.storageResponse != null) {
				return true;
			} else {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
				}
			}
		}
	}

	public boolean waitCartNoticeResponse() {
		logger.info("-->RB" + robotID + " is waiting CartNoticeResponse");
		while (true) {
			if (this.cartNoticeResponse != null) {
				return true;
			} else {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
				}
			}
		}
	}

	public boolean waitDockingResponse() {
		logger.info("-->RB" + robotID + " is waiting dockingResponse");
		while (true) {
			if (this.dockingResponse != null) {
				return true;
			} else {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
				}
			}
		}
	}
	
	
	
	public boolean waitCloseDoorResponse() {
		logger.info("-->RB" + robotID + " is waiting CloseDoorResponse");
		int counter = 3;
		while (counter>0) {
			if (RobotBehaviour.closeDoorResponse != null) {
				return true;
			} else {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
				}
				counter --;
			}
		}
		return false;
	}

	public int waitMarker() {
		logger.info("-->RB" + robotID + " is waiting for pose Marker");
		System.out.println("-->RB" + robotID + " is waiting for pose Marker");
		while (true) {
			if (RobotBehaviour.currentMarkerCounter != RobotBehaviour.newMarkerCounter) {
				RobotBehaviour.currentMarkerCounter = RobotBehaviour.newMarkerCounter;
				logger.info("-->RB" + robotID + " got pose Marker = "+RobotBehaviour.markerID);
				System.out.println("-->RB" + robotID + " got pose Marker = "+RobotBehaviour.markerID);
				return RobotBehaviour.markerID;
			} else {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					logger.error("Robot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
				}
			}
		}
	}

	private WriteGoTo createWriteGoTo(String coordinate) {
		WriteGoTo writeGoTo = new WriteGoTo();
		writeGoTo.robotID = robotID;
		writeGoTo.coordinate = coordinate;
		return writeGoTo;
	}

	private PickCart createPickCart(int markerID) {
		PickCart pc = new PickCart();
		pc.robotID = robotID;
		pc.markerID = markerID;
		return pc;
	}

	private PlaceCart createPlaceCart() {
		PlaceCart placeCart = new PlaceCart();
		placeCart.robotID = robotID;
		return placeCart;
	}

	private CheckMarker createCheckMarker() {
		CheckMarker checkMarker = new CheckMarker();
		checkMarker.robotID = robotID;
		return checkMarker;
	}


	private CartMovedNotice createCartMovedNotice() {
		CartMovedNotice cartMovedNotice=new CartMovedNotice();
		cartMovedNotice.robotID=robotID;
		cartMovedNotice.pickPoint = pickResponse.pickPoint;
		return cartMovedNotice;
	}
	
	private NoCartNotice createNoCartNotice() {
		NoCartNotice noCartNotice=new NoCartNotice();
		noCartNotice.robotID=robotID;
		noCartNotice.pickPoint = pickResponse.pickPoint;
		return noCartNotice;
	}
	
	private DockingRequest createDockingRequest() {
		DockingRequest dockingRequest = new DockingRequest();
		dockingRequest.robotID=robotID;
		dockingRequest.robotIP = robotIP;
		return dockingRequest;
	}
	


	@Deactivate
	void stop() {
//		reg.unregister();
		worker.shutdown();
		try {
			worker.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
		}
		logger.info("------------  Robot Behavior "+ robotID+" is deactivated----------------");
		System.out.println("------------  Robot Behavior "+ robotID+" is deactivated----------------");
	}

}
