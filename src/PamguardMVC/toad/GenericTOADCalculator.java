package PamguardMVC.toad;

import java.awt.Window;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionProcess;
import Array.ArrayManager;
import Array.SnapshotGeometry;
import Localiser.DelayMeasurementParams;
import Localiser.algorithms.Correlations;
import Localiser.algorithms.TimeDelayData;
import Localiser.controls.RawOrFFTParams;
import Localiser.controls.TOADTimingParams;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.SettingsNameProvider;
import PamController.SettingsPane;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamProcess;
import fftManager.FFTDataUnit;
import fftManager.fftorganiser.FFTDataException;
import fftManager.fftorganiser.FFTDataList;
import fftManager.fftorganiser.FFTDataOrganiser;
import fftManager.fftorganiser.FFTInputTypes;
import group3dlocaliser.Group3DLocaliserControl;
import group3dlocaliser.algorithm.toadbase.TOADInformation;
import pamMaths.PamVector;
import pamViewFX.fxNodes.pamDialogFX.ManagedSettingsPane;

/**
 * Generic TOAD calculator which does it's best by any type of sound. Can be used when 
 * specific datablocks claim not to have a TOAD Calculator. It will do it's best to make 
 * sensible options based on the type of data block in use.  
 * @author dg50
 *
 */
public class GenericTOADCalculator implements TOADCalculator, PamSettings {

	private PamDataBlock<?> detectorDataBlock;
	
	/*
	 * There are pretty much the generic parameters required by this 
	 * timing type. May be able to seed some of them though ...
	 */
	private GenericTOADSourceParams toadSourceParams = new GenericTOADSourceParams();

	private RawOrFFTParams rawOrFFTParams;

	private PamDataBlock<?> timingSource;
	
	private FFTDataOrganiser fftDataOrganiser;

	private Correlations correlations = new Correlations();
	
	private DelayMeasurementParams delayMeasurementParams = new DelayMeasurementParams();
	
	private boolean canUseEnvelope = false;
	
	private boolean canUseLeadingEdge = false;
	
//	private double[][] debugDelays = new double[28][30];
//	private int[][] debugOffset = new int[28][30];
//	private int iDebug = 0;

	private int iDD;

	private SettingsNameProvider settingsNameProvider;
	
	/**
	 * @param dataBlock this is the detection source block and may not be where it's going to 
	 * get the timing data from ...
	 * @param settingsNameProvider 
	 */
	public GenericTOADCalculator(SettingsNameProvider settingsNameProvider) {
		super();
//		this.detectorDataBlock = dataBlock;
		this.settingsNameProvider = settingsNameProvider;
		fftDataOrganiser = new FFTDataOrganiser(settingsNameProvider);
//		fftDataOrganiser.setInput(dataBlock, fftDataOrganiser.suggestInputType(dataBlock));
//		toadSourceParams.getRawOrFFTParams().setSourceName(dataBlock.getLongDataName());
		PamSettingManager.getInstance().registerSettings(this);
		setTOADSourceParams(toadSourceParams);
	}
	
	public void setTOADSourceParams(GenericTOADSourceParams toadSourceParams) {
		this.toadSourceParams = toadSourceParams;
//		if (detectorDataBlock == null) {
//			return;
//		}

		rawOrFFTParams = toadSourceParams.getRawOrFFTParams();
		DelayMeasurementParams timingParams = toadSourceParams.getToadTimingParams();
		
		// the source will have been set in the dialog, so ....
		timingSource = PamController.getInstance().getDataBlockByLongName(rawOrFFTParams.getSourceName());
		if (timingSource == null) {
			return;
		}
		
		FFTInputTypes prefType = fftDataOrganiser.suggestInputType(timingSource);
		fftDataOrganiser.setInput(timingSource, prefType);
		
	}
	
