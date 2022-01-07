package annotation.string;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import annotation.handler.AnnotationOptions;

public class StringAnnotationOptions extends AnnotationOptions implements Serializable, ManagedParameters {

	private static final long serialVersionUID = 1L;

	private int maxLength;
	
	public StringAnnotationOptions(String annotationName) {
		super(annotationName);
	}

	/**
	 * @return the maxLength
	 */
	public int getMaxLength() {
		return maxLength;
	}

	/**
	 * @param maxLength the maxLength to set
	 */
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}


}
