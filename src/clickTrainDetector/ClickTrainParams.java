package clickTrainDetector;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamUtils.PamUtils;
import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.classification.simplechi2classifier.Chi2ThresholdParams;
import clickTrainDetector.localisation.CTLocParams;

/**
 * Settings for the click train detector. Algorithm specific settings are stored for each click train
 * algorithm.
 * 
 * @author Jamie Macaulay 
 *
 */
public class ClickTrainParams implements Serializable, Cloneable, ManagedParameters {
	
	/**
	 * 
	 */
	public static final long serialVersionUID = 8L;

	/**
	 * The selected type of click train detector. 
	 */
	public int ctDetectorType=0;
	
	/**
	 * The data source name. 
	 */
	public String dataSourceName = null; 
	
	/**
	 * The index of the data source. 
	 */
	public int dataSourceIndex=0;
	
	/**
	 * The channel groups to be used in the click train detector.
	 */
	public int[] channelGroups=new int[] {1}; //channel 0 selected...might be problamtic but will see. 
	
	/***Click Train Classification**/
	
	/**
	 * Run the click train classifier. 
	 */
	public boolean runClassifier = false; 
	
	/**
	 * A simple initial classifier which junks click trains below a certain length and
	 * above a certain chi2 value. This ensure that not all clicks are saved to the 
	 * database which would take up a lot of storage and possibly destabilise PAMGuard. 
	 * It also ensure that enough click trains are saved so that species classifiers can 
	 * be tweaked.
	 */
	public Chi2ThresholdParams simpleCTClassifier = new Chi2ThresholdParams(); 
	
	/**
	 * A list of classification parameters for each classifier. 
	 */
	public CTClassifierParams[] ctClassifierParams = null; 
	
	/**
	 * Localisation parameters for the click train localisation. (target motion and summary bearing info)
	 */
	public CTLocParams ctLocParams = new CTLocParams();

	/**
	 * True to use the data selector to pre filter clicks 
	 */
	public boolean useDataSelector = false; 

	/**
	 * Make a single channel map out of channel groups bitmaps. 
	 * @return the channel map containing all grouped channels. 
	 */
	public int getChannelMap() {
		int channelMap=1; //32 bit channel map with first channel selected
		if (channelGroups==null) return channelMap;
		int[] channels;
		for (int i=0; i<channelGroups.length; i++) {
			//iterate through all groups
			channels=PamUtils.getChannelArray(channelGroups[i]); 
			for (int j=0; j<channels.length; j++) {
				//add group 
				PamUtils.SetBit(channelMap, channels[j], 1); 
			}
		}
		return channelMap;
	}
	
	@Override
	public String toString() {
		//TODO need to fill this out properly. 
		String out = "Click Train Parameters \n"; 
		if (ctClassifierParams==null) out+="null";
		else{
			for (int i =0; i<ctClassifierParams.length; i++){
				out +=ctClassifierParams[i].type + "\n"; 
			}
		}
		return out;
	}
	
	@Override
	protected ClickTrainParams clone() {
		try {
			ClickTrainParams clonedParams =(ClickTrainParams) super.clone();
			if (clonedParams.ctClassifierParams!=null) {
				clonedParams.ctClassifierParams = new CTClassifierParams[ctClassifierParams.length]; 
				for (int i=0; i<clonedParams.ctClassifierParams.length; i++) {
					clonedParams.ctClassifierParams[i] =  ctClassifierParams[i].clone(); 
				}
			}
			
			return clonedParams;
			
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}


}
