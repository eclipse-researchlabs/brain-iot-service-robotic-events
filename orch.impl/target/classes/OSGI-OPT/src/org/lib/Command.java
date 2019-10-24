package org.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.paremus.brain.iot.example.orch.impl.ComponentImpl;



public class Command {

	static String IP="10.8.0.6";
	static String PORT="8080";
	


	static ArrayList<ArrayList<String>> Robots =new ArrayList<ArrayList<String>> ();
	
	static void addOperation(int RobotId, String Operation){
		
		if(Robots.size()==0){
			for(int i=0; i< 4; i++){
				Robots.add(new ArrayList<String> ());
			}
		}
		else{
			Robots.get(RobotId).add(Operation);
		}
	}
	
	static void removeOperation(int RobotId, String Operation){
		
		if(Robots.size()==0){
			for(int i=0; i< 4; i++){
				Robots.add(new ArrayList<String> ());
			}
		}
		else{
			for(int i=0; i< Robots.get(RobotId).size(); i++){
				if (Robots.get(RobotId).get(i).compareTo(Operation)==0){
					Robots.get(RobotId).remove(i); i=5;
				}
			}
		}
	}
	
	public static void main(String[] a) 
    { 
		System.out.println(read());


    } 
	
	public static Integer read(){
		return null;
		

	}
	
	public static Integer readOperation(){
		return null;
		

	}
	
	
	public static void writePosition(){
		
		System.out.println("ROBOT : SEND POSITION ");
		

	}
	
	public static void writeMarker(){
		
		System.out.println("ROBOT : SEND MARKER IN SIGHT ");
		
	}
	
	public static void writeAvailability( ){
		
		System.out.println("ROBOT : SEND AVAILABILITY ");
		
		
	}
	
	public static void writeGotoAdd( ){
		
		System.out.println("ROBOT : SEND GOTO ADD ");
	
		
	}
	
	public static void writeGotoCancel( ){
		
		System.out.println("ROBOT : SEND GOTO CANCEL ");

		
	}
	
	public static void writeGotoQueryState( ){
		
		System.out.println("ROBOT : SEND GOTO QUERY STATE ");

		
	}
	
	public static void writePickAdd( ){
		
		System.out.println("ROBOT : SEND PICK ADD ");

		
	}
	
	public static void writePickCancel( ){
		
		System.out.println("ROBOT : SEND PICK CANCEL ");

		
	}
	
	public static void writePickQueryState( ){
		
		System.out.println("ROBOT : SEND PICK QUERY STATE ");

		
	}
	

	public static void writePlaceAdd( ){
		
		System.out.println("ROBOT : SEND PLACE ADD ");

		
	}
	
	public static void writePlaceCancel( ){
		
		System.out.println("ROBOT : SEND PLCAE CANCEL ");

		
	}
	
	public static void writePlaceQueryState( ){
		
		System.out.println("ROBOT : SEND PLACE QUERY STATE ");

		
	}

	

	public static void writeChargeAdd( ){
		
		System.out.println("ROBOT : SEND CHARGE ADD ");

		
	}
	
	public static void writeChargeCancel( ){
		
		System.out.println("ROBOT : SEND CHARGE CANCEL ");
	
		
	}
	
	public static void writeChargeQueryState( ){
		
		System.out.println("ROBOT : SEND CHARGE QUERY STATE ");
	
		
	}
	
	public static void writeUnChargeAdd (){
		System.out.println("ROBOT : SEND RETURN UNCHARGE ADD ");

	}
	public static void writeUnChargeCancel(){
		System.out.println("ROBOT : SEND RETURN UNCHARGE CANCEL ");

	}
	
	public  int checkMarkers(int RobotId, int obj){
		if(RobotId==1) {
			return ComponentImpl.checkMarkers(RobotId,obj);
		}
		if(RobotId==2) {
			return ComponentImpl.checkMarkersb(RobotId,obj);
		}
		if(RobotId==3) {
			return ComponentImpl.checkMarkersc(RobotId,obj);
		}
		
		return -1;
	}

	
	public static void printState(int RobotId, int mission ){
		
		if(mission==1){
		System.out.println("ROBOT : ROBOT ("+RobotId+") MISSION IS IN STATE FINISHED" );
		}else{
		System.out.println("ROBOT : ROBOT ("+RobotId+") MISSION IS IN STATE QUEUED RUNNING PAUSED UNKNOWN" );
		}
	}	
	
