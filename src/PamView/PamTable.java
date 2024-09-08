package PamView;

import java.awt.Color;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import PamView.PamColors.PamColor;

public class PamTable extends JTable implements ColorManaged {

	public PamTable() {
		// TODO Auto-generated constructor stub
	}

	public PamTable(TableModel dm) {
		super(dm);
		// TODO Auto-generated constructor stub
	}

	public PamTable(TableModel dm, TableColumnModel cm) {
		super(dm, cm);
		// TODO Auto-generated constructor stub
	}

	public PamTable(int numRows, int numColumns) {
		super(numRows, numColumns);
		// TODO Auto-generated constructor stub
	}

	public PamTable(Vector rowData, Vector columnNames) {
		super(rowData, columnNames);
		// TODO Auto-generated constructor stub
	}

	public PamTable(Object[][] rowData, Object[] columnNames) {
		super(rowData, columnNames);
		// TODO Auto-generated constructor stub
	}

	public PamTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
		super(dm, cm, sm);
		// TODO Auto-generated constructor stub
	}

	private PamColor defaultColor = PamColor.BORDER;
	
	public PamColor getDefaultColor() {
		return defaultColor;
	}

	public void setDefaultColor(PamColor defaultColor) {
		this.defaultColor = defaultColor;
	}
	@Override
	public PamColor getColorId() {
		return defaultColor;
	}
	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		Color lines = PamColors.getInstance().getColor(PamColor.AXIS);
		this.setForeground(lines);
		JTableHeader th = super.getTableHeader();
		th.setOpaque(false);
		th.setBackground(PamColors.getInstance().getColor(PamColor.BACKGROUND_ALPHA)); 
		th.setForeground(bg);
	}


}
