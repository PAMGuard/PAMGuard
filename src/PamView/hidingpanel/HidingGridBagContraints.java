package PamView.hidingpanel;

import java.awt.Insets;

import PamView.dialog.PamGridBagContraints;

public class HidingGridBagContraints extends PamGridBagContraints {

	private static final long serialVersionUID = 1L;

	public HidingGridBagContraints() {
		super();
		insets = new Insets(0,0,1,0);
	}

}
