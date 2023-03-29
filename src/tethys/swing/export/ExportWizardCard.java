package tethys.swing.export;

import javax.swing.JPanel;

import PamguardMVC.PamDataBlock;
import tethys.TethysControl;
import tethys.output.StreamExportParams;

/**
 * Slightly standardised panels to put into the export wizard
 * so that it's easy to work out which 'card' we're on, etc. 
 * @author dg50
 *
 */
abstract class ExportWizardCard extends JPanel {

	private String title;
	private PamDataBlock dataBlock;
	private TethysControl tethysControl;
	
	public ExportWizardCard(TethysControl tethysControl, String title, PamDataBlock dataBlock) {
		this.tethysControl = tethysControl;
		this.title = title;
		this.dataBlock = dataBlock;
	}
	
	public PamDataBlock getDataBlock() {
		return dataBlock;
	}

	public TethysControl getTethysControl() {
		return tethysControl;
	}

	public abstract boolean getParams(StreamExportParams streamExportParams);
	
	public abstract void setParams(StreamExportParams streamExportParams);

	public String getTitle() {
		return title;
	}
	
}
