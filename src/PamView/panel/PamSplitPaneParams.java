package PamView.panel;

import java.io.Serializable;

public class PamSplitPaneParams implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;
	
	public Integer dividerLocation;

	@Override
	protected PamSplitPaneParams clone() {
		try {
			return (PamSplitPaneParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
