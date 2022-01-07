package PamView.symbol;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamGridBagContraints;

public class AnnotationSymboloptsPanel implements PamDialogPanel {

	private AnnotationSymbolOptions annotationSymbolOptions;
	
	private JPanel mainPanel;
	
	private JCheckBox lineColour, fillColour, symbol;
	
	public AnnotationSymboloptsPanel(AnnotationSymbolOptions annotationSymbolOptions) {
		super();
		this.annotationSymbolOptions = annotationSymbolOptions;
		mainPanel = new JPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder("Annotation symbol options"));
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(lineColour = new JCheckBox("Change symbol border colour"), c);
		c.gridy++;
		mainPanel.add(fillColour = new JCheckBox("Change symbol fill colour"), c);
		c.gridy++;
		mainPanel.add(symbol = new JCheckBox("Change symbol shape"), c);
		mainPanel.setToolTipText("Note that some annotation types may not support all of these options");
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		lineColour.setSelected((annotationSymbolOptions.changeChoice & AnnotationSymbolOptions.CHANGE_LINE_COLOUR) != 0);
		fillColour.setSelected((annotationSymbolOptions.changeChoice & AnnotationSymbolOptions.CHANGE_FILL_COLOUR) != 0);
		symbol.setSelected((annotationSymbolOptions.changeChoice & AnnotationSymbolOptions.CHANGE_SYMBOL) != 0);
	}

	@Override
	public boolean getParams() {
		annotationSymbolOptions.changeChoice = 0;
		if (lineColour.isSelected()) {
			annotationSymbolOptions.changeChoice |= AnnotationSymbolOptions.CHANGE_LINE_COLOUR;
		}
		if (fillColour.isSelected()) {
			annotationSymbolOptions.changeChoice |= AnnotationSymbolOptions.CHANGE_FILL_COLOUR;
		}
		if (symbol.isSelected()) {
			annotationSymbolOptions.changeChoice |= AnnotationSymbolOptions.CHANGE_SYMBOL;
		}
		return true;
	}

}
