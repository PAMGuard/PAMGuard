package PamView.symbol.modifier;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import PamView.GeneralProjector;
import PamView.dialog.PamDialogPanel;
import PamView.symbol.SymbolData;
import PamView.symbol.legend.SymbolLegend;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import pamViewFX.symbol.SymbolModifierPane;

/**
 * Wraps a symbol modifier stolen from a super detection in order to pass the super 
 * detection into the symbol modifier rather than the original sub detection. 
 * @author Doug Gillespie
 *
 */
public class SuperDetSymbolWrapper extends SymbolModifier {
	
	private PamDataBlock superDetDataBlock;
	
	private SymbolModifier superDetModifier;

	public SuperDetSymbolWrapper(PamDataBlock superDetDataBlock, SymbolModifier superDetModifier) {
		super(superDetModifier.getName() + " (" + superDetDataBlock.getDataName() + ")", 
				superDetModifier.getSymbolChooser(), superDetModifier.getModifyableBits());
		this.superDetDataBlock = superDetDataBlock;
		this.superDetModifier = superDetModifier;
	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		PamDataUnit superDet = dataUnit.getSuperDetection(superDetDataBlock, true);
		if (superDet == null) {
			return null;
		}
		return superDetModifier.getSymbolData(getSymbolChooser().getProjector(), superDet);
	}
	

	/**
	 * The legend of the wrapped super detection modifier but with this modifier's
	 * name, so it's clear it applies to the super detection. Never modifies all
	 * data since data units which aren't part of a super detection are left alone.
	 */
	@Override
	public SymbolLegend getSymbolLegend() {
		SymbolLegend legend = superDetModifier.getSymbolLegend();
		if (legend != null) {
			legend.setTitle(getName());
		}
		return legend;
	}

	@Override
	public String getToolTipText() {
		return superDetModifier.getToolTipText();
	}

	@Override
	public int getModifyableBits() {
		return superDetModifier.getModifyableBits();
	}

	@Override
	public boolean canModify(int modType) {
		return superDetModifier.canModify(modType);
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		return superDetModifier.getDialogPanel();
	}

	@Override
	public JMenuItem getModifierOptionsMenu() {
		return superDetModifier.getModifierOptionsMenu();
	}

	@Override
	protected void showOptionsDialog(ActionEvent e, PamDialogPanel dialogPanel) {
		superDetModifier.showOptionsDialog(e, dialogPanel);
	}

	@Override
	public SymbolModifierPane getOptionsPane() {
		return superDetModifier.getOptionsPane();
	}

	@Override
	public SymbolModifierParams getSymbolModifierParams() {
		return superDetModifier.getSymbolModifierParams();
	}

	@Override
	public void setSymbolModifierParams(SymbolModifierParams symbolModifierParams) {
		superDetModifier.setSymbolModifierParams(symbolModifierParams);
	}

	@Override
	public void setSelectionBit(int selectionBit, boolean selected) {
		superDetModifier.setSelectionBit(selectionBit, selected);
	}

	@Override
	public void setToolTipText(String tip) {
		superDetModifier.setToolTipText(tip);
	}

}
