package pamViewFX.fxNodes.pamScrollers.acousticScroller;

import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import dataGram.DatagramDataPoint;
import dataGram.DatagramProvider;
import dataPlotsFX.scrollingPlot2D.Plot2DColours;
import dataPlotsFX.scrollingPlot2D.StandardPlot2DColours;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Implementation of AcousticScrollerGraphics which uses the existing datagram of a PamDataBlock to 
 * show information in the acoustic scroll bar graphics. 
 * @author Jamie Macaulay 
 *
 */
public class AcousticDataGramGraphics implements AcousticScrollerGraphics {

	/**
	 * The current data block. 
	 */
	private PamDataBlock<PamDataUnit> dataBlock; 


	/**
	 * The datagram provider. 
	 */
	private DatagramProvider dataGramProvider; 


	/**
	 * The datagram store. 
	 */
	private DataGramStore dataGramStore; 

	/**
	 * 
	 */
	private StandardPlot2DColours standardSpecColours; 

	/*
	 * Reference to the acoustic scroller. 
	 */
	private AcousticScrollerFX acousticScroller; ; 


	private Color nanColor=Color.TRANSPARENT; 

	public AcousticDataGramGraphics(AcousticScrollerFX acousticScroller, PamDataBlock<PamDataUnit> dataBlock){
		this.acousticScroller=acousticScroller; 
		this.dataBlock=dataBlock;
		this.standardSpecColours= new StandardPlot2DColours(); 

		standardSpecColours.getAmplitudeLimits()[0].set(100);
		standardSpecColours.getAmplitudeLimits()[0].set(160);

		dataGramProvider=dataBlock.getDatagramProvider();
		dataGramStore= new DataGramStore(acousticScroller.getRangeMillis()); 
	}


	@Override
	public PamDataBlock getDataBlock() {
		return dataBlock;
	}

