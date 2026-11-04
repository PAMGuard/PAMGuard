package annotation.calcs.measurement;

import java.awt.Color;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;

import PamguardMVC.PamDataUnit;
import annotation.AnnotationDialogPanel;

/**
 * Read only panel showing the values of a list of sound measurements together, e.g. in
 * an annotation dialog, rather than one panel per measurement.
 *
 * @author Jamie Macaulay
 */
public class SoundMeasurementSummaryPanel implements AnnotationDialogPanel {

	private JTextPane textPane;

	private List<SoundMeasurementAnnotationType> types;

	public SoundMeasurementSummaryPanel(List<SoundMeasurementAnnotationType> types) {
		this.types = types;
		textPane = new JTextPane();
		textPane.setContentType("text/html");
		textPane.setEditable(false);
		textPane.setOpaque(false);
		textPane.setBorder(null);
		textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
		textPane.setFont(new JLabel(" ").getFont());
		setHtml(makeHtml(null));
	}

	/**
	 * Set the html text, pushing the look and feel text colour into the style sheet so
	 * the text can be seen in a dark colour scheme (see SPLAnnotationPanel).
	 */
	private void setHtml(String html) {
		Color fg = UIManager.getColor("Label.foreground");
		if (fg == null) {
			fg = new JLabel().getForeground();
		}
		textPane.setForeground(fg);
		Document doc = textPane.getDocument();
		if (doc instanceof HTMLDocument) {
			((HTMLDocument) doc).getStyleSheet().addRule(
					String.format("body {color: #%06x;}", fg.getRGB() & 0xFFFFFF));
		}
		textPane.setText(html);
	}

	private String makeHtml(PamDataUnit dataUnit) {
		StringBuilder sb = new StringBuilder("<html><table border=\"0\" cellpadding=\"0\" cellspacing=\"2\">");
		for (SoundMeasurementAnnotationType type : types) {
			SoundMeasurementAnnotation an = dataUnit == null ? null : type.findAnnotation(dataUnit);
			sb.append(String.format("<tr><td>%s</td><td align='right'>%s</td></tr>", type.getAnnotationName(),
					an == null ? "-" : an.toString()));
		}
		sb.append("</table></html>");
		return sb.toString();
	}

	@Override
	public JComponent getDialogComponent() {
		return textPane;
	}

	@Override
	public void setParams(PamDataUnit pamDataUnit) {
		setHtml(makeHtml(pamDataUnit));
	}

	@Override
	public boolean getParams(PamDataUnit pamDataUnit) {
		return true;
	}

}
