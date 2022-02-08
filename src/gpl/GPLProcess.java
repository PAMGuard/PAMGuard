package gpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import PamController.PamController;
import PamController.PamControllerInterface;
import PamDetection.AbstractLocalisation;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.background.SpecBackgroundDataUnit;
import PamguardMVC.background.SpecBackgroundManager;
import PamguardMVC.blockprocess.BlockMode;
import PamguardMVC.blockprocess.BlockState;
import PamguardMVC.blockprocess.PamBlockDataList;
import PamguardMVC.blockprocess.PamBlockParams;
import PamguardMVC.blockprocess.PamBlockProcess;
import beamformer.loc.BeamFormerLocalisation;
import dataPlotsFX.data.TDDataProviderRegisterFX;
import dataPlotsFX.spectrogramPlotFX.FFTPlotProvider;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import gpl.contour.ContourFinder;
import gpl.contour.GPLContour;
import gpl.contour.NullGPLContour;
import gpl.graphfx.GPLDetPlotProvider;
import gpl.graphfx.GPLStatePlotProvider;
import gpl.graphfx.GPLSymbolManager;
import gpl.io.GPLBinaryDataSource;
import gpl.io.GPLDatagramProvider;
import gpl.io.GPLLogging;
import gpl.swing.GPLOverlayGraphics;
import gpl.whiten.Base3xWhitener;
import gpl.whiten.InfiniteSort;
import gpl.whiten.MovingMatrix;
import gpl.whiten.WhitenMatrix;
import gpl.whiten.WhitenVector;

public class GPLProcess extends PamBlockProcess {

	private GPLControlledUnit gplControlledUnit;

	private FFTDataBlock whitenedSpectrogram;

	private FFTDataBlock sourceFFTData;

	private GPLStateDataBlock stateDataBlock;

	private GPLDetectionBlock gplDetectionBlock;

	private ChannelProcess[] channelProcesses = new ChannelProcess[PamConstants.MAX_CHANNELS];

	private boolean useGPLQuiet = false;

	private GPLBeamSelector gplBeamSelector;

	//	private PamBlockParams blockParams;

	private ContourFinder contourFinder;
	
	public static BlockMode[] blockModes = {BlockMode.BLOCKED, BlockMode.BLOCKFIRST, BlockMode.NONE}; 

	/*
	 * Temp values, to replace in parameters file later. 
	 */
	public int binLo = 37-1;
	public int binHi = 93-1;
	public int sum_binLo = 37-1;
	public int sum_binHi = 93-1;

	private BlockState currentBlockState;
	
	private SpecBackgroundManager backgroundManager;

	public GPLProcess(GPLControlledUnit gplControlledUnit) {
		super(gplControlledUnit, null);
		this.gplControlledUnit = gplControlledUnit;
		contourFinder = new ContourFinder(gplControlledUnit, this);

		whitenedSpectrogram = new FFTDataBlock(gplControlledUnit.getUnitName() + " Whitened Spectrogram", this, 0, 1, 1);
		addOutputDataBlock(whitenedSpectrogram);
		
		stateDataBlock = new GPLStateDataBlock(this, 0);
		stateDataBlock.setBinaryDataSource(new GPLStateDataSource(stateDataBlock));
		addOutputDataBlock(stateDataBlock);
		
		gplDetectionBlock = new GPLDetectionBlock(this);
		addOutputDataBlock(gplDetectionBlock);

		TDDataProviderRegisterFX.getInstance().registerDataInfo(new FFTPlotProvider(null, whitenedSpectrogram));
		TDDataProviderRegisterFX.getInstance().registerDataInfo(new GPLStatePlotProvider(gplControlledUnit, stateDataBlock));
		TDDataProviderRegisterFX.getInstance().registerDataInfo(new GPLDetPlotProvider(gplControlledUnit, gplDetectionBlock));

		gplDetectionBlock.SetLogging(new GPLLogging(gplControlledUnit, gplDetectionBlock));
		gplDetectionBlock.setBinaryDataSource(new GPLBinaryDataSource(this, gplDetectionBlock));
		gplDetectionBlock.setCanClipGenerate(true);
		gplDetectionBlock.setOverlayDraw(new GPLOverlayGraphics(gplControlledUnit, gplDetectionBlock, null));
		gplDetectionBlock.setPamSymbolManager(new GPLSymbolManager(gplDetectionBlock));
		gplDetectionBlock.setDatagramProvider(new GPLDatagramProvider(this, gplDetectionBlock));

		backgroundManager = new SpecBackgroundManager(this, gplDetectionBlock);
		gplDetectionBlock.setBackgroundManager(backgroundManager);
		//		blockParams = new PamBlockParams(BlockMode.BLOCKED, 20000);
	}


	@Override
	public PamBlockParams getBlockParams() {
		PamBlockParams blockParams = gplControlledUnit.getGplParameters().blockParams;
		blockParams.blockLengthMillis = (long) (gplControlledUnit.getGplParameters().backgroundTimeSecs*1000);
		return blockParams;
	}


