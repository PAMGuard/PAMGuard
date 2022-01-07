package PamguardMVC.dataSelector;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;

/**
 * Dialog panel to wrap around a standard dialog panel from a data selector. 
 * This will pull apart a bit the existing panel and steal it's border. It
 * will also add some and / or buttons. <p>
 * Data selectors should still make a simple dialog panel and that will be 
 * automatically wrapped by one of these beasts. 
 * @author Dougl
 *
 */
public class DataSelectorDialogPanel implements PamDialogPanel {

	private DataSelector dataSelector;
	private PamDialogPanel innerPanel;
	private int setIndex;
	
	private JPanel dsPanel;
	private JRadioButton andButton, orButton, disableButton;
	private JPanel optPanel;

	/**
	 * Create a panel to sit around a data selector dialog panel. the main purpose
	 * of this is to add the extra AND OR Skip controls if they are needed. Thier 
	 * nature is controlled by the setIndex. If this is 0, then the options are Enable / skip, 
	 * if setIndex > 0, then the options are AND OR SKIP, if it's < 0, then these options are
	 * not shown and the combination flag is set to AND. This is needed for SuperDetPanels, which 
	 * wrap an existing panel which should not use thes flags, ie. we don't want AND OR options
	 * in the superdetection decorator and in the superdeteciton dialog. 
	 * @param dataSelector Owning data selector
	 * @param innerPanel Dialog panel, which probably shouldn't have AND OR buttons and 
	 * @param setIndex selection index - see above. 
	 */
	public DataSelectorDialogPanel(DataSelector dataSelector, PamDialogPanel innerPanel, int setIndex) {
		this.dataSelector = dataSelector;
		this.innerPanel = innerPanel;
		this.setIndex = setIndex;
		dsPanel = new JPanel(new BorderLayout());
		JComponent dialogComponent = innerPanel.getDialogComponent();
		Border exBorder = dialogComponent.getBorder();
		if (exBorder instanceof TitledBorder) {
			// steal it. 
			dialogComponent.setBorder(null);
//			innerPanel.getDialogComponent().setBorder(new BevelBorder(BevelBorder.LOWERED));
			dsPanel.setBorder(exBorder);
		}
		else {
			dsPanel.setBorder(new TitledBorder(dataSelector.getSelectorTitle()));
		}
		dsPanel.add(BorderLayout.CENTER, dialogComponent);
		optPanel = new JPanel(new GridBagLayout());
//		optPanel.setBorder(BorderFactory.createEtchedBorder());
		GridBagConstraints c = new PamGridBagContraints();
		andButton = new JRadioButton(setIndex == 0 ? "Enable" : "AND");
		orButton = new JRadioButton("OR");
		disableButton = new JRadioButton("Skip");
		ButtonGroup bg = new ButtonGroup();
		bg.add(andButton);
		bg.add(orButton);
		bg.add(disableButton);
		EnableAction en = new EnableAction();
		andButton.addActionListener(en);
		orButton.addActionListener(en);
		disableButton.addActionListener(en);
		optPanel.add(andButton, c);
		c.gridx++;
		optPanel.add(orButton, c);
		c.gridx++;
		optPanel.add(disableButton, c);
		c.gridx++;
		dsPanel.add(optPanel, BorderLayout.NORTH);
		orButton.setVisible(setIndex>0);
		if (dataSelector instanceof CompoundDataSelector || setIndex < 0) {
			optPanel.setVisible(false);
			dsPanel.setBorder(null);
		}
		optPanel.setToolTipText("Options for " + dataSelector.getLongSelectorName());
	}

	@Override
	public JComponent getDialogComponent() {
		return dsPanel;
	}
	
	private class EnableAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			enableComponent();
		}
		
	}

	@Override
	public void setParams() {
		DataSelectParams params = dataSelector.getParams();
		if (params == null) {
			return;
		}
		if (optPanel.isVisible()) {
			andButton.setSelected(params.getCombinationFlag() == DataSelectParams.DATA_SELECT_AND);
			orButton.setSelected(params.getCombinationFlag() == DataSelectParams.DATA_SELECT_OR);
			disableButton.setSelected(params.getCombinationFlag() == DataSelectParams.DATA_SELECT_DISABLE);
		}
		else {
			andButton.setSelected(true);
			orButton.setSelected(false);
			disableButton.setSelected(false);
		}
		
		innerPanel.setParams();
		enableComponent();
	}

	public void enableComponent() {
		boolean enable = !disableButton.isSelected();
		innerPanel.getDialogComponent().setEnabled(enable);
	}

	@Override
	public boolean getParams() {
		DataSelectParams params = dataSelector.getParams();
		
		if (disableButton.isSelected()) {
			if (params != null) {
				params.setCombinationFlag(DataSelectParams.DATA_SELECT_DISABLE);
			}
			return true;
		}
		boolean inOk = innerPanel.getParams();
		if (inOk == false) { // why go on when it's futile ? 
			return false;
		}
		params = dataSelector.getParams(); // might have made a new object by in call to innerPanel!
		if (params != null) {
			if (andButton.isSelected()) {
				params.setCombinationFlag(DataSelectParams.DATA_SELECT_AND);
			}
			else if (orButton.isSelected()) {
				params.setCombinationFlag(DataSelectParams.DATA_SELECT_OR);
			}
			else if (disableButton.isSelected()) {
				params.setCombinationFlag(DataSelectParams.DATA_SELECT_DISABLE);
			}
		}
		return inOk;
	}

}
