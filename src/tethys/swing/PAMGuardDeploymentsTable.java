package tethys.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import PamUtils.PamCalendar;
import PamView.dialog.warn.WarnOnce;
import PamView.panel.PamPanel;
import PamView.tables.SwingTableColumnWidths;
import nilus.Deployment;
import tethys.Collection;
import tethys.TethysControl;
import tethys.TethysState;
import tethys.TethysState.StateType;
import tethys.dbxml.TethysException;
import tethys.deployment.DeploymentHandler;
import tethys.deployment.DeploymentOverview;
import tethys.deployment.DutyCycleInfo;
import tethys.deployment.RecordingList;
import tethys.deployment.RecordingPeriod;
import tethys.niluswraps.PDeployment;

/**
 * Table view of PAMGuard deployments. For a really simple deployment, this may have only
 * one line. For towed surveys where we stop and start a lot, it may have a LOT of lines.
 * @author dg50
 *
 */
public class PAMGuardDeploymentsTable extends TethysGUIPanel {

	private TableModel tableModel;

	private JTable table;

	private JPanel mainPanel;

	private DeploymentOverview deploymentOverview;

	//	private boolean[] selection = new boolean[0];

	private ArrayList<DeploymentTableObserver> observers = new ArrayList<>();

	private RecordingList masterList;

	private ArrayList<RecordingPeriod> displayPeriods;

	public PAMGuardDeploymentsTable(TethysControl tethysControl) {
		super(tethysControl);
		//		deploymentHandler = new DeploymentHandler(getTethysControl());
		mainPanel = new PamPanel(new BorderLayout());
		//		mainPanel.setBorder(new TitledBorder("PAMGuard recording periods"));
		tableModel = new TableModel();
		table = new JTable(tableModel);
		//		table.setRowSelectionAllowed(true);
		table.addMouseListener(new TableMouse());
		JScrollPane scrollPane = new JScrollPane(table);
		mainPanel.add(BorderLayout.CENTER, scrollPane);
		new SwingTableColumnWidths(tethysControl.getUnitName()+"PAMDeploymensTable", table);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}

	public RecordingList getMasterList() {
		return masterList;
	}

	private class TableMouse extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopup(e);
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			int aRow = table.getSelectedRow();
			int col = table.getSelectedColumn();
//			ArrayList<RecordingPeriod> periods = getMasterList().getEffortPeriods();
			if (displayPeriods == null) {
				return;
			}
			if (aRow >= 0 && aRow < displayPeriods.size() && col == TableModel.SELECTCOLUMN) {
				displayPeriods.get(aRow).toggleSelected();
				notifyObservers();
			}
		}

	}

	public void showPopup(MouseEvent e) {
		int aRow = table.getSelectedRow();
		int[] selRows = table.getSelectedRows();
		if (selRows == null || selRows.length == 0) {
			if (aRow >= 0) {
				selRows = new int[1];
				selRows[0] = aRow;
			}
			else {
				return;
			}
		}
		// make a list of RecordingPeriods which don't currently have a Deployment document
		ArrayList<RecordingPeriod> newPeriods = new ArrayList<>();
		if (displayPeriods == null) {
			return;
		}
		ArrayList<RecordingPeriod> allPeriods = displayPeriods;//getMasterList().getEffortPeriods();
		//		ArrayList<RecordingPeriod> allPeriods = deploymentOverview.getRecordingPeriods();
		ArrayList<PDeployment> matchedDeployments = new ArrayList<>();
		for (int i = 0; i < selRows.length; i++) {
			PDeployment tethysDeployment = allPeriods.get(selRows[i]).getMatchedTethysDeployment();
			if (tethysDeployment == null) {
				newPeriods.add(allPeriods.get(i));
			}
			else {
				if (matchedDeployments.contains(tethysDeployment) == false) {
					matchedDeployments.add(tethysDeployment);
				}
			}
		}
		JPopupMenu popMenu = new JPopupMenu();

		JMenuItem menuItem = new JMenuItem("Select all");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectAll(true);
			}
		});
		popMenu.add(menuItem);
		menuItem = new JMenuItem("Select none");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectAll(false);
			}
		});
		popMenu.add(menuItem);

		if (matchedDeployments.size() > 0) {
			popMenu.addSeparator();
		}

		if (matchedDeployments.size() == 1) {
			menuItem = new JMenuItem("Display deployment document " + matchedDeployments.get(0));
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					displayDeployment(matchedDeployments.get(0));
				}
			});
			popMenu.add(menuItem);
			menuItem = new JMenuItem("Export deployment document " + matchedDeployments.get(0));
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					exportDeployment(matchedDeployments.get(0));
				}
			});
			popMenu.add(menuItem);