	/**
	 * Generate TOAD information for a whole load of data units (or just one - maybe 
	 * needs a simpler list if there is only one to avoid having to make lots of lists
	 * of just one data unit !)<br>
	 * Note that any fancy operations that involve waveform envelopes, etc, are 
	 * generally handled by the FFTOrganiser. However, this function does need some 
	 * way of filtering the FFT data for the final cross correlation...
	 * @param dtaUnits Data units, which are assumed to be of the same sound. If not, 
	 * then this function should be called separately for sounds considered part of the 
	 * same group. 
	 * @param channelMap wanted channels ? Do we need this parameter ? 
	 * @return
	 */
	public TOADInformation getTOADInformation(List<PamDataUnit> dataUnits, double sampleRate, int channelMap, SnapshotGeometry geometry) {
		FFTDataList totalFFTList = null;
		for (PamDataUnit<?,?> dataUnit:dataUnits) {
			try {
				/*
				 * It is possible that fftData will come back with a different sample rate to the 
				 * sampleRate parameter fed into createFFTDataList. In particular, this happens
				 * in the click detector ClickFFTOrganiser, which can upsample the waveform data
				 * to give better timing accuracy. 
				 */
				FFTDataList fftData = fftDataOrganiser.createFFTDataList(dataUnit, sampleRate, dataUnit.getChannelBitmap() & channelMap);
//				System.out.println("fftData: " + fftData);
				if (fftData == null || fftData.getMaxChannelCount() == 0) {
					// debug stuff ...
					System.out.println("No FFT Data for " + dataUnit.getSummaryString());
					fftData = fftDataOrganiser.createFFTDataList(dataUnit, sampleRate, dataUnit.getChannelBitmap() & channelMap);
					continue;
				}
				if (totalFFTList == null) {
					totalFFTList = fftData;
				}
				else {
					totalFFTList.mergeIn(fftData);
				}
			} catch (FFTDataException e) {
				System.out.println("Error in GenericTOADCalulator.getTOADInformation: " + e.getMessage());
				continue;
			}
		}
		if (totalFFTList == null) {
			return null;
		}
		
		double[][] maxDelays = getMaxDelays(geometry, totalFFTList.getChannelMap(), dataUnits.get(0));
		
		
		PamProcess sourceProcess = dataUnits.get(0).getParentDataBlock().getSourceProcess();
		AcquisitionControl acquisitionControl = null;
		if (sourceProcess instanceof AcquisitionProcess) {
			AcquisitionProcess ap = (AcquisitionProcess) sourceProcess;
			acquisitionControl = ap.getAcquisitionControl();
		}
		
		/*
		 * 'spose we could organise the whole lot by channel, so that 
		 * we don't have to keep requesting sublists as we loop and loop ... 
		 */
		FFTDataUnit[][] channelFFTData = totalFFTList.getChannelSeparatedData();
		int nChan = channelFFTData.length;
		if (nChan < 2) {
			return null;
		}
		int[] streamerList = geometry.getStreamerList();
		if (streamerList == null) {
			streamerList = new int[nChan];
		}
		double[][][] delaysAndErrors = makeNaNArray(nChan);
		double[][] tdelays = delaysAndErrors[0];
		double[][] terrors = delaysAndErrors[1];
		double[][] tCorrelations = new double[nChan][nChan];
		int[] channelList = new int[nChan];
		int[] hydrophoneList = new int[nChan];
		int hydrophoneMap = 0;
		iDD = 0;
		for (int i = 0; i < nChan; i++) {
			channelList[i] = PamUtils.getNthChannel(i, totalFFTList.getChannelMap());
			if (acquisitionControl == null) {
				hydrophoneList[i] = i;
			}
			else {
				hydrophoneList[i] = acquisitionControl.getChannelHydrophone(channelList[i]);
			}
			hydrophoneMap |= (1<<hydrophoneList[i]);
			for (int j = i+1; j < nChan; j++) {
				TimeDelayData tdData = getDelayFromLists(channelFFTData[i], channelFFTData[j], totalFFTList.getSampleRate(), maxDelays[i][j]);
				if (tdData == null) {
					continue;
				}
				tdelays[i][j] = tdData.getDelay();
				tCorrelations[i][j] = tdData.getDelayScore();
				terrors[i][j] = tdData.getDelayError();
				if (Double.isNaN(terrors[i][j])) {
					terrors[i][j] = 1./totalFFTList.getSampleRate();
				}
//				if (streamerList[i] != streamerList[j]) {
//					terrors[i][j] = 3.8e-5;//*= 10.;
//				}
				iDD++;
			}
		}
		
		TOADInformation toadInformation = new TOADInformation(channelMap, channelList, hydrophoneMap, hydrophoneList, tdelays, terrors, tCorrelations);

//		int iD = 0;
//		for (int i = 0; i < nChan; i++) {
//			for (int j = i+1; j < nChan; j++) {
//				debugDelays[iD++][iDebug] = tdelays[i][j];
//			}
//		}
//		iDebug++;
//		if (iDebug == debugDelays[0].length) {
////			System.out.println("Full debug arrays");
//			iDebug = 0;
//		}
		
		return toadInformation;
	}	
	
