package clickDetector.offlineFuncs;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import PamDetection.AbstractLocalisation;
import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelector;
import clickDetector.ClickDetection;
import clickDetector.dataSelector.ClickTrainDataSelector;

public class OfflineEventGraphics extends PamDetectionOverlayGraphics {

	public static final SymbolData defSymbol = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, Color.RED, Color.RED);

	public OfflineEventGraphics(PamDataBlock parentDataBlock) {
		super(parentDataBlock, new PamSymbol(defSymbol));
	}

	/* (non-Javadoc)
	 * @see PamView.PamDetectionOverlayGraphics#drawOnMap(java.awt.Graphics, PamguardMVC.PamDataUnit, PamView.GeneralProjector)
	 */
	@Override
	protected Rectangle drawOnMap(Graphics g, PamDataUnit pamDetection,
			GeneralProjector generalProjector) {
		DataSelector ds = generalProjector.getDataSelector();
		if (ds != null && ClickTrainDataSelector.class.isAssignableFrom(ds.getClass())) {
			setDefaultRange(((ClickTrainDataSelector) ds).getCtSelectParams().defaultRange);
		}
		return super.drawOnMap(g, pamDetection, generalProjector);
	}

	/* (non-Javadoc)
	 * @see PamView.PamDetectionOverlayGraphics#drawDataUnit(java.awt.Graphics, PamguardMVC.PamDataUnit, PamView.GeneralProjector)
	 */
	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit, GeneralProjector generalProjector) {
		// see if the event has a localisation, if it doesn't, then draw the first click instead. 
		OfflineEventDataUnit oedu = (OfflineEventDataUnit) pamDataUnit;
		Color col = PamColors.getInstance().getWhaleColor(oedu.getColourIndex());
		setLineColour(col);
		setLocColour(col);
		if (hasLatLong(pamDataUnit)) {
			return super.drawDataUnit(g, pamDataUnit, generalProjector);
		}
		else {
			PamDataUnit firstSubDet = oedu.getSubDetection(0);
			if (firstSubDet != null) {
				return super.drawDataUnit(g, firstSubDet, generalProjector);
			}
		}
		return null;
	}

	private boolean hasLatLong(PamDataUnit dataUnit) {
		AbstractLocalisation loc = dataUnit.getLocalisation();
		if (loc == null) return false;
		return (loc.getLatLong(0) != null);
	}
}
