package com.paremus.brain.iot.example.orch.impl;

import static com.paremus.brain.iot.example.door.api.DoorStatus.State.CLOSED;
import static com.paremus.brain.iot.example.door.api.DoorStatus.State.OPEN;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.apache.felix.service.command.CommandProcessor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardResource;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.Promises;

import com.paremus.brain.iot.example.door.api.DoorStatus;
import com.paremus.brain.iot.example.door.api.DoorStatus.State;
import com.paremus.brain.iot.example.door.api.DoorStatusResponse;
import com.paremus.brain.iot.example.robot.api.Cancel;
import com.paremus.brain.iot.example.robot.api.CheckMarker;
import com.paremus.brain.iot.example.robot.api.CheckValueReturn;
import com.paremus.brain.iot.example.robot.api.PickCart;
import com.paremus.brain.iot.example.robot.api.PlaceCART;
import com.paremus.brain.iot.example.robot.api.QueryState;
import com.paremus.brain.iot.example.robot.api.QueryStateValueReturn;
import com.paremus.brain.iot.example.robot.api.writeGOTO;

import compound.sim07;
import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.BrainIoTEvent;
import eu.brain.iot.eventing.api.EventBus;
import eu.brain.iot.eventing.api.SmartBehaviour;

@Component(
		property = {
				CommandProcessor.COMMAND_SCOPE + "=Orchestrator", //
				CommandProcessor.COMMAND_FUNCTION + "=orch" //
		}
	)
@JaxrsResource
@HttpWhiteboardResource(pattern="/quickstart/*", prefix="static")


@SmartBehaviourDefinition(consumed = {CheckValueReturn.class, QueryStateValueReturn.class, DoorStatusResponse.class},
filter = "(timestamp=*)",    
author = "UGA", name = "Smart Orchestrator",
description = "Implements a remote Smart Orchestrator.")
public class ComponentImpl  implements SmartBehaviour<BrainIoTEvent>{


