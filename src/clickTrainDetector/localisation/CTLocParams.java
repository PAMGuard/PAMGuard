package clickTrainDetector.localisation;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import annotation.localise.targetmotion.TMAnnotationOptions;

/**
 * Parameters for Click Train Localisation
 * @author Jamie Macaulay
 *
 */
public class CTLocParams extends TMAnnotationOptions implements ManagedParameters  {

	public CTLocParams() {
		super("ClickTrainTM");
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4L;
	
	
	/*****For target motion localisation****/
	
	
	/**
	 * True to attempt a loclaisation. 
	 */
	public boolean shouldloc = false; 
			
	/**
	 * The minimum number of data units within a detection before attempting a localisation
	 */
	public int minDataUnits = 20; 
	
	/**
	 * The minimum angle range before attempting a localisation
	 */
	public double minAngleRange =Math.toRadians(30); 

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}


}
