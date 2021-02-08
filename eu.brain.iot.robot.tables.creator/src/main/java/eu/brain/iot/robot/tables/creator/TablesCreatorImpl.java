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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.BrainIoTEvent;
import eu.brain.iot.eventing.api.EventBus;
import eu.brain.iot.eventing.api.SmartBehaviour;
import eu.brain.iot.robot.tables.creator.api.Coordinate;
import eu.brain.iot.robot.tables.creator.api.GetPickingTable;
import eu.brain.iot.robot.tables.creator.api.PickingTableValues;
import eu.brain.iot.robot.tables.creator.api.QueryDockResponse;
import eu.brain.iot.robot.tables.creator.api.QueryDockTable;
import eu.brain.iot.robot.tables.creator.api.QueryPickResponse;
import eu.brain.iot.robot.tables.creator.api.QueryPickingTable;
import eu.brain.iot.robot.tables.creator.api.QueryStorageResponse;
import eu.brain.iot.robot.tables.creator.api.QueryStorageTable;
import eu.brain.iot.robot.tables.creator.api.TableCreator;
import eu.brain.iot.robot.tables.creator.api.TableUpdatedResponse;
import eu.brain.iot.robot.tables.creator.api.UpdatePickingTable;
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


@Component(
		immediate=true,
		configurationPid = "eu.brain.iot.robot.tables.creator.TablesCreator", 
		configurationPolicy = ConfigurationPolicy.OPTIONAL, 
		service = {SmartBehaviour.class}
)
@SmartBehaviourDefinition(consumed = {QueryPickingTable.class, QueryStorageTable.class, QueryDockTable.class, UpdatePickingTable.class, GetPickingTable.class }, 
		author = "LINKS", name = "Warehouse Module: Tables Creator", 
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
				
				Dictionary<String, Object> serviceProps = new Hashtable<>(props.entrySet().stream()
						.filter(e -> !e.getKey().startsWith(".")).collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
				
				serviceProps.put(SmartBehaviourDefinition.PREFIX_ + "filter",  // get all events
						String.format("(robotID=*)"));
				
				reg = context.registerService(SmartBehaviour.class, this, serviceProps);
				
			} catch (ClassNotFoundException e) {
				logger.error("\nCreator Exception: {}", e.toString());
				
			} catch (SQLException e) {
				if(stmt != null && !stmt.isClosed()) {
					stmt.close();
				}
				if(conn != null && !conn.isClosed()) {
					conn.close();
				}
				logger.error("\nCreator Exception: {}", e.toString());
			}
		} catch (Exception e) {
			logger.error("\nCreator Exception: {}", e.toString());
		}
			
		}

		
		@Override
		public void notify(BrainIoTEvent event) {

			logger.info("--> Table Creator received an event "+event.getClass());
			
			if (event instanceof QueryPickingTable) {
				QueryPickingTable pickRequest = (QueryPickingTable) event;
				worker.execute(() -> {
				QueryPickResponse rs = getQueryPickResponse(pickRequest);
				rs.robotID = pickRequest.robotID;
				if(rs !=null) {
					logger.info("Creator  sent QueryPickResponse "+ rs);
					eventBus.deliver(rs);
				}
				});
			} else if (event instanceof QueryStorageTable) {
				QueryStorageTable storageRequest = (QueryStorageTable) event;
				worker.execute(() -> {
					QueryStorageResponse resp = getQueryStorageResponse(storageRequest);
					resp.robotID = storageRequest.robotID;
					if(resp!=null) {
						logger.info("Creator  sent QueryStorageResponse "+ resp);
						eventBus.deliver(resp);
					}
				});
				
			} else if (event instanceof QueryDockTable) {
				QueryDockTable dockRequest = (QueryDockTable) event;
				worker.execute(() -> {
					QueryDockResponse resp = getQueryDockResponse(dockRequest);
					resp.robotID = dockRequest.robotID;
					if(resp!=null) {
						logger.info("Creator  sent QueryDockResponse "+ resp);
						eventBus.deliver(resp);
					}
				});
				
			} else if (event instanceof UpdatePickingTable) {
				UpdatePickingTable updateRequest = (UpdatePickingTable) event;
				worker.execute(() -> {
				TableUpdatedResponse rs = updatePickingTable(updateRequest);
				rs.robotID = updateRequest.robotID;
				if(rs !=null) {
					logger.info("Creator  sent TableUpdatedResponse "+ rs);
					eventBus.deliver(rs);
				}
			});
			} else if (event instanceof GetPickingTable) {
				GetPickingTable get = (GetPickingTable) event;
				worker.execute(() -> {
					PickingTableValues rs = getPickingTable(get);
					rs.robotID = get.robotID;
				if(rs !=null) {
					logger.info("Creator  sent PickingTableValues ");
					eventBus.deliver(rs);
				}
			});
			}
		}
		
		private synchronized QueryPickResponse getQueryPickResponse(QueryPickingTable pickRequest) {

			QueryPickResponse pickReponse = new QueryPickResponse();
			try {

			ResultSet rs = stmt.executeQuery("SELECT * FROM PickingTable WHERE isAssigned=false");

				while (rs.next()) {

					pickReponse.pickPoint = rs.getString("pose");
					logger.info("--> Table Creator got a pickPoint "+pickReponse.pickPoint);
					stmt.executeUpdate(
							"UPDATE PickingTable SET isAssigned='" + true + "' WHERE PPid='" + rs.getString("PPid") + "'");
					break;
				}
		
			logger.info("------------Creator:  PickingTable ----------------");
			  rs = stmt.executeQuery("SELECT * FROM PickingTable");
			  while (rs.next()) {
				  logger.info(rs.getString("PPid") + ", " + rs.getString("pose")+ ", " + rs.getString("isAssigned"));
			  }
				  
			} catch (Exception e) {
				logger.error("\n Creator Exception: {}", e.toString());
				return null;
			}
			return pickReponse;
		}

		private synchronized QueryStorageResponse getQueryStorageResponse(QueryStorageTable storageRequest) {

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
				logger.error("\nCreator Exception:{}", e.toString());
				return null;
			}

			return storageReponse;
		}
		
		private synchronized QueryDockResponse getQueryDockResponse(QueryDockTable dockingRequest) {

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
				logger.error("\nCreator Exception: {}", e.toString());
				return null;
			}

			return dockReponse;
		}
		
		
	 private synchronized TableUpdatedResponse updatePickingTable(UpdatePickingTable update) {
		 
		 TableUpdatedResponse resp = new TableUpdatedResponse();
			resp.robotID = resp.robotID;

			Coordinate targetPoint = getCoordinate(update.pickPoint);
			Coordinate pickPose = null;
			try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM PickingTable WHERE isAssigned=true");

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
			
			logger.info("------------Creator:  PickingTable ----------------");

			  rs = stmt.executeQuery("SELECT * FROM PickingTable");

			  while (rs.next()) {
			       logger.info(rs.getString("PPid") + ", " + rs.getString("pose")+ ", " + rs.getString("isAssigned"));
			  }
			  
			  logger.info("Table Creator is sending TableUpdatedResponse with updateStatus = "+resp.updateStatus);
				
			} catch (Exception e) {
				logger.error("\nCreator Exception: {}", e.toString());
				return null;
			}
			return resp;
	 }
	 
	private PickingTableValues getPickingTable(GetPickingTable get) {
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
			logger.error("\nCreator get PickingTable Exception: {}", e.toString());
		}
		
		return table;
	}
		
	  
	  public void initPickingTable(Statement stmt) {
		  try {
		  stmt.execute("DROP TABLE IF EXISTS PickingTable");

		  stmt.execute("CREATE TABLE PickingTable(PPid VARCHAR(10) PRIMARY KEY, pose VARCHAR(30), isAssigned BOOLEAN)");
		  
		  if(jsonDataReader != null) {
		  PickingTable pickingTable = jsonDataReader.pickingTable;
		  
		  List<PickingPoint> pickingPoints = pickingTable.getPickingPoints();
		  
		  StringBuilder value = new StringBuilder();
		  
		  for(PickingPoint pickingPoint : pickingPoints) {
			  if(value == null) {
				  value = new StringBuilder();
			  }
			  
			  value.append("'"+pickingPoint.getPPid()+"', '"+serializePose(pickingPoint.getPose())+"', '"+pickingPoint.getIsAssigned()+"'");
			  
			  stmt.executeUpdate("INSERT INTO PickingTable VALUES("+value.toString()+")");
			  value = null;
		  }
		  logger.info("------------  PickingTable ----------------");

		  ResultSet rs = stmt.executeQuery("SELECT * FROM PickingTable");

		  while (rs.next()) {
			  logger.info(rs.getString("PPid") + ", " + rs.getString("pose")+ ", " + rs.getString("isAssigned"));
			  System.out.println(rs.getString("PPid") + ", " + rs.getString("pose")+ ", " + rs.getString("isAssigned"));
		  }
		  
	  }
	  } catch (Exception e) {
		  logger.error("\nCreator Exception: {}", e.toString());
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
		  logger.info("------------  StorageTable ----------------");

		  ResultSet rs = stmt.executeQuery("SELECT * FROM StorageTable");

		  while (rs.next()) {
			  logger.info(rs.getString("STid") + ", " + rs.getString("storageAUX")+ ", " + rs.getString("storagePose"));
			  System.out.println(rs.getString("STid") + ", " + rs.getString("storageAUX")+ ", " + rs.getString("storagePose"));
		  }
		  }
	  } catch (Exception e) {
		  logger.error("\nCreator Exception: {}", e.toString());
		}
	  }

	  public void initCartTable(Statement stmt) {
		  try {
		  stmt.execute("DROP TABLE IF EXISTS CartTable");

		  stmt.execute("CREATE TABLE CartTable(cartID INT PRIMARY KEY, storageID VARCHAR(10))");
		  if(jsonDataReader != null) {
		  CartTable cartTable = jsonDataReader.cartTable;
		  
		  List<CartStorage> cartStorages = cartTable.getCartStorages();
		  
		  StringBuilder value = new StringBuilder();
		  
		  for(CartStorage cartStorage : cartStorages) {
			  if(value == null) {
				  value = new StringBuilder();
			  }
			  
			  value.append("'"+cartStorage.getCartID()+"', '"+cartStorage.getStorageID()+"'");
			  
			  stmt.executeUpdate("INSERT INTO CartTable VALUES("+value.toString()+")");
			  value = null;
		  }
		  logger.info("------------  CartTable ----------------");

		  ResultSet rs = stmt.executeQuery("SELECT * FROM CartTable");

		  while (rs.next()) {
			  logger.info(rs.getString("cartID") + ", " + rs.getString("storageID"));
		  }
		  }
	  } catch (Exception e) {
		  logger.error("\nCreator Exception: {}", e.toString());
		}
	  }
	  
	  public void initDockTable(Statement stmt) {
		  try {
		  stmt.execute("DROP TABLE IF EXISTS DockTable");

		  stmt.execute("CREATE TABLE DockTable(IPid VARCHAR(20) PRIMARY KEY, dockAUX VARCHAR(30), dockPose VARCHAR(30))");
		  if(jsonDataReader != null) {
		  DockTable dockTable = jsonDataReader.dockTable;
		  
		  List<DockingPoint> dockingPoints = dockTable.getDockingPoints();
		  
		  StringBuilder value = new StringBuilder();
		  
		  for(DockingPoint dockingPoint : dockingPoints) {
			  if(value == null) {
				  value = new StringBuilder();
			  }
			  
			  value.append("'"+dockingPoint.getIPid()+"', '"+serializeDockAUX(dockingPoint.getDockAUX())+"', '"+serializeDockPose(dockingPoint.getDockPose())+"'");
			  
			  stmt.executeUpdate("INSERT INTO DockTable VALUES("+value.toString()+")");
			  value = null;
		  }
		  logger.info("------------  DockTable ----------------");

		  ResultSet rs = stmt.executeQuery("SELECT * FROM DockTable");

		  while (rs.next()) {
			  logger.info(rs.getString("IPid") + ", " + rs.getString("dockAUX")+ ", " + rs.getString("dockPose"));
		  }
		  }
		  } catch (Exception e) {
			  logger.error("\nCreator Exception: {}", e.toString());
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
			logger.error("\nCreator Exception: {}", e.toString());
		}
		logger.info("------------  Table Creator is deactivated----------------");
	}
	
}
