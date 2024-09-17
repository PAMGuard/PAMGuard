package PamView;

import java.awt.Color;

import javax.swing.JList;
import javax.swing.ListModel;

import PamView.PamColors.PamColor;

public class PamList extends JList implements ColorManaged {

	public void PamList() {
		// TODO Auto-generated constructor stub
	}

	public PamList(ListModel model) {
		super(model);
		// TODO Auto-generated constructor stub
	}

	private PamColor defaultColor = PamColor.PlOTWINDOW;
//	private PamColor defaultColor = PamColor.BORDER;
	
	public PamColor getDefaultColor() {
		return defaultColor;
	}

	public void setDefaultColor(PamColor defaultColor) {
		this.defaultColor = defaultColor;
	}
	@Override
	public PamColor getColorId() {
		return defaultColor;
	}
	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		this.setForeground(PamColors.getInstance().getColor(PamColor.AXIS));
	}

}
