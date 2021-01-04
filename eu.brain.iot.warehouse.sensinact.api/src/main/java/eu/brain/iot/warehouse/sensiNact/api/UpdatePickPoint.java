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

/*
 * used to update the Picking_Points table
 * */

public class UpdatePickPoint extends SensiNactCommand {
    
   public String pickID;
   public String  pickPoint;  // coordinate: 8.0,-3.6,-3.14   presenting x,y,z
   public boolean isAssigned;
}
