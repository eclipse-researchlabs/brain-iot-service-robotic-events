package org.lib.request.uncharge.querystate;

import org.lib.availability.Stamp;

public class QuerystateHeader {


	
	int id =-1;
	
	int priority =0;
	
	Stamp stamp =new Stamp();
	
	String name ="";

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Stamp getStamp() {
		return stamp;
	}

	public void setStamp(Stamp stamp) {
		this.stamp = stamp;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
