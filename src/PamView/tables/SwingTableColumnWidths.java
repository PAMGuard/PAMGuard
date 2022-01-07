package PamView.tables;

import java.io.Serializable;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;

public class SwingTableColumnWidths implements PamSettings {

	private JTable table;
	
	private String unitName, unitType;
	
	private TableColumnWidthData widthData = new TableColumnWidthData();

	public SwingTableColumnWidths(String unitName, JTable table) {
		super();
		this.unitType = "Table Column Widths";
		this.unitName = unitName;
		this.table = table;
		PamSettingManager.getInstance().registerSettings(this);
		setColumnWidths();
	}

	public void setColumnWidths() {
		if (widthData == null) {
			return;
		}
		TableModel tableModel = table.getModel();
		for (int i = 0; i < tableModel.getColumnCount(); i++) {
			TableColumn tableCol = table.getColumnModel().getColumn(i);
			
			Integer width = widthData.getColumnWidth(i, getColumnName(tableCol, i));
			if (width != null) {
				tableCol.setPreferredWidth(width);
			}
		}
	}
	
	private String getColumnName(TableColumn tableCol, int iCol) {
		Object headVal = tableCol.getHeaderValue();
		if (headVal != null) {
			return headVal.toString();
		}
		else {
			return "Column"+iCol;
		}
	}
	
	
	public boolean getColumnWidths() {
		TableModel tableModel = table.getModel();
		for (int i = 0; i < tableModel.getColumnCount(); i++) {
			TableColumn tableCol = table.getColumnModel().getColumn(i);
			int width = tableCol.getWidth();
			widthData.setColumnWidth(i, getColumnName(tableCol, i), width);
		}
		return true;
	}

	@Override
	public String getUnitName() {
		return unitName;
	}

	@Override
	public String getUnitType() {
		return unitType;
	}

	@Override
	public Serializable getSettingsReference() {
		getColumnWidths();
		return widthData;
	}

	@Override
	public long getSettingsVersion() {
		return TableColumnWidthData.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		widthData = ((TableColumnWidthData) pamControlledUnitSettings.getSettings()).clone();;
		return true;
	}
	
	
	
}
