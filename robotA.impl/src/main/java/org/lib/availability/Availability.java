package org.lib.availability;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;













public class Availability {


	public static void writeAvailability()
	    { 
	  

		 Availability org = new Availability(); 
	  
		 ObjectMapper mapper = new ObjectMapper();
	      
			try {
				mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

				
				//Convert object to JSON string
				String 			jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(org);
				System.out.println(jsonInString);
				
				
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
	    } 
	
	 String operation_state ="idle";
	
	 String robot_state ="standby";	
	 
	 Navigation_status navigation_status = new Navigation_status();
	
	 Localization_status localization_status = new Localization_status();


	 Robot_status robot_status =new Robot_status();
	 
	 Pose_s pose = new Pose_s(); 
	 
	 Elevator elevator =new Elevator();
	 
	 Velocity velocity =new Velocity();
	 
	 ArrayList<Sensor>  sonsors =new ArrayList<Sensor> ();

	 String robot_id="";
	 
	 Mission_status mission_status = new  Mission_status();

	 String control_state = "auto";

	public String getOperation_state() {
		return operation_state;
	}

	public void setOperation_state(String operation_state) {
		this.operation_state = operation_state;
	}

	public String getRobot_state() {
		return robot_state;
	}

	public void setRobot_state(String robot_state) {
		this.robot_state = robot_state;
	}

	public Navigation_status getNavigation_status() {
		return navigation_status;
	}

	public void setNavigation_status(Navigation_status navigation_status) {
		this.navigation_status = navigation_status;
	}

	public Localization_status getLocalization_status() {
		return localization_status;
	}

	public void setLocalization_status(Localization_status localization_status) {
		this.localization_status = localization_status;
	}

	public Robot_status getRobot_status() {
		return robot_status;
	}

	public void setRobot_status(Robot_status robot_status) {
		this.robot_status = robot_status;
	}

	public Pose_s getPose() {
		return pose;
	}

	public void setPose(Pose_s pose) {
		this.pose = pose;
	}

	public Elevator getElevator() {
		return elevator;
	}

	public void setElevator(Elevator elevator) {
		this.elevator = elevator;
	}

	public Velocity getVelocity() {
		return velocity;
	}

	public void setVelocity(Velocity velocity) {
		this.velocity = velocity;
	}

	public ArrayList<Sensor> getSonsors() {
		Sensor s =new Sensor();
		ArrayList<Sensor> st =new ArrayList<Sensor>();
		st.add(s);
		return st;
	}

	public void setSonsors(ArrayList<Sensor> sonsors) {
		this.sonsors = sonsors;
	}

	public String getRobot_id() {
		return robot_id;
	}

	public void setRobot_id(String robot_id) {
		this.robot_id = robot_id;
	}

	public Mission_status getMission_status() {
		return mission_status;
	}

	public void setMission_status(Mission_status mission_status) {
		this.mission_status = mission_status;
	}

	public String getControl_state() {
		return control_state;
	}

	public void setControl_state(String control_state) {
		this.control_state = control_state;
	}
	 

	
}