	PamDataUnit lastData;
	@Override
	public synchronized void addNewData(PamDataUnit rawData){
		if (rawData==null) return; 
		try {
			if (rawData.getParentDataBlock()==dataBlock && lastData!=rawData){
				//now also must ensure that only data we need for this particular scroll bar is added. 
				//for example, if the clikc detector display is added and at 30mins but the acoustic scroller 
				//is only set to 5 mins then 30mins of data is added whihc messes everything up. 
				if (rawData.getTimeMilliseconds()>acousticScroller.getMinimumMillis() &&
						rawData.getTimeMilliseconds()<=acousticScroller.getMaximumMillis()) {
					dataGramStore.addData(rawData); 
					lastData=rawData; 
				}
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	Canvas canvas;
	Rectangle windowRect;
	long scrollEnd; 
	@Override
	public synchronized void repaint() {

		if (dataGramStore==null || dataGramStore.datagramStore[0]==null) return; 
		//paint the data gram. So need to paint the 
		//		//get the canvas. 
		canvas=acousticScroller.getScrollBarPane().getDrawCanvas();
		windowRect=new Rectangle(0,0, 	canvas.getWidth(), 		canvas.getHeight());
		scrollEnd=acousticScroller.getMaximumMillis();

		canvas.getGraphicsContext2D().clearRect(0, 0, windowRect.getWidth(), windowRect.getHeight());

		//Paint the first part of the writable image.

		//the start time of the datagram 
		double timeStarts=(dataGramStore.datagramStore[0].getStartTime()-acousticScroller.getMinimumMillis())/1000.;
		//		double timeStarts=(dataGramStore.datagramStore[0].getStartTime()-acousticScroller.getMinimumMillis()); 

		//		System.out.println("HELLOW Acoustic Datagram Store: "+dataGramStore.datagramStore[(int) PamUtils.constrainedNumber(dataGramStore.currentIndex-1,
		//				dataGramStore.storeSize-1)]);
		//		System.out.println("HELLOW Acoustic Datagram Store2: "+dataGramStore.datagramStore[(int) PamUtils.constrainedNumber(dataGramStore.currentIndex-1,
		//				dataGramStore.storeSize-1)].getEndTime());

		//the end time fo the datagram. 
		int endIndex=(int) PamUtils.constrainedNumber(dataGramStore.currentIndex-1,
				dataGramStore.storeSize-1);
		if (dataGramStore.datagramStore[endIndex]==null) {
			//might be destroying FX thread so put print statement here
			System.err.println("AcousticDataGramGraphics. The end of the datagRamStroe in Null??? ");
			return; 
		}
		double timeEnds=(dataGramStore.datagramStore[endIndex].getEndTime()-acousticScroller.getMinimumMillis())/1000.;

		//		System.out.println("Acoustic Scroller minimum: " + PamCalendar.formatDateTime2(acousticScroller.getMinimumMillis()));
		//		System.out.println("Acoustic Scroller minimum: " + PamCalendar.formatDateTime2(dataGramStore.datagramStore[0].getStartTime()));
		//		System.out.println("Acoustic Scroller minimum: " + PamCalendar.formatDateTime2(dataGramStore.datagramStore[1].getStartTime()));
		//		System.out.println("Acoustic Scroller minimum: " + PamCalendar.formatDateTime2(dataGramStore.datagramStore[(dataGramStore.currentIndex+1)%dataGramStore.datagramStore.length].getStartTime()));
		//
		//		System.out.println("timestarts: " + timeStarts);
		//		System.out.println("timeends: " + timeEnds);


		double dx=acousticScroller.getTimeAxis().getPosition(timeStarts);
		double dxEnd=acousticScroller.getTimeAxis().getPosition(timeEnds);

		//		System.out.println("dx: "+dx + " dxEnd: " +dxEnd+ " acousticScroller.getMinimumMillis(): " 
		//				+ acousticScroller.getMinimumMillis() + " time satrt: "+ timeStarts + 
		//				" time axis: " + acousticScroller.getTimeAxis().getMaxVal());

		canvas.getGraphicsContext2D().drawImage(dataGramStore.writableImage, 0, 0, dataGramStore.currentIndex, dataGramStore.nPoints, 
				dx, 0, dxEnd-dx, windowRect.getHeight());

		//Paint the last part of the writable image
		canvas.getGraphicsContext2D().drawImage(dataGramStore.writableImage, dataGramStore.currentIndex+1, 0, dataGramStore.storeSize-dataGramStore.currentIndex+1, dataGramStore.nPoints, 
				0, 0, dx, windowRect.getHeight());
	}


	@Override
	public String getName() {
		return dataBlock.getDataName();
	}

	public DataGramStore getDataGramStore(){
		return dataGramStore; 
	}

	/**
	 * The datagram store. 
	 * @author Jamie Macaulay
	 *
	 */
	public class DataGramStore {

		/**
		 * Holds all data points in the current offline display. 
		 */
		private DatagramDataPoint[]  datagramStore;

		/**
		 * The current writable image. There is a one to one correpsondeance between the size of the writable 
		 * image and the datagramStore. 
		 */
		private WritableImage writableImage;

		/**
		 * Current data
		 */
		private float[] currentData; 

		/*Number of points
		 * 
		 */
		private int nPoints; 


		/**
		 * The size of the store. The store is between two fixed time periods and so the size is also
		 * an indication of the resolution. 
		 */
		private int storeSize=500; 

		/**
		 * The current index in the store. 
		 */
		public int currentIndex=0; 

		/*
		 * The total number of writes.  
		 */
		private long totalWrites=0; 

		/**
		 * The time bin. This the milliseconds per single DatagramDataPoint
		 */
		double timeBin=0; 

		/**
		 * The start of the datagram point. 
		 */
		private long currentStartMillis=-1;

		/**
		 * The end of the datagram point. 
		 */
		private long currentEndMillis=-1;

		/*
		 * The total time range of the datagram in milliseconds.
		 */
		long timeRange; 

		/**
		 * Minimum colour value. This is the absolute minimum value that can be represented. The current 
		 * min colour is set by the colour params
		 */
		double minColVal=-30;

		/**
		 * Maximum colour value. This is the absolute maximum value that can be represented. The current 
		 * min colour is set by the colour params
		 */
		double maxColVal=30;

		/**
		 * The curent number of dataunits whihc have been added to the datagram bin. 
		 */
		int countInBin=0;

		/**
		 * The maximum number of dataunits which can be added befroe the datagram bin is off limits
		 * This prevents the display slowing down too much when there is a lot of data. 
		 * The values is number of dataunits per millisecond. 
		 */
		private static final double maxCount = 0.01;  //25 


		public DataGramStore(long timeRange){
			createStore(timeRange);
		}

		/**
		 * Add data to the datagram. 
		 * @param rawData
		 */
		public void addData(PamDataUnit rawData) {
			boolean newLine=checkTimeGap(rawData);
			if (countInBin/timeBin<maxCount || acousticScroller.isViewer){
				dataGramProvider.addDatagramData(rawData, currentData);
			}
			if (newLine){
				writeImageLine(datagramStore[Math.max(0,currentIndex-1)]); 
			}
		}


		long timediff;
		//private int usedDataUnits;
		float[] gramData;
		/**
		 * Checks everything is OK with the datagram and sets the correct time bin and prepares for writing datagram data. 
		 *@return true if a new datagram line has been completed. 
		 */
		public synchronized boolean checkTimeGap(PamDataUnit dataUnit){

			//			System.out.println("AcousticScrollerGraphics: Time Range: " + timeRange + " " + acousticScroller.getRangeMillis()
			//			+ "  " + PamCalendar.formatDateTime2(dataUnit.getTimeMilliseconds()));

			if (writableImage==null || timeRange!=acousticScroller.getRangeMillis()){
				timeRange=acousticScroller.getRangeMillis();
				//				System.out.println("Hello0");
				rebuildStore(timeRange); 
			}

			if (dataUnit.getTimeMilliseconds()>=currentStartMillis
					&& dataUnit.getTimeMilliseconds()<currentEndMillis){
				countInBin++;
			}

			if (currentStartMillis==-1){
				//				System.out.println("Hello1");
				currentStartMillis=dataUnit.getTimeMilliseconds();
				currentEndMillis=(long) (currentStartMillis + timeBin);
			}

			//calculate time difference. 
			timediff=dataUnit.getTimeMilliseconds() - currentStartMillis;

			if (timediff>timeRange){
				//				System.out.println("Hello2: "  + dataUnit.getTimeMilliseconds()  + " " + currentStartMillis + "timediff: " + timediff + "time_range"
				//						+ timeRange + " storeSize: " + storeSize + " " + timeBin);
				countInBin=0; 
				rebuildStore((long) (storeSize*timeBin)); 
				checkTimeGap(dataUnit); 
			}
			else{
				int n=0;
				while(!(dataUnit.getTimeMilliseconds()>=currentStartMillis
						&& dataUnit.getTimeMilliseconds()<currentEndMillis) 
						&& currentStartMillis<dataUnit.getTimeMilliseconds()){
					//					System.out.println("Hello3");


					/*
					 * I think this happens naturally when the data rate is low. 
					 */
					//					if (n++%10==0 && n!=1){
					//						System.out.printf("%s May be stuck in a weird while loop in AcousticDataGram graphics %d\n", getName(),  n ); 
					//					}
					//											System.out.println("in infinite looop:  dataunit: " + rawSoundData.getTimeMilliseconds() 
					//											+ " start: "+currentStartMillis + "end: "+ currentEndMillis+ " dataunit: "+rawSoundData);
					//											try {
					//												Thread.sleep(500);
					//											} catch (InterruptedException e) {
					//												// TODO Auto-generated catch block
					//												e.printStackTrace();
					//											}
					//										}
					datagramStore[currentIndex]= new DatagramDataPoint(currentStartMillis, currentEndMillis, 
							dataGramProvider.getNumDataGramPoints()); 

					gramData = datagramStore[currentIndex].getData(); 
					for (int i = 0; i < dataGramProvider.getNumDataGramPoints(); i++) {
						gramData[i] = (float) currentData[i];
						currentData[i] = 0.f;
					}
					datagramStore[currentIndex].setData(gramData, gramData.length);

					currentIndex++;
					countInBin=0; //reset the counter 


					if (currentIndex>=this.storeSize){
						currentIndex=0;
					}
					totalWrites++; 
					currentStartMillis=currentEndMillis; 
					currentEndMillis=(long) (currentEndMillis+timeBin); 
					//					n++;
				}

				return true; 
			}
			return false; 
		}


		/**
		 * Write the image line. 
		 * @param datagramStore
		 */
		public void writeImageLine(DatagramDataPoint datagramStore){
			if (datagramStore == null) return;
			if (datagramStore.getData()==null) return; 

			int iCol=0; 
			Color specCol; 
			for (int y = 0; y < nPoints; y++) {

				if (datagramStore.getData()[y] < 0) {
					writableImage.getPixelWriter().setColor(currentIndex,nPoints-1-y,nanColor);
				}
				else if (currentIndex == 0) {
					writableImage.getPixelWriter().setColor(currentIndex,nPoints-1-y,nanColor);
				}
				else {


					//a little bit of a hack but gives about right colours. 
					specCol=standardSpecColours.getColours(Math.log(datagramStore.getData()[y])+120);

					//System.out.println(20*Math.log(datagramStore.getData()[y]/Math.pow(10, -6))+201);

					//figure out the colour value. 

					//					iCol = (int) (standardSpecColours.getColourArray().length * ((Math.log(datagramStore.getData()[y]))-minColVal)/(maxColVal-minColVal));
					//
					//					//check color is not above or below the color range. 
					//					iCol = Math.min(Math.max(0, iCol), standardSpecColours.getColourArray().length-1);
					//					specCol=standardSpecColours.getColourArray()[iCol];


					writableImage.getPixelWriter().setColor(currentIndex, nPoints-1-y, specCol);
					//						datagramImage.setRGB(i, y, 0x0000FF);
				}
			}

		}; 


		/**
		 * Rebuild the store. 
		 * @param timeRange - the time range. 
		 */
		public void  rebuildStore(long timeRange){
			createStore(timeRange); 
		}

		/**
		 * Rebuild the image
		 */
		public void  rebuildImage(){
			writableImage=createWritableImage(nanColor);
			totalWrites=0; 
			final int writes=currentIndex; 
			//System.out.println("Writes: " + writes);
			for (int i=0; i<writes; i++){
				//				System.out.println("Write a line " +datagramStore[currentIndex]); 
				currentIndex=i;
				if (datagramStore[currentIndex]!=null){
					//					System.out.println("Write a line " +datagramStore[currentIndex].getData()[0]); 
					writeImageLine(datagramStore[currentIndex]); 
				}
			}
			currentIndex=writes; 
			//			System.out.println("REBUILD IMAGE2: "+ currentIndex);
		}


		/**
		 * Create the writable image with a default background colour.
		 * @param defaultCol - the default background colour. 
		 * @return a new writable image. 
		 */
		private WritableImage createWritableImage(Color defaultCol){
			if (dataGramProvider.getNumDataGramPoints()<=0) return null; 
			WritableImage writableImage=new WritableImage(storeSize, dataGramProvider.getNumDataGramPoints()); 
			for (int x=0; x<writableImage.getWidth(); x++){
				for (int y=0; y<writableImage.getHeight(); y++){
					writableImage.getPixelWriter().setColor(x, y, defaultCol);
				}

			}
			return writableImage;
		}

		/**
		 * Create the store which holds the datagram infoirmation
		 * @param timeRange
		 */
		private void createStore(long timeRange){
			this.timeRange=timeRange; 
			timeBin=timeRange/storeSize; 
			nPoints=dataGramProvider.getNumDataGramPoints(); 
			currentData=new float[dataGramProvider.getNumDataGramPoints()]; 
			datagramStore= new DatagramDataPoint[storeSize]; 
			writableImage=createWritableImage(nanColor);
			currentIndex=0; 
			totalWrites=0; 
			currentStartMillis=-1;
			currentEndMillis=-1;
		}

	}


	@Override
	public void clearStore() {
		this.dataGramStore.rebuildStore((long) (dataGramStore.storeSize*dataGramStore.timeBin));
	}

	/**
	 * True if the data block requires offline loading of data. 
	 */
	public boolean orderOfflineData(){
		return false;
	}


	@Override
	public void notifyUpdate(int flag) {
		// TODO Auto-generated method stub

	}


	@Override
	public void setColors(Plot2DColours specColors) {
		this.standardSpecColours=(StandardPlot2DColours) specColors;

		this.dataGramStore.rebuildImage();
	}


	@Override
	public Plot2DColours getColors() {
		return this.standardSpecColours;
	}

}
