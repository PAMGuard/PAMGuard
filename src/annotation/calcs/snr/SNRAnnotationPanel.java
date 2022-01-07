package annotation.calcs.snr;

import javax.swing.JComponent;
import javax.swing.JLabel;

import PamguardMVC.PamDataUnit;
import annotation.AnnotationDialogPanel;

public class SNRAnnotationPanel implements AnnotationDialogPanel {

	private SNRAnnotationType snrAnnotationType;
	
	private JLabel snrLabel;

	public SNRAnnotationPanel(SNRAnnotationType snrAnnotationType) {
		super();
		this.snrAnnotationType = snrAnnotationType;
		snrLabel = new JLabel(" ", JLabel.CENTER);
	}
	
	@Override
	public JComponent getDialogComponent() {
		return snrLabel;

	}

	@Override
	public void setParams(PamDataUnit pamDataUnit) {
		SNRAnnotation snrAn = (SNRAnnotation) pamDataUnit.findDataAnnotation(SNRAnnotation.class);
		if (snrAn == null) {
			snrLabel.setText(" - ");
		}
		else {
			snrLabel.setText(String.format("%3.2f dB", snrAn.getSnr()));
		}
	}

	@Override
	public boolean getParams(PamDataUnit pamDataUnit) {
		return true;
	}

}
