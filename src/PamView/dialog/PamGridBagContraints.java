package PamView.dialog;

import java.awt.GridBagConstraints;
import java.awt.Insets;

/**
 * Standard grid bag contraints to use as a default in 
 * dialogs. 
 * 
 * @author Douglas Gillespie
 *
 */
public class PamGridBagContraints extends GridBagConstraints {

	private static final long serialVersionUID = 1L;
	public static final int emptyBorderLeft = 2;
	public static final int emptyBorderRight = 2;
	public static final int emptyBorderTop = 2;
	public static final int emptyBorderBottom = 2;
	
	public static Insets getEmptyBorderInsets() {
		return new Insets(emptyBorderTop, emptyBorderLeft, emptyBorderBottom, emptyBorderRight);
	}
	
	/**
	 * Construct a standard gridbagconstaints suitable for most PAM dialog layouts.
	 */
	public PamGridBagContraints() {
		this(new Insets(2,2,2,2), 2, 2);
	}
	
	/**
	 * Construct a GridBagConstraints with a bit more control over insets and padding. 
	 * @param insets insets, can be null
	 * @param padX ipadx
	 * @param padY ipady
	 */
	public PamGridBagContraints(Insets insets, int padX, int padY) {
		super();
		gridx = gridy = 0;
		fill = HORIZONTAL;
		anchor = WEST;
		this.ipadx = padX;
		this.ipady = padY;
		this.insets =insets;
		if (insets == null) {
			this.insets = new Insets(0,0,0,0);
		}
	}

}
