package clickTrainDetector.clickTrainAlgorithms.mht;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.ListIterator;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.ClickTrainDataBlock;
import clickTrainDetector.TempCTDataUnit;
import clickTrainDetector.CTDataUnit;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfo;
import clickTrainDetector.clickTrainAlgorithms.CTAlgorithmInfoLogging;
import clickTrainDetector.clickTrainAlgorithms.ClickTrainAlgorithm;
import clickTrainDetector.layout.CTDetectorGraphics;
import clickTrainDetector.layout.mht.MHTGraphics;
import clickTrainDetector.localisation.CTLocalisation;
import PamUtils.PamUtils;

/**
 * 
 * A click train algorithm based on a multi-hypothesis tracker (MHT) which groups data based
 * on a minimisation function. 
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class MHTClickTrainAlgorithm implements ClickTrainAlgorithm, PamSettings {

	
	/**
	 * The time in millis between updates of the active click trains. 
	 */
	private static final int ACTIVE_TRACK_UPDATE_TIME=5000;


	private static final int JUNK_TRACK_MAXIMUM = 10; 
	
	public static final String MHT_NAME = "MHT detector"; 
	
	/**
	 * Reference to the click train control. 
	 */
	private ClickTrainControl clickTrainControl; 

	//	/**
	//	 * Calculation of chi2 for any click train track. 
	//	 */
	//	private MHTChi2<PamDataUnit> pamMHTChi2; 

	/**
	 * The GUI components for the algorithm 
	 */
	private MHTGraphics mhtGUI; 

	/**
	 * MHT kernal params. 
	 */
	private MHTParams mhtParams; 

	/**
	 * List of current algorithms corresponding to the current number of channels/channel groups. 
	 */
	private ArrayList<MHTAlgorithm> mHTAlgorithms; 


	/**
	 * The MHTCHiManager; 
	 */
	private MHTChi2ProviderManager mhtChiManager;
	
	/**
	 * Counts the number of junked tracks. These are tracks that are flagged by the MHTChi2 algorithm
	 * as being junk, not unclassified tracks. 
	 */
	private int junkCount = 0; 
	
	/**
	 * Counts the number of junked tracks. These are tracks that are flagged by the MHTChi2 algorithm
	 * as being junk, not unclassified tracks. 
	 */
	private int trackCount = 0;

	/**
	 * The MHTGarbageBot, handles junking done sections of the probability mix in the MHTKernel. 
	 */
	private MHTGarbageBot mhtGarbageBot;
	
	/**
	 * The 
	 */
	private MHTAlgorithmInfoJSON mhtAlgorithmInfoJSON; 

	public MHTClickTrainAlgorithm(ClickTrainControl clickTrainControl) {
		this.clickTrainControl=clickTrainControl; 
				
		mhtParams= new MHTParams(); 

		//manages chi2 calculators. 
		mhtChiManager=new MHTChi2ProviderManager(this);
		
		//the track garbage bot
		mhtGarbageBot = new MHTGarbageBot(this); 
		
		//logging of MHT specific info/variables for detected click trains. 
		mhtAlgorithmInfoJSON = new MHTAlgorithmInfoJSON(this);

		//register saved settings
		PamSettingManager.getInstance().registerSettings(this);

		//setup the algorithm 
		setupAlgorithm(); 

	}

	@Override
	public String getName() {
		return MHT_NAME;
	}

	@Override
	public void newDataUnit(PamDataUnit dataUnit) {
			
		//TODO - need to figure out if we use a classification here and/or we 
		//have some sort of cutoff time between data units where we clear the MHT algorithm
		//kernel. Otherwise, eventually we will get a memory leak.

		for (int i=0; i<this.mHTAlgorithms.size(); i++) {
			//System.out.println("New data unit: " +dataUnit + " channelMap: " +  mHTAlgorithms.get(i).channelBitMap);
			if (PamUtils.hasChannelMap(mHTAlgorithms.get(i).channelBitMap, dataUnit.getChannelBitmap())) {
				//optimise the MHTKernel for memory 
				checkCTGarbageCollect(dataUnit, mHTAlgorithms.get(i)); 
				
				mHTAlgorithms.get(i).newDataUnit(dataUnit);
				
				if (!isViewer() && 
						(dataUnit.getTimeMilliseconds()-mHTAlgorithms.get(i).lastActiveTrackUpdate)>ACTIVE_TRACK_UPDATE_TIME) {
					grabUnconfirmedTrains(mHTAlgorithms.get(i));  
					mHTAlgorithms.get(i).lastActiveTrackUpdate = dataUnit.getTimeMilliseconds(); 
				}
				
				grabDoneTrains(mHTAlgorithms.get(i)); 
				
				//send update call to the GUI.
				this.mhtGUI.updateGraphics();
								
				break; 
			}
		}
	}
	
	/**
	 * Check whether in viewer mode. 
	 */
	private boolean isViewer() {
		return clickTrainControl.isViewer(); 
	}

	/**
	 * Checks whether an attempt for a garbage collection of click trains is
	 * necessary. If there are simply too many clicks (MHTGarbageBot.DETECTION_HARD_LIMIT) then
	 * the whole algorithm is reset. Every garbCountNTest clicks the algorithm will
	 * check the possibility matrix in the kernel and see of there is empty space at
	 * the start. If there is then the possibility matrix is trimmed to remove this
	 * section. As long as a single click train never reaches MHTGarbageBot..DETECTION_HARD_LIMIT
	 * then the possibility matrix will simply keep resizing and click train
	 * detector will run indefinitely.
	 * 
	 * @param - the new data unit
	 * @param -the channel based MHTAlgorithm.
	 */
	private void checkCTGarbageCollect(PamDataUnit dataUnit, MHTAlgorithm mhtAlgorithm) {

		boolean garbaged = mhtGarbageBot.checkCTGarbageCollect(dataUnit, mhtAlgorithm.getMHTKernal()); 
		
//		Debug.out.println("CHECKGARBAGE: GARBAGED"); 

		if (!garbaged) {
			//check for too many junk tracks.Junk tracks are only flagged if electrical noise filter is selected
			//System.out.println("No. junks tracks: " + this.junkCount + " No. true tracks: " + this.trackCount);
			boolean junkClear = false; 
			if (trackCount==0) {
				if (junkCount>10*JUNK_TRACK_MAXIMUM) {

					Debug.out.println("CHECKGARBAGE: JUNK TRACK IS GREATER THAN MAXIMUM 1:"); 
					junkClear=true; 
					//reset
					trackCount=0; 
					junkCount=0; 
				}
			}
			else {
				if (junkCount>JUNK_TRACK_MAXIMUM*trackCount) {

					Debug.out.println("JUNK TRACK IS GREATER THAN MAXIMUM 2:"); 
					junkClear=true; 
					//reset
					trackCount=0; 
					junkCount=0; 
				}
			}

			
			//if junk clear then clear the kernel. 
			if (junkClear) {
				//grab tracks
				mhtAlgorithm.getMHTKernal().confirmRemainingTracks();
				grabDoneTrains(mhtAlgorithm); 
				//reset the kernel; 
				mhtAlgorithm.getMHTKernal().clearKernel(); 
			}
		}
	}
	
	
	/**
	 * Check for garbage collection based on the current time. This is used when, for example, no click are detected for a 
	 * long period. 
	 * @param timeMillis - the time in millis
	 * @param mhtAlgorithm - the channel based MHTAlgorithm.
	 */
	private void checkCTGarbageCollect(long timeMillis, MHTAlgorithm mhtAlgorithm) {
			boolean isGarbage = this.mhtGarbageBot.checkCTGarbageCollect(timeMillis, mhtAlgorithm.getMHTKernal()); 
			if (isGarbage) {
				//grab tracks
				mhtAlgorithm.getMHTKernal().confirmRemainingTracks();
				grabDoneTrains(mhtAlgorithm); 
				//reset the kernel; 
				mhtAlgorithm.getMHTKernal().clearKernel(); 
		}
	}


