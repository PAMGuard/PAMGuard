package qa.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamView.dialog.PamButton;
import PamView.panel.PamPanel;
import generalDatabase.lookupTables.LookUpTables;
import generalDatabase.lookupTables.LookupChangeListener;
import generalDatabase.lookupTables.LookupComponent;
import generalDatabase.lookupTables.LookupItem;
import generalDatabase.lookupTables.LookupList;
import qa.QAControl;
import qa.QANotifyable;
import qa.operations.QAOperationsStatus;
import qa.operations.QAOpsDataBlock;
import qa.operations.QAOpsDataUnit;

public class QAOperationsDisplays implements QANotifyable, LookupChangeListener {

	private QAControl qaControl;
	private QAOperationsStatus qaOperationsStatus;
	
	private ArrayList<LookupComponent> allLUTcomponents = new ArrayList<>();
	private ArrayList<OpsOptionsPanel> opsOptionsPanels = new ArrayList<>();
	
	private boolean firstCall = true;
	private LookupComponent lutComponent;
	
	public QAOperationsDisplays(QAControl qaControl, QAOperationsStatus qaOperationsStatus) {
		this.qaControl = qaControl;
		this.qaOperationsStatus = qaOperationsStatus;
		qaControl.addNotifyable(this);
	}

	public JComponent getOpsStatusLookup() {
		return getOpsStatusLookup(true);
	}
	
	public JComponent getOpsStatusLookup(boolean showEditButton) {
		JPanel opsPanel = new PamPanel(new BorderLayout());
//		opsPanel.setBorder(new TitledBorder("Operations Status"));
		LookUpTables lutTables = LookUpTables.getLookUpTables();
		LookupList statusLUT = qaOperationsStatus.getLookupList();
		lutComponent = new LookupComponent(QAOperationsStatus.qaStatusTopic, statusLUT, true);
		allLUTcomponents.add(lutComponent);
		lutComponent.setAllowEdits(true);
		lutComponent.setAllowNullSelection(false);
		lutComponent.addChangeListener(this);
		lutComponent.setShowCodePanel(false);
		opsPanel.add(BorderLayout.WEST, lutComponent.getComponent());
		// try to set to current data unit if there is already a status ...
		QAOpsDataUnit opsDataUnit = qaOperationsStatus.getCurrentStatus();
		if (opsDataUnit != null) {
			setAllLookups(opsDataUnit.getOpsStatusCode());
		}
		if (showEditButton) {
			JButton editButton = new PamButton("Edit");
			editButton.setToolTipText("Edit the items in the drop down list of operational status");
			opsPanel.add(BorderLayout.EAST,	editButton);
			editButton.addActionListener(new EditLUTAction(statusLUT, lutComponent));
		}
		
		return opsPanel;
	}
	
	public Component getOpsOptions() {
		OpsOptionsPanel opsOptionsPanel = new OpsOptionsPanel(qaControl);
		opsOptionsPanels.add(opsOptionsPanel);
		return opsOptionsPanel.getComponent();
	}
	
	private class EditLUTAction implements ActionListener {
		LookupList lookupList;
		LookupComponent lutComponent;
		/**
		 * @param lookupList
		 * @param lutComponent
		 */
		public EditLUTAction(LookupList lookupList, LookupComponent lutComponent) {
			super();
			this.lookupList = lookupList;
			this.lutComponent = lutComponent;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			LookupList newList = LookUpTables.getLookUpTables().editLookupTopic(null, QAOperationsStatus.qaStatusTopic);
//			LookUpTables.getLookUpTables().updateComponents(QAOperationsStatus.qaStatusTopic);
		}
		
	}

	@Override
	public void qaNotify(int noteCode, Object noteObject) {
		if (noteCode == PamController.INITIALIZATION_COMPLETE) {
			initComplete();
		}
		if (noteCode == QANotifyable.OPS_STATUS_CHANGE) {
			QAOpsDataUnit opsDU = (QAOpsDataUnit) noteObject;
			if (opsDU != null) {
				setAllLookups(opsDU.getOpsStatusCode());
			}
		}
		
	}

	/**
	 * Set all the lookups to the same code ...
	 * @param opsStatusCode
	 */
	private void setAllLookups(String opsStatusCode) {
		for (LookupComponent lc:allLUTcomponents) {
			lc.setSelectedCode(opsStatusCode);
		}
		for (OpsOptionsPanel optionsPanel:opsOptionsPanels) {
			optionsPanel.newState(opsStatusCode);
		}
		if (qaControl.isViewer() == false && firstCall && lutComponent != null) {
			firstCall = false;
			// make a new data unit so that the status is set at the start. 
			LookupItem selItem = lutComponent.getSelectedItem();
			if (selItem != null) { 
				lookupChange(selItem);
			}
		}
	}

	private void initComplete() {
		LookUpTables.getLookUpTables().updateComponents(QAOperationsStatus.qaStatusTopic);
	}

	@Override
	public void lookupChange(LookupItem selectedItem) {
		qaOperationsStatus.setStatus(selectedItem);
	}
}
