package rawDeepLearningClassifier.segmenter;

import java.util.Arrays;

import PamDetection.PamDetection;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;

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
