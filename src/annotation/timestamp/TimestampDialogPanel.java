package annotation.timestamp;

import javax.swing.JComponent;

import annotation.AnnotationDialogPanel;
import PamView.DBTextArea;
import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataUnit;

public class TimestampDialogPanel implements AnnotationDialogPanel {

	private TimestampAnnotationType stringAnnotationType;
	
	private DBTextArea textArea;
	
	public TimestampDialogPanel(TimestampAnnotationType stringAnnotationType) {
		super();
		this.stringAnnotationType = stringAnnotationType;
		textArea = new DBTextArea(1, 25, 24);
	}

	@Override
	public JComponent getDialogComponent() {
		return textArea.getComponent();
	}

	@Override
	public void setParams(PamDataUnit pamDataUnit) {
		TimestampAnnotation an = (TimestampAnnotation) pamDataUnit.findDataAnnotation(TimestampAnnotation.class, 
				stringAnnotationType.getAnnotationName());
		if (an == null) {
			textArea.setText(null);
		}
		else {
//			textArea.setText(an.getString());
		}
	}

	@Override
	public boolean getParams(PamDataUnit pamDataUnit) {
		String note = textArea.getText();
		if (note != null) {
			note = note.trim();
		}
		if (note != null && note.length() > 0) {
			TimestampAnnotation an = (TimestampAnnotation) pamDataUnit.findDataAnnotation(TimestampAnnotation.class,
					stringAnnotationType.getAnnotationName());
			if (an == null) {
				an = new TimestampAnnotation(stringAnnotationType);
				pamDataUnit.addDataAnnotation(an);
			}
//			an.setString(textArea.getText());
		}
		return true;
	}
}
