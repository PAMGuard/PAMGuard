package Localiser.algorithms.locErrors;

import java.awt.Color;
import java.awt.Graphics;
import PamUtils.LatLong;
import PamView.TransformShape;
import PamView.GeneralProjector;
import PamguardMVC.PamDataUnit;

/**
 * Class for drawing localisation errors.
 * @author Jamie Macaulay
 *
 */
public interface LocErrorGraphics  {
	
	public TransformShape drawOnMap(Graphics g, PamDataUnit pamDetection, LatLong errorOrigin, GeneralProjector generalProjector, Color ellipseColor);

}
