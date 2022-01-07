package loggerForms.dataselect;

import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import loggerForms.FormDescription;

public class FormDataSelCreator extends DataSelectorCreator {

	private FormDescription formDescription;

	public FormDataSelCreator(PamDataBlock pamDataBlock, FormDescription formDescription) {
		super(pamDataBlock);
		this.formDescription = formDescription;
	}

	@Override
	public FormDataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType) {
		return new FormDataSelector(getPamDataBlock(), formDescription, selectorName, allowScores);
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		return new FormDataSelParams();
	}

	@Override
	public String getUnitName() {
		if (formDescription != null) {
			return formDescription.getFormName();
		}
		else {
			return "Unknown logger form";
		}
	}
}
