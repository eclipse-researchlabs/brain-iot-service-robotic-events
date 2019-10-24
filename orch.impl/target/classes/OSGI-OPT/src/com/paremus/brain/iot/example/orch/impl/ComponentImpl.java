package com.paremus.brain.iot.example.orch.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.felix.service.command.CommandProcessor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.paremus.brain.iot.example.orch.api.Cancel;
import com.paremus.brain.iot.example.orch.api.CheckMarker;
import com.paremus.brain.iot.example.orch.api.CheckMarkerB;
import com.paremus.brain.iot.example.orch.api.CheckMarkerC;
import com.paremus.brain.iot.example.orch.api.DoorClose;
import com.paremus.brain.iot.example.orch.api.DoorOpen;
import com.paremus.brain.iot.example.orch.api.InetRobot;
import com.paremus.brain.iot.example.orch.api.PickCart;
import com.paremus.brain.iot.example.orch.api.PlaceCART;
import com.paremus.brain.iot.example.orch.api.QueryState;
import com.paremus.brain.iot.example.orch.api.QueryStateB;
import com.paremus.brain.iot.example.orch.api.QueryStateC;
import com.paremus.brain.iot.example.orch.api.writeGOTO;

import com.paremus.brain.iot.example.robotA.api.CheckValueReturn;
import com.paremus.brain.iot.example.robotA.api.QueryStateValueReturn;
import com.paremus.brain.iot.example.robotB.api.CheckValueReturnB;
import com.paremus.brain.iot.example.robotB.api.QueryStateValueReturnB;
import com.paremus.brain.iot.example.robotC.api.CheckValueReturnC;
import com.paremus.brain.iot.example.robotC.api.QueryStateValueReturnC;

import compound.sim07;
import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.BrainIoTEvent;
import eu.brain.iot.eventing.api.EventBus;
import eu.brain.iot.eventing.api.SmartBehaviour;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardResource;
import org.osgi.service.jaxrs.whiteboard.propertytypes.JaxrsResource;

@Component(
		property = {
				CommandProcessor.COMMAND_SCOPE + "=Orchestrator", //
				CommandProcessor.COMMAND_FUNCTION + "=orch" //
		},service={SmartBehaviour.class, ComponentImpl.class}
	)
@JaxrsResource
@HttpWhiteboardResource(pattern="/quickstart/*", prefix="static")


@SmartBehaviourDefinition(consumed = {CheckValueReturn.class, QueryStateValueReturn.class,CheckValueReturnB.class, QueryStateValueReturnB.class,CheckValueReturnC.class, QueryStateValueReturnC.class },    
author = "UGA", name = "Smart Orchestrator",
description = "Implements a remote Smart Orchestrator.")
public class ComponentImpl  implements SmartBehaviour<BrainIoTEvent>{


