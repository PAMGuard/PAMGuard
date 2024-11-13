package rawDeepLearningClassifier.dlClassification;

import java.util.ArrayList;
import java.util.List;

import PamDetection.RawDataUnit;
import PamUtils.PamArrayUtils;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.GroupedSourceParameters;
import PamView.PamDetectionOverlayGraphics;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamInstantProcess;
import PamguardMVC.PamObservable;
import binaryFileStorage.DataUnitFileInformation;
import rawDeepLearningClassifier.DLControl;
import rawDeepLearningClassifier.RawDLParams;
import rawDeepLearningClassifier.layoutFX.DLDetectionGraphics;
import rawDeepLearningClassifier.layoutFX.DLGraphics;
import rawDeepLearningClassifier.logging.DLAnnotation;
import rawDeepLearningClassifier.logging.DLAnnotationType;
import rawDeepLearningClassifier.logging.DLGroupDetectionLogging;
import rawDeepLearningClassifier.logging.DLGroupSubLogging;
import rawDeepLearningClassifier.segmenter.GroupedRawData;
import rawDeepLearningClassifier.segmenter.SegmenterDataBlock;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;

/**
 * The deep learning classification process. This takes a segment of raw data from the segmenter. 
 * and passes it to a deep learning model. 
 * <p>
 * All model results are added to a DLClassifiedDataBlock. This stores the probabilities for each species category. 
 * <p>
 * If a binary classification is true then the raw data from the model data block is saved. 
 * <p>
 * 
 * @author Jamie Macaulay
 *
 */
public class DLClassifyProcess extends PamInstantProcess {


	/**
	 *  Holds all model results but no other information. Detections based on
	 *  decision threshold are held in dlDetectionDataBlock. 
	 */
	private DLModelDataBlock dlModelResultDataBlock;

	/**
	 * Reference to the DL control
	 */
	private DLControl dlControl;

	/**
	 * Holds results which have passed a binary classification 
	 */
	private DLDetectionDataBlock dlDetectionDataBlock;

	/**
	 * Buffer which holds positive raw sound data unit results to be merged into one data unit. This mirrors modeResultDataBuffer
	 */
	private ArrayList<GroupedRawData>[] groupRawDataBuffer; 

	/**
	 * Buffer which holds positive detection group data units results to be merged into one data unit. This mirrors modeResultDataBuffer
	 */
	private ArrayList<SegmenterDetectionGroup>[] groupDetectionBuffer; 

	/**
	 * Buffer which holds positive model results to be merged into one data unit. This mirrors groupDataBuffer. 
	 */
	private ArrayList<PredictionResult>[] modelResultDataBuffer; 

	/**
	 * The DL buffer
	 */
	private ArrayList<PamDataUnit> classificationBuffer; 

	/**
	 * The DL annotation type. 
	 */
	private DLAnnotationType dlAnnotationType; 

	/**
	 * The last parent data for grouped data. This is used to ensure that DLDetections 
	 * correspond to the raw chunk of data from a parent detection e.g. a click detection. 
	 */
	private PamDataUnit[] lastParentDataUnit;

	/**
	 * The DL group data block that saves DL Group data block. 
	 */
	private DLGroupDataBlock dlGroupDetectionDataBlock;

	public DLClassifyProcess(DLControl dlControl, SegmenterDataBlock parentDataBlock) {
		super(dlControl);

		this.setParentDataBlock(parentDataBlock);

		//		this.setParentDataBlock(parentDataBlock);
		this.dlControl = dlControl; 

		//create an annotations object. 
		dlAnnotationType = new DLAnnotationType(dlControl);

		//all the deep learning results.
		dlModelResultDataBlock = new DLModelDataBlock("DL Model Data", this, 
				dlControl.getDLParams().groupedSourceParams.getChanOrSeqBitmap());
		addOutputDataBlock(dlModelResultDataBlock);
		dlModelResultDataBlock.setNaturalLifetimeMillis(600*1000); //keep this data for a while.

		//the classifier deep learning data, 
		dlDetectionDataBlock = new DLDetectionDataBlock("DL Classifier Data", this, 
				dlControl.getDLParams().groupedSourceParams.getChanOrSeqBitmap());
		addOutputDataBlock(dlDetectionDataBlock);
		dlDetectionDataBlock.setNaturalLifetimeMillis(600*1000); //keep this data for a while.
		dlDetectionDataBlock.addDataAnnotationType(dlAnnotationType);
		//ClipGeneration allows processing of detections by DIFAR module (and possibly others)
		dlDetectionDataBlock.setCanClipGenerate(true); 

		//add a detection block which creates super detection from the deep learning classifier
		//the classifier deep learning data, 
		dlGroupDetectionDataBlock = new DLGroupDataBlock(this, "DL Group Data", 
				dlControl.getDLParams().groupedSourceParams.getChanOrSeqBitmap());
		addOutputDataBlock(dlGroupDetectionDataBlock);
		dlGroupDetectionDataBlock.setNaturalLifetimeMillis(600*1000); //keep this data for a while.
		dlGroupDetectionDataBlock.addDataAnnotationType(dlAnnotationType);
		dlGroupDetectionDataBlock.setCanClipGenerate(true); 

		classificationBuffer =  new ArrayList<PamDataUnit>(); 

		//the process name. 
		setProcessName("Deep Learning Classifier");  
	}

