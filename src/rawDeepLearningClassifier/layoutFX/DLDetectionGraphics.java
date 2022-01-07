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
import rawDeepLearningClassifier.dlClassification.DLDetection;

/**
 * Graphics for the detected data units
 * @author Jamie Macaulay 
 *
 */
public class DLDetectionGraphics extends PamDetectionOverlayGraphics {

	public Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);

	//	public Stroke dashed = new BasicStroke(4); 

	public Stroke normal = new BasicStroke(2); 

	public int alpha = 64; // 25% transparent

	public Color detColor = Color.GREEN; 

	private PamSymbol defaultSymbol = new PamSymbol(PamSymbolType.SYMBOL_DIAMOND, 10, 12, false,
			detColor, detColor ); 


	public DLDetectionGraphics(PamDataBlock parentDataBlock) {
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
		DLDetection pamDetection = (DLDetection) pamDataUnit;	// originally cast pamDataUnit to PamDetection class


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

		g2d.setStroke(normal);

		g2d.setColor(detColor);

		g2d.drawRect((int) topLeft.x, (int) topLeft.y, 
				(int) botRight.x - (int) topLeft.x, (int) botRight.y - (int) topLeft.y);



		//alpha - higher numbers mean less opaque - means more see through
		//so want more opacity for higher predictions to highlight more
		//so low alpha means more opaque

		//set the alpha so that better results are more opaque 
		int alphaDet = 155 ; //  (int) ((1.0-PamUtils.PamArrayUtils.max(pamDetection.getModelResults().get(0).getPrediction()))*alpha); 

		//			System.out.println("Alpha Det: " + alphaDet + "  " + pamDetection.getModelResult().getPrediction()); 
		Color detColorAlpha = new Color(detColor.getRed(), detColor.getGreen(), detColor.getBlue(), alphaDet);
		g2d.setColor(detColorAlpha);
		g2d.fillRect((int) topLeft.x, (int) topLeft.y, 
				(int) botRight.x - (int) topLeft.x, (int) botRight.y - (int) topLeft.y);


		return new Rectangle((int) topLeft.x, (int) topLeft.y, 
				(int) botRight.x - (int) topLeft.x, (int) botRight.y - (int) topLeft.y);
	}

}
