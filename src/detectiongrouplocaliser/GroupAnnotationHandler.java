package detectiongrouplocaliser;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import annotation.DataAnnotationType;
import annotation.handler.AnnotationChoiceHandler;
import annotation.handler.AnnotationChoices;
import annotation.localise.targetmotion.TMAnnotationType;
import annotation.string.StringAnnotationType;
import annotation.userforms.UserFormAnnotationType;

public class GroupAnnotationHandler  extends AnnotationChoiceHandler {

	private DetectionGroupControl detectionGroupControl;

	public GroupAnnotationHandler(DetectionGroupControl detectionGroupControl, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.detectionGroupControl = detectionGroupControl;
		addAnnotationType(new StringAnnotationType("Text Annotation", 80));
		addAnnotationType(new UserFormAnnotationType());
		addAnnotationType(new TMAnnotationType());
	}

	@Override
	public AnnotationChoices getAnnotationChoices() {
		return detectionGroupControl.getDetectionGroupSettings().getAnnotationChoices();
	}

	/* (non-Javadoc)
	 * @see annotation.handler.AnnotationChoiceHandler#updateAnnotation(PamguardMVC.PamDataUnit, annotation.DataAnnotationType)
	 */
	@Override
	public boolean updateAnnotation(PamDataUnit pamDataUnit, DataAnnotationType annotationType) {
		boolean ans = super.updateAnnotation(pamDataUnit, annotationType);
		if (ans) {
			detectionGroupControl.notifyGroupDataChanged();
		}
		return ans;
	}

}
