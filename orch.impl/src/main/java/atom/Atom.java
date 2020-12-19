package atom;

import task.*;

import port.*;

import org.lib.Command;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;




public class Atom extends Thread  implements Runnable{



	public Atom(){
		
	}
	int step=0;
	int i=0;
	int j=0;
	int time = 0 ;
public void run() {

	Class aClass = getClass();
	synchronized(aClass) {
	Method[] method = aClass.getDeclaredMethods();
	Annotation[] Elementannotations = aClass.getAnnotations();

	for(Annotation annotation : Elementannotations){
	    if(annotation instanceof Task){
	    	Task task = (Task) annotation;
	        time= task.time();
	    }
	}
   // System.out.println("Atom j: " +j+"|");
	while(true){
		
		try {
		//	 System.out.println("Atom j: " +j+"atom i : "+ i);
	        Method privateStringMethod;
			for (int i=0;i<method.length;i++){
	    		Annotation[] annotations = method[i].getDeclaredAnnotations();	
				for(Annotation annotation : annotations){
			    if(annotation instanceof PortName){
			    	PortName myAnnotation = (PortName) annotation;
			    	
			    	try {
						privateStringMethod = this.getClass().getDeclaredMethod("get"+myAnnotation.name(), null);

				        Port rs = (Port) privateStringMethod.invoke(this, null);
				        
				        if(rs.isNotified()){
				        	System.out.println("notification "+ rs.isNotified());
				        	
							privateStringMethod = this.getClass().getDeclaredMethod(myAnnotation.name(), null);

					        int  r = (Integer) privateStringMethod.invoke(this, null);
					        
					        if(r==1){
					        	step =step +1;
						    	if(myAnnotation.name().compareTo("initial")==0){
						    //		System.err.println("\n ["+step+"] Atom name :["+ aClass.getName()+"] Start BIP Behavior on : " + myAnnotation.name()+"\n");
						    					    		
						    	}else{
						    		if(myAnnotation.name().contains("internal")){
								//       System.err.println("\n ["+step+"]  Atom name :["+ aClass.getName()+"] BIP Internal Bahavior :" + myAnnotation.name()+"\n");	
						    		} else{
						    	//	System.err.println("\n ["+step+"]  Atom name :["+ aClass.getName()+"] BIP port name: " + myAnnotation.name()+"\n");	
							    	}
						    	}
					        }
				        }
				        rs.setAvailable(false);
				        
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NoSuchMethodException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
			}
			}
	        if(j<(method.length)){

	    		Annotation[] annotations = method[j].getDeclaredAnnotations();	

			for(Annotation annotation : annotations){


				
			    if(annotation instanceof AtomMethodName){
			    	AtomMethodName myAnnotation = (AtomMethodName) annotation;


			        //Method privateStringMethod;
					try {
						privateStringMethod = this.getClass().getDeclaredMethod(myAnnotation.name(), null);

				        int rs = (Integer) privateStringMethod.invoke(this, null);
				        
				        if(rs==1){
				        	step =step +1;
					    	if(myAnnotation.name().compareTo("initial")==0){
					    	//	System.err.println("\n ["+step+"] Atom name :["+ aClass.getName()+"] Start BIP Behavior on : " + myAnnotation.name()+"\n");
					    					    		
					    	}else{
					    		if(myAnnotation.name().contains("internal")){
							 //      System.err.println("\n ["+step+"]  Atom name :["+ aClass.getName()+"] BIP Internal Bahavior :" + myAnnotation.name()+"\n");	
					    		} else{
					    	//	System.err.println("\n ["+step+"]  Atom name :["+ aClass.getName()+"] BIP port name: " + myAnnotation.name()+"\n");	
						    	}
					    	}
				        }
					} catch (NoSuchMethodException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        

			        
			    }
			
			
			}
			j++;
	        i=j/2;
	        
	        }
	        else{ j=0; i=0; }


			
			sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	}
	
}
boolean portstate;

boolean isEnablePorts(){
	return portstate;
}

void disablePorts(){
	portstate=false;
}

void enablePorts(){
	portstate=true;
}

Queue<String> queue = new LinkedList<String>();

boolean isReady(Object obj){
	
	if((obj.getClass().getName()).compareTo(queue.element())==0){
		return true;
	}
	return false;
}

void deque(){
	queue.remove();
}

void enqueue(Object obj){
	queue.add(obj.getClass().getName());
}


}

