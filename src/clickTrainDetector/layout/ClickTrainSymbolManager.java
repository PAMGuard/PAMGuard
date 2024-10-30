package clickTrainDetector.layout;

import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import detectiongrouplocaliser.DetectionGroupGraphics;

/**
 * Manages colours of symbols. 
 * @author Jamie Macaulay 
 *
 */
@SuppressWarnings("rawtypes")
public class ClickTrainSymbolManager extends StandardSymbolManager {

	public ClickTrainSymbolManager(PamDataBlock pamDataBlock) {
		super(pamDataBlock, DetectionGroupGraphics.defaultSymbol);
		setSpecialColourName("Click Train Index");
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
//			//System.out.println("ClickTrainSymbolManager: Get symbol choice: ");
//			//paint the data unit based on it's UID. 
//			if (CTDataUnit.class.isAssignableFrom(dataUnit.getClass())) {
//				
//				CTDataUnit ctDataUnit = (CTDataUnit) dataUnit;
//				
//				Color col;
//
//				if (ctDataUnit.getClassificationIndex()==-1
//						|| ctDataUnit.getCtClassifications().get(ctDataUnit.getClassificationIndex()).getSpeciesID()<=0) {
//					//the click train has not been classified but has passed a pre classification test 
//					// or is less than 0 and so by convention should be noise. 
//					//i.e. has not been junked by the algorithms
//					col = Color.GRAY; //the click train is dark gray. 
//				}
//				else {
//					//the click train has been classified and is therefore a random whale colour in PG. 
//					col = PamColors.getInstance().getWhaleColor((int) dataUnit.getUID());
//				}
//				
//				//symbol data. 
//				SymbolData symbolData=new SymbolData();
//				symbolData.setFillColor(col);
//				symbolData.setLineColor(col);
//				return symbolData;
//			}
//			return null; 
//		}
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
