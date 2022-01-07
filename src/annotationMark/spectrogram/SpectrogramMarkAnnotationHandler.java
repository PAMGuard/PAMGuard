package annotationMark.spectrogram;

import java.util.List;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import annotation.DataAnnotationType;
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

	/* (non-Javadoc)
	 * @see annotation.handler.AnnotationHandler#getAvailableAnnotationTypes()
	 */
	@Override
	public List<DataAnnotationType<?>> getAvailableAnnotationTypes() {
		return super.getAvailableAnnotationTypes();
	}


}
