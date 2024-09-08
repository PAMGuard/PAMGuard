package mapgrouplocaliser;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import annotation.handler.AnnotationChoiceHandler;
import annotation.handler.AnnotationChoices;
import annotation.string.StringAnnotationType;
import annotation.userforms.UserFormAnnotationType;

public class MapGroupAnnotationHandler extends AnnotationChoiceHandler {

	private MapGroupLocaliserControl mapGroupLocaliserControl;

	public MapGroupAnnotationHandler(MapGroupLocaliserControl mapGroupLocaliserControl, PamDataBlock<PamDataUnit> pamDataBlock) {
		super(pamDataBlock);
		this.mapGroupLocaliserControl = mapGroupLocaliserControl;
		addAnnotationType(new StringAnnotationType("Text Annotation", 80));
		addAnnotationType(new UserFormAnnotationType());
	}

	@Override
	public AnnotationChoices getAnnotationChoices() {
		return mapGroupLocaliserControl.getMapGrouperSettings().getAnnotationChoices();
	}
	

}
