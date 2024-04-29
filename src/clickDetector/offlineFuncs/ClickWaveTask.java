package clickDetector.offlineFuncs;

import java.util.Arrays;
import java.util.ListIterator;

import Acquisition.AcquisitionProcess;
import Acquisition.DaqStatusDataUnit;
import Filters.Filter;
import Filters.FilterBand;
import Filters.FilterMethod;
import Filters.FilterParams;
import Filters.FilterType;
import PamDetection.RawDataUnit;
import PamUtils.PamArrayUtils;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamguardMVC.LoadObserver;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoadInfo;
import PamguardMVC.dataOffline.OfflineDataLoading;
import PamguardMVC.debug.Debug;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import clickDetector.ClickDetector;
import clickDetector.ClickTabPanelControl;
import dataMap.OfflineDataMapPoint;
import offlineProcessing.OfflineTask;

/**
 * Updates click waveform from raw audio data. 
 * This can be used to update the raw click waveform if, for example 
 * an unsuitable pre-filter was used. 
 * <p>
 * For example, useful for situations where there are large annotated data sets were
 * re-analysing clicks will destroy annotation UID reference in database. 
 * <p>
 * This is highly experimental and not for general release. For this to work the
 * must have been analysed from raw sound files. Those exact same files must be 
 * loaded as offline files in viewer mode. This will then, hopefully, extract the correct
 * raw chunks of raw data. 
 * <p>
 * @author Jamie Macaulay
 *
 */
public class ClickWaveTask extends OfflineTask<ClickDetection> {
	
	/**
	 * The maximum possible click length in millis. 
	 */
	private final static long MAXCLICKMILLIS = 100; 
	
	/**
	 * The default chunk length
	 */
	private final static long DEFAULTCHUNKLENGTH = 5000;
	
	/**
	 * Default time to load DAQ units. 
	 */
	private final static long DEFAULTDAQUNITLOAD = 1000*60*60*24; 

	/**
	 * Reference to the click control. 
	 */
	private ClickControl clickControl;
	
	/**
	 * Reference to the click detector. 
	 */
	private ClickDetector clickDetector;

	/**
	 * Reference to the clicks offline. 
	 */
	private ClicksOffline clicksOffline;

	/**
	 * Reference to the raw data block. 
	 */
	private PamRawDataBlock rawDataSource;
	
	/**
	 * The current raw data observer.
	 */
	private RawObserver rawLoadObserver =  new RawObserver();  
	
	/**
	 * Handles the end of rawe data loading. 
	 */
	private RawLoadEndObserver rawEndObserver = new RawLoadEndObserver(); 
	
	/**
	 * Indicates raw data order is underway. 
	 */
	private volatile boolean loadingRawData = true;

	/**
	 * The current chunk of raw data. 
	 */
	private  RawDataChunk rawDataChunk;
	
	/**
	 * Contains info on sample rates etc. 
	 */
	private PamDataBlock<DaqStatusDataUnit> daqStatusDataBlock; 


	/**
	 * The filter methods 
	 */
	private FilterMethod filterMethod;
	
	private FilterParams filterParams; 


	/**
	 * Constructor for the click wave task. 
	 * 
	 * @param clickControl - click control.
	 */
	public ClickWaveTask(ClickControl clickControl) {
		super(clickControl.getClickDataBlock());
		this.clickControl = clickControl;
		clicksOffline = clickControl.getClicksOffline();
		clickDetector = clickControl.getClickDetector();
//		setParentDataBlock(clickControl.getClickDataBlock());
		addAffectedDataBlock(clickControl.getClickDataBlock());
		
		rawDataSource = (PamRawDataBlock) clickDetector.getRawSourceDataBlock();
		
		if (rawDataSource!=null) {
			daqStatusDataBlock = ((AcquisitionProcess) rawDataSource.getParentProcess()).getDaqStatusDataBlock();
		}
	
		//TODO- Should not hard wire these but this is not going to be used very often. 
		filterParams = new FilterParams(FilterType.BUTTERWORTH, FilterBand.HIGHPASS, 250000, 5000, 4); 
		
		//DAQ status is a required data block
		this.addRequiredDataBlock(daqStatusDataBlock, 1000*60*60*24, 1000*60*60*24);
	}

	@Override
	public String getName() {
		return "Import Raw Waveform";
	}

	@Override
	public void newDataLoad(long startTime, long endTime,
			OfflineDataMapPoint mapPoint) {
	}
	
