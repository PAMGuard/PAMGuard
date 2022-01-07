package PamView.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.ContainerListener;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class PamNorthPanel extends PamAlignmentPanel {

	private static final String alignment = BorderLayout.NORTH;
	
	/**
	 * @param isDoubleBuffered
	 * @param alignment
	 */
	public PamNorthPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered, alignment);
	}

	/**
	 * @param component
	 * @param alignment
	 * @param stealBorder
	 */
	public PamNorthPanel(JComponent component, boolean stealBorder) {
		super(component, alignment, stealBorder);
	}

	/**
	 * @param component
	 * @param alignment
	 */
	public PamNorthPanel(JComponent component) {
		super(component, alignment);
	}

	/**
	 * @param innerLayout
	 * @param alignment
	 */
	public PamNorthPanel(LayoutManager innerLayout) {
		super(innerLayout, alignment);
	}

	/**
	 * @param alignment
	 */
	public PamNorthPanel() {
		super(alignment);
	}

}
