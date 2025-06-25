package rawDeepLearningClassifier.layoutFX.dlTransfroms;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.utilsFX.ColourArray;

/**
 * Draws the image of a spectrogram on a canvas. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class SpectrogramImage {

	/**
	 * The spectrogram. 
	 */
	private double[][] spectrogram;

	/**
	 * The colour array type
	 */
	private ColourArray colourArray;

	/**
	 * The colour limits 
	 */
	private double[] clims;

	/**
	 * True to use a log plot
	 */
	private boolean log;

	/**
	 * Constructor for a spectrogram image. 
	 * @param spectrogram - the spectrogram to make an image of 
	 * @param colourArrayType - the colour map to use for the spectrogram 
	 * @param clims - the colour limits for the spectrogram.
	 */
	public SpectrogramImage(double[][] spectrogram, ColourArray colourArray, double[] clims) {
		this.spectrogram = spectrogram;
		this.colourArray=colourArray;
		this.clims=clims; 
		this.log = true; //use a log plot
	}

	/**
	 * Constructor for a spectrogram image. 
	 * @param spectrogram - the spectrogram to make an image of 
	 * @param colourArrayType - the colour map to use for the spectrogram 
	 * @param clims - the colour limits for the spectrogram.
	 * @parma log - true to plot as a log. 
	 */
	public SpectrogramImage(double[][] spectrogram, ColourArray colourArray, double[] clims, boolean log) {
		this.spectrogram = spectrogram;
		this.colourArray=colourArray;
		this.clims=clims; 
		this.log = log; //use a log plot
	}


	/**
	 * Create a an image of the spectrogram by applying a colour gradient to the surface data.
	 * @param spectrogram - the spectrogram to draw
	 * @param colourArray - the colour array to use for the colour gradient. 
	 * @param clims - the colour limits to apply. 
	 */
	public WritableImage writeImageData(double[][] data, ColourArray colourArray, double[] clims) {

		//		double[][] data = spectrogram.getAbsoluteSpectrogramData(); 
		if (data == null || data.length == 0 || data[0].length == 0) {
			return null; //no data to draw
		}
		WritableImage specImage = new WritableImage(data.length, data[0].length); 

		PixelWriter writer = specImage.getPixelWriter(); 

		for (int i=0; i<data.length; i++) {
			for (int j=0; j<data[0].length; j++) {
				if (log)
					writer.setColor(i, data[0].length-1-j, calcColour(20*Math.log10(data[i][j])));
				else 
					writer.setColor(i, data[0].length-1-j, calcColour(data[i][j])); 

			}
		}

		return specImage; 
	}


	/**
	 * Create a an image of the spectrogram by applying a colour gradient to the surface data.
	 * @return spectrogram image which is the same size in pixels as it is in data length
	 */
	public WritableImage getRawSpecImage() {
		return writeImageData(spectrogram,  colourArray,  clims);
	}



	/**
	 * Calculate the colour which corresponds to a point on the spectrogram. 
	 * @param d - the spectrogram surface point to calculate the colour for. 
	 * @return the colour of the spectrogram at d. 
	 */
	private Color calcColour(double d) {
		double colPerc = (d-clims[0])/(clims[1]-clims[0]); 
		//System.out.println("ColPerc: " + colPerc);
		return colourArray.getColour(colPerc);
	}


//	/**
//	 * Scaled the writable images.
//	 * @return the scaled image.
//	 */
//	public Image getSpecImage(int width, int height) {
//		return UtilsFX.scale(writeImageData(spectrogram, colourArray, clims) , width, height, false);
//	}

}