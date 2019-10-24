package com.paremus.brain.iot.example.robotA.impl;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.paremus.brain.iot.example.orch.api.Cancel;
import com.paremus.brain.iot.example.orch.api.CheckMarker;
import com.paremus.brain.iot.example.orch.api.DoorClose;
import com.paremus.brain.iot.example.orch.api.DoorOpen;
import com.paremus.brain.iot.example.orch.api.InetRobot;
import com.paremus.brain.iot.example.orch.api.PickCart;
import com.paremus.brain.iot.example.orch.api.PlaceCART;
import com.paremus.brain.iot.example.orch.api.QueryState;
import com.paremus.brain.iot.example.orch.api.writeGOTO;
import com.paremus.brain.iot.example.robotA.api.CheckValueReturn;
import com.paremus.brain.iot.example.robotA.api.QueryStateValueReturn;

import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.BrainIoTEvent;
import eu.brain.iot.eventing.api.EventBus;
import eu.brain.iot.eventing.api.SmartBehaviour;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.apache.felix.service.command.CommandProcessor;
import org.lib.availability.Header;
import org.lib.availability.Robot_Pose;
import org.lib.availability.Stamp;
import org.lib.markersinsight.Markers_in_sight;
import org.lib.reply.gotolocation.AddReply;
import org.lib.request.gotolocation.Goal;
import org.lib.request.gotolocation.GotToAdd;
import org.lib.request.gotolocation.Procedure;
import org.lib.request.gotolocation.Velocity;
import org.lib.request.gotolocation.querystate.GotoQueryState;
import org.lib.request.pickcomponent.PickAdd;
import org.lib.request.pickcomponent.querystate.PickQueryState;
import org.lib.request.placecomponent.PlaceAdd;
import org.lib.request.placecomponent.querystate.PlaceQueryState;

@Component(
		property = {
				CommandProcessor.COMMAND_SCOPE + "=ROBOT", //
				CommandProcessor.COMMAND_FUNCTION + "=robot" //
		},service={SmartBehaviour.class, ComponentImpl.class}
	)

@SmartBehaviourDefinition(consumed = {writeGOTO.class,Cancel.class, PickCart.class,PlaceCART.class,QueryState.class,CheckMarker.class, InetRobot.class },    
author = "UGA", name = "Smart RobotA",
description = "Implements a remote Smart RobotA.")
public class ComponentImpl implements SmartBehaviour<BrainIoTEvent>{
    
	static String IP ;
	static String PORT ;
	
	public static writeGOTO wgoto ;
	public static Cancel cancel;
	public static PickCart pickcart;
	public static PlaceCART plcacart;
	public static QueryState querySate;
	public static CheckMarker checkmaker;
	public static InetRobot inetrobot;
	
    public static CheckValueReturn checkreturnedvalue;
    
    public static QueryStateValueReturn queryreturnedvalue;
    
	public void notify(BrainIoTEvent event) {
	
		if (event instanceof writeGOTO) {
			wgoto = (writeGOTO) event;
			synchronized (this) {
				
				writeGOTO(1, wgoto.mission);

			}
			
		}else {
			if (event instanceof Cancel) {
				cancel = (Cancel) event;
				synchronized (this) {
					
					cancel(1, cancel.mission);

				}
				
			}else {
				if (event instanceof PickCart) {
					pickcart = (PickCart) event;
					synchronized (this) {
						
						pickCart(1, pickcart.cart);

					}
					
				}else {
					if (event instanceof PlaceCART) {
						plcacart = (PlaceCART) event;
						synchronized (this) {
							
							placeCART(1, plcacart.cart);

						}
						
					}else {
						if (event instanceof QueryState) {
							querySate = (QueryState) event;
							synchronized (this) {
								if(querySate.robotid==1) {
									queryreturnedvalue =new QueryStateValueReturn();
									queryreturnedvalue.value =queryState(1, querySate.mission);
									eventBus.deliver(queryreturnedvalue);
									
									System.out.println("--- QueryState ---");				
								}

								

							}
							
						}else {
							if (event instanceof CheckMarker) {
								checkmaker = (CheckMarker) event;
								synchronized (this) {
									if(checkmaker.robotid==1) {
										checkreturnedvalue =new CheckValueReturn ();
										checkreturnedvalue.value=checkMarkers(1,5);
										eventBus.deliver(checkreturnedvalue);									
										System.out.println("--- CheckMarker ---");	
									}

									
								}
								
							}else {
								
								
								
								if (event instanceof InetRobot) {
									inetrobot = (InetRobot) event;
									synchronized (this) {
										IP= inetrobot.ip;
										PORT=inetrobot.port;
										
									}
									
								}else {
									
								
								
								System.out.println("Argh! Received an unknown event type " + event.getClass());
								
								}
								
								
							}
							
							
						}
						
						
					}
				}
			}
		}
		
	
		
	}
    public void robot() {
	
    }
    
