package dataPlotsFX.rawDataPlotFX;

import pamViewFX.fxNodes.pamAxis.PamAxisFX;
import PamDetection.RawDataUnit;
import PamUtils.PamUtils;
import dataPlotsFX.JamieDev;
import javafx.geometry.Orientation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import pamMaths.PamInterp;


/**
 * Raw sound data is by default not stored in memory. There are good reasons for this, the first being that raw sound data is generally very large. That creates problems when trying to
 * display raw sound data on a display. Only a second or so is available in memory for plotting. In order to have display which show a more than a second or so of data there needs to be
 * an efficient way to store the display information for showing a sound data. 
 * <br><br>
 * This performs operations to plot one channel of raw sound data on a plot pane of a tdGraphFX. The data to display for each pixel is stored in an array and saved in memory. 
 * <br><br>
 * RealTime: //TODO
 * <br>
 * Viewer Mode: //TODO
 * <br>
 * The array is filled with data for each pixel from incoming raw sound data. Once the array is filled 
 * @author Jamie Macaulay
 *
 */
public class RawSoundPlotDataFX {
	
	/**
	 * Holds the current store of acoustic data. 
	 */
	private SoundDataStore soundStore= new SoundDataStore(10000);
	
	/**
	 * A copy of the last soundData array generated during real time operation. 
	 * <p>
	 * The back up store is only used in real time mode.  In real time it must be assumed that there is no longer access to the raw sound data.
	 *  The sound data is stored here in pixels. One way to change the range would be to have a writable image 
	 * which stretched, however this is way more memory than a 2D array. To stretch the current array needs to be interpolated. However
	 * say a smaller range is changed, then interpolation will result in some data being lost. Same with a larger range in which
	 * case the end of the array will be deleted. A user may be constantly stretching and contracting the display and therefore the result
	 * would be a gradual loss in quality. To get over this the backUpStore is used to keep a copy in memory of the last 'raw' pixel store
	 * from real time operation. If the range is changed the backupstore is used for interpolation- that way data is not lost. If real time 
	 * operation resumes, then the backup  store is deleted. Hence this is only really used if the display is paused and a user is investigating a
	 * section of data. 
	 */
	private SoundDataStore backUpStore;
	

	/**
	 * The channel displayed in this plot pane.
	 */
	private int iChannel;

	/**
	 * After writing chunks of raw data to pixels there maybe some bins left - not enough to full an entire pixel but if these are ignored then the display will start to drift slowly. 
	 * The excess are saved in bins left for the next chunk of raw sound data. 
	 */
	private int binsLeft=0; 
	
	/**
	 * The max value of the {@link #binsLeft};
	 */
	private double maxLeft=Double.MIN_VALUE; 
	
	/**
	 * The min value of the {@link #binsLeft};
	 */
	private double  minLeft=Double.MAX_VALUE; 
	
	/**
	 * The current samplerate. 
	 */
	private float sampleRate=1000;
	
	/**
	 * The colour of sound lines. 
	 */
	private Color lineColor=Color.BLUE;


	private double lineWidth=1;

	/**
	 * 
	 */
	private long lastTimeMillis=-1;

	/**
	 * The number of bins in the last data unit plotted. 
	 */
	private int lastRawLength; 
	
	public RawSoundPlotDataFX(RawSoundDataInfo rawSoundDataInfo, int channel){
		this.sampleRate=rawSoundDataInfo.getSampleRate();
		this.iChannel=channel;
		
		//enable viewer mode
		

	}
	
	public RawSoundPlotDataFX(){
		//sample rate must be set...
	}
	
	/**
	 * Set the sample rate.
	 * @param sampleRate sample rate in samples per second. 
	 */
	public void setSampleRate(float sampleRate){
		this.sampleRate=sampleRate;
	}
	
	/**
	 * Get the sample rate
	 * @return the sample rate in bins per second
	 */
	public float getSampleRate(){
		return sampleRate;
	}
	
	/**
	 * Check whether the time scale is correct.
	 */
	public void checkConfig(){
		
	}
	
