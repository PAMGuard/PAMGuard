package performanceTests;

import PamController.PamguardVersionInfo;

public class PamguardInfo implements PerformanceTest {

	private String resultString;
	
	@Override
	public String getName() {
		return "PAMGUARD Information";
	}

	@Override
	public String getResultString() {
		return resultString;
	}
	
	@Override
	public void cleanup() {
		
	}

	@Override
	public boolean runTest() {
		resultString = String.format("PAMGUARD version %s branch %s", PamguardVersionInfo.version,
				PamguardVersionInfo.getReleaseType());
		return true;
	}

}
