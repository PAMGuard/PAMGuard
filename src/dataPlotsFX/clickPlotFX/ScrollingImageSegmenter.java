package dataPlotsFX.clickPlotFX;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import PamguardMVC.PamDataUnit;
import dataPlotsFX.projector.TDProjectorFX;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.util.Duration;

/**
 * A method to quickly plot data units on the display
 * <p>
 * A list of writable images is stored in memory. These record the UID of the data unit that has been painted
 * and repainting of the data unit does not occur unless a specific clear() function is called. 
 * 
 * TODO - better description needed. 
 * 
 * @author Jamie Macaulay
 *
 */
public abstract class ScrollingImageSegmenter {

	/**
	 * The number of seconds each pixel represents. 
	 */
	public double millisPerPixel = 1; 

	/**
	 * The maximum number of allowed images.
	 */
	private int maxNumImages = 100; //100 images should roughly be 200MB

	/**
	 * A hash map of writable images. 
	 */
	private ArrayList<WritableImageSegment> imageSegments = new ArrayList<WritableImageSegment>(); 

	/**
	 * Keep a current reference to the current segment in memory. 
	 */
	private WritableImageSegment currentSegment; 

	/**
	 * The duration of the writable image. 
	 */
	private int imageWidth = 1024;  

	/**
	 * The height of the writable image. 
	 */
	private int imageHeight = 256;  

	/**
	 * The start time of the image buffer in milliseconds time. 
	 */
	private long imageBufferStart = -1;

	/**
	 * 	The buffer needs checked. 
	 */
	private boolean bufferCheck = false; 

	/**
	 * Timeline for repainting images.
	 */
	private Timeline timeline;

	/**
	 * The last paint time
	 */
	private long lastTime; 

	/**
	 * The minimum time between repaints. 
	 */
	private long tm = 100; //millis

	/**
	 * The minimum and maximum y values for the image i.e what values the top and bottom of the image represent. 
	 */
	private double minYVal = 0; 

	/**
	 * The minimum and maximum y values for the image i.e what values the top and bottom of the image represent. 
	 */
	private double maxYVal = 1;

	/**
	 * The current size of the display in pixels 
	 */
	private double displayWidthPixels;

	/**
	 * RThe number of y axis units per pixel. 
	 */
	private double yPerPixel = -1; 

	/**
	 * Create a new scrolling 2D segmented. 
	 */
	public ScrollingImageSegmenter(double minY, double maxY) {
		setYMinMax(minY, maxY); 
	}

	/**
	 * Set the minimum and maximum y values of the writable image (what is stored in memory). 
	 * @param minY  - the minimum y value of the writable image. 
	 * @param maxY  - the maximum y value of the writable image
	 */
	public void setYMinMax(double minY, double maxY) {
		this.minYVal = minY; 
		this.maxYVal = maxY; 
		yPerPixel = (maxY - minY)/imageHeight; 
	}

	/**
	 * Reset the buffer so that all images are cleared all data units must redraw. 
	 */
	public void resetImageBuffer() {
		//System.out.println("RESET IMAGE BUFFER"); 
		imageSegments.clear(); 
	}
	

	/**
	 * Return the number of milliseconds each width pixel corresponds to 
	 * @return the number of milliseconds per pixel. 
	 */
	public double getMillisePerPixel() {
		return millisPerPixel; 
	}



	/**
	 * Get the location of the last image. 
	 * @return the location of the last image. 
	 */
	public long getImageSegmentEnd() {
		return (long) (imageBufferStart + (imageSegments.get(imageSegments.size()-1).getPixelStart()+imageWidth)*millisPerPixel); 
	}

	/**
	 * Get the image millis time for a given pixel. This 
	 * @param pixel - pixels form start of the first image segment in the buffer
	 */
	public long getImageMillisTime(double pixel) {
		if (imageBufferStart==-1) return -1; 
		else {
			return (long) (imageBufferStart + pixel*millisPerPixel); 
		}
	}

	/**
	 * Get the image pixel time for a given millis. This is the total pixels form the first image segment in the buffer. 
	 * @param timeMillis - the time in millis. 
	 */
	public double getImagePixelTime(long timeMillis) {
		if (imageBufferStart==-1) return -1; 
		else {
			return (timeMillis - imageBufferStart)/millisPerPixel; 
		}
	}


