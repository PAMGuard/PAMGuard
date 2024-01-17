package loggerForms;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;

/**
 * Manage a bit of persistent data for a single Logger form description. <p>
 * i.e. the divider location. May get extended with other params later ...
 * @author Doug Gillespie
 *
 */
public class FormSettings implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	public Integer splitPanelPosition;

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected FormSettings clone() {
		try {
			return (FormSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}

}
