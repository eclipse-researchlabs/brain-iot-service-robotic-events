package eu.brain.iot.warehouse.sensinact.test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.util.function.Predicate;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.BrainIoTEvent;
import eu.brain.iot.eventing.api.EventBus;
import eu.brain.iot.eventing.api.SmartBehaviour;
import eu.brain.iot.robot.sensinact.jsonReader.CartStorage;
import eu.brain.iot.robot.sensinact.jsonReader.CartTable;
import eu.brain.iot.robot.sensinact.jsonReader.DockAUX;
import eu.brain.iot.robot.sensinact.jsonReader.DockPose;
import eu.brain.iot.robot.sensinact.jsonReader.DockTable;
import eu.brain.iot.robot.sensinact.jsonReader.DockingPoint;
import eu.brain.iot.robot.sensinact.jsonReader.PickingPoint;
import eu.brain.iot.robot.sensinact.jsonReader.PickingTable;
import eu.brain.iot.robot.sensinact.jsonReader.Pose;
import eu.brain.iot.robot.sensinact.jsonReader.StorageAUX;
import eu.brain.iot.robot.sensinact.jsonReader.StoragePoint;
import eu.brain.iot.robot.sensinact.jsonReader.StoragePose;
import eu.brain.iot.robot.sensinact.jsonReader.StorageTable;
import eu.brain.iot.warehouse.sensiNact.api.PickingPointUpdateNotice;
import eu.brain.iot.warehouse.sensiNact.api.UpdateCartStorage;
import eu.brain.iot.warehouse.sensiNact.api.UpdateDockPoint;
import eu.brain.iot.warehouse.sensiNact.api.UpdatePickPoint;
import eu.brain.iot.warehouse.sensiNact.api.UpdateResponse;
import eu.brain.iot.warehouse.sensiNact.api.UpdateResponse.UpdateStatus;
import eu.brain.iot.warehouse.sensiNact.api.UpdateStoragePoint;
import eu.brain.iot.warehouse.sensiNact.api.WarehouseTables.Tables;

@Component(
		immediate=true,
		configurationPid = "eu.brain.iot.warehouse.sensinact.SensiNact", 
		configurationPolicy = ConfigurationPolicy.OPTIONAL,
		service = {SmartBehaviour.class}
)
@SmartBehaviourDefinition(consumed = {PickingPointUpdateNotice.class, UpdateResponse.class}, 
	/*	filter = "(robotID=*)",*/ author = "LINKS", name = "SensiNact Test", 
		description = "Dynamically configure warehouse backend"
)
public class SensiNact implements SmartBehaviour<BrainIoTEvent> {
    
	private String resourcesPath;
	  
	  @ObjectClassDefinition
		public static @interface Config {
			String resourcesPath() default "/opt/fabric/resources/"; // "/opt/fabric/resources/"; /home/rui/resources
		}

	  private ExecutorService workers;
	  private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
	  
		private ServiceRegistration<?> reg;
		private JsonDataReader jsonDataReader;

	private  Logger logger;
	
	@Reference
	private EventBus eventBus;
	  
	@Activate
	public void init(BundleContext context, Config config, Map<String, Object> props)  {
	
		this.resourcesPath = config.resourcesPath();
		if(resourcesPath != null && resourcesPath.length()>0) {
			if(!resourcesPath.endsWith(File.separator)) {
				resourcesPath+=File.separator;
			}
			
		}
		System.setProperty("logback.configurationFile", resourcesPath+"logback.xml");
		logger = (Logger) LoggerFactory.getLogger(SensiNact.class.getSimpleName());

	//	logger.info("table creator resourcesPath = "+resourcesPath +", UUID = "+ context.getProperty("org.osgi.framework.uuid"));
		System.out.println("SensiNact is starting............");
		
		workers = Executors.newFixedThreadPool(10);

		try {
			jsonDataReader = new JsonDataReader(resourcesPath);
			
		} catch (Exception e) {
			logger.error("\nSensiNact Exception: {}", ExceptionUtils.getStackTrace(e));
		}
		
		updatePickPoint();
		updateStoragePoint();
		updateCartStoragePoint();
		updateDockPoint();
		
		worker.schedule(this::update, 2, TimeUnit.SECONDS);
	}

