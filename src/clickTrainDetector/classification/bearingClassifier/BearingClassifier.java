package clickTrainDetector.classification.bearingClassifier;

import org.renjin.gcc.runtime.Debug;

import PamUtils.PamArrayUtils;
import clickTrainDetector.CTDataUnit;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.classification.CTClassification;
import clickTrainDetector.classification.CTClassifier;
import clickTrainDetector.classification.CTClassifierParams;
import clickTrainDetector.layout.classification.CTClassifierGraphics;
import clickTrainDetector.layout.classification.bearingClassifier.BearingClassifierGraphics;

/**
 * A bearing based classifier for click train. Probably most useful as a way to get rid of click train 
 * which are due to electrical and/or propeller noise. 
 * 
 * @author Jamie Macaulay
 *
 */
public class BearingClassifier implements CTClassifier {

	/**
	 * The click train control.
	 */
	@SuppressWarnings("unused")
	private ClickTrainControl clickTrainControl;

	/**
	 * The bearing classifier parameters. 
	 */
	private BearingClassifierParams bearingClssfrParams = new BearingClassifierParams();

	/**
	 * The bearing classiifer graphics
	 */
	private BearingClassifierGraphics bearingClassifierGraphics; 

	/**
	 * Constructor for the bearing classifier
	 */
	public BearingClassifier(ClickTrainControl clickTrainControl, int defaultSpeciesID) {
		this.clickTrainControl = clickTrainControl; 
		bearingClssfrParams.speciesFlag=defaultSpeciesID; 
		this.bearingClassifierGraphics = new BearingClassifierGraphics(clickTrainControl, this); 

	}

	/**
	 * Constructor for the bearing classifier
	 */
	public BearingClassifier(int defaultSpeciesID) {
		bearingClssfrParams.speciesFlag=defaultSpeciesID; 
		this.bearingClassifierGraphics = new BearingClassifierGraphics(clickTrainControl, this); 
	}

	@Override
	public CTClassification classifyClickTrain(CTDataUnit clickTrain) {

		//need to calculate mean, median, and standard deviation in bearing derivative. 

		if (clickTrain.getSubDetectionsCount()<3) {
			return new BearingClassification(CTClassifier.NOSPECIES, Double.NaN, Double.NaN, Double.NaN);
		}

		//is the click train within the limits?

		//calculate the bearing and time diff. 
		double[] bearingDiff = new double[clickTrain.getSubDetectionsCount()-1]; 
		double[] bearing = new double[clickTrain.getSubDetectionsCount()]; 
		double timeDiff;
		

		int nullcount = 0; 
		for (int i=0; i<clickTrain.getSubDetectionsCount()-1; i++) {
			
			if (clickTrain.getSubDetection(i).getLocalisation()==null) {
				nullcount++;
				continue; 
			}

			bearing[i] = clickTrain.getSubDetection(i).getLocalisation().getAngles()[0];

			bearingDiff[i]  = clickTrain.getSubDetection(i).getLocalisation().getAngles()[0]
					-clickTrain.getSubDetection(i+1).getLocalisation().getAngles()[0]; 

			//time diff is in seconds
			timeDiff = (clickTrain.getSubDetection(i+1).getTimeMilliseconds()
					-clickTrain.getSubDetection(i).getTimeMilliseconds())/1000.; 

			//derivative of bearing in radians per second.  
			bearingDiff[i] = bearingDiff[i]/timeDiff; 

		}	
		
		if (nullcount>clickTrain.getSubDetectionsCount()-4) {
			//less than three data units with loc results 
			Debug.println("The bearing classifier has a null count: "); 
			return new BearingClassification(CTClassifier.NOSPECIES, Double.NaN, Double.NaN, Double.NaN);
		}

		//add the last bearing to the array
		bearing[bearing.length-1] = clickTrain.getSubDetection(bearing.length-1).getLocalisation().getAngles()[0];

		double min = PamArrayUtils.min(bearing);
		double max = PamArrayUtils.max(bearing);
	

		//calculate the bearings
		double meanBearingD = PamArrayUtils.mean(bearingDiff);
		double medianBearingD = PamArrayUtils.median(bearingDiff);
		double stdBearingD = PamArrayUtils.std(bearingDiff);
		
		Debug.println("Bearing classifier: No. Detections: " + clickTrain.getSubDetectionsCount() + " medianBearing: " + medianBearingD);

		int speciesID = CTClassifier.NOSPECIES;
		boolean passed= true; 
		//now has to pass all the tests. 
		
		//is the minimum and maximum bearing in range...
		if ((min>=bearingClssfrParams.bearingLimMin && min<=bearingClssfrParams.bearingLimMax) ||
				(max>=bearingClssfrParams.bearingLimMin && max<=bearingClssfrParams.bearingLimMax)) {
			Debug.println("Passed on min max bearing");
		}
		else passed =false; 
		
		//mean bearing derivative
		if (bearingClssfrParams.useMean && meanBearingD>=bearingClssfrParams.minMeanBearingD 
				&& meanBearingD<=bearingClssfrParams.maxMeanBearingD) {
			Debug.println("Passed on mean bearing");
		}
		else if (bearingClssfrParams.useMean) passed=false; 
		
		//median bearing derivative
		Debug.println("Median Bearing: " + Math.toDegrees(medianBearingD) +
				"   minlim: " + Math.toDegrees(bearingClssfrParams.minMedianBearingD)+
				"   maxlim: " + Math.toDegrees(bearingClssfrParams.maxMedianBearingD)); 
		if (bearingClssfrParams.useMedian && medianBearingD>=bearingClssfrParams.minMedianBearingD 
				&& medianBearingD<=bearingClssfrParams.maxMedianBearingD) {
			Debug.println("Passed on median bearing");
		}
		else if (bearingClssfrParams.useMedian) passed = false; 
		
		//standard deviation derivative
		if (bearingClssfrParams.useStD && stdBearingD>=bearingClssfrParams.minStdBearingD 
				&& stdBearingD<=bearingClssfrParams.maxStdBearingD) {
			Debug.println("Passed on std bearing");
		}
		else if (bearingClssfrParams.useStD) passed= false; 

		if (passed) {
			speciesID = this.bearingClssfrParams.speciesFlag;
		}
		
		Debug.println("SPECIESID!! " + speciesID);


		return new BearingClassification(speciesID, meanBearingD, medianBearingD, stdBearingD);
	}




	@Override
	public String getName() {
		return "Bearing Classifier";
	}

	@Override
	public int getSpeciesID() {
		return bearingClssfrParams.speciesFlag;
	}

	@Override
	public CTClassifierGraphics getCTClassifierGraphics() {
		return bearingClassifierGraphics;
	}

	@Override
	public void setParams(CTClassifierParams ctClassifierParams) {
		this.bearingClssfrParams=(BearingClassifierParams) ctClassifierParams;
	}

	/**
	 * Bearing classifier parameters. 
	 * @return the bearing classifier parameters. 
	 */
	public BearingClassifierParams getParams() {
		return this.bearingClssfrParams;
	}

}
