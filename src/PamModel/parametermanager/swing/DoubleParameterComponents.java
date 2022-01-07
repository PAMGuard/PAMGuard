package PamModel.parametermanager.swing;

import PamModel.parametermanager.PamParameterData;

public class DoubleParameterComponents extends NumericParameterComponents {

	public DoubleParameterComponents(PamParameterData pamParameterData, String textFormat) {
		super(pamParameterData, textFormat);
	}


	@Override
	public boolean getField() {
		try {
			Double data = Double.valueOf(getDataField().getText());
			getPamParameterData().setData(data);
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}

}