	/**
	 * Check the millis per second value is correct for the size of the display. 
	 * @param pixelsWidth - the width of the display in pixels. 
	 * @param secondsWidth - the width of the display in seconds. 
	 * @return true if the value has change. 
	 */
	private boolean checkMillisPerPixel(double pixelsWidth, double secondsWidth) {
		//try and round so that there are an integer number of milliseconds per pixel. This is because PAMGuard
		//data units have a millisecond start time. 

		double milliswidth = secondsWidth*1000/pixelsWidth; 

		//System.out.println("CHECK Millis Per Pixel Current: " + millisPerPixel + "  " + milliswidth); 


		if (milliswidth<millisPerPixel/2 || milliswidth>2*millisPerPixel) {
			//time to chenge 

			//System.out.println("Millis per pixel has changed from " +  millisPerPixel + " to " + Math.ceil(milliswidth) + " pixelswidth: " + pixelsWidth + " secondsWidth " + secondsWidth); 

			if (milliswidth>=1) millisPerPixel = Math.ceil(milliswidth); //round to the nearest integer
			else millisPerPixel = milliswidth; 
			return true; 
		}

		return false; 
	}

	/**
	 * Get the number of X pixels into an image that the time in millis represents
	 * @param timeMillis - the time in milliseconds
	 * @return - the number of x pixels into the image segment; 
	 */
	public double getImageXPixels(double timeMillis, WritableImageSegment writiableImage) {
		//		System.out.println("Timemillis: " + (timeMillis - writiableImage.getMillisStart()) + 
		//				" Writable image start: " + PamCalendar.formatDBDateTime(writiableImage.getMillisStart(), true)); 
		return (timeMillis - writiableImage.getMillisStart())/this.millisPerPixel;	
	}

	/**
	 * Get the number of X pixels into an image that the time in millis represents
	 * @param value - the y value in any units
	 * @return - the number of y pixels into the image segment; 
	 */
	public int getImageYPixels(double value, WritableImageSegment writiableImage) {
		//		System.out.println("Value: " + value + "yPerPixel: " + yPerPixel); 
		return (int) (value/this.yPerPixel); 
	}

	/**
	 * Get the writable image chunk for the start time. If the writable image segment does not exist then it is created. 
	 * @param - the start time in milliseconds.
	 * @return the writable image segment. 
	 */
	private WritableImageSegment findWritableImageChunk(long dataUnitStartTime) {

		if (this.millisPerPixel<=0 || this.millisPerPixel>=Double.POSITIVE_INFINITY) return null; //no point in messing around. 

		//segment must be pixel aligned i.e. the first pixel on a new image must be the pixel after the last pixel on the previous image. 
		//holding images by millis time would mean this sometimes does not happen so they must be counted from the start time. 
		WritableImageSegment writableImageSegment;
		bufferCheck = false;

		//		System.out.println("Buffer start: " + imageBufferStart + " millisperpixel: " + millisPerPixel + " dataunitstrattime: " + dataUnitStartTime + " " + (dataUnitStartTime-imageBufferStart)); 
		//if there are no images create a new one
		if (imageSegments.size()==0) {
			imageBufferStart = dataUnitStartTime-1; 
			writableImageSegment = new WritableImageSegment(imageWidth, imageHeight, 0); 
			imageSegments.add(writableImageSegment); 
		}
		else if (dataUnitStartTime<imageBufferStart) {
			//If the data unit is before the image start then we need to create a new 
			//start image an ensure the other images have their time updated. 

			int pixelsBack = 0;
			long newBufferStart = imageBufferStart; 

			while (dataUnitStartTime<newBufferStart) {
				pixelsBack = pixelsBack + imageWidth; 
				newBufferStart =(long) (imageBufferStart-(millisPerPixel*pixelsBack)); //doing it this way ensure pixel errors do not 
			}

			writableImageSegment = new WritableImageSegment(imageWidth, imageHeight, 0); 
			imageSegments.add(writableImageSegment); 

			//now ensure the pixel start of the other segments is updated
			for (int i=0; i<imageSegments.size(); i++) {
				imageSegments.get(i).setPixelStart(imageSegments.get(i).getPixelStart()-pixelsBack); 
			}

			//			System.out.println("2 New buffer start : old buffer: " + imageBufferStart + " new buffer: " + newBufferStart); 

			this.imageBufferStart = newBufferStart; 

			bufferCheck = true; 
		}
		else {
			//now figure out which image should located
			double imagePixelTime = getImagePixelTime(dataUnitStartTime); 

			//use the stream API to try and find the correct segment. 
			writableImageSegment = imageSegments.stream()
					.filter(aSegment -> imagePixelTime>=aSegment.getPixelStart() &&  imagePixelTime<(aSegment.getPixelStart() + imageWidth))
					.findFirst()
					.orElse(null);

			//the image has not been found....need to create a new image
			if (writableImageSegment == null) {
				//				System.out.println("3 Make a new image: " + imagePixelTime); 

				//create a new segment and add to the list
				writableImageSegment = new WritableImageSegment(imageWidth, imageHeight, (int) Math.floor(imagePixelTime/imageWidth)*imageWidth); 
				imageSegments.add(writableImageSegment); 
			}

			bufferCheck = true; 
		}


		return writableImageSegment; 
	}



