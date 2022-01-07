package difar.plots;

import java.awt.Color;

import dataPlots.data.TDSymbolChooser;
import difar.DifarDataUnit;
import Array.PamArray;
import PamView.PamColors;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PamColors.PamColor;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Set the symbols for DIFAR Time-Data plots. A few things to do: 
 * 1). Lookup the symbol from the DIFAR data unit
 * 2). Set the color of the fill to that of the Channel (i.e. blue for ch 0, red for ch 1).
 * 3). Set the size of the symbol to that of the amplitude 
 * TODO: Make the baseline size a user-adjustable parameter
 * @author brian_mil
 *
 */
public class DifarSymbolChooser implements TDSymbolChooser {
	
	private PamSymbol symbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 8, 8, true, Color.BLACK, Color.BLACK);
	
	private int drawTypes = DRAW_SYMBOLS;

	@Override
	public PamSymbol getPamSymbol(PamDataUnit dataUnit, int type) {
		double amplitudeBaseline = 80;
		DifarDataUnit ddu = (DifarDataUnit) dataUnit;
		Color c = PamColors.getInstance().getChannelColor(ddu.getChannelBitmap()>>1);
		Color fillColor = new Color(c.getRed(),c.getGreen(),c.getBlue(),0);
		symbol = new PamSymbol();

		if (ddu.getLutSpeciesItem() != null){
			symbol = ddu.getLutSpeciesItem().getSymbol();
		}
		else {
//			System.out.println("DifarSymbolChooser cannot find the symbol for species: " + ddu.getSpeciesName() + " Data Unit: " + ddu.toString());
			symbol.setSymbol(PamSymbolType.SYMBOL_CROSS);
			symbol.setFill(false);
			symbol.setWidth(1);
			symbol.setHeight(1);
			return symbol;
		}
		symbol.setFillColor(fillColor);
		// Set the size of the symbol based on the amplitude - higher intensity means larger symbols.
		int size = (int) (ddu.getAmplitudeDB() - amplitudeBaseline);
		symbol.setWidth(size);
		symbol.setHeight(size);
		symbol.setLineThickness(0.5f);
		return symbol;
	}

	@Override
	public int getDrawTypes() {
		return drawTypes ;
	}

	/**
	 * @param drawTypes the drawTypes to set, can be a combination of DRAW_LINES and DRAW_SYMBOLS
	 */
	public void setDrawTypes(int drawTypes) {
		this.drawTypes = drawTypes;
	}

}
