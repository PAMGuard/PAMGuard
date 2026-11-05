package export.swing;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectDialog;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.superdet.SuperDetDataBlock;
import PamguardMVC.superdet.SuperDetection;
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
		this.addRequiredDataBlock(parentDataBlock);
	}
	
	/**
	 * Called at the start of the thread which executes this task. 
	 */
	@Override
	public void prepareTask() {
		//save all data before exporting! 
		PamController.getInstance().saveViewerData();
		exporter.perpareExport();
	}

	@Override
	public String getName() {
		return getExportName(getDataBlock(), getDataBlock().getDataName());
	}

	/**
	 * Get the module that owns the exported data block.
	 * @return the owning module, or null if there isn't one.
	 */
	protected PamControlledUnit getDataModule() {
		PamDataBlock dataBlock = getDataBlock();
		if (dataBlock == null || dataBlock.getParentProcess() == null) {
			return null;
		}
		return dataBlock.getParentProcess().getPamControlledUnit();
	}

	/**
	 * Create a short but unambiguous name for a data stream in the exporter, e.g.
	 * "Click Detector: Clicks" or "Quick Spectrogram Annotation: Mark". The module
	 * name is included so that streams from two instances of the same module (e.g.
	 * two click detectors) can be told apart, but is dropped if the data name
	 * already contains it so names don't become long and repetitive.
	 * @param dataBlock - the data block being exported.
	 * @param dataName - the name of the data within the module.
	 * @return name to display for the data stream.
	 */
	public static String getExportName(PamDataBlock dataBlock, String dataName) {
		if (dataBlock == null || dataBlock.getParentProcess() == null
				|| dataBlock.getParentProcess().getPamControlledUnit() == null) {
			return dataName;
		}
		String unitName = dataBlock.getParentProcess().getPamControlledUnit().getUnitName();
		if (unitName == null || unitName.isBlank()) {
			return dataName;
		}
		if (dataName == null || dataName.isBlank()) {
			return unitName;
		}
		if (dataName.toLowerCase().contains(unitName.toLowerCase())) {
			return dataName;
		}
		return unitName + ": " + dataName;
	}

	@Override
	public boolean processDataUnit(PamDataUnit<?, ?> dataUnit) {
		
		//System.out.println("Huh? " + this.getDataBlock().getDataName() + "  " + dataUnit + "  " + dataUnit.getParentDataBlock().equals(this.getDataBlock()));
		
		if (dataUnit.getParentDataBlock().equals(this.getDataBlock())) {
			//this is very important because the way the exporter works is that it iterates through multiple parent data blocks
//			System.out.println(this.getDataBlock().getDataName() + "  " + dataUnit.getUID() + "  " + dataSelector.scoreData(dataUnit) +  "  " 
//			+ (dataSelector.getParams().getCombinationFlag()  == DataSelectParams.DATA_SELECT_DISABLE));
			
//			if (dataUnit instanceof SuperDetection) {
//				System.out.println("Super detection: " + ((SuperDetection) dataUnit).getSubDetectionsCount() + "  " + ((SuperDetection) dataUnit).getLoadedSubDetectionsCount());
//			}
			
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
		System.out.println("EXPORTER: loaded data complete"); 
		//force the exporter so save any renaming data units in the buffer
		
		//exporter.exportDataUnit(null,  true);
		//exporter.close();
		//exporter.setCurrentFile(null)
	}
	
	
	@Override
	public String getDescription() {
		return "<html>" + getDetailText().replace("\n", "<br>") + "</html>";
	}

	/**
	 * Get plain text details of the data stream, one item per line, for use in tooltips.
	 * @return the module name and type and the data name and type.
	 */
	public String getDetailText() {
		PamControlledUnit module = getDataModule();
		String data = "Data: " + getDataBlock().getDataName()
				+ "\nData type: " + getDataBlock().getUnitClass().getSimpleName();
		if (module == null) {
			return data;
		}
		return String.format("Module: %s (%s)\n%s", module.getUnitName(), module.getUnitType(), data);
	}
	

	/**
	 * Called at the end of the thread which executes this task. 
	 */
	public void completeTask() { 
		System.out.println("EXPORTER: complete task"); 
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
