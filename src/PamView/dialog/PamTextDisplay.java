package PamView.dialog;

import java.awt.Color;

import javax.swing.JTextField;
import javax.swing.text.Document;

import PamView.ColorManaged;
import PamView.PamColors;
import PamView.PamColors.PamColor;

public class PamTextDisplay extends JTextField implements ColorManaged {

	private static final long serialVersionUID = 1L;
	
	private Color fixedTextColour = null;

	public PamTextDisplay() {
		super();
		setup();
	}

	private void setup() {
		setEditable(false);
	}

	public PamTextDisplay(Document doc, String text, int columns) {
		super(doc, text, columns);
		setup();
	}

	public PamTextDisplay(int columns) {
		super(columns);
		setup();
	}

	public PamTextDisplay(String text, int columns) {
		super(text, columns);
		setup();
	}

	public PamTextDisplay(String text) {
		super(text);
		setup();
	}

	private PamColor defaultColor = PamColor.BORDER;
	
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
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#setForeground(java.awt.Color)
	 */
	@Override
	public void setForeground(Color fg) {
		super.setForeground(getTextColour(fg));
	}
	
	/**
	 * Gets a colour for the text. This function can be overridden quite easily to 
	 * generate bespoke behaviours for text colours. 
	 * @param foregroundColor
	 * @return text colour, which will use the foreground colour if the fixed text colour is null.
	 * 
	 */
	public Color getTextColour(Color foregroundColor) {
		Color fixedCol = getFixedTextColour();
		if (fixedCol == null) {
			return foregroundColor;
		}
		else {
			return fixedCol;
		}
	}

	/**
	 * @return the fixedTextColour
	 */
	public Color getFixedTextColour() {
		return fixedTextColour;
	}

	/**
	 * 
	 * Set a fixed text colour. If this is null, then text will 
	 * be set to the default colour scheme axis colour. 
	 * 
	 * @param fixedTextColour the fixedTextColour to set
	 */
	public void setFixedTextColour(Color fixedTextColour) {
		this.fixedTextColour = fixedTextColour;
	}
 
}