//	/**
//	 * Get the interval between the new unit and the last unit. 
//	 * @param dataUnit - get the data units. 
//	 * @param mhtAlgorithm  - the MHTAlgorithm
//	 * @return the ICI in seconds 
//	 */
//	private double getLastICI(PamDataUnit dataUnit, MHTAlgorithm mhtAlgorithm) {
//		if (mhtAlgorithm.lastDataUnit!=null) {
//			//			System.out.println(dataUnit.getTimeMilliseconds() + " " + 
//			//					mhtAlgorithm.lastDataUnit.getTimeMilliseconds());
//			return ((double) (dataUnit.getTimeMilliseconds()
//					- mhtAlgorithm.lastDataUnit.getTimeMilliseconds()))/1000.;
//		}
//		else return 0;
//	}
	
	/**
	 * Get the track data units for a track possibility bitset. 
	 * @param mhtKernel - the MHTKernel
	 * @param bitSet - the bit set.
	 * @param kcount - the kcount.
	 * @return the track data units. 
	 */
	public static TrackDataUnits getTrackDataUnits(MHTKernel<PamDataUnit> mhtKernel, BitSet bitSet, int kcount) {

		int nDataUnits = MHTKernel.getTrueBitCount(bitSet, kcount); 

		ArrayList<PamDataUnit> dataUnits = mhtKernel.getDataUnits();

		//create the new ArrayList
		ArrayList<PamDataUnit> trainDataUnits = new ArrayList<PamDataUnit>(nDataUnits);

		for (int i =0; i<kcount; i++) {
			if (bitSet.get(i)) {
				trainDataUnits.add(dataUnits.get(i)); 
			}
		}
		
		TrackDataUnits trackUnits = new TrackDataUnits(trainDataUnits, dataUnits.get(kcount-1) ); 

		return trackUnits; 
	}; 
	
	/**
	 * Grabs any all the unconfirmed click trains and saves them into the unconfirmed click data block. 
	 * 
	 * @param mhtAlgorithm - the MHT algorithm to grab finished clicks trains from
	 */
	private synchronized void grabUnconfirmedTrains(MHTAlgorithm mhtAlgorithm) {
		
		ClickTrainDataBlock<TempCTDataUnit> unconfirmedBlock = clickTrainControl.getClickTrainProcess().getUnconfirmedCTDataBlock();
		synchronized (unconfirmedBlock.getSynchLock()) {
			ListIterator<TempCTDataUnit> iterator  = unconfirmedBlock.getListIterator(0);

			//clear the data block
			TempCTDataUnit tempCTUnit;
			while (iterator.hasNext()) {
				tempCTUnit = iterator.next(); 
				tempCTUnit.removeAllSubDetections();
				tempCTUnit.clearSubdetectionsRemoved();
			}
			unconfirmedBlock.clearAll(); 
		}
		
		if (mhtAlgorithm.mhtKernal.getActiveTracks()==null) return;
		
		int nTracks = mhtAlgorithm.mhtKernal.getActiveTracks().size();
		
		TrackBitSet trackBitSet;
		TrackDataUnits trackUnits = null;
		int n=0; 
		for (int i =0; i<nTracks; i++) {
			trackBitSet=mhtAlgorithm.mhtKernal.getActiveTracks().get(i);			
			trackUnits=MHTClickTrainAlgorithm.getTrackDataUnits(mhtAlgorithm.mhtKernal, trackBitSet.trackBitSet, 
					 mhtAlgorithm.mhtKernal.getKCount());
			//set the track chi2 value. 
			//trackUnits.chi2Value = trackBitSet.chi2Track.getChi2(); 
			
			if (trackUnits==null || trackUnits.dataUnits.size()<1) return;
			
			TempCTDataUnit tempDataUnit= new TempCTDataUnit(trackUnits.dataUnits.get(0).getTimeMilliseconds(), trackUnits.dataUnits);
			tempDataUnit.setCTChi2(trackBitSet.chi2Track.getChi2());
			
			//something weird is going on here...
			//tempDataUnit.addDetectionList(trackUnits.dataUnits); 
			///tempDataUnit.setCTChi2(trackUnits.chi2Value);
		
			clickTrainControl.getClickTrainProcess().getUnconfirmedCTDataBlock().addPamData(tempDataUnit);
			tempDataUnit.setLocalisation(new CTLocalisation(tempDataUnit, null, this.clickTrainControl.getClickTrainParams().ctLocParams));// needs to be here because needs click train control reference to know params. 
			n++; 
		}
		
		//Debug.out.println("Temp added: " + n); 
	}
	
	/**
	 * Grabs any click trains which are finished and saves them as the appropriate
	 * PAM data unit. Also clear the MHTKernel tracks.
	 * 
	 * @param mhtAlgorithm - the MHT algorithm to grab finished clicks trains from
	 */
	public synchronized void grabDoneTrains(MHTAlgorithm mhtAlgortihm) {
		grabDoneTrains(mhtAlgortihm.mhtKernal); 
	}

	/**
	 * Grabs any click trains which are finished and saves them as the appropriate
	 * PAM data unit. Also clear the MHTKernel tracks.
	 * 
	 * @param mhtKernal - the MHT kernel to grab finished clicks trains from
	 */
	public synchronized void grabDoneTrains(MHTKernel mhtKernal) {
		int nTracks = mhtKernal.getNConfrimedTracks(); 
		TrackBitSet trackBitSet;
		TrackDataUnits trackUnits;
		if (nTracks>0) Debug.out.println("-------------------Grab Done Trains---------------");
		for (int i =0; i<nTracks; i++) {
			trackBitSet=mhtKernal.getConfirmedTrack(i);
			Debug.out.println("MHTAlgorithm: Confirmed Track Grab: No. " + MHTKernel.getTrueBitCount(trackBitSet.trackBitSet)  + " flag: " + trackBitSet.flag + "  chi2: " +trackBitSet.chi2Track.getChi2()); 
			
			if (trackBitSet==null || trackBitSet.flag==TrackBitSet.JUNK_TRACK) {
				//junk the track. 
				Debug.out.println("We have a junk tracks!!!");
				junkCount++;
				continue;
			}
			trackUnits=MHTClickTrainAlgorithm.getTrackDataUnits(mhtKernal, trackBitSet.trackBitSet, 
					mhtKernal.getKCount());
			//set the track chi2 value. 
			trackUnits.chi2Value = trackBitSet.chi2Track.getChi2(); 
			
			if (Double.isNaN(	trackUnits.chi2Value)) {
				trackUnits.chi2Value = 0.1; //TEMP //FIXME
			}
			
			//System.out.println(MHTKernel.bitSetString(trackBitSet.trackBitSet, mhtKernal.getKCount())); 
			
			//save the click train
			trackCount++;
			saveClickTrain(trackUnits, trackBitSet.chi2Track.getMHTChi2Info());
		}
		
		if (nTracks>0) Debug.out.println("-------------------------------------------------");

		mhtKernal.clearConfirmedTracks();
	}

	/**
	 * Save the click train by adding it to the data block 
	 * @param ctAlgorithmInfo 
	 */
	private void saveClickTrain(TrackDataUnits trackUnits, CTAlgorithmInfo ctAlgorithmInfo) {
		
		if (trackUnits.dataUnits.size()<3) {
			Debug.out.println("The size of the click train is less than three: chi^2: " + trackUnits.chi2Value); 
			return;
		}
				
		CTDataUnit dataUnit= new CTDataUnit(trackUnits.dataUnits.get(0).getTimeMilliseconds());
		dataUnit.addSubDetections(trackUnits.dataUnits); 
		dataUnit.setCTAlgorithmInfo(ctAlgorithmInfo); 
		dataUnit.setCTChi2(trackUnits.chi2Value);
		
		Debug.out.println("MHTClickTrainAlgorithm: The number of data units is: "
		+ dataUnit.getSubDetections().size() + " IDIInfo: " + dataUnit.getIDIInfo().meanIDI + "  " +
		 PamCalendar.formatDateTime2(trackUnits.dataUnits.get(0).getTimeMilliseconds()));
		
		clickTrainControl.getClickTrainDataBlock().addPamData(dataUnit);

	}

	@Override
	public CTDetectorGraphics getClickTrainGraphics() {
		if (mhtGUI==null) mhtGUI = new MHTGraphics(this); 
		return mhtGUI;
	}

	/**
	 * Update the algorithm
	 * @param flag- flag indicating the update type. 
	 */
	public void update(int flag, Object info) {

		switch (flag) {
		case ClickTrainControl.PROCESSING_START:
			//make sure the kernel is cleared before processing
			//starts again.
			Debug.out.println("MHTClickTRainAlgorithm: Processing START: " + mHTAlgorithms.size());
			if (mHTAlgorithms!=null) {
				for (int i=0; i<this.mHTAlgorithms.size(); i++) {
					mHTAlgorithms.get(i).mhtKernal.clearKernel(); 
					mHTAlgorithms.get(i).lastActiveTrackUpdate=0; //need to reset or can mess up. 
					mHTAlgorithms.get(i).printSettings(); //Print out the settings for the Algorithm.class  
				}
			}
			break;
		case ClickTrainControl.PROCESSING_END:
			//grab all done click trains
			Debug.out.println("MHTClickTRainAlgorithm: Processing END: " + mHTAlgorithms.size());

			if (mHTAlgorithms!=null) {
				for (int i=0; i<this.mHTAlgorithms.size(); i++) {
					//confirm the remaining tracks
					mHTAlgorithms.get(i).mhtKernal.confirmRemainingTracks();
					//get those tracks. 

					//					/****PRINT SOME DATA****/
					//					mHTAlgorithms.get(i).mhtKernal.printConfirmedTracks();
					//					System.out.println("MHTCLickTRainAlgorithm: Grab finish: " + mHTAlgorithms.get(i).mhtKernal.getNConfrimedTracks());
					//					System.out.println("MHTCLickTRainAlgorithm: k: " + mHTAlgorithms.get(i).mhtKernal.getKCount());
					//					System.out.println("MHTCLickTRainAlgorithm: active tracks: " + mHTAlgorithms.get(i).mhtKernal.getActiveTracks().size());
					//					mHTAlgorithms.get(i).mhtKernal.getMHTParams().print(); 
					//					/**********************/

					Debug.out.println("-------Grab Done Train END--------");
					this.grabDoneTrains(mHTAlgorithms.get(i));
				}
			}
			break;
		case ClickTrainControl.NEW_PARAMS:
			setupAlgorithm(); 
			break;
		case ClickTrainControl.CLOCK_UPDATE:
			for (int i=0; i<this.mHTAlgorithms.size(); i++) {
				checkCTGarbageCollect((Long) info,  mHTAlgorithms.get(i)); 
			}
			break;
		}

	}


	/**
	 * Set up the algorithm. 
	 */
	private void setupAlgorithm() {
		if (mHTAlgorithms==null) mHTAlgorithms = new ArrayList<MHTAlgorithm>();
		else mHTAlgorithms.clear(); 

		//now make the new algorithms with the new channels.
		int[] channelGroups = clickTrainControl.getClickTrainParams().channelGroups; 
		if (channelGroups!=null) {
			for (int i=0; i<channelGroups.length; i++) {
				mHTAlgorithms.add(new MHTAlgorithm(channelGroups[i])); 
			}
		}
		this.getClickTrainGraphics().notifyUpdate(ClickTrainControl.NEW_PARAMS, this.getParams());
	}

	//	/**
	//	 * Get the chi2 MHT
	//	 * @return
	//	 */
	//	public MHTChi2<PamDataUnit> getPamMHTChi2() {
	//		return this.pamMHTChi2;
	//	}


	/**
	 * A single MHT click train algorithm for a particular channel or channel group. 
	 * 
	 * @author Jamie Macaulay 
	 *
	 */
	public class MHTAlgorithm {

		/**
		 * The MHTKernal for the track. 
		 */
		private MHTKernel<PamDataUnit> mhtKernal;

		/**
		 * The channel bitmap. 
		 */
		private int channelBitMap;

		/**
		 * The MHT chi2 calculation object. 
		 */
		private MHTChi2Provider<PamDataUnit> pamMHTChi2; 

		/**
		 * The last data unit added to the kernel. 
		 */
		private PamDataUnit<?, ?> lastDataUnit; 
		
		/**
		 * The time of the last active track update in millis. 
		 */
		private long lastActiveTrackUpdate = 0; 


		private  MHTAlgorithm(int channelBitMap) {
			this.channelBitMap=channelBitMap; 
			this.pamMHTChi2 = createMHTChi2Provider(); 
			mhtKernal= new MHTKernel<PamDataUnit>(pamMHTChi2); 
			mhtKernal.setMHTParams(mhtParams.mhtKernal);
		}

		/**
		 * Get the channel bitmap. 
		 * @return the channel bitmap 
		 */
		public int getChannelBitMap() {
			return channelBitMap;
		}


		private MHTChi2Provider<PamDataUnit> createMHTChi2Provider() {
			return mhtChiManager.createMHTChi2(mhtParams, clickTrainControl.getClickDataBlock(), channelBitMap); 
		}

		/**
		 * Add a new data unit. 
		 */
		private void newDataUnit(PamDataUnit<?, ?> dataUnit) {
			//System.out.println("Add MHT data unit: " + dataUnit.getTimeMilliseconds()+ "   " + mhtKernal.getKCount());
			this.lastDataUnit=dataUnit; 
			mhtKernal.addDetection(dataUnit);
		}


		/**
		 * The parameters. 
		 * @param mhtParams
		 */
		private void setParams(MHTParams mhtParams) {
			mhtKernal.setMHTParams(mhtParams.mhtKernal);
			pamMHTChi2.setMHTParams(mhtParams); 
		}



		/**
		 * Print the settings for the MHT algorithms to the console. 
		 */
		public void printSettings() {

			Debug.out.println("/********MHT PARAMS*******/");
			if (Debug.isPrintDebug()) {
				pamMHTChi2.printSettings();
				mhtKernal.getMHTParams().printSettings();
			}
			Debug.out.println("/*************************/");

			//			//MHT kernel params
			//			System.out.println("npruneback: " + mhtKernal.getMHTParams().nPruneback); 
			//			System.out.println("nprunestart: " + mhtKernal.getMHTParams().nPruneBackStart); 
			//			System.out.println("maxcoasts: " + mhtKernal.getMHTParams().maxCoast); 
			//			System.out.println("nhold: " + mhtKernal.getMHTParams().nHold); 
			//
			//			//Chi2 calc params
			//			System.out.println(" "); 
			//			System.out.println("maxICI: " + pamMHTChi2Params.maxICI); 
			//			System.out.println("newtrackpenalty: " + pamMHTChi2Params.newTrackPenalty); 
			//			System.out.println("coastpenatly: " + pamMHTChi2Params.coastPenalty); 
			//			System.out.println("maxICImultiplier: " + pamMHTChi2Params.maxICIMultipler); 
		}
		
		/**
		 * Get the MHTKernel for the algorithm 
		 * @return the MHTKernel for hte algorithm
		 */
		public MHTKernel<PamDataUnit> getMHTKernal() {
			return mhtKernal;
		}

		/**
		 * Get the last data unit added to the algorithm's probability mix. 
		 */
		public PamDataUnit<?, ?> getLastDataUnit() {
			return this.lastDataUnit;
		}


	}

	/**
	 * Get general MHT params. 
	 * @return the MHT params. 
	 */
	public MHTParams getParams() {
		return this.mhtParams;
	}

	@Override
	public String getUnitName() {
		return getName() + "_" + this.clickTrainControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return getName() ;
	}

	@Override
	public Serializable getSettingsReference() {
		return mhtParams;
	}

	@Override
	public long getSettingsVersion() {
		return MHTParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		try {
			this.mhtParams = (MHTParams) pamControlledUnitSettings.getSettings();
			this.setupAlgorithm();
			//send a settings restore flag 
			this.mhtParams.chi2Params.restoreSettings();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return true; 
		}
	}

	/**
	 * Set the parameters for the MHT algorithm. 
	 * @param mhtParams2 - the MHTParams to set as the new parameters. 
	 */
	public void setParams(MHTParams mhtParams2) {
		this.mhtParams=mhtParams2;
		//make sure all references are updates in algorithms. 
		for (int i=0; i<this.mHTAlgorithms.size(); i++) {
			this.mHTAlgorithms.get(i).setParams(mhtParams2);
		}
	}

	/**
	 * Get the MHTChi2 manager. 
	 * @return
	 */
	public MHTChi2ProviderManager getChi2ProviderManager() {
		return this.mhtChiManager;
	}
	
	/**
	 * Get a list of the current MHT algortihms 
	 * @return a list of the current MHT algorithms. 
	 */
	public ArrayList<MHTAlgorithm> getMHTAlgorithms() {
		return mHTAlgorithms;
	}

	/**
	 * Get the click train controls which owns the algorithm 
	 * @return the click train control. 
	 */
	public ClickTrainControl getClickTrainControl() {
		return this.clickTrainControl;
	}

	@Override
	public CTAlgorithmInfoLogging getCTAlgorithmInfoLogging() {
		return this.mhtAlgorithmInfoJSON; 
	}


	//	/**
	//	 * Function to test the algorithm; 
	//	 */
	//	public void testAlgorithm() {
	//		MHTKernel kernal= mHTAlgorithms.get(0).mhtKernal; 
	//		kernal.getMHTParams().print();
	//
	//
	//		//Do some chi2 calculations on the clicks. 
	//		kernal.clearKernel();
	//
	//		int nclicks=10; 
	//		this.clickTrainControl.getClickDataBlock().clearChannelIterators();
	//		ChannelIterator iterator = this.clickTrainControl.getClickDataBlock().getChannelIterator(3, 0); 
	//		int count =0; 
	//		while (iterator.hasNext() && count<nclicks) {
	//			kernal.addDetection(iterator.next());
	//			count++; 
	//		}
	//
	//		//create a bitset. 
	//		BitSet bitSet = new BitSet(); 
	//		for (int i=0; i<nclicks; i++) {
	//			bitSet.set(i, true);
	//		}
	//
	//		double chi2 = kernal.getMnhtchi2().calcChi2(kernal.getReferenceUnit(), bitSet, 10); 
	//
	//		System.out.println("Chi^2 for first " + count+ " clicks is: " + chi2);
	//
	//	}

}
