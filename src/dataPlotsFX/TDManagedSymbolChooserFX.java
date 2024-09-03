package dataPlotsFX;

import PamView.symbol.PamSymbolChooser;
import PamguardMVC.PamDataUnit;
import clickDetector.tdPlots.ClickSymbolChooser;
import dataPlotsFX.clickPlotFX.ClickSymbolChooserFX;
import dataPlotsFX.data.TDDataInfoFX;
import pamViewFX.fxNodes.PamSymbolFX;

/**
 * Default symbol chooser for the display. This based on the data blocks symbol chooser. 
 * 
 * @author Jamie Maaulay 
 *
 */
public class TDManagedSymbolChooserFX implements TDSymbolChooserFX{

	/**
	 * The pam symbol chooser. 
	 */
	private PamSymbolChooser pamSymbolChooser;
	
	/**
	 * Reference to the TDDataInfo the ymbol chooser belongs to. 
	 */
	private TDDataInfoFX dataInfoFX;
	
	private PamSymbolFX plainSymbol = new PamSymbolFX();
	
	/**
	 * The draw types. 
	 */
	private int drawTypes;
	
	private PamSymbolFX currentSymbol;

	public TDManagedSymbolChooserFX(TDDataInfoFX dataInfoFX, PamSymbolChooser pamSymbolChooser, int drawTypes) {
		super();
		this.pamSymbolChooser = pamSymbolChooser;
		this.dataInfoFX = dataInfoFX;
		this.drawTypes = drawTypes;
	}

	@Override
	public int getDrawTypes(PamDataUnit pamDataUnit) {
		return drawTypes;
	}

	@Override
	public PamSymbolFX getPamSymbol(PamDataUnit dataUnit, int type) {
		PamSymbolFX symbol =  plainSymbol;
		
		if (type==TDSymbolChooserFX.NORMAL_SYMBOL || type==TDSymbolChooserFX.HIGHLIGHT_SYMBOL_MARKED){
			symbol =pamSymbolChooser.getPamSymbolFX(dataInfoFX.getTDGraph().getGraphProjector(), dataUnit);
		}
		else if (type==TDSymbolChooserFX.HIGHLIGHT_SYMBOL) {
			symbol=ClickSymbolChooserFX.highLightClick;
		}
		
		return symbol; 
	}
	
}