    @Reference
    private  EventBus eventBus;
 
	public static void writeGOTO(int RobotId, int mission){
		
		if(mission==4){//GOTO STORAGE
			Robot_Pose rb=null;
				String request = null;
				if(RobotId==1){
					request = "http://"+IP+":"+PORT+"/rb1_base_a/robot_local_control/NavigationComponent/GoToComponent/add";
				 rb= new Robot_Pose(-3.6,8,-3.14);
				}
				if(RobotId==2){
					request = "http://"+IP+":"+PORT+"/rb1_base_b/robot_local_control/NavigationComponent/GoToComponent/add";
			    rb= new Robot_Pose(-5.5,8,-3.14); 
				}
				if(RobotId==3){
					request = "http://"+IP+":"+PORT+"/rb1_base_c/robot_local_control/NavigationComponent/GoToComponent/add";
			    rb= new Robot_Pose(-7.75,8,-3.14); 
				}


				Procedure p =new Procedure();
				Goal g = new Goal ();
				Header header = new Header();
				header.setSeq(0);
				header.setFrame_id("map");
				header.setStamp(new Stamp(0.0,0.0));
				g.setHeader(header);
				g.setPose(rb);
				p.addGoal(g);
				Velocity v =new Velocity(1.0,1.0,0.0);
				p.addVelovity(v);
				GotToAdd gotoadd =new GotToAdd();
				gotoadd.setProcedure(p);
				System.out.println(getJson(gotoadd));
				String jsonObject = postResponse(request, getJson(gotoadd));
				System.out.println(jsonObject);

			
		}

		if(mission==5){//GOTO UNLOAD

			Robot_Pose rb=null;			
				String request = null;
				if(RobotId==1){
					request = "http://"+IP+":"+PORT+"/rb1_base_a/robot_local_control/NavigationComponent/GoToComponent/add";
					 rb= new Robot_Pose(-3.6,8,-3.14);	
				}
				
				if(RobotId==2){
					request = "http://"+IP+":"+PORT+"/rb1_base_b/robot_local_control/NavigationComponent/GoToComponent/add"; 
				    rb= new Robot_Pose(-5.5,8,-3.14); 	
				}
				if(RobotId==3){
					request = "http://"+IP+":"+PORT+"/rb1_base_c/robot_local_control/NavigationComponent/GoToComponent/add"; 
				    rb= new Robot_Pose(-7.75,8,-3.14); 	
				}				

				Procedure p =new Procedure();
				Goal g = new Goal ();
				Header header = new Header();
				header.setSeq(0);
				header.setFrame_id("map");
				header.setStamp(new Stamp(0.0,0.0));
				g.setHeader(header);
				g.setPose(rb);
				p.addGoal(g);
				Velocity v =new Velocity(1.0,1.0,0.0);
				p.addVelovity(v);
				GotToAdd gotoadd =new GotToAdd();
				gotoadd.setProcedure(p);
				System.out.println(getJson(gotoadd));
				String jsonObject = postResponse(request, getJson(gotoadd));
				System.out.println(jsonObject);


		}
		
			
			if(mission==1){//PLACE CENTER

					
					String request = null;
					if(RobotId==1){
						request = "http://"+IP+":"+PORT+"/rb1_base_a/robot_local_control/NavigationComponent/GoToComponent/add"; }
					if(RobotId==2){
						request = "http://"+IP+":"+PORT+"/rb1_base_b/robot_local_control/NavigationComponent/GoToComponent/add"; }
					if(RobotId==3){
						request = "http://"+IP+":"+PORT+"/rb1_base_c/robot_local_control/NavigationComponent/GoToComponent/add"; }
			


					Procedure p =new Procedure();
					Goal g = new Goal ();
					Header header = new Header();
					header.setSeq(0);
					header.setFrame_id("map");
					header.setStamp(new Stamp(0.0,0.0));
					g.setHeader(header);
					g.setPose(new Robot_Pose(0,0,-3.14));
					p.addGoal(g);
					Velocity v =new Velocity(1.0,1.0,0.0);
					p.addVelovity(v);
					GotToAdd gotoadd =new GotToAdd();
					gotoadd.setProcedure(p);
					System.out.println(getJson(gotoadd));
					String jsonObject = postResponse(request, getJson(gotoadd));
					System.out.println(jsonObject);


			}
			if(mission==2){//PLACE LEFT

					String request = null;
					if(RobotId==1){
						request = "http://"+IP+":"+PORT+"/rb1_base_a/robot_local_control/NavigationComponent/GoToComponent/add"; }
					if(RobotId==2){
						request = "http://"+IP+":"+PORT+"/rb1_base_b/robot_local_control/NavigationComponent/GoToComponent/add"; }
					if(RobotId==3){
						request = "http://"+IP+":"+PORT+"/rb1_base_c/robot_local_control/NavigationComponent/GoToComponent/add"; }
				
					
					Procedure p =new Procedure();
					Goal g = new Goal ();
					Header header = new Header();
					header.setSeq(0);
					header.setFrame_id("map");
					header.setStamp(new Stamp(0.0,0.0));
					g.setHeader(header);
					g.setPose(new Robot_Pose(-7.75,0,-3.14));
					p.addGoal(g);
					Velocity v =new Velocity(1.0,1.0,0.0);
					p.addVelovity(v);
					GotToAdd gotoadd =new GotToAdd();
					gotoadd.setProcedure(p);
					System.out.println(getJson(gotoadd));
					String jsonObject = postResponse(request, getJson(gotoadd));
					System.out.println(jsonObject);


			}
			
			if(mission==3){//PLACE RIGHT

					String request = null;
					if(RobotId==1){
						request = "http://"+IP+":"+PORT+"/rb1_base_a/robot_local_control/NavigationComponent/GoToComponent/add"; }
					if(RobotId==2){
						request = "http://"+IP+":"+PORT+"/rb1_base_b/robot_local_control/NavigationComponent/GoToComponent/add"; }
					if(RobotId==3){
						request = "http://"+IP+":"+PORT+"/rb1_base_c/robot_local_control/NavigationComponent/GoToComponent/add"; }
			
					Procedure p =new Procedure();
					Goal g = new Goal ();
					Header header = new Header();
					header.setSeq(0);
					header.setFrame_id("map");
					header.setStamp(new Stamp(0.0,0.0));
					g.setHeader(header);
					g.setPose(new Robot_Pose(7.75,0,-3.14));
					p.addGoal(g);
					Velocity v =new Velocity(1.0,1.0,0.0);
					p.addVelovity(v);
					GotToAdd gotoadd =new GotToAdd();
					gotoadd.setProcedure(p);
					System.out.println(getJson(gotoadd));
					String jsonObject = postResponse(request, getJson(gotoadd));
					System.out.println(jsonObject);


			}
		
		
	}
	
	
	public static void placeCART(int RobotId, int cart){
		String request = null;
		if(RobotId==1){
			request = "http://"+IP+":"+PORT+"/rb1_base_a/robot_local_control/NavigationComponent/PlaceComponent/add"; }
		if(RobotId==2){
			request = "http://"+IP+":"+PORT+"/rb1_base_b/robot_local_control/NavigationComponent/PlaceComponent/add";}
		if(RobotId==3){
			request = "http://"+IP+":"+PORT+"/rb1_base_c/robot_local_control/NavigationComponent/PlaceComponent/add";}

		

		PlaceAdd padd =new PlaceAdd();
		padd.getProcedure().setPick_frame_id("");
		String jsonObjectin = getJson(padd);
		String jsonObject = postResponse(request, jsonObjectin);
		
	}
    