	/**
	 * Setup the classification process. 
	 */
	private void setupClassifierProcess() {

		//System.out.println("Setup raw deep learning classiifer process: "); 

		if (dlControl.getDLParams()==null) {
			System.err.println("SegmenterProcess.setupSegmenter: The DLParams are null???");
		}

		if (dlControl.getDLParams().groupedSourceParams==null) {
			dlControl.getDLParams().groupedSourceParams = new GroupedSourceParameters(); 
			System.err.println("Raw Deep Learning Classifier: The grouped source parameters were null."
					+ " A new instance has been created: Possible de-serialization error.");
		}


		//important for downstream processes such as the bearing localiser.
		dlModelResultDataBlock.setChannelMap(dlControl.getDLParams().groupedSourceParams.getChannelBitmap());
		dlDetectionDataBlock.setChannelMap(dlControl.getDLParams().groupedSourceParams.getChannelBitmap());

		int[] chanGroups = dlControl.getDLParams().groupedSourceParams.getChannelGroups();

		//initialise an array of nulls. 
		if (chanGroups!=null) {
			groupDetectionBuffer = new  ArrayList[chanGroups.length] ; 
			groupRawDataBuffer = new  ArrayList[chanGroups.length] ; 
			modelResultDataBuffer = new  ArrayList[chanGroups.length] ; 
			lastParentDataUnit = new PamDataUnit[chanGroups.length]; 
			for (int i =0; i<chanGroups.length; i++) {
				groupRawDataBuffer[i] = new ArrayList<GroupedRawData>();
				groupDetectionBuffer[i] = new ArrayList<SegmenterDetectionGroup>(); 
				modelResultDataBuffer[i] = new ArrayList<PredictionResult>(); 
			}
		}

	}


	@Override
	public void prepareProcess() {
		setupClassifierProcess();
	}


	/**
	 * called for every process once the system model has been created. 
	 * this is a good time to check out and find input data blocks and
	 * similar tasks. 
	 *
	 */
	@Override
	public void setupProcess() {
		setupClassifierProcess(); 
		super.setupProcess();
	}



	/*
	 * Segments raw data and passes a chunk of multi-channel data to a deep learning algorithms. 
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 *      Gets blocks of raw audio data (single channel), blocks it up into
	 *      fftSize blocks dealing with overlaps as appropriate. fft's each
	 *      complete block and sends it off to the output PamDataBlock, which
	 *      will in turn notify any subscribing processes and views
	 */
	@Override
	public void newData(PamObservable obs, PamDataUnit pamRawData) {
//		System.out.println("NEW SEGMENTER DATA: " +  PamCalendar.formatDateTime2(pamRawData.getTimeMilliseconds(), "dd MMM yyyy HH:mm:ss.SSS", false) + "  " + pamRawData.getUID() + "  " + pamRawData.getChannelBitmap() + " " + pamRawData);

		if (pamRawData instanceof SegmenterDetectionGroup) {
			if (classificationBuffer.size()>=1) {
				runDetectionGroupModel(); 
				classificationBuffer.clear(); 
			}
			else {
				classificationBuffer.add(pamRawData);
			}
		}

		if (pamRawData instanceof GroupedRawData) {
			//the raw data units should appear in sequential channel order  
			GroupedRawData rawDataUnit = (GroupedRawData) pamRawData;

			if (checkGroupData(rawDataUnit)) { 
				//check whether the classification buffer is full. If it is then run 
				if (isRawClassificationBufferFull(classificationBuffer, rawDataUnit)) {

					//first call run model to clear out the classification buffer if needs be
					runRawModel(); 
					classificationBuffer.clear(); 
				}

				classificationBuffer.add(rawDataUnit); 

			}
		}
		//				System.out.println("New raw data in: chan: " + PamUtils.getSingleChannel(pamRawData.getChannelBitmap()) + 
		//						" Size: " +  pamRawData.getSampleDuration() + " first sample: " + rawDataUnit.getRawData()[0][0] 
		//								+ "Parent UID: " + rawDataUnit.getParentDataUnit().getUID());
	}


