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
import eu.brain.iot.service.robotic.door.api.DoorStatusResponse;
import eu.brain.iot.service.robotic.door.api.DoorStatusRequest.State;
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
		configurationPid = "eu.brain.iot.example.robot.RobotBehavior.Ros2", 
		configurationPolicy = ConfigurationPolicy.OPTIONAL,
		scope=ServiceScope.SINGLETON,
		service = {SmartBehaviour.class})

@SmartBehaviourDefinition(consumed = { NewPickPointResponse.class, NewStoragePointResponse.class, DockingResponse.class, 
		CartNoticeResponse.class, MarkerReturn.class, QueryStateValueReturn.class, RobotReadyBroadcast.class,
		DoorStatusResponse.class, AvailabilityReturn.class, BroadcastACK.class},
		author = "LINKS", name = "Robot Behavior", 
		filter = "(|(robotID=2)(robotID=-1))",  // "(|(robotID=1)(robotID=-1))"   "(timestamp=*)"
		description = "Implements a Robot Behavior.")

public class RobotBehaviour2 implements SmartBehaviour<BrainIoTEvent> {

	private static volatile int robotID = 2;
	private static volatile String configName;
	
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
//	private static volatile String robotName;

	@ObjectClassDefinition
	public static @interface Config {
		String logPath() default "/opt/fabric/resources/logback.xml";
	}
	
	private  Logger logger;
	
	private ExecutorService worker;

	@Reference
	private EventBus eventBus;
	
	@Reference
    void setConfigurationAdmin(ConfigurationAdmin cm) {
        this.cm = cm;
    }

