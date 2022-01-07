package clickTrainDetector.clickTrainAlgorithms.mht;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import PamguardMVC.debug.Debug;
import clickTrainDetector.classification.CTClassifierType;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfo;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfoLogging;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTChi2ProviderManager.MHTChi2Type;

/**
 * Class for unpacking algorithm strings.
 * 
 * @author Jamie Macaulay 
 *
 */
public class MHTAlgorithmInfoJSON extends CTAlgorithmInfoLogging {
	
	public static final String MHTCHI2TYPEFIELD = "MHT_CHI2_TYPE"; 

	private MHTClickTrainAlgorithm mhtClickTrainAlgorithm;


	public MHTAlgorithmInfoJSON(MHTClickTrainAlgorithm mhtClickTrainAlgorithm) {
		this.mhtClickTrainAlgorithm=mhtClickTrainAlgorithm; 
	}
	

	@Override
	public void writeJSONData(JsonGenerator jg, CTAlgorithmInfo algorithmInfo) {
		//Do not think this is actually ever used but could be. 
		//get the algorithm to write it's own data. 
		//Note that all algorithms will need to write which MHTChi2 provider they sued. 
		try {
			jg.writeStringField(MHTCHI2TYPEFIELD, ((MHTAlgorithmInfo) algorithmInfo).getMhtChi2Type().toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		algorithmInfo.getCTAlgorithmLogging().writeJSONData(jg, algorithmInfo);
	}

	@Override
	public MHTAlgorithmInfo createCTAlgorithmInfo(String algorithmType, JsonNode jTree) {
		JsonNode na = jTree.findValue(MHTCHI2TYPEFIELD);
		
		
		MHTChi2Type clssfrType = null; 
		if (na != null ) {
			String type = na.textValue();
			clssfrType = MHTChi2Type.valueOf(type);
		}
				
		if (clssfrType!=null) {
		return (MHTAlgorithmInfo) mhtClickTrainAlgorithm.getChi2ProviderManager().
				getCTAlgorithmLogging(clssfrType).createCTAlgorithmInfo(algorithmType, jTree); 
		}
		else {
			Debug.err.println("MHTAlgorithmInfoJSON: No MHTChi2Type"); 
			return null;
		}
		
	}

}
