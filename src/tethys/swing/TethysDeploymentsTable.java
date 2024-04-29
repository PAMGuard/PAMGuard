package tethys.swing;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import PamView.panel.PamPanel;
import PamView.tables.SwingTableColumnWidths;
import tethys.TethysControl;
import tethys.TethysMenuActions;
import tethys.TethysState;
import tethys.deployment.DeploymentOverview;
import tethys.deployment.RecordingList;
import tethys.niluswraps.PDeployment;

public class TethysDeploymentsTable extends TethysGUIPanel {

	private JPanel mainPanel;
	
	private TableModel tableModel;
	
	private JTable table;

	private ArrayList<PDeployment> projectDeployments;

	private DeploymentOverview deploymentOverview;
	
	public TethysDeploymentsTable(TethysControl tethysControl) {
		super(tethysControl);
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("All project deployments"));
		tableModel = new TableModel();
		table = new JTable(tableModel);
		table.setRowSelectionAllowed(true);
		table.addMouseListener(new TableMouse());
		JScrollPane scrollPane = new JScrollPane(table);
		mainPanel.add(BorderLayout.CENTER, scrollPane);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		new SwingTableColumnWidths(tethysControl.getUnitName()+"AllProjectDeploymentsTable", table);
	}
	
	private class TableMouse extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				showPopupMenu(e);
			}
		}
		
	}

	protected void showPopupMenu(MouseEvent e) {
		int row = table.getSelectedRow();
		if (row < 0) {
			return;
		}
		PDeployment pDeployment = projectDeployments.get(row);
		TethysMenuActions menuActions = new TethysMenuActions(getTethysControl());
		menuActions.deploymentMouseActions(e, pDeployment);
		
	}



	@Override
	public JComponent getComponent() {
		return mainPanel;
	}
	
	@Override
	public void updateState(TethysState tethysState) {
		projectDeployments = getTethysControl().getDeploymentHandler().getProjectDeployments();
		deploymentOverview = getTethysControl().getDeploymentHandler().getDeploymentOverview();
		tableModel.fireTableDataChanged();
	}

	private class TableModel extends AbstractTableModel {

		private String[] columnNames = {"Deployment document", "PAMGuard Match"};
		
		@Override
		public int getRowCount() {
			return projectDeployments == null ? 0 : projectDeployments.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			PDeployment deployment = projectDeployments.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return deployment.toString();
			case 1:
				return getMatchText(deployment);
			}
			return null;
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}		

		public String getMatchText(PDeployment deployment) {
			// TODO Auto-generated method stub
			if (deployment.getMatchedPAMGaurdPeriod() != null) {
				return "Matched to PAMGaurd data";
			};
			if (deploymentOverview == null) {
				return "No PAMGuard data";
			}
			RecordingList masterList = deploymentOverview.getMasterList(getTethysControl());
			Long depStart = masterList.getStart();
			Long depEnd = masterList.getEnd();
			if (depStart == null) {
				return "No PAMGuard recordings";
			}
			if (deployment.getAudioEnd() < depStart) {
				return "Earlier than PAMGuard data";
			}
			if (deployment.getAudioStart() > depEnd) {
				return "Later than PAMGuard data";
			}
			return "Partial overlap with PAMGuard data, but no match";
		}
	}


}
