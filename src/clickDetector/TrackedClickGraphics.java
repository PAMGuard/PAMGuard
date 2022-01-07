package clickDetector;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import Localiser.detectionGroupLocaliser.GroupDetection;
import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickDetector.offlineFuncs.OfflineEventDataUnit;

public class TrackedClickGraphics extends PamDetectionOverlayGraphics {

	public TrackedClickGraphics(PamDataBlock parentDataBlock) {
		super(parentDataBlock, new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 20, 20, false, Color.BLUE, Color.BLUE));
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		GroupDetection gd = (GroupDetection) pamDataUnit;
//		System.out.println("TrackedClickGraphics: "+gd.getSubDetectionsCount());
//		System.out.println("TrackedClickGraphics: "+gd.getLocalisation().getLocError(0));
		Color col = PamColors.getInstance().getWhaleColor(gd.getEventId());
		setLineColour(col);
		setLocColour(col);
		/*
		 * See if the 
		 */
		return super.drawDataUnit(g, pamDataUnit, generalProjector);
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		String str =  super.getHoverText(generalProjector, dataUnit, iSide);
		
		// strip off the last /html bit
		str = str.substring(0, str.length()-7);
		OfflineEventDataUnit gd = (OfflineEventDataUnit) dataUnit;
//		str += "<html>";
		str += String.format("<br>Database Id %d", gd.getDatabaseIndex());
		int updateOf = gd.getDatabaseUpdateOf();
		if (updateOf > 0) {
			str += String.format(" (updates Id %d)", updateOf);
		}
		str += "</html>";
		
		return str;
	}

}
