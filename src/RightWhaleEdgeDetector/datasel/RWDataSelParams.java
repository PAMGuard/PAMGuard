package RightWhaleEdgeDetector.datasel;

import java.io.Serializable;

import PamguardMVC.dataSelector.DataSelectParams;

public class RWDataSelParams extends DataSelectParams implements Cloneable, Serializable {

	public static final long serialVersionUID = 1L;
	
	public int minType = 6;

	@Override
	protected RWDataSelParams clone() {
		try {
			return (RWDataSelParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
