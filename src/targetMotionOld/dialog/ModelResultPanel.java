package targetMotionOld.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import Localiser.detectionGroupLocaliser.GroupLocResult;
import PamUtils.LatLong;
import PamView.PamSymbol;
import targetMotionOld.TargetMotionLocaliser;
import targetMotionOld.TargetMotionModel;

/**
 * Reinstated Target motion add-in as used by the click detector. Hope one day still to replace this
 * with Jamie's new one, but keep this one until Jamie's is working. 
 * @author Doug Gillespie
 *
 */
public class ModelResultPanel implements TMDialogComponent {


	private TargetMotionDialog targetMotionDialog;

	private TargetMotionLocaliser targetMotionLocaliser;

	private JPanel mainPanel;

	private JTable resultTable;

	private ResultTableDataModel tableDataModel;

	/**
	 * @param targetMotionLocaliser
	 * @param targetMotionDialog
	 */
	public ModelResultPanel(TargetMotionLocaliser targetMotionLocaliser,
			TargetMotionDialog targetMotionDialog) {
		super();
		this.targetMotionLocaliser = targetMotionLocaliser;
		this.targetMotionDialog = targetMotionDialog;

		mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder("Model Results"));

		tableDataModel = new ResultTableDataModel();
		resultTable = new JTable(tableDataModel);
		resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane sp = new JScrollPane(resultTable);
		sp.setPreferredSize(new Dimension(0, 100));
		mainPanel.add(BorderLayout.CENTER, sp);
		
//		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		int n = tableDataModel.getColumnCount();
		TableColumn col;
		for (int i = 0; i < n; i++) {
			 col = resultTable.getColumnModel().getColumn(i);
			Integer width = tableDataModel.getColumnWidth(i);
			if (width != null) {
				col.setPreferredWidth(width);
			}
		}


	}

	/**
	 * @return the mainPanel
	 */
	public JPanel getPanel() {
		return mainPanel;
	}

	@Override
	public void setCurrentEventIndex(int eventIndex, Object sender) {
		if (sender == this) return;
	}

	@Override
	public boolean canRun() {
		return true;
	}

	@Override
	public void enableControls() {
		// TODO Auto-generated method stub

	}

	public void notifyNewResults() {
		tableDataModel.fireTableDataChanged();
	}

	private class ResultTableDataModel extends AbstractTableModel {

		private String[] colNames = {"Sel", "Model", "Symb", "Side", "Lat Long", "Depth", "Dist", "Error", "Chi2", "p", "nDF", "AIC", "millis"};
		@Override
		public int getColumnCount() {
			return colNames.length;
		}

		public Integer getColumnWidth(int iCol) {
			switch(iCol) {
			case 0:
				return 10;
			case 1:
				return 100;
			case 2:
				return 20;
			case 3:
				return 50;
			case 4:
				return 150;
			case 5:
				return 50;
			case 6:
				return 50;
			case 7:
				return 50;
			case 8:
				return 50;
			case 9:
				return 50;
			case 10:
				return 50;
			case 11:
				return 50;
			case 12:
				return 50;
			}
			return null;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if (columnIndex == 0) {
				return Boolean.class;
			}
			else if (columnIndex == 2) {
				return ImageIcon.class;
			}
			return super.getColumnClass(columnIndex);
		}
		@Override
		public String getColumnName(int col) {
			return colNames[col];
		}

		@Override
		public int getRowCount() {
			ArrayList<GroupLocResult> results = targetMotionLocaliser.getResults();
			return results.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			ArrayList<GroupLocResult> results = targetMotionLocaliser.getResults();
			GroupLocResult aResult = results.get(row);
			Double a;
			Integer intVal;
			switch(col) {
			case 0:
				return row == targetMotionLocaliser.getBestResultIndex();
			case 1:
				return aResult.getModel().getName();
			case 2:
				PamSymbol aSymbol = ((TargetMotionModel) aResult.getModel()).getPlotSymbol(aResult.getSide());
				if (aSymbol == null) {
					return null;
				}
				return aSymbol;
			case 3:
				return aResult.getSide();
			case 4:
				LatLong ll = aResult.getLatLong();
				if (ll == null) {
					return "No position result";
				}
				return aResult.getLatLong().formatLatitude() + "  " + aResult.getLatLong().formatLongitude();
			case 5:
				LatLong latLong = aResult.getLatLong();
				if (latLong == null) {
					return null;
				}
				return String.format("%3.1fm", -latLong.getHeight());
			case 6:
				Double v = aResult.getPerpendicularDistance();
				if (v == null) {
					return null;
				}
				return String.format("%3.1fm", v);
			case 7:
				Double v2 = aResult.getPerpendicularDistanceError();
				if (v2 == null) {
					return null;
				}
				return String.format("%3.1fm", v2);
			case 8:
				Double chi2 = aResult.getChi2();
				if (chi2 == null) {
					return null;
				}
				return String.format("%3.1f",chi2);
			case 9:
				a = aResult.getProbability();
				if (a == null){ 
					return null;
				}
				return String.format("%.4g", a);
			case 10:
				intVal = aResult.getnDegreesFreedom();
				if (intVal == null){ 
					return null;
				}
				return String.format("%d", intVal);
			case 11:
				a = aResult.getAic();
				if (a == null){ 
					return null;
				}
				return String.format("%3.1f", a);
			case 12:
				Double t = aResult.getRunTimeMillis();
				if (t == null){ 
					return null;
				}
				return String.format("%3.1fms", t);
			}

			return null;
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return (col == 0);
		}

		@Override
		public void setValueAt(Object aValue, int row, int col) {
			if (col == 0) {
				targetMotionLocaliser.setBestResultIndex(row);
				targetMotionDialog.notifyNewResults();
			}
			else {
				super.setValueAt(aValue, row, col);
			}
		}

	}

}
