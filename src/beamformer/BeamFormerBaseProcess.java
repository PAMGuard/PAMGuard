package beamformer;

import java.util.ArrayList;

import Array.ArrayManager;
import Array.PamArray;
import PamController.PamController;
import PamUtils.PamUtils;
import PamView.dialog.GroupedSourcePanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import beamformer.algorithms.BeamAlgorithmProvider;
import beamformer.algorithms.BeamFormerAlgorithm;
import beamformer.continuous.BeamFormerDataBlock;
import beamformer.continuous.BeamOGramDataBlock;
import beamformer.plot.BeamOGramPlotProvider;
import dataPlotsFX.data.TDDataProviderRegisterFX;
import dataPlotsFX.spectrogramPlotFX.FFTPlotProvider;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import pamMaths.PamVector;
import pamViewFX.fxNodes.utilityPanes.GroupedSourcePaneFX;

/**
 * Base process for both continuous and detect-then-localise beam forming.
 * @author Doug Gillespie
 *
 */
abstract public class BeamFormerBaseProcess extends PamProcess {

	private BeamFormerBaseControl beamFormerControl;
	
	private BeamAlgorithmProvider beamAlgorithmProvider;
	
	private ArrayList<BeamGroupProcess> groupProcesses = new ArrayList<>();

	private FFTDataBlock fftDataSource;
	
	private BeamFormerDataBlock beamFormerOutput;
	
	private BeamOGramDataBlock beamOGramOutput;

	private int[] channelGroupLUT;
	
	/**
	 * An int array of channel maps.  The ith element represents sequence/group i, and contains
	 * the channelMap associated with that sequence/group
	 */
	private int[] sequenceGroupLUT;
	
	public BeamFormerBaseProcess(BeamFormerBaseControl beamFormerBaseControl, boolean publishBeamOutput) {
		super(beamFormerBaseControl, null);
		this.beamFormerControl = beamFormerBaseControl;

//		beamAlgorithmProvider = new NullBeamProvider();
		
		// a little hack here to force the basic freq domain beamformer
		beamAlgorithmProvider = this.beamFormerControl.getAlgorithmList()[1];
		beamFormerOutput = new BeamFormerDataBlock("Beamformer data", this, 0, 512, 256);
		beamOGramOutput = new BeamOGramDataBlock("Beamogram data", this, 0);
		if (publishBeamOutput) {
			addOutputDataBlock(beamFormerOutput);
			addOutputDataBlock(beamOGramOutput);
			TDDataProviderRegisterFX.getInstance().registerDataInfo(new FFTPlotProvider(null, beamFormerOutput));
			TDDataProviderRegisterFX.getInstance().registerDataInfo(new BeamOGramPlotProvider(beamFormerBaseControl, beamOGramOutput));
		}
	}

	/**
	 * Find the source of FFT data for the beam former. this may not
	 * be the same as the main data source if the main source is raw or detection 
	 * data (overridden in BeamFormLocProcess). 
	 * @return FFT source for the beam former. 
	 */
	public FFTDataBlock findFFTDataBlock() {
		BeamFormerParams bfParams = beamFormerControl.getBeamFormerParams();
//		return (FFTDataBlock) PamController.getInstance().getDataBlock(FFTDataUnit.class, bfParams.getDataSource());
		return (FFTDataBlock) PamController.getInstance().getDataBlockByLongName(bfParams.getDataSource());
	}
	
