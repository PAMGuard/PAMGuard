package clickTrainDetector.clickTrainAlgorithms;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import PamguardMVC.debug.Debug;
import clickTrainDetector.ClickTrainControl;

/**
 * Handles algorithm info logging. 
 * @author Jamie Macaulay 
 *
 */
public class CTAlgorithmInfoManager {

	private ClickTrainControl clickTrainControl;

	/**
	 * 
	 */
	public CTAlgorithmInfoManager(ClickTrainControl clickTrainControl) {
		this.clickTrainControl=clickTrainControl; 
	}

	/**
	 * Get CT algorithm logging depending on the algorithm., 
	 * @return the CTAlgorithmInfo or null. 
	 */
	public CTAlgorithmInfo getAlgorithmInfo(String jsonString) {
		if (jsonString == null) {
			return null;
		}
		// now try to parse it back into numbers.... 
		String algorithmType; 
		try {
			ObjectMapper om = new ObjectMapper();
			JsonNode jTree = om.readTree(new ByteArrayInputStream(jsonString.getBytes()));
			//					JsonNode nv = jTree.findValue("NAME");

			algorithmType = CTAlgorithmInfoLogging.getAlgorithmType(jTree); 

		}
		catch (IOException e) {
			System.err.println("Classification interpreting " + jsonString);
			return null;
		}
		
		

		CTAlgorithmInfoLogging algorithmLogging = null;
		for (int i = 0; i<clickTrainControl.getClickTrainAlgorithms().size(); i++) {

			if (clickTrainControl.getClickTrainAlgorithms().get(i).getName().equals(algorithmType)) {
				algorithmLogging = clickTrainControl.getClickTrainAlgorithms().get(i).getCTAlgorithmInfoLogging(); 
			}
		}
		
		
		if (algorithmLogging!=null) {
			return algorithmLogging.createCTAlgorithmInfo(jsonString); 
		}
		else {
			Debug.err.println("CTAlgorithmInfoManager: Could not find algorithm information"); 
			return null;
		}
	}


	//TODO- xml logging for each algorithm. 



}
