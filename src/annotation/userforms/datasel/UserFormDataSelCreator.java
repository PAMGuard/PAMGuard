package annotation.userforms.datasel;

import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import annotation.DataAnnotationType;
import annotation.dataselect.AnnotationDataSelCreator;
import annotation.userforms.UserFormAnnotationType;
import loggerForms.FormDescription;
import loggerForms.dataselect.FormDataSelCreator;
import loggerForms.dataselect.FormDataSelector;
import loggerForms.monitor.FormsDataSelectorCreator;

/**
 * User form data selector for use with annotations. Not normal selectors. 
 * @author dg50
 *
 */
public class UserFormDataSelCreator extends AnnotationDataSelCreator {

	private FormDataSelCreator formsDataSelectorCreator;
	private UserFormAnnotationType userFormAnnotationType;
	private FormDescription formDescription;
	
	public UserFormDataSelCreator(UserFormAnnotationType userFormAnnotationType, boolean allowScores) {
		super(userFormAnnotationType);
		this.userFormAnnotationType = userFormAnnotationType;
	}

	public void configureFormsDataSelector(FormDescription fd) {
		this.formDescription = fd;
		if (fd != null) {
			formsDataSelectorCreator = new FormDataSelCreator(fd.getFormsDataBlock(), formDescription);
		}
		else {
			formsDataSelectorCreator = null;
		}
		// clear any existing ones, so that all are recreated. 
		clearDataSelectors();
	}
	
	@Override
	public DataSelector createDataSelector(DataAnnotationType dataAnnotationType, String selectorName,
			boolean allowScores, String selectorType) {
		if (formsDataSelectorCreator == null) {
			return null;
		}
		FormDataSelector formsDataSelector = formsDataSelectorCreator.createDataSelector(selectorName, allowScores, selectorType);
		return new UserFormDataSelector(userFormAnnotationType, null, selectorName, allowScores, formsDataSelector);
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		if (formsDataSelectorCreator == null) {
			return null;
		}
		return formsDataSelectorCreator.createNewParams(name);
	}

	@Override
	public String getUnitName() {
		if (formDescription == null) {
			return "Unknown Logger Form";
		}
		return formDescription.getFormName();
	}
}