	/**
	 * Convert new RawDataUnit to pixels and then add to {@link #soundData} array to be plotted on next repaint call. 
	 * @param rawDataUnit
	 */
	public void addNewRawData(RawDataUnit rawDataUnit){
		
		
		lastTimeMillis=rawDataUnit.getTimeMilliseconds();
		lastRawLength=rawDataUnit.getRawData().length;

		
//		//work out sample drift
//		long time=timeMillis-lastRawDataMillis;
//		double sRateTime=rawDataUnit.getRawData().length/(time/1000.); 
//		System.out.println("drift sample rate is " +sRateTime+" real sample rate is " + rawSoundDataInfo.getSampleRate() + " time "+time+" samples "+rawDataUnit.getRawData().length);
//		lastRawDataMillis=timeMillis;
		
		//wave data
		double[] rawData = rawDataUnit.getRawData();
		
		/**
		 * The first thing to do is check if the number of pixels is less than the number samples.
		 * If this is so then got to find the min and max values to display. If not then easy to plot. 
		 */
		int n=binsLeft; 
		double max = maxLeft;
		double min = minLeft;
		if (soundStore.binsPerPixel>1){
			//as we have multiple bins in each pixel. need to find the min/max for each chunk of raw data. 
			for (int i=0; i<rawData.length; i++){
				//keep track of max/min
				if (max<rawData[i]) max=rawData[i];
				if (min>rawData[i]) min=rawData[i];
				
				
				//if we have gone over one pixels then draw max/min
				if (n>= soundStore.binsPerPixel){
					//System.out.println("n: "+n + " i: "+ i+ " binsPerPixel: "+binsPerPixel); 
					writePixel(min,  max);
					//reset everything for next pixel. 
					n=0; 
					max = -Double.MAX_VALUE;
					min = Double.MAX_VALUE;
				}
				else n++;
			}
			
			/**
			 * Now we probably  have a chunk of data left which isn't as large as binsPerPixel. Can not ignore this or we begin a
			 * slow but noticeable drift in the display. Save the number if bins left and the max/min value of those bins in 
			 * fields for the next time raw data is added to the display.  
			 */
			binsLeft=n; 
			minLeft=min;
			maxLeft=max; 
		}
		else{
			for (int i=0; i<rawData.length-1; i++){
				//here one bin is one or more pixels. Therefore no need for a min max- essentially fill the data with raw sound data. 
				writePixel(0,  rawData[i]);
			}
		}
		
		//update last millis
		long durationMillis=(long) (double) rawDataUnit.getDurationInMilliseconds();
		soundStore.currentRawDataMillis=rawDataUnit.getTimeMilliseconds()+durationMillis;
		
//		System.out.println("Total sound writes: "+totalSoundWrites+" rawSoundDataIndex: "+rawSoundDataIndex+ " bins (millis:) "+1000*rawDataUnit.getDuration()/rawSoundDataInfo.getSampleRate()); 
	}
	
	
	/**
	 * Write a pixel into the sound store
	 * @param min - min value in units
	 * @param max - max value in units. 
	 */
	private void writePixel(double min, double max){
		soundStore.rawSoundDataIndex++;
		soundStore.totalSoundWrites++;
		if (soundStore.rawSoundDataIndex>=soundStore.storeSize){
			soundStore.rawSoundDataIndex=0; 
		}
		soundStore.soundData[soundStore.rawSoundDataIndex][0]=min;
		soundStore.soundData[soundStore.rawSoundDataIndex][1]=max;
	}
	
	/**
	 * Called whenever new raw sound data is to be added to display. 
	 * @param rawDataUnit - raw data unit. 
	 */
	public void newRawData(RawDataUnit rawDataUnit, double binsPerPixel) {
		
//		// crashes with a Stack Overflow error processing offline wav files. 
//		if (!JamieDev.isEnabled()) {
//			return;
//		}
		
		if (soundStore.binsPerPixel==binsPerPixel || binsPerPixel==-1){
			//add data to the sound data store. 
			soundStore.binsPerPixel=binsPerPixel; //need to set incase -1;
			backUpStore=null; //this needs to be deleted now. 
			checkTimeGap(rawDataUnit);
			addNewRawData(rawDataUnit); 
		}
		else {
			//need to recalc the current data in the array.
			recalcSoundData(binsPerPixel);
			//newRawData(rawDataUnit, binsPerPixel);
		}
	}
	
	
	double timediff;
	/**
	 * Check that the new rawdata is concurrent to the last fft. If not fill the spectrogram with blank sapce. 
	 */
	public void checkTimeGap(RawDataUnit rawSoundData){
		
		if (lastTimeMillis==-1) return; 
		
		//calculate time difference. 
		timediff=rawSoundData.getTimeMilliseconds() - (lastTimeMillis + lastRawLength*1000./this.getSampleRate()); 
		
//		System.out.println(" Time diff: " + timediff + " "+lastTimeMillis + " "+ rawSoundData.getTimeMilliseconds() );

		if (timediff>1){
			//calculate the number of bins we need to add. 
			double pixels=(this.getSampleRate()*(timediff/1000))/getBinsPerPixel(); 
			for (int i=0; i<pixels; i++){
//				System.out.println("soundStore.rawSoundDataIndex: "+soundStore.rawSoundDataIndex);
				writePixel(0,  0); 
			}
			//update last millis
			soundStore.currentRawDataMillis=rawSoundData.getEndTimeInMilliseconds();
		}
		
	}

	
	
