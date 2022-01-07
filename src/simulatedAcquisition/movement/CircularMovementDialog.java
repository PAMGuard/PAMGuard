/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



package simulatedAcquisition.movement;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

/**
 * @author mo55
 *
 */
public class CircularMovementDialog extends PamDialog {
	
	private CircularMovementParams circularMovementParams;
	private static CircularMovementDialog singleInstance;
	private JTextField radiusMin;
	private JTextField radiusMax;
	private JTextField radiusStep;
	private JTextField depthMin;
	private JTextField depthMax;
	private JTextField depthStep;
	private JTextField angleStep;
	private JTextField numSounds;


	private CircularMovementDialog(Window parentFrame) {
		super(parentFrame, "Circular Movement options", false);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		// create constraints to fill in horizontal space
		GridBagConstraints horizC = new PamGridBagContraints();
		horizC.fill = GridBagConstraints.HORIZONTAL;
		horizC.anchor = GridBagConstraints.LINE_END;
		horizC.weightx = 1.0;
		horizC.gridwidth = GridBagConstraints.REMAINDER;
		
		// radius panel
		JPanel radiusPanel = new JPanel(new GridBagLayout());
		radiusPanel.setBorder(new TitledBorder("Circle Radius/Radii"));
		GridBagConstraints radiusC = new PamGridBagContraints();
		radiusC.gridx = 0;
		radiusC.gridy = 0;
		radiusC.gridwidth = 1;
		radiusC.anchor = GridBagConstraints.LINE_START;
		radiusPanel.add(new JLabel("Minimum", JLabel.CENTER), radiusC);
		radiusC.gridx++;
		radiusPanel.add(radiusMin = new JTextField(6),radiusC);
		radiusC.gridx++;
		radiusPanel.add(new JLabel("m", JLabel.LEFT), radiusC);
		radiusC.gridx = 0;
		radiusC.gridy++;
		radiusPanel.add(new JLabel("Maximum", JLabel.LEFT), radiusC);
		radiusC.gridx++;
		radiusPanel.add(radiusMax = new JTextField(6),radiusC);
		radiusC.gridx++;
		radiusPanel.add(new JLabel("m", JLabel.LEFT), radiusC);
		radiusC.gridx = 0;
		radiusC.gridy++;
		radiusPanel.add(new JLabel("Step Size", JLabel.CENTER), radiusC);
		radiusC.gridx++;
		radiusPanel.add(radiusStep = new JTextField(6),radiusC);
		radiusC.gridx++;
		radiusPanel.add(new JLabel("m", JLabel.LEFT), radiusC);
		mainPanel.add(radiusPanel);
		mainPanel.add(Box.createRigidArea(new Dimension(0,10)));
		
		// depth panel
		JPanel depthPanel = new JPanel(new GridBagLayout());
		depthPanel.setBorder(new TitledBorder("Circle Depth(s)"));
		GridBagConstraints depthC = new PamGridBagContraints();
		depthC.gridx = 0;
		depthC.gridy = 0;
		depthC.gridwidth = 1;
		depthC.anchor = GridBagConstraints.LINE_START;
		depthPanel.add(new JLabel("Minimum", JLabel.CENTER), depthC);
		depthC.gridx++;
		depthPanel.add(depthMin = new JTextField(6),depthC);
		depthC.gridx++;
		depthPanel.add(new JLabel("m", JLabel.LEFT), depthC);
		depthC.gridx = 0;
		depthC.gridy++;
		depthPanel.add(new JLabel("Maximum", JLabel.CENTER), depthC);
		depthC.gridx++;
		depthPanel.add(depthMax = new JTextField(6),depthC);
		depthC.gridx++;
		depthPanel.add(new JLabel("m", JLabel.LEFT), depthC);
		depthC.gridx = 0;
		depthC.gridy++;
		depthPanel.add(new JLabel("Step Size", JLabel.CENTER), depthC);
		depthC.gridx++;
		depthPanel.add(depthStep = new JTextField(6),depthC);
		depthC.gridx++;
		depthPanel.add(new JLabel("m", JLabel.LEFT), depthC);
		depthC.gridx = 0;
		depthC.gridy++;
		mainPanel.add(depthPanel);
		mainPanel.add(Box.createRigidArea(new Dimension(0,10)));
		
		// angle panel
		JPanel anglePanel = new JPanel(new GridBagLayout());
		anglePanel.setBorder(new TitledBorder("Angle Step around circumference"));
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_START;
		anglePanel.add(angleStep = new JTextField(6),c);
		c.gridx++;
		anglePanel.add(new JLabel("degrees", JLabel.CENTER), c);
		horizC.gridx = c.gridx+1;
		horizC.gridy = c.gridy;
		anglePanel.add(Box.createGlue(),horizC);
		mainPanel.add(anglePanel);
		mainPanel.add(Box.createRigidArea(new Dimension(0,10)));

		// number of sounds panel
		JPanel numSoundsPanel = new JPanel(new GridBagLayout());
		TitledBorder longestTB;
		numSoundsPanel.setBorder(longestTB = new TitledBorder("Number of sounds at each location"));
		c = new PamGridBagContraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
//		numSoundsPanel.add(new JLabel("Angle Step", JLabel.CENTER), c);
//		c.gridx++;
		numSoundsPanel.add(numSounds = new JTextField(6),c);
		horizC.gridx = c.gridx+1;
		horizC.gridy = c.gridy;
		numSoundsPanel.add(Box.createGlue(),horizC);
		mainPanel.add(numSoundsPanel);
		int width = this.getFontMetrics( longestTB.getTitleFont()).stringWidth(longestTB.getTitle());
		mainPanel.add(Box.createRigidArea(new Dimension(width+20,0)));

		// add the notes for the radius and depth.  Do that down here, because we know the max width now
		String radiusNote = String.format("<html><div align=\"center\" WIDTH=%d>For a single circle, set the minimum radius equal to the maximum radius.</div></html>",width-10);
		JLabel radiusLabel = new JLabel(radiusNote, JLabel.CENTER);
		radiusC.gridx = 0;
		radiusC.gridy++;
		radiusC.gridwidth = 3;
		radiusC.insets = new Insets(5, 5, 5, 5);
		radiusPanel.add(radiusLabel,radiusC);
		String depthNote = String.format("<html><div align=\"center\" WIDTH=%d>For a single depth, set the minimum depth equal to the maximum depth.</div></html>",width-10);
		JLabel depthLabel = new JLabel(depthNote, JLabel.CENTER);
		depthC.gridx = 0;
		depthC.gridy++;
		depthC.gridwidth = 3;
		depthC.insets = new Insets(5, 5, 5, 5);
		depthPanel.add(depthLabel,depthC);

		// set the dialog
		setDialogComponent(mainPanel);
		
}
	
