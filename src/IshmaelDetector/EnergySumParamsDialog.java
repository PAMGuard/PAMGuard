/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
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
package IshmaelDetector;

import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fftManager.FFTDataUnit;


public class EnergySumParamsDialog extends IshDetParamsDialog  {

	static public final long serialVersionUID = 0;
	private static EnergySumParamsDialog singleInstance;
	JTextField f0Data, f1Data, ratioF0Data, ratioF1Data;
	JCheckBox logCheckBox;
	//JTextField threshData, minTimeData, refractoryTimeData;

	private EnergySumParamsDialog(Frame parentFrame, Class<FFTDataUnit> inputDataClass) {
		super(parentFrame, "Energy Sum Parameters", inputDataClass);
		setHelpPoint("detectors.ishmael.docs.ishmael_energysum");
	}

	public static EnergySumParams showDialog2(Frame parentFrame, EnergySumParams oldParams) 
	{
		//Create a new EnergySumParamsDialog if needed.
		if (singleInstance == null || singleInstance.getOwner() != parentFrame)
			singleInstance = new EnergySumParamsDialog(parentFrame, FFTDataUnit.class);
		return (EnergySumParams)showDialog3(parentFrame, oldParams, singleInstance);
	}
	
	@Override
	protected void addDetectorSpecificControls(JPanel g) {
		//Create the energy sum parameters panel.
		JPanel e = new JPanel();
		e.setBorder(BorderFactory.createTitledBorder("Energy Sum"));
		e.setLayout(new BoxLayout(e, BoxLayout.Y_AXIS));
		JPanel f = new JPanel();
		f.setBorder(BorderFactory.createEmptyBorder());
		f.setLayout(new BoxLayout(f, BoxLayout.X_AXIS));
		JPanel f1 = new JPanel();
		JPanel f2 = new JPanel();
		f1.setLayout(new BoxLayout(f1, BoxLayout.Y_AXIS));
		f2.setLayout(new BoxLayout(f2, BoxLayout.Y_AXIS));
		f1.add(new JLabel("Minimum Frequency "));
		f2.add(f0Data = new JTextField(8));
		f1.add(new JLabel("Maximum Frequency "));
		f2.add(f1Data = new JTextField(8));
		f.add(f1);
		f.add(f2);
		e.add(f);

		/*JPanel j = new JPanel();
		j.setBorder(BorderFactory.createEmptyBorder());
		j.setLayout(new BoxLayout(j, BoxLayout.X_AXIS));
		j.add(dbCheckBox = new JCheckBox("Use log-scaled spectrogram"));
		*/
		e.add(logCheckBox = new JCheckBox("Use log-scaled spectrogram"));
		
		g.add(e);
	}

	//Copy values from an EnergySumParams to the dialog box.
	@Override
	void setParameters() {
		super.setParameters();
		//Set the values that are specific to energy sum.
		EnergySumParams p = (EnergySumParams)ishDetParams;
		f0Data.setText(String.format("%g", p.f0));
		f1Data.setText(String.format("%g", p.f1));
		logCheckBox.setSelected(p.useLog);
	}

	@Override
	//Read the values from the dialog box, parse and place into energySumParams.
	public boolean getParams() {
		EnergySumParams p = (EnergySumParams)ishDetParams;
		try {
			super.getParams();
			p.f0 = Double.valueOf(f0Data.getText());
			p.f1 = Double.valueOf(f1Data.getText());
			p.useLog = logCheckBox.isSelected();			
		} catch (Exception ex) {
			return false;
		}
		
		//Do error-checking here.
		//if (EnergySumParams.isValidLength(energySumParams.fftLength)) {
		//	return true;
		//}
		return true;
	}
}
