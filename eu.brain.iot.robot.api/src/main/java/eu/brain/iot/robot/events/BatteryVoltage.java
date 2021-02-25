/*******************************************************************************
 * Copyright (C) 2021 LINKS Foundation
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package eu.brain.iot.robot.events;

import java.sql.Time;

import eu.brain.iot.robot.api.RobotCommand;

// topic: /turtlebot_id/mobile_base/sensors/core
// message: kobuki_msgs/SensorState message

public class BatteryVoltage extends RobotCommand {

	public String index; // timestamp format: yyyy-MM-dd HH:mm:ss
	public double target; // voltage
}
