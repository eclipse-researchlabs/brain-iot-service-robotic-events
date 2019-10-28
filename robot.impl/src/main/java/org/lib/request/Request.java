package org.lib.request;

import java.io.IOException;

import org.lib.availability.Availability;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Request {
	
	
	
	@Override
	public String toString() {
		return "Request [Type=" + Type + "]";
	}

	public static void main(String[] a) 
    { 
  

		Request org = new Request(); 
  
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
	
	String Type="position";

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}
	
	public static Request getCommand(String jsonInString){
		 ObjectMapper mapper = new ObjectMapper();
	
	try {

		Request cmd = mapper.readValue(jsonInString, Request.class);
		return cmd;

	} catch (JsonGenerationException e) {
		e.printStackTrace();
	} catch (JsonMappingException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	return null;

}
	
	
	public  int getIntType(){
		if(Type.compareTo("position")==0){
			return 0;
		}
		if(Type.compareTo("availability")==0){
			return 1;
		}
		if(Type.compareTo("marker")==0){
			return 2;
		}
		if(Type.compareTo("goto")==0){
			return 3;
		}
		if(Type.compareTo("gotoadd")==0){
			return 4;
		}
		if(Type.compareTo("gotocancel")==0){
			return 5;
		}
		if(Type.compareTo("gotoquerystate")==0){
			return 6;
		}
		
		if(Type.compareTo("pick")==0){
			return 7;
		}
		if(Type.compareTo("pickadd")==0){
			return 8;
		}
		if(Type.compareTo("pickcancel")==0){
			return 9;
		}
		if(Type.compareTo("pickquerystate")==0){
			return 10;
		}
		
		
		if(Type.compareTo("place")==0){
			return 11;
		}
		if(Type.compareTo("placeadd")==0){
			return 12;
		}
		if(Type.compareTo("placecancel")==0){
			return 13;
		}
		if(Type.compareTo("placequerystate")==0){
			return 14;
		}

		
		if(Type.compareTo("charge")==0){
			return 15;
		}
		if(Type.compareTo("chargeadd")==0){
			return 16;
		}
		if(Type.compareTo("chargecancel")==0){
			return 17;
		}
		if(Type.compareTo("chargequerystate")==0){
			return 18;
		}
		if(Type.compareTo("uncharge")==0){
			return 19;
		}
		if(Type.compareTo("unchargeadd")==0){
			return 20;
		}
		if(Type.compareTo("unchargecancel")==0){
			return 21;
		}
		if(Type.compareTo("unchargequerystate")==0){
			return 22;
		}
		
		
		return -1;
		
	}

	
}
