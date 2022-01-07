package clickDetector;

import PamguardMVC.PamDataUnit;
import jsonStorage.JSONObjectDataSource;

public class ClickJSONDataSource extends JSONObjectDataSource<ClickJSONData> {

	/**
	 * Call the super constructor and then initialize the objectData object as
	 * a ClickJSONData class
	 */
	public ClickJSONDataSource() {
		super();
		objectData = new ClickJSONData();
	}

	
	@Override
	protected void addClassSpecificFields(PamDataUnit pamDataUnit) {
		ClickDetection cd = (ClickDetection) pamDataUnit;

		// write the first set of variables that are always present
		objectData.triggerMap = cd.triggerList;
		objectData.type = cd.getClickType();
		objectData.flags = cd.getClickFlags();
		
		// write angles and angle errors, if they exist
		double[] angles = null;
		double[] angleErrors = null;
		if (cd.getLocalisation() != null) {
			angles = cd.getLocalisation().getAngles();
			angleErrors = cd.getLocalisation().getAngleErrors();
		}
		if (angles != null) {
			objectData.angles = new double[angles.length];
			for (int i = 0; i < angles.length; i++) {
				objectData.angles[i] = angles[i];
			}
		}
		if (angleErrors != null) {
			objectData.angleErrors = new double[angleErrors.length];
			for (int i = 0; i < angleErrors.length; i++) {
				objectData.angleErrors[i] = angleErrors[i];
			}
		}
		
		// copy over the duration and channelmap from the superclass fields (matches the Matlab code)
		// note that we're casting the Long sampleDuration to an int, which is ok in this case because
		// a click will never be long enough to overflow an int
		objectData.duration = Math.toIntExact((long) objectData.getSampleDuration());
		objectData.nChan = PamUtils.PamUtils.getNumChannels(objectData.getChannelMap());

		// write out the wav data
		double[][] waveData = cd.getWaveData();
		if (waveData==null) return;
		objectData.wave = new double[objectData.duration][objectData.nChan];
		for (int i = 0; i < objectData.duration; i++) {
			for (int j = 0; j < objectData.nChan; j++) {
				// copy over the wav data, but swap the order because that's what Matlab does
				objectData.wave[i][j] = waveData[j][i];
			}
		}

		
	}

	@Override
	protected void setObjectType(PamDataUnit pamDataUnit) {
		objectData.identifier = ClickBinaryDataSource.CLICK_DETECTOR_CLICK;
	}

	
	
	
}
