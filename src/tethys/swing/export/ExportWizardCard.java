package tethys.swing.export;

import javax.swing.JPanel;

import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;
import PamguardMVC.PamDataBlock;
import tethys.TethysControl;
import tethys.output.StreamExportParams;

/**
 * Slightly standardised panels to put into the export wizard
 * so that it's easy to work out which 'card' we're on, etc. 
 * @author dg50
 *
 */
abstract public class ExportWizardCard extends PamWizardCard<StreamExportParams> {

	private static final long serialVersionUID = 1L;

	private String title;
	private PamDataBlock dataBlock;
	private TethysControl tethysControl;
	
	public ExportWizardCard(TethysControl tethysControl, PamWizard pamWizard, String title, PamDataBlock dataBlock) {
		super(pamWizard, title);
		this.tethysControl = tethysControl;
		this.dataBlock = dataBlock;
	}
	
	public PamDataBlock getDataBlock() {
		return dataBlock;
	}

	public TethysControl getTethysControl() {
		return tethysControl;
	}
	
}
