package clickDetector.dataSelector;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import clickDetector.ClickControl;
import clickDetector.offlineFuncs.ClicksOffline;
import generalDatabase.lookupTables.LookUpTables;
import generalDatabase.lookupTables.LookupList;

public class ClickTrainSelectPanel2 implements PamDialogPanel {

	private  ClickTrainDataSelector2 clickTrainDataSelect2;
	
	private JPanel mainPanel;

	private ClickControl clickControl;
	
	private JCheckBox unclassified;
	
	private JCheckBox[] trainTypes;

	private LookupList lutList;
	
	/**
	 * @param clicTrainDataSelect2
	 */
	public ClickTrainSelectPanel2(ClickControl clickControl, ClickTrainDataSelector2 clickTrainDataSelect2) {
		super();
		this.clickControl = clickControl;
		this.clickTrainDataSelect2 = clickTrainDataSelect2;
		mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("Click Train Types"));
		mainPanel.setLayout(new GridBagLayout());
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		ClickTrainDataSelect2Params params = clickTrainDataSelect2.getParams();
		mainPanel.removeAll();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(unclassified = new JCheckBox("Clicks that are not in a click train"), c);
		unclassified.setSelected(params.isIncludeUnclassified());

		lutList = LookUpTables.getLookUpTables().getLookupList(ClicksOffline.ClickTypeLookupName);
		if (lutList == null) {
			return;
		}
		trainTypes = new JCheckBox[lutList.getList().size()];
		for (int i = 0; i < trainTypes.length; i++) {
			c.gridy++;
			mainPanel.add(trainTypes[i] = new JCheckBox(lutList.getList().get(i).getText()), c);
			trainTypes[i].setSelected(params.isWantType(lutList.getList().get(i).getCode()));
		}
	}

	@Override
	public boolean getParams() {
		ClickTrainDataSelect2Params params = clickTrainDataSelect2.getParams();
		params.setIncludeUnclassified(unclassified.isSelected());
		if (lutList != null) {
			for (int i = 0; i < lutList.getList().size(); i++) {
				boolean want = trainTypes[i].isSelected();
				params.setWantType(lutList.getList().get(i).getCode(), want);
			}
		}
		return true;
	}

}
