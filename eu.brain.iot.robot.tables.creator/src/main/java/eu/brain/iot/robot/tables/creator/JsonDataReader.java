/*******************************************************************************
 * Copyright (C) 2021 LINKS Foundation
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package eu.brain.iot.robot.tables.creator;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import eu.brain.iot.robot.tables.jsonReader.CartTable;
import eu.brain.iot.robot.tables.jsonReader.DockTable;
import eu.brain.iot.robot.tables.jsonReader.PickingTable;
import eu.brain.iot.robot.tables.jsonReader.StorageTable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

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
		this.jsonFilePath = jsonFilePath; // /home/fabric-n9/resources/
		loadTables();

		logger.info("--------------------Warehouse Tables---------------------------");
        System.out.println(pickingTable.toString());
        System.out.println(storageTable.toString());
        System.out.println(cartTable.toString());
        System.out.println(dockTable.toString());
        logger.info("-----------------------------------------------");
	}
	
	private void loadTables() {
		Gson gson = new Gson();
		JsonReader reader;
		try {
			Process proc = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", "pwd" });
			String line = "";
			BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = input.readLine()) != null) {
				logger.info(" Table Creator current path = "+ line);
			}
			input.close();
			proc.destroy();
			proc = null;
		} catch (IOException e) {
			logger.error("\n Exception:", e);
		}

		try {
			reader = new JsonReader(new FileReader(jsonFilePath+"Picking_Points.json"));
			pickingTable = gson.fromJson(reader, PickingTable.class);
			
			reader = new JsonReader(new FileReader(jsonFilePath+"Storage_Points.json"));
			storageTable = gson.fromJson(reader, StorageTable.class);
			
			reader = new JsonReader(new FileReader(jsonFilePath+"Cart_Storage.json"));
			cartTable = gson.fromJson(reader, CartTable.class);
			
			reader = new JsonReader(new FileReader(jsonFilePath+"Docking_Points.json"));
			dockTable = gson.fromJson(reader, DockTable.class);
			
		} catch (FileNotFoundException e) {
			logger.error("\n Exception:", e);
		}
		
	}
}