	/**
	 * Run a model for which the input is a detection group. 
	 */
	private synchronized void runDetectionGroupModel() {
		if (classificationBuffer.size()<=0) return; 
		ArrayList<PamDataUnit> classificationBufferTemp = (ArrayList<PamDataUnit>) classificationBuffer.clone(); 

		ArrayList<? extends PredictionResult> modelResults = this.dlControl.getDLModel().runModel(classificationBufferTemp); 

		for (int i=0; i<classificationBufferTemp.size(); i++) {

			if (modelResults!=null && modelResults.get(i)!=null) {

				newDetectionGroupResult(classificationBufferTemp.get(i),  modelResults.get(i));
			}
		}

	}

	/**
	 * Called whenever there is a new detection group result. Group detection are deep learning results which are derived from
	 * groups of data units. 
	 * 
	 * @param detectionGroup - the detection group.
	 * @param modelResult the model result
	 */
	private void newDetectionGroupResult(PamDataUnit detectionGroup, PredictionResult modelResult) {

		DLDataUnit dlDataUnit =  predictionToDataUnit(detectionGroup, modelResult);

		this.dlModelResultDataBlock.addPamData(dlDataUnit); //here

//		System.out.println("DELPHINID - we have a detection: " + detectionGroup.getUID() + "  " + PamCalendar.formatDateTime(detectionGroup.getTimeMilliseconds())); 

//		//Now generate a detection of a decision threshold is reacheed. 
//		if (dlDataUnit.getPredicitionResult().isBinaryClassification()) {
//			System.out.println("DELPHINID - we have a positive detection: " + detectionGroup.getUID() + "  " + PamCalendar.formatDateTime(detectionGroup.getTimeMilliseconds())); 
//		}

		//need to implement multiple groups. 
		for (int i=0; i<getSourceParams().countChannelGroups(); i++) {

			if (PamUtils.hasChannel(getSourceParams().getGroupChannels(i), PamUtils.getLowestChannel(detectionGroup.getChannelBitmap()))) {
				/****Make our own data units****/
				if (dlDataUnit.getPredicitionResult().isBinaryClassification()) {
					//if the model result has a binary classification then it is added to the data buffer unless the data
					//buffer has reached a maximum size. In that case the data is saved. 
					groupDetectionBuffer[i].add((SegmenterDetectionGroup) detectionGroup); 
					modelResultDataBuffer[i].add(modelResult); 
					
					if (groupDetectionBuffer[i].size()>=dlControl.getDLParams().maxMergeHops) {
						//need to save the data unit and clear the unit. 
						DLGroupDetection dlDetection  = makeGroupDLDetection(groupDetectionBuffer[i], modelResultDataBuffer[i]); 
						clearBuffer(i);
						if (dlDetection!=null) {
							this.dlGroupDetectionDataBlock.addPamData(dlDetection);
						}
					}
				}
				else {
					//no binary classification thus the data unit is complete and buffer must be saved. 
					DLGroupDetection dlDetection  = makeGroupDLDetection(groupDetectionBuffer[i], modelResultDataBuffer[i]); 
					clearBuffer(i) ;
					if (dlDetection!=null) {
						this.dlGroupDetectionDataBlock.addPamData(dlDetection);
						//							System.out.println("Amplitude: " + dlDetection.getAmplitudeDB()  
						//							+ "  " + dlDetection.getMeasuredAmplitudeType());
					}
				}
			}
		}
	}


