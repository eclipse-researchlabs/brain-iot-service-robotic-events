package compound;

import org.lib.Command;

import com.paremus.brain.iot.example.orch.impl.ComponentImpl;

import atom.Orchestrator;
import atom.Orchestrator2;
import atom.Orchestrator3;
import task.Task;
import type.Type;

// Compound Definition;

@Task(time=5)
public class sim07 extends Compound { 

final org.lib.Command Command;
final String doorId;
public sim07(String doorId, ComponentImpl componentImpl ){ 	
 this.doorId = doorId;
Command = new Command(componentImpl);
 start();
 } 
@CompoundMethodName(name="init")
public void init( ){
robot_1= new Orchestrator(Command, doorId, new Type<Integer>(1)) ; 

robot_2= new Orchestrator2(Command, doorId, new Type<Integer>(2)) ; 

robot_3= new Orchestrator3(Command, doorId, new Type<Integer>(3)) ; 

}
 public Orchestrator robot_1; 
 public Orchestrator2 robot_2; 
 public Orchestrator3 robot_3; 

}