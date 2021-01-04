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
 * this event is sent from warehouse backend
 * */

public class PickingPointUpdateNotice extends SensiNactCommand {
    
   public String pickID;

   //after receiving this event from warehouse backend, Sensinact must update the isAssigned column after an iteration starts or finished
   public boolean isAssigned; 
}
