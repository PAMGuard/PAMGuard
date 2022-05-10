package rawDeepLearningClassifier.segmenter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import PamController.PamController;
import PamDetection.PamDetection;
import PamDetection.RawDataUnit;
import PamUtils.PamArrayUtils;
import PamUtils.PamUtils;
import PamView.GroupedSourceParameters;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.debug.Debug;
import clickDetector.ClickDetection;
import clipgenerator.ClipDataUnit;
import rawDeepLearningClassifier.DLControl;
import PamUtils.PamCalendar;


/**
 * Acquires raw sound data and then sends off to a deep learning classifier.
 * 
 * @author Jamie Macaulay 
 */

public class SegmenterProcess extends PamProcess {

	/**
	 * the maximum allowed drift between the sample clocks and the file clock before the clock is reset. 
	 */
	private static final double MAX_MILLIS_DRIFT = 2;

	/**
	 * Reference to the deep learning control. 
	 */
	private DLControl dlControl;

	/**
	 * The current raw data unit chunks. Each array element is a channel group. 
	 */
	private GroupedRawData[] currentRawChunks;

	/**
	 * The current raw data unit chunks. Each array element is a channel group. 
	 */
	private GroupedRawData[][] nextRawChunks;

	/**
	 * Holds segments of raw sound data
	 */
	private SegmenterDataBlock segmenterDataBlock;

	PamSymbol defaultSymbol = new PamSymbol(PamSymbolType.SYMBOL_DIAMOND, 10, 12, false,
			Color.CYAN, Color.CYAN); 


	public SegmenterProcess(DLControl pamControlledUnit, PamDataBlock parentDataBlock) {
		super(pamControlledUnit, parentDataBlock);
		dlControl = pamControlledUnit;

		//sourceDataBlock.addObserver(this);

		setParentDataBlock(parentDataBlock);

		//			AddOutputDataBlock(outputData = new RecyclingDataBlock(PamguardMVC.DataType.FFT, "Raw FFT Data", 
		//					this, sourceDataBlock.getSourceProcess(), fftControl.fftParameters.channelMap));
		//			addOutputDataBlock(outputData = new RecyclingDataBlock<FFTDataUnit>(FFTDataUnit.class, "Raw FFT Data", 
		//					this, fftControl.fftParameters.channelMap));

		segmenterDataBlock = new SegmenterDataBlock("Segmented Raw Data", this,
				dlControl.getDLParams().groupedSourceParams.getChanOrSeqBitmap());
		addOutputDataBlock(segmenterDataBlock);

		setProcessName("Segmenter");  

		//allow drawing of the segmenter on the spectrogram
		PamDetectionOverlayGraphics overlayGraphics = new PamDetectionOverlayGraphics(segmenterDataBlock,  defaultSymbol);
		overlayGraphics.setDetectionData(true);
		segmenterDataBlock.setOverlayDraw(overlayGraphics);

		setupSegmenter();
	}


	@Override
	public void prepareProcess() {
		setupSegmenter();
	}

	/**
	 * A list of data block class types which are compatible as parent data blocks
	 * for the PamProcess. This can return null, e.g. in the case of Acquisition
	 * process.
	 * 
	 * @return a list of PamDataBlock sub class types which can be used as parent
	 *         data blocks for the process.
	 */
	@Override
	public ArrayList getCompatibleDataUnits(){
		return new ArrayList<Class<? extends PamDataUnit>>(Arrays.asList(RawDataUnit.class, ClickDetection.class, ClipDataUnit.class));
	}



	/**
	 * called for every process once the system model has been created. 
	 * this is a good time to check out and find input data blocks and
	 * similar tasks. 
	 *
	 */
	@Override
	public void setupProcess() {
		setupSegmenter(); 
		super.setupProcess();
	}

