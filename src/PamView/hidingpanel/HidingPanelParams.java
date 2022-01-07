package PamView.hidingpanel;

import java.io.Serializable;

public class HidingPanelParams implements Cloneable, Serializable{

	public static final long serialVersionUID = 1L;
	
	public Boolean isExpanded = true;

	@Override
	protected HidingPanelParams clone() {
		try {
			return (HidingPanelParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
