package PamView.dialog;

import java.awt.Color;
import java.awt.Cursor;

import javax.swing.JTextArea;
import javax.swing.text.Document;

import PamView.ColorManaged;
import PamView.PamColors;
import PamView.PamColors.PamColor;

public class PamTextArea extends JTextArea implements ColorManaged {

	public PamTextArea() {
		// TODO Auto-generated constructor stub
	}

	public PamTextArea(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	public PamTextArea(Document doc) {
		super(doc);
		// TODO Auto-generated constructor stub
	}

	public PamTextArea(int rows, int columns) {
		super(rows, columns);
		// TODO Auto-generated constructor stub
	}

	public PamTextArea(String text, int rows, int columns) {
		super(text, rows, columns);
		// TODO Auto-generated constructor stub
	}

	public PamTextArea(Document doc, String text, int rows, int columns) {
		super(doc, text, rows, columns);
		// TODO Auto-generated constructor stub
	}

	private PamColor defaultColor = PamColor.EDITCTRL;
	private Cursor cursor;
	
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
		cursor = this.getCursor();
		this.setForeground(PamColors.getInstance().getColor(PamColor.AXIS));
	}

}
