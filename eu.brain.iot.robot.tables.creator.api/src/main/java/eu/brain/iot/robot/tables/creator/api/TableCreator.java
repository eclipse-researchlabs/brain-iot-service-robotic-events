package eu.brain.iot.robot.tables.creator.api;


import java.sql.Connection;
import java.sql.Statement;

import org.osgi.annotation.versioning.ProviderType;

//@ProviderType
public interface TableCreator {
    
	public Connection getConn();

	public Statement getStmt();
}
