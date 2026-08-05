package PamView.component;

import java.awt.Color;
import java.text.Format;

import javax.swing.JFormattedTextField;

import PamView.ColorManaged;
import PamView.PamColors;
import PamView.PamColors.PamColor;

public class PamFormattedTextField extends JFormattedTextField implements ColorManaged {

	/**
	 * 
	 */
	public PamFormattedTextField() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param formatter
	 */
	public PamFormattedTextField(AbstractFormatter formatter) {
		super(formatter);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param factory
	 * @param currentValue
	 */
	public PamFormattedTextField(AbstractFormatterFactory factory, Object currentValue) {
		super(factory, currentValue);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param factory
	 */
	public PamFormattedTextField(AbstractFormatterFactory factory) {
		super(factory);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param format
	 */
	public PamFormattedTextField(Format format) {
		super(format);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param value
	 */
	public PamFormattedTextField(Object value) {
		super(value);
		// TODO Auto-generated constructor stub
	}
	
	

	@Override
	public PamColor getColorId() {
		return PamColor.EDITCTRL;
	}


	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		Color fg = PamColors.getInstance().getColor(PamColor.AXIS);
		this.setForeground(fg);
		this.setCaretColor(fg);
	}

}