	public   void pickCart(int RobotId, int cart){
		//pick cart2
		PickAdd p =new PickAdd();
			String request = null;			
			if(RobotId==1){
				request = "http://"+IP+":"+PORT+"/rb1_base_a/robot_local_control/NavigationComponent/PickComponent/add";
				p.getProcedure().setPick_frame_id("rb1_base_a_cart"+cart+"_contact");	
			}
			if(RobotId==2){
				request = "http://"+IP+":"+PORT+"/rb1_base_b/robot_local_control/NavigationComponent/PickComponent/add";
				p.getProcedure().setPick_frame_id("rb1_base_b_cart"+3+"_contact");	
			}
			if(RobotId==3){
				request = "http://"+IP+":"+PORT+"/rb1_base_c/robot_local_control/NavigationComponent/PickComponent/add";
				p.getProcedure().setPick_frame_id("rb1_base_c_cart"+4+"_contact");	
			}
				

				

				System.out.println(getJson(p));
				String jsonObject = postResponse(request, getJson(p));
				System.out.println(jsonObject);

			
		
	}
	public static int cancel(int RobotId, int mission){
		if (mission== 7){//pick
			String request = "http://"+IP+":"+PORT+"/rb"+RobotId+"_base_a/robot_local_control/NavigationComponent/PickComponent/cancel";
			

			PickQueryState query =new PickQueryState();
			
			query.getHeader().setId(-1);
			query.getHeader().getStamp().setSecs(0.0);
			query.getHeader().getStamp().setNsecs(0.0);
			
			String jsonObjectin = getJson(query);
			String jsonObject = postResponse(request, jsonObjectin);
			AddReply cmd= (AddReply) getObject(jsonObject,AddReply.class);
			String state = cmd.getState().getCurrent_state();
			System.out.println(state);

			
			if(state.compareTo("finished")==0){return 1;}
		}else{
			if(mission==3){//goto
				String request = "http://"+IP+":"+PORT+"/rb"+RobotId+"_base_a/robot_local_control/NavigationComponent/GoToComponent/cancel";
				
				System.out.println(request);
				GotoQueryState gotoquery =new GotoQueryState();
				
				gotoquery.getHeader().setId(-1);
				gotoquery.getHeader().getStamp().setSecs(0.0);
				gotoquery.getHeader().getStamp().setNsecs(0.0);
				
				String jsonObjectin = getJson(gotoquery);
				String jsonObject = postResponse(request, jsonObjectin);
				AddReply cmd= (AddReply) getObject(jsonObject,AddReply.class);
				String state = cmd.getState().getCurrent_state();
				System.out.println(state);
	
				
				if(state.compareTo("finished")==0){return 1;}
				
			}else{
				if(mission==11){//place
					String request = "http://"+IP+":"+PORT+"/rb"+RobotId+"_base_a/robot_local_control/NavigationComponent/PlaceComponent/cancel";
					

					PlaceQueryState query =new PlaceQueryState();
					
					query.getHeader().setId(-1);
					query.getHeader().getStamp().setSecs(0.0);
					query.getHeader().getStamp().setNsecs(0.0);
					
					String jsonObjectin = getJson(query);
					String jsonObject = postResponse(request, jsonObjectin);
					AddReply cmd= (AddReply) getObject(jsonObject,AddReply.class);
					String state = cmd.getState().getCurrent_state();
					System.out.println(state);
		
					
					if(state.compareTo("finished")==0){return 1;}					
				}
			}
		}
		return 0;
	}
	
