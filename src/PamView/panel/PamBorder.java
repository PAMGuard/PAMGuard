package PamView.panel;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 * Class defining standard borders for PAMGaurd displays. 
 * @author Doug Gillespie
 *
 */
public class PamBorder {

	/**
	 * Create a standard bevel for all inner plot windows in PAMGuard
	 * This is currently a lowered bevel border, but as this goes
	 * out of fashion, we may change it to something planer. 
	 * @return a border for standardised plot windows. 
	 */
	public static Border createInnerBorder() {
//		return new BevelBorder(BevelBorder.LOWERED);
		return BorderFactory.createLineBorder(Color.GRAY);
	}
}