	/**
	 * Get a 2D array of maximum time delays between channels. 
	 * @param geometry current geometry
	 * @param channelMap channel map
	 * @param dataUnit data unit (needed to find acquisition source)
	 * @return 2D array of max delays in seconds. 
	 */
	public double[][] getMaxDelays(SnapshotGeometry geometry, int channelMap, PamDataUnit dataUnit) {
		/*
		 * will need the array geometry at this point in order to work out 
		 * maximum time delays between array components. int ic = 0;
		 */
		AcquisitionControl acquisitionControl;
		int nCh = PamUtils.getNumChannels(channelMap);
		PamProcess sourceProcess = dataUnit.getParentDataBlock().getSourceProcess();
		int[] hIndex = null;
		if (sourceProcess instanceof AcquisitionProcess) {
			AcquisitionProcess ap = (AcquisitionProcess) sourceProcess;
			acquisitionControl = ap.getAcquisitionControl();
			hIndex = acquisitionControl.getHydrophoneList();
		}
		else {
			acquisitionControl = null;
		}
		double c = geometry.getCurrentArray().getSpeedOfSound();
		double[][] maxDelays = new double[nCh][nCh];
		for (int i = 0; i < nCh; i++) {
			for (int j = i+1; j < nCh; j++) {
				int hi = hIndex == null ? i : hIndex[i];
				int hj = hIndex == null ? j : hIndex[j];
				PamVector v = geometry.getGeometry()[geometry.getHydrophoneList()[hi]].
						sub(geometry.getGeometry()[geometry.getHydrophoneList()[hj]]);
				maxDelays[i][j] = v.norm()/c;
			}
		}
		return maxDelays;
	}
	
	/**
	 * Make a blank array for delays and estimated errors full of NaN values
	 * @param n array dimension (number of single channels)
	 * @return array of dimension [2][n][n]
	 */
	protected double[][][] makeNaNArray(int n) {
		double[][][] a = new double[2][n][n];
		for (int i = 0; i < n; i++) {
			Arrays.fill(a[0][i], Double.NaN);
			Arrays.fill(a[1][i], Double.NaN);
		}
		return a;
	}

