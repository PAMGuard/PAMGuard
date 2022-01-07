package PamView.panel;

import java.awt.Dimension;

import javax.swing.BoundedRangeModel;
import javax.swing.JProgressBar;

import PamView.ColorManaged;
import PamView.PamColors;
import PamView.PamColors.PamColor;

public class PamProgressBar extends JProgressBar implements ColorManaged{
	
	private static final long serialVersionUID = 1L;
	
	private PamColor pamColor;

	{
		setStringPainted(true);
		setString("");
	}

	public PamProgressBar() {
		// TODO Auto-generated constructor stub
	}

	public PamProgressBar(PamColor pamColor) {
		this.pamColor = pamColor;
	}

	public PamProgressBar(int orient) {
		super(orient);
		// TODO Auto-generated constructor stub
	}

	public PamProgressBar(BoundedRangeModel newModel) {
		super(newModel);
		// TODO Auto-generated constructor stub
	}

	public PamProgressBar(int min, int max) {
		super(min, max);
		// TODO Auto-generated constructor stub
	}

	public PamProgressBar(int orient, int min, int max) {
		super(orient, min, max);
		// TODO Auto-generated constructor stub
	}

	public static PamColor defaultColor = PamColor.BORDER;
	
//	public static PamColor getDefaultColor() {
//		return defaultColor;
//	}
//
//	public statvoid setDefaultColor(PamColor defaultColor) {
//		this.defaultColor = defaultColor;
//	}

	@Override
	public PamColor getColorId() {
		return pamColor;
	}

//	@Override
//	public Dimension getMinimumSize() {
//		return super.getPreferredSize();
//	}
}
