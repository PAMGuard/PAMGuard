package tethys.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamGridBagContraints;
import PamView.dialog.ScrollingPamLabel;
import PamView.dialog.SettingsButton;
import PamView.panel.PamPanel;
import PamView.panel.WestAlignedPanel;
import pamViewFX.fxNodes.PamComboBox;
import tethys.TethysControl;
import tethys.TethysState;
import tethys.TethysState.StateType;
import tethys.dbxml.ServerStatus;
import tethys.output.TethysExportParams;

/**
 * Top strip of main Tethys GUI for connection and project information
 * @author dg50
 *
 */
public class TethysConnectionPanel extends TethysGUIPanel {
	
	private static final int SERVERNAMELENGTH = 30;
	private static final int SERVERSTATUSLENGTH = 20;
	
	private JPanel mainPanel;

	private JTextField serverName;
	
	private SettingsButton serverSelButton;
	
	private ScrollingPamLabel serverStatus;
	
	private JComboBox<String> projectList;
	
	public TethysConnectionPanel(TethysControl tethysControl) {
		super(tethysControl);
		mainPanel = new WestAlignedPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Connection and project details"));
		serverName = new JTextField(SERVERNAMELENGTH);
		serverSelButton = new SettingsButton();
		serverSelButton.setToolTipText("Select server");
		serverStatus = new ScrollingPamLabel(SERVERSTATUSLENGTH);
		serverName.setEditable(false);
//		serverStatus.setEditable(false);
		serverSelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectServer();
			}
		});
		
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new JLabel("Tethys Server "), c);
		c.gridx++;
		mainPanel.add(serverName, c);
		c.gridx++;
		mainPanel.add(serverSelButton, c);
		c.gridx++;
		mainPanel.add(serverStatus, c);
		c.gridx++;
		
		c.gridx =0;
		c.gridy++;
		mainPanel.add(new JLabel("Projects "), c);
		c.gridx++;
		mainPanel.add(projectList = new JComboBox<>(), c);
		
		fillServerControl();
	}

	protected void selectServer() {
		// will return the same object at the moment, so no need to do anything. 
		TethysExportParams newParams = SelectServerdDialog.showDialog(getTethysControl(), getTethysControl().getGuiFrame(), getTethysControl().getTethysExportParams());
		if (newParams != null) {
			getTethysControl().sendStateUpdate(new TethysState(TethysState.StateType.UPDATESERVER));
		}
	}

	private void fillServerControl() {
		TethysExportParams exportParams = getTethysControl().getTethysExportParams();
		serverName.setText(exportParams.getFullServerName());
		ServerStatus status = getTethysControl().getDbxmlConnect().pingServer();
		serverStatus.setText(status.toString());
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}

	@Override
	public void updateState(TethysState tethysState) {
		super.updateState(tethysState);
		if (tethysState.stateType == StateType.UPDATESERVER) {
			fillServerControl();
			updateProjectList();
		}
		
	}

	private void updateProjectList() {
		projectList.removeAllItems();
		ArrayList<String> dbNames = getTethysControl().getDbxmlQueries().getProjectNames();
		if (dbNames == null || dbNames.size() == 0) {
			System.out.println("No existing projects");
			return;
		}
		for (int i = 0; i < dbNames.size(); i++) {
			projectList.addItem(dbNames.get(i));
		}
	}

	
	
}
