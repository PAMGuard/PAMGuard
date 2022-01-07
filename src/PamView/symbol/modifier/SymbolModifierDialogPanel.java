package PamView.symbol.modifier;

import javax.swing.JComponent;

import PamView.dialog.PamDialogPanel;

public class SymbolModifierDialogPanel implements PamDialogPanel {

	private SymbolModifier symbolModifier;

	public SymbolModifierDialogPanel(SymbolModifier symbolModifier) {
		this.symbolModifier = symbolModifier;
	}

	@Override
	public JComponent getDialogComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParams() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return false;
	}

}