	/**
	 * Check the writable images and remove fringe images if necessary. 
	 * @return true if some images have been deleted from memory. 
	 */
	public synchronized boolean bufferCheck(double starttime, double secondsWidth) {
		//check the number of writable images. 
		//System.out.println("The image segment to check : " + this.imageSegments.size() + " millisperpixel: " + this.millisPerPixel); 
		//aim to keep all images within the display width and as many images after that equally in front of and after the current time. 

		if (imageSegments.size()<maxNumImages) return false; 

		//use a stream to filter the writable images  
		List<WritableImageSegment> writableImagesInDisplay = this.imageSegments
				.stream()
				.filter(c -> c.getMillisEnd() >= starttime && c.getMillisStart()<(starttime+secondsWidth*1000))
				.collect(Collectors.toList());

		//check the number of writable images. 

		double imageSeconds =(millisPerPixel*imageWidth)/1000.; 
		System.out.println("BufferCheck: How many images are within the display? : " + writableImagesInDisplay.size() 
		+ " Should be in display:? " + secondsWidth/imageSeconds); 

		//		if (writableImagesInDisplay.size()>maxNumImages) {
		this.imageSegments = (ArrayList<WritableImageSegment>) writableImagesInDisplay; 
		//		}

		//		//so we have some room to keep images., We take a window forward and backward half the display size and full up the buffer. 
		//		for ()


		for (int i =0; i<this.imageSegments.size(); i++) {

		}

		return false; 
	} 


	/**
	 * Draw the data unit onto a writable image segment. 
	 * @param g - the graphics context the image will be drawn onto
	 * @param pamDataUnit - the data unit. 
	 * @param writableImage - the writable image 
	 * @param tdProjector - the TD  projector for the display
	 * @param scrollStart - the start of the display in milliseconds 
	 */
	public abstract void drawADataUnit(GraphicsContext g, PamDataUnit  pamDataUnit, WritableImageSegment writableImage, TDProjectorFX tdProjector, double scrollStart);


	/**
	 * Get the polygon for the detection. This allows it to be selected by markers. 
	 * @param g - the graphics context the image will be drawn onto
	 * @param pamDataUnit - the data unit. 
	 * @param writableImage - the writable image 
	 * @param tdProjector - the TD  projector for the display
	 * @param scrollStart - the start of the display in milliseconds 
	 */
	public abstract Path2D getPath2D(PamDataUnit  pamDataUnit, WritableImageSegment writableImage, TDProjectorFX tdProjector, double scrollStart);


