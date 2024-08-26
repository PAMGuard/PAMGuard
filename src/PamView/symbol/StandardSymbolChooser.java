package PamView.symbol;

import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import PamUtils.PamArrayUtils;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.dialog.GenericSwingDialog;
import PamView.symbol.modifier.SymbolModifier;
import PamView.symbol.modifier.SymbolModifierParams;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import pamViewFX.symbol.FXSymbolOptionsPane;
import pamViewFX.symbol.StandardSymbolOptionsPane;

@SuppressWarnings("rawtypes")
public class StandardSymbolChooser extends PamSymbolChooser {

	private StandardSymbolOptions symbolOptions;
	private SymbolData defaultSymbol;
	private StandardSymbolManager standardSymbolManager;

	public StandardSymbolChooser(StandardSymbolManager standardSymbolManager, PamDataBlock pamDataBlock, 
			String displayName, SymbolData defaultSymbol, GeneralProjector projector) {
		super(standardSymbolManager, pamDataBlock, displayName, projector);
		this.standardSymbolManager = standardSymbolManager;
		this.defaultSymbol = defaultSymbol;
		symbolOptions = new StandardSymbolOptions(getDefaultSymbol());
	}

	/**
	 * Get the current fixed signal prior to any modification. 
	 * @return the fixed symbol. 
	 */
	public SymbolData getFixedSymbol() {
		SymbolData symbolData = getSymbolOptions().symbolData;

		if (symbolData == null) {
			symbolData = getDefaultSymbol();
		}
		if (symbolData == null) {
			symbolData = new SymbolData();
		}
		return symbolData;
	}

	@Override
	public SymbolData getSymbolChoice(GeneralProjector projector, PamDataUnit dataUnit) {
		//System.out.println("Object id " + this +  "  symbolOptions" +  symbolOptions.colourChoice);

		SymbolData symbolData = getFixedSymbol();

		StandardSymbolOptions options = getSymbolOptions();

		int[] mord = options.getModifierOrder(this);
		ArrayList<SymbolModifier> modifiers = getSymbolModifiers();

//		System.out.println("StandardSymbolChooser: " + modifiers.size() + " Defualt shape: " +  options.symbolData.symbol);
//		System.out.println("StandardSymbolOptions: " + mord.length + "  " + this.getSymbolManager() + "  " + options);
//		PamArrayUtils.printArray(mord);

		if (modifiers == null || modifiers.size() == 0) {
			return symbolData;
		}
		
		Integer alpha = null;
		if (symbolData != null) {
			alpha = symbolData.getFillColor().getAlpha();
		}

		boolean isCloned = false;

		SymbolModifierParams standardSymbolOptions;
		for (int i = 0; i < mord.length; i++) {
			int ind = mord[i];
			if (ind >= modifiers.size()) {
				continue;
			}
			SymbolModifier modifier = modifiers.get(ind);
//			standardSymbolOptions = options.getModifierParams(modifier.getName());
			standardSymbolOptions = modifier.getSymbolModifierParams(); 

			int modBits = standardSymbolOptions.modBitMap;
			
			//System.out.println("StandardSymbolChooser: modifier: " + modifier .getName()  + "  " + modBits + " StandardSymbolOptions class: " +options);

			if (modBits == 0 || !options.isEnabled(this)[mord[i]]) {
				continue;
			}
			// clone the first mod so that it doesn't overwrite the fixed symbol
			if (isCloned == false) {
				symbolData = symbolData.clone();
				isCloned = true;
			}
			symbolData = modifiers.get(ind).modifySymbol(symbolData, projector, dataUnit);
		}
		if (alpha != null && alpha < 255) {
			Color currCol = symbolData.getFillColor();
			currCol = new Color(currCol.getRed(), currCol.getGreen(), currCol.getBlue(), alpha);
			symbolData.setFillColor(currCol);
		}
		return symbolData;
	}

	public ArrayList<JMenuItem> getQuickMenuItems(Window frame, SymbolUpdateMonitor component, int whatCanChange, 
			boolean andDetails, boolean andSuperDetoptions) {
		return getQuickMenuItems(frame, component, null, whatCanChange, andDetails);
	}
	/**
	 * Get some easy to use menu items which can be incorporated into a Swing (or converted to FX) 
	 * menu, e.g. in the click detector where we want to still quickly flick between the different
	 * colour options. 
	 * @return
	 */
	public ArrayList<JMenuItem> getQuickMenuItems(Window frame, SymbolUpdateMonitor component, String prefix, int whatCanChange, 
			boolean andDetails) {
		ArrayList<SymbolModifier> modifiers = getSymbolModifiers();
		@SuppressWarnings("unchecked")
		ArrayList<JMenuItem> items = new ArrayList();
		StandardSymbolOptions options = getSymbolOptions(); // use these, not direct call, since it's overridded in some classes. 
		int[] order = options.getModifierOrder(this);
		int n = Math.min(order.length, modifiers.size());
		if (prefix != null && prefix.endsWith(" ") == false) {
			prefix += " ";
		}
		if (prefix == null) {
			prefix = "";
		}
		for (int i = 0; i < n; i++) {
			SymbolModifier mod = modifiers.get(order[i]);
//			SymbolModifierParams params = options.getModifierParams(mod.getName());
			SymbolModifierParams params = mod.getSymbolModifierParams(); 
			JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(prefix + mod.getName());
			if ((params.modBitMap & whatCanChange) != 0) {
				menuItem.setSelected(true);
			}
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectSymbolModifier(e, component, mod, whatCanChange);
				}
			});
			items.add(menuItem);
		}
