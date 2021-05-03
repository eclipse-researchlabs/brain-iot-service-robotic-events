package org.eclipse.sensinact.brainiot.door.sensinact.door;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.sensinact.gateway.brainiot.door.api.CloseDoorRequest;
import org.eclipse.sensinact.gateway.brainiot.door.api.CommandDoorStatus;
import org.eclipse.sensinact.gateway.brainiot.door.api.DoorStatus;
import org.eclipse.sensinact.gateway.brainiot.door.api.DoorStatusRequest;
import org.eclipse.sensinact.gateway.brainiot.door.api.DoorStatusResponse;
import org.eclipse.sensinact.gateway.brainiot.door.api.OpenDoorRequest;
import org.eclipse.sensinact.gateway.brainiot.door.api.OpenDoorResponse;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.BrainIoTEvent;
import eu.brain.iot.eventing.api.EventBus;
import eu.brain.iot.eventing.api.SmartBehaviour;
import eu.brain.iot.robot.events.RobotReadyBroadcast;

@Component(immediate = true , service = SmartBehaviour.class)
@SmartBehaviourDefinition(
	filter="(timestamp=*)", author = "Kentyou", 
	name="Door Edge Node",
	description = "Brain-IoT Door Edge Node",
	consumed = {
		DoorStatusRequest.class,
		OpenDoorRequest.class }
)

public class SensiNactAndDoorEventListener implements SmartBehaviour<BrainIoTEvent>  {

    private static final Logger LOG = (Logger) LoggerFactory.getLogger(SensiNactAndDoorEventListener.class);

    private ExecutorService worker;
    
    
    
    
    @Reference
    private EventBus bus;
    
    @Activate
	public void activate(ComponentContext context) {
    	LOG.info("\n --------------------------------------------- \n");
    	LOG.info("\n  SensiNactAndDoorEventListener \n");
		
		System.out.println("\n --------------------------------------------- \n");
		System.out.println("\n  SensiNactAndDoorEventListener \n");

		worker = Executors.newFixedThreadPool(10);
	}
    
    @Override
	public void notify(BrainIoTEvent event) {
		
		if(event == null) {
			LOG.error("Null event");
			System.out.println("\n Null event");
			return;
		}
		LOG.info("--> Door listener  received an event: "+event.getClass().getSimpleName());
		System.out.println("--> Door listener  received an event: "+event.getClass().getSimpleName());

		if (event instanceof DoorStatusRequest) {
			DoorStatusRequest rbc = (DoorStatusRequest) event;
			
			worker.execute(() -> {
				DoorStatusResponse resp1 = new DoorStatusResponse();
				resp1.status = DoorStatus.CLOSED;
				resp1.robotID = -1;
				bus.deliver(resp1);
				LOG.info("--> Door listener  delivered DoorStatusResponse event");
				System.out.println("--> Door listener   delivered DoorStatusResponse event");
				
			});
		}
		
		
		
		switch(event.getClass().getSimpleName()){
		case "DoorStatusRequest": // DoorStatusRequest
			DoorStatusResponse resp1 = new DoorStatusResponse();
			resp1.status = DoorStatus.CLOSED;
			resp1.robotID = -1;
			bus.deliver(resp1);
			LOG.info("--> Door listener  delivered DoorStatusResponse event");
			System.out.println("--> Door listener   delivered DoorStatusResponse event");
			

			break;
		case "OpenDoorRequest":	
			OpenDoorResponse r1 = new OpenDoorResponse();
			r1.response = CommandDoorStatus.SUCCESS;
			r1.robotID = -1;
			bus.deliver(r1);
			LOG.info("--> Door listener  delivered OpenDoorResponse event");
			System.out.println("--> Door listener   delivered OpenDoorResponse event");

			break;

		default:
			LOG.debug("Unhandled event type :"+event.getClass());
			System.out.println("Unhandled event type :"+event.getClass());
			return;
	}
}
}
