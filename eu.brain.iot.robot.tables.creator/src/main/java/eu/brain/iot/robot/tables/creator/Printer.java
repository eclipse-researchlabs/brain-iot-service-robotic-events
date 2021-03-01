package eu.brain.iot.robot.tables.creator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.brain.iot.robot.tables.creator.TablesCreatorImpl;

public class Printer {
	
	private  Logger logger = (Logger) LoggerFactory.getLogger(TablesCreatorImpl.class.getSimpleName());
	
	private static final String USER = "RosEdgeNode";

	  private static final String PASSWORD = "123";

	  private static final String DRIVER_CLASS="org.h2.Driver";
	  
	  private Connection conn;
	  private Statement stmt;
	  
	public static void main(String[] args) {
		Printer p = new Printer();
		
		p.printPickingTable();
		p.printCartTable();
		p.printStorageTable();
		p.printDockTable();
		
		p.close();
	}
	
	public void close() {
		try {
			if(stmt != null && !stmt.isClosed()) {
				stmt.close();
			}
			if(conn != null && !conn.isClosed()) {
				conn.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public Printer() {
		  
		  try {
				//	jsonDataReader = new JsonDataReader(resourcesPath);
				
				try {
					final String JDBC_URL = "jdbc:h2:/opt/fabric/resources/tables;DB_CLOSE_DELAY=-1";

					
					Class.forName(DRIVER_CLASS);

					conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
					stmt = conn.createStatement();

				
					
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
	
		  private void printPickingTable() {
				try {
				//	logger.info("------------  PickingTable ----------------");
					System.out.println("------------  PickingTable ----------------");
					
					ResultSet rs = stmt.executeQuery("SELECT * FROM PickingTable");

					while (rs.next()) {
				//		logger.info(rs.getString("PPid") + ", " + rs.getString("pose") + ", " + rs.getString("isAssigned"));
						System.out.println(rs.getString("PPid") + ", " + rs.getString("pose") + ", " + rs.getString("isAssigned"));
					}
				} catch (Exception e) {
					logger.error("\nCreator print PickingTable Exception: {}", ExceptionUtils.getStackTrace(e));
				}
			}
			
			private void printStorageTable() {
				try {
				//	logger.info("------------  StorageTable ----------------");
					System.out.println("------------  StorageTable ----------------");
					
					  ResultSet rs = stmt.executeQuery("SELECT * FROM StorageTable");

					  while (rs.next()) {
				//		  logger.info(rs.getString("STid") + ", " + rs.getString("storageAUX")+ ", " + rs.getString("storagePose"));
						  System.out.println(rs.getString("STid") + ", " + rs.getString("storageAUX")+ ", " + rs.getString("storagePose"));
					  }
				} catch (Exception e) {
					logger.error("\nCreator print StorageTable Exception: {}", ExceptionUtils.getStackTrace(e));
				}
			}
			
			private void printCartTable() {
				try {
				//	  logger.info("------------  CartTable ----------------");
					  System.out.println("------------  CartTable ----------------");
					  
					  ResultSet rs = stmt.executeQuery("SELECT * FROM CartTable");

					  while (rs.next()) {
				//		  logger.info(rs.getString("cartID") + ", " + rs.getString("storageID"));
						  System.out.println(rs.getString("cartID") + ", " + rs.getString("storageID"));
					  }
				} catch (Exception e) {
					logger.error("\nCreator print CartTable Exception: {}", ExceptionUtils.getStackTrace(e));
				}
			}
			
			private void printDockTable() {
				try {
			//		logger.info("------------  DockTable ----------------");
					System.out.println("------------  DockTable ----------------");
					
					  ResultSet rs = stmt.executeQuery("SELECT * FROM DockTable");

					  while (rs.next()) {
				//		  logger.info(rs.getString("IPid") + ", " + rs.getString("dockAUX")+ ", " + rs.getString("dockPose"));
						  System.out.println(rs.getString("IPid") + ", " + rs.getString("dockAUX")+ ", " + rs.getString("dockPose"));
					  }
				} catch (Exception e) {
					logger.error("\nCreator print DockTable Exception: {}", ExceptionUtils.getStackTrace(e));
				}
			}

	

}
