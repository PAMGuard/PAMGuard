package qa.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import PamUtils.Coordinate3d;
import PamUtils.LatLong;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PanelOverlayDraw;
import PamguardMVC.PamDataUnit;
import javafx.geometry.Point3D;
import qa.QASequenceDataBlock;
import qa.QASequenceDataUnit;

public class SequenceOverlayDraw extends PanelOverlayDraw {

	private static PamSymbol defaultSymbol = new PamSymbol(PamSymbolType.SYMBOL_DIAMOND, 10, 10, true, Color.CYAN, Color.BLACK);
	
	public SequenceOverlayDraw() {
		super(defaultSymbol);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		QASequenceDataUnit seqDataunit = (QASequenceDataUnit) pamDataUnit;
		LatLong pos = seqDataunit.getSoundSequence().getSourceLocation();
		Coordinate3d pt = generalProjector.getCoord3d(pos);
		generalProjector.addHoverData(pt, pamDataUnit);
		return getPamSymbol(pamDataUnit, generalProjector).draw(g, pt.getXYPoint());
	}

	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		if (parameterTypes[0] == ParameterType.LATITUDE
				&& parameterTypes[1] == ParameterType.LONGITUDE) {
			return true;
		}
		return false;
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector, int keyType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector, PamDataUnit dataUnit, int iSide) {
		// TODO Auto-generated method stub
		return null;
	}


}
