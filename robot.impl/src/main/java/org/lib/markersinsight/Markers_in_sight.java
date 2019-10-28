package org.lib.markersinsight;

import java.io.IOException;
import java.util.ArrayList;

import org.lib.availability.Availability;
import org.lib.availability.Header;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Markers_in_sight {
	public static void writeMarker()
    { 
  

		Markers_in_sight org = new Markers_in_sight(); 
  
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
	
	
	Header header =new Header();
	
	 ArrayList<Marker>  markers =new ArrayList<Marker> ();

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public ArrayList<Marker> getMarkers() {
		
		//Marker m  =new Marker();
		//ArrayList<Marker> markers =new ArrayList<Marker>();
		//markers.add(m);
		return markers;
	}

	public void setMarkers(ArrayList<Marker> markers) {
		this.markers = markers;
	}
	 
	 
	 
}
