package annotation.handler;

import java.util.List;

import PamController.SettingsNameProvider;
import PamguardMVC.PamDataBlock;
import annotation.DataAnnotationType;
import annotation.string.StringAnnotationType;
import annotation.userforms.UserFormAnnotationType;

public class ManualAnnotationHandler extends OneStopAnnotationHandler {

	public ManualAnnotationHandler(SettingsNameProvider settingsNameProvider, PamDataBlock dataBlock) {
		super(settingsNameProvider, dataBlock);
	}

	@Override
	public void createAnnotationTypes() {
		addAnnotationType(new StringAnnotationType("Text Annotation", 80));
		addAnnotationType(new UserFormAnnotationType(getPamDataBlock()));	
		// now try to add their parameters. 
		AnnotationChoices annotationChoices = getAnnotationChoices();
		if (annotationChoices == null) {
			return;
		}
		List<DataAnnotationType<?>> anTypes = getAvailableAnnotationTypes();
		for (DataAnnotationType annot : anTypes) {
			AnnotationOptions opts = annotationChoices.getAnnotationOptions(annot.getAnnotationName());
			if (opts != null) {
				annot.setAnnotationOptions(opts);
			}
		}
	}


}
