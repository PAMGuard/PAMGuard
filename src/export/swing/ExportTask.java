package export.swing;

import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectDialog;
import PamguardMVC.dataSelector.DataSelector;
import dataMap.OfflineDataMapPoint;
import export.PamExporterManager;
import offlineProcessing.OfflineTask;

/**
 * Export data to a file type. 
 * 
 * @author Jamie Macaulay
 *
 */
public class ExportTask extends OfflineTask<PamDataUnit<?,?>>{
	
	/**
	 * Reference to the data exporter which manages exporting of data. 
	 */
	private PamExporterManager exporter;
	
	/**
	 * The data selector for the data block
	 */
	private DataSelector dataSelector; 

	public ExportTask(PamDataBlock<PamDataUnit<?, ?>> parentDataBlock, PamExporterManager exporter) {
		super(parentDataBlock);
		this.exporter = exporter; 
		dataSelector=parentDataBlock.getDataSelectCreator().getDataSelector(this.getUnitName() +"_clicks", false, null);

	}

	@Override
	public String getName() {
		return this.getDataBlock().getDataName();
	}

	@Override
	public boolean processDataUnit(PamDataUnit<?, ?> dataUnit) {
		exporter.exportDataUnit(dataUnit);
		return true;
	}

	@Override
	public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadedDataComplete() {
		//force the exporter so save any remaning data units in the buffer
		exporter.exportDataUnit(null);
		
	}
	/**
	 * task has settings which can be called
	 * @return true or false
	 */
	public boolean hasSettings() {
		return dataSelector!=null;
	}

	/**
	 * Call any task specific settings
	 * @return true if settings may have changed. 
	 */
	public boolean callSettings() {
		
		dataSelector.getDialogPanel().setParams();
		
		DataSelectDialog dataSelectDialog = new DataSelectDialog(PamController.getMainFrame(),
				this.getDataBlock(), dataSelector, null);
		return dataSelectDialog.showDialog();
		
	}

}
