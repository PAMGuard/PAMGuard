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
//		this.uniqueID = UUID.randomUUID().toString(); // see comment below 
	}
	
	
	/**
	 * The name of the classifier. 
	 */
	public String classifierName = ""; 
	
	
//	/**
//	 * A unique ID for the classifier that never changes. This is important for accessing data selectors. 
//	 * GET RID OF THIS, Every time you make a new set of params you get a new ID and it then creates a new 
//	 * classifier selector with default settings, so the classifier selectors never work. 
//	Classifier selectors need to be names on something that's not oging to change, so the module name and the classifier species ame. 
//	 */
//	public String uniqueID; 
	
	
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
