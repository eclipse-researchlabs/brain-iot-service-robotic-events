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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Dictionary;
import java.util.Hashtable;
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
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
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


@Component(service = { TableQueryer.class },
		   immediate = true)
@SmartBehaviourDefinition(
		consumed = { NewPickPointRequest.class, NewStoragePointRequest.class, NoCartNotice.class,
		CartMovedNotice.class, DockingRequest.class }, 
		author = "LINKS", name = "Warehouse Module: Tables Queryer", 
		description = "Implements the Tables Queryer.")

public class TableQueryer implements SmartBehaviour<BrainIoTEvent> { // TODO must able to cache multiple requests

//	private static String base = "/home/rui/git/ros-edge-node/eu.brain.iot.robot.tables.creator";
//		private static String base = "/home/fabric-n9";

//	private static final String JDBC_URL = "jdbc:h2:" + base + "/tables;DB_CLOSE_DELAY=-1";

	private static final String USER = "RosEdgeNode";

	private static final String PASSWORD = "123";

	private static final String DRIVER_CLASS = "org.h2.Driver";

	private Connection conn;
	private Statement stmt;
	private ExecutorService worker;
	private ServiceRegistration<?> reg;
	
	@Reference
	private EventBus eventBus;
	
	// Question: can tablesCreater be referenced from a different osgi FW in same node or different node in fabric?
	
	// TODO: MUST run with creator to install, but // TODO don't close it if it's a referenced osgi service
//	@Reference
//	private TableCreator tablesCreater;
	
//	private static final Logger logger = (Logger) LoggerFactory.getLogger(TableQueryer.class.getSimpleName());
	private  Logger logger;

