package offlineProcessing.superdet;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import PamView.dialog.SettingsButton;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.superdet.SuperDetDataBlock;

public class OSDFSwingComponent {

	private OfflineSuperDetFilter osdFilter;

	private JPanel mainPanel;

	private JComboBox<String> dataList;

	private ArrayList<SuperDetDataBlock> dataBlocks;

	private SuperDetDataBlock currentSuperBlock;

	private DataSelector dataSelector;

	private Window parent;
	
	private JPanel buttonSpace;
		
	private SettingsButton nullButton = new SettingsButton();

	private JButton realButton;

	/**
	 * @param osdFilter
	 */
	public OSDFSwingComponent(Window parent, OfflineSuperDetFilter osdFilter) {
		super();
		this.parent = parent;
		this.osdFilter = osdFilter;
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Super Detection filter"));
		dataList = new JComboBox<>();
		dataList.addItem("== No filter ==");
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(dataList, c);
		dataBlocks = osdFilter.getAvailableSuperDetctors();
		for (PamDataBlock dataBlock : dataBlocks) {
			dataList.addItem(dataBlock.getLongDataName());
		}
		dataList.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				superDetSelected();
			}
		});
		c.gridx++;
		buttonSpace = new JPanel(new BorderLayout());
		mainPanel.add(buttonSpace, c);
		buttonSpace.add(BorderLayout.CENTER, nullButton);
		nullButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				settingsButton(e);
			}
		});
		
	}

	/**
	 * Settings button pressed
	 * @param e 
	 */
	private void settingsButton(java.awt.event.ActionEvent e) {
		/**
		 * Bit of a fudge since the dialog didn't like swapping buttons in and out. 
		 * Have therefore stuck with a single button, and when it's pressed it rund
		 * the actionlisteners of the 'real' button. 
		 */
		if (realButton == null) {
			return;
		}
		ActionListener[] als = realButton.getActionListeners();
		if (als == null) {
			return;
		}
		for (int i = 0; i < als.length; i++) {
			als[i].actionPerformed(e);
		}
	}

	private void superDetSelected() {
		currentSuperBlock = getSelectedDatablock(); 
		realButton = null;
		if (currentSuperBlock == null) {
			dataSelector = null;
		}
		else {
			dataSelector = currentSuperBlock.getDataSelector(osdFilter.getDataName(), false);
			realButton = dataSelector.getDialogButton(parent);
		}
		nullButton.setEnabled(realButton != null);

		osdFilter.setFilterDataBlock(currentSuperBlock);
	}
	
	private SuperDetDataBlock getSelectedDatablock() {
		int ind = dataList.getSelectedIndex() - 1; // subtract 1 for the 'null' option
		if (ind < 0 || ind >= dataBlocks.size()) {
			return null;
		}
		return dataBlocks.get(ind);
	}

	public JComponent getComponent() {
		return mainPanel;
	}

}