	@Override
	public void prepareProcess() {
		BeamFormerParams bfParams = beamFormerControl.getBeamFormerParams();
//		if (!findAlgorithm()
//		if (beamAlgorithmProvider == null) {
//			return;
//		}				
		if (bfParams.getDataSource() == null) {
			return;
		}
		
		fftDataSource = findFFTDataBlock();
		if (fftDataSource == null) {
			return;
		}
		
		/*
		 * Set the parent data block. for continuous bf this will be the 
		 * fftDatasource but for localising it may be raw, fft or even a detection. 
		 */
		PamDataBlock parentBlock = PamController.getInstance().getDataBlockByLongName(bfParams.getDataSource());
		setParentDataBlock(fftDataSource);
		
		int groupMap = GroupedSourcePaneFX.getGroupMap(bfParams.getChannelBitmap(), bfParams.getChannelGroups());
		int[] groupList = PamUtils.getChannelArray(groupMap);
		if (groupList == null) {
			groupList = new int[0];
		}
//		int nChannelGroups = GroupedSourcePanel.countChannelGroups(bfParams.channelMap, bfParams.channelGroups);
		int nChannelGroups = groupList.length;
		BeamAlgorithmProvider[] algorithmList = beamFormerControl.getAlgorithmList();

		// set the parameters for the output data blocks
		beamFormerOutput.setFftLength(fftDataSource.getFftLength());
		beamFormerOutput.setFftHop(fftDataSource.getFftHop());
		beamFormerOutput.setChannelMap(bfParams.getChannelBitmap());
		beamOGramOutput.setHopSamples(fftDataSource.getFftHop());
		beamOGramOutput.setFftLength(fftDataSource.getFftLength());
		beamOGramOutput.setChannelMap(bfParams.getChannelBitmap());
		
		groupProcesses.clear();
		channelGroupLUT = GroupedSourcePanel.getGroupAssignments(bfParams.getChannelBitmap(), bfParams.getChannelGroups());
		sequenceGroupLUT = new int[nChannelGroups];
		int totalBeams = 0;
		int firstSeqNum = 0;
		int beamogramNum = 0;
		for (int i = 0; i < nChannelGroups; i++) {
			int groupChannels = GroupedSourcePanel.getGroupChannels(i, bfParams.getChannelBitmap(), bfParams.getChannelGroups());
			sequenceGroupLUT[i] = groupChannels;
			BeamAlgorithmParams thisAlgo = bfParams.getAlgorithmParms(groupList[i], groupChannels, bfParams.getAlgorithmName(i));
			if (thisAlgo==null) {
				System.out.println("Error accessing beamformer parameters for group " + String.valueOf(groupList[i]) + ", group channelmap " + String.valueOf(groupChannels) + ", algorithm " + bfParams.getAlgorithmName(i));
				System.out.println("Please check parameters to make sure they have been initialized properly.");
				continue;
			}

			// figure out which provider matches the parameters for that group number and channel map
			for (int j=0; j<algorithmList.length; j++) {
				if (algorithmList[j].getStaticProperties().getName().equals(thisAlgo.getAlgorithmName())) {
					
					// when we find a match, create a new BeamGroupProcess object and add it to the list
					BeamGroupProcess groupProcess = new BeamGroupProcess(this, algorithmList[j], thisAlgo, groupChannels, firstSeqNum, beamogramNum);
					groupProcesses.add(groupProcess);
					totalBeams += groupProcess.getBeamFormerAlgorithm().getNumBeams();
					firstSeqNum = totalBeams;
					if (groupProcess.getBeamFormerAlgorithm().thereIsABeamogram()) {
						beamogramNum++;
						
						// set the number of angles in the beamogram.  This is now done on a group by group basis. 
						beamOGramOutput.setNumAngles(i, groupProcess.getBeamFormerAlgorithm().getNumBeamogramAngles());

					}
					break;
				}
			}
		}
		beamFormerOutput.setSequenceMap(PamUtils.makeChannelMap(totalBeams));
		beamOGramOutput.setSequenceMap(PamUtils.makeChannelMap(beamogramNum));
		super.prepareProcess();
	}
	
	/**
	 * 
	 * @return the fftDataSource
	 */
	public FFTDataBlock getFftDataSource() {
		return fftDataSource;
	}

	
	public void makeContinuousBeams(PamObservable o, PamDataUnit pamDataUnit) {
		FFTDataUnit fftUnit = (FFTDataUnit) pamDataUnit;
		int singleChan = PamUtils.getSingleChannel(fftUnit.getChannelBitmap());
		if (singleChan>channelGroupLUT.length-1) {
			return;
		}
		int groupIndex = channelGroupLUT[singleChan];
		if (groupIndex < 0) {
			return;
		}
		if (groupIndex < groupProcesses.size()) {
			groupProcesses.get(groupIndex).process(fftUnit);
		}
	}
	
	/**
	 * find a beam group process which matches a channel map. 
	 * @param channelMap channel bitmap
	 * @return BeamgroupProcess or null if none match. 
	 */
	public BeamGroupProcess findGroupProcess(int channelMap) {
		if (groupProcesses == null) {
			return null;
		}
		for (BeamGroupProcess groupProc:groupProcesses) {
			if (groupProc.getGroupChannelMap() == channelMap) {
				return groupProc;
			}
		}
		return null;
	}