	public static int queryState(int RobotId, int mission){
		
		
		
		if (mission== 7){//pick
			String request = null;
			if(RobotId==1){
				request = "http://"+IP+":"+PORT+"/rb1_base_a/robot_local_control/NavigationComponent/PickComponent/query_state"; }
			if(RobotId==2){
				request = "http://"+IP+":"+PORT+"/rb1_base_b/robot_local_control/NavigationComponent/PickComponent/query_state";}
			if(RobotId==3){
				request = "http://"+IP+":"+PORT+"/rb1_base_c/robot_local_control/NavigationComponent/PickComponent/query_state";}

			
 
			

			PickQueryState query =new PickQueryState();
			
			query.getHeader().setId(-1);
			query.getHeader().getStamp().setSecs(0.0);
			query.getHeader().getStamp().setNsecs(0.0);
			
			String jsonObjectin = getJson(query);
			String jsonObject = postResponse(request, jsonObjectin);
			AddReply cmd= (AddReply) getObject(jsonObject,AddReply.class);
			String state = cmd.getState().getCurrent_state();
			System.out.println(state);

			
			if(state.compareTo("finished")==0){return 1;}
		}else{
			if(mission==3){//goto
				String request = null;
				if(RobotId==1){
					request = "http://"+IP+":"+PORT+"/rb1_base_a/robot_local_control/NavigationComponent/GoToComponent/query_state"; }
				if(RobotId==2){
					request = "http://"+IP+":"+PORT+"/rb1_base_b/robot_local_control/NavigationComponent/GoToComponent/query_state";}
				if(RobotId==3){
					request = "http://"+IP+":"+PORT+"/rb1_base_c/robot_local_control/NavigationComponent/GoToComponent/query_state";}

				
				System.out.println(request);
				GotoQueryState gotoquery =new GotoQueryState();
				
				gotoquery.getHeader().setId(-1);
				gotoquery.getHeader().getStamp().setSecs(0.0);
				gotoquery.getHeader().getStamp().setNsecs(0.0);
				
				String jsonObjectin = getJson(gotoquery);
				String jsonObject = postResponse(request, jsonObjectin);
				AddReply cmd= (AddReply) getObject(jsonObject,AddReply.class);
				String state = cmd.getState().getCurrent_state();
				System.out.println(state);
	
				
				if(state.compareTo("finished")==0){return 1;}
				
			}else{
				if(mission==11){//place
					
					String request = null;
					if(RobotId==1){
						request = "http://"+IP+":"+PORT+"/rb1_base_a/robot_local_control/NavigationComponent/PlaceComponent/query_state"; 
						}
					if(RobotId==2){
						request = "http://"+IP+":"+PORT+"/rb1_base_b/robot_local_control/NavigationComponent/PlaceComponent/query_state";
						}
					if(RobotId==3){
						request = "http://"+IP+":"+PORT+"/rb1_base_c/robot_local_control/NavigationComponent/PlaceComponent/query_state";
						}
					
					
						

					PlaceQueryState query =new PlaceQueryState();
					
					query.getHeader().setId(-1);
					query.getHeader().getStamp().setSecs(0.0);
					query.getHeader().getStamp().setNsecs(0.0);
					
					String jsonObjectin = getJson(query);
					String jsonObject = postResponse(request, jsonObjectin);
					System.out.println(jsonObject);
					AddReply cmd= (AddReply) getObject(jsonObject,AddReply.class);
					String state = cmd.getState().getCurrent_state();
					System.out.println(state);
		
					
					if(state.compareTo("finished")==0){return 1;}					
				}
			}
		}
		return 0;
		
	}
	
	public    int checkMarkers(int RobotId, int obj){
		String request = null;
		if(RobotId==1){
			request= "http://"+IP+":"+PORT+"/rb1_base_a/ar_pose_marker";
		 }
		if(RobotId==2){
			request= "http://"+IP+":"+PORT+"/rb1_base_b/ar_pose_marker";
		 }
		if(RobotId==3){
			request= "http://"+IP+":"+PORT+"/rb1_base_c/ar_pose_marker";
		 }



		String jsonObject = getResponse(request);
		System.out.println(jsonObject);
		Markers_in_sight cmd= (Markers_in_sight) getObject(jsonObject,Markers_in_sight.class);
		if(cmd!=null){
			if(cmd.getMarkers().size()==0){
				return 0;
				}else{
			
				}
		}else{
			return 0;
		}
		return cmd.getMarkers().size();
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