	/**
	 * Make a deep learning detection which defines a group of data units. This
	 * takes the (possibly overlapping segments) figures out the times of the first
	 * and last sub detections and create a data unit which contains all sub
	 * detections.
	 * 
	 * @param groupDetections  - a series of grouped data units - these may overlap.
	 * @param predictedResults - the results for each group of data.
	 * @return the DLDetection for grouped data.
	 */
	private DLGroupDetection makeGroupDLDetection(ArrayList<SegmenterDetectionGroup> groupDetections, ArrayList<PredictionResult> predictedResults) {

		//TODO - should we add a sub detection list to this. 

		if (groupDetections.size()<1) return null;

		long timeMillis = Long.MAX_VALUE;
		long startSample =  Long.MIN_VALUE;
		long endTimeMillis =  Long.MIN_VALUE;
		double minFreq =  Double.POSITIVE_INFINITY;
		double maxFreq =  Double.NEGATIVE_INFINITY;


		double[] freq;
		List<PamDataUnit> dataUnits = new ArrayList<PamDataUnit>();
		//work out the time limits from the different segments - super safe here assuming they could be oiut of order. 
		for (int i=0; i<groupDetections.size() ; i++) {
			
//			System.out.println("SEGMENT " + i + "  " + "  "  + groupDetections.get(i).getUID() + "  " +  PamCalendar.formatDateTime(groupDetections.get(i).getTimeMilliseconds()) 
//			+ " channel: " + groupDetections.get(i).getChannelBitmap() +  " n det: " + groupDetections.get(i).getSubDetectionsCount()); 

			//work out the frequency limits based on the data units
			for (int j=0; j<groupDetections.get(i).getSubDetectionsCount(); j++) {
				
				if (groupDetections.get(i).getSubDetection(j).getTimeMilliseconds()<timeMillis) {
					timeMillis = groupDetections.get(i).getSubDetection(j).getTimeMilliseconds();
					startSample =  groupDetections.get(i).getSubDetection(j).getStartSample();
				}

				if (groupDetections.get(i).getSubDetection(j).getEndTimeInMilliseconds()>endTimeMillis) {
					endTimeMillis = groupDetections.get(i).getSubDetection(j).getEndTimeInMilliseconds();
				}
				
				freq = groupDetections.get(i).getSubDetection(j).getFrequency();
				if (freq[0]<minFreq) {
					minFreq = freq[0];
				}
				if (freq[1]>maxFreq) {
					maxFreq = freq[1];
				}

				dataUnits.add(groupDetections.get(i).getSubDetection(j)); 
			}

		}

//		System.out.println("MAKE DL GROUP DETECTION " + groupDetections.size() + " segements " + " freq: "
//		+ minFreq + "  " + maxFreq + " time: " + PamCalendar.formatDateTime(timeMillis) + " duration: " + (endTimeMillis-timeMillis));
		
		DLGroupDetection dlgroupDetection = new DLGroupDetection(timeMillis, groupDetections.get(0).getChannelBitmap(), startSample,  (endTimeMillis-timeMillis), dataUnits); 
		dlgroupDetection.setFrequency(new double[] {minFreq, maxFreq});
		addDLAnnotation(dlgroupDetection, predictedResults); 
				
		return dlgroupDetection;
	}


	/**
	 * Run the model for which the input is raw acoustic data. This only runs if the classification buffer is full. 
	 */
	private void runRawModel() {

		if (classificationBuffer.size()<=0) return; 

		//run the deep learning algorithm 
		ArrayList<GroupedRawData> classificationBufferTemp = (ArrayList<GroupedRawData>) classificationBuffer.clone(); 
		ArrayList<? extends PredictionResult> modelResults = this.dlControl.getDLModel().runModel(classificationBufferTemp); 

		if (modelResults==null) {
			return; //there has been a problem
		}

		for (int i=0; i<classificationBufferTemp.size(); i++) {
			if (modelResults.get(i)!=null) {
				//if the model is null there may be a buffer on a different thread 
				//				System.out.println("Compare Times: " + PamCalendar.formatDBDateTime(modelResults.get(i).getTimeMillis(), true)  + 
				//						"   " + PamCalendar.formatDBDateTime(classificationBufferTemp.get(i).getTimeMilliseconds(), true) + "  " +
				//						modelResults.get(i).getPrediction()[1]); 
				newRawModelResult(modelResults.get(i), classificationBufferTemp.get(i)); 
			}
		}
	}


