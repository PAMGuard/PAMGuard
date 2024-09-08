package Map;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class MapCommentOverlayGraphics extends PamDetectionOverlayGraphics {
	
	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_SQUARE, 12, 12, true, Color.BLACK, Color.pink);

	public MapCommentOverlayGraphics(PamDataBlock parentDataBlock) {
		super(parentDataBlock, new PamSymbol(defaultSymbol));
//		if (getPamSymbol() == null) {
//			setPamSymbol(new PamSymbol(defaultSymbol));
//		}
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {	
		if (parameterTypes == null || parameterTypes.length < 2) {
			return false;
		}
		if (parameterTypes[0] == ParameterType.LATITUDE
			&& parameterTypes[1] == ParameterType.LONGITUDE) {
			return true;
		}
		return false;
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		// simply draw symbol at designated latLong
		LatLong ll = ((MapComment) pamDataUnit).latLong;
		Coordinate3d c3d = generalProjector.getCoord3d(ll.getLatitude(), ll.getLongitude(), 0);
		generalProjector.addHoverData(c3d, pamDataUnit);
		return getPamSymbol(pamDataUnit, generalProjector).draw(g, c3d.getXYPoint());
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector,
			PamDataUnit dataUnit, int side) {
		MapComment mapComment = (MapComment) dataUnit;
		String str =  "<html> Map Comment";
		str += String.format("<br>%s", PamCalendar.formatDateTime(dataUnit.getTimeMilliseconds()));
		str += String.format("<br>%s,  %s", mapComment.latLong.formatLatitude(), mapComment.latLong.formatLongitude());
		str += String.format("<br>%s", mapComment.comment);
		str += "</html>";
		return str;
	}


}
