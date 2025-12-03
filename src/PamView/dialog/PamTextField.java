package PamView.dialog;

import java.awt.Color;

import javax.swing.JTextField;
import javax.swing.text.Document;

import PamView.ColorManaged;
import PamView.PamColors;
import PamView.PamColors.PamColor;

public class PamTextField extends JTextField implements ColorManaged {

	private static final long serialVersionUID = 1L;

	public PamTextField() {
	}

	public PamTextField(String text) {
		super(text);
	}


	public PamTextField(int columns) {
		super(columns);
	}
	
	public PamTextField(String text, int columns) {
		super(text, columns);
	}

	public PamTextField(Document doc, String text, int columns) {
		super(doc, text, columns);
	}
/**
 * Now a load of versions with option to NOT use the colouring so these can be used in some standard controls. 
 */

	public PamTextField(boolean colourManaged) {
		setDefaultColor(colourManaged ? defaultColor : null);
	}

	public PamTextField(String text, boolean colourManaged) {
		super(text);
		setDefaultColor(colourManaged ? defaultColor : null);
	}


	public PamTextField(int columns, boolean colourManaged) {
		super(columns);
		setDefaultColor(colourManaged ? defaultColor : null);
	}
	
	public PamTextField(String text, int columns, boolean colourManaged) {
		super(text, columns);
		setDefaultColor(colourManaged ? defaultColor : null);
	}

	public PamTextField(Document doc, String text, int columns, boolean colourManaged) {
		super(doc, text, columns);
		setDefaultColor(colourManaged ? defaultColor : null);
	}

	/*
	 * **********************************
	 */
	public static final PamColor defaultColor = PamColor.EDITCTRL;
	private PamColor selectedColor = defaultColor;
	
	public PamColor getDefaultColor() {
		return selectedColor;
	}

	public void setDefaultColor(PamColor defaultColor) {
		this.selectedColor = defaultColor;
	}
	@Override
	public PamColor getColorId() {
		return selectedColor;
	}
	
	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		this.setForeground(PamColors.getInstance().getColor(PamColor.AXIS));
	}

}
