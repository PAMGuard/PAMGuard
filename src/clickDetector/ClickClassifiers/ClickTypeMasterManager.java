package clickDetector.ClickClassifiers;

import java.util.ArrayList;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamView.symbol.SymbolData;
import clickDetector.ClickControl;

/**
 * The click type manager manages click types within the click detector. The
 * click detector has it's own classifier, however other modules e.g. (Matched
 * Template Classifier, the Click Train Detector/Classifier) can also change
 * click types. The ClickTypeManager manages click types within the click
 * detector and other sub modules, providing a master list.
 * <p>
 * The ClickTypeMasterManager will find any child modules of it's click control
 * which implements ClickTypeProvider.
 * 
 * 
 * @author Jamie Macaulay
 *
 */
public class ClickTypeMasterManager implements ClickTypeProvider {
	
	/**
	 * All current symbols
	 */
	private SymbolData[] symbolDatas;
	
	/**
	 * Species names
	 */
	private String[] speciesNames;
	
	/**
	 * Types. 
	 */
	private int[] types;
	
	/**
	 * Click control;. 
	 */
	private ClickControl clickControl;
	
	/**
	 * The click type master manager constructor
	 * @param clickControl - the click control. 
	 */
	public ClickTypeMasterManager(ClickControl clickControl) {
		this.clickControl = clickControl; 
	}
	
	
	@Override
	public SymbolData[] getSymbolsData() {
		return symbolDatas;
	}
	
	
	@Override
	public String[] getSpeciesList() {
		return speciesNames;
	}


	@Override
	public int[] getCodeList() {
		return types;
	}


	@Override
	public int codeToListIndex(int code) {
		if (types==null) return -1; 
		for (int i=0; i<types.length; i++) {
			if (types[i]==code) return i; 
		}
		return -1;
	}

	
	
	/**
	 * Get all possible click symbols from the click detector and any downstream processes which
	 * change click types. 
	 * @param clickControl - clikc control. 
	 * @return all symbol data.
	 */
	public void updateSpeciesList() {
		
		ArrayList<SymbolTypeData> symbolDatas = new ArrayList<SymbolTypeData>(); 
				
		//get symbols from internel click detector. 
		addClkTypeProviderDat(symbolDatas, clickControl.getClickIdentifier());

		//get symbols for the matched click classifier. 
		PamControlledUnit controlledUnit; 
		for (int i =0; i<PamController.getInstance().getNumControlledUnits(); i++) {
			controlledUnit = PamController.getInstance().getControlledUnit(i); 
			if (controlledUnit instanceof ClickTypeProvider) {
				addClkTypeProviderDat(symbolDatas, ((ClickTypeProvider) controlledUnit));
			}
		}
		
		//now have an updated list of all possible click types. 
//		System.out.println("Click TYPES!!!!: " + symbolDatas);
		
		
		//bit clunky but meh means backward compatible
		int [] types = new int[symbolDatas.size()];
		String [] speciesNames = new String[symbolDatas.size()];
		SymbolData[] symbolData = new SymbolData[symbolDatas.size()];
		
		for (int i=0; i<symbolDatas.size(); i++) {
			types[i]=symbolDatas.get(i).type;
			speciesNames[i]=symbolDatas.get(i).speciesName;
			symbolData[i]=symbolDatas.get(i).symbolData;
		}
		
		this.symbolDatas = symbolData; 
		this.types = types; 
		this.speciesNames = speciesNames; 
		
	}
	
	/**
	 * Add data to the symbolDatas array list from a click type provider. 
	 * @param symbolDatas
	 */
	private void addClkTypeProviderDat(ArrayList<SymbolTypeData> symbolDatas, ClickTypeProvider clkTypeProvider) {
		
		if (clkTypeProvider==null) return; 
				
		//get symbols for the in built click classiifer
		SymbolData[] symbols=clkTypeProvider.getSymbolsData();
		int[] types=clkTypeProvider.getCodeList();
		String[] species=clkTypeProvider.getSpeciesList();
		
		if (symbols==null) return;
		
		for (int i = 0; i< symbols.length; i++ ) {
			symbolDatas.add(new SymbolTypeData(symbols[i], types[i], species[i]));
		}
	}


	/**
	 * Holds symbol data 
	 * @author Jamie Macaulay 
	 *
	 */
	private class SymbolTypeData {
		
		/**
		 * Symbol data
		 */
		protected SymbolData symbolData; 
		
		/**
		 * Type
		 */
		protected int type; 
		
		protected String speciesName;
		
		public SymbolTypeData(SymbolData symbolData, int type, String speciesName) {
			this.symbolData = symbolData;
			this.type = type;
			this.speciesName = speciesName;
		}
		
	}

	/**
	 * Notifications which are passed from the click detector. 
	 * @param changeType
	 */
	public void notifyModelChanged(int changeType) {
		this.updateSpeciesList();
	}



}
