package clickTrainDetector.clickTrainAlgorithms.mht.test;

import java.util.ArrayList;

import PamguardMVC.PamDataBlock;
import clickTrainDetector.clickTrainAlgorithms.mht.mhtMAT.SimpleClick;

/**
 * Simple data block for clicks
 * @author Jamie Macaulay
 */
public class SimpleClickDataBlock extends PamDataBlock<SimpleClick> {

	/**
	 * The default samplerate. 
	 */
	public static float defaultSR = 500000; 
	
	/**
	 * The sample rate to use. 
	 */
	public float sR=defaultSR;
	
	public SimpleClickDataBlock() {
		super(SimpleClick.class, "Simple Clicks", null, 1);
		this.setSampleRate(sR, false);
	}

	/**
	 * Add a list of simple clicks to the data block. 
	 * @param simpleClicks - simple clicks to add. 
	 */
	public void addPamData(ArrayList<SimpleClick> simpleClicks) {
		for (int i=0; i<simpleClicks.size(); i++) {
			this.addPamData(simpleClicks.get(i));
		}
	}
	
	@Override 
	public float getSampleRate() {
		return sR; 
	}
	
	@Override 
	public void setSampleRate(float sR, boolean notify) {
		 this.sR=sR; 
	}

}
