package clickDetector.tdPlots;

import java.awt.Color;
import java.util.ArrayList;

import PamUtils.PamUtils;
import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.PamColors.PamColor;
import PamView.symbol.PamSymbolOptions;
import PamView.symbol.StandardSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.StandardSymbolOptions;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickDetector.BTDisplayParameters;
import clickDetector.ClickControl;
import clickDetector.ClickDataBlock;
import clickDetector.ClickDetection;
import clickDetector.ClickClassifiers.ClickIdentifier;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import pamViewFX.symbol.FXSymbolOptionsPane;
import pamViewFX.symbol.StandardSymbolOptionsPane;

/**
 * Symbol chooser for the click detector. 
 * 
 * @author Doug Gillespie
 *
 */
public class ClickDetSymbolChooser extends StandardSymbolChooser {

	/**
	 * Reference to the click control. 
	 */
	private ClickControl clickControl;

	/**
	 * Click symbol options. 
	 */
	private ClickSymbolOptions clickSymbolOptions;


	/**
	 * The default click symbol. 
	 */
	private static PamSymbol defaultSymbol;


	public ClickDetSymbolChooser(StandardSymbolManager standardSymbolManager, ClickControl clickControl, PamDataBlock pamDataBlock,
			String displayName, SymbolData defaultSymbol, GeneralProjector projector) {
		super(standardSymbolManager, pamDataBlock, displayName, defaultSymbol, projector);
		this.clickControl = clickControl;
		setSymbolOptions(clickSymbolOptions = new ClickSymbolOptions(super.getDefaultSymbol()));
	}

	@Override
	public SymbolData getSymbolChoice(GeneralProjector projector, PamDataUnit dataUnit) {
		ClickDetection click = (ClickDetection) dataUnit;
		SymbolData symbolData;

		// if there is no click, revert to the symbol from the super class
		if (click==null) {
			return super.getSymbolChoice(projector, dataUnit);
		}


		symbolData = super.getSymbolChoice(projector, dataUnit);

		return symbolData;
	}


	@Override
	public FXSymbolOptionsPane getFXOptionPane(GeneralProjector projector) {

		getSymbolManager().addSymbolOption(StandardSymbolManager.HAS_SYMBOL);

		if (showLineLengthOption(projector)){
			getSymbolManager().addSymbolOption(StandardSymbolManager.HAS_LINE_LENGTH);
		}

		FXSymbolOptionsPane ssop = new StandardSymbolOptionsPane(getSymbolManager(), this);

		return ssop;
	}

	@Override
	public StandardSymbolOptions getSymbolOptions() {
		ArrayList<SymbolModifier> modifiers = getSymbolModifiers();
		for (SymbolModifier symbolModifier: modifiers) {
			clickSymbolOptions.setModifierParams(symbolModifier.getName(), symbolModifier.getSymbolModifierParams());
		}
		return clickSymbolOptions; 
	}



	@Override
	public void setSymbolOptions(PamSymbolOptions symbolOptions) {
		super.setSymbolOptions(symbolOptions);
		if (!(symbolOptions instanceof ClickSymbolOptions)) {
			clickSymbolOptions = new ClickSymbolOptions((StandardSymbolOptions) symbolOptions); 
		}
		else this.clickSymbolOptions=(ClickSymbolOptions) symbolOptions; 

	}

	/**
	 * Get the data block for this symbol chooser. 
	 * @return the data block. 
	 */
	public ClickDataBlock getClickDataBlock() {
		return this.clickControl.getClickDataBlock(); 
	}

	/**
	 * 
	 * @param clickIdentifier
	 * @param click
	 * @param colourType
	 * @return
	 */
	@Deprecated
	public static PamSymbol getClickSymbol(ClickIdentifier clickIdentifier, ClickDetection click, int colourType) {

		PamSymbol speciesSymbol =clickIdentifier.getSymbol(click);

		PamSymbol symbol = getDefaultSymbol(true);
		Color aCol;

		if (colourType == BTDisplayParameters.COLOUR_BY_SPECIES) {
			// use the species shape and colour from the speciesSymbol
			if (speciesSymbol != null) {
				return speciesSymbol;
			}
		}
		else if (colourType == BTDisplayParameters.COLOUR_BY_HYDROPHONE) {
			symbol = getDefaultSymbol(true);
			if (speciesSymbol != null) {
				symbol.setSymbol(speciesSymbol.getSymbol());
			}
			int chan = PamUtils.getLowestChannel(click.getChannelBitmap());
			Color col = PamColors.getInstance().getWhaleColor(chan+1);
			symbol.setFillColor(col);
			symbol.setLineColor(col);
		}
		else {// (colourType == BTDisplayParameters.COLOUR_BY_TRAIN) {
			// use the colours of the train, but the shape of the default symbol. 
			symbol = getDefaultSymbol(true);

			if (speciesSymbol != null) {
				symbol.setSymbol(speciesSymbol.getSymbol());
			}			
			OfflineEventDataUnit offlineEvent = 
					(OfflineEventDataUnit) click.getSuperDetection(OfflineEventDataUnit.class);
			if (offlineEvent != null) {
				//				int colind = offlineEvent.getColourIndex();
				//				System.out.printf("Color click %d col index %d\n", click.clickNumber, colind);
				symbol.setFillColor(aCol = PamColors.getInstance().getWhaleColor(offlineEvent.getColourIndex()));
				symbol.setLineColor(aCol);
			}
			else if (colourType == BTDisplayParameters.COLOUR_BY_TRAINANDSPECIES && 
					speciesSymbol != null) {
				return speciesSymbol;
			}
			else {
				symbol.setFillColor(aCol = PamColors.getInstance().getWhaleColor(click.getEventId()));
				symbol.setLineColor(aCol);
				//				System.out.printf("Color click %d event id %d, col index %s\n", click.clickNumber, click.getEventId(), aCol.toString());
			}
		}
		return symbol;
	}
	//	static PamSymbol getClickSymbol(ClickIdentifier clickIdentifier, int eventId) {
	//		PamSymbol symbol = clickIdentifier.getSymbol(click);
	//		if (symbol == null) {
	//			symbol = getDefaultSymbol();
	//			symbol.setFillColor(PamColors.getInstance().getWhaleColor(eventId));
	//			symbol.setLineColor(PamColors.getInstance().getWhaleColor(eventId));
	//		}
	//		return symbol;
	//	}
	private SymbolData getClickSymbol(ClickDetection click) {
		return getClickSymbol(click.getEventId()).getSymbolData();
	}

