package difar;

import java.util.List;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import annotation.DataAnnotationType;
import annotation.handler.AnnotationChoiceHandler;
import annotation.handler.AnnotationChoices;

public class SonobuoyAnnotationHandler  extends AnnotationChoiceHandler {

	private SonobuoyManager buoyControl;
	
//	AnnotationChoices annotationChoices = new AnnotationChoices();
	
	public SonobuoyAnnotationHandler(SonobuoyManager sonobuoyControl, PamDataBlock<PamDataUnit> pamDataBlock) {
		super(pamDataBlock);
		this.buoyControl = sonobuoyControl;
	}

	@Override
	public AnnotationChoices getAnnotationChoices() {
		AnnotationChoices annotationChoices = new AnnotationChoices();
		// by default, set everything to true
		List<DataAnnotationType<?>> totList = getAvailableAnnotationTypes();
		for (DataAnnotationType aType:totList) {
			annotationChoices.setAnnotionOption(aType.getAnnotationName(), aType.getAnnotationOptions(), true);
		}
		return annotationChoices;
	}
	
}
