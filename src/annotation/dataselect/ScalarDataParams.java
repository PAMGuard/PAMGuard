package annotation.dataselect;

import java.io.Serializable;

import PamguardMVC.dataSelector.DataSelectParams;

public class ScalarDataParams extends DataSelectParams implements Cloneable, Serializable {

	public static final long serialVersionUID = 1L;
	
	public double minValue;
	
	public double maxValue;

	@Override
	protected ScalarDataParams clone() {
		try {
			return (ScalarDataParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
