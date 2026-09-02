package annotation.calcs.spl;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;

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
		splLabel.setEditable(false); // as before
		splLabel.setOpaque(false); // this is the same as a JLabel - let the panel behind show through
		splLabel.setBorder(null); // remove the border
		splLabel.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		splLabel.setFont(new JLabel(" ").getFont());
		setHtml(defaultString());
	}

	/**
	 * Set the html text, first pushing the current look and feel text colour into
	 * the document style sheet.
	 * <p>
	 * The html rendered by a text pane defaults to black text whatever the colour
	 * of the component, which is invisible in a dark colour scheme, so the colour
	 * has to be given to the style sheet explicitly. This is done on every update
	 * rather than once at construction since the colour scheme can change while the
	 * dialog exists.
	 *
	 * @param html text to display
	 */
	private void setHtml(String html) {
		Color fg = UIManager.getColor("Label.foreground");
		if (fg == null) {
			fg = new JLabel().getForeground();
		}
		splLabel.setForeground(fg);
		/*
		 * JEditorPane.setText re-uses the existing document, so the rule added here
		 * will still be in place when the text is inserted.
		 */
		Document doc = splLabel.getDocument();
		if (doc instanceof HTMLDocument) {
			((HTMLDocument) doc).getStyleSheet().addRule(
					String.format("body {color: #%06x;}", fg.getRGB() & 0xFFFFFF));
		}
		splLabel.setText(html);
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
			setHtml(defaultString());
		}
		else {
			setHtml(splAn.toString());
		}
	}

	@Override
	public boolean getParams(PamDataUnit pamDataUnit) {
		return true;
	}

}