//			popMenu.addSeparator();

//			menuItem = new JMenuItem("Delete deployment document " + matchedDeployments.get(0));
//			menuItem.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					deleteDeployment(matchedDeployments.get(0));
//				}
//			});
//			popMenu.add(menuItem);

		}
		else if (matchedDeployments.size() > 1){
			menuItem = new JMenuItem(String.format("Delete %d deployment documents", matchedDeployments.size()));
			menuItem.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					deleteMultipleDeployments(matchedDeployments);
				}
			});
			popMenu.add(menuItem);
		}

		popMenu.show(e.getComponent(), e.getX(), e.getY());


	}

	protected void selectAll(boolean select) {
//		ArrayList<RecordingPeriod> recordingPeriods = getMasterList().getEffortPeriods();
		if (displayPeriods == null) {
			return;
		}
		//		ArrayList<RecordingPeriod> recordingPeriods = deploymentOverview.getRecordingPeriods();
		for (int i = 0; i < displayPeriods.size(); i++) {
			displayPeriods.get(i).setSelected(select);
		}

		tableModel.fireTableDataChanged();

		notifyObservers();

	}

	protected void deleteMultipleDeployments(ArrayList<PDeployment> matchedDeployments) {
		int ans = WarnOnce.showWarning(getTethysControl().getGuiFrame(), "Delete Deployment document", 
				"Are you sure you want to delete multiple deployment documents ", WarnOnce.OK_CANCEL_OPTION);
		if (ans == WarnOnce.CANCEL_OPTION) {
			return;
		}
		for (PDeployment depl : matchedDeployments) {
			if (depl.nilusObject == null) {
				continue;
			}
			try {
				if (checkDetections(depl.nilusObject) == false) {
					continue;
				}
				boolean gone = getTethysControl().getDbxmlConnect().deleteDocument(depl.nilusObject);
			} catch (TethysException e) {
				getTethysControl().showException(e);
			}
		}
		getTethysControl().sendStateUpdate(new TethysState(StateType.DELETEDATA, Collection.Deployments));
	}

	/**
	 * Check for detections associated with this deployment. they must be deleted first. 
	 * @param deployment
	 * @return true if there are no Detections or if they are sucessfully removed as well. 
	 */
	private boolean checkDetections(Deployment deployment) {
		// get any deployment documents that associate with this deployment. 		
		ArrayList<String> detectionDocs = getTethysControl().getDbxmlQueries().getDeploymentDocuments(Collection.Detections, deployment.getId());
		ArrayList<String> localizationDocs = getTethysControl().getDbxmlQueries().getDeploymentDocuments(Collection.Localizations, deployment.getId());
		int nDocs = 0;
		if (detectionDocs != null) nDocs += detectionDocs.size();
		if (localizationDocs != null) nDocs += localizationDocs.size();
		if (nDocs == 0) {
			return true;
		}
		String msg = String.format("<html>%d other documents are associated with Deployment %s<br>", nDocs, deployment.getId());
		for (String str : detectionDocs) {
			msg += String.format("<br>%s", str);
		}
		msg += String.format("<br><br>You must delete these prior to deleting the Deploymen. Go ahead and delete ?");
		int ans = WarnOnce.showWarning(getTethysControl().getGuiFrame(), "Existing Detection / Localization documents !" , msg, WarnOnce.OK_CANCEL_OPTION);
		if (ans == WarnOnce.CANCEL_OPTION) {
			return false;
		}
		// OK, so delete all the Detections too !!!
		boolean errors = false;
		if (detectionDocs != null) {
			for (String str : detectionDocs) {
				try {
					boolean gone = getTethysControl().getDbxmlConnect().removeDocument(Collection.Detections, str);
				} catch (TethysException e) {
					getTethysControl().showException(e);
					errors = true;
				}
			}
		}
		if (localizationDocs != null) {
			for (String str : localizationDocs) {
				try {
					boolean gone = getTethysControl().getDbxmlConnect().removeDocument(Collection.Localizations, str);
				} catch (TethysException e) {
					getTethysControl().showException(e);
					errors = true;
				}
			}
		}

		return !errors;
	}

	protected void deleteDeployment(PDeployment pDeployment) {
		Deployment dep = pDeployment.nilusObject;
		if (dep == null) {
			return;
		}
		int ans = WarnOnce.showWarning(getTethysControl().getGuiFrame(), "Delete Deployment document", 
				"Are you sure you want to delete the deployment document " + dep.getId(), WarnOnce.OK_CANCEL_OPTION);
		if (ans == WarnOnce.CANCEL_OPTION) {
			return;
		}
		if (checkDetections(dep) == false) {
			return;
		}
		try {
			boolean gone = getTethysControl().getDbxmlConnect().deleteDocument(dep);
		} catch (TethysException e) {
			getTethysControl().showException(e);
		}
		getTethysControl().sendStateUpdate(new TethysState(StateType.DELETEDATA, Collection.Deployments));
	}

	protected void exportDeployment(PDeployment pDeployment) {
		getTethysControl().exportDocument(Collection.Deployments.collectionName(), pDeployment.nilusObject.getId());
	}

	protected void displayDeployment(PDeployment pDeployment) {
		getTethysControl().displayDocument(Collection.Deployments.collectionName(), pDeployment.nilusObject.getId());
	}

	@Override
	public void updateState(TethysState tethysState) {
		switch(tethysState.stateType) {
		case NEWPROJECTSELECTION:
		case NEWPAMGUARDSELECTION:
			updateDeployments();
			break;
		case UPDATEMETADATA:
			checkExportMeta();
		}

		tableModel.fireTableDataChanged();
	}

	private void checkExportMeta() {
		String metaErr = getTethysControl().getDeploymentHandler().canExportDeployments(false);
		if (metaErr != null) {
			mainPanel.setBackground(Color.RED);
		}
		else {
			JPanel anyPanel = new JPanel();
			mainPanel.setBackground(anyPanel.getBackground());
		}
	}

	/**
	 * Get a list of selected periods irrespective of whether they have an existing deployment document. 
	 * @return
	 */
	public ArrayList<RecordingPeriod> getSelectedPeriods() {
		if (deploymentOverview == null || displayPeriods == null) {
			return null;
		}
		ArrayList<RecordingPeriod> allPeriods = displayPeriods;//getMasterList().getEffortPeriods();
		ArrayList<RecordingPeriod> selPeriods = new ArrayList();
		int n = allPeriods.size();
		for (int i = 0; i < n; i++) {
			if (allPeriods.get(i).isSelected()) {
				selPeriods.add(allPeriods.get(i));
			}
		}
		return selPeriods;
	}
	private void notifyObservers() {
		for (DeploymentTableObserver obs : observers) {
			obs.selectionChanged();
		}
	}

	private void updateDeployments() {
		DeploymentHandler deploymentHandler = getTethysControl().getDeploymentHandler();
		deploymentOverview = deploymentHandler.getDeploymentOverview();
		displayPeriods = null;
		if (deploymentOverview == null) {
			return;
		}
		masterList = deploymentOverview.getMasterList(getTethysControl());
		displayPeriods = masterList.getCompoundPeriods(deploymentHandler.getDeploymentExportOptions());
		tableModel.fireTableDataChanged();
		//		DeploymentData deplData = getTethysControl().getGlobalDeplopymentData();
		//		ArrayList<Deployment> projectDeployments = getTethysControl().getDbxmlQueries().getProjectDeployments(deplData.getProject());
		//		deploymentHandler.matchPamguard2Tethys(deploymentOverview, projectDeployments);
	}

	public void addObserver(DeploymentTableObserver observer) {
		observers.add(observer);
	}

	private class TableModel extends AbstractTableModel {

		private String[] columnNames = {"Id", "Select", "Start", "Stop", "Gap", "Duration", "Duty Cycle", "Tethys Deployment", "Deployment Effort"};

		private static final int SELECTCOLUMN = 1;

		@Override
		public int getRowCount() {
			if (displayPeriods == null) {
				return 0;
			}
			else {
				return displayPeriods.size();
			}
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == SELECTCOLUMN) {
				return Boolean.class;
				//				return JCheckBox.class;
			}
			return super.getColumnClass(columnIndex);
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			RecordingPeriod period = displayPeriods.get(rowIndex);
			//			DeploymentRecoveryPair deplInfo = deploymentInfo.get(rowIndex);
			if (columnIndex == 6) {
				DutyCycleInfo dc = period.getDutyCycleInfo();
				if (dc == null || dc.isDutyCycled == false) {
					return null;
				}
				else {
					return dc.toString();
				}
			}
			if (columnIndex == 4 && rowIndex > 0) {
				RecordingPeriod prevPeriod = displayPeriods.get(rowIndex-1);
				long gap = period.getRecordStart() - prevPeriod.getRecordStop();
				return PamCalendar.formatDuration(gap);
			}
			return getValueAt(period, rowIndex, columnIndex);
		}

		private Object getValueAt(RecordingPeriod period, int rowIndex, int columnIndex) {
			PDeployment deployment = period.getMatchedTethysDeployment();
			switch (columnIndex) {
			case 0:
				return rowIndex;
			case 2:
				return PamCalendar.formatDBDateTime(period.getRecordStart());
				//				return TethysTimeFuncs.formatGregorianTime(deplInfo.deploymentDetails.getAudioTimeStamp());
			case 3:
				return PamCalendar.formatDBDateTime(period.getRecordStop());
				//				return TethysTimeFuncs.formatGregorianTime(deplInfo.recoveryDetails.getAudioTimeStamp());
			case 5:
				//				long t1 = TethysTimeFuncs.millisFromGregorianXML(deplInfo.deploymentDetails.getAudioTimeStamp());
				//				long t2 = TethysTimeFuncs.millisFromGregorianXML(deplInfo.recoveryDetails.getAudioTimeStamp());
				return PamCalendar.formatDuration(period.getRecordStop()-period.getRecordStart());
			case 7:
				if (deployment == null) {
					return null;
				}
				return deployment.nilusObject.getId();
				//						return makeDeplString(period, deployment);
			case 8:
				if (deployment == null) {
					return null;
				}
				return String.format("%s to %s", PamCalendar.formatDBDateTime(deployment.getAudioStart()), 
						PamCalendar.formatDBDateTime(deployment.getAudioEnd()));

			case SELECTCOLUMN:
				//				return selectBoxes[rowIndex];
				return period.isSelected();
			}

			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex == SELECTCOLUMN;
		}

		private String makeDeplString(RecordingPeriod period, PDeployment deployment) {
			if (deployment == null) {
				return "no match";
			}
			DeploymentHandler deploymentHandler = getTethysControl().getDeploymentHandler();
			long overlap = deploymentHandler.getDeploymentOverlap(deployment, period);

			long start = period.getRecordStart();
			long stop = period.getRecordStop();
			double percOverlap = (overlap*100.) / (stop-start);
			//			return String.format("%s : %3.1f%% overlap", deployment.toString(), percOverlap);
			return deployment.toString();
		}

	}
}
