package dataPlotsFX.whistlePlotFX;


import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolOptions;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.TDSymbolChooserFX;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.PamSymbolFX;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * dDefines colours and symbols for whistles on the TD display
 * @author Jamie Macaulay
 *
 */
public class WhistleTDSymbolChooser implements TDSymbolChooserFX {
	
	Color currentCol=Color.DODGERBLUE;
	
	private WhistlePlotInfoFX whistlePlotInfoFX;

	private PamSymbolChooser whistleSymbolChooser;
	
	
	/**
	 * Circle with transparent middle to highlight click 
	 */
//	private static PamSymbolFX highLightWhistle= new PamSymbolFX(PamView.PamSymbolType.SYMBOL_CIRCLE, 6, 6, false, PamUtilsFX.addColorTransparancy(Color.DARKTURQUOISE, 0.3), Color.DARKTURQUOISE);


	public WhistleTDSymbolChooser(WhistlePlotInfoFX whistlePlotInfoFX) {
		this.whistlePlotInfoFX=whistlePlotInfoFX;
//		System.out.println("---------");
		
		whistleSymbolChooser=whistlePlotInfoFX.getDataBlock().getPamSymbolManager().getSymbolChooser(whistlePlotInfoFX.getTDGraph().getUniqueName(), 
				whistlePlotInfoFX.getTDGraph().getGraphProjector());
		
//		System.out.println("WhistleSymbolChooserFX: Whistle Symbol Manager: " + ((StandardSymbolOptions) whistleSymbolChooser.getSymbolOptions()).symbolData); 
//		System.out.println("---------"); 
	}

	@Override
	public int getDrawTypes(PamDataUnit pamDataUnit) {
		  return TDSymbolChooserFX.DRAW_SYMBOLS;
	}

	@Override
	public PamSymbolFX getPamSymbol(PamDataUnit dataUnit, int type) {
	
		PamSymbolFX symbol =  null;
				
		symbol =whistleSymbolChooser.getPamSymbolFX(whistlePlotInfoFX.getTDGraph().getGraphProjector(), dataUnit);
		symbol.setWidth(10);
		symbol.setHeight(10);
		
//		System.out.println("Symbol colours: " + symbol.getFillColor() + " Symbol Manager: " + symbolManager + "  " + whistlePlotInfoFX.getTDGraph().getUniqueName() + "  " + symbolManager.getSymbolOptions()); 

		if (type==TDSymbolChooserFX.NORMAL_SYMBOL){
			return symbol;
		}
		else if (type == TDSymbolChooserFX.HIGHLIGHT_SYMBOL){
			symbol=new PamSymbolFX(symbol.getSymbolData().clone()); //important to clone here or highlighting will cause issues. 
			symbol.setWidth(symbol.getDWidth()+5);
			symbol.setHeight(symbol.getDWidth()+5);
			symbol.setLineColor(Color.DARKTURQUOISE);
			symbol.setFillColor(PamUtilsFX.addColorTransparancy(Color.DARKTURQUOISE, 0.3));
			return symbol;
		}
	
		return symbol; 
	}
	
	/**
	 * Notify settings change 
	 */
	public void notifySettingsChange(){
		whistleSymbolChooser=whistlePlotInfoFX.getDataBlock().getPamSymbolManager().getSymbolChooser(whistlePlotInfoFX.getTDGraph().getUniqueName(), 
				whistlePlotInfoFX.getTDGraph().getGraphProjector());
	}
	
	
//	@Override
//	public Color getLineColor(PamDataUnit pamDataUnit, int type) {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