	@Override
	public void setBlockState(BlockState state) {
		currentBlockState = state;
		debugMessage("Current state: " + state);
	}


	@Override
	public void addBlockData(PamBlockDataList blockDataList) {
		PamBlockDataList[] channelLists = channelSplitList(blockDataList);
		int nChan = channelLists.length;
		for (int i = 0; i < nChan; i++) {
			PamBlockDataList chanList = channelLists[i];
			if (chanList == null) {
				continue;
			}
			int chan = PamUtils.getSingleChannel(chanList.getSequenceBitmap());
			if (channelProcesses[chan] == null) {
				continue;
			}
			channelProcesses[chan].addBlockData(chanList);
		}
	}


	@Override
	public void addSingleData(PamObservable o, PamDataUnit dataUnit) {
		int chan = PamUtils.getSingleChannel(dataUnit.getSequenceBitmap());
		if (channelProcesses[chan] == null) return;
		if ((1<<chan & gplControlledUnit.getGplParameters().sequenceMap) != 0) {
			FFTDataUnit fftDataUnit = (FFTDataUnit) dataUnit;
			channelProcesses[chan].addSingleData(fftDataUnit);
		}
	}


	//	@Override
	//	public void blockBackground(PamObservable o, PamDataUnit dataUnit) {
	//		int chan = PamUtils.getSingleChannel(dataUnit.getSequenceBitmap());
	//		if (channelProcesses[chan] == null) return;
	//		if ((1<<chan & gplControlledUnit.getGplParameters().sequenceMap) != 0) {
	//			FFTDataUnit fftDataUnit = (FFTDataUnit) dataUnit;
	//			channelProcesses[chan].newBackground(fftDataUnit);
	//		}
	//	}


	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamControllerInterface.INITIALIZATION_COMPLETE) {
			prepareProcess();
		}
	}


	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#prepareProcess()
	 */
	@Override
	public void prepareProcess() {
		GPLParameters gplParams = gplControlledUnit.getGplParameters();
		// find any old FFT data for now and subscribe to it. 
		//PamDataBlock fftBlock = PamController.getInstance().getDataBlockByLongName(gplParams.fftSourceName);
		// Find the correct FFT DataBlock if multiple exist
		PamDataBlock fftBlock = PamController.getInstance().getDataBlock(FFTDataUnit.class, gplParams.fftSourceName);

		if (fftBlock == null) {
			return;
		}
		setParentDataBlock(fftBlock);
		sourceFFTData = (FFTDataBlock) fftBlock;

		gplBeamSelector = new GPLBeamSelector(sourceFFTData, gplDetectionBlock);


		//		whitenedSpectrogram.setChannelMap(gplParams.sequenceMap);
		whitenedSpectrogram.sortOutputMaps(sourceFFTData.getChannelMap(), sourceFFTData.getSequenceMapObject(), gplParams.sequenceMap);
		whitenedSpectrogram.setFftLength(sourceFFTData.getFftLength());
		whitenedSpectrogram.setFftHop(sourceFFTData.getFftHop());
		whitenedSpectrogram.setSampleRate(sourceFFTData.getSampleRate(), true);

		//		stateDataBlock.setChannelMap(gplParams.sequenceMap);
		stateDataBlock.sortOutputMaps(sourceFFTData.getChannelMap(), sourceFFTData.getSequenceMapObject(), gplParams.sequenceMap);
		//		gplDetectionBlock.setChannelMap(gplParams.sequenceMap);
		gplDetectionBlock.sortOutputMaps(sourceFFTData.getChannelMap(), sourceFFTData.getSequenceMapObject(), gplParams.sequenceMap);

		binLo = (int) (gplParams.minFreq * sourceFFTData.getFftLength() / getSampleRate());
		binLo = Math.max(binLo, 0);
		sum_binLo = binLo;
		binHi = (int) Math.ceil(gplParams.maxFreq * sourceFFTData.getFftLength() / getSampleRate());
		binHi = Math.min(binHi, sourceFFTData.getFftLength()/2-1);
		sum_binHi = binHi;

		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((1<<i & gplParams.sequenceMap) != 0) {
				channelProcesses[i] = new ChannelProcess(i);
			}
			else {
				channelProcesses[i] = null;
			}
		}

		super.prepareProcess();
	}


	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		super.setSampleRate(sampleRate, notify);
	}


	private class ChannelProcess {
		private int sequence;
		private Base3xWhitener timeWhitener;
		private GPL_Quiet gplQuiet;

		private MovingMatrix historyMatrix;
		private int nFreqBins;
		private double[][] norm_h, norm_v;
		double[] bas;
		private InfiniteSort baseSort;
		private PeakDetector peakDetector;
		private int backgroundBins;
		private double[] specData;
		private int nBlockCalls = 0;
		private boolean allNan = true;
		private WhitenMatrix yWhitener1;
		private WhitenVector whitenVector;
		private InfiniteSort dummySort;

		private boolean isFirst = true;
		int blockCount, singleCount;
		private FFTDataUnit previousFFTDataUnit;
		private long lastBackgroundMillis, lastBackgroundSample;
		
		private SpecBackgroundDataUnit lastBackground;

		public ChannelProcess(int sequence) {
			super();
			this.sequence = sequence;
			//			timeWhitener = new SimpleWhitener(100);
			GPLParameters gplParams = gplControlledUnit.getGplParameters();
			backgroundBins = (int) (gplParams.backgroundTimeSecs * getSampleRate() / sourceFFTData.getFftHop());
			timeWhitener = new Base3xWhitener(backgroundBins, gplParams.white_fac_x, binLo, binHi);
			nFreqBins = binHi-binLo+1;
			gplQuiet = new GPL_Quiet(GPLProcess.this, sequence, backgroundBins, nFreqBins);
			historyMatrix = new MovingMatrix(backgroundBins, nFreqBins);
			yWhitener1 = new WhitenMatrix(nFreqBins, backgroundBins, 1.0);
			whitenVector = new WhitenVector();
			specData = new double[nFreqBins];
			norm_h = new double[backgroundBins][nFreqBins];
			norm_v = new double[backgroundBins][nFreqBins];
			bas = new double[nFreqBins];
			baseSort = new InfiniteSort(backgroundBins);
			dummySort = new InfiniteSort(backgroundBins);
			peakDetector = new PeakDetector(gplControlledUnit, gplParams.minPeakGap, binLo, binHi);

			//			timeWhitener.setAutoInitialise(20);


		}

		/**
		 * Puts FFT data into the whitener. 
		 * @param fftDataUnit
		 */
		public void newBackground(FFTDataUnit fftDataUnit) {
			ComplexArray fftData = fftDataUnit.getFftData();
			for (int i = binLo, b = 0; i <= binHi; i++, b++) {
//				try {
				specData[b] = fftData.mag(i);
//				}
//				catch (Exception e) {
//					System.out.printf("BinHi %d, i %d\n", binHi, i);
//				}
			}
			//			if (nCalls == backgroundBins*2) {
			//				Debug.out.println("Whitening bins processed: " + nCalls);
			//			}
			timeWhitener.addBackground(specData);
		}

		/**
		 * Process new FFT data and look for detections. <p>
		 * Three outputs, smooth spectrogram, detection statistic and detections. 
		 * @param fftDataUnit
		 */
		public void addSingleData(FFTDataUnit fftDataUnit) {
			if (doProcessSingle()) {
				processSingleData(fftDataUnit);
				singleCount++;
			}
		}

		/**
		 * block of single channel data have arrived.
		 * @param blockList
		 */
		public void addBlockData(PamBlockDataList blockList) {
			if (doProcessBlock()) {
				processBlockData(blockList);
				blockCount++;
			}
			else {
				System.out.println("Skip block processing!");
			}
		}

		
		private boolean doProcessSingle() {
			PamBlockParams blockParams = getBlockParams();
			switch (blockParams.blockMode) {
			case BLOCKBYFILE:
				return false;
			case BLOCKED:
				return false;
			case BLOCKFIRST:
				return blockCount > 0;
			case NONE:
				return true;
			case REVERSEFIRST:
				return blockCount > 0;
			default:
				return false;			
			}
		}
		
		private boolean doProcessBlock() {
			PamBlockParams blockParams = getBlockParams();
			switch (blockParams.blockMode) {
			case BLOCKBYFILE:
				return true;
			case BLOCKED:
				return true;
			case BLOCKFIRST:
				return blockCount == 0;
			case NONE:
				return false;
			case REVERSEFIRST:
				return blockCount == 0;
			default:
				return false;			
			}
		}
		
		private void processSingleData(FFTDataUnit fftDataUnit) {
			PamBlockDataList dummyList = new PamBlockDataList();
			dummyList.add(fftDataUnit);
			processBlockData(dummyList);
		}
		
		private  void processBlockData(PamBlockDataList blockList) {

			GPLParameters gplParams = getGplControlledUnit().getGplParameters();

			List<PamDataUnit> unitList = blockList.getList();
			int nFFT = unitList.size();
			if (nFFT == 0) {
				return;
			}

//			if (blockCount == 0) {
//				// going to miss the very first to settle the convolution in the whitener. 
			// don't do this cos we'll not have enough bins in the whitener. 
//				nFFT--;
//			}

			if (nBlockCalls++ == 0) {
				backgroundBins = nFFT;
				timeWhitener = new Base3xWhitener(backgroundBins, gplParams.white_fac_x, binLo, binHi);
				historyMatrix = new MovingMatrix(backgroundBins, nFreqBins);
			}

			/*
			 * Fist time through, need to add it to the whitener to set
			 * the spectral background subtraction
			 */
			ListIterator<PamDataUnit> it = unitList.listIterator();
			while (it.hasNext()) {
				FFTDataUnit fftData = (FFTDataUnit) it.next();
				newBackground(fftData);
			}
			/*
			 * Then go through a second time in order to get a baseline level
			 * for the detection function. This requires whitening for every 
			 * fft partition
			 */
			// a single measure of background for the whole block. 
			double[] mData = timeWhitener.getBackground(); 
			/**
			 * This mData is what we want as a background measurement to write to file. 
			 * If processing in blocks, then it's straight forward to write once per block, otherwise, probably 
			 * want to write every n seconds - will think separately of this is to be controlled globally 
			 * or within the parameters for each process  
			 */
			if (nFFT > 1 || unitList.get(0).getTimeMilliseconds() >= lastBackgroundMillis + gplControlledUnit.getGplParameters().backgroundTimeSecs*1000) {
				FFTDataUnit firstData = (FFTDataUnit) unitList.get(0);
				long endMillis;
				lastBackgroundSample = firstData.getStartSample();
				if (nFFT > 0) { // block processing. 
					lastBackgroundMillis = firstData.getTimeMilliseconds();
					lastBackgroundSample = firstData.getStartSample();
					endMillis = unitList.get(nFFT-1).getEndTimeInMilliseconds();
				}
				else { // single processing
					if (lastBackgroundMillis == 0) { // first time around. 
						lastBackgroundMillis = firstData.getTimeMilliseconds();
						lastBackgroundSample = firstData.getStartSample();
					}
					else {
						lastBackgroundMillis += 10000;
					}
					endMillis = lastBackgroundMillis + 10000;
				}
				double[] bgData = Arrays.copyOf(mData, mData.length);
				// scale for neg frequencies and FFT length, so on a 'Count' scale. 
				double scale = Math.sqrt(2./sourceFFTData.getFftLength());
				for (int i = 0; i < bgData.length; i++) {
					bgData[i] *= scale;
				}
				lastBackground = new SpecBackgroundDataUnit(lastBackgroundMillis, lastBackgroundSample, firstData.getChannelBitmap(), endMillis-lastBackgroundMillis, binLo, binHi, bgData);
				backgroundManager.getBackgroundDataBlock().addPamData(lastBackground);
			}
			
			/*
			 * Checked to here and mData seems mathematically identical to TH code. 
			 */
			// now loop through again and whiten every partition. 
			double[][] wData = new double[nFFT][];
			it = unitList.listIterator();
			int iw = 0;
			while (it.hasNext()) {
				FFTDataUnit nextFFTDataUnit = (FFTDataUnit) it.next();
				ComplexArray fftData = nextFFTDataUnit.getFftData();
				for (int i = binLo, b = 0; i <= binHi; i++, b++) {
					specData[b] = fftData.mag(i);
				}
				wData[iw] = timeWhitener.whitenData(mData, specData);
				/*
				 * Since whitenData includes a 3x3 convolution, the data coming back 
				 * need to be delayed by one frame. 
				 */
				FFTDataUnit fftDataUnit = previousFFTDataUnit;
				previousFFTDataUnit = nextFFTDataUnit;
				if (isFirst) {
					isFirst = false;
					continue;
				}
				/*
				 * Above, that's the whitening done ala TH, at output of GPL_Whiten function
				 * Note that the whitened data has been passed and seems to have same output at TH 
				 * code apart from offset by 1 in convolution. 
				 * 
				 * with an extra bin top and bottom so it can be used in a convolution later on. 
				 * If blocking, the above should be broken up a little so that it only calculates the 
				 * means values, then the mean subtraction and convolution smooth is re-done for all 
				 * data in the block.  
				 */
				/*
				 * And output the whitened data into a new stream
				 */
				ComplexArray whiteComplex = new ComplexArray(sourceFFTData.getFftLength()/2);
				/*
				 * And put the data into it, remembering to offset index in whitened data by the padding. 
				 */
				for (int b = binLo, is = 0; b <= binHi; b++, is++) {
					whiteComplex.setReal(b, Math.max(wData[iw][is], 0.));
				}
				FFTDataUnit newData = new FFTDataUnit(fftDataUnit.getTimeMilliseconds(), fftDataUnit.getChannelBitmap(), 
						fftDataUnit.getStartSample(), fftDataUnit.getSampleDuration(), whiteComplex, fftDataUnit.getFftSlice());
				newData.setSequenceBitmap(fftDataUnit.getSequenceBitmap());
				whitenedSpectrogram.addPamData(newData);
				/*
				 * Now gets interesting again, since there is (yet) another whitener, which needs data from the whole 
				 * block, so put the data into the history matrix, then get out of this loop.  
				 */
				historyMatrix.addData(wData[iw]);


				iw++;
			}
			/*
			 * Here, we seem to have wData numerically identical to TH's sp_whiten as returned from 
			 * GPL_whiten, called from line 17 in GPLV2 
			 * Comparing TH code, his variable u is my norm_v, his y is my norm_h
			 */
			/**
			 * Now do loops through time and frequency, dividing by norms of each. 
			 * First do a sum over frequency for all times. i.e. for each time take all it's frequencies and 
			 * sum them and subtract them off. The next big loop is basically  lines 8 and 9 in GPL_Quiet 
			 */
			//			it = unitList.listIterator();
			double[] yFac2 = historyMatrix.getSumFreq2();
			// don't take the sqrt of the original data - it needs to stay in place !!!
			double[] yFac = new double[yFac2.length];
			for (int i = 0; i < nFreqBins; i++) {
				yFac[i] = Math.sqrt(yFac2[i]); // same as divisor in line 9. 
			}
			double[] base_in_arr = new double[nFFT];
			double[] uFacArr = historyMatrix.getSumTime2(); 
			if (norm_v.length < nFFT) {
				// can sometimes happen with some rounding of how many FFT's are in a block sometimes
				// being one extra
				norm_h = new double[backgroundBins][nFreqBins];
				norm_v = new double[backgroundBins][nFreqBins];
			}
			for (iw = 0; iw < nFFT; iw++) {
				if (wData[iw] == null) {
					break; // happens once on the very first call 
				}
				// not convinced this is getting the right column, so do locally. 
//				int timeIndex = historyMatrix.getTimeIndex(nFFT-iw-1);
//				double uFac = Math.sqrt(uFacArr[timeIndex]);
				double uFac = 0;
				for (int i = 0; i < nFreqBins; i++) {
					uFac += wData[iw][i]*wData[iw][i];
				}
				uFac = Math.sqrt(uFac);
				for (int i = 0; i < nFreqBins; i++) {
					norm_v[iw][i] = wData[iw][i]/uFac;
					norm_h[iw][i] = wData[iw][i]/yFac[i];
					// these are very close to u and y at lines 8 and 9 in sp_whiten. 
				}
				/**
				 * Yet another whitener, which needs to be filled before it can be used
				 * to whiten data. So need to break the loop here. 
				 */
				yWhitener1.addData(norm_h[iw]); // whiten horizontally over time
				norm_v[iw] = whitenVector.whitenVector(norm_v[iw], 1.); // whiten vertically for a single bin.
			}
			// now whiten norm_h and proceed ...
			for (iw = 0; iw < nFFT; iw++) {
				if (wData[iw] == null) {
					break; // happens once on the very first call 
				}
				norm_h[iw] = yWhitener1.whitenData(norm_h[iw]);
				for (int i = 0; i < nFreqBins; i++) {
					bas[i] = Math.pow(Math.abs(norm_v[iw][i]), gplParams.xp1) * Math.pow(Math.abs(norm_h[iw][i]), gplParams.xp2);
				}
				int low_off = sum_binLo-binLo;
				int high_off = sum_binHi-binLo;
				// sum the squared magnitudes....
				double base_in = 0;
				for (int i = 0; i <= high_off; i++) {
					base_in += bas[i]*bas[i];
				}
				baseSort.addData(base_in);
				base_in_arr[iw] = base_in; // same as b0 at line 51 in GLP_quiet. 
			}
			/**
			 * At this point, base_in_arr is identical to TH's baseline0  at L22 in GPL_Quiet which gets repeated in its
			 * calculation and is also the same as base_in at line 65 in GPL_V2
			 */
			/**
			 * Everything above needed to be blocked in order to do the noise normalisation for a block. 
			 * It happened in three separate loops, the first whitening loop which subtracted a mean spectrogram, 
			 * the second which whitened the data and built a history matrix, then a third which used
			 * the history matrix to do even more whitening and produce base_in which is nearly the final scalar detection 
			 * input value.  Can now take the base_in values, use the mean value from baseSort to normalise them 
			 * and feed into the detector. 
			 */

			/*
			 * for the detector we need to bring together quite a few of these lists again 
			 * since it will want the original fft data as well as wData. 
			 */
			double baseMu = baseSort.getCentralMean();
			//			baseMu is quiet_base in GPL_quiet at line 51
			double noise_floor = 0., quiet_base = 0.;
			it = unitList.listIterator();
			iw = 0;
			noise_floor = Double.MAX_VALUE;
			quiet_base = baseMu;
			noise_floor = -(baseSort.getSortedData(0)-baseMu)/baseMu;
			double[] baseline0 = new double[nFFT];
			while (it.hasNext() && iw < nFFT) {
				FFTDataUnit fftDataUnit = (FFTDataUnit) it.next();
				if (wData[iw] == null) {
					break; // happens once on the very first call 
				}
				double base_in = base_in_arr[iw];
				baseline0[iw] = (base_in-baseMu)/baseMu; // Matlab lines 51 and 52 together to whiten and normalise. . 
//				if (!useGPLQuiet) {
					/**
					 * the same as would come from GPLQuit unless more advanced options are 
					 * used (which currently aren't coded in any case)
					 */
//					quiet_base = baseMu;
//					noise_floor = - baseSort.getSortedData(0);
					// noise_floor is the lowest available noise measurement. 
//				}
					iw++;
			}
			//0.9494291111374171
			it = unitList.listIterator();
			iw = 0;
			while (it.hasNext() && iw < nFFT) {
				FFTDataUnit fftDataUnit = (FFTDataUnit) it.next();
				if (wData[iw] == null) {
					break; // happens once on the very first call 
				}
//				base_in = baseline0[iw]/quiet_base; // quiet_base is exactly same as base_mu
				/*
				 * Final call parameters are
				 * 	DetectedPeak newPeak = peakDetector.detectPeaks(fftDataUnit, wData, base_in, 
				 *	gplParams.noise_ceiling * noise_floor, gplParams.thresh * noise_floor);		
				 */
				runDetector(fftDataUnit, wData[iw], baseline0[iw], 
						gplParams.noise_ceiling * noise_floor, gplParams.thresh * noise_floor);

				iw++;

			}

			//			System.out.println("call done");

			//			/**
			//			 * Then a third time to run the detector
			//			 */
			//			it = unitList.listIterator();
			//			while (it.hasNext()) {
			//				FFTDataUnit fftData = (FFTDataUnit) it.next();
			//				runDetector(fftData);
			//			}

		}

		@Deprecated
		private void OldrunSingleBackground(FFTDataUnit fftDataUnit) {
			// dead old code. don't use, but not ready to delete
			//			nCalls++;
			//			if (nCalls == backgroundBins*3-10) {
			//				System.out.println("That's three minutes done");
			//			}
			/**
			 * Whiten the data with a background subtraction. Note that SIO code includes
			 * option of vertical (frequency) whitening, but it's not used in the examples 
			 * they've given, so i'm not bothering with it. 
			 * This is a replication of code in GPL_whiten and whiten_matrix.  
			 */
			ComplexArray fftData = fftDataUnit.getFftData();
			for (int i = binLo, b = 0; i <= binHi; i++, b++) {
				specData[b] = fftData.mag(i);
			}
			//			if (nCalls == backgroundBins*2) {
			//				Debug.out.println("Whitening bins processed: " + nCalls);
			//			}
			double[] mData = timeWhitener.getBackground();
			double[] wData = timeWhitener.whitenData(mData, specData);
			/*
			 * Above, that's the whitening done ala HT, Note that the whitened data has been passed
			 * with an extra bin top and bottom so it can be used in a convolution later on. 
			 * If blocking, the above should be broken up a little so that it only calculates the 
			 * means values, then the mean subtraction and convolution smooth is re-done for all 
			 * data in the block.  
			 */
			ComplexArray whiteComplex = new ComplexArray(sourceFFTData.getFftLength()/2);
			/*
			 * And put the data into it, remembering to offset index in whitened data by the padding. 
			 */
			for (int b = binLo, is = 0; b <= binHi; b++, is++) {
				whiteComplex.setReal(b, Math.max(wData[is], 0.));
			}
			FFTDataUnit newData = new FFTDataUnit(fftDataUnit.getTimeMilliseconds(), fftDataUnit.getChannelBitmap(), 
					fftDataUnit.getStartSample(), fftDataUnit.getSampleDuration(), whiteComplex, fftDataUnit.getFftSlice());
			newData.setSequenceBitmap(fftDataUnit.getSequenceBitmap());
			whitenedSpectrogram.addPamData(newData);
			if (allNan) {
				for (int i = 1; i < wData.length-1; i++) {
					if (Double.isNaN(wData[i]) == false) {
						allNan = false;
						break;
					}
				}
				if (allNan) return;
			}
			/*
			 * Now do the last line of code that was in GPL_Whiten. didn't do it earlier
			 * so that could make the whitened spectrogram on the same scale an ordinary fft data. 
			 * sp_whiten=abs((sp_whiten./(mu*ones(1,parm.nbin))).')';
			 * 
			 * This is now done in whiten since it should happen before the smoothing kernel. 
			 */
			//			for (int i = 0; i < wData[0].length; i++) {
			//				wData[0][i] = Math.abs(wData[0][i]/wData[1][i]);
			//			}
			//			if (++totalCalls == backgroundBins*3-20) {
			//				System.out.println("Thats three minutes gone ...");
			//			}

			double noise_floor = 0., quiet_base = 0.;
			if (useGPLQuiet) {
				/**
				 * the Matlab version of this does some very fancy selection of quiet parts of the
				 * data. however, the output seems not to be used in any of the actual configurations 
				 * provided by SIO, so not worth calculating it for now. Calling this 
				 * gives mathematically identical values for noise_floor and quiet_base as are
				 * extracted below for current configurations. 
				 */
				QuietStruct quiet = gplQuiet.processData(specData, wData);
				if (quiet == null) {
					return;
				}
				noise_floor = quiet.noise_floor;
				quiet_base = quiet.quiet_base;
			}

			historyMatrix.addData(wData);
			// now the main processing loop...
			// Tyler has one factor per time, but we do one time at a go, so only one value. 
			double uFac = Math.sqrt(historyMatrix.getSumTime2()[historyMatrix.getiTime()]);
			double[] yFac = historyMatrix.getSumFreq2();
			for (int i = 0; i < nFreqBins; i++) {
				norm_v[0][i] = wData[i]/uFac;
				norm_h[0][i] = wData[i]/Math.sqrt(yFac[i]);
			}
			norm_h[0] = yWhitener1.whitenData(norm_h[0]); // whiten horizontally over time
			norm_v[0] = whitenVector.whitenVector(norm_v[0], 1.); // whiten vertically for a single bin. 
			GPLParameters gplParams = getGplControlledUnit().getGplParameters();
			for (int i = 0; i < nFreqBins; i++) {
				bas[i] = Math.pow(Math.abs(norm_v[0][i]), gplParams.xp1) * Math.pow(Math.abs(norm_h[0][i]), gplParams.xp2);
			}
			int low_off = sum_binLo-binLo;
			int high_off = sum_binHi-binLo;
			// sum the squared magnitudes....
			double base_in = 0;
			for (int i = 0; i <= high_off; i++) {
				base_in += bas[i]*bas[i];
			}
			//			if (fftDataUnit.getStartSample() > 60000) {
			//				System.out.println("Debug");
			//			}
			/**
			 * Really, up to this second sort all needs to be in the blocked part of the process, probably 
			 * as a second loop through all the data to correctly settle things down 
			 * The whitener has a sort in it, which removes background. What this second sort does is look 
			 * at the level after whitening, then set the threshold based on the central value over a period. 
			 * i.e. this is really part of the background process, requiring a second full pass through the 
			 * data, once to set the whitening and a second time to set the threshold after whitening.  
			 */
			baseSort.addData(base_in);
			double baseMu = baseSort.getCentralMean();
			double b0 = base_in-baseMu;
			if (!useGPLQuiet) {
				/**
				 * the same as would come from GPLQuit unless more advanced options are 
				 * used (which currently aren't coded in any case)
				 */
				quiet_base = baseMu;
				noise_floor = (quiet_base - baseSort.getSortedData(0)) / quiet_base;
			}

			base_in = b0/quiet_base; // quiet_base is exactly same as base_mu
			//			dummySort.addData(base_in);
			//			base_in = quiet.baseline0;


			//			quiet.noise_floor = 1.;

		}		
		/**
		 * This is the call to the detector, which is remembering state, will mostly return
		 * null, but when there has been a detection, will return an object with time and 
		 * frequency information. 
		 */
		public void runDetector(FFTDataUnit fftDataUnit, double[] wData, double base_in, double ceilnoise, double threshfloor) {		
			//			DetectedPeak newPeak = peakDetector.detectPeaks(fftDataUnit, wData, base_in, 
			//					gplParams.noise_ceiling * noise_floor, gplParams.thresh * noise_floor);		
			DetectedPeak newPeak = peakDetector.detectPeaks(fftDataUnit, wData, base_in, 
					ceilnoise, threshfloor);
			/*
			 * Here: need to call contour extractor. Contour extractor needs an array of quitened data for this time period 
			 * from startSample to endSample.  
			 */
			DetectedPeak inPeak = newPeak;
			newPeak = wantPeak(newPeak);
//			if (inPeak != null) {
//				System.out.printf("Peak from %d to %d want = %s\n", inPeak.getStartBin(), inPeak.getEndBin(), new Boolean(newPeak != null));
//			}
			ArrayList<GPLContour> contours = null;
			if (newPeak != null) {
				contours = contourFinder.findContours(newPeak);
				contours = contourFinder.mergeContours(contours, gplControlledUnit.getGplParameters().contourMerge);
//				if (contours != null) {
//					System.out.printf("Found %d contours at %3.1fs: areas (energy) ",  contours.size(), (double)fftDataUnit.getStartSample()/100.);
//					for (int i = 0; i < contours.size(); i++) {
//						GPLContour aCont = contours.get(i);
//						System.out.printf(", %d (%3.1f)", aCont.getArea(), aCont.getTotalEnergy());
//					}
//					System.out.printf("\n");
//					for (int i = 0; i < contours.size(); i++) {
//						GPLContour aCont = contours.get(i);
//						int[][] outline = aCont.getOutline();
//						if (outline == null) {
//							continue;
//						}
//						
//					}
//				}
			}

			boolean up = peakDetector.getPeakState() > 0;

			//			int state = 0;
			//			if (base_in > (gplParams.noise_ceiling * noise_floor)) {
			//				state = 1;
			//				if (base_in > (gplParams.thresh * noise_floor)) {
			//					state = 2;
			//				}
			//			}
			//			GPLStateDataUnit sdu = new GPLStateDataUnit(newData.getTimeMilliseconds(), sequence, base_in, peakDetector.getPeakState());
			GPLStateDataUnit sdu = new GPLStateDataUnit(fftDataUnit.getTimeMilliseconds(), sequence, fftDataUnit.getChannelBitmap(), 
					base_in, ceilnoise, threshfloor, peakDetector.getPeakState());
			stateDataBlock.addPamData(sdu);
			
			GPLParameters params = gplControlledUnit.getGplParameters();

			if (newPeak != null && !gplControlledUnit.isViewer() && contours != null) {				
				double dT = sourceFFTData.getHopSamples() / sourceFFTData.getSampleRate();
				double dF = sourceFFTData.getSampleRate() / sourceFFTData.getFftLength();
				long startSample = newPeak.getStartSample();
				long endSample = newPeak.getEndSample();
				long startMillis = absSamplesToMilliseconds(startSample);
				int storedContours = 0;
				for (GPLContour aContour : contours) {
					if (wantContour(aContour, params.minContourArea, storedContours) == false) {
						continue;
					}
//					startSample += aContour.getMinTBin()*sourceFFTData.getHopSamples();
//					endSample = startSample + (aContour.getMaxTBin()- aContour.getMinTBin()+1)*sourceFFTData.getHopSamples();
//					aContour.shiftTimeBins();
					//			long endSample = fftDataUnit.getStartSample()+sourceFFTData.getFftLength();
					//			System.out.printf("New detection %d bins %d millis peak %3.1f start %s, end %s\n",
					//					newPeak.endBin-newPeak.startBin+1, endMillis-startMillis, Math.log10(newPeak.maxValue),
					//					PamCalendar.formatDateTime(startMillis), PamCalendar.formatTime(endMillis));
					GPLDetection newDet = new GPLDetection(startMillis, fftDataUnit.getChannelBitmap(), startSample, 
							endSample-startSample, dT, dF, aContour);
					double spl = Math.sqrt(aContour.getTotalEnergy()/aContour.getBinsDuration());
					newDet.setSignalSPL((float) spl);
					newDet.setSequenceBitmap(1<<sequence);
					if (lastBackground != null) {
						int minF = aContour.getMinFBin();
						int maxF = aContour.getMaxFBin();
						newDet.setNoiseBackground((float) lastBackground.getCountSPL(minF, maxF));
					}
					long durMillis = relSamplesToMilliseconds(endSample-startSample);
					newDet.setDurationInMilliseconds(durMillis);
					double[] freq = new double[2];
					freq[0] = (aContour.getMinFBin()) * getSampleRate() / sourceFFTData.getFftLength();
					freq[1] = (aContour.getMaxFBin() + 1) * getSampleRate() / sourceFFTData.getFftLength();
					newDet.setFrequency(freq);
					newDet.setPeakValue(newPeak.getMaxValue());
					// copy over any localisation information in the fft data (will have if it's a beam). 
					AbstractLocalisation loc = fftDataUnit.getLocalisation();
					if (loc != null && loc.getClass() == BeamFormerLocalisation.class) {
						loc = ((BeamFormerLocalisation) loc).clone();
						loc.setParentDetection(newDet);
						newDet.setLocalisation(loc);
					}
					gplBeamSelector.addPamData(newDet);
					storedContours++;
				}


			}
			gplBeamSelector.setSequenceState(fftDataUnit.getChannelBitmap(), sequence, up);

		}
		
		private boolean wantContour(GPLContour gplContour, int minContourArea, int storedContours) {
			if (gplContour instanceof NullGPLContour && storedContours == 0) {
				/**
				 * slightly silly code, but there is always a null contour at the end of the list, which we 
				 * might save if nothing else is saved. 
				 */
				return true;
			}
			if (gplContour.getArea() < minContourArea) {
				return false;
			}
			if (gplContour.getOutline() == null) {
				return false;
			}
			return true;
		}

		/**
		 * Check we want the detected peak. Currently only tests its length
		 * @param newPeak detected peak
		 * @return the peak if we want it, or null. 
		 */
		private DetectedPeak wantPeak(DetectedPeak newPeak) {
			if (newPeak == null) {
				return null;
			}
			GPLParameters gplParams = gplControlledUnit.getGplParameters();
			int nFFT = newPeak.getEndBin()-newPeak.getStartBin()+1;
			double secs = nFFT * sourceFFTData.getHopSamples() / getSampleRate();
			if (secs < gplParams.minCallLengthSeconds || secs > gplParams.maxCallLengthSeconds) {
				return null;
			}

			return newPeak;
		}
	}

	/**
	 * @return the gplControlledUnit
	 */
	public GPLControlledUnit getGplControlledUnit() {
		return gplControlledUnit;
	}


	/**
	 * @return the whitenedSpectrogram
	 */
	public FFTDataBlock getWhitenedSpectrogram() {
		return whitenedSpectrogram;
	}


	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#getFrequencyRange()
	 */
	@Override
	public double[] getFrequencyRange() {
		double[] fRange = {gplControlledUnit.getGplParameters().minFreq, gplControlledUnit.getGplParameters().maxFreq};
		return fRange;
	}


	/**
	 * @return the stateDataBlock
	 */
	public GPLStateDataBlock getStateDataBlock() {
		return stateDataBlock;
	}


	/**
	 * @return the sourceFFTData
	 */
	public FFTDataBlock getSourceFFTData() {
		return sourceFFTData;
	}


	public int getSourceFFTLength() {
		if (sourceFFTData == null) {
			return 2;
		}
		else {
			return sourceFFTData.getFftLength();
		}
	}

}
