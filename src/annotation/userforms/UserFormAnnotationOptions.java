package annotation.userforms;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import annotation.handler.AnnotationOptions;

public class UserFormAnnotationOptions extends AnnotationOptions implements Serializable, ManagedParameters {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8028334325731182581L;
	
	private String udfFormName;	

	public UserFormAnnotationOptions(String annotationName) {
		super(annotationName);
	}

	/**
	 * @return the udfFormName
	 */
	public String getUdfFormName() {
		return udfFormName;
	}

	/**
	 * @param udfFormName the udfFormName to set
	 */
	public void setUdfFormName(String udfFormName) {
		this.udfFormName = udfFormName;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}

}
