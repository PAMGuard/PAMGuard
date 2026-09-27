package clickTrainDetector.clickTrainAlgorithms.adaptive;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;

import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfo;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfoLogging;

/**
 * Saves and retrieves {@link AdaptiveCTInfo} to and from the database.
 *
 * @author Jamie Macaulay
 */
public class AdaptiveCTInfoJSON extends CTAlgorithmInfoLogging {

	public static final String MEANCHI2FIELD = "ADAPTIVE_MEAN_CHI2";
	public static final String NCOASTSFIELD = "ADAPTIVE_NCOASTS";
	public static final String FEATURESFIELD = "ADAPTIVE_FEATURES";

	@Override
	public void writeJSONData(JsonGenerator jg, CTAlgorithmInfo algorithmInfo) {
		AdaptiveCTInfo info = (AdaptiveCTInfo) algorithmInfo;
		try {
			jg.writeNumberField(MEANCHI2FIELD, info.getMeanChi2());
			jg.writeNumberField(NCOASTSFIELD, info.getNCoasts());
			jg.writeStringField(FEATURESFIELD, concat(info.getFeatureNames()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public CTAlgorithmInfo createCTAlgorithmInfo(String algorithmType, JsonNode jTree) {
		double meanChi2 = 0;
		int nCoasts = 0;
		String[] features = new String[0];

		JsonNode na = jTree.findValue(MEANCHI2FIELD);
		if (na != null) {
			meanChi2 = na.asDouble();
		}
		na = jTree.findValue(NCOASTSFIELD);
		if (na != null) {
			nCoasts = na.asInt();
		}
		na = jTree.findValue(FEATURESFIELD);
		if (na != null && na.asText().length() > 0) {
			features = na.asText().split(",");
		}

		return new AdaptiveCTInfo(meanChi2, nCoasts, features);
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
