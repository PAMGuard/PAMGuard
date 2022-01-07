package clickTrainDetector.layout;

import java.awt.Color;

import PamView.ColourArray;
import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.PamSymbolType;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.SymbolModType;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataUnit;
import clickTrainDetector.CTDataUnit;
import clickTrainDetector.TempCTDataUnit;

public class ClickTrainSymbolModifer extends SymbolModifier {

	private ColourArray colourArray;


	private double maxChi2=1500; 

	public final static String CLICK_TRAIN_MODIFIER_NAME = "Click Train";


	public ClickTrainSymbolModifer(PamSymbolChooser symbolChooser) {
		super(CLICK_TRAIN_MODIFIER_NAME, symbolChooser, SymbolModType.FILLCOLOUR |  SymbolModType.LINECOLOUR );
		colourArray = ColourArray.createMergedArray(100, Color.DARK_GRAY, Color.LIGHT_GRAY); 

	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		//System.out.println("UnconfirmedClickTrainSymbolManager: Get symbol choice: " + dataUnit.getClass());

		//paint the data unit based on it's UID. 
		if (TempCTDataUnit.class.isAssignableFrom(dataUnit.getClass())) {

			SymbolData symbolData=new SymbolData();
			symbolData.symbol=PamSymbolType.SYMBOL_STAR;

			Color col = getChi2Col(((TempCTDataUnit) dataUnit).getCTChi2()); 

			symbolData.setFillColor(col);
			symbolData.setLineColor(col);

			//System.out.println("Unconfirmed symbol data: " + symbolData);


			return symbolData;
		}
		else if (CTDataUnit.class.isAssignableFrom(dataUnit.getClass())) {

			CTDataUnit ctDataUnit = (CTDataUnit) dataUnit;

			Color col;

			if (ctDataUnit.getClassificationIndex()==-1
					|| ctDataUnit.getCtClassifications().get(ctDataUnit.getClassificationIndex()).getSpeciesID()<=0) {
				//the click train has not been classified but has passed a pre classification test 
				// or is less than 0 and so by convention should be noise. 
				//i.e. has not been junked by the algorithms
				col = Color.GRAY; //the click train is dark gray. 
			}
			else {
				//the click train has been classified and is therefore a random whale colour in PG. 
				col = PamColors.getInstance().getWhaleColor((int) dataUnit.getUID());
			}

			//symbol data. 
			SymbolData symbolData=new SymbolData();
			symbolData.setFillColor(col);
			symbolData.setLineColor(col);
			return symbolData;
		}
		return null; 

	}

	/**
	 * Get the colour to paint the click train based on it's chi2 value
	 * @return the colour coded to the click trains chi2 value. 
	 */
	private Color getChi2Col(double chi2) {

		int colind = (int) (colourArray.getNumbColours()  * chi2/maxChi2); 

		if (colind>=colourArray.getNumbColours() ) {
			colind=colourArray.getNumbColours() -1;
		}

		return colourArray.getColour(colind); 


	}
}
