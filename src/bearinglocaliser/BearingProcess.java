package bearinglocaliser;

import PamController.PamController;
import PamView.GroupedSourceParameters;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamInstantProcess;
import PamguardMVC.PamObservable;
import bearinglocaliser.algorithms.BearingAlgorithm;
import bearinglocaliser.algorithms.BearingAlgorithmParams;
import bearinglocaliser.algorithms.BearingAlgorithmProvider;

public class BearingProcess extends PamInstantProcess {

	private BearingLocaliserControl bearingLocaliserControl;
		
	private PamDataBlock sourceDataBlock;
	
	private BearingDataBlock bearingDataBlock;
	
	private BearingAlgorithmGroup[] bearingAlgorithmGroups;

	private int nBearingGroups;
	
	public BearingProcess(BearingLocaliserControl bearingLocaliserControl) {
		super(bearingLocaliserControl);
		this.bearingLocaliserControl = bearingLocaliserControl;
		bearingDataBlock = new BearingDataBlock(bearingLocaliserControl, this, "Bearing Data");
		addOutputDataBlock(bearingDataBlock);
	}

	@Override
	public void pamStart() {
	}

	@Override
	public void pamStop() {
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#getRequiredDataHistory(PamguardMVC.PamObservable, java.lang.Object)
	 */
	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		/**
		 * Probably need to be smarter about this - especially when users are marking on a display, which 
		 * may be quite a long time after a sound has passed, however if we're beam forming on 
		 * automatic detectors, then there is no need to store anything beyond the length of a detection.
		 * Also, there is no need to store if the raw or fft data is held by the data unit.   
		 */
		if (bearingAlgorithmGroups == null) {
			return 0;
		}
		long maxReq = 0;
		for (int i = 0; i < bearingAlgorithmGroups.length; i++) {
			if (bearingAlgorithmGroups[i] == null) {
				continue;
			}
			maxReq = Math.max(maxReq, bearingAlgorithmGroups[i].bearingAlgorithm.getRequiredDataHistory(o, arg));
		}
		return maxReq;
	}
	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#prepareProcess()
	 */
	@Override
	public void prepareProcess() {
		super.prepareProcess();
	}

	/**
	 * Set the main data block this process is going to 
	 * subscribe t ofor data input. thi smay be raw or fft data. 
	 */
	private void setSourceDataBlock() {
		
		BearingLocaliserParams params = bearingLocaliserControl.getBearingLocaliserParams();
		sourceDataBlock = PamController.getInstance().getDataBlockByLongName(params.getDataSource());
		setParentDataBlock(sourceDataBlock);
	
	}

	/**
	 * Organise bearing groups - one for each channel group
	 * that is to be localised. 
	 */
	public void prepareBearingGroups() {
		
		setSourceDataBlock();
		
		BearingLocaliserParams params = bearingLocaliserControl.getBearingLocaliserParams();
		GroupedSourceParameters groupedParams = params.getRawOrFFTSourceParameters();
		nBearingGroups = groupedParams.countChannelGroups();
		bearingAlgorithmGroups = new BearingAlgorithmGroup[nBearingGroups];
		for (int i = 0; i < nBearingGroups; i++) {
			int groupChannels = groupedParams.getGroupChannels(i);
			String algorithmName = params.getAlgorithmName(i);
			BearingAlgorithmProvider algorithmProvider = bearingLocaliserControl.findAlgorithmByName(algorithmName);
			if (algorithmProvider == null) {
				System.err.println("Unable to find bearing algorithm: " + algorithmName);
				return;
			}
			BearingAlgorithmParams algoParams = params.getAlgorithmParms(i, groupChannels, algorithmName);
//			System.out.printf("Preparing algorithm %s for channels %d\n", algorithmName, groupChannels);
			if (algoParams == null) {
				algoParams = algorithmProvider.createNewParams(i, groupChannels);
			}
			BearingAlgorithm bearingAlgorithm = algorithmProvider.createAlgorithm(this, algoParams, i);
			bearingAlgorithm.prepare();
			bearingAlgorithmGroups[i] = new BearingAlgorithmGroup(i, groupChannels, algorithmProvider, bearingAlgorithm);
		}
		bearingDataBlock.setChannelMap(groupedParams.getChanOrSeqBitmap());
		
		bearingLocaliserControl.getConfigObservable().updateSettings();
	}
	
	/**
	 * Find a bearing algorithm group - everything we need to know about the 
	 * processor for a specific channel. 
	 * @param anyChannelMap any one or more channels associated with the group. 
	 * @return group information. 
	 */
	private BearingAlgorithmGroup findAlgorithmGroup(int anyChannelMap) {
		if (bearingAlgorithmGroups == null) {
			return null;
		}
		for (int i = 0; i < bearingAlgorithmGroups.length; i++) {
			if (bearingAlgorithmGroups[i] == null) {
				continue;
			}
			if ((bearingAlgorithmGroups[i].channelMap & anyChannelMap) != 0) {
				return bearingAlgorithmGroups[i];
			}
		}
		return null;
	}

	public void estimateBearings(PamDataUnit pamDataUnit) {
		BearingAlgorithmGroup bag = findAlgorithmGroup(pamDataUnit.getChannelBitmap());
		if (bag == null) {
			return;
		}
		if (bearingLocaliserControl.getBearingLocaliserParams().doAllGroups) {
			for (int i = 0; i < bearingAlgorithmGroups.length; i++) {
				if (bearingAlgorithmGroups[i] == bag) {
					continue;
				}
				TempDataUnit tempDataUnit = new TempDataUnit(pamDataUnit, bearingAlgorithmGroups[i].channelMap);
				if (estimateBearings(tempDataUnit, bearingAlgorithmGroups[i])) {
					bearingDataBlock.addPamData(tempDataUnit);
				}
			}
		}
		if (estimateBearings(pamDataUnit, bag)) {
			bearingDataBlock.addPamData(pamDataUnit);
		}
	}

	private boolean estimateBearings(PamDataUnit pamDataUnit, BearingAlgorithmGroup bag) {
		return bag.bearingAlgorithm.process(pamDataUnit, sourceDataBlock.getSampleRate(), bag);
	}

	/**
	 * @return the bearingLocaliserControl
	 */
	public BearingLocaliserControl getBearingLocaliserControl() {
		return bearingLocaliserControl;
	}

	/**
	 * @return the bearingAlgorithmGroups
	 */
	public BearingAlgorithmGroup[] getBearingAlgorithmGroups() {
		return bearingAlgorithmGroups;
	}

	@Override
	public PamDataBlock getSourceDataBlock() {
		return sourceDataBlock;
	}

}