	/**
	 * Check whether the buffer is full and the results should be passed to the classification model if we are using GrpoupDataUnits
	 * @param classificationBuffer2 - the classification buffer. 
	 * @param rawDataUnit  - the next raw data unit to add to the buffer. 
	 * @return true if the buffer is full. 
	 */
	private boolean isRawClassificationBufferFull(ArrayList<PamDataUnit> classificationBuffer2, GroupedRawData rawDataUnit) {

		if (classificationBuffer2.size()==0) return false; 

		//buffer is full if
		//1) It's over  a max time
		//2) Contains different parent data units (if not from raw data). 

		GroupedRawData lastUnit = (GroupedRawData) classificationBuffer2.get(classificationBuffer2.size()-1); 

		if (!(lastUnit.getParentDataUnit() instanceof RawDataUnit) && lastUnit.getParentDataUnit()!=rawDataUnit.getParentDataUnit()) {
			//there is a new parent data unit. 
			return true; 
		}

		//get the start time. Use min value instead of first data just in case units are not in order. 
		long min = Long.MAX_VALUE;
		for (PamDataUnit groupedRawData:  classificationBuffer2) {
			if (groupedRawData.getTimeMilliseconds()<min) {
				min=groupedRawData.getTimeMilliseconds(); 
			}
		}

		double timeDiff = rawDataUnit.getTimeMilliseconds()-min; 

		if (timeDiff>=this.dlControl.getDLParams().maxBufferTime) {
			//		if (timeDiff>=this.dlControl.getDLParams().maxBufferTime) {

			//we are over the max buffer size. 
			//System.out.println("DLClassifyProcess: warning:  buffer is over max time gap"); 
			return true; 
		}

		return false;
	}


	/**
	 * Check grouped data before passing it to the classifications. Checks are:
	 * <p>
	 * Is everything zero? If so there is no information so discard.
	 * 
	 * @param rawDataUnit - the raw data unit
	 * @return true if the data unit can be passed to the localiser.
	 */
	private boolean checkGroupData(GroupedRawData rawDataUnit) {

		if (PamArrayUtils.sum(rawDataUnit.getRawData())==0) {
			System.out.println("DLCLassifyProcess: Warning: the input data was all zero - no passing to DL Process: "); 
			return false; 
		}

		return true;
	}



	private DLDataUnit predictionToDataUnit(PamDataUnit pamDataUnit, PredictionResult modelResult) {

		//create a new data unit - always add to the model result section. 
		DLDataUnit dlDataUnit = new DLDataUnit(pamDataUnit.getTimeMilliseconds(), pamDataUnit.getChannelBitmap(), 
				pamDataUnit.getStartSample(), pamDataUnit.getSampleDuration(), modelResult); 


		dlDataUnit.setFrequency(new double[] {0, dlControl.getDLClassifyProcess().getSampleRate()/2});
		dlDataUnit.setDurationInMilliseconds(pamDataUnit.getDurationInMilliseconds()); 

		return dlDataUnit;
	}


