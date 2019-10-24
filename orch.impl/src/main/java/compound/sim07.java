package compound;

import org.lib.Command;
import atom.*;
import port.*;
import type.*;

import task.*;

import connector.*;

// Compound Definition;

@Task(time=5)
public class sim07 extends Compound { 

org.lib.Command Command = new org.lib.Command() ;
public sim07( ){ 	
 start();
 } 
@CompoundMethodName(name="init")
public void init( ){
robot_1= new Orchestrator(new Type<Integer>(1)) ; 

robot_2= new Orchestrator2(new Type<Integer>(2)) ; 

robot_3= new Orchestrator3(new Type<Integer>(3)) ; 

}
 public Orchestrator robot_1; 
 public Orchestrator2 robot_2; 
 public Orchestrator3 robot_3; 

}