package org.lib.availability;

import java.util.ArrayList;

public class Mission_status {

	Current current = new Current () ;
	ArrayList<String> last =new ArrayList<String> ();
	public Current getCurrent() {
		return current;
	}
	public void setCurrent(Current current) {
		this.current = current;
	}
	public ArrayList<String> getLast() {
		return last;
	}
	public void setLast(ArrayList<String> last) {
		this.last = last;
	}
	
	
}
