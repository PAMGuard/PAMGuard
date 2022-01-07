package clickDetector;

import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryTypes;
import clickDetector.background.ClickBackgroundDataUnit;
import jsonStorage.JSONObjectDataSource;

public class ClickBackgroundJSONDataSource extends JSONObjectDataSource<ClickBackgroundJSONData> {

	/**
	 * Call the super constructor and then initialize the objectData object as
	 * a ClickJSONData class
	 */
	public ClickBackgroundJSONDataSource() {
		super();
		objectData = new ClickBackgroundJSONData();
	}

	
	@Override
	protected void addClassSpecificFields(PamDataUnit pamDataUnit) {
		ClickBackgroundDataUnit cbdu = (ClickBackgroundDataUnit) pamDataUnit;

		// write background levels, if they exist
		double[] levels = null;
		levels = cbdu.getLevels();
		if (levels != null) {
			objectData.backGround = new double[levels.length];
			objectData.noiseLen = levels.length;
			for (int i = 0; i < levels.length; i++) {
				objectData.backGround[i] = levels[i];
			}
		}
	}

	@Override
	protected void setObjectType(PamDataUnit pamDataUnit) {
		objectData.identifier = BinaryTypes.BACKGROUND_DATA;
	}

}
