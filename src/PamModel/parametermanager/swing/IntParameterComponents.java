package PamModel.parametermanager.swing;

import PamModel.parametermanager.PamParameterData;

public class IntParameterComponents extends NumericParameterComponents {

	public IntParameterComponents(PamParameterData pamParameterData, String textFormat) {
		super(pamParameterData, textFormat);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean getField() {
		try {
			Integer data = Integer.valueOf(getDataField().getText());
			getPamParameterData().setData(data);
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}

}
