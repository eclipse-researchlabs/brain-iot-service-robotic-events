package eu.brain.iot.robot.tables.creator.api;

public class QueryStorageResponse extends TableEvent{

	public int markerID;  // cart marker ID, is used to identify the Place position.
	
	public boolean hasNewPoint = false;
	
	public String storagePoint;

	public String storageAuxliaryPoint;
}
