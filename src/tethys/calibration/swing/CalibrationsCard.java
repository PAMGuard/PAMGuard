package tethys.calibration.swing;

import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;
import nilus.Calibration;

abstract public class CalibrationsCard extends PamWizardCard<Calibration> {

	public CalibrationsCard(PamWizard pamWizard, String title) {
		super(pamWizard, title);
	}

}