	/**
	 * Draw the spectrogram on the 
	 * @param g - the graphics context.
	 * @param pamDataUnit - the pamDataUnit. 
	 * @param tC - the time to plot in seconds form the start of the display. 
	 * @return the Path2D of the click. 
	 */
	public Path2D drawDataUnit(GraphicsContext g, PamDataUnit  pamDataUnit, TDProjectorFX tdProjector, double scrollStart) {
		

		//check the image is OK. 
		this.displayWidthPixels = tdProjector.getWidth(); 

		//check the size of the stored images in memory. If a very large time axis is shwoing then the images need to 
		//show more data per pixel otherwise either memory will run out or there will not be enough of an image buffer
		//to paint a full display. 
		boolean checkImage = checkMillisPerPixel(tdProjector.getWidth(), tdProjector.getTimeAxis().getMaxVal()); 

		if (checkImage) {
			//System.out.println("CHECK IMAGE BUFFER RESET"); 
			this.resetImageBuffer();
		}

		//get the writable image chunk
		WritableImageSegment writableImageChunk = findWritableImageChunk(pamDataUnit.getTimeMilliseconds());
		
		//System.out.println("draw data unit: UID" + pamDataUnit.getUID() + " Image chunk: " + writableImageChunk); 


		if (writableImageChunk==null) return null; 

		//check the image buffer to ensure that not too many images are stored in memory
		if (bufferCheck) {
			this.bufferCheck(scrollStart, tdProjector.getTimeAxis().getMaxVal()); 
			bufferCheck=false; 
		}

		//paint the image 
		if (!writableImageChunk.searchArrayReuseSet(pamDataUnit.getUID())) {

			/***This is where the data unit UID is stored so that it is not painted again until clear is called****/
			writableImageChunk.addDrawnUnit(pamDataUnit.getUID()); //increase speed by not drawing unless necessary
			/***/
			
			drawADataUnit( g, pamDataUnit,  writableImageChunk,  tdProjector, scrollStart);
			//paintImages(g, tdProjector, scrollStart, 0); 
		}

		//paintImages(g, tdProjector, scrollStart, 0); 

		return getPath2D(pamDataUnit, writableImageChunk,  tdProjector, scrollStart); //TODO - should always be returning a path2D. 
	}


	/**
	 * Paint the images. 
	 * @param g - the graphics context.
	 * @param tdProjector - the projector of the graph. 
	 * @param scrollStart - the start of the display in millis.
	 * @param tm - the repaint time in millis.
	 */
	public void paintImages(GraphicsContext g, TDProjectorFX tdProjector, double scrollStart, long tm) {
		//System.out.println("ScrollingImageSegmenter: paintImages: " + tm); 

		if (tm==0) {
			paintImages(g, tdProjector, scrollStart);
			return;
		}
		//paint the images.
		// Start of block moved over from the panel repaint(tm) function. 
		long currentTime=System.currentTimeMillis();
		if (currentTime-lastTime<tm){
			//start a timer. If a repaint hasn't be called because diff is too short this will ensure that 
			//the last repaint which is less than diff is called. This means a final repaint is always called 
			if (timeline!=null) timeline.stop();
			timeline = new Timeline(new KeyFrame(
					Duration.millis(tm),
					ae -> paintImages(g, tdProjector, scrollStart)));
			timeline.play();
			return;
		}
		else {
			paintImages(g, tdProjector, scrollStart);
		}
		lastTime=currentTime;
	}

	/**
	 * Paint the writibale images onto the graphics context. 
	 * @param g - the graphics context
	 * @param tdProjector - the td projector. 
	 * @param scrollStart - the scroll start. 
	 */
	private void paintImages(GraphicsContext g, TDProjectorFX tdProjector, double scrollStart) {
		double tC;
		double tCEnd; 
		double y1; 
		double y2; 
		double tcMillis; 
				
		for (int i=0; i<this.imageSegments.size(); i++) {
			//Parameters:
			//    img - the image to be drawn or null.
			//    sx - the source rectangle's X coordinate position.
			//    sy - the source rectangle's Y coordinate position.
			//    sw - the source rectangle's width.
			//    sh - the source rectangle's height.
			//    dx - the destination rectangle's X coordinate position.
			//    dy - the destination rectangle's Y coordinate position.
			//    dw - the destination rectangle's width.
			//    dh - the destination rectangle's height. 

			//get the location on the canvas to draw the image. 

//						System.out.println("Paint the image: tC: "
//								+ "writiableimage segment start: " + 
//								PamCalendar.formatDBDateTime(imageSegments.get(i).getMillisStart()) + " scrollStart: " +  PamCalendar.formatDBDateTime(scrollStart) 
//								+ " writableImageChunk: " + imageSegments.get(i).hashCode() + " N: " + imageSegments.size()); 

			tcMillis = imageSegments.get(i).getMillisStart()-scrollStart;



			tC = tdProjector.getTimePix(tcMillis);
			tCEnd = tdProjector.getTimePix(tcMillis + imageSegments.get(i).getWidth()*millisPerPixel);

			y1=tdProjector.getYPix(minYVal); 
			y2=tdProjector.getYPix(maxYVal); 


//						System.out.println("Paint the image: tC: "
//								+ "" + tC + " tCEnd: " + tCEnd + " tcMillis " + tcMillis + " y1: " + y1 + " y2 " + y2 ); 

			g.drawImage(imageSegments.get(i),
					0,
					0,
					imageSegments.get(i).getWidth(),
					imageSegments.get(i).getHeight(),
					tC,
					y1,
					tCEnd-tC,
					y2-y1);	

		}
	}

