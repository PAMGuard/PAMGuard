package clickTrainDetector.clickTrainAlgorithms.mht;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import PamguardMVC.debug.Debug;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfo;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfoLogging;

/**
 * Logging for the StandardMHTChi2Info used with an MHT algorithm. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class StandardMHTChi2InfoJSON extends CTAlgorithmInfoLogging {

	
	
	public final static String CHI2NAMEFIELD = "MHT_CHI2_NAME"; 
	public final static String CHI2VALFIELD = "MHT_CHI2_VAR"; 


	@Override
	public void writeJSONData(JsonGenerator jg, CTAlgorithmInfo algorithmInfo) {
		StandardMHTChi2Info standardMHTChi2Info = (StandardMHTChi2Info) algorithmInfo; 
		//get the algorithm to write it's own data. 
		//Note that all algorithms will need to write which MHTChi2 provider they sued. 
		try {
			jg.writeStringField(MHTAlgorithmInfoJSON.MHTCHI2TYPEFIELD, standardMHTChi2Info.getMhtChi2Type().toString());
			jg.writeStringField(CHI2NAMEFIELD, concatString(standardMHTChi2Info.getMhtChi2Names()));
			writeJsonArray(jg, CHI2VALFIELD, standardMHTChi2Info.getMhtChi2Chi2Vals());
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public StandardMHTChi2Info createCTAlgorithmInfo(String algorithmType, JsonNode jTree) {
		JsonNode na = jTree.get(CHI2NAMEFIELD); 
		String[] names; 
		double[] chi2vals; 
		if (na!=null) {
			names = na.asText().split(","); 
		}
		else {
			Debug.err.println("StandardMHTChi2InfoJSON: Could not find " + CHI2NAMEFIELD + " field in JSON"); 
			return null; 
		}

		 na = jTree.findValue(CHI2VALFIELD);
		if (na != null && ArrayNode.class.isAssignableFrom(na.getClass())) {
			chi2vals = unpackJsonArray((ArrayNode) na);
		}
		else {
			Debug.err.println("StandardMHTChi2InfoJSON: Could not unpack " + CHI2VALFIELD + " array field in JSON"); 
			return null; 
		}

		StandardMHTChi2Info standardMHTChi2Info = new StandardMHTChi2Info(names, chi2vals ); 
		
		return standardMHTChi2Info;
	}

	private String concatString(String[] stArray) {
		String concat = ""; 
		for (int i=0; i<stArray.length; i++){
			if (i<stArray.length-1) concat += stArray[i] + ","; 
			else concat += stArray[i]; 
		}
		return concat; 
	}


}
