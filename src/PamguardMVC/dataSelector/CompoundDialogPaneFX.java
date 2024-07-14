package PamguardMVC.dataSelector;

import java.util.ArrayList;

import javafx.scene.Node;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;


/**
 * shows multiple data selectors in a pane. This would be used, for example, in a situation where
 * a data unit has multiple associated data selectors, for example if is annotated with an annotation 
 * that has a data selector. 
 */
public class CompoundDialogPaneFX extends DynamicSettingsPane<Boolean> {
	

	private CompoundDataSelector compoundDataSelector;
	
	private ArrayList<DataSelector> selectorList;
	private ArrayList<DataSelectorDialogPaneFX> selectorPanels;
	
	private PamVBox mainPanel;

	public CompoundDialogPaneFX(CompoundDataSelector compoundDataSelector) {
		super(null);

		this.compoundDataSelector = compoundDataSelector;
		this.selectorList = compoundDataSelector.getSelectorList();
		
		mainPanel = new PamVBox();
		mainPanel.setSpacing(10);
		selectorPanels = new ArrayList<DataSelectorDialogPaneFX>(selectorList.size());
		int ind = 0;
		for (DataSelector ds : selectorList) {
			DynamicSettingsPane<Boolean> panel = ds.getDialogPaneFX();
			// turn all these panels into the compound ones with the extra enable options. 
			
			DataSelectorDialogPaneFX dsp = new DataSelectorDialogPaneFX(ds, panel, ind++);
			selectorPanels.add(dsp);
			
			mainPanel.getChildren().add(dsp.getContentNode());
		}
	}


	@Override
	public Boolean getParams(Boolean currParams) {
		boolean ok = true;
		for (int i = 0; i < selectorPanels.size(); i++) {
			DataSelectorDialogPaneFX panel = selectorPanels.get(i);
			ok |= panel.getParams(currParams);
		}
		return ok;
	}

	@Override
	public void setParams(Boolean input) {
		for (int i = 0; i < selectorPanels.size(); i++) {
			DataSelectorDialogPaneFX panel = selectorPanels.get(i);
			panel.setParams(input);
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
