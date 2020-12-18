package eu.brain.iot.robot.events;

import java.sql.Time;

import eu.brain.iot.robot.api.RobotCommand;

// topic: /turtlebot_id/mobile_base/sensors/core
// message: kobuki_msgs/SensorState message

public class BetteryVoltage extends RobotCommand {

	public String index; // timestamp format: yyyy-MM-dd HH:mm:ss
	public double target; // voltage
}
