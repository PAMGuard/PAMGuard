package PamguardMVC.dataSelector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import PamView.GeneralProjector;
import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.panel.PamAlignmentPanel;
import PamView.panel.PamNorthPanel;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.PamSymbolManager;
import PamguardMVC.PamDataBlock;

public class DataSelectDialog extends PamDialog {

	private DataSelector dataSelector;
	private boolean cancelled;
	private PamSymbolChooser symbolChooser;
	private PamDialogPanel dataPanel;
	private PamDialogPanel symbolPanel;
	private JTabbedPane tabPane;
	private static int currentTab = 0;
	
	private static final String helpPoint = "displays.dataselect.docs.selectandsymbol";

	public DataSelectDialog(Window parentFrame, PamDataBlock pamDataBlock, DataSelector dataSelector, PamSymbolChooser symbolChooser) {
		super(parentFrame, (pamDataBlock==null) ? "Data Selection":pamDataBlock.getDataName(), false);
		this.dataSelector = dataSelector;
		this.symbolChooser = symbolChooser;
		tabPane = new JTabbedPane();
		if (dataSelector != null) {
			dataPanel = dataSelector.getDialogPanel();
			if (dataPanel instanceof DataSelectorDialogPanel == false && dataSelector instanceof CompoundDataSelector == false) {
				dataPanel = new DataSelectorDialogPanel(dataSelector, dataPanel, 0);
			}
			JComponent dsComponent = dataPanel.getDialogComponent();
			if (dsComponent.getPreferredSize().height > 400 || dataSelector instanceof CompoundDataSelector) {
				JScrollPane scrollPane = new JScrollPane(dsComponent, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
						JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				dsComponent = scrollPane;
			}

			tabPane.add("Data", new PamNorthPanel(dsComponent));
		}
		if (symbolChooser != null) {
			symbolPanel = symbolChooser.getSwingOptionsPanel(symbolChooser.getProjector());
			tabPane.addTab("Symbols", new PamNorthPanel(symbolPanel.getDialogComponent()));
		}
		setDialogComponent(tabPane);
		if (tabPane.getComponentCount() > currentTab) {
			tabPane.setSelectedIndex(currentTab);
		}
		
		setResizable(true);
		
		setHelpPoint(helpPoint);
//		PamSymbolManager symbolManager = dataSelector.getPamDataBlock().getPamSymbolManager();
//		if (symbolManager != null) {
//			PamSymbolChooser symbolChooser = symbolManager.getSymbolChooser(dataSelector.getSelectorName(), null);
//			tabPane.add("Data", dataSelector.getDialogPanel().getDialogComponent());
//			tabPane.addTab("Symbols", symbolChooser.getSwingOptionsPanel(dataSelector.getSelectorName(), null).getDialogComponent());
//			setDialogComponent(tabPane);
//		}
//		else{
//			setDialogComponent(dataSelector.getDialogPanel().getDialogComponent());
//		}
	}
	
	public boolean setTab(int tabIndex) {
		if (tabPane == null) {
			return false;
		}
		if (tabIndex < 0 || tabIndex >= tabPane.getTabCount()) {
			return false;
		}
		try {
			tabPane.setSelectedIndex(tabIndex);
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public boolean showDialog() {
		if (dataPanel != null) {
			dataPanel.setParams();
			dataPanel.getDialogComponent().invalidate();
		}
		if (symbolPanel != null) {
			symbolPanel.setParams();
		}
		cancelled = false;
		setVisible(true);
		this.pack();
		return (cancelled == false);
	}

	@Override
	public boolean getParams() {
		boolean ok = false;
		if (dataPanel != null) {
			if(!dataPanel.getParams()) {
				return false;
			}
		}
		if (symbolPanel != null) {
			if (!symbolPanel.getParams()) {
				return false;
			}
		}
		currentTab = tabPane.getSelectedIndex();
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		cancelled = true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