	/**
	 * Set up the DL process. 
	 */
	public void setupSegmenter() {

		if (dlControl.getDLParams()==null) {
			System.err.println("SegmenterProcess.setupSegmenter: The DLParams are null???"); 

		}

		if (dlControl.getDLParams().groupedSourceParams==null) {
			dlControl.getDLParams().groupedSourceParams = new GroupedSourceParameters(); 
			System.err.println("Raw Deep Learning Classifier: The grouped source parameters were null. A new instance has been created: Possible de-serialization error.");
		}

		int[] chanGroups = dlControl.getDLParams().groupedSourceParams.getChannelGroups();

		//initialise an array of nulls. 
		if (chanGroups!=null) {
			currentRawChunks = new GroupedRawData[chanGroups.length]; 
			nextRawChunks = new GroupedRawData[chanGroups.length][]; 
		}


		//set up connection to the parent
		PamDataBlock rawDataBlock = null;

		/*
		 * Data block used to be by number, now it's by name, but need to handle situations where
		 * name has not been set, so if there isn't a name, use the number !
		 */
		if (getSourceParams().getDataSource()  != null) {
			//use the data block set by the user. 
			rawDataBlock = (PamDataBlock) PamController.getInstance().getDataBlockByLongName(getSourceParams().getDataSource()); 
		}
		else {
			//try and find a raw data block to use
			if (PamController.getInstance().getRawDataBlocks().size()>0) {
				rawDataBlock = PamController.getInstance().getRawDataBlocks().get(0); 
				if (rawDataBlock != null) {
					getSourceParams().setDataSource(rawDataBlock.getDataName());
				}
			}
		}


		if (rawDataBlock==null) return;

		setParentDataBlock(rawDataBlock);

	}

	/*
	 * Segments raw data and passes a chunk of multi -channel data to a deep learning algorithms. 
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void newData(PamObservable obs, PamDataUnit pamRawData) {
		
		//if raw data then the dl data selector will be null and should always score data as O. 
		

			if (obs == getParentDataBlock()) {
				newData(pamRawData); 
			}
	}


	/*
	 * Segments raw data and passes a chunk of multi -channel data to a deep learning algorithms. 
	 * (non-Javadoc)
	 */
	public void newData(PamDataUnit pamRawData) {

		if (!dlControl.getDLParams().useDataSelector || dlControl.getDataSelector().scoreData(pamRawData)>0) {	

			//System.out.println("New data for segmenter: " + pamRawData); 
			if (pamRawData instanceof RawDataUnit) {
				newRawDataUnit(pamRawData); 
			}
			else if (pamRawData instanceof ClickDetection) {
				newClickData( pamRawData);
			}
			else if (pamRawData instanceof ClipDataUnit)  {
				newClipData(pamRawData);
			}
		}

	}


	/**
	 * A new raw data unit. 
	 * @param obs - the PAM observable 
	 * @param pamRawData - PAM raw data. 
	 */
	public void newRawDataUnit(PamDataUnit pamRawData) {

		//the raw data units should appear in sequential channel order  
		//		System.out.println("New raw data in: chan: " + PamUtils.getSingleChannel(pamRawData.getChannelBitmap()) + " Size: " +  pamRawData.getSampleDuration()); 

		RawDataUnit rawDataUnit = (RawDataUnit) pamRawData;

		double[] rawDataChunk = rawDataUnit.getRawData();

		int iChan = PamUtils.getSingleChannel(rawDataUnit.getChannelBitmap());

		newRawData(rawDataUnit, rawDataChunk,  iChan);

	}


	/**
	 * Process new clip data. 
	 * @param obs - the PAM observable 
	 * @param pamRawData - the new raw data unit 
	 */
	public void newClipData(PamDataUnit pamRawData) {

		//the raw data units should appear in sequential channel order  
		//System.out.println("New raw data in: chan: " + PamUtils.getSingleChannel(pamRawData.getChannelBitmap()) 
		//+ " Size: " +  pamRawData.getSampleDuration()); 

		ClipDataUnit rawDataUnit = (ClipDataUnit) pamRawData;

		double[][] rawDataChunk = rawDataUnit.getRawData(); 

		newRawDataChunk(rawDataUnit, rawDataChunk); 
	}


	/**
	 * Process new click data. 
	 * @param obs - the PAM observable
	 * @param pamRawData
	 */
	public void newClickData(PamDataUnit pamRawData) {

		//the raw data units should appear in sequential channel order  
		//		System.out.println("New raw data in: chan: " + PamUtils.getSingleChannel(pamRawData.getChannelBitmap()) + " Size: " +  pamRawData.getSampleDuration()); 

		ClickDetection clickDataUnit = (ClickDetection) pamRawData;

		double[][] rawDataChunk = clickDataUnit.getWaveData(); 

		newRawDataChunk(clickDataUnit, rawDataChunk); 
	}



