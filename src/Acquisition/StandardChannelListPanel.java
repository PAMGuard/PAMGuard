package Acquisition;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import PamView.dialog.PamDialog;
import PamguardMVC.PamConstants;
import javafx.scene.Node;

public class StandardChannelListPanel implements ChannelListPanel {

//	public int channelList[] = new int[PamConstants.MAX_CHANNELS];
	private JLabel panelChannelLabel[] = new JLabel[PamConstants.MAX_CHANNELS];
	private JComboBox panelChannelList[] = new JComboBox[PamConstants.MAX_CHANNELS];
	
	private JPanel panel;
	int nChannels = 0;
	
	public StandardChannelListPanel() {
		super();
		panel = createStandardChannelListPanel();
	}

	@Override
	public Component getComponent() {
		return panel;
	}

	@Override
	public int[] getChannelList() {
		if (nChannels == 0) {
			return null;
		}
		int[] list = new int[nChannels];
		for (int i = 0; i < nChannels; i++) {
			list[i] = (Integer) panelChannelList[i].getSelectedItem();
//			System.out.println(String.format("%s get list item %d = %d", this.toString(), i, list[i]));
		}
		return list;
	}

	@Override
	public void setNumChannels(int nChannels) {
		
		nChannels = Math.min(nChannels, panelChannelList.length);
		
		this.nChannels = nChannels; 
		
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			panelChannelLabel[i].setVisible(i < nChannels);
			panelChannelList[i].setVisible(i < nChannels);
		}

//		
//		for (int iL = 0; iL < nChannels; iL++) {
//			panelChannelList[iL].removeAllItems();
//			for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
//				panelChannelList[iL].addItem(i);
//			}
//			
//		}
	}

	@Override
	public void setParams(int[] channelList) {

		if (channelList == null) {
			return;
		}
		for (int i = 0; i < Math.min(panelChannelList.length, channelList.length); i++){
			panelChannelList[i].setSelectedIndex(channelList[i]);
//			System.out.println(String.format("%s Set box %d to item %d and get %d", 
//					this.toString(), i, channelList[i], panelChannelList[i].getSelectedItem()));
		}
	}
	@Override
	public boolean isDataOk() {
		
		// now check for repeat combinations. 
		for (int i = 0; i < nChannels-1; i++) {
			for (int j = i+1; j < nChannels; j++) {
				if (panelChannelList[i].getSelectedIndex() == panelChannelList[j].getSelectedIndex()) {
					String w = String.format("Channel %d is used twice\n"+
							"only use each channel once on each device",
							panelChannelList[i].getSelectedIndex());
					JOptionPane.showConfirmDialog(null, w, 
							"Error", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
					return false;			
				}
			}
		}
		return true;
	}


	private JPanel createStandardChannelListPanel() {

		/* code for select channel */
		/*
		 * put this in a separate panel so it can be hidden if 
		 * it's not possible to change these parameters. 
		 * 
		 * Text information updated DG & JG 12/8/08
		 */
		JPanel cP;
		cP = new JPanel();
		cP.setLayout(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		c2.gridx = 0;
		c2.gridy = 0;
		c2.gridwidth = 4;
		c2.anchor = GridBagConstraints.WEST;
//		addComponent(channelListPanel, new JLabel("Select Hardware (HW) Channels"), c2);
		PamDialog.addComponent(cP, new JLabel("Map Hardware (HW) to Software (SW) Channels"), c2);

		c2.gridwidth = 1;
		String spaceStr;
		String s = "<html>PAMGUARD channel numbering starts at 0.<br>Your hardware channel numbering may start at 1.";
		s += "<br>So be aware. If you've put a plug into socket 1, <br>you probably want to select channel 0, etc.</html>";
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++){ //Xiao Yan Deng
		//for (int i = 0; i < getNumChannels(); i++){
			
			if (i%2 ==0){
				c2.gridx = 0;
				c2.gridy ++;
			}
			else {
				c2.gridx++;
			}
			//constraints.gridwidth = 2;
			if (i%2 == 1) {
				spaceStr = "           ";
			}
			else {
				spaceStr = "";
			}
			PamDialog.addComponent(cP, panelChannelLabel[i] = 
				new JLabel(spaceStr + " SW Ch " + i + " = HW Ch "), c2);
			c2.gridx ++;
			//constraints.gridwidth = 2;
			PamDialog.addComponent(cP, panelChannelList[i] = new JComboBox(), c2);
			panelChannelLabel[i].setToolTipText(s);
			panelChannelList[i].setToolTipText(s);
			
//			System.out.println(String.format("Fill channel list %d", i));
			for (int iC = 0; iC < PamConstants.MAX_CHANNELS; iC++) {
				panelChannelList[i].addItem(iC);
			}
			
		}
		return cP;
	}

	@Override
	public Node getNode() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
