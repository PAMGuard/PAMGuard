package cpod;

import java.awt.Color;

import PamView.GeneralProjector;
import PamView.PamSymbolType;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.SymbolModType;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataUnit;
import cpod.CPODClassification.CPODSpeciesType;

public class CPODSpeciesModifier extends SymbolModifier {
	
	final Color porpColor = new Color(93,30,255);

	final Color dolphColor = new Color(255,160,0);
			
	private SymbolData symbolData = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 5, 5, true, java.awt.Color.BLACK, java.awt.Color.BLACK);

	public CPODSpeciesModifier(PamSymbolChooser symbolChooser) {
		super("Species", symbolChooser, SymbolModType.FILLCOLOUR |  SymbolModType.LINECOLOUR);
	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		
		CPODClick cpodClick = (CPODClick) dataUnit; 
		
		Color color = Color.BLACK;

		
		if (cpodClick.getCPODClickTrain()!=null) {
		
		CPODSpeciesType species = cpodClick.getCPODClickTrain().getSpecies(); 
		
		switch(species) {
		case DOLPHIN:
			color=dolphColor;
			break;
		case NBHF:
			color=porpColor;;
			break;
		case SONAR:
			color = Color.DARK_GRAY;
			break;
		case UNKNOWN:
			break;
		default:
			break;
		}
		
		}
	
		symbolData.setFillColor(color);
		symbolData.setLineColor(color);
		
		return symbolData;
	}

}
