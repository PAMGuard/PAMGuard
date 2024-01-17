package loggerForms.monitor;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Hashtable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamguardMVC.dataSelector.DataSelectParams;

public class FormsSelectorParams extends DataSelectParams implements Cloneable, Serializable, ManagedParameters {
	
	public static final long serialVersionUID = 1L;
	
	private Hashtable<String, Boolean> formSelection;

	public FormsSelectorParams() {
		formSelection = new Hashtable<>();
	}
	
	public boolean isFormSelected(String formName) {
		Boolean isSel = formSelection.get(formName);
		return isSel == null ? false : isSel;
	}

	public void setFormSelected(String formName, boolean selected) {
		formSelection.put(formName, selected);
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("formSelection");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return formSelection;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}


}
