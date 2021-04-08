/*******************************************************************************
 * Copyright (C) 2021 LINKS Foundation
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package eu.brain.iot.warehouse.sensinact.test;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import eu.brain.iot.robot.sensinact.jsonReader.CartTable;
import eu.brain.iot.robot.sensinact.jsonReader.DockTable;
import eu.brain.iot.robot.sensinact.jsonReader.PickingTable;
import eu.brain.iot.robot.sensinact.jsonReader.StorageTable;

import java.io.FileReader;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsonDataReader {
	
	protected PickingTable pickingTable;
	protected StorageTable storageTable;
	protected CartTable cartTable;
	protected DockTable dockTable;
	private String jsonFilePath;
	
	private static final Logger logger = (Logger) LoggerFactory.getLogger(JsonDataReader.class.getSimpleName());
	
	public JsonDataReader(){}


	public JsonDataReader(String jsonFilePath)
	{
		this.jsonFilePath = jsonFilePath;
		loadTables();

	//	logger.info("--------------------Warehouse Tables---------------------------");
		System.out.println("--------------------Warehouse Tables---------------------------");
	/*	System.out.println(pickingTable.getPickingPoints());
		System.out.println(storageTable.getStoragePoints());
		System.out.println(cartTable.getCartStorages());
		System.out.println(dockTable.getDockingPoints());*/
	}
	
	private void loadTables() {
		Gson gson = new Gson();
		JsonReader reader;
		try {
			reader = new JsonReader(new FileReader(jsonFilePath+"Picking_Points.json"));
			pickingTable = gson.fromJson(reader, PickingTable.class);
			
			reader = new JsonReader(new FileReader(jsonFilePath+"Storage_Points.json"));
			storageTable = gson.fromJson(reader, StorageTable.class);
			
			reader = new JsonReader(new FileReader(jsonFilePath+"Cart_Storage.json"));
			cartTable = gson.fromJson(reader, CartTable.class);
			
			reader = new JsonReader(new FileReader(jsonFilePath+"Docking_Points.json"));
			dockTable = gson.fromJson(reader, DockTable.class);
			
		} catch (Exception e) {
			logger.error("\n Exception: {}", ExceptionUtils.getStackTrace(e));
			e.printStackTrace();
		}
		
	}
}