	/**
	 * Create a data unit form a model result. This is called whenever data passes a prediction threshold.
	 * 
	 * @param modelResult - the model result. 
	 * @param pamRawData - the raw data unit which the model result came from. 
	 */
	public void newRawModelResult(PredictionResult modelResult, GroupedRawData pamRawData) {

		//the model result may be null if the classifier uses a new thread. 

		//System.out.println("New segment: parent UID: " + pamRawData.getParentDataUnit().getUID() + " Prediciton: " + modelResult.getPrediction()[0]+ "  " + getSourceParams().countChannelGroups());

		//create a new data unit - always add to the model result section. 
		DLDataUnit dlDataUnit =  predictionToDataUnit(pamRawData,  modelResult);

		this.dlModelResultDataBlock.addPamData(dlDataUnit); //here

		//need to implement multiple groups. 
		for (int i=0; i<getSourceParams().countChannelGroups(); i++) {

			//						System.out.println("RawDataIn: chan: " + pamRawData.getChannelBitmap()+ "  " +
			//						PamUtils.hasChannel(getSourceParams().getGroupChannels(i), pamRawData.getChannelBitmap()) + 
			//						" grouped source: " +getSourceParams().getGroupChannels(i) + " Channels OK? " 
			//						+PamUtils.hasChannel(getSourceParams().getGroupChannels(i), PamUtils.getSingleChannel(pamRawData.getChannelBitmap())) 
			//						+ "  groupchan: " + getSourceParams().getGroupChannels(i) + "  " + PamUtils.getLowestChannel(pamRawData.getChannelBitmap())
			//						+ " chan bitmap: " + pamRawData.getChannelBitmap()); 

			if (PamUtils.hasChannel(getSourceParams().getGroupChannels(i), PamUtils.getLowestChannel(pamRawData.getChannelBitmap()))) {

				/*** 
				 * The are two options here. 
				 * 1) Create a new data unit from the segmented data. This data unit may be made up of multiple segment that all pass binary
				 * classification. 
				 * 2) Annotated an existing data unit with a deep learning annotation. 
				 */
				if (pamRawData.getParentDataUnit() instanceof RawDataUnit) {

					/****Make our own data units****/
					if (dlDataUnit.getPredicitionResult().isBinaryClassification()) {
						//if the model result has a binary classification then it is added to the data buffer unless the data
						//buffer has reached a maximum size. In that case the data is saved. 
						groupRawDataBuffer[i].add(pamRawData); 
						modelResultDataBuffer[i].add(modelResult); 
						if (groupRawDataBuffer[i].size()>=dlControl.getDLParams().maxMergeHops) {
							//need to save the data unit and clear the unit. 
							DLDetection dlDetection  = makeRawDLDetection(groupRawDataBuffer[i], modelResultDataBuffer[i]); 
							clearBuffer(i);
							if (dlDetection!=null) {
								this.dlDetectionDataBlock.addPamData(dlDetection);

							}
						}

					}
					else {
						//no binary classification thus the data unit is complete and buffer must be saved. 
						DLDetection dlDetection  = makeRawDLDetection(groupRawDataBuffer[i], modelResultDataBuffer[i]); 
						clearBuffer(i) ;
						if (dlDetection!=null) {
							this.dlDetectionDataBlock.addPamData(dlDetection);
							//							System.out.println("Amplitude: " + dlDetection.getAmplitudeDB()  
							//							+ "  " + dlDetection.getMeasuredAmplitudeType());
						}

					}
				}
				else {
					/****Add annotation to existing data unit (e.g. click, clip or other RawDataHolder)****/
					//Need to go by the parent data unit for merging data not the segments. Note that we may still add multiple
					//predictions to a single data unit depending on how many segments it contains. 

					//System.out.println("New model data " + pamRawData.getParentDataUnit().getUID() + " " + groupDataBuffer[i].size() + " " + modelResultDataBuffer[i].size()); 

					if (pamRawData.getParentDataUnit()!=lastParentDataUnit[i]) {
						//save any data
						if (groupRawDataBuffer[i].size()>0 && lastParentDataUnit[i]!=null) {
							//System.out.println("Save click annotation 1 "); 
							if (this.dlControl.getDLParams().forceSave) {
								DLDetection dlDetection = makeRawDLDetection(groupRawDataBuffer[i],modelResultDataBuffer[i]);
								clearBuffer(i);
								if (dlDetection!=null) {
									this.dlDetectionDataBlock.addPamData(dlDetection);

								}
							}
							else {
								System.out.println("Save click annotation to " + lastParentDataUnit[i].getUID()); 
								addDLAnnotation(lastParentDataUnit[i],modelResultDataBuffer[i]); 
								clearBuffer(i); 
							}
						}
					}
					lastParentDataUnit[i]=pamRawData.getParentDataUnit();
					groupRawDataBuffer[i].add(pamRawData); 
					modelResultDataBuffer[i].add(modelResult); 
					//System.out.println("Buffer click annotation to " + lastParentDataUnit[i].getUID() + " " + groupDataBuffer[i].size()); 
				}
			}
		}
	}