	@Activate
	public void activate(BundleContext context, Map<String, Object> props) throws SQLException {
		try {
			
	//		String home  = System.getenv("HOME");
		
	
			System.setProperty("logback.configurationFile", "/opt/fabric/resources/logback.xml");
			
			logger = (Logger) LoggerFactory.getLogger(TableQueryer.class.getSimpleName());
					
			logger.info("\nHello, this is Table Queryer !");
			
			Class.forName(DRIVER_CLASS);
			
//-----------------------todo------------------------------
			
			// /home/fabric-n9/tables
			final String JDBC_URL = "jdbc:h2:"+"/opt/fabric/resources/tables;DB_CLOSE_DELAY=-1";
			
			logger.info("Table Queryer is reading "+"/opt/fabric/resources/tables.mv.db..........");
			
			conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
			stmt = conn.createStatement();
//-----------------------------------------------------			
			
	//		conn = tablesCreater.getConn();
	//		stmt = tablesCreater.getStmt();

			worker = Executors.newFixedThreadPool(10);
			
			Dictionary<String, Object> serviceProps = new Hashtable<>(props.entrySet().stream()
					.filter(e -> !e.getKey().startsWith(".")).collect(Collectors.toMap(Entry::getKey, Entry::getValue)));

		/*	serviceProps.put(SmartBehaviourDefinition.PREFIX_ + "filter",  // -1, get all events
					String.format("(|(robotID=%s)(robotID=%s))", 2, RobotCommand.ALL_ROBOTS));*/

		/*	serviceProps.put(SmartBehaviourDefinition.PREFIX_ + "filter",  // robotBehaviorID = -1, warehouse backend get all events from robot behaviours
					String.format("(|(robotID=%s)(robotBehaviorID=%s)", null, RobotCommand.ALL_ROBOTS, true));*/
			
			serviceProps.put(SmartBehaviourDefinition.PREFIX_ + "filter",  // get all events
					String.format("(robotID=*)"));
			
			logger.info("+++++++++ Table Queryer filter = " + serviceProps.get(SmartBehaviourDefinition.PREFIX_ + "filter"));
			reg = context.registerService(SmartBehaviour.class, this, serviceProps);
			
			logger.info("------------  PickingTable ----------------");

			  ResultSet rs = stmt.executeQuery("SELECT * FROM PickingTable");

			  while (rs.next()) {
				  logger.info(rs.getString("PPid") + ", " + rs.getString("pose")+ ", " + rs.getString("isAssigned"));
			  }
			
		} catch (ClassNotFoundException e) {
			logger.error("\n Exception:", e);

		} catch (SQLException e) {
			if (stmt != null && !stmt.isClosed()) {
				stmt.close();
			}
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
			logger.error("\n Exception:", e);
		}
	
	}
	
	
	@Override
	public void notify(BrainIoTEvent event) {

		logger.info("--> Table Queryer received an event "+event.getClass()/*.getSimpleName()*/);
		
		if (event instanceof NewPickPointRequest) {
			NewPickPointRequest pickRequest = (NewPickPointRequest) event;
			NewPickPointResponse rs = getPickResponse(pickRequest);
			logger.info("Queryer  sent NewPickPointResponse "+ rs);
			eventBus.deliver(rs);
			
		} else if (event instanceof NewStoragePointRequest) {
			NewStoragePointRequest storageRequest = (NewStoragePointRequest) event;
			worker.execute(() -> {
		//		System.out.println("--> Table Queryer received NewStoragePointRequest event");
				eventBus.deliver(getStorageResponse(storageRequest));
			});
			
		} else if (event instanceof DockingRequest) {
			DockingRequest dockRequest = (DockingRequest) event;
			worker.execute(() -> {
				eventBus.deliver(getDockResponse(dockRequest));
			});
			
		} else if (event instanceof CartMovedNotice) {
			CartMovedNotice cartMovedNotice = (CartMovedNotice) event;
			worker.execute(() -> {
				eventBus.deliver(getCartMovedNotice(cartMovedNotice));
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

		ResultSet rs;
		try {
			rs = stmt.executeQuery("SELECT * FROM PickingTable WHERE isAssigned=false");

			while (rs.next()) {

				pickReponse.hasNewPoint = true;

		//		pickReponse.pickPoint = getCoordinate(rs.getString("pose"));
				pickReponse.pickPoint = rs.getString("pose");
				logger.info("--> Table Queryer got a pickPoint "+pickReponse.pickPoint);
				stmt.executeUpdate(
						"UPDATE PickingTable SET isAssigned='" + true + "' WHERE PPid='" + rs.getString("PPid") + "'");
				break;
			}
			
			logger.info("------------  PickingTable ----------------");
			  rs = stmt.executeQuery("SELECT * FROM PickingTable");
			  while (rs.next()) {
				  logger.info(rs.getString("PPid") + ", " + rs.getString("pose")+ ", " + rs.getString("isAssigned"));
			  }
			  
		} catch (SQLException e) {
			logger.error("\n Exception:", e);
		}

		return pickReponse;
	}

	private NewStoragePointResponse getStorageResponse(NewStoragePointRequest storageRequest) {

		int markerID = storageRequest.markerID;
		String storageID = null;

		NewStoragePointResponse storageReponse = new NewStoragePointResponse();
		storageReponse.robotID = storageRequest.robotID;

		ResultSet rs;
		try {
			rs = stmt.executeQuery("SELECT * FROM CartTable WHERE cartID=" + markerID);

			while (rs.next()) {
				storageID = rs.getString("storageID");
			}
			if (storageID != null) {

				rs = stmt.executeQuery("SELECT * FROM StorageTable WHERE STid = '" +storageID+"'");
				
				while (rs.next()) {

					storageReponse.hasNewPoint = true;
					storageReponse.storageAuxliaryPoint = rs.getString("storageAUX");
					storageReponse.storagePoint = rs.getString("storagePose");
					break;
				}
			}
		} catch (SQLException e) {
			logger.error("\n Exception:", e);
		}

		return storageReponse;
	}
	
	private DockingResponse getDockResponse(DockingRequest dockingRequest) {
		logger.info("--> Table Queryer got a DockingRequest for robot "+dockingRequest.robotID);
		
		DockingResponse dockReponse = new DockingResponse();
		dockReponse.robotID = dockingRequest.robotID;

		ResultSet rs;
		try {
			rs = stmt.executeQuery("SELECT * FROM DockTable WHERE IPid = '"+ dockingRequest.robotID+"'");

			while (rs.next()) {

				dockReponse.hasNewPoint = true;
				dockReponse.dockAuxliaryPoint = rs.getString("dockAUX");
				dockReponse.dockingPoint = rs.getString("dockPose");
				break;
			}
		} catch (SQLException e) {
			logger.error("\n Exception:", e);
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
		
		ResultSet rs;
		try {
			rs = stmt.executeQuery("SELECT * FROM PickingTable WHERE isAssigned=true");

			while (rs.next()) {
				pickPose = getCoordinate(rs.getString("pose"));
				
				if(pickPose.getX() == targetPoint.getX() && pickPose.getY() == targetPoint.getY() && pickPose.getZ() == targetPoint.getZ()) {
					stmt.executeUpdate(
							"UPDATE PickingTable SET isAssigned='" + false + "' WHERE PPid='" + rs.getString("PPid") + "'");
					pickPose = null;
					break;
				}
				pickPose = null;
				
			}
			logger.info("------------  PickingTable ----------------");

			  rs = stmt.executeQuery("SELECT * FROM PickingTable");

			  while (rs.next()) {
			       logger.info(rs.getString("PPid") + ", " + rs.getString("pose")+ ", " + rs.getString("isAssigned"));
			  }
			  
			  logger.info("Table Queryer is sending CartNoticeResponse = "+CartNoticeResponse.noticeStatus);
			  
		} catch (SQLException e) {
			logger.error("\n Exception:", e);
		}
		
		

		return cartNoticeResponse;
	}
	
	private CartNoticeResponse getNoCartNotice(NoCartNotice noCartNotice) {

		CartNoticeResponse cartNoticeResponse = new CartNoticeResponse();
		cartNoticeResponse.robotID = noCartNotice.robotID;

	//	Coordinate targetPoint = noCartNotice.pickPoint;
		Coordinate targetPoint = getCoordinate(noCartNotice.pickPoint);
		Coordinate pickPose = null;
		ResultSet rs;
		try {
			rs = stmt.executeQuery("SELECT * FROM PickingTable WHERE isAssigned=true");

			while (rs.next()) {
				pickPose = getCoordinate(rs.getString("pose"));
				
				if(pickPose.getX() == targetPoint.getX() && pickPose.getY() == targetPoint.getY() && pickPose.getZ() == targetPoint.getZ()) {
					stmt.executeUpdate(
							"UPDATE PickingTable SET isAssigned='" + false + "' WHERE PPid='" + rs.getString("PPid") + "'");
					pickPose = null;
					break;
				}
				pickPose = null;
				
			}
		} catch (SQLException e) {
			logger.error("\n Exception:", e);
		}

		return cartNoticeResponse;
	}
	
	
	@Deactivate
	void stop() {
		try {
			if (stmt != null && !stmt.isClosed()) {
				stmt.close();
			}
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		} catch (SQLException e) {
			logger.error("\n Exception:", e);
		}
		worker.shutdown();
		try {
			worker.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			logger.error("\n Exception:", ie);
		}
	}



}
