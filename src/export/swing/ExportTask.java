package export.swing;

import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectDialog;
import PamguardMVC.dataSelector.DataSelectParams;
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

	private boolean canExport; 

	public ExportTask(PamDataBlock<PamDataUnit<?, ?>> parentDataBlock, PamExporterManager exporter) {
		super(parentDataBlock);
		this.exporter = exporter; 
		dataSelector=parentDataBlock.getDataSelectCreator().getDataSelector(this.getUnitName() +"_export", false, null);


	}
	
	/**
	 * Called at the start of the thread which executes this task. 
	 */
	@Override
	public void prepareTask() {
		exporter.perpareExport();
	}

	@Override
	public String getName() {
		return this.getDataBlock().getDataName();
	}

	@Override
	public boolean processDataUnit(PamDataUnit<?, ?> dataUnit) {

		//System.out.println("Huh? " + this.getDataBlock().getDataName() + "  " + dataUnit + "  " + dataUnit.getParentDataBlock().equals(this.getDataBlock()));
		
		if (dataUnit.getParentDataBlock().equals(this.getDataBlock())) {
			//this is very important because the way the exporter works is that it iterates through multiple parent data blocks
			//System.out.println(this.getDataBlock().getDataName() + "  " + dataUnit + "  " + dataSelector.scoreData(dataUnit));
			
			if (dataSelector==null) {
				//System.out.println("Data selector null: " + this.getDataBlock().getDataName() + "  " + dataUnit);
				exporter.exportDataUnit(dataUnit, false);
			}
			else if (dataSelector.scoreData(dataUnit)>0 || dataSelector.getParams().getCombinationFlag()  == DataSelectParams.DATA_SELECT_DISABLE) {
				//System.out.println("Data selector OK: " + this.getDataBlock().getDataName() + "  " + dataUnit);
				exporter.exportDataUnit(dataUnit, false);
			}
		}
		return false; //we don't need to indicate that anything has changed - we are just exporting. 
	}

	@Override
	public void newDataLoad(long startTime, long endTime, OfflineDataMapPoint mapPoint) {
		// TODO Auto-generated method stub
		//		System.out.println("EXPORTER: new data load"); 

	}

	@Override
	public void loadedDataComplete() {
//		System.out.println("EXPORTER: loaded data complete"); 
		//force the exporter so save any renaming data units in the buffer
		exporter.exportDataUnit(null,  true);
		exporter.close();
		exporter.setCurrentFile(null); 

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

	/**
	 * Set whether the task can export based on the current selection
	 * @param exportSelection - the index of the selected exporter
	 */
	public boolean canExport(int exportSelection) {
		return exporter.getExporter(exportSelection).hasCompatibleUnits(getDataBlock().getUnitClass());
	}


	@Override
	public boolean canRun() {		
		boolean can = getDataBlock() != null; 

		if (can) {
			//check whether we can export based on the export selection
			can =  canExport(exporter.getExportParams().exportChoice);
		}

		return can;
	}

}
