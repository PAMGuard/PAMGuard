package annotationMark.spectrogram;

import java.util.List;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import annotation.DataAnnotationType;
import annotation.calcs.measurement.SoundMeasurementAnnotationType;
import annotation.handler.AnnotationChoiceHandler;
import annotation.handler.AnnotationChoices;
import annotationMark.MarkDataBlock;

public class SpectrogramMarkAnnotationHandler extends AnnotationChoiceHandler {

	private SpectrogramAnnotationModule spectrogramAnnotationModule;

	public SpectrogramMarkAnnotationHandler(SpectrogramAnnotationModule spectrogramAnnotationModule, PamDataBlock<PamDataUnit> pamDataBlock) {
		super(pamDataBlock);
		this.spectrogramAnnotationModule = spectrogramAnnotationModule;
	}

	@Override
	public AnnotationChoices getAnnotationChoices() {
		return spectrogramAnnotationModule.getAnnotationChoices();
	}

	/**
	 * There's an annotation type for every sound measurement, too many to list, so the
	 * user adds the ones they want to the list.
	 */
	@Override
	public boolean isEditableList() {
		return true;
	}

	/**
	 * Sound measurements aren't listed until the user adds them.
	 */
	@Override
	public boolean isDefaultListed(DataAnnotationType<?> annotationType) {
		return !(annotationType instanceof SoundMeasurementAnnotationType);
	}

	/* (non-Javadoc)
	 * @see annotation.handler.AnnotationHandler#getAvailableAnnotationTypes()
	 */
	@Override
	public List<DataAnnotationType<?>> getAvailableAnnotationTypes() {
		return super.getAvailableAnnotationTypes();
	}


}