	public void updatePickPoint() {
		worker.execute(() -> {
		PickingTable pickingTable = jsonDataReader.pickingTable;
		List<PickingPoint> pickingPoints = pickingTable.getPickingPoints();
		
		System.out.println("pickingPoints size = "+pickingPoints.size());
		
		for (PickingPoint pickingPoint : pickingPoints) {
			UpdatePickPoint pick = new UpdatePickPoint();
			pick.pickID = pickingPoint.getPPid();
			pick.pickPoint = serializePose(pickingPoint.getPose());
			pick.isAssigned = pickingPoint.getIsAssigned();
			
			System.out.println("SensiNact sends to creator PickPoint: pickID="+pick.pickID+", pickPoint="+pick.pickPoint+", isAssigned= "+pick.isAssigned);
			
	//		eventBus.deliver(pick);
			UpdateStatus status = queryUpdateState(pick, Tables.PICKING_TABLE);
			
			System.out.println("SensiNact update Pick Point status="+status);
		}
		});
	}
	
	public void updateStoragePoint() {
		worker.execute(() -> {
		StorageTable storageTable = jsonDataReader.storageTable;
		  List<StoragePoint> storagePoints = storageTable.getStoragePoints();
		
		  System.out.println("storagePoints size = "+storagePoints.size());
		  
		for (StoragePoint sp : storagePoints) {
			UpdateStoragePoint st = new UpdateStoragePoint();
			st.storageID = sp.getSTid();
			st.storageAUX = serializeStorageAUX(sp.getStorageAUX());
			st.storagePoint = serializeStoragePose(sp.getStoragePose());
			
	//		eventBus.deliver(st);
			queryUpdateState(st, Tables.STORAGE_TABLE);
			
			System.out.println("SensiNact sends to creator StoragePoint: storageID="+st.storageID+", storageAUX="+st.storageAUX+", storagePoint"+st.storagePoint);
			
		}
		});
	}
	
	public void updateCartStoragePoint() {
		worker.execute(() -> {
	
		CartTable cartTable = jsonDataReader.cartTable;
		List<CartStorage> cartStorages = cartTable.getCartStorages();
		
		System.out.println("cartStorages size = "+cartStorages.size());
		
		for (CartStorage cs : cartStorages) {
			UpdateCartStorage ucs = new UpdateCartStorage();
			ucs.cartID = Integer.parseInt(cs.getCartID());
			ucs.storageID = cs.getStorageID();
			
		//	eventBus.deliver(ucs);
			queryUpdateState(ucs, Tables.CART_STORAGE_TABLE);
			
			System.out.println("SensiNact sends to creator CartStoragePoint: cartID="+ucs.cartID+", storageID="+ucs.storageID);
			
		}
		});
	}
	
