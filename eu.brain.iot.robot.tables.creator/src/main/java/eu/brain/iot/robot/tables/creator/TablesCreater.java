package eu.brain.iot.robot.tables.creator;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.BrainIoTEvent;
import eu.brain.iot.eventing.api.SmartBehaviour;
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
		configurationPid = "eu.brain.iot.robot.tables.creator.TablesCreater", 
		configurationPolicy = ConfigurationPolicy.REQUIRE, 
		service = {SmartBehaviour.class, TablesCreater.class}
)
@SmartBehaviourDefinition(consumed = {}, 
		author = "LINKS", name = "Warehouse Module: Tables Creator", 
		description = "Implements Four Shared Tables in H2 DB."
)
public class TablesCreater implements SmartBehaviour<BrainIoTEvent> {
	//Define the connection of database 
	  private static final String JDBC_URL = "jdbc:h2:./tables;DB_CLOSE_DELAY=-1";//"./tables":DB locaiton;"DB_CLOSE_DELAY=-1":allow single connection 

	  private static final String USER = "RosEdgeNode";

	  private static final String PASSWORD = "123";

	  private static final String DRIVER_CLASS="org.h2.Driver";
	  
	  private String jsonFilePath;
	  
	  private Connection conn;
	private Statement stmt;
	  private JsonDataReader jsonDataReader;
	  
	  @ObjectClassDefinition
		public static @interface Config {

			@AttributeDefinition(description = "The identifier for the robot behaviour")
			String jsonFilePath();

		}

		private Config config;
		private ServiceRegistration<?> reg;
	  
	@Activate
	public void init(BundleContext context, Config config, Map<String, Object> props) throws SQLException {
		this.jsonFilePath = config.jsonFilePath();
		
		if(jsonFilePath != null && jsonFilePath.length()>0) {
			if(!jsonFilePath.endsWith(File.separator)) {
				jsonFilePath+=File.separator;
			}
			jsonDataReader = new JsonDataReader(jsonFilePath);
		}
		
		System.out.println("jsonFilePath = "+jsonFilePath);
		
		try {
			
			
			Class.forName(DRIVER_CLASS);

			conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
			stmt = conn.createStatement();

			initPickingTable(stmt);
			initStorageTable(stmt);
			initCartTable(stmt);
			initDockTable(stmt);

		//	stmt.close();
		//	conn.close();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			
		} catch (SQLException e) {
			if(stmt != null && !stmt.isClosed()) {
				stmt.close();
			}
			if(conn != null && !conn.isClosed()) {
				conn.close();
			}
			e.printStackTrace();
		}
	}
	  
	  public void initPickingTable(Statement stmt) throws SQLException {
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
		  System.out.println("------------  PickingTable ----------------");

		  ResultSet rs = stmt.executeQuery("SELECT * FROM PickingTable");

		  while (rs.next()) {
		       System.out.println(rs.getString("PPid") + ", " + rs.getString("pose")+ ", " + rs.getString("isAssigned"));
		  }
		  
	  }
		  
	  }
	  
	  public void initStorageTable(Statement stmt) throws SQLException {
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
		  System.out.println("------------  StorageTable ----------------");

		  ResultSet rs = stmt.executeQuery("SELECT * FROM StorageTable");

		  while (rs.next()) {
		       System.out.println(rs.getString("STid") + ", " + rs.getString("storageAUX")+ ", " + rs.getString("storagePose"));
		  }
		  }
	  }

	  public void initCartTable(Statement stmt) throws SQLException {
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
		  System.out.println("------------  CartTable ----------------");

		  ResultSet rs = stmt.executeQuery("SELECT * FROM CartTable");

		  while (rs.next()) {
		       System.out.println(rs.getString("cartID") + ", " + rs.getString("storageID"));
		  }
		  }
	  }
	  
	  public void initDockTable(Statement stmt) throws SQLException {
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
		  System.out.println("------------  DockTable ----------------");

		  ResultSet rs = stmt.executeQuery("SELECT * FROM DockTable");

		  while (rs.next()) {
		       System.out.println(rs.getString("IPid") + ", " + rs.getString("dockAUX")+ ", " + rs.getString("dockPose"));
		  }
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
	
	public Connection getConn() {
		return conn;
	}

	public Statement getStmt() {
		return stmt;
	}
	
}
