package rawDeepLearningClassifier.dlClassification.delphinID;

import org.jamdev.jdl4pam.transforms.FreqTransform;
import org.jamdev.jpamutils.spectrogram.SpecTransform;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;

/**
 * Transform whistles to an image.
 * 
 * Here we extend the FreqTransform class because it contains lots of image
 * transforms that are useful once the whistles have been converted into an
 * image.
 * 
 * @author Jamie Macaulay
 *
 */
public class Whistles2Image extends FreqTransform {

	/**
	 * Create an image transform from a whistleGroup. 
	 * @param whistleGroup
	 * @param params
	 */
	public Whistles2Image(SegmenterDetectionGroup whistleGroup, Number[] params) {
		super(null, params);
		double[] freqLimits = 	new double[] {params[0].doubleValue(), params[1].doubleValue()};
		double[] size =			new double[] {params[2].doubleValue(), params[3].doubleValue()};
		
		SpecTransform specTransform = whistleGroupToImage( whistleGroup,  freqLimits, size);
		
		this.setSpecTransfrom(specTransform);
		this.setFreqlims(freqLimits);
		
	}
	

	/**
	 * Convert a group of whistles 
	 * @param whistleGroup - the whistle groups
	 * @param freqLimits - the frequency limits
	 * @return the spectrogram transform. 
	 */
	private SpecTransform whistleGroupToImage(SegmenterDetectionGroup whistleGroup, double[] freqLimits, double[] size) {

		SpecTransform specTransform = new SpecTransform(); 

		/*
		 * All time-frequency points are saved as a scatterplot with x-axis spanning 0-4
		 * seconds in time and y-axis spanning 0-20 kHz in frequency. - Matplotlib was
		 * used to produce plot (matplotlib.pyplot.scatter) (Point size set at 5 and all
		 * other values kept at default, including figure size which is saved as 6.4 x
		 * 4.8 inches as default, axes removed before saving using plt.axes(‘off’))
		 **/

		double[][] points = whistContours2Points(whistleGroup);

		Canvas canvas = makeScatterImage(points, size, new double[]{0, whistleGroup.getDurationInMilliseconds()}, freqLimits,  5.);

		WritableImage image = canvas.getGraphicsContext2D().getCanvas().snapshot(null, null);

		double[][] imaged = new double[(int) size[0]][(int) size[1]];

		Color color;
		for (int i=0; i<imaged.length; i++) {
			for (int j=0; j<imaged[0].length; j++) {
				color = image.getPixelReader().getColor(i, j);
				imaged[i][j] = color.getRed();
			}
		}
		
		specTransform.setSpecData(imaged);
		specTransform.setSampleRate((float) (freqLimits[1]*2)); 

		return specTransform;
	}

	/**
	 * Convert a list of whistle contours to a list of time and frequency points. 
	 * @param whistleGroup - list of whistle contours within a detection group. 
	 * @return an array with time (milliseconds from start of group) and frequency (Hz)
	 */
	private double[][] whistContours2Points(SegmenterDetectionGroup whistleGroup) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Create a scatter image from points
	 * @param points - the points are time (milliseconds from 0) and frequency
	 * @param size - the width and height of the image in pixels
	 * @param xlims - the minimum and maximum time in milliseconds from 0; 
	 * @param ylims - the minimum and maximum frequency in Hz
	 * @param markerSize - the marker size in pixels
	 * @return an image with y axis as frequency and x axis as time. 
	 */
	private Canvas makeScatterImage(double[][] points, double[] size, double[] xlims, double[] ylims, double markerSize) {

		Canvas canvas = new Canvas(size[0], size[1]);

		double x, y;
		for (int i=0; i<points.length; i++) {
			canvas.getGraphicsContext2D().setFill(Color.BLACK);

			//Calculate x and y in pixels. 
			x = ((points[i][0]-xlims[0])/(xlims[1]-xlims[0]))*size[0];
			y = ((points[i][0]-xlims[0])/(xlims[1]-xlims[0]))*size[0];

			canvas.getGraphicsContext2D().fillOval(x+markerSize/2, y-markerSize/2, markerSize/2, markerSize/2);
		}

		return canvas; 
	}




}
