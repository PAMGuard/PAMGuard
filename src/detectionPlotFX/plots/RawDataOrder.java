package detectionPlotFX.plots;


import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import PamguardMVC.LoadObserver;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import PamguardMVC.PamRawDataBlock;
import PamguardMVC.dataOffline.OfflineDataLoading;
import javafx.concurrent.Task;

/**
 * Used to load data for any display in which raw data is required to be loaded for detections.
 * This requires multi threading etc.  
 * @author Jamie Macaulay
 *
 * @param <D>
 */
public abstract class RawDataOrder {

	/**
	 * Observer of loaded data. 
	 */
	private RawObserver rawObserver;

	/**
	 * The task thread. 
	 */
	private Task<Integer> task;

	private Thread th; 

	/**
	 * Constructor for the raw data order. 
	 */
	public RawDataOrder(){
		rawObserver= new RawObserver(); 
	}


	/**
	 * Start the raw data loading on a new thread
	 * @param pamDetection - the pam detection
	 * @param buffer - a buffer for the pam data unit. 
	 */
	public void startRawDataLoad(PamDataUnit pamDetection, long buffer, int channel){
		//have to load waveform on a new thread. 
		if (pamDetection == null) {
			return;
		}

		PamRawDataBlock rawDataBlock; 
		if (pamDetection.getParentDataBlock() instanceof PamRawDataBlock) {
			//just in case we are trying to load from a raw data unit.
			rawDataBlock = (PamRawDataBlock) pamDetection.getParentDataBlock(); 
		}
		else {
			//Note: must use first data block in chain to deal with decimator stuff well. 
			rawDataBlock=(PamRawDataBlock) pamDetection.getParentDataBlock().getFirstRawSourceDataBlock();
		}


		//		System.out.println("RawDataOrder: Raw source data block samplerate : " + rawDataBlock.getSampleRate() + " "+ rawDataBlock.getDataName());

		long timeMillis= pamDetection.getTimeMilliseconds(); 
		long dataStart = timeMillis-buffer;


		//Note: do not try to add a float and a long. Weird things happen
		long dataEnd = pamDetection.getEndTimeInMilliseconds()+buffer;
		startRawDataLoad(rawDataBlock, dataStart,  dataEnd, channel);
	}


	/**
	 * Start the raw data loading on a new thread
	 * @param rawDataBlock - the raw data block 
	 * @param dataStart - data strat 
	 * @param dataEnd - dtaa end 
	 */
	public void startRawDataLoad(PamRawDataBlock rawDataBlock, long dataStart, long dataEnd, int channel){

		//		if (task!=null) task.cancel();
		//		
		//		try {
		//			if (th!=null) th.join(2000);
		//		} catch (InterruptedException e1) {
		//			// TODO Auto-generated catch block
		//			e1.printStackTrace();
		//		}

		//		System.out.println("dataStart: " + dataStart + " dataEnd: " + dataEnd + 
		//				" pamDetection.getDurationInMilliseconds() " + pamDetection.getDurationInMilliseconds() ); 
		//reset the observer for a load. 

		rawDataBlock.cancelDataOrder();

		rawObserver.resetForLoad(dataStart, dataEnd, rawDataBlock.getSampleRate(), channel);


		//		task = new Task<Integer>() {
		//			@Override protected Integer call() throws Exception {
		//				Thread.sleep(200); //wait a fraction of a second before firing up. 
		//				if (task.isCancelled()) {
		//					return -1; 
		//				}
		orderData( rawDataBlock,  dataStart,  dataEnd);
		//				return 0;
		//			}
		//			
		//			 @Override protected void cancelled() {
		//	             super.cancelled();
		//	             rawDataBlock.cancelDataOrder();
		//	         }
		//		};   
		//         th = new Thread(task);
		//         th.setDaemon(true);
		//         th.start();

	}


	/**
	 * Load some raw data
	 * @param rawDataBlock - the raw data block 
	 * @param dataStart - the start time to load from
	 * @param dataEnd - the end time to load from
	 */
	private void orderData(PamRawDataBlock rawDataBlock, long dataStart, long dataEnd){
		rawDataBlock.orderOfflineData(this.rawObserver, new RawLoadObserver(), dataStart, dataEnd, 0, OfflineDataLoading.OFFLINE_DATA_INTERRUPT);
	}