	/**
	 * work out the delay ...
	 * @param fftDataUnits1
	 * @param fftDataUnits2
	 * @param maxDelaySecs Maximum TIOAD in seconds.  
	 * @return
	 */
	private TimeDelayData getDelayFromLists(FFTDataUnit[] fftDataUnits1, FFTDataUnit[] fftDataUnits2, double sampleRate, double maxDelaySecs) {
		if (fftDataUnits1 == null || fftDataUnits1.length == 0) {
			return null;
		}
		if (fftDataUnits2 == null || fftDataUnits2.length == 0) {
			return null;
		}
		TimeDelayData tdData;
		double maxDelaySamples = maxDelaySecs * sampleRate;
		if (fftDataUnits1.length == 1 && fftDataUnits2.length == 1) {
			tdData = correlations.getDelay(fftDataUnits1[0].getFftData(), fftDataUnits2[0].getFftData(), 
					fftDataUnits1[0].getFftData().length()*2, maxDelaySamples, fftDataUnits1[0].getUsefulBinRange());
			long delayOffset = fftDataUnits2[0].getStartSample() - fftDataUnits1[0].getStartSample();
			tdData.addDelayOffset(delayOffset);
		}
		else {
			tdData = getSlidingBestDelay(fftDataUnits1, fftDataUnits2, maxDelaySamples);
		}
		if (tdData == null) {
			return null;
		}
		tdData.scaleDelay(1. / sampleRate);
		return tdData;
	}
	
	private TimeDelayData getSlidingBestDelay(FFTDataUnit[] fftListA, FFTDataUnit[] fftListB, double maxDelaySamples) {
		int nA = fftListA.length;
		int nB = fftListB.length;
		if (nA == 0 || nB == 0) {
			return null;
		}
		long s1a = fftListA[0].getStartSample();
		long s1b = fftListB[0].getStartSample();
		int minOverlap = 1; //might vary this !
		double[] bestDelay = new double[3];
		int bestA=-2, bestB=-1;
		int fftLen = fftListA[0].getFftData().length()*2 ;
		// start at the start of A, but at different points in B.
		for (int i = 0; i < nB-1; i++) {
//			for (int i = 0; i < 1; i++) {
			long tOff = fftListB[i].getStartSample()-fftListA[0].getStartSample();
			double minD = tOff - fftLen/2;
			double maxD = tOff + fftLen/2; 
			if (maxD < -maxDelaySamples || minD > maxDelaySamples) {
				//impossible to get a match out of this. 
				continue;
			}
			double[] ds = getSlidingDelay(fftListA, fftListB, 0, i);
			if (ds == null) {
				continue;
			}
			if (ds[1] > bestDelay[1]) {
				bestDelay = ds;
				bestA = 0;
				bestB = i;
//				debugOffset[iDD][iDebug] = i;
			}
		}
		// then slide A but not B;
		for (int i = 1; i < nA-1; i++) {
			long tOff = fftListB[0].getStartSample()-fftListA[i].getStartSample();
			double minD = tOff - fftLen/2;
			double maxD = tOff + fftLen/2; 
			if (maxD < -maxDelaySamples || minD > maxDelaySamples) {
				//impossible to get a match out of this. 
				continue;
			}
			double[] ds = getSlidingDelay(fftListA, fftListB, i, 0);
			if (ds == null) {
				continue;
			}
			if (ds[1] > bestDelay[1]) {
				bestDelay = ds;
				bestA = i;
				bestB = 0;
//				debugOffset[iDD][iDebug] = -i;
			}

		}
//		System.out.printf("Best delay chans %d,%d at offset %d,%d, Corr %3.3f, %d overlap\n", 
//				PamUtils.getSingleChannel(fftListA[0].getChannelBitmap()),
//				PamUtils.getSingleChannel(fftListB[0].getChannelBitmap()),
//				bestA, bestB, bestDelay[1], (int)bestDelay[2]);
		if (bestA < 0 || bestB < 0) {
			return null;
		}
				
		double offset = (fftListB[bestB].getStartSample()-fftListA[bestA].getStartSample());
		TimeDelayData tdData = new TimeDelayData(bestDelay[0]+offset, bestDelay[1]);
		return tdData;
//		return bestDelay[0] + offset;
	}
	
	/**
	 * Get the minimal overlap between two bin ranges. 
	 * @param rangeA
	 * @param rangeB
	 * @param fftLen
	 * @return minimal overlap or 0 - fftLength.
	 */
	private int[] getUsefulBinRange(int[] rangeA, int[] rangeB, int fftLen) {
		if (rangeA == null && rangeB == null) {
			return new int[]{0, fftLen/2}; // both null so give the full range
		}
		if (rangeA == null) {
			return rangeB;
		}
		if (rangeB == null) {
			return rangeA;
		}
		/*
		 * If it get's here, neither are null, so return the minimal overlap. 
		 */
		return new int[] {Math.max(rangeA[0], rangeB[0]), Math.min(rangeA[1], rangeB[1])};
	}
	
