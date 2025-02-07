package rawDeepLearningClassifier.segmenter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import PamController.PamController;
import PamDetection.RawDataUnit;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.GroupedSourceParameters;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import clickDetector.ClickDetection;
import clipgenerator.ClipDataUnit;
import rawDeepLearningClassifier.DLControl;
import whistlesAndMoans.ConnectedRegionDataUnit;


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

	private static final long MAX_SEGMENT_GAP_MILLIS = 3600*1000L;

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

	/**
	 * Holds groups of data units which are within a defined segment. 
	 */
	private SegmenterGroupDataBlock segmenterGroupDataBlock;

	/**
	 * The first clock update - segments for detection groups (not raw sound data) are referenced from this. 
	 */
	private long firstClockUpdate; 
	
	/**
	 * The current segmenter detection group.
	 */
	private SegmenterDetectionGroup[] segmenterDetectionGroup = null;

	private long segmentStart=-1;

	private long segmenterEnd=-1; 


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
		
		segmenterGroupDataBlock = new SegmenterGroupDataBlock("Segmented data units", this,
				dlControl.getDLParams().groupedSourceParams.getChanOrSeqBitmap());
		
		addOutputDataBlock(segmenterDataBlock);
		addOutputDataBlock(segmenterGroupDataBlock);

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
		return new ArrayList<Class<? extends PamDataUnit>>(Arrays.asList(RawDataUnit.class, ClickDetection.class, ClipDataUnit.class, ConnectedRegionDataUnit.class));
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
			segmenterDetectionGroup = new SegmenterDetectionGroup[chanGroups.length];
		}
		
		
		//reset segments - important otherwise PG will crash if the wav file is reset. 
		segmentStart=-1;
		segmenterEnd=-1; 


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
		
		this.dlControl.createDataSelector(rawDataBlock);
		
		this.firstClockUpdate = -1;

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