	/**
	 * Segment a single new raw data chunk. This is for a discrete data chunk i.e.
	 * the data is not a continuous time series of acoustic data but a clip of some
	 * kind of that should be passed a s single detection to a deep learning model.
	 * 
	 * @param pamDataUnit  - the PAM data unit containing the chunk of raw data.
	 * @param rawDataChunk - the raw chunk of data from the data unit.
	 * @param forceSave    - all segments to be saved, even if the segment is not
	 *                     full e.g. for clicks detections.
	 */
	private synchronized void newRawDataChunk(PamDataUnit pamDataUnit, double[][] rawDataChunk) {
		//Note it is impoetant this is synchronized to prevent currentRawChunks=null being called and
		//causing a null pointer exception in newRawData; 
		
		//System.out.println("----New raw data unit: " + pamDataUnit.getUID()); 

		//reset for each click 
		currentRawChunks=null; 
		nextRawChunks =null; 

		//initialise arrays
		if (rawDataChunk!=null) {
			currentRawChunks = new GroupedRawData[rawDataChunk.length]; 
			nextRawChunks = new GroupedRawData[rawDataChunk.length][]; 
		}
		else {
			System.err.println("SegmeterProcess: the data unit waveform is null");
		}

		int[] chans  = PamUtils.getChannelArray(pamDataUnit.getChannelBitmap()); 

		//PamArrayUtils.printArray(chans); 

		//pass the raw click data to the segmenter
		for (int i=0;i<chans.length; i++) {
			newRawData(pamDataUnit,
					rawDataChunk[i], chans[i], true);
			
			//the way that the newRawdata works is it waits for the next chunk and copies all relevant bits
			//from previous chunks into segments. This is fine for continuous data but means that chunks of data
			//don't get their last hop...
		}

		//got to save the last chunk of raw data -even if the segment has not been filled. 
		saveRawGroupData(true);
	}


	/**
	 * Take a raw sound chunk of data and segment into discrete groups. This handles
	 * much situations e.g. where the segment is much larger than the raw data or
	 * where the segment is much small than each rawDataChunk returning multiple
	 * segments.
	 * 
	 * @param unit         - the data unit which contains relevant metadata on time
	 *                     etc.
	 * @param rawDataChunk - the sound chunk to segment extracted form the data
	 *                     unit.
	 * @param iChan        - the channel that is being segmented
	 */
	public void newRawData(PamDataUnit unit, double[] rawDataChunk, int iChan) {
		newRawData(unit, rawDataChunk, iChan, false); 
	}