	/**
	 * A new binsPerPixel value is being used. In real time mode will need to convert the store
	 * In viewer model will need to reload data from the store. 
	 * @param binsPerPixel2 - the new bins per pixel. The old bins per pixel is still stored in the binsPerPixel field. 
	 */
	public synchronized void recalcSoundData(double binsPerPixel2) {
		//TODO - viewer mode just loads up data from the data store...
		
		if (soundStore.totalSoundWrites<=0){
			soundStore.binsPerPixel=binsPerPixel2; //prevent stack overflow. 
			return;
		}
				
		//work out the new number of pixels we are going to be working with
		double totalBinsStored=soundStore.binsPerPixel*soundStore.totalSoundWrites; 
		
		//work out the new number of pixels. 
		double newNumberPixels=totalBinsStored/binsPerPixel2;
		
		//make a copy of the array. If the backup is there already don;t make a new one as
		//that's the 'raw' data. 
		if (backUpStore==null){
			backUpStore=soundStore.clone();
		}
		
		//find the start bin in the array of sound pixels. 
		int bin;
		if (backUpStore.totalSoundWrites<=backUpStore.storeSize) bin=0; //the array has zero still.
		else bin=backUpStore.rawSoundDataIndex+1;

		
		//now need to go through current data store and create an ordered array of times to 
		//allow for an interpolation to take place
		
		/**
		 * Note binsTimes is just a number for interpolation purposes. It has not other
		 * physical meaning than the ratio of time values between the two different binperpixel values. 
		 */

		//now need to go through current data store and create an ordered array of times to 
		//allow for an interpolation to take place
		double[] binTimes=new double[Math.min(backUpStore.totalSoundWrites, backUpStore.storeSize)];
		double[] minVal=new double[Math.min(backUpStore.totalSoundWrites, backUpStore.storeSize)];
		double[] maxVal=new double[Math.min(backUpStore.totalSoundWrites, backUpStore.storeSize)];

		
		
		int n=0; 
		for (int i=0; i<binTimes.length; i++){
			if (bin>=backUpStore.storeSize){
				bin=0;
			}
			binTimes[n]=n*backUpStore.binsPerPixel; //the current bin time array
			minVal[n]=backUpStore.soundData[bin][0];
			maxVal[n]=backUpStore.soundData[bin][1];
			bin++;
			n++;
		}
		
		double[] binTimesNew=new double[(int) (n*backUpStore.binsPerPixel/binsPerPixel2)];
		for (int i=0; i<binTimesNew.length; i++){
			binTimesNew[i]=i*binsPerPixel2; //the current bin time array
		}
		
		if (n<2) return;
		
		double[] newInterpMin=PamInterp.interp1(binTimes, minVal, binTimesNew);
		double[] newInterpMax=PamInterp.interp1(binTimes, maxVal, binTimesNew);
		
		
		
		//now clear sound store and add new values. The store starts from zero again and iterates through
		soundStore.soundData=new double[soundStore.storeSize][2];
		int start = Math.max(0,binTimesNew.length-soundStore.storeSize); //want to be efficient and not filling the soundstore array multiple times. 
		int count = start;
		bin=0;
		for (int i=start; i<binTimesNew.length; i++){
			//System.out.println(" i "+i%binTimesNew.length+ " binTimesNew.length: "+binTimesNew.length);
			//if the new array is greater in length the new a modules operator to overwrite
			if (bin>=backUpStore.storeSize){
				bin=0;
			}
			soundStore.soundData[bin][0]=newInterpMin[i];
			soundStore.soundData[bin][1]=newInterpMax[i];
			count++;
			bin++;
		}
		//reset counters.
		//System.out.println("COUNT: count%soundStore.storeSize: "  + count%soundStore.storeSize + " count: "+count );
		soundStore.rawSoundDataIndex=bin;
		soundStore.totalSoundWrites=count+start; 
		
		soundStore.binsPerPixel=binsPerPixel2;
		
	}
	