//		System.out.println("New data for segmenter: " + pamRawData + "  isGroup: " + dlControl.isGroupDetections()); 

		if (!dlControl.getDLParams().useDataSelector || dlControl.getDataSelector().scoreData(pamRawData)>0) {	

			if (!dlControl.isGroupDetections()){
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
			else {
				newGroupData(pamRawData);
			}
		}

	}


	/**
	 * A new detection data unit i.e. this is only if we have detection data which is being grouped into segments. 
	 * @param detection - the whistle data unit. 
	 */
	public synchronized boolean newGroupData(PamDataUnit detection) {
		
//		PamDataUnit detection = dataUnit;

		//TODO
		//this contains no raw data so we are branching off on a completely different processing path here.
		//Whistle data units are saved to a buffer and then fed to the deep learning algorithms
		
		int[] chanGroups = dlControl.getDLParams().groupedSourceParams.getChannelGroups();
		
		int index = -1;
		for (int i=0; i<chanGroups.length; i++) {			
			if (PamUtils.hasChannelMap(dlControl.getDLParams().groupedSourceParams.getGroupChannels(chanGroups[i]), detection.getChannelBitmap())) {
				index=i;
				break;
			}
		}

//		//FIXME - TWEMP
//		index =0;
		
//		System.out.println("Group data: " + ((detection.getTimeMilliseconds()-firstClockUpdate)/1000.) + "s " + chanGroups.length +  "  " +  index + "  " + detection.getChannelBitmap());
		//PamArrayUtils.printArray(chanGroups);
		
		if (index<0) {
			return false;
		}
		
		boolean segSaved = false;
	
		if (segmenterDetectionGroup[index] == null || !detectionInSegment(detection,  segmenterDetectionGroup[index])) {
			
			//System.out.println("Whiste not in segment"); 
			//iterate until we find the correct time for this detection. This keeps the segments consist no matter 
			//the data units. What we do not want is the first data unit defining the start of the first segment.
			if (segmentStart < 0) {
				//FIXME - seems like the master clock update is slightly after the start of a file - this needs sorted but for now
				//this is a fix that will work - just means segments miss being completely lined up with the start of files but 
				//will always include all data units. 
				segmentStart= detection.getTimeMilliseconds()< firstClockUpdate ? detection.getTimeMilliseconds() : firstClockUpdate;
				segmenterEnd = (long) (segmentStart + getSegmentLenMillis());
				newGroupSegment(index);
			}
			
			//sometimes if a detection is a long time after the previous then we don't want to iterate through new segments so start again
			if ((detection.getTimeMilliseconds() - segmentStart)>MAX_SEGMENT_GAP_MILLIS) {
				segmentStart = detection.getTimeMilliseconds();
				segmenterEnd = (long) (segmentStart + getSegmentLenMillis());
				newGroupSegment(index);
			}
			
			while(!detectionInSegment(detection,  segmentStart,  segmenterEnd)) {
				//System.out.println("Detection in segment: " + segmentStart + " det millis: " + detection.getTimeMilliseconds()); 
				
				if (detection.getTimeMilliseconds()<segmentStart) {
					//something has gone quite wrong
					System.err.println("rawdeepLearningClassifier.segmenterProcess: Detection: " +  detection.getUID() + 
							" was detected before the segment?  " + "seg start: " + segmentStart + " det start: " +detection.getTimeMilliseconds() );
					return false;
				}
				
				segSaved = nextGroupSegment(index);
				
			}
		}
		
		segmenterDetectionGroup[index].addSubDetection(detection);
		//System.out.println("Segment sub detection count: " + 	segmenterDetectionGroup[index].getSubDetectionsCount()); 
		
		return segSaved;
	}
	
	/**
	 * Iterate to the next group segment
	 * @param index - the group index;
	 */
	private boolean nextGroupSegment(int index) {
		
//		System.out.println("----------------------------------");

		segmentStart = (long) (segmentStart+ getSegmentHopMillis());
		segmenterEnd = (long) (segmentStart + getSegmentLenMillis());
		
		return newGroupSegment(index);
	}
	
	/**
	 * Create a new group segment based on the current segmentStart and segmentEnd fields. Will saved the previous segment if 
	 * it exists and contains more than one sub detection. 
	 * @param index - the segment group index (i.e. channel group index)
	 * @return true of a segment has completed and saved to the segment group data block. 
	 */
	private boolean newGroupSegment(int index) {
		boolean segSaved = false;
		
		int[] chanGroups = dlControl.getDLParams().groupedSourceParams.getChannelGroups();

		long startSample = this.absMillisecondsToSamples(segmentStart);

		//now we need to create a new data unit.
		SegmenterDetectionGroup aSegment = new SegmenterDetectionGroup(segmentStart, chanGroups[index], startSample,  getSegmentLenMillis());
		aSegment.setStartSecond((segmentStart-firstClockUpdate)/1000.);

		//save the last segment
		if (segmenterDetectionGroup[index]!=null) {
			//add any data units from the previous segment (because segments may overlap);
			int count =0;
			for (int i=0; i<segmenterDetectionGroup[index].getSubDetectionsCount() ; i++) {
				if (detectionInSegment(segmenterDetectionGroup[index].getSubDetection(i), aSegment)){
					aSegment.addSubDetection(segmenterDetectionGroup[index].getSubDetection(i));
					count++;
				}
			}
			
//			System.out.println("SAVE GROUP SEGMENT!: " + ((segmenterDetectionGroup[index].getSegmentStartMillis()-firstClockUpdate)/1000.) + "s" + " " + " no. whistles: " 
//			+ segmenterDetectionGroup[index].getSubDetectionsCount() + " " + PamCalendar.formatDateTime(segmenterDetectionGroup[index].getSegmentStartMillis()) + "  " 
//					+ segmenterDetectionGroup[index]);
			//save the data unit to the data block

			if (segmenterDetectionGroup[index].getSubDetectionsCount()>0) {
				this.segmenterGroupDataBlock.addPamData(segmenterDetectionGroup[index]);
				segSaved=true;
			}
		}
		
		segmenterDetectionGroup[index] = aSegment;
		
		return segSaved;
	}
	
	private boolean detectionInSegment(PamDataUnit dataUnit, SegmenterDetectionGroup segmenterDetectionGroup2) {
		return detectionInSegment(dataUnit, segmenterDetectionGroup2.getSegmentStartMillis(),
				(long) (segmenterDetectionGroup2.getSegmentStartMillis()+segmenterDetectionGroup2.getSegmentDuration()));
	}


	private boolean detectionInSegment(PamDataUnit dataUnit, long segStart, long segEnd) {
		//TODO - this is going to fail for very small segments. 
		long whistleStart 	= dataUnit.getTimeMilliseconds();
		long whistleEnd 	= whistleStart + dataUnit.getDurationInMilliseconds().longValue();

		if ((whistleStart>=segStart && whistleStart<segEnd) || ((whistleEnd>=segStart && whistleEnd<segEnd))){
			//some part of the whistle is in the segment. 
//			System.out.println("Whsitle in segment: " + whistleStart + "  " + whistleEnd);
			return true;
		}
		return false;
	}
	
	private double getSegmentLenMillis() {
		double millis = (dlControl.getDLParams().rawSampleSize/this.getSampleRate())*1000.;
		return millis;
	}
	
	private double getSegmentHopMillis() {
		double millis = (dlControl.getDLParams().sampleHop/this.getSampleRate())*1000.;
		return millis;
	}
	
	
	
	int count=0;
	public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		super.masterClockUpdate(milliSeconds, sampleNumber);
		
		if (firstClockUpdate<0) {
			firstClockUpdate = milliSeconds;
		}
		
		//want to make sure that a segment is saved if we suddenly lose 
		// a steady stream of data units. This ensure that the segments are saved properly
		//after the master clock has gone past the end of the current segment. 
		if (segmenterDetectionGroup!=null && count%20==0) {
			for (int i=0; i<segmenterDetectionGroup.length; i++) {
				if (segmenterDetectionGroup[i]!=null && segmenterDetectionGroup[i].getSegmentEndMillis()<milliSeconds) {
					nextGroupSegment(i);
				}
			}
		}
		count++;
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
			
				if (dlControl.getDLParams().enableSegmentation) {
					//segment the data unit into different chunks. 
					newRawData(pamDataUnit,
							rawDataChunk[i], chans[i], dlControl.getDLParams().rawSampleSize, dlControl.getDLParams().sampleHop, true);
				}
				else {
//					//send the whole data chunk to the deep learning unit
					newRawData(pamDataUnit,
							rawDataChunk[i], chans[i], 	rawDataChunk[i].length, rawDataChunk[i].length, true);
//					currentRawChunks[i] = new GroupedRawData(pamDataUnit.getTimeMilliseconds(), getSourceParams().getGroupChannels(i), 
//							pamDataUnit.getStartSample(), 	rawDataChunk[i].length, 	rawDataChunk[i].length); 
				}
				
			//the way that the newRawdata works is it waits for the next chunk and copies all relevant bits
			//from previous chunks into segments. This is fine for continuous data but means that chunks of data
			//don't get their last hop...
		}

		//got to save the last chunk of raw data -even if the segment has not been filled. 
		saveRawGroupData(true);
	}


	/**
	 * Take a raw sound chunk of data and segment into discrete groups. This handles
	 * many situations e.g. where the segment is much larger than the raw data or
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
		newRawData(unit, rawDataChunk, iChan, dlControl.getDLParams().rawSampleSize ,dlControl.getDLParams().sampleHop , false); 
	}

	/**
	 * Take a raw sound chunk of data and segment into discrete groups. This handles
	 * many situations e.g. where the segment is much larger than the raw data or
	 * where the segment is much small than each rawDataChunk returning multiple
	 * segments.
	 * 
	 * @param unit         - the data unit which contains relevant metadata on time
	 *                     etc.
	 * @param rawDataChunk - the sound chunk extracted from the data
	 *                     unit.
	 * @param iChan        - the channel that is being segmented
	 * @param rawSampleSize - the segment size in samples i.e. the size of the segmenting window. 
	 * @param rawSampleHop - the segment hop in samples i.e. how far the window jumps for each segment. 
	 * @param forceSave    - make sure that all data is passed into the buffers and
	 *                     do not wait for the next data unit. This is used to make
	 *                     sure that discrete chunks have their full number of
	 *                     segments saved.
	 */
	public synchronized void newRawData(PamDataUnit unit, double[] rawDataChunk, int iChan, int rawSampleSize, int rawSampleHop, boolean forcesave) {

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
							startSampleTime, rawSampleSize, rawSampleSize); 
					
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
						int nChunks = (int) Math.ceil((overFlow)/(double) rawSampleHop); 

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
							long startSample = lastRawDataChunk.getStartSample() + rawSampleHop; 
							long timeMillis = this.absSamplesToMilliseconds(startSample); 

							nextRawChunks[i][j] = new GroupedRawData(timeMillis, getSourceParams().getGroupChannels(i), 
									startSample, rawSampleSize, rawSampleSize); 
							nextRawChunks[i][j].setParentDataUnit(unit);

						}

						//add the hop from the current grouped raw data unit to the new grouped raw data unit 
						//					System.out.println("Pointer to copy from: "  + (currentRawChunks[i].rawData[groupChan].length - dlControl.getDLParams().sampleHop )); 
						int overFlow2 = nextRawChunks[i][j].copyRawData(lastRawDataChunk.rawData[groupChan], lastRawDataChunk.rawData[groupChan].length - getBackSmapleHop(rawSampleSize, rawSampleHop)  , 
								getBackSmapleHop(rawSampleSize, rawSampleHop) , groupChan);

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
		//System.out.println("Segmenter process: Save current segments to datablock: " + currentRawChunks[i].getParentDataUnit().getUID() + " " + i + currentRawChunks[i].getRawData()[0][0]); 

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
			 * It's very important to clone this as otherwise some very weird things happen as the units are
			 * passed to downstream processes. 
			 */
			currentRawChunks[i] = nextRawChunks[i][nextRawChunks[i].length-1].clone(); //in an unlikely situation this could be null should be picked up by the first null check. 
		}
		else {
			currentRawChunks[i] = null; 
		}
		nextRawChunks[i] = null; //make null until it's restarted 
	}


	private int getBackSmapleHop(int segSize, int segHop) {
		return segSize-segHop;
//		return dlControl.getDLParams().rawSampleSize - dlControl.getDLParams().sampleHop; 
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


	public SegmenterGroupDataBlock getSegmenteGroupDataBlock() {
		return this.segmenterGroupDataBlock;
	}


	/**
	 * Set the first clock update which helps the segmenter work out where to start segmenting from. 
	 * @param startTime - the start time in millis. 
	 */
	public void setFirstClockUpdate(long startTime) {
		this.firstClockUpdate = startTime;
	}

} 