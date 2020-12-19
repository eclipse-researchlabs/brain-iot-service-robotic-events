package org.lib.request.uncharge.querystate;

import java.io.IOException;

import org.lib.request.gotolocation.cancel.CancelGoTo;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class QueryState {

	public static void main(String[] a) 
    { 
  

		QueryState org = new QueryState(); 
  
	 ObjectMapper mapper = new ObjectMapper();
      
		try {
			mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

			
			//Convert object to JSON string
			String 			jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(org);
			System.out.println(jsonInString);
			
			
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

    } 
	
	QuerystateHeader header =new QuerystateHeader();

	public QuerystateHeader getHeader() {
		return header;
	}

	public void setHeader(QuerystateHeader header) {
		this.header = header;
	}
	
	
	
	
}