	private double[] getSlidingDelay(FFTDataUnit[] fftListA, FFTDataUnit[] fftListB, int startA, int startB) {
		int halfFFFTLen = fftListA[0].getFftData().length();
		int fftLength = halfFFFTLen*2;
		ComplexArray conjArray = new ComplexArray(fftLength);
		double[] conjData = conjArray.getData();
		double totPowerA=0, totPowerB=0;
		int nA = fftListA.length;
		int nB = fftListB.length;
		int n = Math.min(nA, nB);
		long startOffset = fftListB[startB].getStartSample()-fftListA[startA].getStartSample();
		double reVal,imVal;
		/**
		 * Sum all the first channel * conj of second channel into a single array. 
		 */
		int totalOverlap = 0;
		for (int iA = startA, iB = startB; iA<nA && iB<nB; iA++, iB++) {
			double[] cA = fftListA[iA].getFftData().getData();
			double[] cB = fftListB[iB].getFftData().getData();
			int[] binsA = fftListA[iA].getUsefulBinRange();
			int[] binsB = fftListB[iB].getUsefulBinRange();
			int[] usefulRange = getUsefulBinRange(binsA, binsB, fftLength);
			if (binsA== null) {
				binsA = usefulRange;
			}
			if (binsB == null) {
				binsB = usefulRange;
			}
			
			/**
			 * Try summing the totals over their full range, not just the
			 * useful range, this should reduce the correlation coeff when
			 * the match is poor. 
			 */
			for (int re = binsA[0]*2, im = re+1; re < binsA[1]*2; re+=2, im+=2) {
				totPowerA += cA[re]*cA[re]+cA[im]*cA[im];
			}
			for (int re = binsB[0]*2, im = re+1; re < binsB[1]*2; re+=2, im+=2) {
				totPowerB += cB[re]*cB[re]+cB[im]*cB[im];
			}
			for (int re = usefulRange[0]*2, im = re+1; re < usefulRange[1]*2; re+=2, im+=2) {
				reVal = cA[re]*cB[re]+cA[im]*cB[im];
				imVal = -cA[re]*cB[im]+cA[im]*cB[re];
				conjData[re] += reVal;
				conjData[im] += imVal;
				totalOverlap++;
			}
		}
		if (totalOverlap == 0) {
			return null;
		}
		/**
		 * Now fill in the second half of the conj array...
		 */
		double scale = Math.sqrt(totPowerA*totPowerB)*2;
		for (int re = 0, im = 1, re2=fftLength*2-2, im2 = fftLength*2-1; re < fftLength; re+=2, im+=2, re2-=2, im2-=2) {
			conjData[re2] = conjData[re];
			conjData[im2] = -conjData[im];
		}
//		double[] newDat = Arrays.copyOf(conjData, fftLength/2);
//		ComplexArray oth = new ComplexArray(newDat);
//		double[] xCorr = correlations.getFastFFT().realInverse(oth);
		correlations.getFastFFT().ifft(conjArray, fftLength);
		double[] delayAndHeight = correlations.getInterpolatedPeak(conjArray, scale, halfFFFTLen);
		delayAndHeight = Arrays.copyOf(delayAndHeight, 3);
		delayAndHeight[2] = totalOverlap; 
//		System.out.printf("Offsets %d, %d, corr %3.3f, overlap %d of %d = %3.1f%%\n", 
//				startA, startB, delayAndHeight[1], (int)delayAndHeight[2], Math.min(nA, nB), 
//				delayAndHeight[2] / Math.min(nA, nB)*100);
		return delayAndHeight;
	}

