package test.export;

import test.helper.PamControllerTestHelper;
import clickDetector.ClickControl;
import clickDetector.ClickDetector;
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
	public void matFileTest() throws Exception {
		PamControllerTestHelper.InitializePamControllerForTesting();

		System.out.println("Matched template classifier test: match corr");

		//create a list of click detections.
		ClickControl control = new ClickControl("name");
		ClickDetector detector = new ClickDetector(control);
		ClickDetection clickDetection = new ClickDetection(0, 0, 0, detector, null, 0);

		//now open the mat file and check that we have all the data from these click detections. 
		
	}

}
