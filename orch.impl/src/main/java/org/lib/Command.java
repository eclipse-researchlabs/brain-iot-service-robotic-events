package org.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.paremus.brain.iot.example.orch.impl.ComponentImpl;



public class Command {

	private ComponentImpl component;
	


	public Command(ComponentImpl component) {
		super();
		this.component = component;
	}

	ArrayList<ArrayList<String>> Robots =new ArrayList<ArrayList<String>> ();
	
	void addOperation(int RobotId, String Operation){
		
		if(Robots.size()==0){
			for(int i=0; i< 4; i++){
				Robots.add(new ArrayList<String> ());
			}
		}
		else{
			Robots.get(RobotId).add(Operation);
		}
	}
	
	void removeOperation(int RobotId, String Operation){
		
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
	
	public void main(String[] a) 
    { 
		System.out.println(read());


    } 
	
	public Integer read(){
		return null;
		

	}
	
	public Integer readOperation(){
		return null;
		

	}
	
	
	public void writePosition(){
		
		System.out.println("ROBOT : SEND POSITION ");
		

	}
	
	public void writeMarker(){
		
		System.out.println("ROBOT : SEND MARKER IN SIGHT ");
		
	}
	
	public void writeAvailability( ){
		
		System.out.println("ROBOT : SEND AVAILABILITY ");
		
		
	}
	
	public void writeGotoAdd( ){
		
		System.out.println("ROBOT : SEND GOTO ADD ");
	
		
	}
	
	public void writeGotoCancel( ){
		
		System.out.println("ROBOT : SEND GOTO CANCEL ");

		
	}
	
	public void writeGotoQueryState( ){
		
		System.out.println("ROBOT : SEND GOTO QUERY STATE ");

		
	}
	
	public void writePickAdd( ){
		
		System.out.println("ROBOT : SEND PICK ADD ");

		
	}
	
	public void writePickCancel( ){
		
		System.out.println("ROBOT : SEND PICK CANCEL ");

		
	}
	
	public void writePickQueryState( ){
		
		System.out.println("ROBOT : SEND PICK QUERY STATE ");

		
	}
	

	public void writePlaceAdd( ){
		
		System.out.println("ROBOT : SEND PLACE ADD ");

		
	}
	
	public void writePlaceCancel( ){
		
		System.out.println("ROBOT : SEND PLCAE CANCEL ");

		
	}
	
	public void writePlaceQueryState( ){
		
		System.out.println("ROBOT : SEND PLACE QUERY STATE ");

		
	}

	

	public void writeChargeAdd( ){
		
		System.out.println("ROBOT : SEND CHARGE ADD ");

		
	}
	
	public void writeChargeCancel( ){
		
		System.out.println("ROBOT : SEND CHARGE CANCEL ");
	
		
	}
	
	public void writeChargeQueryState( ){
		
		System.out.println("ROBOT : SEND CHARGE QUERY STATE ");
	
		
	}
	
	public void writeUnChargeAdd (){
		System.out.println("ROBOT : SEND RETURN UNCHARGE ADD ");

	}
	public void writeUnChargeCancel(){
		System.out.println("ROBOT : SEND RETURN UNCHARGE CANCEL ");

	}
	
	public  int checkMarkers(int RobotId, int obj){
		return component.checkMarkers(RobotId,obj);
	}

	
	public void printState(int RobotId, int mission ){
		
		if(mission==1){
		System.out.println("ROBOT : ROBOT ("+RobotId+") MISSION IS IN STATE FINISHED" );
		}else{
		System.out.println("ROBOT : ROBOT ("+RobotId+") MISSION IS IN STATE QUEUED RUNNING PAUSED UNKNOWN" );
		}
	}	
	
	public void writeUnChargeQueryState(){
		System.out.println("ROBOT : SEND UNCHARGE QUERY STATE ");

	}
	public void  writeOpenDoor(String doorId){
		component.writeOpenDoor(doorId);
	}
	public void  writeCloseDoor(String doorId){
		component.writeCloseDoor(doorId);
	}

	
	public void writeGOTO(int RobotId, int mission){
		component.writegoto(RobotId, mission);
	}

	public void placeCART(int RobotId, int cart){
		component.placeCART(RobotId, cart);
		
	}
	public int cancel(int RobotId, int mission){
		return component.cancel(RobotId, mission);
	}
	public  int queryState(int RobotId, int mission){
		return component.queryState(RobotId, mission);
	}
	
	double DOCKINGAX;double DOCKINGAY;	double DOCKINGBX;double DOCKINGBY;
	double theta;
	
	double UNLOADBX;	double UNLOADBY;		double UNLOADAX;	double UNLOADAY;	
	
	double  STORAGEAX=8;	double  STORAGEAY=-3.6;	double  STORAGEBX=6.427;	double  STORAGEBY=-5.627; double  STORAGECX=6.489;	double  STORAGECY=-7.801;	
	
	
	double  FRONTDOORX=5.429;	double  FRONTDOORY=-0.102;	
	
	public void pickCart(int RobotId, int cart){
		component.pickCart(RobotId, cart);
	}
	

	
	
	public void printPosition (int RobotId, int place){
		
		
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
	
	
	public String getResponse(String request) {
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
	
	public <T> T getObject(String txt, Class<T> c){
		 ObjectMapper mapper = new ObjectMapper();
		 T cmd = null;
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
	
	public String getJson(Object c){	

	  
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
