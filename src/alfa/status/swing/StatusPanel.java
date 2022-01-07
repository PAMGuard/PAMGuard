package alfa.status.swing;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.border.TitledBorder;

import PamController.status.ModuleStatus;
import PamModel.PamModuleInfo;
import PamView.PamColors.PamColor;
import PamView.panel.PamPanel;
import alfa.ControlledModuleInfo;
import alfa.status.StatusMonitor;
import alfa.status.StatusObserver;

/**
 * Swing status display panel to go with the status monitor.
 * @author Doug Gillespie
 *
 */
public class StatusPanel implements StatusObserver {

	private StatusMonitor statusMonitor;
	
	private PamPanel statusPanel;
	
	private PamPanel buttonPanel;
	
	private StatusButton[] statusButtons;

	public StatusPanel(StatusMonitor statusMonitor) {
		this.statusMonitor = statusMonitor;
		statusPanel = new PamPanel(PamColor.BORDER);
		statusPanel.setBorder(new TitledBorder("Module Status"));
		buttonPanel = new PamPanel(PamColor.BORDER);
		statusPanel.setLayout(new BorderLayout());
		statusPanel.add(buttonPanel, BorderLayout.NORTH);
		refillButtonPanel();
		statusMonitor.addObserver(this);
	}

	public JComponent getPanel() {
		return statusPanel;
	}
	
	private void refillButtonPanel() {
		buttonPanel.removeAll();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		ArrayList<ControlledModuleInfo> list =	statusMonitor.getModulesList();
		if (list == null) {
			statusButtons = null;
			return;
		}
		statusButtons = new StatusButton[list.size()];
		for (int i = 0; i < list.size(); i++) {
			statusButtons[i] = new StatusButton(list.get(i).getDefaultName());
			buttonPanel.add(statusButtons[i].getComponent());
		}
	}

	@Override
	public void newModuleList() {
		refillButtonPanel();
	}

	@Override
	public void newStatus() {
		updateAllStatus();
	}

	private void updateAllStatus() {
		if (statusButtons == null) {
			return;
		}
		ModuleStatus[] statuss = statusMonitor.getModuleStatus();
		if (statuss == null) {
			return;
		}
		if (statuss.length != statusButtons.length) {
			return;
		}
		boolean[] existss = statusMonitor.getModuleExists();
		for (int i = 0; i < statuss.length; i++) {
			statusButtons[i].setStatus(existss[i], statuss[i]);
		}
		
	}
}