	/**
	 * Take a raw sound chunk of data and segment into discrete groups. This handles
	 * much situations e.g. where the segment is much larger than the raw data or
	 * where the segment is much small than each rawDataChunk returning multiple
	 * segments.
	 * 
	 * @param unit         - the data unit which contains relevant metadata on time
	 *                     etc.
	 * @param rawDataChunk - the sound chunk to segment extracted form the data
	 *                     unit.
	 * @param iChan        - the channel that is being segmented
	 * @param forceSave    - make sure that all data is passed into the buffers and
	 *                     do not wait for the next data unit. This is used to make
	 *                     sure that discrete chunks have their full number of
	 *                     segments saved.
	 */
	public synchronized void newRawData(PamDataUnit unit, double[] rawDataChunk, int iChan, boolean forcesave) {

		long timeMilliseconds = unit.getTimeMilliseconds();
		long startSampleTime = unit.getStartSample(); 

		//System.out.println("Segmenter: RawDataIn: chan: 1 " + getSourceParams().countChannelGroups() + currentRawChunks); 

		if (currentRawChunks==null) {
			System.err.println("Current raw chunk arrays are null");
			return;
		}

		//TODO - what if the raw data lengths are larger than the segments by a long way?
		for (int i=0; i<getSourceParams().countChannelGroups(); i++) {

			//System.out.println("Segmenter: RawDataIn: chan: " + iChan+ "  " + PamUtils.hasChannel(getSourceParams().getGroupChannels(i), iChan) + " grouped source: " + getSourceParams().getGroupChannels(i)); 

			if (PamUtils.hasChannel(getSourceParams().getGroupChannels(i), iChan)) {

				//				System.out.println("Data holder size: " + unit.getChannelBitmap()); 

				if (currentRawChunks[i]==null) {
					//create a new data unit - should only be called once after initial start.  
					currentRawChunks[i] = new GroupedRawData(timeMilliseconds, getSourceParams().getGroupChannels(i), 
							startSampleTime, dlControl.getDLParams().rawSampleSize, dlControl.getDLParams().rawSampleSize); 
					currentRawChunks[i].setParentDataUnit(unit);; 
				}

				//				System.out.println("------------"); 
				//				System.out.println("Group time: " + PamCalendar.formatDBDateTime(currentRawChunks[i].getTimeMilliseconds(), true)); 
				//				System.out.println("Data unit time: " + PamCalendar.formatDBDateTime(timeMilliseconds)); 
				//				System.out.println(": " + Math.abs(currentRawChunks[i].getTimeMilliseconds()  - timeMilliseconds) + " : " +  1000*currentRawChunks[i].getRawDataPointer()[0]/this.getSampleRate()); 

				//current time milliseconds is referenced from the first chunk with samples added. But, this can mean, especially for long period of times and multiple 
				//chunks that things get a bit out of sync. So make a quick check to ensure that time millis is roughly correct. If not then fix. 
				if (Math.abs(((double) currentRawChunks[i].getTimeMilliseconds() + 1000.*currentRawChunks[i].getRawDataPointer()[0]/this.getSampleRate()) - timeMilliseconds)>MAX_MILLIS_DRIFT) {
					//Debug.out.println("DL SEGMENTER: RESETTING TIME: "); 
					currentRawChunks[i].setTimeMilliseconds((long) (timeMilliseconds - 1000.*currentRawChunks[i].getRawDataPointer()[0]/this.getSampleRate()));
					currentRawChunks[i].setStartSample(startSampleTime-currentRawChunks[i].getRawDataPointer()[0]);
				}

				int chanMap = getSourceParams().getGroupChannels(i); 

				int groupChan = PamUtils.getChannelPos(iChan, chanMap); 

				//how much of the chunk should we copy? 
				//				int lastPos = currentRawChunks[i].rawDataPointer[groupChan]+ 1 + rawDataChunk.length; 
				//
				//				int copyLen = rawDataChunk.length;
				//
				//
				//				if (lastPos>currentRawChunks[i].rawData[groupChan].length) {
				//					copyLen=copyLen-(lastPos-currentRawChunks[i].rawData[groupChan].length); 
				//				}
				//
				//				//update the current grouped raw data unit with new raw data. 
				//				System.arraycopy(rawDataChunk, 0, currentRawChunks[i].rawData[groupChan], currentRawChunks[i].rawDataPointer[groupChan]+1, copyLen); 
				//
				//				currentRawChunks[i].rawDataPointer[groupChan]=currentRawChunks[i].rawDataPointer[groupChan] + copyLen; 

				
				//the overflow is the spill over in samples from the sound data chunk added to the segment i.e. if the chunk is greater than the segment
				// then there is a spill over to the next segment which needs to be dealt with
				int overFlow = currentRawChunks[i].copyRawData(rawDataChunk, 0 , rawDataChunk.length, groupChan);

				//				System.out.println("Data holder size: " + currentRawChunks[i].rawDataPointer[groupChan]); 


				/**
				 * The overflow should be saved into the next chunk. But what if the raw sound data is very large and the overflow is 
				 * in fact multiple segments. Need to iterate through the overflow data. 
				 */
				//System.out.println("Current chunk time: " +  PamCalendar.formatDBDateTime(currentRawChunks[i].getTimeMilliseconds(), true) + " Overflow: " + overFlow + " Raw len: " + rawDataChunk.length); 
				if (overFlow>0 || forcesave) {

					//how many new raw chunks do we need? 
					if (nextRawChunks[i]==null) {
						//need to figure out how many new raw chunks we may need. 
						//for last zero padded segment to be included. 
						//int nChunks = (int) Math.ceil((overFlow+dlControl.getDLParams().sampleHop)/(double) dlControl.getDLParams().sampleHop); 
						
						//segments which do not include any last zero padded segmen- zeros can confuse deep learning models so it may be better to keep use 
						//this instead of zero padding end chunks. 
						int nChunks = (int) Math.ceil((overFlow)/(double) dlControl.getDLParams().sampleHop); 

						nChunks = Math.max(nChunks, 1); //cannot be less than one (if forceSave is used then can be zero if no overflow)
						nextRawChunks[i]=new GroupedRawData[nChunks]; 
						//System.out.println("Total data len: " + rawDataChunk.length + " overFlow: " + overFlow + " nChunks: " + nChunks); 
					}

					GroupedRawData lastRawDataChunk = currentRawChunks[i]; 

					for (int j = 0; j<nextRawChunks[i].length; j++) {

						//					System.out.println("There has been a data overflow. " + PamUtils.getSingleChannel(pamRawData.getChannelBitmap())); 

						int copyLen = rawDataChunk.length - overFlow; //how much data was copied into the last data unit

						//now we need to populate the next data unit
						//to prevent any weird errors from accommodate sample offsets etc try to get timing information from the last pamRawDataUnit.
						if (nextRawChunks[i][j]== null) {
							//long timeMillis = lastRawDataChunk.getTimeMilliseconds() + (long) (1000*(copyLen-getBackSmapleHop() )/this.getSampleRate()); 
							//long startSample = lastRawDataChunk.getStartSample() + copyLen - getBackSmapleHop() ; 

							//go from current raw chunks tim millis to try and minimise compounding time errors. 
							//							long timeMillis = (long) (currentRawChunks[i].getTimeMilliseconds() + j*(1000.*(dlControl.getDLParams().sampleHop)/this.getSampleRate())); 
							long startSample = lastRawDataChunk.getStartSample() + dlControl.getDLParams().sampleHop; 
							long timeMillis = this.absSamplesToMilliseconds(startSample); 

							nextRawChunks[i][j] = new GroupedRawData(timeMillis, getSourceParams().getGroupChannels(i), 
									startSample, dlControl.getDLParams().rawSampleSize, dlControl.getDLParams().rawSampleSize); 
							nextRawChunks[i][j].setParentDataUnit(unit);

						}

						//add the hop from the current grouped raw data unit to the new grouped raw data unit 
						//					System.out.println("Pointer to copy from: "  + (currentRawChunks[i].rawData[groupChan].length - dlControl.getDLParams().sampleHop )); 
						int overFlow2 = nextRawChunks[i][j].copyRawData(lastRawDataChunk.rawData[groupChan], lastRawDataChunk.rawData[groupChan].length - getBackSmapleHop()  , 
								getBackSmapleHop() , groupChan);

						//					System.arraycopy(currentRawChunks[i].rawData[groupChan], currentRawChunks[i].rawData[groupChan].length - dlControl.getDLParams().sampleHop,
						//							nextRawChunks[i].rawData[groupChan], 0, dlControl.getDLParams().sampleHop); 
						//					currentRawChunks[i].rawDataPointer[groupChan]=currentRawChunks[i].rawDataPointer[groupChan] + dlControl.getDLParams().sampleHop; 


						//finally copy in the slop that was not copied into the end of the last array. 
						overFlow = nextRawChunks[i][j].copyRawData(rawDataChunk, copyLen, rawDataChunk.length-copyLen, groupChan);

						//					System.arraycopy(rawDataChunk, rawDataChunk.length-copyLen, nextRawChunks[i].rawData[groupChan], dlControl.getDLParams().sampleHop+1, rawDataChunk.length-copyLen); 
						//					currentRawChunks[i].rawDataPointer[groupChan]=currentRawChunks[i].rawDataPointer[groupChan] + copyLen; 

						//System.out.println("Next chunk time: " +  PamCalendar.formatDBDateTime(nextRawChunks[i][j].getTimeMilliseconds(), true) + " Overflow: " + overFlow + " Raw len: " + rawDataChunk.length); 

						lastRawDataChunk = nextRawChunks[i][j]; 
					}
				}

				//System.out.println("Should we save the raw data? : " + isRawGroupDataFull(currentRawChunks[i])); 


				//now that the data has been copied into the temporary unit need to check if the temporary data unit is full
				//check if the data unit is ready to go. 
				if (isRawGroupDataFull(currentRawChunks[i])) {
					saveRawGroupData(i) ;
				}
				//no need to carry on through the for loop
				break; 
			}; 
		}
	}