	/**
	 * A single segmenting of writable image. Contains a list of data that have already been painted on the image. 
	 * 
	 * @author Jamie Macaulay 
	 *
	 */
	public class WritableImageSegment extends WritableImage implements  Comparable<WritableImageSegment>  {

		/**
		 * The start of the writable image from the very first writable image in pixels. i.e. this can be a vary 
		 * large number of pixels.
		 */
		private int pixelstart;

		/** 
		 * The UID values. 
		 */
		private HashSet<Long> uidVals = new  HashSet<Long>(); 
		
		/**
		 * An extra number that can be used to store additional information
		 */
		private Number extraInfo = null;

		/**
		 * Create a writable image segment. 
		 * @param xpixels - the xpixels
		 * @param ypixels - the y pixels
		 * @param timestart - the start time of the image in pixels from the first image in the whole segment list. 
		 */
		public WritableImageSegment(int xpixels, int ypixels, int pixelstart) {
			super(xpixels, ypixels);
			this.pixelstart=pixelstart;
		}

		/**
		 * Set the pixel start time of the image. This is the pixel start from the very first image. 
		 * @param pixelstart - the pixel start. 
		 */
		public void setPixelStart(int pixelstart) {
			this.pixelstart = pixelstart; 
		}

		/**
		 * Get the start time in pixels. 
		 */
		public int getPixelStart() {
			return pixelstart;
		}

		/**
		 * Convenience function the start time in milliseconds. 
		 * @return the start time of the image in millis
		 */
		public long getMillisStart() {
			return imageBufferStart + ((long) (pixelstart*millisPerPixel));
		}


		/**
		 * Convenience function the start time in milliseconds. 
		 * @return the end time of the image in millis
		 */
		public long getMillisEnd() {
			return imageBufferStart + ((long) ((pixelstart+this.getWidth())*millisPerPixel));
		}


		@Override
		public int compareTo(WritableImageSegment writableImage ) {
			return writableImage.getPixelStart() > this.getPixelStart() ? -1:1;
		}

		/**
		 * Check whether the UID value has been painted. 
		 */
		public boolean searchArrayReuseSet(long uidval) {
			//super fast way to find if something is contained in a list. 
			return uidVals.contains(uidval);
		}

		/**
		 * Add a unit to the array reuse set...this will result in the data unit not being drawn
		 * again until the buffer is reset. 
		 * @param uidval - the UID value
		 * @return true if the set did not already contain the UID value
		 */
		public boolean addDrawnUnit(long uidval) {
			//super fast way to find if something is contained in a list. 
			return uidVals.add(uidval);
		}

		public ScrollingImageSegmenter getScrollingPLot2DSegmenter() {
			return ScrollingImageSegmenter.this;
		}
		
		/**
		 * Get the extra info
		 * @return - the extra info
		 */
		public Number getExtraInfo() {
			return extraInfo;
		}

		/**
		 * Set the extra info. 
		 * @param extraInfo - the extra info to send. 
		 */
		public void setExtraInfo(Number extraInfo) {
			this.extraInfo = extraInfo;
		}
	}



	/**
	 * Get the image segments. 
	 * @return the image segments
	 */
	public ArrayList<WritableImageSegment> getImageSegments() {
		return imageSegments;
	}

	/**
	 * Get the maximum y value. This the y value that the top of the image segments correspond to. 
	 * @return the max y value. 
	 */
	public double getMaxY() {
		return this.maxYVal;
	}

	/**
	 * Get the minimum y value. This the y value that the bottom of the image segments correspond to. 
	 * @return the max y value. 
	 */
	public double getMinY() {
		return this.maxYVal;
	}

}
