package landMarks;

import PamguardMVC.PamDataUnit;

public class LandmarkDataUnit extends PamDataUnit {

	private LandmarkData landmarkData;

	public LandmarkDataUnit(long timeMilliseconds, LandmarkData landmarkData) {
		super(0);
		this.landmarkData = landmarkData;
	}

	public LandmarkData getLandmarkData() {
		return landmarkData;
	}

	public void setLandmarkData(LandmarkData landmarkData) {
		this.landmarkData = landmarkData;
	}

}
