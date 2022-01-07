package qa.generator;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamView.GeneralProjector;
import PamView.HoverData;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class GeneratorOverlayGraphics extends PamDetectionOverlayGraphics {
	
	public static PamSymbol defaultSymbol = new PamSymbol(PamSymbolType.SYMBOL_SQUARE, 10, 10, true, Color.RED, Color.BLACK);

	public GeneratorOverlayGraphics(PamDataBlock parentDataBlock) {
		super(parentDataBlock, defaultSymbol);
	}

	/* (non-Javadoc)
	 * @see PamView.PamDetectionOverlayGraphics#canDrawOnMap()
	 */
	@Override
	protected boolean canDrawOnMap() {
		return true;
	}

//	/* (non-Javadoc)
//	 * @see PamView.PamDetectionOverlayGraphics#drawOnMap(java.awt.Graphics, PamguardMVC.PamDataUnit, PamView.GeneralProjector)
//	 */
//	@Override
//	protected Rectangle drawOnMap(Graphics g, PamDataUnit pamDetection, GeneralProjector generalProjector) {
//		
//		QATestDataUnit genDataUnit = (QATestDataUnit) pamDetection;
//		LatLong latLong = genDataUnit.getSoundGenerator().getLatLong();
//		Coordinate3d pt3d = generalProjector.getCoord3d(latLong);
//		
//		PamSymbol symb = getPamSymbol(genDataUnit, generalProjector);
//		if (symb == null) {
//			symb = defaultSymbol;
//		}
//		generalProjector.addHoverData(pt3d, genDataUnit);
//		return symb.draw(g, pt3d.getXYPoint());
//	}
//
//	/* (non-Javadoc)
//	 * @see PamView.PamDetectionOverlayGraphics#getHoverText(PamView.GeneralProjector, PamguardMVC.PamDataUnit, int)
//	 */
//	@Override
//	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
//		QATestDataUnit genDataUnit = (QATestDataUnit) dataUnit;
//		QASoundGenerator generator = genDataUnit.getSoundGenerator();
//		LatLong latLong = generator.getLatLong();
//		
//		return String.format("<html>%s<br>%s</html>", generator.getName(), latLong.toString());
//	}

}
