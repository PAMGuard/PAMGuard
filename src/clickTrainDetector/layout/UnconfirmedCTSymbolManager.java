package clickTrainDetector.layout;

import java.awt.Color;

import PamView.ColourArray;
import PamView.GeneralProjector;
import PamView.PamSymbolType;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.RotateColoursModifier;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickTrainDetector.ClickTrainControl;
import clickTrainDetector.TempCTDataUnit;
import detectiongrouplocaliser.DetectionGroupGraphics;

public class UnconfirmedCTSymbolManager extends StandardSymbolManager {
	
//	private ColourArray colourArray;
	
	private ClickTrainControl clickTrainControl; 
	
//	private double maxChi2=1500; 

	public UnconfirmedCTSymbolManager(ClickTrainControl clickTrainControl, PamDataBlock pamDataBlock) {
		super(pamDataBlock, DetectionGroupGraphics.starSymbol);
		this.clickTrainControl=clickTrainControl; 
//		setSpecialColourName("Unconfirmed Click Train Index");
//		colourArray = ColourArray.createMergedArray(100, Color.DARK_GRAY, Color.LIGHT_GRAY); 
	}
	
//	/* (non-Javadoc)
//	 * @see PamView.symbol.StandardSymbolManager#createSymbolChooser(java.lang.String, PamView.GeneralProjector)
//	 */
//	@Override
//	protected StandardSymbolChooser createSymbolChooser(String displayName, GeneralProjector projector) {
//		return new ClikcTrainSymbolChooser(this, getPamDataBlock(), displayName, DetectionGroupGraphics.defaultSymbol, projector);
//	}
//
//	/**
//	 * Special Symbol chooser for click trains. 
//	 * @author Jamie Macaulay
//	 *
//	 */
//	private class ClikcTrainSymbolChooser extends StandardSymbolChooser {
//
//		public ClikcTrainSymbolChooser(StandardSymbolManager standardSymbolManager, PamDataBlock pamDataBlock,
//				String displayName, SymbolData defaultSymbol, GeneralProjector projector) {
//			super(standardSymbolManager, pamDataBlock, displayName, defaultSymbol, projector);
//		}
//		
//		@Override
//		public SymbolData getSymbolChoice(GeneralProjector projector, PamDataUnit dataUnit) {
//			//System.out.println("UnconfirmedClickTrainSymbolManager: Get symbol choice: " + dataUnit.getClass());
//
//			//paint the data unit based on it's UID. 
//			if (TempCTDataUnit.class.isAssignableFrom(dataUnit.getClass())) {
//				
//				SymbolData symbolData=new SymbolData();
//				symbolData.symbol=PamSymbolType.SYMBOL_STAR;
//				
//				Color col = getChi2Col(((TempCTDataUnit) dataUnit).getCTChi2()); 
//								
//				symbolData.setFillColor(col);
//				symbolData.setLineColor(col);
//				
//				//System.out.println("Unconfirmed symbol data: " + symbolData);
//
//				
//				return symbolData;
//			}
//			return null; 
//		}
//	}
//	
//	/**
//	 * Get the colour to paint the click train based on it's chi2 value
//	 * @return the colour coded to the click trains chi2 value. 
//	 */
//	private Color getChi2Col(double chi2) {
//		
//		int colind = (int) (colourArray.getNumbColours()  * chi2/maxChi2); 
//		
//		if (colind>=colourArray.getNumbColours() ) {
//			colind=colourArray.getNumbColours() -1;
//		}
//		
//		return colourArray.getColour(colind); 
//		
//		
//	}
	
	
	@Override
	public void addSymbolModifiers(PamSymbolChooser psc) {
	
		
		super.addSymbolModifiers(psc);
		
		//add the peak frequency modifier that allows clicks to be coloured by peak frequency. 
		psc.addSymbolModifier(new ClickTrainSymbolModifer(psc));
		
		// we can also add some default behaviour here to match the old behaviour
		// these will get overridden once user options are set, but it's good to give defaults. 
//		SymbolModifier eventMod = psc.hasSymbolModifier(SuperDetSymbolModifier.class);
//		if (eventMod != null) {
//			eventMod.getSymbolModifierParams().modBitMap = (SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR);
//		}
		
	}

}
