package annotationMark.spectrogram;

import java.io.Serializable;
import java.util.List;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import annotation.DataAnnotationType;
import annotation.handler.AnnotationChoices;

public class SpectrogramMarkParams implements Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	private AnnotationChoices annotationChoices;

	/**
	 * @return the annotationChoices
	 */
	public AnnotationChoices getAnnotationChoices(SpectrogramMarkAnnotationHandler annotationChoiceHandler) {
		if (annotationChoices == null) {
			annotationChoices = new AnnotationChoices();
			// by default, set everything to true
			List<DataAnnotationType<?>> totList = annotationChoiceHandler.getAvailableAnnotationTypes();
			for (DataAnnotationType aType:totList) {
				annotationChoices.setAnnotionOption(aType.getAnnotationName(), aType.getAnnotationOptions(), true);
			}
		}
		return annotationChoices;
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}

}
