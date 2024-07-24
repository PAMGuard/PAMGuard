package PamView.symbol.modifier;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import PamController.PamController;
import PamView.GeneralProjector;
import PamView.dialog.GenericSwingDialog;
import PamView.dialog.PamDialogPanel;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;
import pamViewFX.symbol.SymbolModifierPane;

/**
 * A symbol modifier modifies a PamSymbol. SymbolModifiers are typically used in a list
 * where the user can define exactly how they wish symbols to be coloured, shaped based on 
 * various properties e.g. hydrophone, peak freq. super detection etc. 
 * 
 * 
 * @author Doug Gillespie
 *
 */
abstract public class SymbolModifier {

	/**
	 * The name of the symbol modifier. 
	 */
	private String name;

	/**
	 * The symbol chooser which uses the symbol modifier. 
	 */
	private PamSymbolChooser symbolChooser;

	/**
	 * Bitmap of the potential options which the symbol modifier can modify. 
	 * These are the available bits, not the ones actually modified. 
	 */
	private int modifyableBits;
	
	/**
	 * Parameters for this symbol modifier. 
	 */
	public SymbolModifierParams symbolModifierParams;

	private String toolTip;

	public SymbolModifier(String name, PamSymbolChooser symbolChooser, int modifyableBits) {
		super();
		this.name = name;
		this.symbolChooser = symbolChooser;
		this.modifyableBits = modifyableBits;
	}

	/**
	 * Bits that can be selected / modified. This is NOT the bits which 
	 * are currently selected for modification
	 * @return the modifyableBits
	 */
	public int getModifyableBits() {
		return modifyableBits;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * This is the symbol data to use if this modifier were to change everything. the Symbolchooser
	 * will actually be calling the modifySymbol function, which calls this, then based on which bits
	 * are set for modification, will transfer over the appropriate information. 
	 * <p>Must be able to respond to a null projector and dataUnit for tree vies. 
	 * @param projector
	 * @param dataUnit
	 * @return
	 */
	abstract public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit);

	/**
	 * Modify the symbol. In it's basic form it calls getSymbolData to get a totally fresh symbol, 
	 * then copies over only the bits of information specified in modifyBits
	 * @param symbolData
	 * @param projector
	 * @param dataUnit
	 * @param modifyBits
	 * @return modified symbol. Must never be null. 
	 */
	public SymbolData modifySymbol(SymbolData symbolData, GeneralProjector projector, PamDataUnit dataUnit) {
		
		int modifyBits = getSymbolModifierParams().modBitMap; 
		//System.out.println("SymbolModifier: " + name + " bits: "  + modifyBits +  " dataunit: " + dataUnit); 
		
		if (modifyBits == 0) {
			return symbolData;
		}
		SymbolData modData = getSymbolData(projector, dataUnit);
		if (modData == null) {
			return symbolData;
		}
		if ((modifyBits & SymbolModType.SHAPE) != 0) {
			symbolData.symbol = modData.symbol;
		}
		if ((modifyBits & SymbolModType.LINECOLOUR) != 0) {
			symbolData.setLineColor(modData.getLineColor());
		}
		if ((modifyBits & SymbolModType.FILLCOLOUR) != 0) {
			symbolData.setFillColor(modData.getFillColor());
			symbolData.fill = modData.fill;
		}
		return symbolData;
	}


	/**
	 * Say if can modify symbol in a particular way or ways .
	 * Note that this is what it CAN do, not necessarily what it's been told to do. 
	 * @param modType bitmap of modificatio types
	 * @return
	 */
	public boolean canModify(int modType) {
		return (getModifyableBits() & modType) != 0;
	}

	public PamDialogPanel getDialogPanel() {
		return null;
	}
	
	/**
	 * Get a menu item which can be added to options menu to control symbol modifier settings. 
	 * by default, the menu item is only created if the getDialogPanel function returns non null, but
	 * this behaviour can be overridden for more bespoke solutions such as simple additional menu items, etc. 
	 * @return null or a menu item 
	 */
	public JMenuItem getModifierOptionsMenu() {
		PamDialogPanel dialogPanel = getDialogPanel();
		if (dialogPanel == null) {
			return null;
		}
		JMenuItem menuItem = new JMenuItem("More options ...");
		menuItem.setToolTipText("More symbol options");
		menuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				showOptionsDialog(e, dialogPanel);
			}
		});
		return menuItem;
	}

	/**
	 * Default behaviour to show the dialog panel.
	 * @param e
	 * @param dialogPanel
	 */
	protected void showOptionsDialog(ActionEvent e, PamDialogPanel dialogPanel) {
		GenericSwingDialog.showDialog(PamController.getMainFrame(), getName() + " options", dialogPanel);
	}

	/**
	 * Get the JavaFX symbol options pane that has options for the symbol pane.
	 * @return the symbol options pane. 
	 */
	public SymbolModifierPane getOptionsPane() {
		return null; 
	}

	/**
	 * @return the symbolChooser
	 */
	public PamSymbolChooser getSymbolChooser() {
		return symbolChooser;
	}
	
	/**
	 * Get the symbol modifier parameters associated with the symbol modifier.  
	 * <p> Note that this function should create a new instance of the settings class if the 
	 * settings class is null. This allows subclasses to override this function to return custom
	 * parameter objects. 
	 * @return  the symbol modifier parameters. 
	 */
	public SymbolModifierParams getSymbolModifierParams() {
		if (symbolModifierParams==null) symbolModifierParams = new SymbolModifierParams(); 
		return symbolModifierParams;
	}

	/**
	 * Set the symbol modifier paramters. 
	 * @param symbolModifierParams - the symbol modifiers paramters. 
	 */
	public void setSymbolModifierParams(SymbolModifierParams symbolModifierParams) {
		this.symbolModifierParams = symbolModifierParams;
	}

	/**
	 * Set a selection bit to true. 
	 * @param selectionBit
	 * @param selected
	 */
	public void setSelectionBit(int selectionBit, boolean selected) {
		modifyableBits = modifyableBits | selectionBit; 
	}
	
	/**
	 * Get a tool tip to display in dialogs, menus, etc. 
	 * @return tip text
	 */
	public String getToolTipText() {
		if (toolTip != null) {
			return toolTip;
		}
		else {
			return getName();
		}
	}
	
	/**
	 * 
	 * Set a tool tip to display in dialogs, menus, etc. 
	 * @param tip tip text
	 */
	public void setToolTipText(String tip) {
		this.toolTip = tip;
	}


}