	/**
	 * The classifier process works with a few buffers. The classificationBuffer
	 * saves enough raw data so that arrays of images can be sent to the deep
	 * learning model (which is faster). The next buffer involves detections -
	 * either making one because there are model results that pass threshold or
	 * saving the predictions to an existing detection. For offline processing these
	 * buffers are problematic because they are only cleared when the next data unit
	 * is added. This function can be used to bypass the buffers. Add raw data to
	 * the segmenter process and the buffers will fill. Then call this function to
	 * run the deep learning algorithm and save the detections as annotation to a
	 * data unit.
	 * 
	 * @param dataUnit - the data unit to add prediction annotations to
	 * 
	 */
	public void forceRunClassifier(PamDataUnit dataUnit) {

		if (this.classificationBuffer.size()>0) {
			if (classificationBuffer.get(0) instanceof GroupedRawData) {
				runRawModel(); //raw data or raw data units
			}
			if (classificationBuffer.get(0) instanceof SegmenterDetectionGroup) {
				runDetectionGroupModel(); //any other data units. 
			}
		}

		//first call run model to clear out the classification buffer if needs be
		classificationBuffer.clear(); 

		//need to implement multiple groups. 
		for (int i=0; i<getSourceParams().countChannelGroups(); i++) {


			//			System.out.println("Nummber segments " + groupDataBuffer[i].size() + " data unit len: " + dataUnit.getSampleDurationAsInt() + " samples UID: " + dataUnit.getUID()); 
			//						System.out.println("RawDataIn: chan: " + dataUnit.getChannelBitmap()+ "  " +
			//						PamUtils.hasChannel(getSourceParams().getGroupChannels(i), dataUnit.getChannelBitmap()) + 
			//						" grouped source: " +getSourceParams().getGroupChannels(i)); 


			if (PamUtils.hasChannel(getSourceParams().getGroupChannels(i), PamUtils.getSingleChannel(dataUnit.getChannelBitmap()))) {
				if (groupRawDataBuffer[i].size()>0) {
					//System.out.println("Save click annotation to " + lastParentDataUnit[i].getUID()); 
					addDLAnnotation(dataUnit,modelResultDataBuffer[i]); 
					lastParentDataUnit[i]=null;
					clearBuffer(i); 
				}
			}
		}
	}


	/**
	 * Make a positive DL detection from a number of model results and corresponding chunks of raw sound data. 
	 * @param groupDataBuffer - the raw data chunks (these may overlap). 
	 * @param modelResult - the model results. 
	 * @return a DL detection with merged raw data. 
	 */
	private synchronized DLDetection makeRawDLDetection(ArrayList<GroupedRawData> groupDataBuffer, ArrayList<PredictionResult> modelResult) {

		if (groupDataBuffer==null || groupDataBuffer.size()<=0) {
			return null; 
		}

		int chans = PamUtils.getNumChannels(groupDataBuffer.get(0).getChannelBitmap());

		//need to merge the raw data chunks- these are overlapping which is a bit of pain but should be OK if we only copy in the hop lengths.
		double[][] rawdata = new double[PamUtils.getNumChannels(groupDataBuffer.get(0).getChannelBitmap())]
				[dlControl.getDLParams().sampleHop*(groupDataBuffer.size()-1) + dlControl.getDLParams().rawSampleSize]; 

		//copy all data into a new data buffer making sure to compensate for hop size.  
		for (int i=0; i<groupDataBuffer.size(); i++) {

			int copyLen = dlControl.getDLParams().sampleHop; 

			//at the end have to copy the entire length or else we miss the end of the detection...
			if (i==groupDataBuffer.size()-1) copyLen = dlControl.getDLParams().rawSampleSize; 

			for (int j=0; j<chans; j++) {
				System.arraycopy(groupDataBuffer.get(i).getRawData()[j], 0, rawdata[j], 
						i*dlControl.getDLParams().sampleHop, copyLen);
			}
		}

		DataUnitBaseData basicData  = groupDataBuffer.get(0).getBasicData().clone(); 
		basicData.setMillisecondDuration(1000.*rawdata[0].length/this.sampleRate);
		basicData.setSampleDuration((long) (groupDataBuffer.size()*dlControl.getDLParams().rawSampleSize));

		//		System.out.println("Model result: " + modelResult.size()); 
		DLDetection dlDetection = new DLDetection(basicData, rawdata); 
		addDLAnnotation(dlDetection,modelResult); 

		//create the data unit
		return dlDetection; 
	}


	/**
	 * Get the result with the highest score. 
	 * @return model result with the highest score. 
	 */
	public PredictionResult getBestModelResult(DLDetection dlDetection) {
		ArrayList<PredictionResult> results = dlDetection.getModelResults();
		if (results == null || results.size() == 0) {
			return null;
		}
		/*
		 *  probably need to improve this function to only look at results
		 *  that are in a list of used results or something crazy ? 
		 */

		//		dlControl.getDLModel().;
		PredictionResult bestResult = null;
		float bestScore = 0;
		for (PredictionResult pred : results) {
			float[] scores = pred.getPrediction();
			if (scores == null) {
				continue;
			}
			for (int i = 0; i < scores.length; i++) {
				if (scores[i] > bestScore) {
					bestScore = scores[i];
					bestResult = pred;
				}
			}
		}

		return bestResult;
	}