	public static PamSymbol getClickSymbol(int eventId) {
		PamSymbol symbol = getDefaultSymbol(false);
		symbol.setFillColor(PamColors.getInstance().getWhaleColor(eventId));
		symbol.setLineColor(PamColors.getInstance().getWhaleColor(eventId));


		return symbol;
	}


	public static PamSymbol getDefaultSymbol(boolean makeClone) {
		if (defaultSymbol == null) {
			defaultSymbol = new PamSymbol(PamSymbolType.SYMBOL_CIRCLE, 8, 8,
					true, PamColors.getInstance().getColor(PamColor.PLAIN), 
					PamColors.getInstance().getColor(PamColor.PLAIN));
		}
		if (defaultSymbol.getFillColor() != PamColors.getInstance().getColor(PamColor.PLAIN)) {
			defaultSymbol.setFillColor(PamColors.getInstance().getColor(PamColor.PLAIN));
			defaultSymbol.setLineColor(PamColors.getInstance().getColor(PamColor.PLAIN));
		}
		// always reset the shape since it may have been messed about with
		defaultSymbol.setSymbol(PamSymbolType.SYMBOL_CIRCLE);
		if (makeClone) {
			return defaultSymbol.clone();
		}
		else {
			return defaultSymbol;
		}
	}
	
	
	//	/**
	//	 * Set the current symbol type. This is only ever used in the click
	//	 * detector BT display and needs to go on working, even though it
	//	 * must now bypass the standard methods. We can do this by setting the
	//	 * two colour flags on the appropriate modifier, which we'll just have
	//	 * to try to find by it's class. 
	//	 * @param type the type flag.
	//	 */
	//	public void setSymbolType(int type) {
	//		/**
	//		 * Need to discuss with J how we're going to make these symbol options more 
	//		 * flexible for new incoming annotations. Perhaps can just override full choice ? 
	//		 */
	////		this.clickSymbolOptions.colourChoice=type;
	//		switch (type) {
	//		case StandardSymbolOptions.COLOUR_FIXED:
	//			setModifierSelection(null);
	//			break;
	//		case StandardSymbolOptions.COLOUR_BY_SUPERDET:
	//			setModifierSelection(null);
	//			break;
	//			
	//		}
	//	}


	//	private void setModifierSelection(Object object) {
	//		// TODO Auto-generated method stub
	//		
	//	}

	//	@Override
	//	public SymbolData colourBySpecial(SymbolData symbolData, GeneralProjector projector, PamDataUnit dataUnit) {
	//		//		SymbolData sd = super.colourBySpecial(symbolData, projector, dataUnit);
	//
	//		ClickDetection click = (ClickDetection) dataUnit;
	//		
	//		byte clickType = click.getClickType();
	//		if (clickType == 0) {
	//			return symbolData;
	//		}
	//
	//		//search through some annotations for the matched click classifier. This is a special case because
	//		//the matched click classifier changes the click species ID - an inbuilt variable that defines
	//		//click colours on the display. Usually annotation colouring his handled by the  StandardSymbolChooser
	//		DataAnnotation annotation = dataUnit.findDataAnnotation(MatchedClickAnnotation.class);
	//		if (annotation!=null) {
	//			SymbolData mtSymbolData = ((MatchedClickAnnotation) annotation).getSymbolData(clickType); 
	//			if (mtSymbolData!=null) {
	//				return mtSymbolData;
	//			}
	//		}
	//		
	//		//now search for click which has standard classification - all a bit messy but needs to be done in this order. 
	//		PamSymbol symbol=clickControl.getClickIdentifier().getSymbol(click);
	//		if (symbol != null) {
	//			return symbol.getSymbolData();
	//		}
	//
	//		//check annotation for other click types. 
	//		return symbolData;
	//	}




}
