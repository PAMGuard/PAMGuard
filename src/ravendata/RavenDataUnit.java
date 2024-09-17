package ravendata;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import PamDetection.PamDetection;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.PamDataUnit;

public class RavenDataUnit extends PamDataUnit implements AcousticDataUnit, PamDetection {
	
	private HashMap<String, String> extraData;

	public RavenDataUnit(long timeMilliseconds, int channelMap, long durationMillis, double f1, double f2) {
		super(timeMilliseconds);
		setChannelBitmap(channelMap);
		setDurationInMilliseconds(durationMillis);
		double[] freq = {f1, f2};
		setFrequency(freq);
	}

	public HashMap<String, String> getExtraData() {
		return extraData;
	}

	public void setExtraData(HashMap<String, String> extraData) {
		this.extraData = extraData;
	}

	@Override
	public String getSummaryString() {
		String base = super.getSummaryString();
		if (extraData == null) {
			return base;
		}
		Set<Entry<String, String>> entries = extraData.entrySet();
		for (Entry<String, String> e : entries) {
			base += String.format("<br>%s: %s", e.getKey(), e.getValue());
		}
		
		return base;
	}



}
