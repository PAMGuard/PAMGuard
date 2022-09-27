package clickTrainDetector.classification;

import java.io.Serializable;
import java.util.UUID;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;



/**
 * Classifier params class. 
 * @author Jamie Macaulay
 *
 */
public class CTClassifierParams implements Cloneable, Serializable, ManagedParameters {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public CTClassifierParams() {
		//this.uniqueID = UUID.randomUUID().toString();
		//System.out.println("Create classifier params: " + uniqueID); 
	}
	
	
	/**
	 * The name of the classifier. 
	 */
	public String classifierName = ""; 
	
	
	/**
	 * A unique ID for the classifier that never changes. This is important for accessing data selectors. 
	 */
	public String uniqueID =  UUID.randomUUID().toString();
	
	
	/**
	 * A very simple species flag to indicate what classifier was used. 0 means not classified. 
	 */
	public int speciesFlag = 1; 
	
	/**
	 * Easy way to know which classifier the parameter class belong to rather than big instance of statement
	 */
	public CTClassifierType type; 
	
	public CTClassifierParams clone() {
		try {
			CTClassifierParams clonedParams =(CTClassifierParams) super.clone();
			return clonedParams;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