	/**
	 * Save the current segmented raw data units to the segmenter data block.
	 * @param forceSave - true to also save the remaining unfilled segment. 
	 */
	private void saveRawGroupData(boolean forceSave) {
		for (int i=0; i<getSourceParams().countChannelGroups(); i++) {
			saveRawGroupData(i, forceSave); 
		}
	}

	/**
	 * Save the current segmented raw data units to the segmenter data block.
	 */
	private void saveRawGroupData() {
		saveRawGroupData(false); 
	}


	/**
	 * Save the current segmented raw data units to the segmenter data block.
	 * @param i - the group index. 
	 */
	private void saveRawGroupData(int i) {
		saveRawGroupData(i, false); 
	}


	/**
	 * Save the current segmented raw data units to the segmenter data block.
	 * @param forceSave - true to also save the remaining unfilled segment. 
	 * @param i - the group index. 
	 */
	private void saveRawGroupData(int i, boolean forceSave) {

		if (currentRawChunks[i]==null) {
			return; 
		}

		//add some extra metadata to the chunks 
		packageSegmenterDataUnit(currentRawChunks[i]); 
		//System.out.println("Segmenter process: Save current segments to datablock: " + currentRawChunks[i].getParentDataUnit().getUID()); 

		//send the raw data unit off to be classified!

		this.segmenterDataBlock.addPamData(currentRawChunks[i]);

		if (nextRawChunks[i]!=null) {
			int n = nextRawChunks[i].length-1; 
			
			if (forceSave) n=n+1; //save the last data unit too. 
			
			//add all segments up to the last one. 
			for (int j=0;j<n; j++) {
				if (nextRawChunks[i][j]!=null) {
				//System.out.println("Segmenter process: Save next raw chunks: " + nextRawChunks[i][j].getUID() + " raw data pointer: " +nextRawChunks[i][j].getRawDataPointer()[0]); 
					this.segmenterDataBlock.addPamData(packageSegmenterDataUnit(nextRawChunks[i][j]));
				}
			}
		}

		//System.out.println("Add PAM data! " + PamCalendar.formatDBDateTime(currentRawChunks[i].getTimeMilliseconds(), true) + " duration: " + currentRawChunks[i].getBasicData().getMillisecondDuration() "ms"); 

		//Need to copy a section of the old data into the new 
		if (nextRawChunks[i]!=null) {
			/**
			 * It's very important to clone this as otherwise some very weird things happnen as the units are
			 * passed to downstream processes. 
			 */
			currentRawChunks[i] = nextRawChunks[i][nextRawChunks[i].length-1].clone(); //in an unlikely situation this could be null should be picked up by the first null check. 
		}
		else {
			currentRawChunks[i] = null; 
		}
		nextRawChunks[i] = null; //make null until it's restarted 
	}