	/**
	 * Show the dialog
	 * 
	 * @param owner
	 * @param circularMovementParams
	 * @return
	 */
	public static CircularMovementParams showDialog(Window owner, CircularMovementParams circularMovementParams) {
		if (singleInstance == null || singleInstance.getOwner() != owner) {
			singleInstance = new CircularMovementDialog(owner);
		}
		singleInstance.circularMovementParams = circularMovementParams.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.circularMovementParams;
	}

	private void setParams() {
		radiusMin.setText(String.format("%d", circularMovementParams.getRangeRange()[0]));
		radiusMax.setText(String.format("%d", circularMovementParams.getRangeRange()[1]));
		radiusStep.setText(String.format("%d", circularMovementParams.getRangeStep()));
		depthMin.setText(String.format("%d", circularMovementParams.getDepthRange()[0]));
		depthMax.setText(String.format("%d", circularMovementParams.getDepthRange()[1]));
		depthStep.setText(String.format("%d", circularMovementParams.getDepthStep()));
		angleStep.setText(String.format("%d", circularMovementParams.getAngleStep()));
		numSounds.setText(String.format("%d", circularMovementParams.getDirectionsPerPoint()));
	}

	/* (non-Javadoc)
	 * @see PamView.dialog.PamDialog#getParams()
	 */
	@Override
	public boolean getParams() {
		try {
			int[] radiusParams = new int[2];
			radiusParams[0] = Integer.valueOf(radiusMin.getText());
			radiusParams[1] = Integer.valueOf(radiusMax.getText());
			circularMovementParams.setRangeRange(radiusParams);
			circularMovementParams.setRangeStep(Integer.valueOf(radiusStep.getText()));
		}
		catch (NumberFormatException e) {
			return showWarning("Error in radius entry");
		}
		try {
			int[] depthParams = new int[2];
			String test = depthMin.getText();
			depthParams[0] = Integer.valueOf(depthMin.getText());
			depthParams[1] = Integer.valueOf(depthMax.getText());
			circularMovementParams.setDepthRange(depthParams);
			circularMovementParams.setDepthStep(Integer.valueOf(depthStep.getText()));
		}
		catch (NumberFormatException e) {
			return showWarning("Error in depth entry");
		}
		try {
			circularMovementParams.setAngleStep(Integer.valueOf(angleStep.getText()));
		}
		catch (NumberFormatException e) {
			return showWarning("Error in angle step entry");
		}
		try {
			circularMovementParams.setDirectionsPerPoint(Integer.valueOf(numSounds.getText()));
		}
		catch (NumberFormatException e) {
			return showWarning("Error in number of sounds");
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see PamView.dialog.PamDialog#cancelButtonPressed()
	 */
	@Override
	public void cancelButtonPressed() {
		circularMovementParams = null;
	}

	/* (non-Javadoc)
	 * @see PamView.dialog.PamDialog#restoreDefaultSettings()
	 */
	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
