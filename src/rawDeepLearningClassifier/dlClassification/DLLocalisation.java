package rawDeepLearningClassifier.dlClassification;

import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamguardMVC.PamDataUnit;
import bearinglocaliser.annotation.BearingAnnotation;

/**
 * The localisation for a DL data unit. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DLLocalisation extends AbstractLocalisation {

	private double[] angles;

	public DLLocalisation(PamDataUnit pamDataUnit, int locContents, int referenceHydrophones) {
		super(pamDataUnit, locContents, referenceHydrophones);
		// TODO Auto-generated constructor stub
	}

	public void setBearing(BearingAnnotation bearingAnnotation) {
		this.setLocContents(bearingAnnotation.getBearingLocalisation().getLocContents());
//		this.getLocContents().removeLocContent(LocContents.HAS_AMBIGUITY);
		
		this.angles = bearingAnnotation.getBearingLocalisation().getAngles(); 
		this.setSubArrayType(bearingAnnotation.getBearingLocalisation().getSubArrayType()); 
		
		//System.out.println("Loc content!: " + this.getLocContents().hasLocContent(LocContents.HAS_AMBIGUITY) + " angles: " + angles.length); 
		//PamUtils.PamArrayUtils.printArray(angles);
	}
	
	@Override
	public double[] getAngles() {
		return angles;
	}
	
}
