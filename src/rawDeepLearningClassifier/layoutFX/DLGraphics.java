package rawDeepLearningClassifier.layoutFX;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import PamUtils.Coordinate3d;
import PamView.GeneralProjector;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import Spectrogram.SpectrogramProjector;
import rawDeepLearningClassifier.dlClassification.DLDataUnit;

/**
 * The detection graphics for the DL localiser. Forces the graphics to be one colour and paints some 
 * translucent boxes on the spectrogram depending on the classification model result. 
 * @author Jamie Macaulay
 *
 */
public class DLGraphics extends PamDetectionOverlayGraphics {

	public Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);

	//	public Stroke dashed = new BasicStroke(4); 

	public Stroke normal = new BasicStroke(2); 

	public int alpha = 64; // 25% transparent

	public Color detColor = Color.CYAN; 

	private PamSymbol defaultSymbol = new PamSymbol(PamSymbolType.SYMBOL_DIAMOND, 10, 12, false,
			detColor, detColor ); 


	public DLGraphics(PamDataBlock parentDataBlock) {
		super(parentDataBlock,null);
		//		this.setDefaultSymbol(defaultSymbol);
		setLineColor(detColor); 
		setLocColour(detColor);
	}

	/**
	 * Override to forget all symbol chooser stuff. 
	 */
	public PamSymbol getPamSymbol(PamDataUnit pamDataUnit, GeneralProjector projector) {
		return this.defaultSymbol; 
	}


	@Override
	protected Rectangle drawOnSpectrogram(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		// draw a rectangle with time and frequency bounds of detection.
		// spectrogram projector is now updated to use Hz instead of bins. 
		DLDataUnit pamDetection = (DLDataUnit) pamDataUnit;	// originally cast pamDataUnit to PamDetection class


		double[] frequency = pamDetection.getFrequency();
		Coordinate3d topLeft = generalProjector.getCoord3d(pamDetection.getTimeMilliseconds(), 
				frequency[1], 0);
		Coordinate3d botRight = generalProjector.getCoord3d(pamDetection.getTimeMilliseconds() + 
				pamDetection.getSampleDuration() * 1000./getParentDataBlock().getSampleRate(),
				frequency[0], 0);

		if (botRight.x < topLeft.x){
			botRight.x = g.getClipBounds().width;
		}
		if (generalProjector.isViewer()) {
			Coordinate3d middle = new Coordinate3d();
			middle.x = (topLeft.x + botRight.x)/2;
			middle.y = (topLeft.y + botRight.y)/2;
			middle.z = (topLeft.z + botRight.z)/2;
			generalProjector.addHoverData(middle, pamDataUnit);
		}


		//creates a copy of the Graphics instance
		Graphics2D g2d = (Graphics2D) g.create();

		//		System.out.println("New OrcaSpotDatauNit draw: " + topLeft.x + "  " + botRight.x 
		//				+ " " + topLeft.y + "  " + botRight.y + " Frequency: " + frequency[0] + " " + frequency[1]); 

		//do not paint unles sit's passed binary classiifcation 
		if (pamDetection.getPredicitionResult().isBinaryClassification()) {
			//set the stroke of the copy, not the original 
			g2d.setStroke(normal);
		}
		else {
			g2d.setStroke(dashed);
		}

		g2d.setColor(detColor);

		g2d.drawRect((int) topLeft.x, (int) topLeft.y, 
				(int) botRight.x - (int) topLeft.x, (int) botRight.y - (int) topLeft.y);


		//		System.out.println("Is classification: " + pamDetection.getModelResult().isClassification()); 
		if (pamDetection.getPredicitionResult().isBinaryClassification()) {

			//alpha - higher numbers mean less opaque - means more see through
			//so want more opacity for higher predictions to highlight more
			//so low alpha means more opaque

			//set the alpha so that better results are more opaque 
			int alphaDet =  (int) ((1.0-PamUtils.PamArrayUtils.max(pamDetection.getPredicitionResult().getPrediction()))*alpha); 

			//			System.out.println("Alpha Det: " + alphaDet + "  " + pamDetection.getModelResult().getPrediction()); 
			Color detColorAlpha = new Color(detColor.getRed(), detColor.getGreen(), detColor.getBlue(), alphaDet);
			g2d.setColor(detColorAlpha);
			g2d.fillRect((int) topLeft.x, (int) topLeft.y, 
					(int) botRight.x - (int) topLeft.x, (int) botRight.y - (int) topLeft.y);

		}

		return new Rectangle((int) topLeft.x, (int) topLeft.y, 
				(int) botRight.x - (int) topLeft.x, (int) botRight.y - (int) topLeft.y);
	}

}
