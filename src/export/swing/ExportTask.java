package export.swing;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
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
	
	PamExporterManager exporter; 

	public ExportTask(PamDataBlock<PamDataUnit<?, ?>> parentDataBlock, PamExporterManager exporter) {
		super(parentDataBlock);
		this.exporter = exporter; 
		// TODO Auto-generated constructor stub
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
		// TODO Auto-generated method stub
		
	}

}