	int timeErrors = 0;
	/**
	 * Draw data from the array onto the screen. This functions assumes that the {@link #soundData} array contains data with the correct {@link #binsPerPixel} for the current time range on the display. 
	 * @param g2d - the graphics context to draw onto. 
	 * @param windowRect - rectangle describing the window. 
	 * @param orientation - the orientation of the display VERTICAL or HORIZONTAL. 
	 * @param timeAxis - the time axis. 
	 * @param dataAxis - the amplitude axis. 
	 * @param scrollStart - the start of the display in milliseconds datenum. The is the left most side of the display i.e. where the oldest visble data is displayed. 
	 * @param wrapPix - the wrap position on the time display  -1 if not wrapping.l 
	 *
	 */
	public void drawRawSoundData(GraphicsContext g2d, Rectangle windowRect, Orientation orientation, PamAxisFX timeAxis, PamAxisFX dataAxis,
			double scrollStart,  double wrapPix){
		
		if (soundStore.currentRawDataMillis==0){
			if (++timeErrors < 10) {
//			System.err.println("RawSoundPlotData: Raw sound data has no associated millisecond time: "+ soundStore.currentRawDataMillis);
			}
			return;
		}
		else {
			timeErrors = 0;
		}
		
		//set line colours
		g2d.setStroke(lineColor);
		g2d.setLineWidth(lineWidth);
		
		//the total number of pixels available to draw from array; 
		int dataCount=(int) Math.min(soundStore.totalSoundWrites, soundStore.storeSize);
		
		/**
		 * Figure out where the array starts. The array  may have been totally filled and is wrapping, or it may
		 * not be full and there's a load of blank space. We assume that the array has been filling continuously i.e. no gaps. 
		 */
		long arrayStart=(long) (soundStore.currentRawDataMillis-dataCount*(soundStore.binsPerPixel/getSampleRate())*1000.); 
		

		//Find out where in the array in memory the start of the screen is.
		double secondsBack=Math.abs(((soundStore.currentRawDataMillis-scrollStart)/1000.));
				
		//the number of pixels back in the array the start of visible data occurs.
		double arrayPixBack=Math.round(secondsBack/(soundStore.binsPerPixel/getSampleRate())); 
		
		//the position in the array of the start 
		int arrayPixStart=(int) PamUtils.constrainedNumber(soundStore.rawSoundDataIndex-arrayPixBack, soundStore.storeSize); 
		

		//Find the screen start pixel. 
		double startPixel=0;
		if (wrapPix==-1){
			//start pixel might not be zero 
			startPixel=timeAxis.getPosition((arrayStart-scrollStart)/1000.);
		}
		else startPixel=wrapPix;
		
		
		int start=0; 
		if (startPixel>0){
			start=(int) Math.round(startPixel);
		}
		
//		System.out.println(String.format("secondsBack: %.3f  arrayPixBack %.1f  arrayPixStart: %d start: %d soundStore.storeSize: %d wrapPix %.2f", secondsBack, arrayPixBack, arrayPixStart,start, soundStore.storeSize, wrapPix)); 

	
		int windowSize = (int) ((orientation==Orientation.HORIZONTAL) ? windowRect.getWidth(): windowRect.getHeight());
		
		int index=0; //index in array
		int pixel=start; //pixel on screen
		
		int n; 
		int end; //end of writing 
		if (wrapPix==-1){
			//scrolling
			n=start; //start of writing 
			end=windowSize; 
		}
		else {
			n=0; 
			end=start+windowSize; //wrapping display	
		}
			
	
		/**
		 * The pixel bin is the size between subsequent values in the soundstore in pixels
		 * When sound is below 
		 */
		double pixelbin=1; 
		if (soundStore.binsPerPixel<=1) {
			pixelbin=1/soundStore.binsPerPixel; 
			pixel=0; 
			start=0;
			end=windowSize; 
			arrayPixStart=0; 
		}
		
		//System.out.println("Start painting: " +pixel + "   pixelbin "  + pixelbin + " end: " + end + " arrayPixStart " + arrayPixStart); 

		int count =0; 
		for (int i=start; i<end; i++){
			/**
			 * Find the pixel in the array. If the array is not full then first pixel starts at zero. 
			 * Else first pixel starts at current rawSoundDataIndex. 
			 */
			index=arrayPixStart+n;
			n++; 



				while (index>=soundStore.storeSize) {
					index=index-soundStore.storeSize; 
				}

				if (index<0) return;

				//.out.println("i: "+ i + " index: "+index); 

				/**
				 * Now there are two situations. Either we have an array in which there is more than one bin per pixel or we have an array 
				 * which basically holds raw sound data. 
				 */
				if (soundStore.binsPerPixel>1){

				if (orientation==Orientation.HORIZONTAL){
					g2d.strokeLine(pixel, dataAxis.getPosition(soundStore.soundData[index][0]),
							pixel, dataAxis.getPosition(soundStore.soundData[index][1]));
				}
				else {
					g2d.strokeLine(dataAxis.getPosition(soundStore.soundData[index][0]),
							pixel, dataAxis.getPosition(soundStore.soundData[index][1]),i);
				}
			}
			//now this array holds raw sound data. Therefore don't need to plot min/max;  
			else{
				if (orientation==Orientation.HORIZONTAL && i<soundStore.soundData[index].length-1){
				count++;
				g2d.strokeLine(pixel, dataAxis.getPosition(soundStore.soundData[index][0]),
						pixel+pixelbin, dataAxis.getPosition(soundStore.soundData[index][1]));

				g2d.fillOval(pixel, dataAxis.getPosition(soundStore.soundData[index][0]), 2, 2);
				}
				//					}
				//TODO; 
				//g2d.strokeLine((i/binsPerPixel + tCStart), yAxis.getPosition(rawData[i]), ((i+1)/binsPerPixel + tCStart), yAxis.getPosition(rawData[i+1]));
			}


			pixel++; 


			while (pixel>windowSize){
				pixel-=windowSize;
			}
		}
		
		if (wrapPix>=0){
			g2d.setStroke(Color.RED);			
			g2d.setLineWidth(1);
			g2d.strokeLine(start, 0, start,windowRect.getHeight());
		}
		
		//System.out.println("Finished painting: " +pixel + " index: " +index + " count: " + count); 

		}

	
	/**
	 * Reset the raw sound data for loading.
	 * 
	 */
	public void resetForLoad() {
		clearRawData(); 
	}

