package org.lib.request.gotolocation;

import java.io.IOException;
import java.util.ArrayList;

import org.lib.request.Request;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Procedure {

	public static void main(String[] a) 
    { 
  

		Procedure org = new Procedure(); 
  
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
	
	ArrayList<Goal> goals =new ArrayList<Goal>();
	
	ArrayList<Velocity> max_velocities =new ArrayList<Velocity>();

	public ArrayList<Goal> getGoals() {
		return goals;
	}

	public void setGoals(ArrayList<Goal> goals) {
		this.goals = goals;
	}
	public void addGoal(Goal goal){
		goals.add(goal);
	}

	public ArrayList<Velocity> getMax_velocities() {
		return max_velocities;
	}
	public void addVelovity(Velocity velocity){
		max_velocities.add(velocity);
	}
	public void setMax_velocities(ArrayList<Velocity> max_velocities) {
		this.max_velocities = max_velocities;
	}	
	
	
	
	
}
