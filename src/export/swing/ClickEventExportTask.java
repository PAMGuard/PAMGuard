package export.swing;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickControl;
import clickDetector.offlineFuncs.OfflineEventDataBlock;
import export.PamExporterManager;

public class ClickEventExportTask extends ExportTask {

	public ClickEventExportTask(PamDataBlock parentDataBlock, PamExporterManager exporter) {
		super(parentDataBlock, exporter);
		
		OfflineEventDataBlock clickEventDataBlock= (OfflineEventDataBlock) parentDataBlock;

		ClickControl clickControl = (ClickControl) clickEventDataBlock.getParentProcess().getPamControlledUnit();
		
		this.addRequiredDataBlock(clickControl.getClickDataBlock());
		
	}
	
	@Override
	public String getName() {
		//"Tracked Events is not a good name"
		return "Marked Click Events_" + this.getUnitName();
	}

}
