package rawDeepLearningClassifier.dataPlotFX;

import javafx.scene.paint.Color;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * The default colours used for deep learning classes.
 * <p>
 * Deep learning classes appear in more than one display, e.g. the prediction
 * lines in the prediction plot ({@link DLPredictionPlotInfoFX}) and the
 * detection symbols coloured by class ({@link DLSymbolModifier}). All of these
 * should start off with the same colour for the same class so that a user does
 * not see, for example, a blue prediction line but a red detection symbol for
 * the same class.
 * <p>
 * The colours are mid tone and saturated so that they are visible on both a
 * white background and a grey spectrogram - pale colours (e.g. yellow) and very
 * dark colours are therefore avoided.
 *
 * @author Jamie Macaulay
 */
public class DLClassColours {

	/**
	 * The base colours for the classes. If there are more classes than colours
	 * then the colours are recycled but darkened/brightened so that neighbouring
	 * repeats can still be told apart.
	 */
	private static final Color[] BASE_COLOURS = new Color[] {
			Color.rgb(214, 39, 40),		//red
			Color.rgb(31, 119, 180),	//blue
			Color.rgb(44, 160, 44),		//green
			Color.rgb(255, 127, 14),	//orange
			Color.rgb(148, 103, 189),	//purple
			Color.rgb(23, 162, 184),	//teal
			Color.rgb(227, 26, 158),	//magenta
			Color.rgb(140, 86, 75),		//brown
			Color.rgb(184, 134, 11),	//dark gold
			Color.rgb(0, 105, 92)		//dark green/teal
	};

	/**
	 * Get the default colour for a class.
	 *
	 * @param classIndex - the index of the class.
	 * @return the default colour for the class.
	 */
	public static Color getClassColour(int classIndex) {
		int index = Math.abs(classIndex);
		Color colour = BASE_COLOURS[index % BASE_COLOURS.length];

		//if there are more classes than colours then alter the recycled colours a
		//little so that they are not identical.
		int cycle = index / BASE_COLOURS.length;
		for (int i = 0; i < cycle; i++) {
			colour = (i % 2 == 0) ? colour.darker() : colour.brighter();
		}

		return colour;
	}

	/**
	 * Get the default colour for a class as an RGB packed integer.
	 *
	 * @param classIndex - the index of the class.
	 * @return the default colour for the class.
	 */
	public static int getClassColourInt(int classIndex) {
		return PamUtilsFX.colorToInt(getClassColour(classIndex));
	}

	/**
	 * Get the number of unique colours in the palette.
	 *
	 * @return the number of unique colours.
	 */
	public static int getNumColours() {
		return BASE_COLOURS.length;
	}

}
