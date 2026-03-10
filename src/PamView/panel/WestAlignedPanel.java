package PamView.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

import PamView.ColorManaged;
import PamView.PamColors.PamColor;

/**
 * Panel to west align components within other components. 
 * Will automatically use or not use PAMcolours to apply 
 * to the blank space in the panel.  
 * @author dg50
 *
 */
public class WestAlignedPanel extends PamAlignmentPanel implements ColorManaged {


	/**
	 * 
	 */
	public WestAlignedPanel() {
		super(BorderLayout.WEST);
	}
	

	public WestAlignedPanel(JComponent component) {
		this(component, false);
	}
	
	public WestAlignedPanel(JComponent component, boolean stealBorder) {
		super(component, BorderLayout.WEST);
	}


	/**
	 * @param innerLayout
	 * @param alignment
	 */
	public WestAlignedPanel(LayoutManager innerLayout) {
		super(innerLayout, BorderLayout.WEST);
		// TODO Auto-generated constructor stub
	}
	
	
}
