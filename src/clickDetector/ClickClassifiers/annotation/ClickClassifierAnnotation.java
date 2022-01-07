package clickDetector.ClickClassifiers.annotation;

import annotation.DataAnnotation;

/**
 * Annotation for click classification 
 * 
 * @author Jamie Macaulay 
 *
 */
public class ClickClassifierAnnotation extends DataAnnotation<ClickClassificationType> {
	
	/**
	 * The classifiers that the click passed. e.g. [1 3 5] would mean that
	 * the click was classified by classifiers returning types 1 3 and 5. 
	 */
	private int[] clickClassificationTypes;


	public ClickClassifierAnnotation(ClickClassificationType clickClassificationType, int[] types) {
		super(clickClassificationType);
		this.clickClassificationTypes= types;
	}
	
	
	/***Constructors for different types of clicks classifier can go her ein the future***/

	/**
	 * 
	 * @param clickClassificationTypes the clickClassificationTypes to set
	 */
	public void setClickClassifierSet(int[] clickClassificationTypes) {
		this.clickClassificationTypes = clickClassificationTypes;
	}

	/**
	 * Get the classifier set
	 * @return the classifier set. 
	 */
	public int[] getClassiferSet() {
		return clickClassificationTypes;
	}
	
	@Override
	public String toString() {
		String results = "Click classifier set: "; 
		if (clickClassificationTypes!=null)
		for (int i=0; i<clickClassificationTypes.length; i++) {
			results += "  " + clickClassificationTypes[i]; 
		}
		else results+=" none";
			 
		return results;
	}
	

}
