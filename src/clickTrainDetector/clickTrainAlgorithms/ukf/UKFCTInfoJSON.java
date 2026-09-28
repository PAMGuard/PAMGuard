package clickTrainDetector.clickTrainAlgorithms.ukf;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfo;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfoLogging;

/**
 * Saves and retrieves {@link UKFCTInfo} to and from the database.
 *
 * @author Jamie Macaulay
 */
public class UKFCTInfoJSON extends CTAlgorithmInfoLogging {

	public static final String ICIFIELD = "UKF_FINAL_ICI";
	public static final String NCLICKSFIELD = "UKF_NCLICKS";
	public static final String FEATURESFIELD = "UKF_FEATURES";

	@Override
	public void writeJSONData(JsonGenerator jg, CTAlgorithmInfo algorithmInfo) {
		UKFCTInfo info = (UKFCTInfo) algorithmInfo;
		try {
			jg.writeNumberField(ICIFIELD, info.getFinalICI());
			jg.writeNumberField(NCLICKSFIELD, info.getNClicks());
			jg.writeStringField(FEATURESFIELD, concat(info.getFeatureNames()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public CTAlgorithmInfo createCTAlgorithmInfo(String algorithmType, JsonNode jTree) {
		double ici = 0;
		int nClicks = 0;
		String[] features = new String[0];

		JsonNode na = jTree.findValue(ICIFIELD);
		if (na != null) {
			ici = na.asDouble();
		}
		na = jTree.findValue(NCLICKSFIELD);
		if (na != null) {
			nClicks = na.asInt();
		}
		na = jTree.findValue(FEATURESFIELD);
		if (na != null && na.asText().length() > 0) {
			features = na.asText().split(",");
		}

		return new UKFCTInfo(ici, nClicks, features);
	}

	private static String concat(String[] strings) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < strings.length; i++) {
			sb.append(strings[i]);
			if (i < strings.length - 1) {
				sb.append(",");
			}
		}
		return sb.toString();
	}

}