	/**
	 * Reset the array, clearing all data. 
	 */
	public void clearRawData() {
		//System.out.println("Clear raw sound data: "); 
		soundStore.soundData=new double[soundStore.storeSize][2];
		soundStore.rawSoundDataIndex=0; 
		soundStore.totalSoundWrites=0; 
	}
	
	/**
	 * A class to package info about a sound store. 
	 * @author Jamie Macaulay
	 *
	 */
	private class SoundDataStore implements Cloneable {
		
		
		protected SoundDataStore(int storeSize){
			this.storeSize=storeSize;
			soundData=new double[storeSize][2];
		}
		
		/**
		 * The raw sound data to store in pixels. Currently 8000 as it is unlikely displays be greater than 8000 pixels in width or height
		 * at any time in the near future. Storing date like this means users can scroll back a little in real time mode and in viewer allows#
		 *  for smoother scrolling.  
		 */
		protected int storeSize;
		
		/**
		 * This array holds raw sound data. This does not hold raw samples but the min/max for each pixel on the screen. e.g.say the time range on the display means that
		 * each pixel corresponds to 1 millisecond (screen = 1000 pixels wide and time range is 1 second). If the the sample rate is 500kS/s then this means each pixels corresponds 
		 * to 500 samples. In the display the min/max of those 500 samples is shown on the pixel. This array stores those min/max values by holding data and wrapping. 
		 * Only when the number of pixels<the number of samples in time range does this hold raw samples. 
		 */
		protected double[][] soundData;
		
		/**
		 * The current index in the {@link #soundData} array. 
		 */
		protected int rawSoundDataIndex=0; 
		
		/**
		 * Keeps a record of the total number of writes to {@link #soundData} array. Returns to zero when array reset.
		 */
		protected int totalSoundWrites=0; 
		
		/**
		 * The position of rawSoundDataIndex in millis datenum. This the position of the last bin...not the
		 * start of the last raw deata unit added. 
		 */
		long currentRawDataMillis=0; 
		
		/**
		 * The number of bins per pixel. 
		 */
		private double binsPerPixel=-1; 
		
		/* (non-Javadoc)
		 * @see java.lang.Object#clone()
		 */
		@Override
		protected SoundDataStore clone() {
			try {
				return (SoundDataStore) super.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
				return null;
			}
		}
		
	}

	public double getBinsPerPixel() {
		return soundStore.binsPerPixel;
	}

}
