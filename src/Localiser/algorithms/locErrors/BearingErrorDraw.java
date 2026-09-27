package Localiser.algorithms.locErrors;

import java.awt.Color;
import java.awt.Graphics;

import PamUtils.LatLong;
import PamView.GeneralProjector;
import PamView.TransformShape;
import PamguardMVC.PamDataUnit;

public class BearingErrorDraw implements LocErrorGraphics {
	
	private BearingError bearingError;

	public BearingErrorDraw(BearingError bearingError) {
		this.bearingError = bearingError;
	}

	@Override
	public TransformShape drawOnMap(Graphics g, PamDataUnit pamDetection, LatLong errorOrigin,
			GeneralProjector generalProjector, Color color) {
		// need to get the line length, then draw a semi transparent cone around it. 
		
		return null;
	}

}
