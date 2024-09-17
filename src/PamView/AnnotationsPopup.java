package PamView;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Window;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamDataBlock;
import PamguardMVC.ProcessAnnotation;

public class AnnotationsPopup extends PamDialog {

	public AnnotationsPopup(Window parentFrame, Point location, PamDataBlock pamDataBlock) {
		super(parentFrame, pamDataBlock.getDataName(), false);

		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		Vector<ProcessAnnotation> annotations = pamDataBlock.getProcessAnnotations();
		ProcessAnnotation a;
		c.gridwidth = 4;
		addComponent(p, new JLabel("Processes applied to data ..."), c);
		c.gridwidth = 1;
		for (int i = 0; i < annotations.size(); i++) {
			c.gridy++;
			c.gridx = 0;
			a = annotations.get(i);
			addComponent(p, new JLabel(String.format("%d. ", i+1)), c);
			c.gridx++;
			addComponent(p, new JLabel(a.getType()), c);
			c.gridx++;
			addComponent(p, new JLabel(" - "), c);
			c.gridx++;
			addComponent(p, new JLabel(a.getName()), c);
		}
		
		setDialogComponent(p);
		getCancelButton().setVisible(false);
		setLocation(location);
		setVisible(true);
	}

	@Override
	public void cancelButtonPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getParams() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
