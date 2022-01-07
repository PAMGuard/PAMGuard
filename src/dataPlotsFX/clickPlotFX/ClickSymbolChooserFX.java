package dataPlotsFX.clickPlotFX;

import pamViewFX.fxNodes.PamSymbolFX;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;
import javafx.scene.paint.Color;
import clickDetector.ClickControl;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolOptions;
import PamguardMVC.PamDataUnit;
import dataPlotsFX.TDSymbolChooserFX;

/**
 * Chooses which symbols should be displayed by the click detector
 * @author Jamie Macaulay
 *
 */
public class ClickSymbolChooserFX implements TDSymbolChooserFX {
	
	private ClickControl clickControl;
	
	private ClickPlotInfoFX clickPlotInfoFX;
	
	private PamSymbolChooser symbolChooser;


	/**
	 * Circle with transparent middle to highlight click 
	 */
	public static PamSymbolFX highLightClick= new PamSymbolFX(PamView.PamSymbolType.SYMBOL_CIRCLE, 6, 6, 
				false, PamUtilsFX.addColorTransparancy(Color.DARKTURQUOISE, 0.3), Color.DARKTURQUOISE);

	public ClickSymbolChooserFX(ClickControl clickControl,
			ClickPlotInfoFX clickPlotInfoFX) {
			this.clickControl=clickControl; 
			this.clickPlotInfoFX=clickPlotInfoFX;
			
			highLightClick.setLineThickness(4);
			
			String uName = clickPlotInfoFX.getTDGraph().getUniqueName();
//			System.out.println("Symbol choser name: " + uName);
			this.symbolChooser=clickControl.getClickDataBlock().getPamSymbolManager().
					getSymbolChooser(uName, clickPlotInfoFX.getTDGraph().getGraphProjector());
	}

	@Override
	public int getDrawTypes(PamDataUnit pamDataUnit) {
//		int choice = ((StandardSymbolOptions) symbolChooser.getSymbolOptions()).colourChoice; 
//		if ((choice==StandardSymbolOptions.COLOUR_BY_SUPERDET || choice==StandardSymbolOptions.COLOUR_SUPERDET_THEN_SPECIAL) && 
//				pamDataUnit.getSuperDetectionsCount()>0) {
//			//draw both symbols and lines
//			//Causes slow draw speeds. 
//			//return TDSymbolChooserFX.DRAW_SYMBOLS|TDSymbolChooserFX.DRAW_LINES; 
//			return TDSymbolChooserFX.DRAW_SYMBOLS;
//		}
//		else {
			//draw only symbols
		 return TDSymbolChooserFX.DRAW_SYMBOLS;
//		}
	}
	
//	private double calcPixelWidth(ClickDetection clickDetection){
//		return clickPlotInfoFX.getTDGraph().getGraphProjector().getTimePix(clickDetection.getDurationInMilliseconds());
//	}
	
	/**
	 * Calculate the width of the click. 
	 * @param click - the click to calculate width for 
	 * @return the click width
	 */
	private double calcPixelWidth(PamDataUnit click) {
//		System.out.println("Click minClickLength: " + clickPlotInfoFX.getClickDisplayParams().minClickLength + 
//			"Click maxClickLength: " + clickPlotInfoFX.getClickDisplayParams().maxClickLength); 
		return (double) (click.getSampleDuration() / (double) clickPlotInfoFX.getClickControl().getClickParameters().maxLength)  * 
		(clickPlotInfoFX.getClickDisplayParams().maxClickLength - clickPlotInfoFX.getClickDisplayParams().minClickLength) + 
		clickPlotInfoFX.getClickDisplayParams().minClickLength;		
	}
	

//	@Override
//	public Color getLineColor(PamDataUnit pamDataUnit, int type) {
//		
//		symbolManager=clickPlotInfoFX.getDataBlock().getPamSymbolManager().getSymbolChooser(clickPlotInfoFX.getTDGraph().getUniqueName(), 
//				clickPlotInfoFX.getTDGraph().getGraphProjector());
//		
//		LineData lineData =symbolManager.getLineChoice(clickPlotInfoFX.getTDGraph().getGraphProjector(), pamDataUnit);
//
//		return PamUtilsFX.awtToFXColor(lineData.getLineColor());
//	}


