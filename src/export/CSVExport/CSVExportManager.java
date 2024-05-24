package export.CSVExport;

import java.io.File;
import java.util.List;

import PamguardMVC.PamDataUnit;
import export.PamDataUnitExporter;

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

}
