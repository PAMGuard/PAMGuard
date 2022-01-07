package generalDatabase.dataExport;

import generalDatabase.PamTableItem;
import generalDatabase.pamCursor.PamCursor;

import java.util.ArrayList;

abstract public class DataExportSystem {
	
	abstract String getSystemName();

	abstract boolean exportData(ArrayList<PamTableItem> tableItems,
			PamCursor exportCursor);
	
}
