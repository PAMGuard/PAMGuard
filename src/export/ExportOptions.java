package export;

import java.io.Serializable;

import javax.swing.JFrame;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.StorageParameters;
import export.swing.ExportProcessDialog;

/**
 * Manages opening settings dialog and saving settings for both FX and Swing GUI's
 * 
 * @author Jamie Macaulay
 *
 */
public class ExportOptions implements PamSettings {
	
	private static ExportOptions singleInstance;
	
//	/**
//	 * Parameters for the exporter. 
//	 */
//	private ExportParams storageParameters = new ExportParams();

	/**
	 * Swing dialog for exporting data. 
	 */
	private ExportProcessDialog exportProcessDialog;
	
	/**
	 * Reference to the export manager. This handles exporting data units. 
	 */
	private PamExporterManager exportManager = new PamExporterManager(); 

	private ExportOptions() {
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	public static ExportOptions getInstance() {
		if (singleInstance == null) {
			singleInstance = new ExportOptions();
		}
		return singleInstance;
	}

	
	/**
	 * Show the swing dialog. 
	 * @param parentFrame - the parent frame. 
	 * @return true if settings are OK on close
	 */
	public boolean showDialog(JFrame parentFrame) {
		
		if (exportProcessDialog==null) {
			exportProcessDialog= new ExportProcessDialog(exportManager); 
		}
		this.exportProcessDialog.showOfflineDialog(parentFrame, exportManager.getExportParams());
		
//		ExportParams newParams = StorageOptionsDialog.showDialog(parentFrame, storageParameters);
//		if (newParams != null) {
//			storageParameters = newParams.clone();
//			return true;
//		}
//		else {
//			return false;
//		}
		
		return true;
	}
	
	@Override
	public Serializable getSettingsReference() {
		return  exportManager.getExportParams();
	}

	@Override
	public long getSettingsVersion() {
		return StorageParameters.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return "PAMGUARD Export Options";
	}

	@Override
	public String getUnitType() {
		return "PAMGUARD Export Options";
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		ExportParams storageParameters = ((ExportParams) pamControlledUnitSettings.getSettings()).clone();
		exportManager.setExportParams(storageParameters);
		return true;
	}

	
	public void setExportParameters(ExportParams storageParameters) {
		exportManager.setExportParams(storageParameters);
	}

	/**
	 * Get storage parameters settings. 
	 * @return the storage paramters settings
	 */
	public ExportParams getExportParameters() {
		return exportManager.getExportParams();
	}
}