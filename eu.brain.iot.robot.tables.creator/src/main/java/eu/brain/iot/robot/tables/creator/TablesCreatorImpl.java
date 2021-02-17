/*******************************************************************************
 * Copyright (C) 2021 LINKS Foundation
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package eu.brain.iot.robot.tables.creator;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.BrainIoTEvent;
import eu.brain.iot.eventing.api.EventBus;
import eu.brain.iot.eventing.api.SmartBehaviour;
import eu.brain.iot.eventing.api.UntypedSmartBehaviour;
import eu.brain.iot.robot.tables.creator.api.Coordinate;
import eu.brain.iot.robot.tables.creator.api.GetPickingTable;
import eu.brain.iot.robot.tables.creator.api.PickingTableValues;
import eu.brain.iot.robot.tables.creator.api.QueryDockResponse;
import eu.brain.iot.robot.tables.creator.api.QueryDockTable;
import eu.brain.iot.robot.tables.creator.api.QueryPickResponse;
import eu.brain.iot.robot.tables.creator.api.QueryPickingTable;
import eu.brain.iot.robot.tables.creator.api.QueryStorageResponse;
import eu.brain.iot.robot.tables.creator.api.QueryStorageTable;
import eu.brain.iot.robot.tables.creator.api.UnsignPickingPointResponse;
import eu.brain.iot.robot.tables.creator.api.UnsignPickingPoint;
import eu.brain.iot.robot.tables.jsonReader.CartStorage;
import eu.brain.iot.robot.tables.jsonReader.CartTable;
import eu.brain.iot.robot.tables.jsonReader.DockAUX;
import eu.brain.iot.robot.tables.jsonReader.DockPose;
import eu.brain.iot.robot.tables.jsonReader.DockTable;
import eu.brain.iot.robot.tables.jsonReader.DockingPoint;
import eu.brain.iot.robot.tables.jsonReader.PickingPoint;
import eu.brain.iot.robot.tables.jsonReader.PickingTable;
import eu.brain.iot.robot.tables.jsonReader.Pose;
import eu.brain.iot.robot.tables.jsonReader.StorageAUX;
import eu.brain.iot.robot.tables.jsonReader.StoragePoint;
import eu.brain.iot.robot.tables.jsonReader.StoragePose;
import eu.brain.iot.robot.tables.jsonReader.StorageTable;
import eu.brain.iot.warehouse.sensiNact.api.UpdateCartStorage;
import eu.brain.iot.warehouse.sensiNact.api.UpdateDockPoint;
import eu.brain.iot.warehouse.sensiNact.api.UpdatePickPoint;
import eu.brain.iot.warehouse.sensiNact.api.UpdateResponse;
import eu.brain.iot.warehouse.sensiNact.api.UpdateResponse.UpdateStatus;
import eu.brain.iot.warehouse.sensiNact.api.UpdateStoragePoint;


@Component(
		immediate=true,
		configurationPid = "eu.brain.iot.robot.tables.creator.TablesCreatorImpl", 
		configurationPolicy = ConfigurationPolicy.OPTIONAL,
		service = {SmartBehaviour.class}
)
@SmartBehaviourDefinition(consumed = {QueryPickingTable.class, QueryStorageTable.class, QueryDockTable.class, UnsignPickingPoint.class, 
		GetPickingTable.class, UpdateCartStorage.class, UpdateDockPoint.class, UpdatePickPoint.class, UpdateStoragePoint.class}, 
		filter = "(robotID=*)", author = "LINKS", name = "Warehouse Module: Tables Creator", 
		description = "Implements Four Shared  Tables."
)
public class TablesCreatorImpl implements SmartBehaviour<BrainIoTEvent> {
		//Define the connection of database 
		  private static final String USER = "RosEdgeNode";

		  private static final String PASSWORD = "123";

		  private static final String DRIVER_CLASS="org.h2.Driver";
		  
		  private Connection conn;
		  private Statement stmt;
		  private JsonDataReader jsonDataReader;
		  private String resourcesPath;
		  
		  @ObjectClassDefinition
			public static @interface Config {
				String resourcesPath() default "/opt/fabric/resources/"; // "/opt/fabric/resources/"; /home/rui/resources
			}

		  private ExecutorService worker;
			private ServiceRegistration<?> reg;

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
			logger = (Logger) LoggerFactory.getLogger(TablesCreatorImpl.class.getSimpleName());

			logger.info("table creator resourcesPath = "+resourcesPath +", UUID = "+ context.getProperty("org.osgi.framework.uuid"));
			System.out.println("table creator resourcesPath = "+resourcesPath +", UUID = "+ context.getProperty("org.osgi.framework.uuid"));
			
			worker = Executors.newFixedThreadPool(10);
			try {
			jsonDataReader = new JsonDataReader(resourcesPath);
			
			try {
				final String JDBC_URL = "jdbc:h2:"+resourcesPath+"tables;DB_CLOSE_DELAY=-1";
				
				logger.info("Table Creator is creating "+resourcesPath+"tables.mv.db..........");
				
				Class.forName(DRIVER_CLASS);

				conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
				stmt = conn.createStatement();

				initPickingTable(stmt);
				initStorageTable(stmt);
				initCartTable(stmt);
				initDockTable(stmt);

				logger.info("Table Creator finished to create "+resourcesPath+"tables.mv.db..........");
				System.out.println("Table Creator finished to create "+resourcesPath+"tables.mv.db..........");
				
			/*	Dictionary<String, Object> serviceProps = new Hashtable<>(props.entrySet().stream()
						.filter(e -> !e.getKey().startsWith(".")).collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
				
				serviceProps.put(SmartBehaviourDefinition.PREFIX_ + "filter",String.format("(robotID=*)"));
				
				serviceProps.put("com.paremus.dosgi.scope", "universal");
				
				reg = context.registerService(SmartBehaviour.class, this, serviceProps);*/
				
			} catch (ClassNotFoundException e) {
				logger.error("\nCreator Exception: {}", ExceptionUtils.getStackTrace(e));
				
			} catch (SQLException e) {
				if(stmt != null && !stmt.isClosed()) {
					stmt.close();
				}
				if(conn != null && !conn.isClosed()) {
					conn.close();
				}
				logger.error("\nCreator Exception: {}", ExceptionUtils.getStackTrace(e));
			}
		} catch (Exception e) {
			logger.error("\nCreator Exception: {}", ExceptionUtils.getStackTrace(e));
		}
			
		}

		
	@Override
	public void notify(BrainIoTEvent event) {

		logger.info("--> Table Creator received an event " + event.getClass().getSimpleName());
		
	// -----------------------------------   used for Queryer  start  ------------------------------------------------	
		
		if (event instanceof QueryPickingTable) {
			QueryPickingTable pickRequest = (QueryPickingTable) event;
			worker.execute(() -> {
				QueryPickResponse rs = getQueryPickResponse(pickRequest);
				if (rs != null && rs.pickID!=null) {
					rs.robotID = pickRequest.robotID;
					eventBus.deliver(rs);
				//	logger.info("--> Table Creator got a pickID="+rs.pickID+", pickPoint= " + rs.pickPoint);
					logger.info("Creator  sent to Queryer QueryPickResponse, robotID= "+rs.robotID+", pickID= " + rs.pickID+", pickPoint="+rs.pickPoint);
					
			/*		PickingPointUpdateNotice notice = new PickingPointUpdateNotice();   // for interaction with SensiNact
					notice.pickID = rs.pickID;
					notice.isAssigned = true;
					eventBus.deliver(notice);
					logger.info("Creator  sent to SensiNact PickingPointUpdateNotice " + notice);  */ 
				}
			});
		} else if (event instanceof QueryStorageTable) {
			QueryStorageTable storageRequest = (QueryStorageTable) event;
			worker.execute(() -> {
				QueryStorageResponse resp = getQueryStorageResponse(storageRequest);
				if (resp != null) {
					resp.robotID = storageRequest.robotID;
					logger.info("Creator  sent to Queryer QueryStorageResponse,  robotID= "+resp.robotID+", markerID= " 
					+ resp.markerID+", hasNewPoint="+resp.hasNewPoint+", storageAuxPoint="+resp.storageAuxliaryPoint+", storagePoint="+resp.storagePoint);
					eventBus.deliver(resp);
				}
			});

		} else if (event instanceof QueryDockTable) {
			QueryDockTable dockRequest = (QueryDockTable) event;
			worker.execute(() -> {
				QueryDockResponse resp = getQueryDockResponse(dockRequest);
				if (resp != null) {
					resp.robotID = dockRequest.robotID;
					logger.info("Creator  sent to Queryer QueryDockResponse,  robotID= "+resp.robotID+", robotIP= " 
							+ resp.robotIP+", hasNewPoint="+resp.hasNewPoint+", dockAuxPoint="+resp.dockAuxliaryPoint+", dockPoint="+resp.dockingPoint);
					eventBus.deliver(resp);
				}
			});
	// -----------------------------------   used for Queryer  end  ------------------------------------------------
			
		} else if (event instanceof UnsignPickingPoint) {  // it's sent when iteration is done or no cart found at picking point
			UnsignPickingPoint updateRequest = (UnsignPickingPoint) event;
			worker.execute(() -> {
				UnsignPickingPointResponse rs = unsignPickingPoint(updateRequest);
				if (rs != null) {
					rs.robotID = updateRequest.robotID;
					eventBus.deliver(rs);
					logger.info("Creator  sent  to Queryer UnsignPickingPointResponse, pickID=" + rs.pickID+", updateStatus="+rs.updateStatus);
					
			/*		PickingPointUpdateNotice notice = new PickingPointUpdateNotice();  // for interaction with SensiNact
					notice.pickID = rs.pickID;
					notice.isAssigned = false;
					eventBus.deliver(notice);
					logger.info("Creator  sent to SensiNact PickingPointUpdateNotice " + notice);*/
				}
			});
		} else if (event instanceof GetPickingTable) {  // print picking table by Queryer
			GetPickingTable get = (GetPickingTable) event;
			worker.execute(() -> {
				PickingTableValues rs = getPickingTable(get);
				if (rs != null) {
					rs.robotID = get.robotID;
					logger.info("Creator  sent to Queryer PickingTableValues ");
					eventBus.deliver(rs);
				}
			});
			
	// -----------------------------------   SensiNact  start  ------------------------------------------------	
		} else if (event instanceof UpdatePickPoint) {
			UpdatePickPoint up = (UpdatePickPoint) event;
			worker.execute(() -> {
				logger.info("--> Table Creator got a SensiNact UpdatePickPoint, pickID= " + up.pickID+", pickPoint= "+up.pickPoint+", isAssigned="+up.isAssigned);
				UpdateResponse rs = new UpdateResponse();
				if (updatePickTableRow(up)) {
					rs.updateStatus = UpdateStatus.OK;
				} else {
					rs.updateStatus = UpdateStatus.ERROR;
				}
				logger.info("Creator sent Picking table Row UpdateResponse = " + rs.updateStatus);
				eventBus.deliver(rs);
			});
		} else if (event instanceof UpdateCartStorage) {
			UpdateCartStorage up = (UpdateCartStorage) event;
			worker.execute(() -> {
				logger.info("--> Table Creator got a SensiNact UpdateCartStorage, cartID= " + up.cartID+", storageID= "+up.storageID);
				UpdateResponse rs = new UpdateResponse();
				if (updateCartStorageRow(up)) {
					rs.updateStatus = UpdateStatus.OK;
				} else {
					rs.updateStatus = UpdateStatus.ERROR;
				}
				logger.info("Creator sent Cart Row UpdateResponse = " + rs.updateStatus);
				eventBus.deliver(rs);
			});
		}  else if (event instanceof UpdateStoragePoint) {
			UpdateStoragePoint up = (UpdateStoragePoint) event;
			worker.execute(() -> {
				logger.info("--> Table Creator got a SensiNact UpdateStoragePoint, storageID = " + up.storageID+", storageAUX= "+up.storageAUX+", storagePoint= "+up.storagePoint);
				UpdateResponse rs = new UpdateResponse();
				if (updateStorageTableRow(up)) {
					rs.updateStatus = UpdateStatus.OK;
				} else {
					rs.updateStatus = UpdateStatus.ERROR;
				}
				logger.info("Creator sent storage table Row UpdateResponse = " + rs.updateStatus);
				eventBus.deliver(rs);
			});
		} else if (event instanceof UpdateDockPoint) {
			UpdateDockPoint up = (UpdateDockPoint) event;
			worker.execute(() -> {
				logger.info("--> Table Creator got a SensiNact UpdateDockPoint, robotIP = " + up.robotIP+", dockAUX= "+up.dockAUX+", dockPoint= "+up.dockPoint);
				UpdateResponse rs = new UpdateResponse();
				if (updateDockTableRow(up)) {
					rs.updateStatus = UpdateStatus.OK;
				} else {
					rs.updateStatus = UpdateStatus.ERROR;
				}
				logger.info("Creator sent Docking table Row UpdateResponse = " + rs.updateStatus);
				eventBus.deliver(rs);
			});
		}
		// -----------------------------------   SensiNact  end  ------------------------------------------------	
	}
	

	// -----------------------------------   SensiNact  start  ------------------------------------------------
	private boolean updatePickTableRow(UpdatePickPoint pp) {

		StringBuilder value = new StringBuilder();
		value.append("'" + pp.pickID + "', '" + pp.pickPoint.trim() + "', '" + pp.isAssigned + "'");
		int flag = 0;
		try {
			synchronized (this) {
				ResultSet rs = stmt.executeQuery("SELECT * FROM PickingTable WHERE PPid=" + pp.pickID);
				while (rs.next()) {
					stmt.executeUpdate("UPDATE PickingTable SET PPid='" + pp.pickID + "', pose='" + pp.pickPoint.trim()+ "', isAssigned='" + pp.isAssigned + "'");
					flag = 1;
					break;
				}
				if (flag == 0) {
					stmt.executeUpdate("INSERT INTO PickingTable VALUES(" + value.toString() + ")");
				}
				printPickingTable();
			}
		} catch (Exception e) {
			logger.error("\n Creator update PickingTable row Exception: {}", ExceptionUtils.getStackTrace(e));
			value = null;
			return false;
		}
		value = null;
		return true;
	}
		
	private boolean updateCartStorageRow(UpdateCartStorage pp) {

		StringBuilder value = new StringBuilder();
		value.append("'" + pp.cartID + "', '" + pp.storageID.trim() + "'");
		int flag = 0;
		try {
			synchronized (this) {
				ResultSet rs = stmt.executeQuery("SELECT * FROM CartTable WHERE cartID=" + pp.cartID);
				while (rs.next()) {

					stmt.executeUpdate("UPDATE CartTable SET cartID='" + pp.cartID + "', storageID='" + pp.storageID.trim() + "'");
					flag = 1;
					break;
				}
				if (flag == 0) {
					stmt.executeUpdate("INSERT INTO CartTable VALUES(" + value.toString() + ")");
				}
			}
			printCartTable();

		} catch (Exception e) {
			logger.error("\n Creator update CartTable row Exception: {}", ExceptionUtils.getStackTrace(e));
			value = null;
			return false;
		}
		value = null;
		return true;
	}
		
	private boolean updateStorageTableRow(UpdateStoragePoint pp) {

		StringBuilder value = new StringBuilder();
		value.append("'" + pp.storageID.trim() + "', '" + pp.storageAUX.trim() + "', '" + pp.storagePoint.trim() + "'");
		int flag = 0;
		try {
			synchronized (this) {
				ResultSet rs = stmt.executeQuery("SELECT * FROM StorageTable WHERE STid=" + pp.storageID.trim());
				while (rs.next()) {

					stmt.executeUpdate("UPDATE StorageTable SET STid='" + pp.storageID.trim() + "', storageAUX='"+ pp.storageAUX.trim() + "', storagePose='" + pp.storagePoint.trim() + "'");
					flag = 1;
					break;
				}
				if (flag == 0) {
					stmt.executeUpdate("INSERT INTO StorageTable VALUES(" + value.toString() + ")");
				}
			}
			printStorageTable();
		} catch (Exception e) {
			logger.error("\n Creator update StorageTable row Exception: {}", ExceptionUtils.getStackTrace(e));
			value = null;
			return false;
		}
		value = null;
		return true;
	}
		
	private boolean updateDockTableRow(UpdateDockPoint pp) {

		StringBuilder value = new StringBuilder();
		value.append("'" + pp.robotIP.trim() + "', '" + pp.dockAUX.trim() + "', '" + pp.dockPoint.trim() + "'");
		int flag = 0;
		try {
			synchronized (this) {
				ResultSet rs = stmt.executeQuery("SELECT * FROM DockTable WHERE IPid=" + pp.robotIP.trim());
				while (rs.next()) {

					stmt.executeUpdate("UPDATE DockTable SET IPid='" + pp.robotIP.trim() + "', dockAUX='"+ pp.dockAUX.trim() + "', dockPose='" + pp.dockPoint.trim() + "'");
					flag = 1;
					break;
				}
				if (flag == 0) {
					stmt.executeUpdate("INSERT INTO DockTable VALUES(" + value.toString() + ")");
				}
			}
			printDockTable();
		} catch (Exception e) {
			logger.error("\n Creator update DockTable row Exception: {}", ExceptionUtils.getStackTrace(e));
			value = null;
			return false;
		}
		value = null;
		return true;
	}
		// -----------------------------------   SensiNact  end  ------------------------------------------------
		
		
		// -----------------------------------   used for Queryer   start  ------------------------------------------------
		
	private QueryPickResponse getQueryPickResponse(QueryPickingTable pickRequest) {

		QueryPickResponse pickReponse = new QueryPickResponse();
		try {
			synchronized (this) {
				ResultSet rs = stmt.executeQuery("SELECT * FROM PickingTable WHERE isAssigned=false");

				while (rs.next()) {
					pickReponse.pickID = rs.getString("PPid");
					pickReponse.pickPoint = rs.getString("pose");
					logger.info("--> Table Creator got a pickID="+pickReponse.pickID+", pickPoint= " + pickReponse.pickPoint);
					stmt.executeUpdate("UPDATE PickingTable SET isAssigned='" + true + "' WHERE PPid='"+ rs.getString("PPid") + "'");
					break;
				}
				
				if(pickReponse.pickID == null) {
					logger.info("==============> NO PICK POINT Found by Creator  for RB=  "+pickRequest.robotID);
				}

				logger.info("------------Creator Assigns  PickingTable ----------------");
				System.out.println("------------Creator Assigns  PickingTable ----------------");

				rs = stmt.executeQuery("SELECT * FROM PickingTable");
				while (rs.next()) {
					logger.info(rs.getString("PPid") + ", " + rs.getString("pose") + ", " + rs.getString("isAssigned"));
					System.out.println(rs.getString("PPid") + ", " + rs.getString("pose") + ", " + rs.getString("isAssigned"));
				}
			}

		} catch (Exception e) {
			logger.error("\n Creator Exception: {}", ExceptionUtils.getStackTrace(e));
			return null;
		}
		return pickReponse;
	}

		private  QueryStorageResponse getQueryStorageResponse(QueryStorageTable storageRequest) {

			int markerID = storageRequest.markerID;
			String storageID = null;

			QueryStorageResponse storageReponse = new QueryStorageResponse();
			storageReponse.markerID = markerID;
			
			try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM CartTable WHERE cartID=" + markerID);

				while (rs.next()) {
					storageID = rs.getString("storageID");
					break;
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
				
			} catch (Exception e) {
				logger.error("\nCreator Exception:{}", ExceptionUtils.getStackTrace(e));
				return null;
			}

			return storageReponse;
		}
		
		private QueryDockResponse getQueryDockResponse(QueryDockTable dockingRequest) {

			QueryDockResponse dockReponse = new QueryDockResponse();
			dockReponse.robotIP = dockingRequest.robotIP;
			
			try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM DockTable WHERE IPid = '"+ dockingRequest.robotIP+"'");

				while (rs.next()) {

					dockReponse.hasNewPoint = true;
					dockReponse.dockAuxliaryPoint = rs.getString("dockAUX");
					dockReponse.dockingPoint = rs.getString("dockPose");
					break;
				}
			} catch (Exception e) {
				logger.error("\nCreator Exception: {}", ExceptionUtils.getStackTrace(e));
				return null;
			}

			return dockReponse;
		}
		// -----------------------------------   used for Queryer   end  ------------------------------------------------		
		
		
	 private  UnsignPickingPointResponse unsignPickingPoint(UnsignPickingPoint update) {  //  update picking point True-->False
		 
		 UnsignPickingPointResponse resp = new UnsignPickingPointResponse();
			resp.robotID = update.robotID;

			Coordinate targetPoint = getCoordinate(update.pickPoint);
			Coordinate pickPose = null;
			try {
				synchronized(this) {
				ResultSet rs = stmt.executeQuery("SELECT * FROM PickingTable WHERE isAssigned=true");

				while (rs.next()) {
					pickPose = getCoordinate(rs.getString("pose"));
					
					if(pickPose.getX() == targetPoint.getX() && pickPose.getY() == targetPoint.getY() && pickPose.getZ() == targetPoint.getZ()) {
						resp.pickID = rs.getString("PPid");
				//		stmt.executeUpdate(  // TODO 3, to be used in real robot
				//				"UPDATE PickingTable SET isAssigned='" + false + "' WHERE PPid='" + rs.getString("PPid").trim() + "'");
						pickPose = null;
						break;
					}
					pickPose = null;
				}
			
				logger.info("------------Creator un-assign  PickingTable ----------------");
				System.out.println("------------Creator un-assign  PickingTable ----------------");

				rs = stmt.executeQuery("SELECT * FROM PickingTable");

				while (rs.next()) {
			       logger.info(rs.getString("PPid") + ", " + rs.getString("pose")+ ", " + rs.getString("isAssigned"));
			       System.out.println(rs.getString("PPid") + ", " + rs.getString("pose")+ ", " + rs.getString("isAssigned"));
				}
				}
			  
				logger.info("Table Creator is sending UnsignPickingPointResponse with updateStatus = "+resp.updateStatus);
				
			} catch (Exception e) {
				logger.error("\nCreator Exception: {}", ExceptionUtils.getStackTrace(e));
				return null;
			}
			return resp;
	 }
	 
	private PickingTableValues getPickingTable(GetPickingTable get) {     // print picking table
		PickingTableValues table = new PickingTableValues();
		table.robotID = get.robotID;
		try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM PickingTable");
			
			StringBuilder builder = new StringBuilder();
				while (rs.next()) {
					logger.info("Creator's PickingTable: " + rs.getString("PPid") + ", " + rs.getString("pose") + ", "+ rs.getString("isAssigned"));
					builder.append(rs.getString("PPid") + ", " + rs.getString("pose") + ", " + rs.getString("isAssigned")+"\n");
				}
			if(builder.length()!=0) {
				table.pickingTableValues = builder.toString();
			}

		} catch (Exception e) {
			logger.error("\nCreator print PickingTable Exception: {}", ExceptionUtils.getStackTrace(e));
		}
		
		return table;
	}
		
	// -----------------------------------   init tables  ------------------------------------------------  
	
	public void initPickingTable(Statement stmt) {
		try {
			stmt.execute("DROP TABLE IF EXISTS PickingTable");

			stmt.execute(
					"CREATE TABLE PickingTable(PPid VARCHAR(10) PRIMARY KEY, pose VARCHAR(30), isAssigned BOOLEAN)");

			if (jsonDataReader != null) {
				PickingTable pickingTable = jsonDataReader.pickingTable;

				List<PickingPoint> pickingPoints = pickingTable.getPickingPoints();

				StringBuilder value = new StringBuilder();

				for (PickingPoint pickingPoint : pickingPoints) {
					if (value == null) {
						value = new StringBuilder();
					}
					value.append("'" + pickingPoint.getPPid() + "', '" + serializePose(pickingPoint.getPose()) + "', '"
							+ pickingPoint.getIsAssigned() + "'");

					stmt.executeUpdate("INSERT INTO PickingTable VALUES(" + value.toString() + ")");
					value = null;
				}

				printPickingTable();
			}
		} catch (Exception e) {
			logger.error("\nCreator create PickingTable Exception: {}", ExceptionUtils.getStackTrace(e));
		}

	}
	  
	  public void initStorageTable(Statement stmt) {
		  try {
		  stmt.execute("DROP TABLE IF EXISTS StorageTable");

		  stmt.execute("CREATE TABLE StorageTable(STid VARCHAR(10) PRIMARY KEY, storageAUX VARCHAR(30), storagePose VARCHAR(30))");

		  if(jsonDataReader != null) {
		  StorageTable storageTable = jsonDataReader.storageTable;
		  
		  List<StoragePoint> storagePoints = storageTable.getStoragePoints();
		  
		  StringBuilder value = new StringBuilder();
		  
		  for(StoragePoint storagePoint : storagePoints) {
			  if(value == null) {
				  value = new StringBuilder();
			  }
			  
			  value.append("'"+storagePoint.getSTid()+"', '"+serializeStorageAUX(storagePoint.getStorageAUX())+"', '"+serializeStoragePose(storagePoint.getStoragePose())+"'");
			  
			  stmt.executeUpdate("INSERT INTO StorageTable VALUES("+value.toString()+")");
			  value = null;
		  }
		  printStorageTable();
		  }
	  } catch (Exception e) {
		  logger.error("\nCreator create StorageTable Exception: {}", ExceptionUtils.getStackTrace(e));
		}
	  }

	public void initCartTable(Statement stmt) {
		try {
			stmt.execute("DROP TABLE IF EXISTS CartTable");

			stmt.execute("CREATE TABLE CartTable(cartID INT PRIMARY KEY, storageID VARCHAR(10))");
			if (jsonDataReader != null) {
				CartTable cartTable = jsonDataReader.cartTable;

				List<CartStorage> cartStorages = cartTable.getCartStorages();

				StringBuilder value = new StringBuilder();

				for (CartStorage cartStorage : cartStorages) {
					if (value == null) {
						value = new StringBuilder();
					}

					value.append("'" + cartStorage.getCartID() + "', '" + cartStorage.getStorageID() + "'");

					stmt.executeUpdate("INSERT INTO CartTable VALUES(" + value.toString() + ")");
					value = null;
				}
				printCartTable();
			}
		} catch (Exception e) {
			logger.error("\nCreator create CartTable Exception: {}", ExceptionUtils.getStackTrace(e));
		}
	}
	  
	public void initDockTable(Statement stmt) {
		try {
			stmt.execute("DROP TABLE IF EXISTS DockTable");

			stmt.execute("CREATE TABLE DockTable(IPid VARCHAR(20) PRIMARY KEY, dockAUX VARCHAR(30), dockPose VARCHAR(30))");
			if (jsonDataReader != null) {
				DockTable dockTable = jsonDataReader.dockTable;

				List<DockingPoint> dockingPoints = dockTable.getDockingPoints();

				StringBuilder value = new StringBuilder();

				for (DockingPoint dockingPoint : dockingPoints) {
					if (value == null) {
						value = new StringBuilder();
					}

					value.append("'" + dockingPoint.getIPid() + "', '" + serializeDockAUX(dockingPoint.getDockAUX())
							+ "', '" + serializeDockPose(dockingPoint.getDockPose()) + "'");

					stmt.executeUpdate("INSERT INTO DockTable VALUES(" + value.toString() + ")");
					value = null;
				}
				printDockTable();
			}
		} catch (Exception e) {
			logger.error("\nCreator create DockTable Exception: {}", ExceptionUtils.getStackTrace(e));
		}
	}
	  
	private void printPickingTable() {
		try {
			logger.info("------------  PickingTable ----------------");
			System.out.println("------------  PickingTable ----------------");
			
			ResultSet rs = stmt.executeQuery("SELECT * FROM PickingTable");

			while (rs.next()) {
				logger.info(rs.getString("PPid") + ", " + rs.getString("pose") + ", " + rs.getString("isAssigned"));
				System.out.println(rs.getString("PPid") + ", " + rs.getString("pose") + ", " + rs.getString("isAssigned"));
			}
		} catch (Exception e) {
			logger.error("\nCreator print PickingTable Exception: {}", ExceptionUtils.getStackTrace(e));
		}
	}
	
	private void printStorageTable() {
		try {
			logger.info("------------  StorageTable ----------------");
			System.out.println("------------  StorageTable ----------------");
			
			  ResultSet rs = stmt.executeQuery("SELECT * FROM StorageTable");

			  while (rs.next()) {
				  logger.info(rs.getString("STid") + ", " + rs.getString("storageAUX")+ ", " + rs.getString("storagePose"));
				  System.out.println(rs.getString("STid") + ", " + rs.getString("storageAUX")+ ", " + rs.getString("storagePose"));
			  }
		} catch (Exception e) {
			logger.error("\nCreator print StorageTable Exception: {}", ExceptionUtils.getStackTrace(e));
		}
	}
	
	private void printCartTable() {
		try {
			  logger.info("------------  CartTable ----------------");
			  System.out.println("------------  CartTable ----------------");
			  
			  ResultSet rs = stmt.executeQuery("SELECT * FROM CartTable");

			  while (rs.next()) {
				  logger.info(rs.getString("cartID") + ", " + rs.getString("storageID"));
				  System.out.println(rs.getString("cartID") + ", " + rs.getString("storageID"));
			  }
		} catch (Exception e) {
			logger.error("\nCreator print CartTable Exception: {}", ExceptionUtils.getStackTrace(e));
		}
	}
	
	private void printDockTable() {
		try {
			logger.info("------------  DockTable ----------------");
			System.out.println("------------  DockTable ----------------");
			
			  ResultSet rs = stmt.executeQuery("SELECT * FROM DockTable");

			  while (rs.next()) {
				  logger.info(rs.getString("IPid") + ", " + rs.getString("dockAUX")+ ", " + rs.getString("dockPose"));
				  System.out.println(rs.getString("IPid") + ", " + rs.getString("dockAUX")+ ", " + rs.getString("dockPose"));
			  }
		} catch (Exception e) {
			logger.error("\nCreator print DockTable Exception: {}", ExceptionUtils.getStackTrace(e));
		}
	}
	  
		private Coordinate getCoordinate(String crd) {

			Coordinate cord = new Coordinate();

			String[] strs = crd.trim().split(",");
			cord.setX(new Double(strs[0]).doubleValue());
			cord.setY(new Double(strs[1]).doubleValue());
			cord.setZ(new Double(strs[2]).doubleValue());

			return cord;
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
	
	@Deactivate
	void deactivate() {
		try {
			if (stmt != null && !stmt.isClosed()) {
				stmt.close();
			}
			if (conn != null && !conn.isClosed()) {
				conn.close();
			}
		} catch (SQLException e) {
			logger.error("\nCreator Exception: {}", ExceptionUtils.getStackTrace(e));
		}
		logger.info("------------  Table Creator is deactivated----------------");
	}
	
}
