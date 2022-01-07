package PamView.panel;

import java.awt.FlowLayout;

/**
 * Flow layout with a standard gap for consisten appearance througout PAMGUARD
 * @author Doug Gillespie
 *
 */
public class PamFlowLayout extends FlowLayout {
	
	private int standardHGap = 4;

	public PamFlowLayout() {
		super();
		setHgap(standardHGap);
	}

	public PamFlowLayout(int align) {
		super(align);
		setHgap(standardHGap);
	}

	public PamFlowLayout(int align, int hgap, int vgap) {
		super(align, hgap, vgap);
	}

}
