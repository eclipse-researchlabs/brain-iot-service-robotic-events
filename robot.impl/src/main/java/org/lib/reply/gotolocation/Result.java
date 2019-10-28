package org.lib.reply.gotolocation;

public class Result {

	
	@Override
	public String toString() {
		return "Result [message=" + message + ", result=" + result + "]";
	}
	String message ="Cannot add procedure because goal frame is empty";
	String result ="error";
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	
	
}
