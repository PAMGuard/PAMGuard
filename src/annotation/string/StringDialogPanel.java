package annotation.string;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import PamUtils.PamCalendar;
import annotation.AnnotationDialogPanel;
import PamView.DBTextArea;
import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataUnit;

public class StringDialogPanel implements AnnotationDialogPanel {

	private StringAnnotationType stringAnnotationType;
	
	private DBTextArea textArea;
	
	public StringDialogPanel(StringAnnotationType stringAnnotationType) {
		super();
		this.stringAnnotationType = stringAnnotationType;
		textArea = new DBTextArea(2, 25, stringAnnotationType.getMaxLength());
	}

	@Override
	public JComponent getDialogComponent() {
		return textArea.getComponent();
	}

	@Override
	public void setParams(PamDataUnit pamDataUnit) {
		StringAnnotation an = (StringAnnotation) pamDataUnit.findDataAnnotation(StringAnnotation.class, 
				stringAnnotationType.getAnnotationName());
		if (an == null) {
			textArea.setText(null);
		}
		else {
			textArea.setText(an.getString());
		}
	}

	@Override
	public boolean getParams(PamDataUnit pamDataUnit) {
		String note = textArea.getText();
		if (note != null) {
			note = note.trim();
		}
		if (note != null && note.length() > 0) {
			StringAnnotation an;
			/*
			 *  always add a new annotation rather than editing the old one since
			 *  the old one will get removed to handle annotation types which really 
			 *  do need to make a new one each time. 
			 */
			
//			= (StringAnnotation) pamDataUnit.findDataAnnotation(StringAnnotation.class,
//					stringAnnotationType.getAnnotationName());
//			if (an == null) {
				an = new StringAnnotation(stringAnnotationType);
				pamDataUnit.addDataAnnotation(an);
//			}
			an.setString(textArea.getText());
			pamDataUnit.setLastUpdateTime(PamCalendar.getTimeInMillis());
		}
		return true;
	}
}
