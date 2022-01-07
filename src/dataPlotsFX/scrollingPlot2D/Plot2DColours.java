package dataPlotsFX.scrollingPlot2D;

import javafx.scene.paint.Color;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;

/**
 * Define colours for spectrogram
 * 
 * @author Jamie Macaulay
 *
 */
public interface Plot2DColours {

	/**
	 * Get the wrap colour for the spectrogram. This is the colour of the line which
	 * shows the current time location in wrap mode and should contrast with the
	 * spectrogram if possible (obviously that's a bit hard when using a
	 * multicoloured colour scheme!)
	 * 
	 * @return the wrap colour.
	 */
	public Color getWrapColor();

	/**
	 * Get the colour for a specified dB level.
	 * 
	 * @param dBLevel - the dB level in dB re 1Pa/Hz
	 * @return the colour for the dB level
	 */
	public Color getColours(double dBLevel);

}
