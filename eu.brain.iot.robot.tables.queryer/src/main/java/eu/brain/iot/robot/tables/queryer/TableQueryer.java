/*******************************************************************************
 * Copyright (C) 2021 LINKS Foundation
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package eu.brain.iot.robot.tables.queryer;

import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
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
import eu.brain.iot.warehouse.events.CartMovedNotice;
import eu.brain.iot.warehouse.events.CartNoticeResponse;
import eu.brain.iot.warehouse.events.DockingRequest;
import eu.brain.iot.warehouse.events.DockingResponse;
import eu.brain.iot.warehouse.events.NewPickPointRequest;
import eu.brain.iot.warehouse.events.NewPickPointResponse;
import eu.brain.iot.warehouse.events.NewStoragePointRequest;
import eu.brain.iot.warehouse.events.NewStoragePointResponse;
import eu.brain.iot.warehouse.events.NoCartNotice;
import eu.brain.iot.robot.api.Coordinate;
import eu.brain.iot.robot.tables.creator.api.TableCreator;


@Component(service = { TableQueryer.class },
		   immediate = true,
		   configurationPid = "eu.brain.iot.robot.tables.queryer.TablesQueryer", 
		   configurationPolicy = ConfigurationPolicy.OPTIONAL
		)
@SmartBehaviourDefinition(
		consumed = { NewPickPointRequest.class, NewStoragePointRequest.class, NoCartNotice.class,
		CartMovedNotice.class, DockingRequest.class }, 
		author = "LINKS", name = "Warehouse Module: Tables Queryer", 
		description = "Implements the Tables Queryer.")

public class TableQueryer implements SmartBehaviour<BrainIoTEvent> { // TODO must able to cache multiple requests

	private ExecutorService worker;
	private ServiceRegistration<?> reg;
	
	@Reference
	private EventBus eventBus;
	
	@Reference
	private TableCreator tablesCreator;
	
	private  Logger logger;
	private String logPath;
	
	@ObjectClassDefinition
	public static @interface Config {  // if run with creator, this logback.xml is not used
		String logPath() default "/opt/fabric/resources/logback.xml";

	}

	@Activate
	public void activate(BundleContext context, Config config, Map<String, Object> props) throws SQLException {
		this.logPath = config.logPath();

			System.setProperty("logback.configurationFile", logPath);
			
			logger = (Logger) LoggerFactory.getLogger(TableQueryer.class.getSimpleName());
					
			logger.info("Hello, this is Table Queryer !");
			
			logger.info("Table Queryer is using log: "+logPath);

			worker = Executors.newFixedThreadPool(10);
			
			Dictionary<String, Object> serviceProps = new Hashtable<>(props.entrySet().stream()
					.filter(e -> !e.getKey().startsWith(".")).collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
			
			serviceProps.put(SmartBehaviourDefinition.PREFIX_ + "filter",  // get all events
					String.format("(robotID=*)"));
			
			logger.info("+++++++++ Table Queryer filter = " + serviceProps.get(SmartBehaviourDefinition.PREFIX_ + "filter"));
			reg = context.registerService(SmartBehaviour.class, this, serviceProps);
			
			logger.info("------------Queryer:  PickingTable ----------------");
			
			
			try {
			  ResultSet rs = tablesCreator.executeQuery("SELECT * FROM PickingTable");

		if (rs != null) {
			while (rs.next()) {
				logger.info(rs.getString("PPid") + ", " + rs.getString("pose") + ", " + rs.getString("isAssigned"));
			}
		} else
			logger.error("Error accours to query PickingTable, got null");
			} catch(Exception e) {
				logger.error("\n Exception:", e);
			}
	}
	
	
	@Override
	public void notify(BrainIoTEvent event) {

		logger.info("--> Table Queryer received an event "+event.getClass());
		
		if (event instanceof NewPickPointRequest) {
			NewPickPointRequest pickRequest = (NewPickPointRequest) event;
			NewPickPointResponse rs = getPickResponse(pickRequest);
			if(rs !=null) {
				logger.info("Queryer  sent NewPickPointResponse "+ rs);
				eventBus.deliver(rs);
			}	
		} else if (event instanceof NewStoragePointRequest) {
			NewStoragePointRequest storageRequest = (NewStoragePointRequest) event;
			worker.execute(() -> {
				NewStoragePointResponse resp = getStorageResponse(storageRequest);
				if(resp!=null) {
					logger.info("Queryer  sent NewStoragePointResponse "+ resp);
					eventBus.deliver(resp);
				}
			});
			
		} else if (event instanceof DockingRequest) {
			DockingRequest dockRequest = (DockingRequest) event;
			worker.execute(() -> {
				DockingResponse resp = getDockResponse(dockRequest);
				if(resp!=null) {
					logger.info("Queryer  sent DockingResponse "+ resp);
					eventBus.deliver(resp);
				}
			});
			
		} else if (event instanceof CartMovedNotice) {
			CartMovedNotice cartMovedNotice = (CartMovedNotice) event;
			worker.execute(() -> {
				CartNoticeResponse resp = getCartMovedNotice(cartMovedNotice);
				if(resp!=null) {
					logger.info("Queryer  sent CartNoticeResponse "+ resp);
					eventBus.deliver(resp);
				}
			});
			
		} else if (event instanceof NoCartNotice) {
			NoCartNotice noCartNotice = (NoCartNotice) event;
			worker.execute(() -> {
				eventBus.deliver(getNoCartNotice(noCartNotice));
			});
		}
	}
	

	private NewPickPointResponse getPickResponse(NewPickPointRequest pickRequest) {

		NewPickPointResponse pickReponse = new NewPickPointResponse();
		pickReponse.robotID = pickRequest.robotID;
		try {

		ResultSet rs = tablesCreator.executeQuery("SELECT * FROM PickingTable WHERE isAssigned=false");

		if (rs == null) {
			logger.error("Error accours to query PickingTable, nothing to reply");
			return null;
		} else {
			while (rs.next()) {

				pickReponse.hasNewPoint = true;
				pickReponse.pickPoint = rs.getString("pose");
				logger.info("--> Table Queryer got a pickPoint "+pickReponse.pickPoint);
				tablesCreator.executeUpdate(
						"UPDATE PickingTable SET isAssigned='" + true + "' WHERE PPid='" + rs.getString("PPid") + "'");
				break;
			}
		}
		logger.info("------------Queryer:  PickingTable ----------------");
		  rs = tablesCreator.executeQuery("SELECT * FROM PickingTable");
		  while (rs.next()) {
			  logger.info(rs.getString("PPid") + ", " + rs.getString("pose")+ ", " + rs.getString("isAssigned"));
		  }
			  
		} catch (Exception e) {
			logger.error("\n Exception:", e);
			return null;
		}
		return pickReponse;
	}

	private NewStoragePointResponse getStorageResponse(NewStoragePointRequest storageRequest) {

		int markerID = storageRequest.markerID;
		String storageID = null;

		NewStoragePointResponse storageReponse = new NewStoragePointResponse();
		storageReponse.robotID = storageRequest.robotID;
		try {
		ResultSet rs = tablesCreator.executeQuery("SELECT * FROM CartTable WHERE cartID=" + markerID);
		if (rs == null) {
			logger.error("Error accours to query CartTable, no StorageResponse to reply");
			return null;
		} else {
			while (rs.next()) {
				storageID = rs.getString("storageID");
				break;
			}
			if (storageID != null) {

				rs = tablesCreator.executeQuery("SELECT * FROM StorageTable WHERE STid = '" +storageID+"'");
				if (rs == null) {
					logger.error("Error accours to query StorageTable, no StorageResponse to reply");
					return null;
				} else {
				while (rs.next()) {

					storageReponse.hasNewPoint = true;
					storageReponse.storageAuxliaryPoint = rs.getString("storageAUX");
					storageReponse.storagePoint = rs.getString("storagePose");
					break;
				}
				}
			}
		}
			
		} catch (Exception e) {
			logger.error("\n Exception:", e);
			return null;
		}

		return storageReponse;
	}
	
	private DockingResponse getDockResponse(DockingRequest dockingRequest) {
		logger.info("--> Table Queryer got a DockingRequest for robot "+dockingRequest.robotID);
		
		DockingResponse dockReponse = new DockingResponse();
		dockReponse.robotID = dockingRequest.robotID;
		try {
		ResultSet rs = tablesCreator.executeQuery("SELECT * FROM DockTable WHERE IPid = '"+ dockingRequest.robotID+"'");
		if (rs == null) {
			logger.error("Error accours to query DockTable, nothing to reply");
			return null;
		} else {
			while (rs.next()) {

				dockReponse.hasNewPoint = true;
				dockReponse.dockAuxliaryPoint = rs.getString("dockAUX");
				dockReponse.dockingPoint = rs.getString("dockPose");
				break;
			}
		}
		} catch (Exception e) {
			logger.error("\n Exception:", e);
			return null;
		}

		return dockReponse;
	}
	

	private Coordinate getCoordinate(String crd) {

		Coordinate cord = new Coordinate();

		String[] strs = crd.trim().split(",");
		cord.setX(new Double(strs[0]).doubleValue());
		cord.setY(new Double(strs[1]).doubleValue());
		cord.setZ(new Double(strs[2]).doubleValue());

		return cord;
	}
	
	private CartNoticeResponse getCartMovedNotice(CartMovedNotice cartMovedNotice) {

		CartNoticeResponse cartNoticeResponse = new CartNoticeResponse();
		cartNoticeResponse.robotID = cartMovedNotice.robotID;

		Coordinate targetPoint = getCoordinate(cartMovedNotice.pickPoint);
		Coordinate pickPose = null;
		try {
		ResultSet rs = tablesCreator.executeQuery("SELECT * FROM PickingTable WHERE isAssigned=true");
		if (rs == null) {
			logger.error("Error accours to query PickingTable marked with TRUE, nothing to reply");
			return null;
		} else {
			while (rs.next()) {
				pickPose = getCoordinate(rs.getString("pose"));
				
				if(pickPose.getX() == targetPoint.getX() && pickPose.getY() == targetPoint.getY() && pickPose.getZ() == targetPoint.getZ()) {
					tablesCreator.executeUpdate(
							"UPDATE PickingTable SET isAssigned='" + false + "' WHERE PPid='" + rs.getString("PPid") + "'");
					pickPose = null;
					break;
				}
				pickPose = null;
				
			}
		}
		logger.info("------------Queryer:  PickingTable ----------------");

		  rs = tablesCreator.executeQuery("SELECT * FROM PickingTable");

		  while (rs.next()) {
		       logger.info(rs.getString("PPid") + ", " + rs.getString("pose")+ ", " + rs.getString("isAssigned"));
		  }
		  
		  logger.info("Table Queryer is sending CartNoticeResponse = "+CartNoticeResponse.noticeStatus);
			
		} catch (Exception e) {
			logger.error("\n Exception:", e);
			return null;
		}
		return cartNoticeResponse;
	}
	
	private CartNoticeResponse getNoCartNotice(NoCartNotice noCartNotice) {

		CartNoticeResponse cartNoticeResponse = new CartNoticeResponse();
		cartNoticeResponse.robotID = noCartNotice.robotID;

	//	Coordinate targetPoint = noCartNotice.pickPoint;
		Coordinate targetPoint = getCoordinate(noCartNotice.pickPoint);
		Coordinate pickPose = null;
	
		try {
			ResultSet rs = tablesCreator.executeQuery("SELECT * FROM PickingTable WHERE isAssigned=true");
			if (rs == null) {
				logger.error("Error accours to query PickingTable marked with TRUE, nothing to reply");
				return null;
			} else {
				while (rs.next()) {
					pickPose = getCoordinate(rs.getString("pose"));
					
					if(pickPose.getX() == targetPoint.getX() && pickPose.getY() == targetPoint.getY() && pickPose.getZ() == targetPoint.getZ()) {
						tablesCreator.executeUpdate(
								"UPDATE PickingTable SET isAssigned='" + false + "' WHERE PPid='" + rs.getString("PPid") + "'");
						pickPose = null;
						break;
					}
					pickPose = null;
					
				}
			}
			logger.info("------------Queryer:  PickingTable ----------------");

			  rs = tablesCreator.executeQuery("SELECT * FROM PickingTable");

			  while (rs.next()) {
			       logger.info(rs.getString("PPid") + ", " + rs.getString("pose")+ ", " + rs.getString("isAssigned"));
			  }
			  
			  logger.info("Table Queryer is sending CartNoticeResponse = "+CartNoticeResponse.noticeStatus);
				
			} catch (Exception e) {
				logger.error("\n Exception:", e);
				return null;
			}

		return cartNoticeResponse;
	}
	
	
	
	@Deactivate
	void stop() {
		worker.shutdown();
		try {
			worker.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			logger.error("\n Exception:", ie);
		}
		logger.info("------------  Table Queryer is deactivated----------------");
	}


}