	/**
	 * Handle channel groupings so that the actual algorithm
	 * gets called with a complete array of all channels in 
	 * it's group in one call. Do it here since every single 
	 * algorithm will need to do this, so save them the effort. 
	 * @author dg50
	 *
	 */
//	public class BeamGroupProcess {
//		private BeamAlgorithmProvider provider;
//		private BeamFormerAlgorithm beamFormerAlgorithm;
//		private BeamAlgorithmParams parameters;
//		private int groupChannelMap;
//		private int currentChanMap;
//		private int nextChanIndex = 0;
//		private int firstSeqNum = 0;
//		private int numChannels;
//		private FFTDataUnit[] channelFFTUnits;
//		private int arrayShape;
//		private PamVector[] arrayMainAxes;
//		
//		public BeamGroupProcess(BeamAlgorithmProvider provider, BeamAlgorithmParams parameters, int groupChannels, int firstSeqNum, int beamogramNum) {
//			this.provider = provider;
//			this.parameters = parameters;
//			this.groupChannelMap = groupChannels;
//			this.firstSeqNum = firstSeqNum;
//			this.numChannels = PamUtils.getNumChannels(groupChannels);
//			channelFFTUnits = new FFTDataUnit[numChannels];
//			beamFormerAlgorithm = provider.makeAlgorithm(BeamFormerBaseProcess.this, parameters, firstSeqNum, beamogramNum);
//			beamFormerAlgorithm.prepare();
//			
//
//			// work out the subArray Shape. 
//			ArrayManager arrayManager = ArrayManager.getArrayManager();
//			PamArray currentArray = arrayManager.getCurrentArray();
//			int phones = groupChannelMap;
//			phones = getFftDataSource().getChannelListManager().channelIndexesToPhones(phones);
//			arrayShape = arrayManager.getArrayShape(currentArray, phones);
//			arrayMainAxes = arrayManager.getArrayDirections(currentArray, phones);
//		}
//		
//		/**
//		 * 
//		 * @return The parameter set currently used for this set of channels. 
//		 */
//		public BeamAlgorithmParams getAlgorithmParams() {
//			return parameters;
//		}
//		
//		public void process(FFTDataUnit fftDataUnit) {
//			int chMap = fftDataUnit.getChannelBitmap();
//			channelFFTUnits[nextChanIndex++] = fftDataUnit;
//			currentChanMap |= chMap;
//			if (nextChanIndex == numChannels) {
//				beamFormerAlgorithm.process(channelFFTUnits);
//				nextChanIndex = 0;
//			}
//		}
//
//		/**
//		 * @return the beamFormerAlgorithm
//		 */
//		public BeamFormerAlgorithm getBeamFormerAlgorithm() {
//			return beamFormerAlgorithm;
//		}
//
//		/**
//		 * @return the arrayShape
//		 */
//		public int getArrayShape() {
//			return arrayShape;
//		}
//
//		/**
//		 * @return the arrayMainAxes
//		 */
//		public PamVector[] getArrayMainAxes() {
//			return arrayMainAxes;
//		}
//
//		/**
//		 * Called from the localiser to reset the FFT store so 
//		 * that it always starts from scratch as new data are sent
//		 */
//		public void resetFFTStore() {
//			nextChanIndex = 0;
//			currentChanMap = 0;
//		}
//		
//	}

	/**
	 * @return the beamFormerOutput
	 */
	public BeamFormerDataBlock getBeamFormerOutput() {
		return beamFormerOutput;
	}
	/**
	 * @return the beamOGramOutput
	 */
	public BeamOGramDataBlock getBeamOGramOutput() {
		return beamOGramOutput;
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			prepareProcess();
		}
		super.notifyModelChanged(changeType);
	}

	/**
	 * @return the groupProcesses
	 */
	public ArrayList<BeamGroupProcess> getGroupProcesses() {
		return groupProcesses;
	}

	/**
	 * @return the channelGroupLUT
	 */
	public int[] getChannelGroupLUT() {
		return channelGroupLUT;
	}
	
	/**
	 * Convert a frequency to the nearest bin. The range of the bin
	 * will be from 0 to fftLength()/2 inclusive, so when looping to a higher 
	 * limit, loop to < the top bin number!
	 * @param frequency Frequency in Hz
	 * @return FFT bin number. 
	 */
	public int frequencyToBin(double frequency) {
		if (fftDataSource == null) {
			fftDataSource = findFFTDataBlock();
			if (fftDataSource == null) {
				return 0;
			}
		}
		double binSize = fftDataSource.getSampleRate() / fftDataSource.getFftLength();
		int bin = (int) Math.round(frequency/binSize);
		return Math.max(0, Math.min(bin, fftDataSource.getFftLength()/2));
	}

	/**
	 * Convert an array of frequency values to the nearest bins. The range of the bins
	 * will be from 0 to fftLength()/2 inclusive, so when looping to a higher 
	 * limit, loop to < the top bin number!
	 * @param frequency Frequencies in Hz
	 * @return FFT bin numbers. 
	 */
	public int[] frequencyToBin(double[] frequency) {
		if (frequency == null) {
			return null;
		}
		int[] bins = new int[frequency.length];
		for (int i = 0; i < frequency.length; i++) {
			bins[i] = frequencyToBin(frequency[i]);
		}
		return bins;
	}

	/**
	 * Given a sequenceMap, this function returns all of the channels that are
	 * associated with it.
	 * 
	 * @param sequenceMap
	 * @return
	 */
	public int getChannelsForSequenceMap(int sequenceMap) {
		int returnMap=0;
		for (int i=0; i<sequenceGroupLUT.length; i++) {
			if (((1 << i) & sequenceMap) == 1) {
				returnMap |= sequenceGroupLUT[i];
			}
		}
		return returnMap;
	}
	
	

	
}
