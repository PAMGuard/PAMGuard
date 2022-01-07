package IshmaelLocator;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamView.GeneralProjector;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
/**
 * Process for choosing a symbol to represent IshLocalizations on the
 * map (and elsewhere).
 * @author Dave Mellinger
 */
public class IshOverlayGraphics extends PamDetectionOverlayGraphics {

	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_DIAMOND, 5, 5, true, Color.red, Color.red);

	public IshOverlayGraphics(PamDataBlock parentDataBlock) {
		super(parentDataBlock, new PamSymbol(defaultSymbol));
		// TODO Auto-generated constructor stub
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		// TODO Auto-generated method stub
		return super.drawDataUnit(g, pamDataUnit, generalProjector);
	}
	
	@Override
	protected Rectangle drawOnMap(Graphics g, PamDataUnit pamDetection,
			GeneralProjector generalProjector) {
		Graphics2D g2d = (Graphics2D) g;
		PamSymbol dataSymbol = getPamSymbol(pamDetection, generalProjector);
		Color col = dataSymbol.getLineColor();
		
		LatLong ll = pamDetection.getLocalisation().getLatLong(0);
		Coordinate3d crossPoint = generalProjector.getCoord3d(ll.getLatitude(), ll.getLongitude(), 0);
		
		dataSymbol.draw(g2d, crossPoint.getXYPoint());
				
		return null;
		
	}
	
	
}
