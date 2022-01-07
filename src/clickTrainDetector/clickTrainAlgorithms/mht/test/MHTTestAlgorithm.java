package clickTrainDetector.clickTrainAlgorithms.mht.test;

import PamguardMVC.PamDataUnit;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTChi2;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTKernel;
import clickTrainDetector.clickTrainAlgorithms.mht.MHTKernelParams;
import clickTrainDetector.clickTrainAlgorithms.mht.StandardMHTChi2;
import clickTrainDetector.clickTrainAlgorithms.mht.StandardMHTChi2Params;
import clickTrainDetector.clickTrainAlgorithms.mht.StandardMHTChi2Provider;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtMAT.SimpleClick;

/**
 * Simple implementation of click train detector which tests the MHT kernel and a simple chi^2 algorithm. 
 * 
 * @author Jamie Macaulay
 *
 */
public class MHTTestAlgorithm {
	
	/**
	 * The MHT Kernel.
	 */
	protected MHTKernel<PamDataUnit> mhtKernel;
	
	/**
	 * The chi^2 calculator. 
	 */
	protected StandardMHTChi2Provider mhtChi2;
	
	/**
	 * Standard MHT params.
	 */
	private StandardMHTChi2Params pamMHTChi2Params = new StandardMHTChi2Params(); 
	
	/**
	 * The MHTKernal params. 
	 */
	private MHTKernelParams mHTkernalParams= new MHTKernelParams(); 
	
	public MHTTestAlgorithm() {
		
		//bit of a hack but remove bearing and correlation. 
		pamMHTChi2Params.enable= new boolean[] {true, true, false, true, false, false}; 
		pamMHTChi2Params.maxICI=0.5; 
		pamMHTChi2Params.newTrackPenalty=50;
		pamMHTChi2Params.newTrackN=2; 
		pamMHTChi2Params.longTrackExponent = 0.1; 
		pamMHTChi2Params.lowICIExponent = 0.2;

		pamMHTChi2Params.coastPenalty=5;
		mHTkernalParams.nPruneback=5; 
		mHTkernalParams.nPruneBackStart=7; 
		mHTkernalParams.maxCoast=5; 
		mHTkernalParams.nHold=50; 

		
		mhtChi2 =  new StandardMHTChi2Provider(pamMHTChi2Params,  mHTkernalParams); 
		mhtKernel = new MHTKernel<PamDataUnit>(mhtChi2);
		mhtKernel.setMHTParams(mHTkernalParams);
		
		//pamMHTChi2Params.printSettings();
	}
	
	/**
	 * Print the settings 
	 */
	public void printSettings() {
		pamMHTChi2Params.printSettings(); 
		mHTkernalParams.printSettings();
	}
	
	/**
	 * Add a simple click to the click train. This must be sequential. 
	 */
	public void addSimpleClick(SimpleClick simpleClick) {
//		System.out.println("New detection: -----------------------: " +simpleClick.getStartSample()); 
//		if (mhtKernel.getKCount()>10) {
//			return;
//		}
		mhtKernel.addDetection(simpleClick);
	}

	/**
	 * Clear the MHT kernel. 
	 */
	public void clearKernel() {
		mhtKernel.clearKernel();	
		this.mhtChi2.clear();
	}

	public MHTKernel<PamDataUnit> getMHTKernel() {
		return this.mhtKernel;
	}

}
