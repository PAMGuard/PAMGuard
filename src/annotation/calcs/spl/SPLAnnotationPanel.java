package annotation.calcs.spl;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JTextPane;

import PamguardMVC.PamDataUnit;
import annotation.AnnotationDialogPanel;

public class SPLAnnotationPanel implements AnnotationDialogPanel {

	private SPLAnnotationType splAnnotationType;
	
	private JTextPane splLabel;

	public SPLAnnotationPanel(SPLAnnotationType splAnnotationType) {
		super();
		this.splAnnotationType = splAnnotationType;
		splLabel = new JTextPane();
		splLabel.setContentType("text/html"); // let the text pane know this is what you want
		splLabel.setText(defaultString()); // showing off
		splLabel.setEditable(false); // as before
		splLabel.setBackground(null); // this is the same as a JLabel
		splLabel.setBorder(null); // remove the border
		splLabel.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		splLabel.setFont(new JLabel(" ").getFont());
		
	}
	
	private String defaultString() {
		 return String.format("<html><table border=\"0\" cellpadding=\"0\" cellspacing=\"2\" width=\"200\">" +
					"<tr><td>RMS         </td><<td align='right'>-</td></tr>" + 
					"<tr><td>Zero-to-Peak</td><td align='right'>-</td></tr>" + 
					"<tr><td>Peak-to-Peak</td><td align='right'>-</td></tr>" + 
					"<tr><td>SEL         </td><td align='right'>-</td></tr>" + 
					"</table></html>");
	}

	@Override
	public JComponent getDialogComponent() {
		return splLabel;

	}

	@Override
	public void setParams(PamDataUnit pamDataUnit) {
		SPLAnnotation splAn = (SPLAnnotation) pamDataUnit.findDataAnnotation(SPLAnnotation.class);
		if (splAn == null) {
			splLabel.setText(defaultString());
		}
		else {
			splLabel.setText(splAn.toString());
		}
	}

	@Override
	public boolean getParams(PamDataUnit pamDataUnit) {
		return true;
	}

}