	@Activate
	void activate(BundleContext context, Config config, Map<String, Object> properties) {
		
		System.setProperty("logback.configurationFile", config.logPath());
		
		logger = (Logger) LoggerFactory.getLogger(RobotBehaviour2.class.getSimpleName());
		
		UUID = context.getProperty("org.osgi.framework.uuid");

	//	this.robotID = Integer.parseInt(context.getProperty("ID1"));
		
	/*		if (!properties.isEmpty()) {
				for (Entry<String, Object> entry : properties.entrySet()) {
					String key = entry.getKey();
					if (key.equals("robotId")) {
						this.robotID = Integer.parseInt((String) entry.getValue());
					}/* else if (key.equals("robotName")) {
						this.robotName = (String) entry.getValue();
					}* else if (key.equals("configName")) {
						this.configName = (String) entry.getValue();
					}
				}
			}*/
		
		logger.info("\n Hello!  I am Robot Behavior : " + robotID);
		System.out.println("\n Hello!  I am Robot Behavior : " + robotID);

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
					
					logger.info("---------------------------RB-"+robotID+" Start New Iteration --------------------------------------");
					System.out.println("---------------------------RB-"+robotID+" Start New Iteration --------------------------------------");
					
					// --------------------------- Query Pick point --------------------------------------
					logger.info("--------------------------- RB-"+robotID+" Query Pick point --------------------------------------");
					System.out.println("--------------------------- RB-"+robotID+" Query Pick point --------------------------------------");
					
					while (query) {

						NewPickPointRequest pickRequest = new NewPickPointRequest();
						pickRequest.robotID = this.robotID;
						RobotBehaviour2.pickResponse = null;
						eventBus.deliver(pickRequest);

						waitPickResponse();

						if (RobotBehaviour2.pickResponse.hasNewPoint) {	
							logger.info("-----------RB-"+robotID+" has new Pick Point = true-------------");
							System.out.println("-----------RB-"+robotID+" has new Pick Point = true-------------");
							
							pickPoint = getPickResponse().pickPoint;
							if(pickPoint == null) {
								logger.info("-->empty, no pick point, RB exit!");
								System.out.println("-->empty, no pick point, RB exit!");
								stop();
							}
							logger.info("-->RB" + robotID + " get new Pick Point: " + pickPoint);
							System.out.println("-->RB" + robotID + " get new Pick Point: " + pickPoint);
							break;
							
						} else {

							if (pickCounter > 0) { // just ask for 2 times
								logger.info("-->RB" + robotID + " doesn't get any Pick Point, continue to query after 10s");
								System.out.println("-->RB" + robotID + " doesn't get any Pick Point, continue to query after 10s");
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
								System.out.println("-->RB" + robotID + " doesn't get any Pick Point, all carts have been moved, exit! ");
								break;
							}

						}
					} // while

					if (nextIteration) { // it means a new pick point is found, the break is not because no pick point is found
						
						// --------------------------- Go to Picking point --------------------------------------
						logger.info("---------------------------RB-"+robotID+" Go to Picking point --------------------------------------");
						System.out.println("---------------------------RB-"+robotID+" Go to Picking point --------------------------------------");

						if (!executeGoTo(pickPoint, "Picking point")) {
							break; // execution failed
						}
						
						// --------------------------- check Cart Marker --------------------------------------
						logger.info("---------------------------RB-"+robotID+" check Cart Marker --------------------------------------");
						System.out.println("---------------------------RB-"+robotID+" check Cart Marker --------------------------------------");

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
						logger.info("---------------------------RB-"+robotID+" Pick Cart --------------------------------------");
						System.out.println("---------------------------RB-"+robotID+" Pick Cart --------------------------------------");
						
						PickCart pickCart = createPickCart(newMarkerID); // PickCart
						queryReturn = null;
						eventBus.deliver(pickCart);
						logger.info("-->RB" + robotID + " is sending PickCart with robotID = "+pickCart.robotID);

						if (waitQueryReturn(pickCart.command)) { // always true.
							CurrentState currentState = queryReturn.currentState;

							if (currentState.equals(CurrentState.unknown)) {
								robotReady = false;
								logger.info("-->RB" + robotID + " execute PickCart action failed, Robot Behavior stops !!!!");
								break;
							} else { // FINISHED
								logger.info("-->RB " + robotID + " Pick Cart successfully");
							}
						}
						

						// --------------------------- Query Storage point --------------------------------------
						logger.info("---------------------------RB-"+robotID+" Query Storage point --------------------------------------");
						System.out.println("---------------------------RB-"+robotID+" Query Storage point --------------------------------------");
						
						while (query) {

							NewStoragePointRequest storageRequest = new NewStoragePointRequest();
							storageRequest.robotID = this.robotID;
							storageRequest.markerID = markerID;
							RobotBehaviour2.storageResponse = null;
							eventBus.deliver(storageRequest);

							waitStorageResponse();

							if (RobotBehaviour2.storageResponse.hasNewPoint) {
								logger.info("-----------RB-"+robotID+"has new Storage Point-------------");
								System.out.println("-----------RB-"+robotID+"has new Storage Point-------------");
								break;
							} else {
								if (storageCounter > 0) { // just ask for 2 times
									logger.info("-->RB" + robotID + " doesn't get any Storage Point, continue to query after 10s");
									System.out.println("-->RB" + robotID + " doesn't get any Storage Point, continue to query after 10s");
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
							logger.info("---------------------------RB-"+robotID+" Go to Storage AUX --------------------------------------");
							System.out.println("---------------------------RB-"+robotID+" Go to Storage AUX --------------------------------------");
							
							if (!executeGoTo(RobotBehaviour2.storageResponse.storageAuxliaryPoint, "storage AUX")) {
								break; // execution failed
							}
							try {
								TimeUnit.SECONDS.sleep(4);
							} catch (InterruptedException e) {
								logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
							}
							
							// --------------------------- check Door Marker  --------------------------------------
					/*		logger.info("--------------------------- Check Door Marker --------------------------------------");
							
							CheckMarker checkDoorMarker = createCheckMarker(); // CheckMarker
							eventBus.deliver(checkDoorMarker);
							logger.info("-->RB" + robotID + " is sending cmd to check Door Marker");

							int DoorID = waitMarker();
							logger.info("-->RB" + robotID + " got DoorID = " + DoorID);
*/
							
							// --------------------------- Go to Storage Point --------------------------------------
							logger.info("---------------------------RB-"+robotID+" Go to Storage Point --------------------------------------");
							System.out.println("---------------------------RB-"+robotID+" Go to Storage Point --------------------------------------");
							
							if (!executeGoTo(RobotBehaviour2.storageResponse.storagePoint, "storage Point")) {
								break; // execution failed
							}

							// --------------------------- Place Cart --------------------------------------
							logger.info("--------------------------- Place Cart --------------------------------------");
							System.out.println("--------------------------- Place Cart --------------------------------------");
							
							PlaceCart placeCart = createPlaceCart(); // PickCart
							queryReturn = null;
							eventBus.deliver(placeCart);
							logger.info("-->RB" + robotID + " sending placeCart");

							if (waitQueryReturn(placeCart.command)) { // always true. otherwise it always query
								CurrentState currentState = queryReturn.currentState;

								if (currentState.equals(CurrentState.unknown)) {
									robotReady = false;
									logger.info("-->RB" + robotID + " execute PickCart action failed, Robot Behavior stops !!!!");
									System.out.println("-->RB" + robotID + " execute PickCart action failed, Robot Behavior stops !!!!");
									break;
								} else { // FINISHED
									logger.info("-->RB " + robotID + " Place Cart successfully");
								}
							}

							// --------------------------- Cart Moved Notice --------------------------------------
							logger.info("---------------------------RB-"+robotID+" Cart Moved Notice --------------------------------------");
							System.out.println("---------------------------RB-"+robotID+" Cart Moved Notice --------------------------------------");
							
							CartMovedNotice cartMovedNotice = createCartMovedNotice();
							cartNoticeResponse = null;
							eventBus.deliver(cartMovedNotice);
							logger.info("-->RB" + robotID + " is sending CartMovedNotice");

							waitCartNoticeResponse(); // noticeStatus = "OK"

						//	logger.info("-->RB" + robotID + " got CartNoticeResponse");

							// --------------------------- Docking Request--------------------------------------
							logger.info("---------------------------RB-"+robotID+" Docking Request --------------------------------------");
							System.out.println("---------------------------RB-"+robotID+" Docking Request --------------------------------------");
							
							DockingRequest dockingRequest = createDockingRequest();
							dockingResponse = null;
							eventBus.deliver(dockingRequest);
							logger.info("-->RB" + robotID + " is sending DockingRequest with robotIP = "+ robotID); // TODO change to robotIP

							if (waitDockingResponse()) {
								if (dockingResponse.hasNewPoint) {

									// --------------------------- Go to Docking AUX -------------------------------------
									logger.info("---------------------------RB-"+robotID+" Go to Docking AUX --------------------------------------");
									System.out.println("---------------------------RB-"+robotID+" Go to Docking AUX --------------------------------------");
									
									if (!executeGoTo(dockingResponse.dockAuxliaryPoint, "docking AUX")) {
										break; // execution failed
									}
									// --------------------------- check Door Marker --------------------------------------
					/*				logger.info("--------------------------- Check Door Marker --------------------------------------");

									
									CheckMarker checkDoorMarker2 = createCheckMarker(); // CheckMarker
									eventBus.deliver(checkDoorMarker2);
									logger.info("-->RB" + robotID + " sending check Door Marker on the way to Docking area");

									int DoorID2 = waitMarker();
									logger.info("-->RB" + robotID + " got DoorID = " + DoorID2);
*/
									
									// --------------------------- Go to Docking Point --------------------------------------
									logger.info("---------------------------RB-"+robotID+" Go to Docking Point --------------------------------------");
									System.out.println("---------------------------RB-"+robotID+" Go to Docking Point --------------------------------------");

									if (!executeGoTo(dockingResponse.dockingPoint, "dock Point")) {
										break; // execution failed
									} else {
										logger.info("---------------------------RB-"+robotID+" End of the Iteration --------------------------------------");
										System.out.println("---------------------------RB-"+robotID+" End of the Iteration --------------------------------------");
									}
								} else {
									logger.info("-->RB" + robotID + " exit because NO Docking point found ");
									nextIteration = false;
									break;
								}
							}
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
				System.out.println("-->Tasks are done. Robot Behavior " + robotID + " exit !!!");
			} else { // when the WriteGoTo action fail with the last_event=abort, robot behavior will exit.
				logger.info("-->RB " + robotID + "  exit because of failure in robot!!!");
				System.out.println("-->RB " + robotID + "  exit because of failure in robot!!!");
			}
			stop();
		}

		);
		
	

	}
	
	@Modified
    void modified(Map<String, Object> properties) {
		logger.info("\n\n --> RB " + robotID + "  has osgi service properties :" + properties);
		System.out.println("\n\n --> RB " + robotID + "  has osgi service properties :" + properties);

    }

	@Override
	public void notify(BrainIoTEvent event) {
		logger.info("\n-->RB " + robotID + " received an event: "+event.getClass().getSimpleName()+ ", with robotID="+((RobotCommand)event).robotID);
		System.out.println("\n-->RB " + robotID + " received an event: "+event.getClass().getSimpleName()+ ", with robotID="+((RobotCommand)event).robotID);

	/*	if(event instanceof StartDTO) {

			logger.info("-->RB "+ robotID+" received StartDTO event................");
			System.out.println("-->RB "+ robotID+" received StartDTO event................");
			
			worker.execute(() -> {
				Bundle adminBundle = FrameworkUtil.getBundle(RobotBehaviour1.class);
				String location = adminBundle.getLocation();
			
			Configuration config;
			try {
				config = cm.getConfiguration("eu.brain.iot.example.robot.RobotBehavior.Ros1", location);

				Hashtable<String, Object> props = new Hashtable<>();
				props.put(SmartBehaviourDefinition.PREFIX_ + "filter", // only receive some sepecific events with robotID
						String.format("(|(robotID=%s)(robotID=%s))", robotID, RobotCommand.ALL_ROBOTS));
				config.update(props); // the modified() method will be called. it will receive only the events with the robotID.
				logger.info("-->RB " + robotID + " update properties = "+props+"\n");
				System.out.println("-->RB " + robotID + " update properties = "+props+"\n");
				
				TimeUnit.SECONDS.sleep(1);
				
			} catch (Exception e) {
				logger.error("RobotBehavior-1 OSGI Service Exception: {}", ExceptionUtils.getStackTrace(e));
			}
			});	
			
		}
		
		else */ if (event instanceof RobotReadyBroadcast) {
			if(!receivedBroadcast) {
			RobotReadyBroadcast rbc = (RobotReadyBroadcast) event;
		
			if(rbc.robotID==robotID) {
				
			worker.execute(() -> {
				
		/*		Bundle adminBundle = FrameworkUtil.getBundle(RobotBehaviour1.class);
				String location = adminBundle.getLocation();
				
				Configuration config;
				try {
					config = cm.getConfiguration("eu.brain.iot.example.robot.RobotBehavior.Ros1", location);

					Hashtable<String, Object> props = new Hashtable<>();
					props.put(SmartBehaviourDefinition.PREFIX_ + "filter", // only receive some sepecific events with robotID
							String.format("(|(robotID=%s)(robotID=%s))", robotID, RobotCommand.ALL_ROBOTS));
					config.update(props); // the modified() method will be called. it will receive only the events with the robotID.
					logger.info("-->RB " + robotID + " update properties = "+props+ ", UUID="+UUID);
					System.out.println("-->RB " + robotID + " update properties = "+props+ ", UUID="+UUID);
					
				} catch (IOException e) {
					logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
				}*/
				
		//		robotReady = rbc.isReady;
				receivedBroadcast = true;
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
				
				robotReady = rbc.isReady;  // then RB start to ask for a pick point
				logger.info("\n-->RB " + robotID + " got robot "+robotID+" is Ready -- "+robotReady+", sent BroadcastResponse to robot="+robotID);
				System.out.println("\n-->RB " + robotID + " got robot "+robotID+" is Ready -- "+robotReady+", sent BroadcastResponse to robot="+robotID);
			});
		}
		}

		} else if (event instanceof BroadcastACK) {
			BroadcastACK ack = (BroadcastACK) event;

			if(!receivedBroadcast || ack.robotID!=robotID) {
				logger.info("-->RB " + robotID + " got broadcastACK with robotID="+ack.robotID+", but to be ignored.......... ");
				System.out.println("-->RB " + robotID + " got broadcastACK with robotID="+ack.robotID+", but to be ignored.......... ");
			} else {
				logger.info("\n-->RB " + robotID + " got broadcastACK with robotID="+ack.robotID+", start moving.......... ");
				System.out.println("\n-->RB " + robotID + " got broadcastACK with robotID="+ack.robotID+", start moving.......... ");
			
				broadcastACK = true;
			}

		}
		
		else if (event instanceof NewPickPointResponse) {
			NewPickPointResponse resp = (NewPickPointResponse) event;

			if(resp.robotID == robotID) {
				RobotBehaviour2.pickResponse = resp;
			
				logger.info("-->RB" + robotID + " get new Pick Point: " + RobotBehaviour2.pickResponse.pickPoint +" with robotID="+RobotBehaviour2.pickResponse.robotID);
				System.out.println("-->RB" + robotID + " get new Pick Point: " + RobotBehaviour2.pickResponse.pickPoint+" with robotID="+RobotBehaviour2.pickResponse.robotID);
			} else {
				logger.info("-->RB" + robotID + " get new Pick Point: " + RobotBehaviour2.pickResponse.pickPoint +" with robotID="+RobotBehaviour2.pickResponse.robotID+", but is ignored....");
				System.out.println("-->RB" + robotID + " get new Pick Point: " + RobotBehaviour2.pickResponse.pickPoint+" with robotID="+RobotBehaviour2.pickResponse.robotID+", but is ignored....");
			}
		} else if (event instanceof NewStoragePointResponse) {
			
			NewStoragePointResponse resp = (NewStoragePointResponse) event;
			
			if(resp.robotID == robotID) {
				RobotBehaviour2.storageResponse = resp;
			
				logger.info("-->RB" + robotID + " get new storageResponse AUX: " + RobotBehaviour2.storageResponse.storageAuxliaryPoint +" with robotID="+RobotBehaviour2.storageResponse.robotID);
				System.out.println("-->RB" + robotID + " get new storageResponse AUX: " + RobotBehaviour2.storageResponse.storageAuxliaryPoint +" with robotID="+RobotBehaviour2.storageResponse.robotID);
			} else {
				logger.info("-->RB" + robotID + " get new storageResponse AUX: " + RobotBehaviour2.storageResponse.storageAuxliaryPoint +" with robotID="+RobotBehaviour2.storageResponse.robotID+", but is ignored....");
				System.out.println("-->RB" + robotID + " get new storageResponse AUX: " + RobotBehaviour2.storageResponse.storageAuxliaryPoint +" with robotID="+RobotBehaviour2.storageResponse.robotID+", but is ignored....");
			}
		} else if (event instanceof DockingResponse) {
			
			DockingResponse resp = (DockingResponse) event;
			
			if(resp.robotID == robotID) {
				RobotBehaviour2.dockingResponse = resp;
				
				logger.info("-->RB" + robotID + " get new dockingResponse AUX: " + RobotBehaviour2.dockingResponse.dockAuxliaryPoint+" with robotID="+RobotBehaviour2.dockingResponse.robotID);
				System.out.println("-->RB" + robotID + " get new dockingResponse AUX: " + RobotBehaviour2.dockingResponse.dockAuxliaryPoint+" with robotID="+RobotBehaviour2.dockingResponse.robotID);
			} else {
				logger.info("-->RB" + robotID + " get new dockingResponse AUX: " + RobotBehaviour2.dockingResponse.dockAuxliaryPoint+" with robotID="+RobotBehaviour2.dockingResponse.robotID+", but is ignored....");
				System.out.println("-->RB" + robotID + " get new dockingResponse AUX: " + RobotBehaviour2.dockingResponse.dockAuxliaryPoint+" with robotID="+RobotBehaviour2.dockingResponse.robotID+", but is ignored....");
			}

		} else if (event instanceof CartNoticeResponse) {
			RobotBehaviour2.cartNoticeResponse = (CartNoticeResponse) event;

		} else if (event instanceof QueryStateValueReturn) {
			QueryStateValueReturn qs = (QueryStateValueReturn) event;
			worker.execute(() -> {
				if(qs.robotID == robotID) {
				
					logger.info("-->RB" + robotID + " receive QueryStateValueReturn = " + qs.currentState +" with robotID="+qs.robotID);
					System.out.println("-->RB" + robotID + " receive QueryStateValueReturn = " + qs.currentState +" with robotID="+qs.robotID);
					RobotBehaviour2.queryReturn = qs;
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
					RobotBehaviour2.markerID = cvr.markerID;
					RobotBehaviour2.newMarkerCounter += 1;
				} else {
					logger.info("-->RB" + robotID + " receive Check Marker return, marker ID = " + cvr.markerID+" with robotID="+cvr.robotID+", but is ignored....");
					System.out.println("-->RB" + robotID + " receive Check Marker return, marker ID = " + cvr.markerID+" with robotID="+cvr.robotID+", but is ignored....");
				}
			});

		} else if (event instanceof DoorStatusResponse) {
			worker.execute(() -> {
				DoorStatusResponse response = (DoorStatusResponse) event;
				if (response.currentState == State.OPEN) {
					isDoorOpen = true;
					logger.info("-->RB" + robotID + " door is opened successfully!!!!");
				}
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
			if (RobotBehaviour2.pickResponse != null) {
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
			if (RobotBehaviour2.storageResponse != null) {
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

	public int waitMarker() {
		logger.info("-->RB" + robotID + " is waiting for pose Marker");
		System.out.println("-->RB" + robotID + " is waiting for pose Marker");
		while (true) {
			if (RobotBehaviour2.currentMarkerCounter != RobotBehaviour2.newMarkerCounter) {
				RobotBehaviour2.currentMarkerCounter = RobotBehaviour2.newMarkerCounter;
				logger.info("-->RB" + robotID + " got pose Marker = "+RobotBehaviour2.markerID);
				System.out.println("-->RB" + robotID + " got pose Marker = "+RobotBehaviour2.markerID);
				return RobotBehaviour2.markerID;
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
		return dockingRequest;
	}

	@Deactivate
	void stop() {
		worker.shutdown();
		try {
			worker.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("\nRobot Behavior Exception: {}", ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		}
		logger.info("\n------------  Robot Behavior "+ robotID+" is deactivated----------------\n");
		System.out.println("\n------------  Robot Behavior "+ robotID+" is deactivated----------------\n");
	}

}
