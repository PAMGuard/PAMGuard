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



package PamController;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import PamView.dialog.PamDialog;
import javafx.scene.layout.Border;

/**
 * @author mo55
 *
 */
public class DisplayScalingDialog extends PamDialog {

	private static final long serialVersionUID = 1L;

	private static DisplayScalingDialog singleInstance;

	JTextField scalingFactor;
	
	double currentScaling;

	private DisplayScalingDialog(Frame parentFrame, double currentScaling) {
		super(parentFrame, "Display Scaling", true);
		
		this.currentScaling = currentScaling;
		
		JPanel mainWindow = new JPanel();
		mainWindow.setLayout(new BorderLayout(5,10));
		mainWindow.setBorder(new EmptyBorder(10, 10, 10, 10));
		JLabel intro = new JLabel("<html><div WIDTH=300>Please enter the scaling factor that PAMGuard should apply to the display elements."+
				"  A scaling factor of 1.0 indicates no scaling.  A scaling factor of 2.0 indicates elements should be displayed" +
				" at twice their normal size.</div></html>");
		mainWindow.add(intro, BorderLayout.PAGE_START);
		JLabel lbl = new JLabel("New Scaling:");
		mainWindow.add(lbl, BorderLayout.WEST);
		
		scalingFactor = new JTextField(String.valueOf(currentScaling));
		mainWindow.add(scalingFactor, BorderLayout.CENTER);
		
		setDialogComponent(mainWindow);
	}
	
	public static double showDialog(Frame parentFrame, double currentScalingFactor ) {
		
		if (singleInstance == null || parentFrame != singleInstance.getOwner()) {
			singleInstance = new DisplayScalingDialog(parentFrame, currentScalingFactor);
		}
		singleInstance.setVisible(true);
		return singleInstance.currentScaling;
	}
		

		/* (non-Javadoc)
	 * @see PamView.dialog.PamDialog#getParams()
	 */
	@Override
	public boolean getParams() {
		try {
			currentScaling = Double.valueOf(scalingFactor.getText());
		}
		catch (NumberFormatException ex) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see PamView.dialog.PamDialog#cancelButtonPressed()
	 */
	@Override
	public void cancelButtonPressed() {
	}

	/* (non-Javadoc)
	 * @see PamView.dialog.PamDialog#restoreDefaultSettings()
	 */
	@Override
	public void restoreDefaultSettings() {
		currentScaling = 1.0;
		scalingFactor.setText("1.0");
	}

}
