package simulatedAcquisition.movement;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class GridMovementDialog extends PamDialog {

	private JTextField[] rangeMin = new JTextField[3];
	private JTextField[] rangeMax = new JTextField[3];
	private JTextField[] rangeStep = new JTextField[3];
	private JTextField numAngles;
	private GridMovementParams gridMovementParams;
	private static GridMovementDialog singleInstance;
	
	private GridMovementDialog(Window parentFrame) {
		super(parentFrame, "Grid Movement options", false);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		JPanel xyzPanel = new JPanel(new GridBagLayout());
		xyzPanel.setBorder(new TitledBorder("Movement Steps"));
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = 1;
		c.gridwidth = 3;
		xyzPanel.add(new JLabel("Range", JLabel.CENTER), c);
		c.gridx += c.gridwidth + 1;
		c.gridwidth = 2;
		xyzPanel.add(new JLabel("Step Size", JLabel.CENTER), c);
		c.gridwidth = 0;
		c.gridwidth = 1;
		for (int i = 0; i < 3; i++) {
			c.gridx = 0;
			c.gridy ++;
			xyzPanel.add(new JLabel(GridMovementParams.dimNames[i] + " ", JLabel.RIGHT), c);
			c.gridx++;
			xyzPanel.add(rangeMin[i] = new JTextField(6), c);
			c.gridx++;
			xyzPanel.add(new JLabel(" to ", JLabel.CENTER), c);
			c.gridx++;
			xyzPanel.add(rangeMax[i] = new JTextField(6), c);
			c.gridx++;
			xyzPanel.add(new JLabel(",   ", JLabel.CENTER), c);
			c.gridx++;
			xyzPanel.add(rangeStep[i] = new JTextField(6), c);
			c.gridx++;
			xyzPanel.add(new JLabel(" m", JLabel.LEFT), c);
		}
		mainPanel.add(xyzPanel);
		
		JPanel angPanel = new JPanel(new GridBagLayout());
		angPanel.setBorder(new TitledBorder("Heading"));
		c = new PamGridBagContraints();
		angPanel.add(new JLabel("Number of sounds at each location ", JLabel.RIGHT), c);
		angPanel.add(numAngles = new JTextField(3));
		mainPanel.add(angPanel);
		
		
		
		setDialogComponent(mainPanel);
		
	}
	
	public static GridMovementParams showDialog(Window owner, GridMovementParams gridMovementParams) {
		if (singleInstance == null || singleInstance.getOwner() != owner) {
			singleInstance = new GridMovementDialog(owner);
		}
		singleInstance.gridMovementParams = gridMovementParams.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.gridMovementParams;
	}

	private void setParams() {
		for (int i = 0; i < 3; i++) {
			rangeMin[i].setText(String.format("%d", gridMovementParams.distRangeMetres[i][0]));
			rangeMax[i].setText(String.format("%d", gridMovementParams.distRangeMetres[i][1]));
			rangeStep[i].setText(String.format("%3.1f", gridMovementParams.distStepsMetres[i]));
		}
		numAngles.setText(String.format("%d", gridMovementParams.directionsPerPoint));
	}

	@Override
	public boolean getParams() {
		for (int i = 0; i < 3; i++) {
			try {
				gridMovementParams.distRangeMetres[i][0] = Integer.valueOf(rangeMin[i].getText());
				gridMovementParams.distRangeMetres[i][1] = Integer.valueOf(rangeMax[i].getText());
				gridMovementParams.distStepsMetres[i] = Double.valueOf(rangeStep[i].getText());
			}
			catch (NumberFormatException e) {
				return showWarning(String.format("Error in %s coordinate parameter", GridMovementParams.dimNames[i]));
			}
		}
		try {
			gridMovementParams.directionsPerPoint = Integer.valueOf(numAngles.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Error in number of sounds");
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		gridMovementParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
