package nidaqdev;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import Acquisition.AcquisitionControl;
import Acquisition.ChannelListPanel;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamConstants;
import javafx.scene.Node;

public class NIChannelListPanel implements ChannelListPanel {

	private NIDAQProcess niDaqProcess;
	
	private JPanel niPanel = new JPanel();
	
	private JComponent[] hiddenLabels = new JComponent[3];
	private JLabel[] channelLabels = new JLabel[PamConstants.MAX_CHANNELS];
	private JComboBox[] channelLists = new JComboBox[PamConstants.MAX_CHANNELS];
	private JComboBox[] deviceLists = new JComboBox[PamConstants.MAX_CHANNELS];
	private JComboBox[] rangeLists = new JComboBox[PamConstants.MAX_CHANNELS];
	
	private int nChannels;

	public NIChannelListPanel(NIDAQProcess niDProcess, AcquisitionControl daqControl) {
		this.niDaqProcess = niDProcess;
		niPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		JLabel l;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.anchor = GridBagConstraints.CENTER;
		PamDialog.addComponent(niPanel, 
			new JLabel("Map Hardware (HW) to Software (SW) Channels", SwingConstants.CENTER), c);
		c.gridy++;
		c.gridx = 1;
		c.gridwidth = 1;
		for (int iCol = 0; iCol < 2; iCol++) {
			PamDialog.addComponent(niPanel, hiddenLabels[0] = new JLabel("Device", SwingConstants.CENTER), c);
			c.gridx++;
			PamDialog.addComponent(niPanel, hiddenLabels[1] = new JLabel("HW Chan"), c);
			c.gridx++;
			PamDialog.addComponent(niPanel, hiddenLabels[2] = new JLabel("Range", SwingConstants.CENTER), c);
			c.gridx ++;
		}
		ArrayList<NIDeviceInfo> niDevices = niDProcess.getNiDevices();
		int y0 = c.gridy;
		int x0 = 0;		
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			c.gridx=x0;
			c.gridy++;
			PamDialog.addComponent(niPanel, channelLabels[i] = 
				new JLabel(String.format("Software Channel %d", i)), c);
			c.gridx++;
			PamDialog.addComponent(niPanel, deviceLists[i] = new JComboBox(), c);
			deviceLists[i].addActionListener(new DeviceSelect(i));
			c.gridx++;
			PamDialog.addComponent(niPanel, channelLists[i] = new JComboBox(), c);
			c.gridx++;
			PamDialog.addComponent(niPanel, rangeLists[i] = new JComboBox(), c);
			rangeLists[i].addActionListener(new RangeSelect(i));
			
			for (int iC = 0; iC < PamConstants.MAX_CHANNELS; iC++) {
				channelLists[i].addItem(iC);
			}
			for (int iD = 0; iD < niDevices.size(); iD++) {
				deviceLists[i].addItem(niDevices.get(iD));
			}
						
			if (i == 15) {
				c.gridy = y0;
				x0 = 4;
			}
		}
	}
	
	@Override
	public int[] getChannelList() {
		if (nChannels == 0) {
			return null;
		}
		/*
		 * Get the device numbers and other information
		 * Need to get the ranges here too and set them in the
		 * main ni daq parameters. 
		 */
		int[] devList = new int[nChannels];
		NIDeviceInfo devInfo;
		int rangeIndex;
		double[] aiRange;
		NIDaqParams niDaqParams = niDaqProcess.getNiParameters();
		for (int i = 0; i < nChannels; i++) {
			devList[i] = deviceLists[i].getSelectedIndex();
			devInfo = niDaqProcess.getDeviceInfo(devList[i]);
			rangeIndex = rangeLists[i].getSelectedIndex();
			aiRange = devInfo.getAIVoltageRange(rangeIndex);
			niDaqParams.setAIRange(i, aiRange);
		}
		niDaqProcess.setDeviceList(devList);

		int[] list = new int[nChannels];
		for (int i = 0; i < nChannels; i++) {
			list[i] = (Integer) channelLists[i].getSelectedItem();
		}
		niDaqProcess.setHWChannelList(list);
		
		for (int i = 0; i < nChannels; i++) {
		}
		
		int[] swList = new int[nChannels];
		for (int i = 0; i < nChannels; i++) {
			swList[i] = i;
		}
		return swList;
		
		
	}

	@Override
	public Component getComponent() {
		return niPanel;
	}

	@Override
	public void setNumChannels(int nChannels) {
		this.nChannels = nChannels;
		for (int i = 0; i < channelLabels.length; i++) {
			channelLabels[i].setVisible(i < nChannels);
			channelLists[i].setVisible(i < nChannels);
			deviceLists[i].setVisible(i < nChannels);
			rangeLists[i].setVisible(i < nChannels);
		}
		for (int i = 0; i < hiddenLabels.length; i++) {
			hiddenLabels[i].setVisible(nChannels > 16);
		}
	}

	@Override
	public void setParams(int[] channelList) {
		/*
		 * Don't use the channelList from the main acquisition. 
		 * For NI devices this should always be left as 0,1,2, etc. 
		 * Instead, use the list from the NIDaqProcess. This is necessary
		 * to allow for repeated channel numbers in multi board ops. 
		 */
		channelList = niDaqProcess.getHWChannelList();
		if (channelList == null) {
			channelList = new int[PamConstants.MAX_CHANNELS];
			for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
				channelList[i] = i;
			}
		}
		int n = Math.min(channelLists.length, channelList.length);
		for (int i = 0; i < n; i++){
			channelLists[i].setSelectedItem(channelList[i]);
		}
		int[] devList = niDaqProcess.getDeviceList();
		int i = 0;
		int defaultDevice = niDaqProcess.getMasterDevice();
		if (devList != null) {
			for (i = 0; i < devList.length; i++) {
				if (deviceLists[i].getItemCount() > devList[i]) {
					deviceLists[i].setSelectedIndex(devList[i]);
				}
			}
		}
		if (defaultDevice >= 0) {
			for (; i < deviceLists.length; i++) {
				if (deviceLists[i].getItemCount() > defaultDevice) {
					deviceLists[i].setSelectedIndex(defaultDevice);
				}
			}
		}
		
		NIDaqParams niDaqParams = niDaqProcess.getNiParameters();
		for (int iSWChannel = 0; iSWChannel < rangeLists.length; iSWChannel++) {
			setRange(iSWChannel, niDaqParams.getAIRange(iSWChannel));
		}
		
	}
	
	protected void setAllBoards(int boardNum) {
		for (int i = 0; i < deviceLists.length; i++) {
			deviceLists[i].setSelectedIndex(boardNum);
		}
	}
	
	class DeviceSelect implements ActionListener {

		private int iSWChannel;
		
		public DeviceSelect(int iSWChannel) {
			super();
			this.iSWChannel = iSWChannel;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			setDeviceHoverInfo(iSWChannel);
			setDeviceRangeList(iSWChannel);
			if (iSWChannel == 0) {
				niDaqProcess.setMasterDevice(deviceLists[iSWChannel].getSelectedIndex());
			}
		}
		
	}
	
	private boolean setRange(int iSWChannel, double[] range) {
		int iDev = deviceLists[iSWChannel].getSelectedIndex();
		if (iDev < 0) {
			return false;
		}
		NIDeviceInfo devInfo = niDaqProcess.getDeviceInfo(iDev);
		int rangeIndex = devInfo.findAIRangeIndex(range);
		if (rangeIndex < 0) {
			return false;
		}
		if (rangeLists[iSWChannel].getItemCount() > rangeIndex){
			rangeLists[iSWChannel].setSelectedIndex(rangeIndex);
			return true;
		}
		return false;
	}
	
	private double[] getRange(int iSWChannel) {
		int iDev = deviceLists[iSWChannel].getSelectedIndex();
		if (iDev < 0) {
			return null;
		}
		NIDeviceInfo devInfo = niDaqProcess.getDeviceInfo(iDev);
		int rangeIndex = rangeLists[iSWChannel].getSelectedIndex();
		if (rangeIndex < 0) {
			return null;
		}
		double[] range = devInfo.getAIVoltageRange(rangeIndex);
		return range;
	}
	
	private void setDeviceHoverInfo(int iDevice) {
		JComboBox deviceBox = deviceLists[iDevice];
		int dev = deviceBox.getSelectedIndex();
		if (dev < 0) {
			return;
		}
		ArrayList<NIDeviceInfo> niDevices = niDaqProcess.getNiDevices();
		NIDeviceInfo di = niDevices.get(dev);
		
		deviceBox.setToolTipText(di.gethoverInfo());
	}
	
	private void setDeviceRangeList(int iSWChannel) {
		JComboBox deviceBox = deviceLists[iSWChannel];
		JComboBox rangeBox = rangeLists[iSWChannel];
		double[] currRange = getRange(iSWChannel);
		if (rangeBox == null || deviceBox == null) {
			return;
		}
		int dev = deviceBox.getSelectedIndex();
		NIDeviceInfo devInfo = niDaqProcess.getDeviceInfo(dev);
		if (devInfo == null) {
			return;
		}
		rangeBox.removeAllItems();
		int nRange = devInfo.getNumAIVoltageRanges();
		for (int i = 0; i < nRange; i++) {
			rangeBox.addItem(devInfo.getAIVoltageRangeString(i));
		}
		if (currRange != null) {
			setRange(iSWChannel, currRange);
		}
	}
	
	class RangeSelect implements ActionListener {

		private int iDevice;
		
		public RangeSelect(int iDevice) {
			super();
			this.iDevice = iDevice;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (iDevice == 0) {
				double[] range = getRange(iDevice);
				if (range != null) {
					niDaqProcess.setVP2P(range[1]-range[0]);
				}
			}
		}
		
	}
	
	@Override
	public boolean isDataOk() {
		/**
		 * Check that at least one channel is using the master board 
		 * and that there are no repeated combinations of board and channel
		 */
		int master = niDaqProcess.getMasterDevice();
		boolean hasMaster = false;
		for (int i = 0; i < nChannels; i++) {
			if (deviceLists[i].getSelectedIndex() == master) {
				hasMaster = true;
			}
		}
		if (!hasMaster) {
			JOptionPane.showConfirmDialog(null, 
					"At least one channel must be using the master device selected above!", 
					"Error", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		// now check for repeat combinations. 
		for (int i = 0; i < nChannels-1; i++) {
			for (int j = i+1; j < nChannels; j++) {
				if (channelLists[i].getSelectedIndex() == channelLists[j].getSelectedIndex() &&
						deviceLists[i].getSelectedIndex() == deviceLists[j].getSelectedIndex()) {
					String w = String.format("Channel %d on device %s is used twice\n"+
							"only use each channel once on each device", channelLists[i].getSelectedIndex(),
							deviceLists[i].getSelectedItem());
					JOptionPane.showConfirmDialog(null, w, 
							"Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
					return false;			
				}
			}
		}
		/*
		 * Now check that devices are all grouped together. 
		 */
		boolean isSame; // flag to say it's the same device.
		int iDev, jDev;
		for (int i = 0; i < nChannels-1; i++) {
			iDev = deviceLists[i].getSelectedIndex();
			isSame = true;
			for (int j = i+1; j < nChannels; j++) {
				jDev = deviceLists[j].getSelectedIndex();
				if (iDev != jDev) {
					isSame = false;
				}
				else if (!isSame && iDev == jDev) {
					// was different & is now the same again - error !
					String w = String.format("Channels on each device must be grouped together\n"+
							"Re-order channels for device: %s", deviceLists[i].getSelectedItem().toString());
					JOptionPane.showConfirmDialog(null, w, 
							"Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}
		/**
		 * Now check that devices can support the requested sample rate.
		 */
		double sr = niDaqProcess.readSampleRate();
		if (sr <= 0) {					
			JOptionPane.showConfirmDialog(null, "Invalid sample rate", 
				"Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		int nDevices = deviceLists[0].getItemCount();
		int[] devChannels = new int[nDevices];
		NIDeviceInfo devInfo;
		for (int i = 0; i < nChannels; i++) {
			iDev = deviceLists[i].getSelectedIndex();
			if (iDev >= 0) {
				devChannels[iDev]++;
			}
		}
		for (int i = 0; i < nDevices; i++) {
			if (devChannels[i] > 0) {
				devInfo = niDaqProcess.getDeviceInfo(i);
				if (!devInfo.isExists()) {
					String er2 = String.format("The device %s is not currently present, continue anyway ?", 
							devInfo);
					int ans = JOptionPane.showConfirmDialog(null, er2, 
							"Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
					if (ans == JOptionPane.CANCEL_OPTION) {
						return false;
					}
					else {
						continue; // skip the next stage !
					}
				}
				if (!devInfo.canSample(sr, devChannels[i])) {
					String err = String.format("The device %s cannot sample %d channels at %d Hz",
							devInfo.toString(), devChannels[i], (int) sr);
					JOptionPane.showConfirmDialog(null, err, 
							"Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
						return false;
				}
			}
		}
		
		return true;
	}

	protected void enableMultiBoardOps() {
		boolean b = niDaqProcess.isEnabledMultiBoardOps();
		// if false, set all boards to the master
		int master = niDaqProcess.getMasterDevice();
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			deviceLists[i].setEnabled(b);
//			rangeLists[i].setEnabled(b);
			if (!b){
				deviceLists[i].setSelectedIndex(master);				
			}
		}
	}

	@Override
	public Node getNode() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
