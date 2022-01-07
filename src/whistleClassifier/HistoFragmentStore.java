package whistleClassifier;

import pamMaths.PamHistogram;
import whistleDetector.WhistleDetector;

/**
 * Doesnt' store fragemnts individually - just adds their data to a series of
 * histograms and whenever the parameters are requried, it estimates those
 * parameters based on the contents of the histograms.
 * 
 * @author Douglas Gillespie
 *
 */
public class HistoFragmentStore extends BasicFragmentStore {


//	private WhistleClassifierProcess whistleClassifierProcess;
	private PamHistogram[] fitHistograms = new PamHistogram[3];
	private PamHistogram posInflections, negInflections;

	
	public HistoFragmentStore(float sampleRate) {
		super(sampleRate);
		fitHistograms[0] = new PamHistogram(0, Math.max(1, sampleRate/2), 96);
		fitHistograms[1] = new PamHistogram(-100000, 100000, 100);
		fitHistograms[2] = new PamHistogram(-1000000, 1000000, 100);
		fitHistograms[0].setName("Mean frequency");
		fitHistograms[1].setName("Fit slope");
		fitHistograms[2].setName("Fit quadratic term");
		posInflections = new PamHistogram(0, 1, 2, true);
		posInflections.setName("Positive inflections");
		negInflections = new PamHistogram(0, 1, 2, true);
		negInflections.setName("Negative inflections");
		
		prepareStore();
	}
	

	@Override
	public void setSampleRate(float sampleRate) {
		// only call prepareStore to reset params if the sample rate has actually changed
		if (super.getSampleRate()!=sampleRate) {
			super.setSampleRate(sampleRate);
			prepareStore();
		}
	}


	@Override
	public void addFragemnt(WhistleFragment newFragment) {

		super.addFragemnt(newFragment);
//		fragments.add(newFragment);
		
		double[] params = getLatestParams();
//		fragmentParams.add(params);
		
//		double[] shapeFit = newFragment.getShapeFit();
		for (int i = 0; i < 3; i++) {
			fitHistograms[i].addData(params[i], true);
		}
//		posInflections.addData(newFragment.getPosInflections());
//		negInflections.addData(newFragment.getNegInflections());
//
	}

	/**
	 * Called at run start, takes some basic parameters from the whistle 
	 * detector and sets up histogram bins accordingly.
	 *
	 */
	@Override
	public void prepareStore() {
		super.prepareStore();
		if (fitHistograms != null && fitHistograms[0] != null) {
		fitHistograms[0].setRange(0, getSampleRate()/2, 96);
		// the max sweep should be something similar to that set in the
		// whistle detector parameters, so try to find those.
		WhistleDetector whistleDetector;
		clearStore();
		
//		try {
//			whistleDetector = (WhistleDetector) whistleClassifierProcess.getParentProcess();
//		}
//		catch (Exception ex) {
////			ex.printStackTrace();
//			whistleDetector = null;
//		}		
//		if (whistleDetector != null) {
//			WhistleParameters whistleParameters = whistleDetector.getWhistleControl().getWhistleParameters();
//			double maxSweep = whistleParameters.maxDF * 1.5;
//			fitHistograms[1].setRange(-maxSweep, maxSweep, 100);
//			double maxBend = whistleParameters.maxD2F * 5;
//			fitHistograms[2].setRange(-maxBend, maxBend, 100);
//		}
		}
	}
	@Override
	public void clearStore() {

		super.clearStore();
		
		for (int i = 0; i < 3; i++) {
			fitHistograms[i].clear();
		}
		posInflections.clear();
		negInflections.clear();
		
	}

//	public void scaleStoreData(double scaleFactor) {
//		super.scaleStoreData(scaleFactor);
//		for (int i = 0; i < 3; i++) {
//			fitHistograms[i].scaleData(scaleFactor);
//		}
//		negInflections.scaleData(scaleFactor);
//		posInflections.scaleData(scaleFactor);
//		
//	}



	@Override
	public PamHistogram getFitHistogram(int iFit) {

		return fitHistograms[iFit];
	}

	@Override
	public PamHistogram getNegInflectionsHistogram() {
		return negInflections;
	}

	@Override
	public PamHistogram getPosInflectionsHistogram() {
		return posInflections;
	}


}
