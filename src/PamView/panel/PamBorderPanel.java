package PamView.panel;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import PamView.PamColors;
import PamView.PamColors.PamColor;

/**
 * Class primarily aimed at making little panels for the
 * side panel
 * @author Doug Gillespie
 *@see SoundRecorder.RecorderSidePanel
 */
public class PamBorderPanel extends PamPanel {

	public PamBorderPanel() {
		super(PamColor.BORDER);
		setBackground(PamColors.getInstance().getColor(PamColor.BORDER));
	}

	public PamBorderPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamBorderPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	public PamBorderPanel(LayoutManager layout) {
		super(layout);
//		PamColors.getInstance().registerComponent(this, PamColor.BORDER);
	}

	@Override
	public PamColor getColorId() {
		return PamColor.BORDER;
	}
	
	public static void addComponent(JPanel panel, Component p, GridBagConstraints constraints){
		((GridBagLayout) panel.getLayout()).setConstraints(p, constraints);
		panel.add(p);
	}
	
	private Border border;

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#setBorder(javax.swing.border.Border)
	 */
	@Override
	public void setBorder(Border border) {
		super.setBorder(border);
		this.border = border;
	}

//	/* (non-Javadoc)
//	 * @see javax.swing.JComponent#setBackground(java.awt.Color)
//	 */
//	@Override
//	public void setBackground(Color bg) {
//		super.setBackground(bg);
//		if (border != null && TitledBorder.class.isAssignableFrom(border.getClass())) {
//			TitledBorder tb = (TitledBorder) border;
//			boolean isNight = PamColors.getInstance().getColorSettings().nightTime;
//			if (isNight) {
//				tb.setTitleColor(PamColors.getInstance().getColor(PamColor.AXIS));
//				tb.setBorder(new LineBorder(PamColors.getInstance().getColor(PamColor.AXIS)));
//			}
//			else {
//				// just create a new default border. 
//				setBorder(new TitledBorder(tb.getTitle()));
//			}
//		}
//	}
	
	

}
