package clickDetector.tdPlots;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.GeneralProjector;
import PamView.PamSymbol;
import PamView.dialog.PamDialogPanel;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.legend.LegendSymbolData;
import PamView.symbol.legend.SymbolLegend;
import PamView.symbol.modifier.SymbolModType;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickControl;
import clickDetector.ClickDetection;
import clickDetector.ClickClassifiers.ClickIdentifier;

public class ClickClassSymbolModifier extends SymbolModifier {

	private ClickControl clickControl;

	public ClickClassSymbolModifier(ClickControl clickControl, PamSymbolChooser symbolChooser) {
		super("Click Classification", symbolChooser, SymbolModType.EVERYTHING);
		this.clickControl = clickControl;
		getSymbolModifierParams().modBitMap = SymbolModType.SHAPE;
//		setModifyableBits(SymbolModType.SHAPE);
	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		PamSymbol symbol=clickControl.getClickIdentifier().getSymbol((ClickDetection) dataUnit);
		if (symbol != null) {
			return symbol.getSymbolData();
		}
		else {
			return null;
		}
	}

	/**
	 * Legend with a symbol for each click type. Unclassified clicks are not
	 * modified, so this never modifies all clicks.
	 */
	@Override
	public SymbolLegend getSymbolLegend() {
		SymbolLegend legend = new SymbolLegend(getName());
		ClickIdentifier clickIdentifier = clickControl.getClickIdentifier();
		if (clickIdentifier == null) {
			return legend;
		}
		String[] speciesList = clickIdentifier.getSpeciesList();
		PamSymbol[] symbols = clickIdentifier.getSymbols();
		if (speciesList == null || symbols == null) {
			return legend;
		}
		int n = Math.min(speciesList.length, symbols.length);
		for (int i = 0; i < n; i++) {
			if (symbols[i] == null) {
				continue;
			}
			legend.addSymbol(new LegendSymbolData(symbols[i].getSymbolData(), speciesList[i]));
		}
		return legend;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		// Just a play to check buttons and menus work. Not actually used at all so revert to super: returns null.
		return super.getDialogPanel();
//		return new DumyPanel();
	}
	
	private class DumyPanel implements PamDialogPanel {

		private JPanel mainPanel;
		public DumyPanel() {
			super();
			mainPanel = new JPanel();
			mainPanel.setBorder(new TitledBorder("More options"));
			mainPanel.add(new JCheckBox("Dummy option"));
		}

		@Override
		public JComponent getDialogComponent() {
			// TODO Auto-generated method stub
			return mainPanel;
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

}
