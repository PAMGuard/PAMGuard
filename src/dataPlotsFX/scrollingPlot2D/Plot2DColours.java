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

	/**
	 * Get the colour for a specified dB level as a non-premultiplied ARGB integer,
	 * i.e. in the form wanted by {@link javafx.scene.image.PixelWriter#setPixels}.
	 * <p>
	 * Spectrogram images are built a whole column at a time, and writing a column
	 * with one {@code setPixels} call is a good deal quicker than a
	 * {@code setColor} per pixel. Implementations backed by a fixed colour map
	 * should override this to return a value from a pre-converted table, so that no
	 * {@link Color} to integer conversion is done per pixel at all.
	 * 
	 * @param dBLevel - the dB level in dB re 1Pa/Hz
	 * @return the colour for the dB level as 0xAARRGGBB.
	 */
	public default int getColoursARGB(double dBLevel) {
		return toARGB(getColours(dBLevel));
	}

	/**
	 * Convert a colour to a non-premultiplied ARGB integer, matching the conversion
	 * {@link javafx.scene.image.PixelWriter#setColor} does internally.
	 * 
	 * @param colour - the colour to convert.
	 * @return the colour as 0xAARRGGBB.
	 */
	public static int toARGB(Color colour) {
		return ((int) Math.round(colour.getOpacity() * 255) << 24)
				| ((int) Math.round(colour.getRed() * 255) << 16)
				| ((int) Math.round(colour.getGreen() * 255) << 8)
				| ((int) Math.round(colour.getBlue() * 255));
	}

}
