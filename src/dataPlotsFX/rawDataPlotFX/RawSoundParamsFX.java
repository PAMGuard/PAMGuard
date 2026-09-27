package dataPlotsFX.rawDataPlotFX;

import java.io.Serializable;

import javafx.scene.paint.Color;
import pamViewFX.fxNodes.PamColorsFX;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * Display settings for the raw sound (waveform) plot on a tdGraphFX.
 *
 * @author Jamie Macaulay
 */
public class RawSoundParamsFX implements Serializable, Cloneable {

	public static final long serialVersionUID = 1L;

	/**
	 * The index into the standard PAMGuard colours ({@link PamColorsFX#getWhaleColor(int)})
	 * to draw the waveform in, or -1 to use {@link #lineColour} directly.
	 */
	public int lineColourIndex = 1;

	/**
	 * The colour to draw the waveform in as a packed RGB integer. Only used when
	 * {@link #lineColourIndex} is -1. Colours are held as an int since
	 * {@link Color} is not serializable.
	 */
	public int lineColour = 0x0000FF;

	/**
	 * True to colour each plot with the standard PAMGuard colour for its channel
	 * rather than using a single colour for every channel.
	 */
	public boolean colourByChannel = false;

	/**
	 * Get the colour to draw the waveform for a channel in.
	 *
	 * @param channel - the channel number.
	 * @return the line colour.
	 */
	public Color getLineColour(int channel) {
		if (colourByChannel) {
			return PamColorsFX.getInstance().getChannelColor(channel);
		}
		if (lineColourIndex >= 0) {
			return PamColorsFX.getInstance().getWhaleColor(lineColourIndex);
		}
		return PamUtilsFX.intToColor(lineColour);
	}

	@Override
	public RawSoundParamsFX clone() {
		try {
			return (RawSoundParamsFX) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

}
