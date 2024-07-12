package PamguardMVC.dataSelector;

import java.util.ArrayList;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

public class CompoundDataSelector extends DataSelector {

	private ArrayList<DataSelector> selectorList;
	
	private CompoundParams compoundParams = new CompoundParams();

	private CompoundDialogPaneFX compoundPaneFX;

	public CompoundDataSelector(PamDataBlock pamDataBlock, ArrayList<DataSelector> allSelectors,
			String selectorName, boolean allowScores, String selectorType) {
		super(pamDataBlock, selectorName, allowScores);
		this.selectorList = allSelectors;
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		if (dataSelectParams instanceof CompoundParams == false) {
			return;
		}
		this.compoundParams = (CompoundParams) dataSelectParams;
		for (DataSelector selector : selectorList) {
			DataSelectParams sp = compoundParams.getSelectorParams(selector);
			if (sp != null) {
//				sp = selector.get
				try {
					selector.setParams(sp);
				}
				catch (ClassCastException e) {
					System.out.printf("Cannot cast parameter type for data selector %s: %s", selector.getLongSelectorName(), e.getMessage());
				}
			}
		}
	}

	@Override
	public DataSelectParams getParams() {
		if (compoundParams == null) {
			compoundParams = new CompoundParams();
		}
		for (DataSelector selector : selectorList) {
			compoundParams.setSelectorParams(selector, selector.getParams());
		}
		return compoundParams;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		return new CompoundDialogPanel(this);
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		if (compoundPaneFX==null) {
			compoundPaneFX = new CompoundDialogPaneFX(this);
		}
		return compoundPaneFX;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
//		if (getParams().getCombinationFlag() == DataSelectParams.DATA_SELECT_DISABLE) {
		/*
		 * Don't want this in compound data selector. Just do what the sub-selectors tell you to 
		 */
//			return 1;
//		}
		double score = 1.; // default in case no selectors are active.  
		boolean first = true;
		for (int i = 0; i < selectorList.size(); i++) {
			DataSelector subSelector = selectorList.get(i);
			DataSelectParams subParams = subSelector.getParams();
			int combinFlag = subParams.getCombinationFlag();
			if (combinFlag == DataSelectParams.DATA_SELECT_DISABLE) {
				continue;
			}
			double score2 = selectorList.get(i).scoreData(pamDataUnit);
			if (first) {
				// first one gets whatever score is returned from the first used selector. 
				score = score2;
				first = false;
			}
			else if (combinFlag == DataSelectParams.DATA_SELECT_AND) {
				score = Math.min(score,  score2); // take the lowest
			}
			else if (combinFlag == DataSelectParams.DATA_SELECT_OR) {
				score = Math.max(score,  score2); // take the largest
			}
		}
		return score;
	}

	/**
	 * @return the selectorList
	 */
	public ArrayList<DataSelector> getSelectorList() {
		return selectorList;
	}

}
