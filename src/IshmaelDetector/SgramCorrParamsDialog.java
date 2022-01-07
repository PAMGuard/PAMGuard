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
import javax.swing.SwingConstants;

import fftManager.FFTDataUnit;

public class SgramCorrParamsDialog extends IshDetParamsDialog  {

	static public final long serialVersionUID = 0;
	private static SgramCorrParamsDialog singleInstance;
	JTextField segmentData[][];
	JTextField widthData;
	JPanel linePanel;
	JCheckBox logCheckBox;

	private SgramCorrParamsDialog(Frame parentFrame, Class inputDataClass) {
		super(parentFrame, "Spectrogram Correlation Parameters", inputDataClass);
	}

	public static SgramCorrParams showDialog2(Frame parentFrame, SgramCorrParams oldParams) 
	{
		//Create a new SgramCorrParamsDialog if needed.
		if (singleInstance == null || singleInstance.getOwner() != parentFrame)
			singleInstance = new SgramCorrParamsDialog(parentFrame, FFTDataUnit.class);
		return (SgramCorrParams)showDialog3(parentFrame, oldParams, singleInstance);
	}
	
	@Override
	protected void addDetectorSpecificControls(JPanel g) {
		SgramCorrParams p = (SgramCorrParams)ishDetParams;
		
		//Create the energy sum parameters panel.
		JPanel e = new JPanel();
		e.setBorder(BorderFactory.createTitledBorder("Spectrogram Correlation"));
		e.setLayout(new BoxLayout(e, BoxLayout.Y_AXIS));
		
		//linePanel has the t0/f0/t1/f1 data.  It is populated in setParameters().
		linePanel = new JPanel();
		linePanel.setBorder(BorderFactory.createEmptyBorder());
		linePanel.setLayout(new BoxLayout(linePanel, BoxLayout.Y_AXIS));
		//Add enough JTextFields to make dialog box big enough.
		for (int i = 0; i <(p==null ? 5 : p.segment.length+4); i++) 
			linePanel.add(new JTextField(8));
		e.add(linePanel);
		
		JPanel f = new JPanel();
		f.setBorder(BorderFactory.createEmptyBorder());
		f.setLayout(new BoxLayout(f, BoxLayout.X_AXIS));
		JPanel f1 = new JPanel();
		JPanel f2 = new JPanel();
		f1.setLayout(new BoxLayout(f1, BoxLayout.Y_AXIS));
		f2.setLayout(new BoxLayout(f2, BoxLayout.Y_AXIS));
		f1.add(new JLabel("Kernel Width, Hz "));
		f2.add(widthData = new JTextField(8));
		f.add(f1);
		f.add(f2);
		e.add(f);

		e.add(logCheckBox = new JCheckBox("Use log-scaled spectrogram"));
		
		g.add(e);
	}

	//Copy values from an SgramCorrParams to the dialog box.
	@Override
	void setParameters() {
		super.setParameters();
		//Set values specific to SgramCorr.
		SgramCorrParams p = (SgramCorrParams)ishDetParams;
		widthData.setText(String.format("%g", p.spread));
		logCheckBox.setSelected(p.useLog);
		
		//Populate linePanel with JTextFields.  Always make 4 blank lines
		//for segments to be added.
		linePanel.removeAll();
		JPanel lines[] = new JPanel[p.segment.length + 4];  //4 blank lines
		segmentData = new JTextField[lines.length][4]; 
		for (int i = -1; i < lines.length; i++) {
			JPanel line = new JPanel();
			line.setBorder(BorderFactory.createEmptyBorder());
			line.setLayout(new BoxLayout(line, BoxLayout.X_AXIS));
			JLabel lab = new JLabel("Segment " + (i+1) + " ");
			line.add(lab);
			for (int j = 0; j < 4; j++) {
				if (i == -1) {			//special case
					//Alignment is the headache here. Use a "Segment 0" label,
					//the same size as the other ones, so it aligns right, but
					//make it invisible.  And create t0/f0/t1/f1 as JTextFields,
					//just like succeeding lines, so they align right.
					lab.setForeground(lab.getBackground());  //make invisible!
					JTextField txt = new JTextField(8);
					line.add(txt);
					txt.setText(j==0 ? "t0" : j==1 ? "f0" : j==2 ? "t1" : "f1");
					txt.setEditable(false);
					txt.setHorizontalAlignment(SwingConstants.CENTER);
					txt.setBorder(BorderFactory.createEmptyBorder());
				} else {
					line.add(segmentData[i][j] = new JTextField(8));
					//Write values from p.segment into the text boxes.
					if (i < p.segment.length)
						segmentData[i][j].setText(
								String.format("%g", p.segment[i][j]));
				}
			}
			linePanel.add(line);
		}
		linePanel.validate();
	}

	@Override
	//Read the values from the dialog box, parse and place into sgramCorrParams.
	public boolean getParams() {
		SgramCorrParams p = (SgramCorrParams)ishDetParams;
		try {
			super.getParams();
			p.spread = Double.valueOf(widthData.getText());
			p.useLog = logCheckBox.isSelected();	
			
			//First count lines with stuff in them to get size of p.segment.
			int nSegments = 0;
			boolean[] segPresent = new boolean[segmentData.length];
			for (int i = 0; i < segmentData.length; i++) {
				segPresent[i] = false;
				for (int j = 0; j < 4; j++) {
					if (segmentData[i][j].getText().trim().length() > 0) {
						nSegments++;
						segPresent[i] = true;
						break;  //break out of j loop
					}
				}
			}
			p.segment = new double[nSegments][4];
			//p.nSegments = nSegments;
			
			//Now copy values to the new p.segment.
			//double t = Double.NEGATIVE_INFINITY; //is t allowed to decrease?
			int pSegI = 0;		//which row of p.segment[] to copy values to
			for (int i = 0; i < segmentData.length; i++) {
				if (segPresent[i]) {
					for (int j = 0; j < 4; j++)
						p.segment[pSegI][j] = 
							Double.valueOf(segmentData[i][j].getText());
					pSegI++;
				}
			}
		} catch (Exception ex) {
			return false;
		}
		
		//Do error-checking here.
		//if (SgramCorrParams.isValidLength(sgramCorrParams.fftLength)) {
		//	return true;
		//}
		return true;
	}
}
