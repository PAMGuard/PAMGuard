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
	 * Show the annotation dialog automatically whenever a new annotation is marked
	 * out on a display. Boolean rather than boolean so that settings saved before
	 * this option existed deserialise to null and get the default behaviour (dialog
	 * shown) rather than a silent false.
	 */
	private Boolean showDialogOnNewMark = true;

	/**
	 * @return true if the annotation dialog should open automatically when a new
	 *         annotation is marked out.
	 */
	public boolean isShowDialogOnNewMark() {
		return showDialogOnNewMark == null ? true : showDialogOnNewMark;
	}

	/**
	 * @param showDialogOnNewMark true to open the annotation dialog automatically
	 *                            when a new annotation is marked out.
	 */
	public void setShowDialogOnNewMark(boolean showDialogOnNewMark) {
		this.showDialogOnNewMark = showDialogOnNewMark;
	}

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
