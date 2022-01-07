package annotation.handler;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;

/**
 * Annotation options. Does nothing but used as a base 
 * class for forming a list of annotations. 
 * @author dg50
 *
 */
public class AnnotationOptions implements Serializable, ManagedParameters {

	private static final long serialVersionUID = 1L;

	private String annotationName;

	private boolean isSelected;

	/**
	 * 
	 */
	public AnnotationOptions(AnnotationOptions otherOptions) {
		super();
		if (otherOptions != null) {
			this.annotationName = otherOptions.annotationName;
			this.isSelected = otherOptions.isSelected;
		}
	}

	/**
	 * @param annotationClassName
	 */
	public AnnotationOptions(String annotationName) {
		super();
		this.annotationName = annotationName;
	}

	/**
	 * @return the annotationClassName
	 */
	public String getAnnotationName() {
		return annotationName;
	}

	/**
	 * @return the isSelected boolean
	 */
	public boolean isIsSelected() {
		return isSelected;
	}

	/**
	 * @param isSelected the isSelected to set
	 */
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}


}
