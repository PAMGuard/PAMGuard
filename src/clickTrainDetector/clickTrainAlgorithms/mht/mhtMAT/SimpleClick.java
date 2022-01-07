package clickTrainDetector.clickTrainAlgorithms.mht.mhtMAT;

import PamDetection.AbstractLocalisation;
import PamguardMVC.PamDataUnit;
import PamguardMVC.RawDataHolder;
import PamguardMVC.RawDataTransforms;

/**
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class SimpleClick extends PamDataUnit implements RawDataHolder  {
	
	/**
	 * The time in milliseconds. 
	 */
	public double timeSeconds; 

	/**
	 * The amplitude of the click in dB
	 */
	public double amplitude;

	/**
	 * The bearing of the click. 
	 */
	private SimpleLocalisation simpleLocalisation;

	/**
	 * The waveform. (ALways only holds one waveform but in this format for compatibility reasons)
	 */
	private double[][] waveform; 
	

	/**
	 * Create a simple click
	 * 
	 * @param UID - a unique identifier for the click
	 * @param timeSeconds 0- the time in seconds
	 * @param amplitude - the amplitude in dB
	 */
	public SimpleClick(Integer UID, double timeSeconds, double amplitude, float sR) {
		super(((long) (timeSeconds*1000.))); 
		//System.out.println("Time millis: " + getTimeMilliseconds() + "  " + timeSeconds);
		this.timeSeconds=timeSeconds; 
		this.amplitude=amplitude; 
		this.setStartSample((long) (timeSeconds*sR));
		this.setChannelBitmap(1);
		this.setUID(UID);
	}
	
	/**
	 * Create a simple click.
	 * @param UID - a unique identifier for the click
	 * @param timeSeconds 0- the time in seconds
	 * @param amplitude - the amplitude in dB
	 * @param bearing - the bearing in DEGREES
	 * @param sR - the sample rate 
	 */
	public SimpleClick(Integer UID, Double timeSeconds, Double amplitude, Double bearing, float sR) {
		super(((long) (timeSeconds*1000.))); 
		//System.out.println("Time millis: " + getTimeMilliseconds() + "  " + timeSeconds);
		this.timeSeconds=timeSeconds; 
		this.amplitude=amplitude; 
		
		if (bearing!=null) this.simpleLocalisation=new SimpleLocalisation(this, bearing); 
		
		this.setStartSample((long) (timeSeconds*sR));
		this.setChannelBitmap(1);
		this.setUID(UID);

	}

	/**
	 * Create a simple click.
	 * 
	 * @param UID - a unique identifier for the click
	 * @param amplitude - the amplitude in dB
	 * @param bearing - the bearing in DEGREES
	 * @param sR - the sample rate 
	 * @param waveform - the waveform 
	 * @param sR - the sample rate.
	 */
	public SimpleClick(Integer UID, Double timeSeconds, Double amplitude, Double bearing, double[] waveform, float sR) {
		super(((long) (timeSeconds*1000.))); 
		
		this.timeSeconds=timeSeconds; 
		this.amplitude=amplitude; 
		this.waveform = new double[][] {waveform}; 
		
		if (bearing!=null) this.simpleLocalisation=new SimpleLocalisation(this, bearing); 
		
		this.setStartSample((long) (timeSeconds*sR));
		this.setChannelBitmap(1);
		this.setUID(UID);
	}

	/**
	 * Comparison for ArrayList comparator. 
	 * @param a - click to compare
	 * @return the integer 
	 */
	public int compareTo(SimpleClick a) {
		
		if (this.timeSeconds>a.timeSeconds) return 1;
		if (this.timeSeconds<a.timeSeconds) return -1;
		if (this.timeSeconds==a.timeSeconds) return 0;
		
		return 0; 
	}
	
	@Override
	public double getAmplitudeDB() {
		return amplitude; 
	}
	
	@Override 
	public String toString() {
		return "Simple Click: time: " + timeSeconds + " amplitude: " +amplitude;
	}
	
	@Override
	public double[][] getWaveData() {
		return this.waveform;
	}
	
	
	@Override
	public AbstractLocalisation getLocalisation() {
		return simpleLocalisation;
	}

	/**
	 * Simple localisation class for the MHT localiser. 
	 * @author Jamie Macaulay. 
	 *
	 */
	private class SimpleLocalisation extends AbstractLocalisation {
		
		/**
		 * The bearing in RADIANS. 
		 */
		private double bearing=0; 

		/**
		 * Create a simple click loclaisation. 
		 * @param pamDataUnit - reference to the simple click
		 * @param bearing - the bearing in DEGREES
		 */
		public SimpleLocalisation(SimpleClick pamDataUnit, double bearing) {
			super(pamDataUnit, 0, 0);
			this.bearing=Math.toRadians(bearing); 
		}
		
		@Override
		public double[] getAngles() {
			return new double[] {bearing}; 
		}
		
	}


	@Override
	public RawDataTransforms getDataTransforms() {
		// TODO Auto-generated method stub
		return null;
	}


	

}
