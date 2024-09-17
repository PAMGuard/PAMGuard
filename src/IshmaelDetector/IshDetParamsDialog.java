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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import PamView.dialog.GroupedSourcePanel;
import PamView.dialog.PamDialog;

public abstract class IshDetParamsDialog extends PamDialog 
{
	public IshDetParams ishDetParams; //actually an EnergySumParams, SgramCorrParams, etc.
	//private static IshDetParamsDialog singleInstance;
//	JComboBox sourceList;
	GroupedSourcePanel sourcePanel;
	JTextField vsData;
	JTextField threshData, minTimeData, refractoryTimeData;
//	JCheckBox[] selectChannel;

	public static IshDetParams showDialog3(Frame parentFrame,
			IshDetParams oldParams, IshDetParamsDialog singleInstance) 
	{
		singleInstance.ishDetParams = oldParams.clone();
		singleInstance.setParameters();
		singleInstance.pack();
		singleInstance.setVisible(true);  //causes wait until OK/Cancel clicked
		
		return singleInstance.ishDetParams;
	}

	protected IshDetParamsDialog(Frame parentFrame, String dialogName, 
			Class inputDataClass) 
	{
		super(parentFrame, dialogName, false);
		
		JPanel g = new JPanel();
		g.setBorder(new EmptyBorder(10,10,10,10));
		g.setLayout(new BoxLayout(g, BoxLayout.Y_AXIS));
		
		//Create "Data Source" panel, populate it with checkboxes for all possible
		//channels.
//		JPanel h = new JPanel();
//		h.setBorder(BorderFactory.createTitledBorder("Data Source"));
//		h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS));
//		h.add(sourceList = new JComboBox());
//		sourceList.addActionListener(this);
//		h.add(new JLabel("Channel list ..."));
//		selectChannel = new JCheckBox[PamConstants.MAX_CHANNELS];
//		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++){
//			selectChannel[i] = new JCheckBox("Channel " + i);
//			h.add(selectChannel[i]);
//		}
//		g.add(h);
		
		sourcePanel = new GroupedSourcePanel(this, "Data Source", inputDataClass, true, true, false);
		g.add(sourcePanel.getPanel());
		
		this.addDetectorSpecificControls(g);

		JPanel f = new JPanel();
		f.setBorder(BorderFactory.createTitledBorder("Display scaling"));
		f.setLayout(new BoxLayout(f, BoxLayout.X_AXIS));
		f.add(new JLabel("Vertical scale factor  "));
		f.add(vsData = new JTextField(8));
		//g.add(f); //no need for vertical scaling factor any more. In display instead. 
		
		//Create the peak-picker parameters panel.
		JPanel k = new JPanel();
		k.setBorder(BorderFactory.createTitledBorder("Peak-picking"));
		k.setLayout(new BoxLayout(k, BoxLayout.X_AXIS));
		JPanel k1 = new JPanel();
		JPanel k2 = new JPanel();
		k1.setLayout(new BoxLayout(k1, BoxLayout.Y_AXIS));
		k2.setLayout(new BoxLayout(k2, BoxLayout.Y_AXIS));
		//f1.setAlignmentY(java.awt.Component.TOP_ALIGNMENT);
		k1.add(new JLabel("Threshold "));
		k2.add(threshData = new JTextField(8));
		k1.add(new JLabel("Min time over threshold "));
		k2.add(minTimeData = new JTextField(8));
		k1.add(new JLabel("Min time before next detection "));
		k2.add(refractoryTimeData = new JTextField(8));
		k.add(k1);
		k.add(k2);
		g.add(k);
		
		setDialogComponent(g);
	}
	
	/** Add parameter-setting controls to the dialog box that are specific to the
	 * type of detector in your subclass. Some controls are common to all
	 * detectors; they are handled in IshDetParamsDialog. But others are
	 * specific to one type of detector, and in subclasses, they should be added
	 * in addDetectorSpecificControls.  See EnergySumParamsDialog for an example.
	 * 
	 * @param JPanel g -- the panel to which you should add controls.  Calling
	 * add will append them vertically; to use horizontal placement, make a
	 * sub-panel.
	 */
	protected abstract void addDetectorSpecificControls(JPanel g);
	
//	void showChannelList() {
//		PamDataBlock inputDataBlock = (PamDataBlock)sourceList.getSelectedItem();
//		int channelMap = 
//			(inputDataBlock == null) ? 0xFFFF : inputDataBlock.getChannelMap();
// 		
//		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
//			selectChannel[i].setVisible((1<<i & channelMap) > 0 && inputDataBlock != null);
//		}
//		pack();
//	}

//	public void actionPerformed(ActionEvent e) {
//		if (e.getSource() == sourceList) {
//			showChannelList();
//		}
//	}

	//Copy values from an IshDetParams to the dialog box.
	void setParameters() {
		//Set up the choice box for the input stream.
//		String src = ishDetParams.inputDataSource;
//		PamDataBlock inputDataBlock = 
//			PamController.getInstance().getDataBlock(null, src);
//		ArrayList<PamDataBlock> possibleInputBlocks = 
//			PamController.getInstance().getDataBlocks(PamDataBlock.class, true);  //WRONG!
//		sourceList.removeAllItems();
//		for (int i = 0; i < possibleInputBlocks.size(); i++)
//			sourceList.addItem(possibleInputBlocks.get(i));
//		sourceList.setSelectedItem(inputDataBlock);
//
//		//Set the checkboxes for the channels to process.
//		showChannelList();
		sourcePanel.setParams(ishDetParams.groupedSourceParmas);
//		sourcePanel.setSourceList();
//		sourcePanel.setChannelList(ishDetParams.channelList);
//		sourcePanel.setSource(ishDetParams.inputDataSource);
//		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
//			selectChannel[i].setSelected((ishDetParams.channelList & 1<<i) > 0);
//		}

		//Vertical scaling number.
		vsData            .setText(String.format("%g", ishDetParams.vscale));
		
		//Peak-picking parameters.
		threshData        .setText(String.format("%g", ishDetParams.thresh));
		minTimeData       .setText(String.format("%g", ishDetParams.minTime));
		refractoryTimeData.setText(String.format("%g", ishDetParams.refractoryTime));
	}

	/** Read the values from the dialog box, parse and place into energySumParams.
	 * The subclasses, before or after calling super.getParams(), do likewise for
	 * their detector-specific values.
	 * @return boolean -- true if parameters are valid, false if not
	 */ 
	@Override
	public boolean getParams() {
		try {
//			ishDetParams.inputDataSource = sourceList.getSelectedItem().toString();
//			ishDetParams.inputDataSource = sourcePanel.getSource().toString();
			ishDetParams.vscale         = Double.valueOf(vsData.getText());
			ishDetParams.thresh         = Double.valueOf(threshData.getText());
			ishDetParams.minTime        = Double.valueOf(minTimeData.getText());
			ishDetParams.refractoryTime = Double.valueOf(refractoryTimeData.getText());
//			ishDetParams.channelList 	= sourcePanel.getChannelList();
			 sourcePanel.getParams(ishDetParams.groupedSourceParmas); 

		} catch (Exception ex) {
			return false;
		}
		
		//Do error-checking here.
//		if (ishDetParams.channelList == 0)	return false;
		if (ishDetParams.groupedSourceParmas.getChanOrSeqBitmap() == 0)	return false;
		//if (EnergySumParams.isValidLength(energySumParams.fftLength)) {
		//	return true;
		//}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		ishDetParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		
	}

}
