package beamformer;

import PamController.PamControlledUnit;
import beamformer.algorithms.BeamAlgorithmProvider;
import beamformer.algorithms.basicFreqDomain.BasicFreqDomBeamProvider;
import beamformer.algorithms.mvdr.MVDRProvider;

public class BeamFormerBaseControl extends PamControlledUnit {
	
	/**
	 * List of Beamformer algorithms available.  This is held as an array and
	 * not as an ArrayList to make it easier to reference in the parameters GUI (i.e.
	 * we can use the index position to identify which algorithm has been selected,
	 * as opposed to searching for a matching name).  The list is populated in
	 * the constructor.  New algorithms should be added TO THE END OF THE LIST, because
	 * otherwise old psf's which rely on the index position will point to the wrong
	 * algorithm.
	 */
	private BeamAlgorithmProvider[] algorithmList = new BeamAlgorithmProvider[2];

	private BeamFormerParams beamFormerParams = new BeamFormerParams();

	private BeamFormerBaseProcess beamFormerProcess;

	public BeamFormerBaseControl(String unitType, String unitName) {
		super(unitType, unitName);
		
		// create list of potential algorithm providers
//		algorithmList[0] = new NullBeamProvider();
		algorithmList[0] = new BasicFreqDomBeamProvider(this);
		algorithmList[1] = new MVDRProvider(this);
	}

	/**
	 * @return the beamFormerParams
	 */
	public BeamFormerParams getBeamFormerParams() {
		return beamFormerParams;
	}

	/**
	 * @param beamFormerParams the beamFormerParams to set
	 */
	public void setBeamFormerParams(BeamFormerParams beamFormerParams) {
		this.beamFormerParams = beamFormerParams;
	}
	
	/**
	 * @return the algorithmList
	 */
	public BeamAlgorithmProvider[] getAlgorithmList() {
		return algorithmList;
	}

	/**
	 * find an algorithm provider by name
	 * @param algoName Algorithm name
	 * @return algorithm provider or null if no matches. 
	 */
	public BeamAlgorithmProvider findAlgorithmByName(String algoName) {
		for (int i = 0; i < algorithmList.length; i++) {
			if (algorithmList[i].getStaticProperties().getName().equals(algoName)) {
				return algorithmList[i];
			}
		}
		return null;
	}
	/**
	 * @return the beamFormerProcess
	 */
	public BeamFormerBaseProcess getBeamFormerProcess() {
		return beamFormerProcess;
	}
	/**
	 * @return the beamFormerProcess
	 */
	public void setBeamFormerProcess(BeamFormerBaseProcess beamFormerProcess) {
		this.beamFormerProcess = beamFormerProcess;
	}


}
