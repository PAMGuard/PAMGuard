package clickDetector.tdPlots;

import java.awt.Color;

import clickDetector.BTDisplayParameters;
import clickDetector.ClickBTDisplay;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamguardMVC.PamDataUnit;
import dataPlots.data.TDSymbolChooser;

@Deprecated
public class ClickSymbolChooser implements TDSymbolChooser {

	private ClickControl clickControl;
	
	private ClickPlotInfo clickPlotInfo;
	
	private PamSymbol clickSymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE,6, 6, true, Color.BLACK, Color.BLACK);
	
	/**
	 * Circle with transparant middle to highlight click 
	 */
	private PamSymbol highLightClick= new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 6, 6, false, new Color(255,255,255,0), Color.GRAY);
	
	
	public ClickSymbolChooser(ClickControl clickControl,
			ClickPlotInfo clickPlotInfo) {
		super();
		this.clickControl = clickControl;
		this.clickPlotInfo = clickPlotInfo;
	}

	@Override
	public int getDrawTypes() {
		return TDSymbolChooser.DRAW_SYMBOLS;
	}
	
	@Override
	public PamSymbol getPamSymbol(PamDataUnit dataUnit, int type) {
		
		ClickDetection click = (ClickDetection) dataUnit;
		PamSymbol symbol =null;
		//select the correct symbol
		switch (type){
			case TDSymbolChooser.HIGHLIGHT_SYMBOL: symbol=highLightClick;
				break;
			case TDSymbolChooser.HIGHLIGHT_SYMBOL_MARKED: symbol= ClickDetSymbolChooser.getClickSymbol(clickControl.getClickIdentifier(), click, 
					clickPlotInfo.btDisplayParams.colourScheme);
				break;
			default: symbol = ClickDetSymbolChooser.getClickSymbol(clickControl.getClickIdentifier(), click, 
					clickPlotInfo.btDisplayParams.colourScheme);
		}

		if (symbol == null) {
			symbol = clickSymbol;
		}
		//apply any size transfroms to sym bols. 
		switch (type){
			case TDSymbolChooser.HIGHLIGHT_SYMBOL: 
				symbol.setLineThickness(3);
				symbol.setWidth((int) (getClickWidth(click)+5));
				symbol.setHeight((int) (getClickHeight(click)+5));
				break;
			case TDSymbolChooser.HIGHLIGHT_SYMBOL_MARKED: 
				symbol.setWidth((int) (getClickWidth(click)+2));
				symbol.setHeight((int) (getClickHeight(click)+2));
				break;
			default:
				symbol.setWidth((int) (getClickWidth(click)+0.5));
				symbol.setHeight((int) (getClickHeight(click)+0.5));
		}
	
		return symbol;
	}
	
	private double getClickHeight(ClickDetection click) {
		// scale according to the amplitude range and min / max pixels of 3 and 12
		BTDisplayParameters btDisplayParameters = clickPlotInfo.btDisplayParams;
		return Math.max((click.getAmplitudeDB() - btDisplayParameters.amplitudeRange[0]) / 
				(btDisplayParameters.amplitudeRange[1] - btDisplayParameters.amplitudeRange[0]) * 
				(btDisplayParameters.maxClickHeight - btDisplayParameters.minClickHeight) + 
				btDisplayParameters.minClickHeight, 2);
	}

	private double getClickWidth(ClickDetection click) {
		BTDisplayParameters btDisplayParameters = clickPlotInfo.btDisplayParams;
		return (double) click.getSampleDuration() / (double) clickControl.getClickParameters().maxLength * 
		(btDisplayParameters.maxClickLength - btDisplayParameters.minClickLength) + 
		btDisplayParameters.minClickLength;		
	}

}
