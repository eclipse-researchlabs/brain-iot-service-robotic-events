package org.lib.reply.chargecomponent;

public class State {

	Addheader header =new Addheader();

    String current_state ="";
    
    String last_event ="";

	public Addheader getHeader() {
		return header;
	}

	public void setHeader(Addheader header) {
		this.header = header;
	}

	public String getCurrent_state() {
		return current_state;
	}

	public void setCurrent_state(String current_state) {
		this.current_state = current_state;
	}

	public String getLast_event() {
		return last_event;
	}

	public void setLast_event(String last_event) {
		this.last_event = last_event;
	}
    
    
	
}