	/**
	 * Observes incoming FFT data and updates the spectrogram. 
	 * 
	 *<p>
	 * Note: that this data is on the AWT thread and must be switched to the FX thread before any processing takes place.  
	 * <p>
	 * Note: this will not handle any situation on which there is a gap in the raw data. 
	 * 
	 * @author Jamie Macaulay
	 *
	 */
	class RawObserver extends PamObserverAdapter {

		private int maxMb=1024*1024*10; //10 mega bytes

		private float fftSampleRate;

		/**
		 * The current count. 
		 */
		int count=0; 


		/**
		 * Temporary store of raw data for the whistle. 
		 */
		double[] rawData;

		/**
		 * The start of the data to load
		 */
		long dataStart;

		/**
		 * The end of the data to load
		 */
		long dataEnd;

		/**
		 * The channel to load. 
		 */
		int channel;

		/**
		 * Reset for loading a clip of raw data. 
		 * @param dataStart
		 * @param dataEnd
		 * @param sampleRate
		 * @return
		 */
		public boolean resetForLoad(long dataStart, long dataEnd, float sampleRate, int channel) {

			count=0; 
			this.dataStart=dataStart;
			this.dataEnd=dataEnd;
			this.channel=channel; 
			this.fftSampleRate=sampleRate;

			int dataFrame=(int) (sampleRate*((dataEnd-dataStart)/1000.));
			
//			System.out.println("RESET FOR LOAD: " + dataFrame);

			if (dataFrame>=maxMb){
				System.err.println("The raw data is way too big"); 
				return false;
			}
			else {
				//				System.out.println("Size of array: " +  dataEnd + " " + dataStart + " " + (int) (sampleRate*((dataEnd-dataStart)/1000.)) + " SR " + sampleRate );
				rawData=new double[(int) (sampleRate*((dataEnd-dataStart)/1000.))];
				return true;
			}

		}

		@Override
		public void addData(PamObservable o, PamDataUnit dataUnit) {
			newRawData((RawDataUnit) dataUnit); //do not put in FX thread!
		}

		private int currentIndex=0; 
		
		/***
		 * Called whenever new raw data is acquired
		 * @param dataUnit
		 */
		private void newRawData(RawDataUnit dataUnit) {
			if (rawData==null) return;
//					System.out.println("New raw data " +  count);
			//			try{
			if (PamUtils.hasChannel(dataUnit.getChannelBitmap(), channel)){

				double[] clipRaw = dataUnit.getRawData();
				for (int i=0; i<clipRaw.length; i++){
					if (count<rawData.length){
						rawData[count]=(float) clipRaw[i]; 
					}
					count++; 
				}

			}
			//			} 
			//			catch (Exception e){
			//				e.printStackTrace();
			//			}
		}

		/**
		 * Get the raw data sample rate.
		 */
		public float getSampleRate(){
			return this.fftSampleRate;
		}

		/**
		 * Get the raw data
		 * @return the raw data. 
		 */
		public double[] getRawData(){
			return this.rawData;
		}

		@Override
		public String getObserverName() {
			return "Whistle Detection Plot";
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
			return channel;
		}

		/**
		 * Get the requested load start. 
		 * @return
		 */
		public long getLoadStart() {
			return dataStart;
		}

	}

	/**
	 * Get the raw data observer whihc handles loading of raw data
	 * @return the raw data observer. 
	 */
	public RawObserver getRawDataObserver(){
		return this.rawObserver;
	}

	/**
	 * Get the raw data observer whihc handles loading of raw data
	 * @return the raw data observer. 
	 */
	public double[] getRawDataOrder(){
		return this.rawObserver.rawData;
	}


	/**
	 * Observe the success of the data load
	 * @author Jamie Macaulay
	 *
	 */
	public class RawLoadObserver implements LoadObserver {
		@Override
		public void setLoadStatus(int loadState) {
			if (loadState==PamDataBlock.REQUEST_DATA_LOADED){
			}
			dataLoadingFinished(rawObserver.rawData);
		}
	}
	

	/**
	 * Manually set the raw data in the observer.
	 * @param rawData - the raw data to set
	 * @param sR - the sample rate
	 * @param channel - the channel
	 * @param datestart
	 */
	public void setRawData(double[] rawData, float sR, int channel, long dateStart ) {
		rawObserver.rawData = rawData;
		rawObserver.fftSampleRate = sR; 
		rawObserver.channel = channel;
		rawObserver.dataStart = dateStart;
		rawObserver.dataEnd = (long) (dateStart+(1000.*rawObserver.rawData.length/sR));
	}


	/**
	 * Called whenever the data 
	 * @param rawData
	 */
	public abstract void dataLoadingFinished(double[] rawData);

}