	@Override
	public PamSymbolFX getPamSymbol(PamDataUnit dataUnit, int type) {
		
		PamSymbolFX symbol =  null;
			
		if (type==TDSymbolChooserFX.NORMAL_SYMBOL || type==TDSymbolChooserFX.HIGHLIGHT_SYMBOL_MARKED){
			symbol =symbolChooser.getPamSymbolFX(clickPlotInfoFX.getTDGraph().getGraphProjector(), dataUnit);
			if (symbol==null) return null; 
			symbol.setWidth(calcPixelWidth(dataUnit));
			symbol.setHeight(getClickHeight(dataUnit));
		}
		else if (type==TDSymbolChooserFX.HIGHLIGHT_SYMBOL) {
			symbol=highLightClick; 
			symbol.setWidth(calcPixelWidth(dataUnit)+5);
			symbol.setHeight(getClickHeight(dataUnit)+5);
		}
	
		return symbol; 
		
//		ClickDetection click = (ClickDetection) dataUnit;
//		PamSymbolFX symbol =  null; 
//		
//		
//		//TODO
//		PamSymbolFX speciesSymbol;
//		if (clickControl.getClickIdentifier().getSymbol(click)!=null){
//			speciesSymbol = new PamSymbolFX(clickControl.getClickIdentifier().getSymbol(click));
//		}
//		else speciesSymbol=clickSymbol; 
//		
//		speciesSymbol.setWidth(calcPixelWidth(click));
//		
//		//System.out.println("Click width: "+ calcPixelWidth(click)); 
//
//		//apply correct shape and colour to symbol.
//		Color col;
//		 switch (clickPlotInfoFX.clickDisplayParams.colourBy){
//			 case COLOUR_BY_HYDROPHONE:                       
//					symbol = clickSymbol;
//					if (speciesSymbol != null) {
//						symbol.setSymbol(speciesSymbol.getSymbol());
//					}
//					int chan = PamUtils.getLowestChannel(click.getChannelBitmap());
//					col = PamColorsFX.getInstance().getWhaleColor(chan+1);
//					symbol.setFillColor(col);
//					symbol.setLineColor(col);
//				 break;
//			 case COLOUR_BY_SPECIES:
//				 symbol=speciesSymbol;
//				 
//				 break;
//			 case COLOUR_BY_TRAIN:
//				 //TODO
//				 symbol=clickSymbol;
//				 col = getTrainColour(click);
//				 if (col != null) {
//					 symbol.setFillColor(col);
//					 symbol.setLineColor(col);
//				 }
//				 else {
//					 symbol.setFillColor(Color.BLACK);
//					 symbol.setLineColor(Color.BLACK);
//				 }
//				 break;
//			 case COLOUR_BY_TRAINANDSPECIES:
//				 //TODO
//				 symbol=clickSymbol;
//				 break;
//			 default:
//				 symbol=clickSymbol;
//				 break;
//		 }
//		 
//		//apply any size transforms to symbols
//		switch (type){
//				case TDSymbolChooser.HIGHLIGHT_SYMBOL: 
//					symbol.setLineThickness(3);
//					symbol.setWidth((int) (calcPixelWidth(click)+5));
//					symbol.setHeight((int) (getClickHeight(click)+5));
//					break;
//				case TDSymbolChooser.HIGHLIGHT_SYMBOL_MARKED: 
//					symbol.setWidth((int) (calcPixelWidth(click)+2));
//					symbol.setHeight((int) (getClickHeight(click)+2));
//					break;
//				default:
//					symbol.setWidth((int) (calcPixelWidth(click)+0.5));
//					symbol.setHeight((int) (getClickHeight(click)+0.5));
//		}
//		 return symbol;
	}
	
//	private Color getTrainColour(ClickDetection click) {
//		OfflineEventDataUnit offlineEvent = (OfflineEventDataUnit) click.getSuperDetection(OfflineEventDataUnit.class);
//		if (offlineEvent == null) {
//			return null;
//		}
//		return PamColorsFX.getInstance().getWhaleColor((int) offlineEvent.getUID());
//	}
	
	/**
	 * Get the height of the click for the display
	 * @param click click to determine height for. 
	 * @return the height of the click on the display in pixels 
	 */
	private double getClickHeight(PamDataUnit click) {
		// scale according to the amplitude range and min / max pixels of 3 and 12
		ClickDisplayParams clickDisplayParams = clickPlotInfoFX.clickDisplayParams;
		return Math.max((click.getAmplitudeDB() - clickPlotInfoFX.getAmpScaleInfo().getMinVal()) / 
				(clickPlotInfoFX.getAmpScaleInfo().getMaxVal() - clickPlotInfoFX.getAmpScaleInfo().getMinVal()) * 
				(clickDisplayParams.maxClickHeight - clickDisplayParams.minClickHeight) + 
				clickDisplayParams.minClickHeight, 2);
	}
	
	/**
	 * Called whenever settings on the control pane for the click detector change, 
	 */
	protected void notifySettingsChange(){
		symbolChooser=clickPlotInfoFX.getDataBlock().getPamSymbolManager().getSymbolChooser(clickPlotInfoFX.getTDGraph().getUniqueName(), 
				clickPlotInfoFX.getTDGraph().getGraphProjector());
		
	}
	
	public PamSymbolChooser getSymbolChooser() {
		return symbolChooser;
	}


}
