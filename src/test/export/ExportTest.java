package test.export;

import static org.junit.jupiter.api.Assertions.assertEquals;


import org.junit.jupiter.api.Test;

import clickDetector.ClickDetection;


/**
 * Tests for export functionality.
 */
public class ExportTest {
	
	
	/**
	 * Test exporting detections to mat files. 
	 */
	@Test
	public void matFileTest() {
		
		System.out.println("Matched template classifier test: match corr");
		
		//create a list of click detections. 
		ClickDetection clickDetection = new ClickDetection(0, 0, 0, null, null, 0); 
		
		
		//now open the mat file and check that we have all the data from these click detections. 
		
	}

}
