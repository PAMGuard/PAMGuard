package PamView.panel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import PamView.ColorManaged;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.dialog.PamGridBagContraints;

public class PamPanel extends JPanel implements ColorManaged {

	/*
	 * preferred colours which override the standard night day colours. 
	 */
	private Color[] preferredBorderColours = new Color[2];
	
	public PamPanel() {
		super();
		setBackground(PamColors.getInstance().getColor(PamColor.BORDER));
		setForeground(PamColors.getInstance().getForegroudColor(PamColor.BORDER));
	}

	public PamPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		setBackground(PamColors.getInstance().getColor(PamColor.BORDER));
		setForeground(PamColors.getInstance().getForegroudColor(PamColor.BORDER));
	}

	public PamPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		setBackground(PamColors.getInstance().getColor(PamColor.BORDER));
		setForeground(PamColors.getInstance().getForegroudColor(PamColor.BORDER));
	}

	public PamPanel(LayoutManager layout) {
		super(layout);
		setBackground(PamColors.getInstance().getColor(PamColor.BORDER));
		setForeground(PamColors.getInstance().getForegroudColor(PamColor.BORDER));
	}

	public PamPanel(PamColor defaultColor) {
		super();
		setDefaultColor(defaultColor);
	}

	private PamColor defaultColor = PamColor.BORDER;
	
	public PamColor getDefaultColor() {
		return defaultColor;
	}

	public void setDefaultColor(PamColor defaultColor) {
		PamColors.getInstance().setColor(this, defaultColor);
		this.defaultColor = defaultColor;
	}

	@Override
	public PamColor getColorId() {
		return defaultColor;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#setBackground(java.awt.Color)
	 */
	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		Border border = getBorder();
		if (border != null && TitledBorder.class.isAssignableFrom(border.getClass())) {
			/*
			 * Work out if this is the night colour or the day colour, then consider overriding
			 * this colour selection. 
			 */
//			boolean isNight = PamColors.getInstance().getColorSettings().nightTime;
//			Color overColour = preferredBorderColours[isNight ? 1 : 0];
			Color lineColour = PamColors.getInstance().getColor(PamColor.TITLEBORDER);
			Color textColour = PamColors.getInstance().getColor(PamColor.AXIS);
			TitledBorder tb = (TitledBorder) border;
			tb.setTitleColor(textColour);
			tb.setBorder(new LineBorder(lineColour));
//			tb.setTitleColor(overColour);
//			if (overColour != null) {
//				tb.setTitleColor(overColour);
//				tb.setBorder(new LineBorder(overColour));
//			}
//			else if (isNight) {
//				tb.setTitleColor(PamColors.getInstance().getColor(PamColor.AXIS));
//				tb.setBorder(new LineBorder(PamColors.getInstance().getColor(PamColor.AXIS)));
//			}
//			else {
//				// just create a new default border. 
//				setBorder(new TitledBorder(tb.getTitle()));
//			}
		}
	}
	
	public static void addComponent(JPanel panel, Component p, GridBagConstraints constraints){
		((GridBagLayout) panel.getLayout()).setConstraints(p, constraints);
		panel.add(p);
	}
	
	/**
	 * lays out a grid of textfields with labels being their names
	 * @param panel panel to add to
	 * @param tfs
	 */
	public static void layoutGrid(JPanel panel, JComponent[] tfs){
		GridBagConstraints gc = new PamGridBagContraints();
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridy = GridBagConstraints.RELATIVE;
		
//		gc.fill=GridBagConstraints.HORIZONTAL;
//		gc.ipadx = 7;
//		gc.ipady = 2;
//		gc.insets = new Insets(1, 1, 1, 1);
		
		gc.gridx = 0;
		for (JComponent tf:tfs){
			JLabel l;
			String name = tf.getName();
			if (name == null) {
				name = " ";
			}
			if (name.charAt(name.length()-1) != ' ') {
				name += " ";
			}
			addComponent(panel,l= new JLabel(tf.getName(), JLabel.RIGHT), gc);
			l.setToolTipText(tf.getToolTipText());
		}
		gc.gridx=1;
		for (JComponent tf:tfs){
			addComponent(panel, tf, gc);
		}
		
	}
	
	public JFrame getFrame(int maxTries){
		Container parent=this;
		int i=0;
		while(i<maxTries){
			parent=parent.getParent();
			try{
				JFrame frame = (JFrame) parent;
				return frame;
			}catch(ClassCastException e){
				
			}
		}
		return null;
	}

	/**
	 * @return the preferredBorderColours
	 */
	public Color[] getPreferredBorderColours() {
		return preferredBorderColours;
	}

//	/**
//	 * Override the preferred (black and red) colours for any border included in 
//	 * in the panel. 
//	 * @param nightOrDay 0 = day colour; 1 = night colour
//	 * @param preferredBorderColour the preferredBorderColour to set
//	 */
//	public void setPreferredBorderColour(Color preferredBorderColour, int nightOrDay) {
//		if (nightOrDay < 0 || nightOrDay >= this.preferredBorderColours.length) {
//			return;
//		}
//		this.preferredBorderColours[nightOrDay] = preferredBorderColour;
//		setBackground(getBackground());
//	}
	
	
}