	public void updateDockPoint() {
		worker.execute(() -> {
	
		DockTable dockTable = jsonDataReader.dockTable;
		List<DockingPoint> dockingPoints = dockTable.getDockingPoints();

		System.out.println("dockingPoints size = "+dockingPoints.size());
		
		for (DockingPoint dp : dockingPoints) {
			UpdateDockPoint udp = new UpdateDockPoint();
			udp.robotIP = dp.getIPid();
			udp.dockAUX = serializeDockAUX(dp.getDockAUX());
			udp.dockPoint = serializeDockPose(dp.getDockPose());

	//		eventBus.deliver(udp);
			queryUpdateState(udp, Tables.DOCKING_TABLE);
			
			System.out.println("SensiNact sends to creator DockPoint: robotIP="+udp.robotIP+", dockAUX="+udp.dockAUX+", dockPoint"+udp.dockPoint);
			
		}
		});
	}
	
	
/*	@Override
	public void notify(BrainIoTEvent event) {
		logger.info("--> SensiNact received an event " + event.getClass().getSimpleName());
		
		// -----------------------------------   used for Queryer  start  ------------------------------------------------	
			
			if (event instanceof PickingPointUpdateNotice) {
				PickingPointUpdateNotice pickRequest = (PickingPointUpdateNotice) event;
				worker.execute(() -> {
					QueryPickResponse rs = getQueryPickResponse(pickRequest);
					if (rs != null && rs.pickID!=null) {
						rs.robotID = pickRequest.robotID;
						eventBus.deliver(rs);
					//	logger.info("--> Table Creator got a pickID="+rs.pickID+", pickPoint= " + rs.pickPoint);
						logger.info("Creator  sent to Queryer QueryPickResponse, robotID= "+rs.robotID+", pickID= " + rs.pickID+", pickPoint="+rs.pickPoint);
						
						PickingPointUpdateNotice notice = new PickingPointUpdateNotice();   // TODO 1 for interaction with SensiNact
						notice.pickID = rs.pickID;
						notice.isAssigned = true;
						eventBus.deliver(notice);
						logger.info("Creator  sent to SensiNact PickingPointUpdateNotice " + notice);
					}
				});
			}
		
	}*/
	
	
	@Override
	public void notify(BrainIoTEvent event) {

	//	System.out.println("SensiNact Received an event type " + event.getClass().getSimpleName());

		if (event instanceof PickingPointUpdateNotice) {
			PickingPointUpdateNotice notice = (PickingPointUpdateNotice) event;
			System.out.println("SensiNact got PickingPointUpdateNotice pickID= "+notice.pickID+", isAssigned= " + notice.isAssigned);

				
			
		} else {
			for (PendingRequest pr : pendingRequests) {
				try {
					if (pr.consumer.test(event)) {
						pr.latch.resolve(event);
					}
				} catch (Exception e) {
					pr.latch.fail(e);
				}
			}
		}
	}
	
	
	private void update() {

		if (pendingRequests.size() > 0) {
			for (PendingRequest pr : pendingRequests) {
				pr.processed++;
				if (pr.processed > 0) {
					eventBus.deliver(pr.toResend);
				}
			}
		}
  //  	worker.schedule(this::update, 2, TimeUnit.SECONDS);
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
	
	
	public UpdateStatus queryUpdateState(BrainIoTEvent event, Tables table){
		try {
			return queryUpdateStateAsync(event, table)
					.getValue();
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Failed to update " + table, e.getTargetException());
		} catch (InterruptedException e) {
			throw new RuntimeException("Failed to update " + table, e);
		}
	}


	private Promise<UpdateStatus> queryUpdateStateAsync(BrainIoTEvent event, Tables table) {

			return awaitResponse(event, e -> {
		//		System.out.println("SensiNact send  "+event);
				
				if(e instanceof UpdateResponse) {
					UpdateResponse resp = (UpdateResponse) e;
					return /*resp.updateStatus == UpdateResponse.UpdateStatus.OK && */resp.table == table;
				}
				return false;
			})
			.map(e -> (UpdateResponse)e)
			.thenAccept(e -> System.out.println("SensiNact Recieved a update "+table+" response = "+e.updateStatus))
			.map(e -> e.updateStatus);
	}
	
	private String serializePose(Pose pose) {
		  
		  return pose.getX()+","+pose.getY()+","+pose.getZ();
	  }
	private String serializeStorageAUX(StorageAUX pose) {
		  
		  return pose.getX()+","+pose.getY()+","+pose.getZ();
	  }

	  private String serializeStoragePose(StoragePose pose) {
	  
		  return pose.getX()+","+pose.getY()+","+pose.getZ();
	  }
	  
	  private String serializeDockAUX(DockAUX pose) {
		  
		  return pose.getX()+","+pose.getY()+","+pose.getZ();
	  }

	  private String serializeDockPose(DockPose pose) {
	  
		  return pose.getX()+","+pose.getY()+","+pose.getZ();
	  }
    
}