	private int getBackSmapleHop() {
		return dlControl.getDLParams().rawSampleSize - dlControl.getDLParams().sampleHop; 
	}

	//	/***TODO - hand small windows***/
	//
	//	/**
	//	 * Check if the window size is likely smaller than the raw data. 
	//	 * @return
	//	 */
	//	private boolean smallWindowSize() {
	//		if (dlControl.getDLParams().rawSampleSize < this.getSampleRate()/100) return true;
	//		return false; 
	//	}
	//
	//	/**
	//	 * There is an issue if the window length is very much smaller than the raw data chunks coming in. Instead of
	//	 * over complicating code, if this is the case then each GroupedRawData holds multiple raw data windows and is then 
	//	 * divided before being sent off to the datablock. 
	//	 */
	//	private void getWindowLength() {
	//		//now we want to create an integer size of array 
	//
	//	}
	//
	//	/***TODO **/


	/**
	 * Add all the bits and pieces to make sure the segmenter data unit
	 * has all the appropriate meta data for drawing etc. 
	 */
	private GroupedRawData packageSegmenterDataUnit(GroupedRawData groupedRawData) {
		groupedRawData.setFrequency(new double[] {0, this.getSampleRate()/2});
		groupedRawData.setDurationInMilliseconds((long) (1000*dlControl.getDLParams().rawSampleSize/this.getSampleRate()));

		return groupedRawData;
	}

	/**
	 * Check whether a raw data unit is full. A units is full if all channels have been filled with acoustic data. 
	 * @param groupedRawData - the grouped raw data 
	 * @return true if the grouped data unit is full. 
	 */
	private boolean isRawGroupDataFull(GroupedRawData groupedRawData) {
		if (groupedRawData==null) return false; 
		for (int i=0; i<groupedRawData.rawData.length; i++) {
			if ( groupedRawData.rawDataPointer[i] < groupedRawData.rawData[i].length-1) {
				return false; 
			}
		}
		return true;
	}


	/**
	 * Convenience function to get grouped source parameters. 
	 * @return the grouped source parameters. 
	 */
	private GroupedSourceParameters getSourceParams() {
		return dlControl.getDLParams().groupedSourceParams; 
	}


