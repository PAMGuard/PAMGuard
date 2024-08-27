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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.util.ArrayList;

//import javax.swing.BorderFactory;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
//import javax.swing.JButton;
//import javax.swing.JCheckBox;
//import javax.swing.JComboBox;
//import javax.swing.JDialog;
//import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
//import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import PamController.PamController;
import PamDetection.PamDetection;
import PamView.dialog.PamDialog;
import PamView.dialog.SourcePanel;
import PamguardMVC.PamDataBlock;
import warnings.PamWarning;
import warnings.WarningSystem;

public abstract class IshLocParamsDialog extends PamDialog implements ActionListener 
{
	public IshLocParams ishLocParams; //actually an IshLocHyperbParams, IshLocPairParams, etc.
	SourcePanel sourcePanel;
	JTextField tBeforeData, tAfterData;
	JCheckBox detectorCheckBox;
	private static PamWarning IshWarning = new PamWarning("Ishmael Localiser", "", 2);
	//private static IshDetParamsDialog singleInstance;
	//JComboBox sourceList;

	public static IshLocParams showDialog3(Frame parentFrame,
			IshLocParams oldParams, IshLocParamsDialog singleInstance) 
	{
		singleInstance.ishLocParams = oldParams.clone();
		singleInstance.setParameters();
		singleInstance.setVisible(true);  //causes wait until OK/Cancel clicked
		
		return singleInstance.ishLocParams;
	}

	protected IshLocParamsDialog(Frame parentFrame, String dialogName) {
		super(parentFrame, dialogName, false);
		
		JPanel g = new JPanel();
		g.setBorder(new EmptyBorder(10,10,10,10));
		g.setLayout(new BoxLayout(g, BoxLayout.Y_AXIS));

		//Create checkbox.  Can't figure out how to left-justify it.
		detectorCheckBox = new JCheckBox("Get input from a detector?");
		detectorCheckBox.addActionListener(new IshLocActionListener(parentFrame));
		g.add(BorderLayout.WEST, detectorCheckBox);
		
		//Specify where localizations will come from.  Anything that outputs PamDetections
		//should work.
		sourcePanel = new SourcePanel(this, "Data Source", PamDetection.class, true, true);
		g.add(sourcePanel.getPanel());

		JPanel f = new JPanel();
		f.setBorder(BorderFactory.createTitledBorder("Auto detection"));
		f.setLayout(new BoxLayout(f, BoxLayout.X_AXIS));
		JPanel f1 = new JPanel();
		JPanel f2 = new JPanel();
		f1.setLayout(new BoxLayout(f1, BoxLayout.Y_AXIS));
		f2.setLayout(new BoxLayout(f2, BoxLayout.Y_AXIS));
		f1.add(new JLabel("Time before detection, s "));
		f2.add(tBeforeData = new JTextField(8));
		f1.add(new JLabel("Time after detection, s "));
		f2.add(tAfterData = new JTextField(8));
		f.add(f1);
		f.add(f2);
		g.add(f);
		
		this.addLocatorSpecificControls(g);
		
		setDialogComponent(g);
	}

	class IshLocActionListener implements ActionListener {
		Frame parentFrame;
		public IshLocActionListener(Frame parentFrame) {
			this.parentFrame = parentFrame;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			enableFromCheckBox();
		}
	}
	
	/** Using detectorCheckBox, set the enabled state of the sourcePanel and
	 * the time measures.
	 */
	private void enableFromCheckBox() {
		boolean b = detectorCheckBox.isSelected();
		
		sourcePanel.setEnabled(b);
		tBeforeData.setEnabled(b);
		tAfterData .setEnabled(b);
	}
	
	/** Add parameter-setting controls to the dialog box that are specific to the
	 * type of detector in your subclass. Some controls are common to all
	 * detectors; they are handled in IshDetParamsDialog. But others are
	 * specific to one type of detector, and in subclasses, they should be added
	 * in addDetectorSpecificControls.  See EnergySumParamsDialog for an example.
	 * 
	 * @param  g -- the panel to which you should add controls.  Calling
	 * add will append them vertically; to use horizontal placement, make a
	 * sub-panel.
	 */
	protected abstract void addLocatorSpecificControls(JPanel g);
	
