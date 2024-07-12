package PamguardMVC.dataSelector;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JPanel;

import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import javafx.scene.Node;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

public class CompoundDialogPaneFX extends DynamicSettingsPane<Boolean> {
	

	private CompoundDataSelector compoundDataSelector;
	
	private ArrayList<DataSelector> selectorList;
	private ArrayList<PamDialogPanel> selectorPanels;
	
	private PamVBox mainPanel;

	public CompoundDialogPaneFX(CompoundDataSelector compoundDataSelector) {
		this.compoundDataSelector = compoundDataSelector;
		this.selectorList = compoundDataSelector.getSelectorList();
		
		mainPanel = new PamVBox();
		mainPanel.setSpacing(5);
		selectorPanels = new ArrayList<PamDialogPanel>(selectorList.size());
		int ind = 0;
		for (DataSelector ds : selectorList) {
			DynamicSettingsPane<Boolean> panel = ds.getDialogPaneFX();
			// turn all these panels into the compound ones with the extra enable options. 
			
//			DataSelectorDialogPanel dsp = new DataSelectorDialogPanel(ds, panel, ind++);
//			selectorPanels.add(dsp);
			
			mainPanel.getChildren().add(panel.getContentNode());
		}
	}


	@Override
	public Boolean getParams(Boolean currParams) {
		boolean ok = true;
		for (int i = 0; i < selectorPanels.size(); i++) {
			PamDialogPanel panel = selectorPanels.get(i);
			ok |= panel.getParams();
		}
		return ok;
	}

	@Override
	public void setParams(Boolean input) {
		for (int i = 0; i < selectorPanels.size(); i++) {
			PamDialogPanel panel = selectorPanels.get(i);
			panel.setParams();
		}
	}

	@Override
	public String getName() {
		return "Compound data selector pane";
	}

	@Override
	public Node getContentNode() {
		return mainPanel;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

}
