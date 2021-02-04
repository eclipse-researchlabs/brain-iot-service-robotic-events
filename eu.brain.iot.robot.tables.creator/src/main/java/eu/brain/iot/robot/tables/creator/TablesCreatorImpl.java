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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.BrainIoTEvent;
import eu.brain.iot.eventing.api.SmartBehaviour;
import eu.brain.iot.robot.tables.creator.api.TableCreator;
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
		configurationPolicy = ConfigurationPolicy.REQUIRE, 
		service = {TableCreator.class}
)
@SmartBehaviourDefinition(consumed = {}, 
		author = "LINKS", name = "Warehouse Module: Tables Creator", 
		description = "Implements Four Shared  Tables."
)
public class TablesCreatorImpl implements SmartBehaviour<BrainIoTEvent>, TableCreator {
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
				String resourcesPath() default "/home/rui/resources/"; // "/opt/fabric/resources/"; /home/rui/resources
			}

			private ServiceRegistration<?> reg;

		private  Logger logger;
		  
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

			logger.info("table creator resourcesPath = "+resourcesPath);

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

		/*		stmt.close(); // TODO don't close it if it's a referenced osgi service
				conn.close();
				stmt = null; // TODO don't close it if it's a referenced osgi service
				conn = null;
				*/
				logger.info("Table Creator finished to create "+resourcesPath+"tables.mv.db..........");

			} catch (ClassNotFoundException e) {
				logger.error("\n Exception:", e);
				
			} catch (SQLException e) {
				if(stmt != null && !stmt.isClosed()) {
					stmt.close();
				}
				if(conn != null && !conn.isClosed()) {
					conn.close();
				}
				logger.error("\n Exception:", e);
			}
		} catch (Exception e) {
			logger.error("\n Exception:", e);
		}
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
		  }
		  
	  }
	  } catch (Exception e) {
			logger.error("\n Exception:", e);
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
		  }
		  }
	  } catch (Exception e) {
			logger.error("\n Exception:", e);
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
			logger.error("\n Exception:", e);
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
				logger.error("\n Exception:", e);
			}
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

	@Override
	public void notify(BrainIoTEvent event) {

		
	}


	@Override
	public synchronized ResultSet executeQuery(String sql) {

		ResultSet rs = null;
		try {
			rs = stmt.executeQuery(sql);
		} catch (Exception e) {
			logger.error("\n Exception:", e);
		}
		return rs;
	}
	
	@Override
	public synchronized int executeUpdate(String sql) {

		int i = 0;
		try {
			i = stmt.executeUpdate(sql);
		} catch (SQLException e) {
			logger.error("\n Exception:", e);
		}
		return i;
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
			logger.error("\n Exception:", e);
		}
		logger.info("------------  Table Creator is deactivated----------------");
	}
	
}