	public DelayMeasurementParams getDelayMeasurementParams() {
		return delayMeasurementParams;
	}
	
	@Override
	public ManagedSettingsPane<?> getSettingsPane(Window parent, PamDataBlock<?> detectionSource) {
		return new ManagedTOADSourcePane(parent, detectionSource);
	}

	private class ManagedTOADSourcePane extends ManagedSettingsPane<GenericTOADSourceParams> {

		private GenericTOADSourcePane toadSourcePane;
		private PamDataBlock<?> detectionSource;
		
		public ManagedTOADSourcePane(Window parent, PamDataBlock<?> detectionSource) {
			this.toadSourcePane = new GenericTOADSourcePane(parent, GenericTOADCalculator.this, detectionSource);
			this.detectionSource = detectionSource;
		}
		
		@Override
		public boolean useParams(GenericTOADSourceParams newParams) {
			setTOADSourceParams(newParams);
			return true;
		}

		@Override
		public GenericTOADSourceParams findParams() {
			toadSourcePane.setDetectionSource(detectorDataBlock);
			GenericTOADSourceParams ppp = toadSourceParams;
			return ppp;
		}

		@Override
		public SettingsPane<GenericTOADSourceParams> getSettingsPane() {
			return toadSourcePane;
		}
		
	}
	
	@Override
	public boolean hasTOADDialog() {
		return false;
	}

	@Override
	public boolean showTOADDialog(Object parentWindow) {
		return false;
	}
	@Override
	public String getUnitName() {
		return settingsNameProvider.getUnitName();
	}
	@Override
	public String getUnitType() {
		return "Standard TOAD Calculator";
	}
	@Override
	public Serializable getSettingsReference() {
		return toadSourceParams;
	}
	@Override
	public long getSettingsVersion() {
		return GenericTOADSourceParams.serialVersionUID;
	}
	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		toadSourceParams = ((GenericTOADSourceParams) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	/**
	 * @return the detectorDataBlock
	 */
	public PamDataBlock<?> getDetectorDataBlock() {
		return detectorDataBlock;
	}

	/**
	 * @param detectorDataBlock the detectorDataBlock to set
	 */
	public void setDetectorDataBlock(PamDataBlock<?> detectorDataBlock) {
		this.detectorDataBlock = detectorDataBlock;
//		toadSourceParams.getRawOrFFTParams()
//		toadSourceParams.getToadTimingParams().s
	}

	/**
	 * @return the fftDataOrganiser
	 */
	public FFTDataOrganiser getFftDataOrganiser() {
		return fftDataOrganiser;
	}

	/**
	 * @param fftDataOrganiser the fftDataOrganiser to set
	 */
	public void setFftDataOrganiser(FFTDataOrganiser fftDataOrganiser) {
		this.fftDataOrganiser = fftDataOrganiser;
	}

	/**
	 * @return the canUseEnvelope
	 */
	public boolean isCanUseEnvelope() {
		return canUseEnvelope;
	}

	/**
	 * @param canUseEnvelope the canUseEnvelope to set
	 */
	public void setCanUseEnvelope(boolean canUseEnvelope) {
		this.canUseEnvelope = canUseEnvelope;
	}

	/**
	 * @return the canUseLeadingEdge
	 */
	public boolean isCanUseLeadingEdge() {
		return canUseLeadingEdge;
	}

	/**
	 * @param canUseLeadingEdge the canUseLeadingEdge to set
	 */
	public void setCanUseLeadingEdge(boolean canUseLeadingEdge) {
		this.canUseLeadingEdge = canUseLeadingEdge;
	}

	/**
	 * @return the timingSource
	 */
	public PamDataBlock<?> getTimingSource() {
		return timingSource;
	}

	/**
	 * @param timingSource the timingSource to set
	 */
	public void setTimingSource(PamDataBlock<?> timingSource) {
		this.timingSource = timingSource;
		fftDataOrganiser.getFftObservable().setObservable(timingSource);
	}

}
