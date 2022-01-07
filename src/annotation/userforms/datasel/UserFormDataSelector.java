package annotation.userforms.datasel;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import annotation.dataselect.AnnotationDataSelector;
import annotation.userforms.UserFormAnnotation;
import loggerForms.dataselect.FormDataSelector;
import loggerForms.monitor.FormsDataSelectorCreator;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

/**
 * This is a wrapper around a standard forms data selector. Needed because annotations hold data in a different
 * way to a normal FormsDataUnit, so mustn't call the standard scoreData fundtion in the formsDataSelector. 
 * @author Dougl
 *
 */
public class UserFormDataSelector extends AnnotationDataSelector {
	

	private FormDataSelector formsDataSelector;

	public UserFormDataSelector(DataAnnotationType annotationType, PamDataBlock pamDataBlock, String selectorName,
			boolean allowScores, FormDataSelector formsDataSelector) {
		super(annotationType, pamDataBlock, selectorName, allowScores);
		this.formsDataSelector = formsDataSelector;
	}

	@Override
	protected double scoreData(PamDataUnit pamDataUnit, DataAnnotation annotation) {
		UserFormAnnotation userFormAnnotation = (UserFormAnnotation) annotation;
		if (annotation == null) {
			return 0;
		}
		Object[] loggerData = userFormAnnotation.getLoggerFormData();
		return formsDataSelector.scoreData(pamDataUnit, loggerData);
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		formsDataSelector.setParams(dataSelectParams);
	}

	@Override
	public DataSelectParams getParams() {
		return formsDataSelector.getParams();
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		return formsDataSelector.getDialogPanel();
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		return formsDataSelector.getDialogPaneFX();
	}

	@Override
	public String getSelectorTitle() {
		return formsDataSelector.getSelectorTitle();
	}

}
