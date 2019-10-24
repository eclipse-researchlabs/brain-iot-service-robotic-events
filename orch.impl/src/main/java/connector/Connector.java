package connector;

import task.*;


import org.lib.Command;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class Connector extends Thread implements Runnable {


	public Connector(){
		
	}
	int i=0;
	int time=0;
public void run() {
		

		Class aClass = getClass();
		synchronized(aClass) {
		Method[] method = aClass.getDeclaredMethods();
		Annotation[] Elementannotations = aClass.getAnnotations();
		//task execution time
		for(Annotation annotation : Elementannotations){
		    if(annotation instanceof Task){
		    	Task task = (Task) annotation;
		        time= task.time();
		    }
		}
		
		while(true){
			
			try {
				

		       // System.out.println("method: " +method.length+"|");
				Annotation[] annotations = method[i].getDeclaredAnnotations();
		       // System.out.println("Connector annotation: " +annotations.length+"|");
				//Annotation annotation =annotations[i];

						

				for(Annotation annotation : annotations){					
				    if(annotation instanceof ConnectorMethodName){
				    	ConnectorMethodName myAnnotation = (ConnectorMethodName) annotation;
				       
				        
				        Method privateStringMethod;
						try {
							privateStringMethod = this.getClass().getDeclaredMethod(myAnnotation.name(), null);

					        int r =(Integer) privateStringMethod.invoke(this, null);
					        
					        if(r==1){
					       // 	 System.err.println("\n Connector name : ["+aClass.getName()+"] Connector interraction name: " + myAnnotation.name());
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
				
		        i++;
		        if(i==annotations.length){ i=0; }
				sleep(time);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

}
