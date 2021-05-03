package eu.brain.iot.ros.edge.node;

import java.util.concurrent.ExecutorService;

import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;

import eu.brain.iot.robot.events.BroadcastResponse;
import eu.brain.iot.robot.events.RobotReadyBroadcast;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.sensinact.gateway.brainiot.door.api.CloseDoorResponse;
import org.eclipse.sensinact.gateway.brainiot.door.api.DoorStatusRequest;
import org.eclipse.sensinact.gateway.brainiot.door.api.DoorStatusResponse;
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
import org.osgi.service.component.annotations.Reference;

import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.BrainIoTEvent;
import eu.brain.iot.eventing.api.EventBus;
import eu.brain.iot.eventing.api.SmartBehaviour;
import eu.brain.iot.robot.events.*;
import eu.brain.iot.robot.events.QueryStateValueReturn.CurrentState;
import eu.brain.iot.service.robotic.startButton.api.StartDTO;
import eu.brain.iot.robot.api.Command;
import eu.brain.iot.robot.api.Coordinate;
import eu.brain.iot.robot.api.RobotCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.service.component.annotations.ServiceScope;

@Component(service = {SmartBehaviour.class},
immediate=true,

		
property = {"osgi.command.scope=ros", 
	"osgi.command.function=env",	
	"osgi.command.function=nodes",
	"osgi.command.function=topics",
	"osgi.command.function=publishers",
	"osgi.command.function=subscribers",
	"osgi.command.function=services",
	"osgi.command.function=providers"})
@SmartBehaviourDefinition(
	consumed = {WriteGoTo.class, Cancel.class, PickCart.class, PlaceCart.class, QueryState.class, CheckMarker.class, StartDTO.class, BroadcastResponse.class},    
	author = "LINKS", name = "ROS Edge Node",
	filter = "(timestamp=*)",
	description = "An Adaptor for ROS-based Cyber Physical System")

public class RosEdgeNode implements SmartBehaviour<BrainIoTEvent>{
    
	@Reference
	private EventBus eventBus;
	
	private static volatile int robotID=1;
    private static volatile String robotIP="192.168.2.202";
    private String robotName="rb1_base_a";
    private static volatile String UUID;
	private ExecutorService worker;
	private static volatile boolean receivedBroadcastResponse = false;
	private ConfigurationAdmin cm;
	
	@Reference
    void setConfigurationAdmin(ConfigurationAdmin cm) {
        this.cm = cm;
    }
	
	private static final Logger logger = (Logger) LoggerFactory.getLogger(RosEdgeNode.class.getSimpleName());
	
	@Activate
	void activate(BundleContext context, Map<String,Object> props){
    	
		
		UUID = context.getProperty("org.osgi.framework.uuid");
    	logger.info("\nHello!  I am ROS Edge Node : "+robotID+ "  name = "+robotName);
    	
    	System.out.println("\nHello!  I am ROS Edge Node : "+robotID+ "  name = "+robotName);
    	
	    worker = Executors.newFixedThreadPool(10);

	}
	
	@Override
	public void notify(BrainIoTEvent event) {
		
		if(event instanceof StartDTO) {

			logger.info("ROS Edge Node "+ robotID+" received StartDTO event................");
			System.out.println("ROS Edge Node "+ robotID+" received StartDTO event................");
			
			worker.execute(() -> {
			Bundle adminBundle = FrameworkUtil.getBundle(RosEdgeNode.class);
			String location = adminBundle.getLocation();
			
			Configuration config;
			try {
				config = cm.getConfiguration("eu.brain.iot.ros.edge.node.RosEdgeNode", location);

				Hashtable<String, Object> props = new Hashtable<>();
				props.put(SmartBehaviourDefinition.PREFIX_ + "filter", // only receive some sepecific events with robotID
						String.format("(|(robotID=%s)(robotID=%s))", robotID, RobotCommand.ALL_ROBOTS));
				config.update(props); // the modified() method will be called. it will receive only the events with the robotID.
				logger.info("-->RosEdgeNode " + robotID + " update properties = "+props+"\n");
				System.out.println("-->RosEdgeNode " + robotID + " update properties = "+props+"\n");
				
		//		RosEdgeNode.isStarted = true;
				
				TimeUnit.SECONDS.sleep(1);
				
				while(!receivedBroadcastResponse) {
					broadCastReady();
					logger.info("ROS Edge Node "+robotID +"  is sending RobotReadyBroadcast event................ ");
					System.out.println("ROS Edge Node "+robotID +"  is sending RobotReadyBroadcast event...............");
					TimeUnit.SECONDS.sleep(1);
				}
				
			} catch (Exception e) {
				logger.error("RosEdgeNode OSGI Service Exception: {}", ExceptionUtils.getStackTrace(e));
			}
			});	
			
		} else if(event instanceof BroadcastResponse) {
			BroadcastResponse bcr = (BroadcastResponse) event;
			worker.execute(() -> {
			if(!receivedBroadcastResponse) {
		//	BroadcastResponse bcr = (BroadcastResponse) event;
			logger.info("-->RosEdgeNode " + robotID + " received an BroadcastResponse event with robotID="+bcr.robotID+ " and UUID="+bcr.UUID+ "==>  RosNode.UUID="+UUID+"\n");
			System.out.println("-->RosEdgeNode " + robotID + " received an BroadcastResponse event with robotID="+bcr.robotID+ " and UUID="+bcr.UUID+ "==>  RosNode.UUID="+UUID+"\n");
			
			if(bcr.robotID == robotID && bcr.UUID.equals(UUID)) {
				receivedBroadcastResponse = true;
				
				BroadcastACK ack = new BroadcastACK();
				ack.robotID = robotID;
				eventBus.deliver(ack);
				
				logger.info("-->RosEdgeNode " + robotID + " connects to RB "+bcr.robotID+", send BroadcastACK");
				System.out.println("-->RosEdgeNode " + robotID + " connects to RB "+bcr.robotID+", send BroadcastACK");
			} else {
				logger.info("-->Failed!! RosEdgeNode " + robotID + " rejected to connect to RB "+bcr.robotID);
				System.out.println("-->Failed!! RosEdgeNode " + robotID + " rejected to connect to RB "+bcr.robotID);
			}
			
			DoorStatusRequest dsr = new DoorStatusRequest();
			dsr.robotID = robotID;
			eventBus.deliver(dsr);
			logger.info("-->RB" + robotID + " is asking for Door Status");
			System.out.println("-->RB" + robotID + " is asking for Door Status");
			
		   }
		});
		}
		
	}
	
	public void broadCastReady(){
		RobotReadyBroadcast rbc=new RobotReadyBroadcast();  // TODO  RobotReady not exist in real robot, now only for local test
		rbc.robotID = robotID;
		rbc.robotIP = robotIP;
		rbc.UUID = UUID;
		rbc.isReady = true;
		eventBus.deliver(rbc);
	}
	
	@Modified
    void modified(Map<String, Object> properties) {
		
		logger.info("-->Hi, RosEdgeNode " + robotID + "  has osgi service properties :" + properties);
		System.out.println("-->Hi, RosEdgeNode " + robotID + "  has osgi service properties :" + properties);

    }
    
}
