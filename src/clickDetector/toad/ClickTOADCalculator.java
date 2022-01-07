package clickDetector.toad;

import java.awt.Window;
import java.util.List;

import Array.SnapshotGeometry;
import Localiser.DelayMeasurementParams;
import Localiser.algorithms.DelayGroup;
import Localiser.controls.TOADTimingPane;
import Localiser.controls.TOADTimingParams;
import PamController.SettingsPane;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.toad.GenericTOADCalculator;
import clickDetector.ClickControl;
import clickDetector.ClickDataBlock;
import clickDetector.ClickDetection;
import clickDetector.ClickDetector;
import clickDetector.ClickParameters;
import clickDetector.dialogs.ClickDelayPanel;
import fftManager.fftorganiser.FFTDataOrganiser;
import fftManager.fftorganiser.FFTInputTypes;
import group3dlocaliser.algorithm.toadbase.TOADInformation;
import pamViewFX.fxNodes.TitledSettingPane;
import pamViewFX.fxNodes.pamDialogFX.ManagedSettingsPane;

public class ClickTOADCalculator extends GenericTOADCalculator {

	private ClickControl clickControl;
	
	private ClickDetector clickDetector;
	
	private ClickDataBlock clickDataBlock;

	private ClickFFTOrganiser fftOrganiser;
	
//	private DelayGroup delayGroup;
	
	/**
	 * @param clickControl
	 */
	public ClickTOADCalculator(ClickControl clickControl) {
		super(clickControl);
		this.clickControl = clickControl;
		this.clickDetector = clickControl.getClickDetector();
		this.clickDataBlock = clickDetector.getClickDataBlock();
		fftOrganiser = new ClickFFTOrganiser(clickControl);
//		delayGroup = new 
		this.setFftDataOrganiser(fftOrganiser);
		setCanUseEnvelope(true);
		setCanUseLeadingEdge(true);
		fftOrganiser.setOnlyAllowedDataBlock(clickDataBlock);
		setupCalculator();
	}
	
	public void setupCalculator() {
		ClickParameters clickParams = clickControl.getClickParameters();
		int fftLength = PamUtils.getMinFftLength(clickParams.maxLength);
		fftOrganiser.setFftLength(fftLength);
		fftOrganiser.setFftHop(fftLength); // should never hop anyway !
		fftOrganiser.setInput(clickDataBlock, FFTInputTypes.FFTDataHolder);
		setTimingSource(clickDataBlock);
	}

	@Override
	public TOADInformation getTOADInformation(List<PamDataUnit> dataUnits, double sampleRate, int channelMap, SnapshotGeometry geometry) {
		/**
		 * Get the click specific delay measurement params. 
		 */
		DelayMeasurementParams dmp = null;
		for (PamDataUnit dataUnit:dataUnits) {
			if (dataUnit instanceof ClickDetection == false) {
				continue;
			}
			ClickDetection click = (ClickDetection) dataUnit;
			dmp = clickControl.getClickDetector().getDelayMeasurementParams(click);
			if (click.getClickType() > 0) {
				break;
			}
		}
		if (dmp != null) {
			fftOrganiser.setDelayParams(dmp);
			// may also need to set a bigger FFT length in the organiser to handle upsampling
			int upSamp = dmp.getUpSample();
			ClickParameters clickParams = clickControl.getClickParameters();
			int fftLength = PamUtils.getMinFftLength(clickParams.maxLength * upSamp);
			fftOrganiser.setFftLength(fftLength);
			fftOrganiser.setFftHop(fftLength/2); // should never hop anyway !
		}
		return super.getTOADInformation(dataUnits, sampleRate, channelMap, geometry);
	}
//	/* (non-Javadoc)
//	 * @see PamguardMVC.toad.GenericTOADCalculator#getTOADInformation(java.util.List, int, Array.SnapshotGeometry)
//	 */
//	@Override
//	public TOADInformation getTOADInformation(List<PamDataUnit> dataUnits, int channelMap, SnapshotGeometry geometry) {
//		if (dataUnits == null || dataUnits.isEmpty()) {
//			return null;
//		}
////		/*
////		 * work out what delay measurement params to use based on click type
////		 */
//		delayMeasurementParams = null;
//		int totChanMap = 0;
//		for (PamDataUnit dataUnit:dataUnits) {
//			totChanMap |= dataUnit.getChannelBitmap();
//		}
//		totChanMap &= channelMap;
//		for (PamDataUnit dataUnit:dataUnits) {
//			ClickDetection click = (ClickDetection) dataUnit;
//			if (click.getClickType() > 0) {
//				delayMeasurementParams = clickDetector.getDelayMeasurementParams(click);
//				break;
//			}
//		}
//		int nChan = PamUtils.getNumChannels(totChanMap);
//		double[][] wavData = new double[totChanMap][];
//		int iG = 0;
//		for (PamDataUnit dataUnit:dataUnits) {
//			ClickDetection click = (ClickDetection) dataUnit;
//			int clickChans = click.getChannelBitmap();
//			for (int iChan = 0; iChan < click.getNChan(); iChan++) {
//				int ithChan = PamUtils.getNthChannel(iChan, clickChans);
//				if ((1<<ithChan & channelMap) != 0) {
//					wavData[iG++] = click.getWaveData(iChan);
//				}
//			}
//		}
//		double[][][] delaysAndErrors = makeNaNArray(nChan);
//		double[][] maxDelays = getMaxDelays(geometry, channelMap, dataUnits.get(0));
//		double[][] delays = delaysAndErrors[0];
//		double[][] errors = delaysAndErrors[1];
//		double[][] pairData = new double[2][];
//		for (int i = 0; i < nChan; i++) {
//			for (int j = i+1; j < nChan; j++) {
//				pairData[0] = wavData[i];
//				pairData[1] = wavData[j];
//				double madDelay = maxDelays[i][j];
//				
//			}
//		}
////		/**
////		 * Click filter information gets added in the FFT organiser, which is click specific
////		 * after the FFT's have been made. Ofcourse, not if we're using envelope !
////		 */
////		return super.getTOADInformation(dataUnits, channelMap, geometry);
//		/*
//		 * Do our own thing using the DelayGroup functions, then rearranging into a TOADInofrmatoin object. 
//		 */
//	}
	
	/* (non-Javadoc)
	 * @see PamguardMVC.toad.GenericTOADCalculator#getSettingsPane(java.awt.Window)
	 */
	@Override
	public ManagedSettingsPane<?> getSettingsPane(Window parent, PamDataBlock<?> detectionSource) {
		return new ClickTOADOptions(parent);
	}

	private class ClickTOADOptions extends ManagedSettingsPane<ClickParameters> {

//		private TitledSettingPane<DelayMeasurementParams> settingsPane;
//		private TOADTimingPane toadPane;
//		private ClickDelayPanel clickDelayPanel = new ClickDelayPanel(clickControl, null);
		private ClickDelayPanelFX clickDelayPanelFX;

		private ClickTOADOptions(Window parent) {
//			settingsPane = new TitledSettingPane<DelayMeasurementParams>(toadPane = new TOADTimingPane(parent), "TDOA Options");
//			toadPane.setTimingSource(clickDataBlock);
			clickDelayPanelFX = new ClickDelayPanelFX(parent, clickControl);
		}
		
		@Override
		public SettingsPane<ClickParameters> getSettingsPane() {
			return clickDelayPanelFX;
		}

		@Override
		public boolean useParams(ClickParameters newParams) {
			setupCalculator();
			return true;
		}

		@Override
		public ClickParameters findParams() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

}
