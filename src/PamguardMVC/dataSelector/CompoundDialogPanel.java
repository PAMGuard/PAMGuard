package PamguardMVC.dataSelector;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;

public class CompoundDialogPanel implements PamDialogPanel {

	private CompoundDataSelector compoundDataSelector;
	private ArrayList<DataSelector> selectorList;
	private ArrayList<PamDialogPanel> selectorPanels;
	
	private JPanel mainPanel;
	
	public CompoundDialogPanel(CompoundDataSelector compoundDataSelector) {
		this.compoundDataSelector = compoundDataSelector;
		this.selectorList = compoundDataSelector.getSelectorList();
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.;
		c.gridx = 0;
		selectorPanels = new ArrayList<PamDialogPanel>(selectorList.size());
		int ind = 0;
		for (DataSelector ds : selectorList) {
			PamDialogPanel panel = ds.getDialogPanel();
			// turn all these panels into the compound ones with the extra enable options. 
			DataSelectorDialogPanel dsp = new DataSelectorDialogPanel(ds, panel, ind++);
			selectorPanels.add(dsp);
			mainPanel.add(dsp.getDialogComponent(), c);
			c.gridy++;
		}
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		for (int i = 0; i < selectorPanels.size(); i++) {
			PamDialogPanel panel = selectorPanels.get(i);
			panel.setParams();
		}
	}

	@Override
	public boolean getParams() {
		boolean ok = true;
		for (int i = 0; i < selectorPanels.size(); i++) {
			PamDialogPanel panel = selectorPanels.get(i);
			ok |= panel.getParams();
		}
		return ok;
	}

}
