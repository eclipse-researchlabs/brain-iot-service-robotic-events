package eu.brain.iot.robot.behaviour;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class StateMachineMonitoring { 
		final static  String prefix="TRANSITION|ServiceRobotics::Robot::Robot::RobotStateMachine::Region1::";
		private DatagramSocket dsocket;
		private String hostname;
		private int port;
		
		/*public static void main(String[] args) {
			StateMachineMonitoring sm=new StateMachineMonitoring("192.168.2.167",4445);
			sm.startMonitorning();
			sm.UDPSend("RobotReadyBroadcast");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sm.UDPSend("BroadcastACK");	
			sm.stopMonitoring();
	    }*/
		
	   public StateMachineMonitoring(String hostname, int port){
		   this.hostname=hostname;
		   this.port= port;
		   
	   }
	   public DatagramSocket startMonitorning(){	       
		        DatagramSocket dsocket =null;
			   // Create a datagram socket, send the packet through it, close it.
				try {
				   this.dsocket = new DatagramSocket();
				   
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				return dsocket;
	   }
	   public void stopMonitoring() {
		   this.dsocket.close();
	   }
	   public void UDPSend(String message) {
		   byte [] msg=(prefix+message).getBytes();
		   // Get the internet address of the specified host
		      try {
				InetAddress address = InetAddress.getByName(hostname);
				
				// Initialize a datagram packet with data and address
			      DatagramPacket packet = new DatagramPacket(msg, msg.length,
			          address, port);
			      // Create a datagram socket, send the packet through it, close it.
			      dsocket.send(packet);
			      
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   
		   
	   }
	   public void closeSocket(DatagramSocket socket) {
		   socket.close();
	   }
	   public String getServerHostName() {
		   return this.hostname;
	   }
	   public int getServerPort() {
		   return this.port;
	   }
	}
