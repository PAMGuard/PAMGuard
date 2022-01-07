package PamView.dialog;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.Icon;

import PamView.panel.PamBorderPanel;

public class IconPanel extends PamBorderPanel {
	
	private Icon icon;
	private int nRows = 1;
	private int nCols = 1;

	public IconPanel(Icon icon) {
		super();
		this.icon = icon;
		setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
	}
	
	public IconPanel(int nRows, int nCols) {
		setLayout(new GridLayout(nRows, nCols));
	}
	
	public void addIconToGrid(Icon icon) {
		add(new IconPanel(icon));
	}

	@Override
	protected void paintComponent(Graphics g) {
		// TODO Auto-generated method stub
		if (icon != null) {
			icon.paintIcon(this, g, 0, 0);
		}
	}
 
	
}