	@Override
	public void prepareTask() {
		Debug.out.println("Load data units from: daqStatusDataBlock: " + daqStatusDataBlock.getUnitsCount()); 
		
		if (daqStatusDataBlock==null) {
			if (rawDataSource!=null) {
				daqStatusDataBlock = ((AcquisitionProcess) rawDataSource.getParentProcess()).getDaqStatusDataBlock();
			}
		}
		
		filterMethod = FilterMethod.createFilterMethod(clickDetector.getSampleRate(), filterParams);

		daqStatusDataBlock.loadViewerData(new OfflineDataLoadInfo(clickControl.getClickDataBlock().getFirstUnit().getTimeMilliseconds()-DEFAULTDAQUNITLOAD, 
				clickControl.getClickDataBlock().getLastUnit().getTimeMilliseconds()+DEFAULTDAQUNITLOAD), null);

		Debug.out.println("Load data units done: daqStatusDataBlock: " + daqStatusDataBlock.getUnitsCount()); 		
	}

	private int errorCount =0 ; 
	@Override
	public boolean processDataUnit(ClickDetection click) {
		try {
			//check whether a new chunk is needed. 
			loadNewRawDataChunk(click); 
			//now set the wave chunk 
			setNewWaveData(click);
			return true; 
		}
		catch (Exception e) {
			//e.printStackTrace();
			System.err.println("There was an error in click: " + click.getUID() + "  " + PamCalendar.formatDateTime(click.getTimeMilliseconds())); 
			if (errorCount%100==0) {
				e.printStackTrace();
			}
			errorCount++; 
			return false; 
		}
	}
	
	
	/**
	 * Checks whether a new chunk of raw data is required and if so loads it on and waits
	 * for the load thread to complete. 
	 * 
	 * @param click - the click to check. 
	 */
	private synchronized void loadNewRawDataChunk(ClickDetection click) {
		loadingRawData= true; 
		//check whether it needs a new load
		if (rawDataChunk==null || click.getTimeMilliseconds()-MAXCLICKMILLIS<rawDataChunk.startMillis || 
				click.getTimeMilliseconds()+MAXCLICKMILLIS>rawDataChunk.endMillis) {
			
			long dataStart = click.getTimeMilliseconds()-DEFAULTCHUNKLENGTH; 
			long dataEnd = click.getTimeMilliseconds()+DEFAULTCHUNKLENGTH; 
			
			Debug.out.println("Load data between: " + PamCalendar.formatDateTime(dataStart) + " and " + PamCalendar.formatDateTime(dataEnd)); 

			//set up new filters.
			iirfFilters = new Filter[PamUtils.getNumChannels(click.getChannelBitmap())]; 
			for (int i=0; i<iirfFilters.length; i++){
				iirfFilters[i] = filterMethod.createFilter(i); 
			}

			//get wave data from offline sound files.
			rawLoadObserver.resetForLoad(dataStart, dataEnd, click.getParentDataBlock().getSampleRate(), click.getChannelBitmap()); 
			rawDataSource.orderOfflineData(this.rawLoadObserver, this.rawEndObserver, dataStart, dataEnd, 0, OfflineDataLoading.OFFLINE_DATA_INTERRUPT);

			try {
				//wait for the raw data to load. 
				while (loadingRawData) {
					Thread.sleep(10);
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			

		}
		//once data are loaded they should be set as fields.
	}
	
	int count = 0;

	private Filter[] iirfFilters;

	/**
	 * Sets new wave data for a click. 
	 * 
	 * @param click - the click to set wave data for. 
	 */
	private void setNewWaveData(ClickDetection click) {
		
		long startSample = click.getStartSample(); 
		
//		Debug.out.println("RawChunk: " +  this.rawDataChunk.rawStartSample ); 

		//work out where to copy the data from in the raw chunk
		int startrawchunk 	= (int) (startSample-this.rawDataChunk.rawStartSample); 
		int endrawchunk  	= (int) (startrawchunk+click.getSampleDuration()); 
		
		int nchan = PamUtils.getNumChannels(click.getChannelBitmap()); 
		
//		Debug.out.println("Click time: " + PamCalendar.formatDateTime(click.getTimeMilliseconds()) + " Click Start sample: " + click.getStartSample()); 
//		Debug.out.println("RawDataChunk length: " + rawDataChunk.rawData.length + " No. samples" 
//				+ rawDataChunk.rawData[0].length + " startrawchunk: " + startrawchunk + " endrawchunk: " + endrawchunk + " Raw Sytart Samples: " + rawDataChunk.rawStartSample);
		
		double[][] waveData = new double[nchan][]; 
		
		double[] waveDataS; 
		for (int i=0; i<PamUtils.getNumChannels(click.getChannelBitmap()); i++) {
			waveData[i] =  Arrays.copyOfRange(this.rawDataChunk.rawData[i], startrawchunk, endrawchunk);
		}

//		if (count==0) {
//			System.out.println("--Click Wave Data: ---" + click.getUID()); 
//			PamArrayUtils.printArray(click.getWaveData());
////			System.out.println("--Raw Data [0]: ---"); 
////			PamArrayUtils.printArray(this.rawDataChunk.rawData[0]);
//			System.out.println("--New Click Wave Data: ---"); 
//			PamArrayUtils.printArray(waveData);
//		}
		
		//remove all set filtered waveforms spectra etc. 
		click.freeClickMemory();
		//need to delete compressed data to make the click detection use new waveform. 
		click.setCompressedData(null, 0);
		click.setWaveData(waveData);
	
//		if (count==0) {
//			System.out.println("--New Click Wave Data: 2 ---"); 
//			PamArrayUtils.printArray(click.getWaveData());
//		}
		count++; 
	}
	
	
	/**(
	 * 
	 * Observes incoming raw data and creates a hold of it in memory.
	 *<p>
	 * Note: that this data is on the AWT thread and must be switched to the FX thread before any processing takes place.  
	 *<p>
	 * Note: this will not handle any situation on which there is a gap in the raw data. 
	 * 
	 * @author Jamie Macaulay
	 *
	 */
	class RawObserver extends PamObserverAdapter {
		
		/**
		 * Filters for wave data. 
		 */
		private Filter[] iirfFilters;

		/**
		 * The maximum number of megabytes
		 */
		private int maxMb=1024*1024*10; //10 mega bytes

		/**
		 * The current data count for data in different channels. 
		 */
		int[] count; 


		/**
		 * Temporary store of raw data for the whistle. 
		 */
		double[][] rawData;

		/**
		 * The start of the data to load
		 */
		long dataStart;

		/**
		 * The end of the data to load
		 */
		long dataEnd;
		
		/**
		 * The current channels which are being loaded. 
		 */
		int channelMap;
		
		/**
		 * The start sample of the first incoming data unit., 
		 */
		long startSample; 

		/**
		 * Reset for loading a clip of raw data. 
		 * @param dataStart - the start of the data. 
		 * @param dataEnd - the end of the data
		 * @param sampleRate - the sample rate 
		 * @param channelBitmap - the channel bitmap. 
		 * @return true if reset was successful. False if there was an error. 
		 */
		public boolean resetForLoad(long dataStart, long dataEnd, float sampleRate, int channelBitmap) {
			

//			System.out.println("RESET FOR LOAD: ");
			this.dataStart=dataStart;
			this.dataEnd=dataEnd;
			this.channelMap = channelBitmap; 
			this.startSample = -1; 

			int dataFrame=(int) (sampleRate*((dataEnd-dataStart)/1000.));

			if (dataFrame>=maxMb){
				System.err.println("The raw data is way too big"); 
				return false;
			}
			else {
				
				int nchan = PamUtils.getNumChannels(channelBitmap); 
//				System.out.println("Size of array: " +  dataEnd + " " + dataStart + " " + (int) (sampleRate*((dataEnd-dataStart)/1000.)) + " SR " + sampleRate );
				rawData=new double[nchan][(int) (sampleRate*((dataEnd-dataStart)/1000.))];
				count  = new int[nchan]; 
				
				return true;
			}

		}

		@Override
		public void addData(PamObservable o, PamDataUnit dataUnit) {
			newRawData((RawDataUnit) dataUnit); //do not put in FX thread!
		}

		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			// TODO Auto-generated method stub
			
		}

		double[] clipRaw; 
		/**
		 * Called whenever new raw data is acquired
		 * @param dataUnit - the data unit. 
		 */
		private void newRawData(RawDataUnit dataUnit) {
			//want to put all the raw data into one large double array.
			try{
				if (this.startSample<0) {
					this.startSample=dataUnit.getFileSamples();
				}

				int pos = PamUtils.getChannelPos(PamUtils.getSingleChannel(dataUnit.getChannelBitmap()), this.channelMap); 

//				Debug.out.println("New raw data start sample " +  dataUnit.getStartSample() + " File Samples: " + dataUnit.getFileSamples() + " pos: " + pos + " count[pos] " + count[pos] + " length raw: "  + dataUnit.getRawData().length);

				clipRaw = dataUnit.getRawData();
				//import the data into the array
				for (int i=0; i<clipRaw.length; i++){
					if (count[pos]<rawData[pos].length){
						rawData[pos][count[pos]]=(float) clipRaw[i]; 
						count[pos]++; 
					}
				}
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	
		
		/**
		 * Get the raw data
		 * @return the raw data. 
		 */
		public double[][] getRawData(){
			return this.rawData;
		}

		@Override
		public String getObserverName() {
			return "Click offline wave replace";
		}

		@Override
		public PamObserver getObserverObject() {
			return this;
		}

		/**
		 * Get the channel of raw data that is being loaded. 
		 * @return 
		 */
		public int getChannel() {
			return channelMap;
		}

		/**
		 * Get the requested load start. 
		 * @return
		 */
		public long getLoadStart() {
			return dataStart;
		}
		
		/**
		 * Get the requested load start. 
		 * @return
		 */
		public long getLoadSampleStart() {
			return startSample;
		}

	}

	@Override
	public void loadedDataComplete() {
		ClickTabPanelControl ctpc = (ClickTabPanelControl) clickControl.getTabPanel();
		if (ctpc != null) {
			ctpc.offlineDataChanged();
		}
	}


	@Override
	public boolean hasSettings() {
		return false;
	}

	@Override
	public void completeTask() {
		System.out.println("The total error count was: "+ errorCount);
		super.completeTask();
	}
	
	
	/**
	 * Observe the success of the data load
	 * @author Jamie Macaulay
	 *
	 */
	public class RawLoadEndObserver implements LoadObserver {

		@Override
		public void setLoadStatus(int loadState) {
			if (loadState==PamDataBlock.REQUEST_DATA_LOADED){
			}
			
			
			//need to find the correct sample start
			
			long millisback = (long) (1000*(rawLoadObserver.startSample/clickDetector.getSampleRate())); 
			long filestartmillis = rawLoadObserver.dataStart-millisback; 
			//now that should be the start of the wav file. 
			//find the DAQ unit which is closest in time- might be entirely exact
			ListIterator<DaqStatusDataUnit> daqUnitIterator = daqStatusDataBlock.getListIterator(0);
			
			Debug.out.println("Looking for a DAQ unit starting at: " + 
			PamCalendar.formatDateTime(filestartmillis) + "  -- Data start: " + 
					PamCalendar.formatDateTime(rawLoadObserver.dataStart) + " No. Units: " + daqStatusDataBlock.getUnitsCount() + " Millis back : " + millisback); 
			
			long minmillis = Long.MAX_VALUE; 
			DaqStatusDataUnit daqStatusDataUnit =null;  
			DaqStatusDataUnit foundDaqStatusUnit = null; 
			while (daqUnitIterator.hasNext()) {
				daqStatusDataUnit = daqUnitIterator.next(); 
				long timeDiff = Math.abs(daqStatusDataUnit.getTimeMilliseconds() - filestartmillis); 
				if (timeDiff<minmillis) {
					minmillis = timeDiff; 
					foundDaqStatusUnit = daqStatusDataUnit; 
				}
			}
			
			Debug.out.println("Found  a DAQ unit starting at: " +
			PamCalendar.formatDateTime(foundDaqStatusUnit.getTimeMilliseconds()) +  "  Samples: " + foundDaqStatusUnit.getSamples() +  "  UID: " + foundDaqStatusUnit.getUID()); 

			//now have found the unit. 
			if (foundDaqStatusUnit==null) {
				Debug.err.print("Could not find the unit: ");
			}
			
			
			//filter the data
			double[][] filteredData = new double[rawLoadObserver.rawData.length][rawLoadObserver.rawData[0].length]; 
			for (int i=0; i<iirfFilters.length; i++){
				iirfFilters[i].runFilter(rawLoadObserver.rawData[i], filteredData[i]);
			}
			rawLoadObserver.rawData = null; //ensure memory is freed up. 
			
			rawDataChunk = new RawDataChunk(filteredData, rawLoadObserver.startSample + foundDaqStatusUnit.getSamples(),
					rawLoadObserver.dataStart, rawLoadObserver.dataEnd); 
			
			loadingRawData = false; 

		}
	}
	

	class RawDataChunk {

		long startMillis;

		long endMillis;

		long rawStartSample; 

		double[][] rawData;

		private RawDataChunk(double[][] rawData, long rawStartSample, long startMillis, long endMillis) {
			this.rawData = rawData;
			this.rawStartSample = rawStartSample; 
			this.startMillis = startMillis;
			this.endMillis = endMillis; 
		}
	}
	
	
	@Override
	public boolean canRun() {
		//TODO - can run should be based on whether there are sounds files loaded in viewer mode. 
		return true; 
	}
	

}


