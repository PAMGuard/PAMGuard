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
package IshmaelLocator;

import java.awt.Frame;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import PamguardMVC.PamDataBlock;

/* Dialog box extensions for the parameters relevant to hyperbolic
 * localization.
 * @author Dave Mellinger
 */
public class IshLocHyperbParamsDialog extends IshLocParamsDialog implements ActionListener {

	static public final long serialVersionUID = 0;
	private static IshLocHyperbParamsDialog singleInstance;
	JTextField nDimsData;

	private IshLocHyperbParamsDialog(Frame parentFrame) {
		super(parentFrame, "Hyperbolic Localization Parameters");
	}

	public static IshLocHyperbParams showDialog2(Frame parentFrame, 
			IshLocHyperbParams oldParams, PamDataBlock outputDataBlock) 
	{
	//Create a new IshLocHyperbParamsDialog if needed.
	if (singleInstance == null || singleInstance.getOwner() != parentFrame)
			singleInstance = new IshLocHyperbParamsDialog(parentFrame);
		//Stop circularity -- don't let me subscribe to my own output.
		if (outputDataBlock != null)
			singleInstance.sourcePanel.excludeDataBlock(outputDataBlock, true);
		return (IshLocHyperbParams)showDialog3(parentFrame, oldParams, singleInstance);
	}
	
	@Override
	protected void addLocatorSpecificControls(JPanel g) {
		//Create the hyperbolic loc parameters panel.
		JPanel e = new JPanel();
		e.setBorder(BorderFactory.createTitledBorder("Hyperbolic Localization"));
		e.setLayout(new BoxLayout(e, BoxLayout.Y_AXIS));
		JPanel f = new JPanel();
		f.setBorder(BorderFactory.createEmptyBorder());
		f.setLayout(new BoxLayout(f, BoxLayout.X_AXIS));
		JPanel f1 = new JPanel();
		JPanel f2 = new JPanel();
		f1.setLayout(new BoxLayout(f1, BoxLayout.Y_AXIS));
		f2.setLayout(new BoxLayout(f2, BoxLayout.Y_AXIS));
		f1.add(new JLabel("Number of Dimensions "));
		f2.add(nDimsData = new JTextField(8));
		f.add(f1);
		f.add(f2);
		e.add(f);
		g.add(e);
	}

	//Copy values from an IshLocHyperbParams to the dialog box.
	@Override
	void setParameters() {
		super.setParameters();
		//Set the values that are specific to hyperbolic loc.
		IshLocHyperbParams p = (IshLocHyperbParams)ishLocParams;
		nDimsData.setText(String.format("%d", p.nDimensions));
	}

	@Override
	//Read the values from the dialog box, parse and place into ishLocParams.
	public boolean getParams() {
		IshLocHyperbParams p = (IshLocHyperbParams)ishLocParams;   //shorthand
		try {
			if (!super.getParams())
				return false;
			p.nDimensions = Integer.valueOf(nDimsData.getText());		
		} catch (Exception ex) {
			return false;
		}
		
		//Check for errors.
		if (p.nDimensions != 2 && p.nDimensions != 3)
			return false;
		
		return true;
	}
}
