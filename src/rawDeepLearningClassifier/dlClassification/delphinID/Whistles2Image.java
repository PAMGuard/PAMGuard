package rawDeepLearningClassifier.dlClassification.delphinID;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.FreqTransform;
import org.jamdev.jpamutils.spectrogram.SpecTransform;

import rawDeepLearningClassifier.segmenter.SegmenterDetectionGroup;
import whistlesAndMoans.AbstractWhistleDataUnit;

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
	 * @param whistleGroup - the whistle group
	 * @param params - the paramters for whsilte image - min. freq., max. freq., width in pixels and height in pixels. 
	 */
	public Whistles2Image(SegmenterDetectionGroup whistleGroup, Whistle2ImageParams params) {
		super(null, null);
		//		double[] freqLimits = 	new double[] {params[0].doubleValue(), params[1].doubleValue()};
		//		double[] size =			new double[] {params[2].doubleValue(), params[3].doubleValue()};

		SpecTransform specTransform = whistleGroupToImage( whistleGroup,  params.freqLimits, params.size);

		this.setSpecTransfrom(specTransform);
		this.setFreqlims(params.freqLimits);

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

		ArrayList<double[][]> points = whistContours2Points(whistleGroup);

		//does not work becaue it has to be on the AWT thread. 
		BufferedImage canvas = makeScatterImage(points, size, new double[]{0, whistleGroup.getDurationInMilliseconds()}, freqLimits,  5.);

		double[][] imaged = new double[(int) size[0]][(int) size[1]];

		int color;
		for (int i=0; i<imaged.length; i++) {
			for (int j=0; j<imaged[0].length; j++) {
				color = canvas.getRGB(i, j);
				imaged[i][j] = color;
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
	private ArrayList<double[][]> whistContours2Points(SegmenterDetectionGroup whistleGroup) {

		ArrayList<double[][]> contours = new ArrayList<double[][]>();

		AbstractWhistleDataUnit whistleContour;

		long segStart = whistleGroup.getTimeMilliseconds();

		for (int i=0; i<whistleGroup.getSubDetectionsCount(); i++) {

			whistleContour = (AbstractWhistleDataUnit) whistleGroup.getSubDetection(i);

			double[][] contourD = new double[whistleContour.getSliceCount()][2];
			for (int j=0; j<whistleContour.getSliceCount(); j++) {
				contourD[j][0] = (segStart - whistleContour.getTimeMilliseconds())/1000. + whistleContour.getTimesInSeconds()[i];
				contourD[j][1] = whistleContour.getFreqsHz()[j];
			}
			contours.add(contourD);
		}

		return contours;
	}

//	/**
//	 * Create a scatter image from points
//	 * @param points - list of time frequency points - the points are time (milliseconds from 0) and frequency
//	 * @param size - the width and height of the image in pixels
//	 * @param xlims - the minimum and maximum time in milliseconds from 0; 
//	 * @param ylims - the minimum and maximum frequency in Hz
//	 * @param markerSize - the marker size in pixels
//	 * @return an image with y axis as frequency and x axis as time. 
//	 */
//	private Canvas makeScatterImage(ArrayList<double[][]> points, double[] size, double[] xlims, double[] ylims, double markerSize) {
//
//		Canvas canvas = new Canvas(size[0], size[1]);
//
//		double x, y;
//		for (int j=0; j<points.size(); j++) {
//
//			for (int i=0; i<points.get(j).length; i++) {
//				canvas.getGraphicsContext2D().setFill(Color.BLACK);
//
//				//Calculate x and y in pixels. 
//				x = ((points.get(j)[i][0]-xlims[0])/(xlims[1]-xlims[0]))*size[0];
//				y = ((points.get(j)[i][0]-xlims[0])/(xlims[1]-xlims[0]))*size[0];
//
//				canvas.getGraphicsContext2D().fillOval(x+markerSize/2, y-markerSize/2, markerSize/2, markerSize/2);
//			}
//		}
//
//		return canvas; 
//	}
	
	/**
	 * Create a scatter image from points
	 * @param points - list of time frequency points - the points are time (milliseconds from 0) and frequency
	 * @param size - the width and height of the image in pixels
	 * @param xlims - the minimum and maximum time in milliseconds from 0; 
	 * @param ylims - the minimum and maximum frequency in Hz
	 * @param markerSize - the marker size in pixels
	 * @return an image with y axis as frequency and x axis as time. 
	 */
	private BufferedImage makeScatterImage(ArrayList<double[][]> points, double[] size, double[] xlims, double[] ylims, double markerSize) {

		BufferedImage canvas = new BufferedImage((int) size[0], (int) size[1], BufferedImage.TYPE_INT_RGB);

		double x, y;
		for (int j=0; j<points.size(); j++) {

			for (int i=0; i<points.get(j).length; i++) {
				canvas.getGraphics().setColor(Color.BLACK);

				//Calculate x and y in pixels. 
				x = ((points.get(j)[i][0]-xlims[0])/(xlims[1]-xlims[0]))*size[0];
				y = ((points.get(j)[i][0]-xlims[0])/(xlims[1]-xlims[0]))*size[0];

				canvas.getGraphics().fillOval((int) (x+markerSize/2),(int) (y-markerSize/2), (int) markerSize,(int) markerSize);
			}
		}

		return canvas; 
	}

	public static class Whistle2ImageParams {

		/**
		 * The frequency limitis in 
		 */
		public double[] freqLimits;

		public double[] size;	

	}



}
