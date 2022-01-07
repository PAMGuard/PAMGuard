package PamView.paneloverlay;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelector;

/**
 * Standard swing panel for selecting overlay mark information. 
 * This includes a column of check boxes for selecting data and where they 
 * have a data selector, additional buttons for fine scale data selection. 
 * @author dg50
 *
 */
public class OverlaySwingPanel implements PamDialogPanel {

	private OverlayDataManager overlayDataManager;

	private JPanel swingPanel;

	private List<PamDataBlock> dataList;

	protected JCheckBox[] selCheckBoxes;

	private Window parentWindow;

	private JButton[] settingButtons;

	private int firstFreeGridColumn = 2;
	
	/**
	 * @param overlayDataManager
	 */
	public OverlaySwingPanel(OverlayDataManager overlayDataManager, Window parentWindow) {
		super();
		this.overlayDataManager = overlayDataManager;
		this.parentWindow = parentWindow;
		swingPanel = new JPanel(new GridBagLayout());
	}
	
	/**
	 * @return the index of the first free column in the gridbag layout.
	 */
	public int getFirstFreeColumn() {
		return firstFreeGridColumn;
	}

	/**
	 * @return the main panel components are added to. 
	 */
	public JPanel getSwingPanel() {
		return swingPanel;
	}
	@Override
	public JComponent getDialogComponent() {
		return swingPanel;
	}

	@Override
	public void setParams() {
		dataList = overlayDataManager.listDataBlocks(true);
		createCheckboxes();
		for (int i = 0; i < dataList.size(); i++) {
			OverlayDataInfo dataInfo = overlayDataManager.getOverlayInfo(dataList.get(i));
			if (dataInfo != null) {
				selCheckBoxes[i].setSelected(dataInfo.select);
			}
		}
		enableControls();
	}

	protected void createCheckboxes() {
		swingPanel.removeAll();
		GridBagConstraints c = new PamGridBagContraints();
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.NONE;
		selCheckBoxes = new JCheckBox[dataList.size()];
		settingButtons = new JButton[dataList.size()];
		ChangeCheck changeCheck = new ChangeCheck();
		c.gridx = 0;
		c.gridy = 0;

		c.anchor = GridBagConstraints.EAST;
		swingPanel.add(new JLabel("Data name", JLabel.RIGHT), c);
		c.gridx++;
		swingPanel.add(new JLabel(" Select ", JLabel.CENTER), c);
		JLabel label;
		for (int i = 0; i < dataList.size(); i++) {
			PamDataBlock dataBlock = dataList.get(i);
			c.gridx = 0;
			c.gridy++;
			c.anchor = GridBagConstraints.EAST;
			swingPanel.add(label = new JLabel(dataBlock.getDataName(), JLabel.RIGHT), c);
			label.setToolTipText(dataBlock.getParentProcess().getProcessName() + ":" + dataBlock.getDataName());
			c.gridx++;
			c.anchor = GridBagConstraints.CENTER;
			swingPanel.add(selCheckBoxes[i] = new JCheckBox(), c);
			selCheckBoxes[i].addActionListener(changeCheck);
			selCheckBoxes[i].setToolTipText("Select " + dataBlock.getParentProcess().getProcessName() + ":" + dataBlock.getDataName());
			c.gridx+=20; // allow space for people to put stuff between in subclasses. 
			DataSelector dataSel = dataBlock.getDataSelector(overlayDataManager.getDataSelectorName(), true);
			if (dataSel != null) {
				settingButtons[i] = dataSel.getDialogButton(parentWindow);
				swingPanel.add(settingButtons[i], c);
			}
		}
	}
	
	class ChangeCheck implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
		
	}
	
	/**
	 * Enable all the settings buttons based on checkbox state. 
	 */
	public void enableControls() {
		if (selCheckBoxes == null) {
			return;
		}
		for (int i = 0; i < selCheckBoxes.length; i++) {
			if (settingButtons[i] != null) {
				boolean sel = selCheckBoxes[i].isSelected();
				settingButtons[i].setEnabled(sel);
			}
		}
	}

	@Override
	public boolean getParams() {
		int nSels = 0;
		for (int i = 0; i < dataList.size(); i++) {
			OverlayDataInfo dataInfo = overlayDataManager.getOverlayInfo(dataList.get(i));
			if (dataInfo != null) {
				dataInfo.select = selCheckBoxes[i].isSelected();
				if (dataInfo.select) {
					nSels++;
				}
			}
		}
		return (nSels > 0);
	}

	/**
	 * @return the dataList
	 */
	protected List<PamDataBlock> getDataList() {
		return dataList;
	}

	/**
	 * @return the overlayDataManager
	 */
	protected OverlayDataManager getOverlayDataManager() {
		return overlayDataManager;
	}

}
