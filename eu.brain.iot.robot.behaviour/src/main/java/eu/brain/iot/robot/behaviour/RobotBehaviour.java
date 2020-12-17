package eu.brain.iot.robot.behaviour;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import eu.brain.iot.service.robotic.door.api.DoorStatusRequest;
import eu.brain.iot.service.robotic.door.api.DoorStatusRequest.State;
import eu.brain.iot.service.robotic.door.api.DoorStatusResponse;
import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.BrainIoTEvent;
import eu.brain.iot.eventing.api.EventBus;
import eu.brain.iot.eventing.api.SmartBehaviour;
import eu.brain.iot.robot.api.Command;
import eu.brain.iot.robot.api.Coordinate;
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
import eu.brain.iot.warehouse.events.WarehouseCommand;


@Component(
		configurationPid= "eu.brain.iot.robot.behaviour.Behaviour",
		configurationPolicy=ConfigurationPolicy.REQUIRE,
		service = {SmartBehaviour.class})

@SmartBehaviourDefinition(
		consumed = {MarkerReturn.class, QueryStateValueReturn.class, RobotReadyResponse.class, AvailabilityReturn.class, DoorStatusResponse.class, // AvailabilityReturn event will not be used in current workflow, it's for future use
				NewPickPointResponse.class, NewStoragePointResponse.class, CartNoticeResponse.class, DockingResponse.class },  // the events in this line are used to communicate with warehouse backend component  
		author = "UGA", name = "Robot Behavior",
		description = "Implements a Robot Behavior.")

public class RobotBehaviour implements SmartBehaviour<BrainIoTEvent> {

    private int robotID;
    private static volatile String robotIP;
	private boolean isRobotReady=false;
	private static volatile QueryStateValueReturn queryReturn;
	private static volatile int markerID = 0;
	private static volatile NewPickPointResponse pickResponse = null;
	private static volatile NewStoragePointResponse storageResponse = null;
	private static volatile DockingResponse dockingResponse = null;
	private static volatile CartNoticeResponse cartNoticeResponse = null;

	
	@ObjectClassDefinition
	public static @interface Config {
		
		@AttributeDefinition(description = "The identifier for the robot behaviour")
		int id();

	}

	private Config config;
	private ExecutorService worker;
	private ServiceRegistration<?> reg;

	@Reference
	private EventBus eventBus;
	
    @Activate
	void activate(BundleContext context, Config config, Map<String,Object> props){
    	
	    this.config=config;
	    this.robotID=config.id();
	    
	    System.out.println("\n Hello!  I am robotBehavior : "+robotID);
	    
	    worker = Executors.newFixedThreadPool(10);

	    Dictionary<String, Object> serviceProps = new Hashtable<>(props.entrySet().stream()
				.filter(e -> !e.getKey().startsWith("."))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
			
			serviceProps.put(SmartBehaviourDefinition.PREFIX_ + "filter", 
		    String.format("(|(robotID=%s)(robotID=%s))", robotID, RobotCommand.ALL_ROBOTS));
			
			System.out.println("+++++++++ Robot Behaviour filter = "+serviceProps.get(SmartBehaviourDefinition.PREFIX_ + "filter"));
			reg = context.registerService(SmartBehaviour.class, this, serviceProps);

		

	}
    
    
	@Override
	public void notify(BrainIoTEvent event) {
		
		if (event instanceof RobotReadyResponse) {
			RobotReadyResponse rbc = (RobotReadyResponse) event;
			worker.execute(() -> {
				System.out.println("-->RB" + robotID + " received RobotReady event");
				isRobotReady = rbc.isReady;
			});

		} else if (event instanceof NewPickPointResponse) {
			this.pickResponse = (NewPickPointResponse) event;
				worker.execute(() -> {
					System.out.println("-->RB" + robotID + " receive NewPickPointResponse ");
				});

			} else if (event instanceof NewStoragePointResponse) {
				this.storageResponse = (NewStoragePointResponse) event;
				worker.execute(() -> {
					System.out.println("-->RB" + robotID + " receive NewStoragePointResponse ");
				});

			} else if (event instanceof DockingResponse) {
				this.dockingResponse = (DockingResponse) event;
				worker.execute(() -> {
					System.out.println("-->RB" + robotID + " receive DockingResponse ");
				});

			} else if (event instanceof CartNoticeResponse) {
				this.cartNoticeResponse = (CartNoticeResponse) event;


			} else if (event instanceof QueryStateValueReturn) {
				QueryStateValueReturn qs = (QueryStateValueReturn) event;


			} else if (event instanceof MarkerReturn) {
				MarkerReturn cvr = (MarkerReturn) event;
				

			} else if (event instanceof DoorStatusResponse) {
						DoorStatusResponse response = (DoorStatusResponse) event;
						if(response.currentState == State.OPEN) {
							System.out.println("-->RB" + robotID + " door is opened successfully!!!!");
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
		PickCart pc= new PickCart();
		pc.robotID=robotID;
		pc.markerID=markerID;
		return pc;
	}
	
	private PlaceCart createPlaceCart() {
		PlaceCart placeCart=new PlaceCart();
		placeCart.robotID=robotID;
		return placeCart;
	}
	private Cancel createCancel(Command command) {
		Cancel cancel=new Cancel();
		cancel.robotID=robotID;
		return cancel;
	}
	
	private CheckMarker createCheckMarker() {
		CheckMarker checkMarker=new CheckMarker();
		checkMarker.robotID=robotID;
		return checkMarker;
	}
	
	private CartMovedNotice createCartMovedNotice() {
		CartMovedNotice cartMovedNotice=new CartMovedNotice();
		// the shared warehouse backend will receive all events with robotBehaviorID=-1, not only the events from a specific robot behaviour
		cartMovedNotice.robotBehaviorID = WarehouseCommand.ALL_ROBOT_BEHAVIOURS;  // the default robotBehaviorID = -1, so this statement could also be removed
		cartMovedNotice.pickPoint = pickResponse.pickPoint;
		return cartMovedNotice;
	}
	
	private NoCartNotice createNoCartNotice() {
		NoCartNotice noCartNotice=new NoCartNotice();
		// the shared warehouse backend will receive all events with robotBehaviorID=-1, not only the events from a specific robot behaviour
		noCartNotice.robotBehaviorID = WarehouseCommand.ALL_ROBOT_BEHAVIOURS;
		noCartNotice.pickPoint = pickResponse.pickPoint;
		return noCartNotice;
	}
	
	private DockingRequest createDockingRequest() {
		DockingRequest dockingRequest = new DockingRequest();
		dockingRequest.robotIP = robotIP;
		return dockingRequest;
	}
	

}
