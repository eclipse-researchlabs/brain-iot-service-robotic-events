package compound;

import task.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;



public class Compound extends Thread  implements Runnable{



	public Compound(){
		
	}
	int i=0;
	int j=0;
	int time=0;
public void run() {
	

	Class aClass = getClass();
	Method[] method = aClass.getDeclaredMethods();
	Annotation[] Elementannotations = aClass.getAnnotations();

	for(Annotation annotation : Elementannotations){
	    if(annotation instanceof Task){
	    	Task task = (Task) annotation;
	        time= task.time();
	    }
	}
    
	
		while(true){
	
	
		
		try {
			
	        if(j<(method.length)){

	    		Annotation[] annotations = method[j].getDeclaredAnnotations();	

			for(Annotation annotation : annotations){		
			    if(annotation instanceof CompoundMethodName){
			    	CompoundMethodName myAnnotation = (CompoundMethodName) annotation;
			    //		System.err.println("\n Compound name :["+ aClass.getName()+"] start: " + myAnnotation.name()+"\n");	

			        Method privateStringMethod;
					try {
						privateStringMethod = this.getClass().getDeclaredMethod(myAnnotation.name(), null);

				        privateStringMethod.invoke(this, null);
				        
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
	        
	        }
	        //else{ j=0; }


			
			sleep(time);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		}
	
	}
	

}
