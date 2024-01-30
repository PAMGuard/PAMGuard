package tethys.calibration.swing;

import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;
import nilus.Calibration;
import tethys.swing.TethysWizardCard;

abstract public class CalibrationsCard extends TethysWizardCard<Calibration> {

	public CalibrationsCard(PamWizard pamWizard, String title) {
		super(pamWizard, title);
	}

}
