package bearinglocaliser.beamformer;

import java.util.ArrayList;
import java.util.List;

import beamformer.BeamFormerBaseControl;
import beamformer.BeamFormerBaseProcess;
import beamformer.BeamGroupProcess;
import beamformer.algorithms.BeamAlgorithmProvider;
import bearinglocaliser.BearingLocaliserControl;
import bearinglocaliser.algorithms.BearingAlgorithmParams;
import bearinglocaliser.algorithms.BearingAlgorithmProvider;
import fftManager.FFTDataBlock;

/**
	 * Class to wrap the beam former control system and extract from it 
	 * the algorithms that can create beamograms and then wrap each of those
	 * up so that they become available to the more generic bearing localiser.  
	 * 
	 * This class contains most of the functionality that was in the earlier
	 * BeamFormLocaliser module. 
	 */
public class BeamFormBearingWrapper extends BeamFormerBaseControl {

	private BearingLocaliserControl bearingLocaliserControl;
	
	private WrappedBeamFormerProcess wrappedBeamFormerProcess;
	
	private WrappedBeamFormAlgorithm[] wrappedAlgorithms;
	
	public BeamFormBearingWrapper(BearingLocaliserControl bearingLocaliserControl) {
		super("BeamFormerBase", bearingLocaliserControl.getUnitName());
		this.bearingLocaliserControl = bearingLocaliserControl;
		wrappedBeamFormerProcess = new WrappedBeamFormerProcess(this, bearingLocaliserControl, false);
		setBeamFormerProcess(wrappedBeamFormerProcess);
//		wrappedAlgorithm = new WrappedBeamFormAlgorithm(wrappedBeamFormerProcess, bearingLocaliserControl.getBearingProcess(), null, 0)
	}
	
	public List<BearingAlgorithmProvider> getWrappedAlgorithms() {
		
		BeamAlgorithmProvider[] algoList = this.getAlgorithmList();
		ArrayList<BearingAlgorithmProvider> list = new ArrayList<>();
		for (int i = 0; i < algoList.length; i++) {
			if (algoList[i].getStaticProperties().isCanBeamogram()) {
				list.add(new WrappedBeamFormAlgorithmProvider(this, algoList[i]));
			}
		}
		return list;
	}

	/**
	 * @return the bearingLocaliserControl
	 */
	public BearingLocaliserControl getBearingLocaliserControl() {
		return bearingLocaliserControl;
	}

	public void sortBeamFormers(BearingAlgorithmParams algorithmParams, int groupIndex) {
		/*
		 * Real mess and making me think this wasn't a good idea !
		 * Need to copy all the params from the bearing loc params to the 
		 * bf params, then set up the beamformer process as normal to handle the data. 
		 */
		/*
		 * Before we can do anything at all, need to set the correct FFT finder in the base process
		 * this is awkward, since we've not set it until we've made the bearing algorithm, but can't make
		 * the algorithm until we've made the beam algorith, bu th ebeam algorithm need the fft source !!!!!
		 */
		wrappedBeamFormerProcess.sortFFTBlock();
		
	}

	/**
	 * @return the wrappedBeamFormerProcess
	 */
	public WrappedBeamFormerProcess getWrappedBeamFormerProcess() {
		return wrappedBeamFormerProcess;
	}


}
