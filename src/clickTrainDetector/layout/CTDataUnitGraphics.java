package clickTrainDetector.layout;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

import Map.MapController;
import Map.MapPanel;
import Map.MapRectProjector;
import PamView.GeneralProjector;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickTrainDetector.CTDetectionGroupDataUnit;
import clickTrainDetector.dataselector.CTDataSelector;

/**
 * Swing based graphics for drawing click trains on the map. 
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class CTDataUnitGraphics extends PamDetectionOverlayGraphics {

	public static final SymbolData defSymbol = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, Color.RED, Color.RED);

	/**
	 * The current data unit.
	 */
	private CTDetectionGroupDataUnit ctDataUnit;

	public CTDataUnitGraphics(PamDataBlock parentDataBlock) {
		super(parentDataBlock, new PamSymbol(defSymbol));
	}

	/** 
	 * (non-Javadoc)
	 * @see PamView.PamDetectionOverlayGraphics#drawOnMap(java.awt.Graphics, PamguardMVC.PamDataUnit, PamView.GeneralProjector)
	 */
	@Override
	protected Rectangle drawOnMap(Graphics g, PamDataUnit pamDetection,
			GeneralProjector generalProjector) {

		this.ctDataUnit = (CTDetectionGroupDataUnit) pamDetection;

		MapController mapController  = ((MapRectProjector) generalProjector).getMapPanelRef().getMapController();

		CTDataSelector dataSel = (CTDataSelector) pamDetection.getParentDataBlock().
				getDataSelector(mapController.getUnitName(), false, MapPanel.DATASELECTNAME);
		
		if (dataSel != null) {
			//System.out.println("CTDataUnit: " + ctDataUnit + "  " + ctDataUnit.getLocalisation());
			if (ctDataUnit.getLocalisation()!=null &&
					dataSel.scoreData(ctDataUnit)>0) {
				//CTLocalisation ctLoc = (CTLocalisation) ctDataUnit.getLocalisation();
				if (ctDataUnit.getSummaryUnits()==null) {
					ctDataUnit.calcSummaryUnits(dataSel.getCTSelectParams());
				}
				
				ArrayList<PamDataUnit> summaryUnits = ctDataUnit.getSummaryUnits(); 
				if (summaryUnits!=null) {
					Rectangle rect = null; 
					for (int i=0; i<summaryUnits.size() ; i++) {
						rect = super.drawOnMap(g, summaryUnits.get(i), generalProjector);
					}
				}
			}
		}

		//now draw localisation symbols if they exist.,
		return super.drawOnMap(g, ctDataUnit, generalProjector);
	}


	/**
	 * Override this function so symbols (which are drawn from sub detection sometimes) are always from the super detection. 
	 * @param pamDataUnit
	 * @return PamSymbol to use in plotting. Generally this is just the 
	 * set symbol for the overlay, but can be overridden if a detector has
	 * some complicated way of using different symbols for different dataUnits. 
	 */
	@Override
	public PamSymbol getPamSymbol(PamDataUnit pamDataUnit, GeneralProjector projector) {
		PamSymbolChooser symbolChooser = projector.getPamSymbolChooser();
		if (symbolChooser == null) {
			return pamSymbol;
		}
		else if (ctDataUnit!=null) {
			return symbolChooser.getPamSymbol(projector, ctDataUnit);
		}
		else {
			return symbolChooser.getPamSymbol(projector, pamDataUnit);
		}
	}

}