	@Path("rest/robot")
	@POST  
    @Consumes(MediaType.MULTIPART_FORM_DATA)  
    public String toUpper( @FormParam("ip") String ip, @FormParam("port") String port) {
		 InetAddress link;
		 if(ip.contains(".")) {
			 
		 
		try {
			link = InetAddress.getByName(ip);
		    if (link.isReachable(5000)) 
		    { 
		    	System.out.println("Host is reachable"); 
		    	 InetRobot inetrobot = new InetRobot();
		    	 inetrobot.ip=ip;
		    	 inetrobot.port=port;
		    	 
		    	eventBus.deliver(inetrobot);	
		    	
		    	orch();
		    	return "<style>\n" + 
		    			".content {\n" + 
		    			"  max-width: 500px;\n" + 
		    			"  margin: auto;\n" + 
		    			"}\n" + 
		    			"</style>\n" + 
		    			"\n" + 
		    			"<div class=\"content\">\n" + 
		    			"\n" + 
		    			"\n" + 
		    			"<pre style=\"color:Tomato;\">\n" + 
		    			"     <b>Host : "+ip+" is reachable </b>\n" + 
		    			"</pre>\n" + 
		    			"<pre style=\"color:DodgerBlue;\">\n" + 
		    			"     <b>Activating Robots  ...</b>\n" + 
		    			"</pre>\n" + 		    			
		    			"\n" + 
		    			"\n" + 
		    			"</div>";
		    }
			else
			{ 
				System.out.println("Sorry ! We can't reach to this host");
		    	return "<style>\n" + 
    			".content {\n" + 
    			"  max-width: 500px;\n" + 
    			"  margin: auto;\n" + 
    			"}\n" + 
    			"</style>\n" + 
    			"\n" + 
    			"<div class=\"content\">\n" + 
    			"\n" + 
    			"\n" + 
    			"<pre style=\"color:Tomato;\">\n" + 
    			"     <b>Sorry ! We can't reach to this host: "+ ip+"</b>\n" + 
    			"</pre>\n" + 
    			"\n" + 
    			"\n" + 
    			"</div>";
				
				}
		    
		    
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		 }
		
    	return "<style>\n" + 
		".content {\n" + 
		"  max-width: 500px;\n" + 
		"  margin: auto;\n" + 
		"}\n" + 
		"</style>\n" + 
		"\n" + 
		"<div class=\"content\">\n" + 
		"\n" + 
		"\n" + 
		"<pre style=\"color:Tomato;\">\n" + 
		"     <b>Sorry ! The connection can not be established</b>\n" + 
		"</pre>\n" + 
		"\n" + 
		"\n" + 
		"</div>";

		    
    }
    @Path("/main")
    @GET
    public String load( ) {
        return "\n" + 
        		"<style>\n" + 
        		".content {\n" + 
        		"  max-width: 500px;\n" + 
        		"  margin: auto;\n" + 
        		"}\n" + 
        		"</style>\n" + 
        		"\n" + 
        		"<div class=\"content\">\n" + 
        		"<form name=\"mon-formulaire1\" action=\"rest/robot/\" method=\"post\" enctype=\"multipart/form-data\" align=\"center\">\n" + 
        		"\n" + 
        		"<p>\n" + 
        		"   IP  :<br />\n" + 
        		"   <input type=\"text\" name=\"ip\" value=\"\" />\n" + 
        		"</p>\n" + 
        		"<p>\n" + 
        		"   Port :<br />\n" + 
        		"   <input type=\"text\" name=\"port\" value=\"\" />\n" + 
        		"</p>\n" + 
        		"\n" + 
        		"\n" + 
        		"<p>\n" + 
        		"   <input type=\"submit\" value=\"Start Robots\" />\n" + 
        		"   <input type=\"reset\" value=\"Cancel\" />\n" + 
        		"</p>\n" + 
        		"</form>\n" + 
        		"\n" + 
        		"</div>";
    }
	
	
	
	private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
	
	
	
	
	
    public void  orch() {
    	new sim07();
    	worker.execute(this::update);

    }
    
    @Reference
    private  EventBus eventBus;
 
    
    public static CheckValueReturn checkreturnedvalue;
    
    public static QueryStateValueReturn queryreturnedvalue;
    
    public static CheckValueReturnB checkreturnedvalueb;
    
    public static QueryStateValueReturnB queryreturnedvalueb;    
    
    public static CheckValueReturnC checkreturnedvaluec;
    
    public static QueryStateValueReturnC queryreturnedvaluec;      
    
    
    public static DoorOpen dooropen;
    
    public static DoorClose doorclose;
    
	public static writeGOTO wgoto ;
	
	public static PlaceCART pc ;
	
	public static Cancel cl;
	
	
	public static QueryState qs ;
	public static QueryStateB qsb ;
	public static QueryStateC qsc ;
	
	public static PickCart pk ;
	
	
	public static CheckMarker cm; 
	public static CheckMarkerB cmb; 
	public static CheckMarkerC cmc; 
	
	private void update() {

		if(dooropen!=null) {
			eventBus.deliver(dooropen);
			dooropen=null;
		}
		if(doorclose!=null) {
			eventBus.deliver(doorclose);
			doorclose=null;
		}	
		if(wgoto!=null) {
			eventBus.deliver(wgoto);
			wgoto=null;
		}		
		
		if(pc!=null) {
			eventBus.deliver(pc);
			pc=null;
		}	
		if(cl!=null) {
			eventBus.deliver(cl);
			cl=null;
		}	
		if(qs!=null) {
			eventBus.deliver(qs);
			qs=null;
		}
		if(qsb!=null) {
			eventBus.deliver(qsb);
			qsb=null;
		}
		if(qsc!=null) {
			eventBus.deliver(qsc);
			qsc=null;
		}
		if(pk!=null) {
			eventBus.deliver(pk);
			pk=null;
		}
		if(cm!=null) {
			eventBus.deliver(cm);
			cm=null;
		}
		if(cmb!=null) {
			eventBus.deliver(cmb);
			cmb=null;
		}
		if(cmc!=null) {
			eventBus.deliver(cmc);
			cmc=null;
		}
    	worker.schedule(this::update, 1, TimeUnit.MILLISECONDS);
	}
    
    public static void  writeOpenDoor(){
    	dooropen=new DoorOpen();
		System.out.println("ORCHESTRATOR: OPEN DOOR");
	}
	public static void  writeCloseDoor(){
		doorclose = new DoorClose();	
		System.out.println("ORCHESTRATOR: CLOSE DOOR");
	}
	public static void  writegoto(int RobotId, int mission){
		wgoto =new com.paremus.brain.iot.example.orch.api.writeGOTO();
		wgoto.mission=mission;	
		
		System.out.println("ORCHESTRATOR: SEND GOTO");
	}
    
	public static void placeCART(int RobotId, int cart) {
		pc =new PlaceCART();
		pc.cart=cart;
		System.out.println("ORCHESTRATOR: SEND PLACE CART");
	}
	
	public static int cancel(int RobotId, int mission){
		cl =new Cancel();
		cl.mission =mission;
		
		return mission;		
	}
	public static int queryState(int RobotId, int mission){
		qs =new QueryState();
		qs.robotid=RobotId;
		
		 qs.mission =mission;
		  int i=0;
			while(i==0) {
				if(queryreturnedvalue!=null) {
				i=1;	
				System.out.print("--- WATING QueryState loop--- " + i);
				}
				//System.out.print("--- WATING QueryState loop--- " + i);
			try {	
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}

			}
			int q = queryreturnedvalue.value;
			queryreturnedvalue=null;
			System.out.println("ORCHESTRATOR: SEND QUERY STATE");
			return q;

		
	}
	
	public static int queryStateb(int RobotId, int mission){
		qsb =new QueryStateB();
		qsb.robotid=RobotId;
		
		 qsb.mission =mission;
		  int i=0;
			while(i==0) {
				if(queryreturnedvalueb!=null) {
				i=1;	
				System.out.print("--- WATING QueryState loop--- " + i);
				}
				//System.out.print("--- WATING QueryState loop--- " + i);
			try {	
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}

			}
			int q = queryreturnedvalueb.value;
			queryreturnedvalueb=null;
			System.out.println("ORCHESTRATOR: SEND QUERY STATE");
			return q;

	}
	public static int queryStatec(int RobotId, int mission){
		qsc =new QueryStateC();
		qsc.robotid=RobotId;
		
		 qsc.mission =mission;
		  int i=0;
			while(i==0) {
				if(queryreturnedvaluec!=null) {
				i=1;	
				System.out.print("--- WATING QueryState loop--- " + i);
				}
				//System.out.print("--- WATING QueryState loop--- " + i);
			try {	
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}

			}
			int q = queryreturnedvaluec.value;
			queryreturnedvaluec=null;
			System.out.println("ORCHESTRATOR: SEND QUERY STATE");
			return q;

	}
	
	public static void pickCart(int RobotId, int cart){
		pk =new PickCart();
		pk.cart=cart;
		
		System.out.println("ORCHESTRATOR: SEND PICK CART STATE");
		
	}
	public static int checkMarkers(int RobotId, int obj) {
		cm =new CheckMarker();
		cm.robotid=RobotId;
		int i=0;
		while(i==0) {
			if(checkreturnedvalue!=null) {
			i=1;	
			}
			//System.out.println("--- WATING checkMarkers loop---");
			try {	
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
		
		int q = checkreturnedvalue.value;
		checkreturnedvalue=null;
		System.out.println("ROBOT: SEND CHECK QRT STATE");
		return q;
	}
	public static int checkMarkersb(int RobotId, int obj) {
		cmb =new CheckMarkerB();
		cmb.robotid=RobotId;
		int i=0;
		while(i==0) {
			if(checkreturnedvalueb!=null) {
			i=1;	
			}
			//System.out.println("--- WATING checkMarkers loop---");
			try {	
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
		
		int q = checkreturnedvalueb.value;
		checkreturnedvalueb=null;
		System.out.println("ROBOT B: SEND CHECK QRT STATE");
		return q;
	}
	public static int checkMarkersc(int RobotId, int obj) {
		cmc =new CheckMarkerC();
		cmc.robotid=RobotId;
		int i=0;
		while(i==0) {
			if(checkreturnedvaluec!=null) {
			i=1;	
			}
			//System.out.println("--- WATING checkMarkers loop---");
			try {	
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
		
		int q = checkreturnedvaluec.value;
		checkreturnedvaluec=null;
		System.out.println("ROBOT C: SEND CHECK QRT STATE");
		return q;
	}
	
	@Override
	public void notify(BrainIoTEvent event) {

		if (event instanceof CheckValueReturn) {
			checkreturnedvalue = (CheckValueReturn) event;
			
		}else {
			
			if (event instanceof QueryStateValueReturn) {
				queryreturnedvalue = (QueryStateValueReturn) event;

				System.out.println("--- QueryState received---");
			}else {
				
				if (event instanceof QueryStateValueReturnB) {
					queryreturnedvalueb = (QueryStateValueReturnB) event;

					System.out.println("--- QueryState received---");
				}else {
					
					if (event instanceof CheckValueReturnB) {
						checkreturnedvalueb = (CheckValueReturnB) event;
						
					}else {
						
						if (event instanceof CheckValueReturnC) {
							checkreturnedvaluec = (CheckValueReturnC) event;
							
						}else {
							if (event instanceof QueryStateValueReturnC) {
								queryreturnedvaluec = (QueryStateValueReturnC) event;
								System.out.println("--- QueryState received---");
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
