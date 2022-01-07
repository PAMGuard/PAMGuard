package clickTrainDetector.clickTrainAlgorithms.mht;

import PamguardMVC.PamDataUnit;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtvar.IDIManager;

/**
 * Standard MHT Chi2 provider manages the StandardMHTChi2 calculators associated with each possible track.
 * <p>
 * The standard chi^2 provider holds a list of subsequent ICI values between all clicks (on one channel). This is then used
 * to calculate ICI value for all possible tracks quickly. 
 * 
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class StandardMHTChi2Provider implements MHTChi2Provider<PamDataUnit> {
	
	/**
	 * Manager for calculation of ICI values. 
	 */
	private IDIManager iDIManager; 
	

	/**
	 * Parameters for the chi2 calculation 
	 */
	private StandardMHTChi2Params pamMHTChi2Params;

	/**
	 * Reference to the MHT kernel params. 
	 */
	private MHTKernelParams mhtKernelParams;


	public StandardMHTChi2Provider(MHTParams mhtParams) {
		pamMHTChi2Params=(StandardMHTChi2Params) mhtParams.chi2Params; 
		mhtKernelParams= mhtParams.mhtKernal; 
		iDIManager= new IDIManager(); 
	}

	public StandardMHTChi2Provider(StandardMHTChi2Params pamMHTChi2Params2, MHTKernelParams mHTkernalParams) {
		pamMHTChi2Params=pamMHTChi2Params2; 
		mhtKernelParams=mHTkernalParams; 
		iDIManager= new IDIManager(); 
	}

	@Override
	public void addDetection(PamDataUnit detection, int kcount) {
		iDIManager.addDetection(detection, kcount); 
	}

	@Override
	public MHTChi2<PamDataUnit> newMHTChi2(MHTChi2<PamDataUnit> mhtChi2) {
		if (mhtChi2==null) return new StandardMHTChi2(this); 
		return mhtChi2.cloneMHTChi2();
	}
	 
	/**
	 * Get the ICIManager. This handles calculation of inter click interval. 
	 * @return the ICI manager.
	 */
	public IDIManager getIDIManager() {
		return iDIManager;
	}
	
	/**
	 * Get the MHT Kernel parameters. 
	 * @return the MHT kernel parameters. 
	 */
	public MHTKernelParams getMHTKernelParams() {
		return mhtKernelParams;
	}
	
	
	/**
	 * Get the parameters for the standard 
	 * @return the standard MHT chi2 parameters. 
	 */
	public StandardMHTChi2Params getPamMHTChi2Params() {
		return pamMHTChi2Params;
	}

	public void setPamMHTChi2Params(StandardMHTChi2Params pamMHTChi2Params) {
		this.pamMHTChi2Params = pamMHTChi2Params;
	}


	/*************GUI*************/
	
	@Override
	public MHTChi2Params getSettingsObject() {
		return this.pamMHTChi2Params;
	}

	@Override
	public void clear() {
		iDIManager.clear(); 
	}

	/**
	 * Get the chi2 parameters. 
	 * @return returning the standard MHT chi2. 
	 */
	public StandardMHTChi2Params getParams() {
		return this.pamMHTChi2Params;
	}

	@Override
	public void printSettings() {
		getParams().printSettings(); 
	}

	@Override
	public void setMHTParams(MHTParams mhtParams) {
		this.pamMHTChi2Params=(StandardMHTChi2Params) mhtParams.chi2Params; 
		this.mhtKernelParams=mhtParams.mhtKernal; 
	}

	@Override
	public void clearKernelGarbage(int newRefIndex) {
		//need to reset the IDI manager to have new reference data units. 
		iDIManager.trimData(newRefIndex);
	}

	@Override
	public MHTChi2Params getChi2Params() {
		return getPamMHTChi2Params();
	}



}
