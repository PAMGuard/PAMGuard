package annotation.calcs.snr;

import PamModel.parametermanager.PamParameterSet;
import annotation.handler.AnnotationOptions;

public class SNRAnnotationOptions extends AnnotationOptions {

	private static final long serialVersionUID = 1L;
	
	private SNRAnnotationParameters snrAnnotationParameters = new SNRAnnotationParameters();

	public SNRAnnotationOptions(String annotationName) {
		super(annotationName);
	}

	/**
	 * @return the snrAnnotationParameters
	 */
	public SNRAnnotationParameters getSnrAnnotationParameters() {
		return snrAnnotationParameters;
	}

	/**
	 * @param snrAnnotationParameters the snrAnnotationParameters to set
	 */
	public void setSnrAnnotationParameters(SNRAnnotationParameters snrAnnotationParameters) {
		this.snrAnnotationParameters = snrAnnotationParameters;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}


}