	/*
	void showChannelList() {
		PamDataBlock inputDataBlock = (PamDataBlock)sourceList.getSelectedItem();
		int channelMap = 
			(inputDataBlock == null) ? 0xFFFF : inputDataBlock.getChannelMap();
 		
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			selectChannel[i].setVisible((1<<i & channelMap) > 0 && inputDataBlock != null);
		}
		pack();
	}
	*/

	@Override
	public void actionPerformed(ActionEvent e) {
		//if (e.getSource() == sourceList) {
			//showChannelList();
		//}
	}

	//Copy values from an IshLocParams to the dialog box.
	void setParameters() {
		//Set up the choice box for the input stream and channels.
		detectorCheckBox.setSelected(ishLocParams.useDetector);
		sourcePanel.setSourceList();
		sourcePanel.setChannelList(ishLocParams.channelList);
		sourcePanel.setSource(ishLocParams.inputDataSource);
		
		tBeforeData.setText(String.format("%g", ishLocParams.tBefore));
		tAfterData .setText(String.format("%g", ishLocParams.tAfter));
		
		enableFromCheckBox();
		
		//String src = ishLocParams.inputDataSource;
		/*
		PamDataBlock inputDataBlock = 
			PamController.getInstance().getDataBlock(null, src);
		ArrayList<PamDataBlock> possibleInputBlocks = 
			PamController.getInstance().getDataBlocks(inputDataBlock.getDataType());
		sourceList.removeAllItems();
		for (int i = 0; i < possibleInputBlocks.size(); i++)
			sourceList.addItem(possibleInputBlocks.get(i));
		sourceList.setSelectedItem(inputDataBlock);

		//Set the checkboxes for the channels to process.
		showChannelList();
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			selectChannel[i].setSelected((ishLocParams.channelList & 1<<i) > 0);
		}
		*/
	}

	/** Read the values from the dialog box, parse and place into this dialog box's
	 * ishLocParams. The subclasses, before or after calling super.getParams(), do
	 * likewise for their detector-specific values.
	 * @return boolean -- true if parameters are valid, false if not
	 */ 
	@Override
	public boolean getParams() {
		
		// quick check - if the source has sequence numbers, we're not going to know which channels to use for localization.  Warn the user and exit
		PamDataBlock source = PamController.getInstance().getDataBlock(PamDetection.class, sourcePanel.getSourceName());
		if (detectorCheckBox.isSelected() && source.getSequenceMapObject()!=null) {
			String err = "Error: the selected Source Detector uses Beamformer output as a data source, and Beamformer output does not contain "
			+ "the link back to a single channel of raw audio data that is required for analysis.  Please either change the Source "
			+ "Detector's data source, or select a different Detector.";
			IshWarning.setWarningMessage(err);
			WarningSystem.getWarningSystem().addWarning(IshWarning);
			return false;
		} else {
			WarningSystem.getWarningSystem().removeWarning(IshWarning);
		}
		
		
		try {
			ishLocParams.useDetector = detectorCheckBox.isSelected();
			ishLocParams.inputDataSource = sourcePanel.getSource().toString();
			ishLocParams.channelList     = sourcePanel.getChannelList();
			ishLocParams.tBefore = Double.valueOf(tBeforeData.getText());
			ishLocParams.tAfter  = Double.valueOf(tAfterData .getText());
			/*
			ishLocParams.inputDataSource = sourceList.getSelectedItem().toString();
			ishLocParams.channelList = 0;
			for (int i = 0; i < PamConstants.MAX_CHANNELS; i++){
				if (selectChannel[i].isVisible()) {
					if (selectChannel[i].isSelected()) {
						ishLocParams.channelList |= 1<<i;
					}
				}
			}
			*/
		} catch (Exception ex) {
			return false;
		}
		
		//Error checking.
		if (ishLocParams.channelList == 0)
			return false;
		
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		ishLocParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		
	}

}
