/*******************************************************************************
 * Copyright (C) 2021 LINKS Foundation
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package eu.brain.iot.warehouse.sensiNact.api;

import eu.brain.iot.warehouse.sensiNact.api.WarehouseTables.Tables;

/*
 * this event is sent from warehouse backend
 * */

public class UpdateResponse extends SensiNactCommand {

	public UpdateStatus updateStatus; // Acknowledge response from warehouse backend
	
	public Tables table;
	
	public static enum UpdateStatus {
		OK, ERROR;
	}
	
	
	
}
