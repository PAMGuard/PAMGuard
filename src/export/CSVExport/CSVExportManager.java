package export.CSVExport;

import java.awt.Component;
import java.io.File;
import java.util.List;

import PamguardMVC.PamDataUnit;
import export.PamDataUnitExporter;
import javafx.scene.layout.Pane;


/**
 * Export to CSV files which are RAVEN compatible. 
 */
public class CSVExportManager implements PamDataUnitExporter{

	@Override
	public boolean hasCompatibleUnits(Class dataUnitType) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exportData(File fileName, List<PamDataUnit> dataUnits, boolean append) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getFileExtension() {
		return "csv";
	}

	@Override
	public String getIconString() {
		return "mdi2f-file-table-outline";
	}

	@Override
	public String getName() {
		return "CSV Export";
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isNeedsNewFile() {
		return false;
	}

	@Override
	public Component getOptionsPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pane getOptionsPane() {
		// TODO Auto-generated method stub
		return null;
	}

}