	@Path("rest/robot")
	@GET  
    public String begin() {
		try {
			DoorStatus status = new DoorStatus();
			status.doorId = DoorStatus.ALL_DOORS;
			status.targetState = State.QUERY;
			
			Promise<String> door = awaitResponse(status, e -> e instanceof DoorStatusResponse)
					.map(e -> ((DoorStatusResponse)e).doorId);
			
			Promise<?> robotA = queryStateAsync(1, 0);
			Promise<?> robotB = queryStateAsync(2, 0);
			Promise<?> robotC = queryStateAsync(3, 0);
			
		    if (Promises.all(door, robotA, robotB, robotC).getFailure() == null) 
		    { 
		    	
		    	orch(door.getValue());
		    	return "<style>\n" + 
		    			".content {\n" + 
		    			"  max-width: 500px;\n" + 
		    			"  margin: auto;\n" + 
		    			"}\n" + 
		    			"</style>\n" + 
		    			"\n" + 
		    			"<div class=\"content\">\n" + 
		    			"\n" + 
		    			"\n" + 
		    			"<pre style=\"color:DodgerBlue;\">\n" + 
		    			"     <b>Activating Robots  ...</b>\n" + 
		    			"</pre>\n" + 		    			
		    			"\n" + 
		    			"\n" + 
		    			"</div>";
		    }
			else
			{ 
				
				StringBuilder sb = new StringBuilder("<ul>\n");
				
				if(door.getFailure() != null) {
					sb.append("<li>The Door</li>\n");	
				}
				if(robotA.getFailure() != null) {
				    sb.append("<li> Robot A</li>\n");	
				}
				if(robotB.getFailure() != null) {
					sb.append("<li> Robot B</li>\n");	
				}
				if(robotC.getFailure() != null) {
					sb.append("<li> Robot C</li>\n");	
				}
				
				sb.append("</ul>\n");
				
				System.out.println("Sorry ! We can't reach the necessary devices");
		    	return "<style>\n" + 
    			".content {\n" + 
    			"  max-width: 500px;\n" + 
    			"  margin: auto;\n" + 
    			"}\n" + 
    			"</style>\n" + 
    			"\n" + 
    			"<div class=\"content\">\n" + 
    			"\n" + 
    			"\n" + 
    			"<div style=\"color:Tomato;\">\n" +
    			"<b>Sorry ! We can't reach the following devices:</b>\n" + 
    			sb.toString() +
    			"</div>\n" + 
    			"\n" + 
    			"\n" + 
    			"</div>";
				
				}
		    
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
    	return "<style>\n" + 
		".content {\n" + 
		"  max-width: 500px;\n" + 
		"  margin: auto;\n" + 
		"}\n" + 
		"</style>\n" + 
		"\n" + 
		"<div class=\"content\">\n" + 
		"\n" + 
		"\n" + 
		"<pre style=\"color:Tomato;\">\n" + 
		"     <b>Sorry ! The connection can not be established</b>\n" + 
		"</pre>\n" + 
		"\n" + 
		"\n" + 
		"</div>";

		    
    }
    @Path("/main")
    @GET
    public String load( ) {
        return "\n" + 
        		"<style>\n" + 
        		".content {\n" + 
        		"  max-width: 500px;\n" + 
        		"  margin: auto;\n" + 
        		"}\n" + 
        		"</style>\n" + 
        		"\n" + 
        		"<div class=\"content\">\n" + 
        		"<form name=\"mon-formulaire1\" action=\"rest/robot/\" method=\"get\" align=\"center\">\n" + 
        		"\n" + 
        		"<p>\n" + 
        		"   <input type=\"submit\" value=\"Start Robots\" />\n" + 
        		"   <input type=\"reset\" value=\"Cancel\" />\n" + 
        		"</p>\n" + 
        		"</form>\n" + 
        		"\n" + 
        		"</div>";
    }
	
	
	
	private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
	
	
	
	
	
    public void  orch(String doorId) {
    	new sim07(doorId, this);
    	worker.execute(this::update);

    }
    
    @Reference
    private  EventBus eventBus;
 
	
	private void update() {

		for(PendingRequest pr : pendingRequests) {
			pr.processed++;
			if(pr.processed > 0) {
				eventBus.deliver(pr.toResend);
			}
		}
		
    	worker.schedule(this::update, 2, TimeUnit.SECONDS);
	}
    
	private List<PendingRequest> pendingRequests = new CopyOnWriteArrayList<>();
	
	private static class PendingRequest {
		public Predicate<? super BrainIoTEvent> consumer;
		public BrainIoTEvent toResend;
		public Deferred<? super BrainIoTEvent> latch;
		
		public int processed = -1;
	}
	
	private Promise<BrainIoTEvent> awaitResponse(BrainIoTEvent event, 
			Predicate<BrainIoTEvent> acceptableResponse) {
		PendingRequest pr = new PendingRequest();
		pr.toResend = event;
		pr.consumer = acceptableResponse;
		Deferred<BrainIoTEvent> deferred = new Deferred<>();
		pr.latch = deferred;
		
		pendingRequests.add(pr);
		
		eventBus.deliver(event);
		
		return deferred.getPromise().timeout(10000).onResolve(() -> pendingRequests.remove(pr));
	}
	
	
    public void  writeOpenDoor(String doorId){
    	DoorStatus status = new DoorStatus();
    	status.targetState = OPEN;
    	status.doorId = doorId;
    	
    	try {
			awaitResponse(status, e -> {
				if(e instanceof DoorStatusResponse) {
					DoorStatusResponse dsr = (DoorStatusResponse) e;
					return doorId.equals(dsr.doorId) && dsr.state == OPEN;
				}
				return false;
			})
			.map(e -> (DoorStatusResponse)e)
			.thenAccept(e -> System.out.println("Recieved a Door OPEN response from door " + e.doorId))
			.getValue();
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Failed to open the door", e.getTargetException());
		} catch (InterruptedException e) {
			throw new RuntimeException("Failed to open the door", e);
		}
    	
		System.out.println("ORCHESTRATOR: OPEN DOOR");
	}
	public void  writeCloseDoor(String doorId){
		DoorStatus status = new DoorStatus();
    	status.targetState = CLOSED;
    	status.doorId = doorId;
    	
    	try {
			awaitResponse(status, e -> {
				if(e instanceof DoorStatusResponse) {
					DoorStatusResponse dsr = (DoorStatusResponse) e;
					return doorId.equals(dsr.doorId) && dsr.state == CLOSED;
				}
				return false;
			})
			.map(e -> (DoorStatusResponse)e)
			.thenAccept(e -> System.out.println("Recieved a Door CLOSED response from door " + e.doorId))
			.getValue();
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Failed to close the door", e.getTargetException());
		} catch (InterruptedException e) {
			throw new RuntimeException("Failed to close the door", e);
		}
		System.out.println("ORCHESTRATOR: CLOSE DOOR");
	}
	public void  writegoto(int RobotId, int mission){
		writeGOTO wgoto =new writeGOTO();
		wgoto.mission=mission;	
		wgoto.robotId = RobotId;
		
		eventBus.deliver(wgoto);
		System.out.println("ORCHESTRATOR: SEND GOTO");
	}
    
	public void placeCART(int RobotId, int cart) {
		PlaceCART pc =new PlaceCART();
		pc.cart=cart;
		pc.robotId = RobotId;
		
		eventBus.deliver(pc);
		System.out.println("ORCHESTRATOR: SEND PLACE CART");
	}
	
	public int cancel(int RobotId, int mission){
		Cancel cl =new Cancel();
		cl.mission =mission;
		cl.robotId = RobotId;
		
		eventBus.deliver(cl);
		
		return mission;		
	}
	public int queryState(int RobotId, int mission){
		try {
			return queryStateAsync(RobotId, mission)
					.getValue();
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Failed to query the robot " + RobotId, e.getTargetException());
		} catch (InterruptedException e) {
			throw new RuntimeException("Failed to query the robot " + RobotId, e);
		}
	}


	private Promise<Integer> queryStateAsync(int RobotId, int mission) {
		QueryState qs =new QueryState();
		qs.robotId=RobotId;
		qs.mission =mission;
    	
		System.out.println("ORCHESTRATOR: SEND QUERY STATE");
			return awaitResponse(qs, e -> {
				if(e instanceof QueryStateValueReturn) {
					QueryStateValueReturn qsvr = (QueryStateValueReturn) e;
					return RobotId == qsvr.robotId && qsvr.mission == mission;
				}
				return false;
			})
			.map(e -> (QueryStateValueReturn)e)
			.thenAccept(e -> System.out.println("Recieved a Query response from Robot " + e.robotId))
			.map(e -> e.value);
	}
	
	public void pickCart(int RobotId, int cart){
		PickCart pk =new PickCart();
		pk.cart=cart;
		pk.robotId = RobotId;
		
		eventBus.deliver(pk);
		
		System.out.println("ORCHESTRATOR: SEND PICK CART STATE");
		
	}
	public int checkMarkers(int RobotId, int obj) {
		CheckMarker cm =new CheckMarker();
		cm.robotId=RobotId;
    	
		System.out.println("ORCHESTRATOR: SEND CHECK QRT STATE");
    	try {
			return awaitResponse(cm, e -> {
				if(e instanceof CheckValueReturn) {
					CheckValueReturn cvr = (CheckValueReturn) e;
					return RobotId == cvr.robotId;
				}
				return false;
			})
			.map(e -> (CheckValueReturn)e)
			.thenAccept(e -> System.out.println("Recieved a Check Marker response from Robot " + e.robotId))
			.map(e -> e.value)
			.getValue();
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Failed to check the markers for robot " + RobotId, e.getTargetException());
		} catch (InterruptedException e) {
			throw new RuntimeException("Failed to check the markers for robot " + RobotId, e);
		}
	}
	
	@Override
	public void notify(BrainIoTEvent event) {

		System.out.println("Received an event type " + event.getClass());

		for(PendingRequest pr : pendingRequests) {
			try {
				if(pr.consumer.test(event)) {
					pr.latch.resolve(event);
				}
			} catch (Exception e) {
				pr.latch.fail(e);
			}
		}
	}
    
}
