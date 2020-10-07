package eu.brain.iot.robot.events;

public class DoorFound extends RobotCommand{
	
	/* 
	 * marker ID contained in Door QR Code scanned by robot camera.
	 * this event will be sent automatically by the ROS Edge Node if it detects a closed Door
	 * on the way to Storage area. When robot stops in front of the Door, the GoTo action has been finished,
	 * but not yet reached the target Storage position,
	 * ROS Edge Node will check the marker ID of the object which has been seen by camera,
	 * then create this event and report the Door is found
	 * 
	 * Robot Behaviour can send the 'DoorStatus' command to the Door and receives 'DoorStatusResponse' event, same as the ones defined for M18 Review. 
	  after getting response from Door,  Robot Behaviour can send GoTo again to the storage point
	 */
	public int doorMarkerID;
}