	/**
	 * 
	 * Temporary holder for raw data with a pre defined size. This holds one channel group of raw 
	 * sound data. 
	 * 
	 * @author Jamie Macaulay 
	 *
	 */
	public class GroupedRawData extends PamDataUnit implements PamDetection, Cloneable {


		/*
		 * Raw data holder
		 */
		protected double[][] rawData;


		/**
		 *  Current position in the rawData;
		 */
		protected int[] rawDataPointer;

		/**
		 * The data unit associated with this raw data chunk. 
		 */
		private PamDataUnit rawDataUnit;


		/**
		 * Create a grouped raw data unit. This contains a segment of sound data. 
		 * @param timeMilliseconds - the time in milliseconds. 
		 * @param channelBitmap - the channel bitmap of the raw data. 
		 * @param startSample - the start sample of the raw data. 
		 * @param duration - the duration of the raw data in samples. 
		 * @param samplesize - the total sample size of the raw data unit chunk in samples. 
		 */
		public GroupedRawData(long timeMilliseconds, int channelBitmap, long startSample, long duration, int samplesize) {
			super(timeMilliseconds, channelBitmap, startSample, duration);
			rawData = new double[PamUtils.getNumChannels(channelBitmap)][];
			rawDataPointer = new int[PamUtils.getNumChannels(channelBitmap)];
			//			rawDataStartMillis = new long[PamUtils.getNumChannels(channelBitmap)];

			for (int i =0; i<rawData.length; i++) {
				rawData[i] = new double[samplesize];
			}
			
		}

		/**
		 * Set the parent data unit. 
		 * @param unit - the raw data unit. 
		 */
		public void setParentDataUnit(PamDataUnit rawDataUnit) {
			this.rawDataUnit=rawDataUnit; 
		}

		/**
		 * Get the data unit that this raw sound segment is associated with. 
		 * @Return unit - the raw data unit
		 */
		public PamDataUnit getParentDataUnit() {
			return rawDataUnit;
		}


		/**
		 * Copy raw data from an array to another. 
		 * @param src - the array to come from 
		 * @param srcPos - the raw source position
		 * @param copyLen - the copy length. 
		 * @groupChan - the channel (within the group)
		 * @return overflow - the  number of raw data points  left at the end which were not copied. 
		 */
		public int copyRawData(Object src, int srcPos, int copyLen, int groupChan) {
			//how much of the chunk should we copy? 


			int lastPos = rawDataPointer[groupChan] + copyLen; 

			int dataOverflow = 0; 

			int arrayCopyLen; 
			//make sure the copy length 
			if (lastPos>=rawData[groupChan].length) {
				arrayCopyLen=copyLen-(lastPos-rawData[groupChan].length)-1; 
				dataOverflow = copyLen - arrayCopyLen; 
			}
			else {
				arrayCopyLen= copyLen; 
			}
			
			arrayCopyLen = Math.max(arrayCopyLen, 0); 

			//update the current grouped raw data unit with new raw data. 
			System.arraycopy(src, srcPos, rawData[groupChan], rawDataPointer[groupChan], arrayCopyLen); 

			rawDataPointer[groupChan]=rawDataPointer[groupChan] + arrayCopyLen; 

			return dataOverflow; 
		}

		/**
		 * Get the raw data grouped by channel.
		 * @return the raw acoustic data.
		 */
		public double[][] getRawData() {
			return rawData;
		}

		/**
		 * Get the current pointer for rawData.
		 * @return the data pointer per channel. 
		 */
		public int[] getRawDataPointer() {
			return rawDataPointer;
		}


		@Override
		protected GroupedRawData clone()  {
			try {
				GroupedRawData groupedRawData =  (GroupedRawData) super.clone();
				
				//hard clone the acoustic data
				groupedRawData.rawData = new double[this.rawData.length][]; 
				for (int i=0; i<groupedRawData.rawData.length; i++) {
					groupedRawData.rawData[i] = Arrays.copyOf(this.rawData[i], this.rawData[i].length); 
				}

				return groupedRawData;

			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
				return null;
			}
		}
	}


	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
	}

	@Override
	public void pamStop() {

		//		this.nextRawChunks = null;
		//		this.currentRawChunks = null; 
	}

	/**
	 * Get the segmenter data block. This holds raw chunks of data to be sent to the 
	 * deep learning classifier. 
	 * @return the segmenter data block. 
	 */
	public SegmenterDataBlock getSegmenterDataBlock() {
		return segmenterDataBlock;
	}

} 