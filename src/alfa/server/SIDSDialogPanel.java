package alfa.server;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;

public class SIDSDialogPanel implements PamDialogPanel {

	private ServerIntervalDataSelector sids;
	
	private JPanel mainPanel;
	
	private JRadioButton showAll, showSystem;
	private JComboBox<Long> systemList;

	private List<Long> imeiList;

	public SIDSDialogPanel(ServerIntervalDataSelector sids) {
		this.sids = sids;
		mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.setBorder(new TitledBorder("Online ALFA data selection"));
		showAll = new JRadioButton("Show all systems");
		showSystem = new JRadioButton("Show only the selected system data");
		systemList = new JComboBox<>();
		ButtonGroup bg = new ButtonGroup();
		bg.add(showAll);
		bg.add(showSystem);
		mainPanel.add(showAll, c);
		c.gridy++;
		mainPanel.add(showSystem, c);
		c.gridy++;
		mainPanel.add(systemList, c);
		showAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		});
		showSystem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		});
		imeiList = sids.getServerDataBlock().getImeiList();
		if (imeiList != null) {
			for (Long imei : imeiList) {
				systemList.addItem(imei);
			}
		}
	}

	protected void enableControls() {
		systemList.setEnabled(showSystem.isSelected());
		if (imeiList == null || imeiList.size() == 0) {
			systemList.setEnabled(false);
			showSystem.setEnabled(false);
			systemList.setToolTipText("No system data available from online database");
		}
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		SIDSParams params = sids.getParams();
		showAll.setSelected(params.showAll);
		showSystem.setSelected(params.showAll == false);
		if (params.selectedSystemId != null) {
			systemList.setSelectedItem(params.selectedSystemId);
		}
	}

	@Override
	public boolean getParams() {
		SIDSParams params = sids.getParams();
		params.showAll = showAll.isSelected();
		if (!params.showAll) {
			Long selVal = (Long) systemList.getSelectedItem();
			if (selVal == null) {
				return PamDialog.showWarning(null, "Error", "You must select a valid system from the drop down list");
			}
			else {
				params.selectedSystemId = selVal;
			}
		}
		return true;
	}

}
