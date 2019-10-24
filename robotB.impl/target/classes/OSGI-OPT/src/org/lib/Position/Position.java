package org.lib.Position;

import java.io.IOException;

import org.lib.availability.Availability;
import org.lib.availability.Pose_s;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Position {

	
	public static String writePosition()
    { 
  

		Position org = new Position(); 
  
	 ObjectMapper mapper = new ObjectMapper();
      
		try {
			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

			
			//Convert object to JSON string
			String 			jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(org);
			return jsonInString;
			
			
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "NULL";

    } 
	
	String node = "-1";
	boolean reliable = true;
	Pose_s pose = new Pose_s();
	
	String type;
	
	String state;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public boolean isReliable() {
		return reliable;
	}
	public void setReliable(boolean reliable) {
		this.reliable = reliable;
	}
	public Pose_s getPose() {
		return pose;
	}
	public void setPose(Pose_s pose) {
		this.pose = pose;
	}
	@Override
	public String toString() {
		return "Position [node=" + node + ", reliable=" + reliable + ", pose="
				+ pose.toString() + "]";
	} 
	
	
	
	
}
