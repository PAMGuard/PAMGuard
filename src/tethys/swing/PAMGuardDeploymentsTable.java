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
import tethys.TethysControl;
import tethys.TethysState;
import tethys.TethysState.StateType;
import tethys.dbxml.TethysException;
import tethys.deployment.DeploymentHandler;
import tethys.deployment.DeploymentOverview;
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
	
	private boolean[] selection = new boolean[0];
	
	private ArrayList<DeploymentTableObserver> observers = new ArrayList<>();

	public PAMGuardDeploymentsTable(TethysControl tethysControl) {
		super(tethysControl);
//		deploymentHandler = new DeploymentHandler(getTethysControl());
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("PAMGuard recording periods"));
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
			if (aRow >= 0 && aRow < selection.length && col == TableModel.SELECTCOLUMN) {
				selection[aRow] = !selection[aRow];
				for (DeploymentTableObserver obs : observers) {
					obs.selectionChanged();
				}
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
		ArrayList<RecordingPeriod> allPeriods = deploymentOverview.getRecordingPeriods();
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
		if (matchedDeployments.size() == 1) {
			JPopupMenu popMenu = new JPopupMenu();
			JMenuItem menuItem = new JMenuItem("Remove deployment document " + matchedDeployments.get(0));
			menuItem.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteDeployment(matchedDeployments.get(0));
				}
			});
			popMenu.add(menuItem);
			popMenu.show(e.getComponent(), e.getX(), e.getY());
		}
//		if (newPeriods.size() == 0) {
//			return;
//		}
//		/*
//		 *  if we get here, we've one or more rows without a Tethys output, so can have
//		 *  a menu to create them. 
//		 */
		
		
	}
	
	protected void deleteDeployment(PDeployment pDeployment) {
		Deployment dep = pDeployment.deployment;
		if (dep == null) {
			return;
		}
		int ans = WarnOnce.showWarning(getTethysControl().getGuiFrame(), "Delete Deployment document", 
				"Are you sure you want to delete the deployment document " + dep.getId(), WarnOnce.OK_CANCEL_OPTION);
		if (ans == WarnOnce.CANCEL_OPTION) {
			return;
		}
		try {
			boolean gone = getTethysControl().getDbxmlConnect().deleteDocument(dep);
		} catch (TethysException e) {
			getTethysControl().showException(e);
		}
		getTethysControl().sendStateUpdate(new TethysState(StateType.UPDATESERVER));
	}

	/**
	 * Get a list of selected recording periods. 
	 * @return list of selected periods. 
	 */
	public ArrayList<RecordingPeriod> getSelectedDeployments() {
		if (deploymentOverview == null) {
			return null;
		}
		ArrayList<RecordingPeriod> selDeps = new ArrayList<>();
		int n = Math.min(selection.length, deploymentOverview.getRecordingPeriods().size());
		for (int i = 0; i < n; i++) {
			if (selection[i]) {
				selDeps.add(deploymentOverview.getRecordingPeriods().get(i));
			}
		}
		return selDeps;
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
		String metaErr = getTethysControl().getDeploymentHandler().canExportDeployments();
		if (metaErr != null) {
			mainPanel.setBackground(Color.RED);
		}
		else {
			JPanel anyPanel = new JPanel();
			mainPanel.setBackground(anyPanel.getBackground());
		}
	}

	private void updateDeployments() {
		DeploymentHandler deploymentHandler = getTethysControl().getDeploymentHandler();
		deploymentOverview = deploymentHandler.getDeploymentOverview();
		if (deploymentOverview == null) {
			return;
		}
		int n = deploymentOverview.getRecordingPeriods().size();
		if (selection.length < n) {
			selection = Arrays.copyOf(selection, n);
//			for (int i = 0; i < setDefaultStores.length; i++) {
//				if (selectBoxes[i] == null) {
//					selectBoxes[i] = new JCheckBox();
//				}
//			}
		}
		tableModel.fireTableDataChanged();
//		DeploymentData deplData = getTethysControl().getGlobalDeplopymentData();
//		ArrayList<Deployment> projectDeployments = getTethysControl().getDbxmlQueries().getProjectDeployments(deplData.getProject());
//		deploymentHandler.matchPamguard2Tethys(deploymentOverview, projectDeployments);
	}
	
	public void addObserver(DeploymentTableObserver observer) {
		observers.add(observer);
	}

	private class TableModel extends AbstractTableModel {

		private String[] columnNames = {"Id", "Start", "Stop", "Gap", "Duration", "Cycle", "Tethys Deployment", "Select"};
		
		private static final int SELECTCOLUMN = 7;

		@Override
		public int getRowCount() {
			if (deploymentOverview == null) {
				return 0;
			}
			else {
				return deploymentOverview.getRecordingPeriods().size();
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
			RecordingPeriod period = deploymentOverview.getRecordingPeriods().get(rowIndex);
//			DeploymentRecoveryPair deplInfo = deploymentInfo.get(rowIndex);
			if (columnIndex == 5) {
				return deploymentOverview.getDutyCycleInfo();
			}
			if (columnIndex == 3 && rowIndex > 0) {
				RecordingPeriod prevPeriod = deploymentOverview.getRecordingPeriods().get(rowIndex-1);
				long gap = period.getRecordStart() - prevPeriod.getRecordStop();
				return PamCalendar.formatDuration(gap);
			}
			return getValueAt(period, rowIndex, columnIndex);
		}

		private Object getValueAt(RecordingPeriod period, int rowIndex, int columnIndex) {
					switch (columnIndex) {
					case 0:
						return rowIndex;
					case 1:
						return PamCalendar.formatDBDateTime(period.getRecordStart());
		//				return TethysTimeFuncs.formatGregorianTime(deplInfo.deploymentDetails.getAudioTimeStamp());
					case 2:
						return PamCalendar.formatDBDateTime(period.getRecordStop());
		//				return TethysTimeFuncs.formatGregorianTime(deplInfo.recoveryDetails.getAudioTimeStamp());
					case 4:
		//				long t1 = TethysTimeFuncs.millisFromGregorianXML(deplInfo.deploymentDetails.getAudioTimeStamp());
		//				long t2 = TethysTimeFuncs.millisFromGregorianXML(deplInfo.recoveryDetails.getAudioTimeStamp());
						return PamCalendar.formatDuration(period.getRecordStop()-period.getRecordStart());
					case 6:
						PDeployment deployment = period.getMatchedTethysDeployment();
						return makeDeplString(period, deployment);
					case SELECTCOLUMN:
		//				return selectBoxes[rowIndex];
						return selection[rowIndex];
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
			return String.format("%s : %3.1f%% overlap", deployment.toString(), percOverlap);
		}

	}
}