	public static void writeUnChargeQueryState(){
		System.out.println("ROBOT : SEND UNCHARGE QUERY STATE ");

	}
	public static void  writeOpenDoor(){
		ComponentImpl.writeOpenDoor();
	}
	public static void  writeCloseDoor(){
		ComponentImpl.writeCloseDoor();
	}

	
	public static void writeGOTO(int RobotId, int mission){
		ComponentImpl.writegoto(RobotId, mission);
	}

	public static void placeCART(int RobotId, int cart){
		ComponentImpl.placeCART(RobotId, cart);
		
	}
	public static int cancel(int RobotId, int mission){
		return ComponentImpl.cancel(RobotId, mission);
	}
	public  int queryState(int RobotId, int mission){
		int i=0;
		if(RobotId==1) {
			return ComponentImpl.queryState(RobotId, mission);
		}
		
		if(RobotId==2) {
			return ComponentImpl.queryStateb(RobotId, mission);
		}

		if(RobotId==3) {
			return ComponentImpl.queryStatec(RobotId, mission);
		}

		return -1;
	}
	


	static double DOCKINGAX;static double DOCKINGAY;	static double DOCKINGBX;static double DOCKINGBY;
	static double theta;
	
	static double UNLOADBX;	static double UNLOADBY;		static double UNLOADAX;	static double UNLOADAY;	
	
	static double  STORAGEAX=8;	static double  STORAGEAY=-3.6;	static double  STORAGEBX=6.427;	static double  STORAGEBY=-5.627; static double  STORAGECX=6.489;	static double  STORAGECY=-7.801;	
	
	
	static double  FRONTDOORX=5.429;	static double  FRONTDOORY=-0.102;	
	
	public static void pickCart(int RobotId, int cart){
		ComponentImpl.pickCart(RobotId, cart);
	}
	

	
	
	public static void printPosition (int RobotId, int place){
		
		
		if (place== 0){//Docking
			
		System.out.println("ORCHESTRATOR : ROBOT is in DOCKING PLACE");	
			
		}
		if (place== 1){//FRONTdoor
			
		System.out.println("ORCHESTRATOR : ROBOT is in FRONT OF THE DOOR");				
			
		}
		if (place== 2){//UNLOAD
			
		System.out.println("ORCHESTRATOR : ROBOT is in UNLOAD PLACE");					
			
		}
		if (place== 3){//CART
			
	    System.out.println("ORCHESTRATOR : ROBOT is in STORAGE PLACE");					
			
		}	
		//send commands
		//wait response
		//getresponse
		
		
		
	}
	
	
	public static String getResponse(String request) {
		String jsonObject ="";
		
		// Step2: Now pass JSON File Data to REST Service
		try {
			URL url = new URL(request);
			URLConnection connection = url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			//OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			//out.write(jsonObject.toString());
			//out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			jsonObject="";
			String str="";
			while ((str=in.readLine()) != null) {
				jsonObject+=str;
			}
			System.out.println("\nRobot REST Service Invoked Successfully..");
			in.close();
		} catch (Exception e) {
			System.out.println("\nError while calling ROBOT REST Service");
			System.out.println(e);
		}
		
		return jsonObject;

	}
	
	private static String postResponse(String request, String jsonObjectin) {
		String jsonObject ="";
		
		// Step2: Now pass JSON File Data to REST Service
		try {
			URL url = new URL(request);
			URLConnection connection = url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			out.write(jsonObjectin.toString());
			out.close();

			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			jsonObject="";
			String str="";
			while ((str=in.readLine()) != null) {
				jsonObject+=str;
			}
			System.out.println("\nRobot REST Service Invoked Successfully..");
			in.close();
		} catch (Exception e) {
			System.out.println("\nError while calling ROBOT REST Service");
			System.out.println(e);
		}
		
		return jsonObject;
	}
	
	public static Object getObject(String txt, Class c){
		 ObjectMapper mapper = new ObjectMapper();
		 Object cmd = null;
			try {

				 cmd = mapper.readValue(txt, c);
				return cmd;

			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return cmd;
	}
	
	public static String getJson(Object c){	

	  
		ObjectMapper mapper = new ObjectMapper();
  
	try {
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		
		//Convert object to JSON string
		String 			jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(c);
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
	
	
	
	
}