//		if (andSuperDetoptions) {
//			List<JMenuItem> moreItems = GlobalSymbolManager.getInstance().getSuperDetMenuItems(getPamDataBlock(), 
//					this.getDisplayName(), getProjector(), component);
//			if (moreItems != null && moreItems.size() > 0) {
//				JMenu superMenu = new JMenu("Super detection options");
//				for (JMenuItem aSupIt : moreItems) {
//					superMenu.add(aSupIt);
//				}
//				items.add(superMenu);
//			}
//		}
		if (andDetails) {
			JMenuItem detMenu = new JMenuItem("More symbol options...");
			detMenu.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					fullsumbolOptionsDialog(frame, component);
				}
			});
			items.add(detMenu);
		}
		return items;
	}

	/**
	 * Called when a modifiers main menu item is selected. 
	 * <br> Its not very clear what to do here, whether to select just that one, or to toggle its
	 * state or what. Probably really need a new menu to pop up with checkboxes for the different
	 * options ? 
	 * Might override this for the click detector so that we can easily recreate the existing functionality 
	 * of the old system. 
	 * @param e
	 * @param component 
	 * @param modifier
	 * @param whatCanChange
	 */
	protected void selectSymbolModifier(ActionEvent e, SymbolUpdateMonitor component, SymbolModifier modifier, int whatCanChange) {
//		StandardSymbolOptions options = getSymbolOptions();
		SymbolModifierParams modParams = modifier.getSymbolModifierParams(); 
		boolean isSel = modParams.modBitMap > 0;
		if (isSel) {
			modParams.modBitMap = 0; // keep the shape (for click detector!). 
		}
		else {
			modParams.modBitMap = modifier.getModifyableBits(); // set everything.
		}
		component.symbolUpdate();
	}

	protected void fullsumbolOptionsDialog(Window frame, SymbolUpdateMonitor component) {
		GenericSwingDialog.showDialog(frame, "Symbol Options", getSwingOptionsPanel(null));
		component.symbolUpdate();
	}

	/**
	 * Create default symbol data. 
	 * @return default symvoil, data. 
	 */
	public SymbolData getDefaultSymbol() {
		if (defaultSymbol == null) {
			defaultSymbol = new SymbolData();
		}
		return defaultSymbol;
	}


	public boolean showLineLengthOption(GeneralProjector projector) {
		if (projector == null) return false;
		ParameterType[] paramTypes = projector.getParameterTypes();
		if (paramTypes == null) return false;
		if (paramTypes.length < 0) return false;
		return paramTypes[0] == ParameterType.LATITUDE;
	}

	@Override
	public SwingSymbolOptionsPanel getSwingOptionsPanel(GeneralProjector projector) {

		//		standardSymbolManager.addSymbolOption(StandardSymbolManager.HAS_SYMBOL);

		//		if (showLineLengthOption(projector)){
		//			standardSymbolManager.addSymbolOption(StandardSymbolManager.HAS_LINE_LENGTH);
		//		}
		//		else {
		//			standardSymbolManager.removeSymbolOption(StandardSymbolManager.HAS_LINE_LENGTH);
		//		}
		standardSymbolManager.addAnnotationModifiers(this);
		StandardSymbolOptionsPanel ssop = new StandardSymbolOptionsPanel(standardSymbolManager, this);

		return ssop;
	}

	@Override
	public FXSymbolOptionsPane getFXOptionPane(GeneralProjector projector) {

		standardSymbolManager.addSymbolOption(StandardSymbolManager.HAS_SYMBOL);

		if (showLineLengthOption(projector)){
			standardSymbolManager.addSymbolOption(StandardSymbolManager.HAS_LINE_LENGTH);
		}
		FXSymbolOptionsPane ssop = new StandardSymbolOptionsPane(standardSymbolManager, this);

		return ssop;
	}

	/**
	 * @return the symbolOptions
	 */
	@Override
	public StandardSymbolOptions getSymbolOptions() {
//		System.out.println("||||||||------Save symbol options!!!");
		//set the paramters so that they can be serialized. 
		ArrayList<SymbolModifier> modifiers = getSymbolModifiers();
		for (SymbolModifier symbolModifier: modifiers) {
			symbolOptions.setModifierParams(symbolModifier.getName(), symbolModifier.getSymbolModifierParams());
		}
		
		return symbolOptions;
	}

	/**
	 * Get the map line length. 
	 * @return the default map line length
	 */
	public double getMapLineLength() {
		return getSymbolOptions() .mapLineLength;
	}

	/**
	 * Show lines to detections that have a lat long (or range & bearing).
	 * Hiding these can make the map a lot less cluttered. 
	 * @return true if lines are to be drawn. 
	 */
	public boolean showLinesToLatLongs() {
		return !getSymbolOptions() .hideLinesWithLatLong;
	}

	@Override
	public void setSymbolOptions(PamSymbolOptions symbolOptions) {
//		System.out.println("|||||||------Set symbol options!!!");

		if (StandardSymbolOptions.class.isAssignableFrom(symbolOptions.getClass())) {
			this.symbolOptions = (StandardSymbolOptions) symbolOptions;
			
			//set the option in the symbol chooser. 
			ArrayList<SymbolModifier> modifiers = getSymbolModifiers();
			for (SymbolModifier symbolModifier: modifiers) {
				symbolModifier.setSymbolModifierParams(this.symbolOptions.getModifierParams(symbolModifier));
			}
		}
	}

	/**
	 * Get the symbol manager.
	 * @return the symbol manager
	 */
	public StandardSymbolManager getSymbolManager() {
		return standardSymbolManager;
	}


	//	public SymbolData colourByAnnotation(SymbolData symbolData, GeneralProjector projector, PamDataUnit dataUnit, AnnotationSymbolOptions annotationSymbolOptions) {
	//	if (dataUnit == null) {
	//		return symbolData;
	//	}
	//	if (getSymbolOptions().annotationChoice == null) {
	//		return symbolData;
	//	}
	//	DataAnnotation annotation = null;
	//	PamDataBlock dataBlock = dataUnit.getParentDataBlock();
	//	AnnotationHandler annotationHandler = dataBlock.getAnnotationHandler();
	//	if (annotationHandler == null) {
	//		return symbolData;
	//	}
	//	try {
	//		Class<?> anClass = Class.forName(getSymbolOptions().annotationChoice);
	//		DataAnnotationType<?> annotationType = annotationHandler.findAnnotationType(anClass);
	//		 annotation = dataUnit.findDataAnnotation(annotationType.getAnnotationClass());
	//	} catch (ClassNotFoundException e1) {
	//		return symbolData;
	//	}
	//	if (annotation == null) {
	//		return symbolData;
	//	}
	//	DataAnnotationType<?> annotationType = annotation.getDataAnnotationType();
	//	if (annotationType == null) {
	//		return symbolData;
	//	}
	//	AnnotationSymbolChooser symbolChooser = annotationType.getSymbolChooser();
	//	if (symbolChooser == null) {
	//		return symbolData;
	//	}
	//	SymbolData data = symbolChooser.getSymbolData(dataUnit, symbolData, annotation, annotationSymbolOptions);
	//	
	//	return data != null ? data : symbolData;
	//}

	///**
	// * Get the channel 
	// * @param dataUnit
	// * @return
	// */
	//private Color  getChanColor(PamDataUnit dataUnit){
	////	int chn = PamUtils.getLowestChannel(dataUnit.getChannelBitmap());
	//	int chn = 0;
	//	if (dataUnit != null) {
	//		chn = PamUtils.getLowestChannel(dataUnit.getSequenceBitmap());
	//	}
	//	if (chn >= 0) return PamColors.getInstance().getChannelColor(chn);
	//	return null;
	//}

	//private SymbolData colourByHydrophone(SymbolData symbolData, PamDataUnit dataUnit) {
	//	symbolData = symbolData.clone();
	//	Color col = getChanColor(dataUnit);
	//	if (col!=null) {
	//		symbolData.setFillColor(col);
	//		symbolData.setLineColor(col);
	//	}
	//	return symbolData;
	//}
	//
	//public SymbolData colourBySpecial(SymbolData symbolData, GeneralProjector projector, PamDataUnit dataUnit) {
	//	return symbolData;
	//}
	//
	//public LineData colourBySpecial(LineData lineData, GeneralProjector projector, PamDataUnit dataUnit) {
	//	return lineData;
	//}


	//	/**
	//	 * 
	//	 * @param symbolData
	//	 * @param projector
	//	 * @param dataUnit
	//	 * @return
	//	 */
	//	@Deprecated
	//	public SymbolData colourBySuperDetection(SymbolData symbolData, GeneralProjector projector, PamDataUnit dataUnit) {
	//		SymbolData superSymbol = getSuperSymbol( projector,  dataUnit);
	//
	//		if (superSymbol==null){
	//			return symbolData;
	//		}
	//		
	//		else {
	//			symbolData = symbolData.clone();
	//			symbolData.setFillColor(superSymbol.getFillColor());
	//			symbolData.setLineColor(superSymbol.getLineColor());
	//		}
	//		
	//		return symbolData;
	//	}


}
