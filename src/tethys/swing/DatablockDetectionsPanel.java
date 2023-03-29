package tethys.swing;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import PamView.tables.SwingTableColumnWidths;
import PamguardMVC.PamDataBlock;
import tethys.TethysControl;

/**
 * Table of Detections documents for a single PAMGuard datablock. 
 * Generally, this should only have a smallish number of entries in it
 * so may not need too much real estate on the display. 
 * @author dg50
 *
 */
public class DatablockDetectionsPanel extends TethysGUIPanel implements StreamTableObserver {
	
	private JPanel mainPanel;
	
	private JLabel dataBlockName;

	private TableModel tableModel;
	
	private JTable table;

	private PamDataBlock dataBlock;
	
	public DatablockDetectionsPanel(TethysControl tethysControl) {
		super(tethysControl);
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.NORTH, dataBlockName = new JLabel("PAMGUard data stream", JLabel.LEFT));
		mainPanel.setBorder(new TitledBorder("Data stream Tethys Detections documents"));
		
		tableModel = new TableModel();
		table = new JTable(tableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		mainPanel.add(BorderLayout.CENTER, scrollPane);
		
		new SwingTableColumnWidths(tethysControl.getUnitName() + getClass().getName(), table);
		
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}

	@Override
	public void selectDataBlock(PamDataBlock dataBlock) {
		this.dataBlock = dataBlock;
		dataBlockName.setText(dataBlock.getLongDataName());
	}

	private class TableModel extends AbstractTableModel {
		
		private String[] colNames = {"Person", "Name", "Abstract"};

		@Override
		public int getRowCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getColumnCount() {
			return colNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return colNames[column];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
