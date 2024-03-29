package tethys.calibration.swing;

import java.awt.Window;

import PamView.wizard.PamWizard;
import PamView.wizard.PamWizardCard;
import nilus.Calibration;
import tethys.calibration.CalibrationHandler;

public class CalibrationsExportWizard extends PamWizard {

	private Calibration sampleDocument;

	private CalibrationsExportWizard(Window parentFrame, Calibration sampleDocument) {
		super(parentFrame, "Calibrations Export");
		this.sampleDocument = sampleDocument;
		addCard(new CalibrationsContactCard(this));
		addCard(new CalibrationProcessCard(this));
		setHelpPoint(CalibrationHandler.helpPoint);
	}
	
	public static Calibration showWizard(Window parentFrame, Calibration sampleDocument) {
		CalibrationsExportWizard wiz = new CalibrationsExportWizard(parentFrame, sampleDocument);
		wiz.setParams();
		wiz.setVisible(true);
		return wiz.sampleDocument;
	}

	@Override
	public void setCardParams(PamWizardCard wizardCard) {
		wizardCard.setParams(sampleDocument);
	}

	@Override
	public boolean getCardParams(PamWizardCard wizardCard) {
		return wizardCard.getParams(sampleDocument);
	}
	
	@Override
	public void cancelButtonPressed() {
		sampleDocument = null;
	}

}
