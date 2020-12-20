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
import java.io.InputStreamReader;


public class JsonDataReader {
	
	protected PickingTable pickingTable;
	protected StorageTable storageTable;
	protected CartTable cartTable;
	protected DockTable dockTable;
	

	public JsonDataReader()
	{
		loadTables();

        System.out.println("--------------------Warehouse Tables---------------------------");
        System.out.println(pickingTable);
        System.out.println(storageTable);
        System.out.println(cartTable);
        System.out.println(dockTable);
        System.out.println("-----------------------------------------------");
	}
	
	private void loadTables() {
		Gson gson = new Gson();
		JsonReader reader;
		try {
			Process proc = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", "pwd" });
			String line = "";
			BufferedReader input = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			input.close();
			proc.destroy();
			proc = null;
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			reader = new JsonReader(new FileReader("resources/Picking_Points.json"));
			pickingTable = gson.fromJson(reader, PickingTable.class);
			
			reader = new JsonReader(new FileReader("resources/Storage_Points.json"));
			storageTable = gson.fromJson(reader, StorageTable.class);
			
			reader = new JsonReader(new FileReader("resources/Cart_Storage.json"));
			cartTable = gson.fromJson(reader, CartTable.class);
			
			reader = new JsonReader(new FileReader("resources/Docking_Points.json"));
			dockTable = gson.fromJson(reader, DockTable.class);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
}
