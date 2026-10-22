package matchedTemplateClassifer.annotation;

import PamView.GeneralProjector;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.legend.LegendSymbolData;
import PamView.symbol.legend.SymbolLegend;
import PamView.symbol.modifier.SymbolModType;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickDetection;
import matchedTemplateClassifer.MTClassifierControl;

public class MatchedClickSymbolModifier extends SymbolModifier {
	
	private MTClassifierControl mtControl;

	public MatchedClickSymbolModifier(MTClassifierControl mtControl, PamSymbolChooser symbolChooser) {
		super(mtControl.getUnitName(), symbolChooser,  SymbolModType.EVERYTHING);
		this.mtControl = mtControl;
	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		if (dataUnit instanceof ClickDetection == false) {
			return null;
		}
		/**
		 * Jamie, you could possible set the symbol based on the mt annotation which is pretty easy to pull out 
		 * of the data unit using the lines below. For now though I've left it calling back into the function 
		 * you'd previously been calling in mtControl. Feel free to make something more sophisticated using the following
		 * lines ...
		 */
//		MatchedClickAnnotation mtAnnotation = (MatchedClickAnnotation) dataUnit.findDataAnnotation(MatchedClickAnnotation.class);
//		if (mtAnnotation != null) {
//			
//		}
		ClickDetection clickDet = (ClickDetection) dataUnit;
		return mtControl.getSymbolData(clickDet.getClickType());
	}

	/**
	 * Legend with the symbol for clicks classified by the matched template
	 * classifier. Other clicks are not modified.
	 */
	@Override
	public SymbolLegend getSymbolLegend() {
		SymbolLegend legend = new SymbolLegend(getName());
		SymbolData symbolData = mtControl.getMTParams().pamSymbol;
		if (symbolData != null) {
			legend.addSymbol(new LegendSymbolData(symbolData, mtControl.getUnitName()));
		}
		return legend;
	}

}