	/**
	 * Clear the data unit buffer. 
	 */
	private void clearBuffer(int group) {
		//System.out.println("CLEAR BUFFER: "); 
		//do not clear because the arrays are referenced by other data. Prevent
		//the need to clone the arrays
		groupDetectionBuffer[group] = new ArrayList<SegmenterDetectionGroup>();
		groupRawDataBuffer[group] = new ArrayList<GroupedRawData>();
		modelResultDataBuffer[group] = new ArrayList<PredictionResult>(); 
		//		groupDataBuffer[group].clear(); 
		//		modelResultDataBuffer[group].clear(); 
	}


	/**
	 * Add a data annotation to an existing data unit from a number of model results
	 * and corresponding chunks of raw sound data.
	 * 
	 * @param groupDataBuffer - the raw data chunks (these may overlap).
	 * @param modelResult     - the model results.
	 * @return a DL detection with merged raw data.
	 */
	private void addDLAnnotation(PamDataUnit parentDataUnit,
			ArrayList<PredictionResult> modelResult) {

		//System.out.println("DLClassifyProces: Add annnotation to  " + parentDataUnit); 
		parentDataUnit.addDataAnnotation(new DLAnnotation(dlAnnotationType, modelResult)); 
		//parentDataUnit.updateDataUnit(System.currentTimeMillis());

		DataUnitFileInformation fileInfo = parentDataUnit.getDataUnitFileInformation();
		if (fileInfo != null) {
			fileInfo.setNeedsUpdate(true);
		}
		parentDataUnit.updateDataUnit(System.currentTimeMillis());
	}

	@Override
	public void pamStart() {
		//		System.out.println("PREP MODEL:");
		this.dlControl.getDLModel().prepModel(); 
	}

	@Override
	public void pamStop() {
		//make sure to run the last data in the buffer.
		if (this.classificationBuffer.size()>0) {
			if (classificationBuffer.get(0) instanceof GroupedRawData) {
				runRawModel(); //raw data or raw data units
			}
			if (classificationBuffer.get(0) instanceof SegmenterDetectionGroup) {
				runDetectionGroupModel(); //any other data units. 
			}
		}

		//21/11/2022 - it seems like this causes a memory leak when models are reopened and closed every file...
		//this.dlControl.getDLModel().closeModel(); 
	}

	/**
	 * Get the data block which contains all results from the deep learning output. 
	 * @return the data block which holds results output form the deep learning classifier. 
	 */
	public DLModelDataBlock getDLPredictionDataBlock() {
		return dlModelResultDataBlock; 
	}

	/**
	 * Get the data block which contains detections from the deep learning output. 
	 * @return the data block which holds classified data units
	 */
	public DLDetectionDataBlock getDLDetectionDatablock() {
		return this.dlDetectionDataBlock; 
	}

	/**
	 * Get the number of classes the model outputs e.g. 
	 * the number of species it can recognise. 
	 * @return the number of classes. 
	 */
	public int getNumClasses() {
		return this.dlControl.getNumClasses(); 
	}


	/**
	 * Convenience function to get grouped source parameters. 
	 * @return the grouped source parameters. 
	 */
	private GroupedSourceParameters getSourceParams() {
		return dlControl.getDLParams().groupedSourceParams; 
	}

	/**
	 * Get the DL annotation type. This handles adding annotations to data units 
	 * with the deep learning results. 
	 * @return the annotation type. 
	 */
	public DLAnnotationType getDLAnnotionType() {
		return dlAnnotationType;
	}

	/**
	 * Get the parameters for the raw deep learning module.
	 * @return the parameters object for the raw deep learning classifier. 
	 */
	public RawDLParams getDLParams() {
		return this.dlControl.getDLParams();
	}

	public DLControl getDLControl() {
		return dlControl;
	}

	/**
	 * Get the data block which contains detections from groups of data units. E.g.
	 * when a deep learning model considers a group of clicks or whistles rather
	 * than an individual detection.
	 * 
	 * @return the data block which holds results output from the deep learning
	 *         classifier.
	 */
	public DLGroupDataBlock getDLGroupDetectionDataBlock() {
		return this.dlGroupDetectionDataBlock;
	}

}
