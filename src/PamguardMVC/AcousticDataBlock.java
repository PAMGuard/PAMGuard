package PamguardMVC;

import PamController.PamControllerInterface;
import PamguardMVC.nanotime.NanosFromSamples;

/**
 * Class for all PAMGAURD data based on acoustic types of data, i.e. data which 
 * have a sample number (so not NMEA data, depth data, visual data, etc, but 
 * raw data, FFT data, detections, etc.). 
 * <p>
 * Includes some functions to relate channel numbers to hydrophone numbers
 * (which are messed up in the decimator) and functions to turn amplitudes
 * from raw units into dB re 1 mu Pa.  
 * 
 * @author Doug Gillespie
 *
 * @param <Tunit>
 */
abstract public class AcousticDataBlock <Tunit extends PamDataUnit> extends PamDataBlock<Tunit> {

	private ChannelListManager channelListManager = new DefaultChannelListManager();

	private AcousticDataBlock parentSourceData;

	public AcousticDataBlock(Class unitClass, String dataName,
			PamProcess parentProcess, int channelMap) {
		super(unitClass, dataName, parentProcess, channelMap);
		setNanoTimeCalculator(new NanosFromSamples());
	}

	private boolean initialisationComplete = false;

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			initialisationComplete = true;
			// no break so that this runs into the next case block. 
		case PamControllerInterface.CHANGED_PROCESS_SETTINGS:
			if (initialisationComplete) {
				findParentSource();
				prepareChannelLists();
				prepareAmplitudeCalculations();
			}
		}
	}

	protected void findParentSource() {
		parentSourceData = null;
		if (parentProcess == null) {
			return;
		}
		PamDataBlock parentData = parentProcess.getSourceDataBlock();
		if (parentData == null) {
			return;
		}
		if (AcousticDataBlock.class.isAssignableFrom(parentData.getClass())) {
			parentSourceData = (AcousticDataBlock) parentData;
		}
		else {
			System.out.println("Cannot assign " + parentData.getDataName() + " as AcousticData");
		}
	}

	protected void prepareAmplitudeCalculations() {
		//

	}

	protected void prepareChannelLists() {
		//		System.out.println(getDataName() + " prepareChannelLists");
		channelListManager = null;
		if (parentSourceData != null) {
			channelListManager = parentSourceData.getChannelListManager();
		}

	}

	public ChannelListManager getChannelListManager() {
		return channelListManager;
	}

	public AcousticDataBlock getParentSourceData() {
		if (parentSourceData == null) {
			findParentSource();
			prepareChannelLists();
			prepareAmplitudeCalculations();
		}
		return parentSourceData;
	}


}
