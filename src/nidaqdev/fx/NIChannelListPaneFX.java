package nidaqdev.fx;

import java.awt.Component;
import java.util.ArrayList;

import Acquisition.AcquisitionControl;
import Acquisition.ChannelListPanel;
import PamguardMVC.PamConstants;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import nidaqdev.NIDAQProcess;
import nidaqdev.NIDaqParams;
import nidaqdev.NIDeviceInfo;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;

/**
 * 
 * Pane which shows hardware channels on a National Instruments device mapped to PAMGuard software channels.
 * 
 * @author Jamie Macaulay
 *
 */
public class NIChannelListPaneFX implements ChannelListPanel {

	private NIDAQProcess niDaqProcess;
	
	private PamGridPane niPanel;;
	
	private Node[] hiddenLabels = new Node[3];
	private Label[] channelLabels = new Label[PamConstants.MAX_CHANNELS];
	
	/**
	 * The hardwar channels available on the device. 
	 */
	@SuppressWarnings("unchecked")
	private ComboBox<Integer>[] channelLists = new ComboBox[PamConstants.MAX_CHANNELS];
	
	/**
	 * List of possible devices that can be used (populated with multiple options if there are 
	 * synced NI cards)
	 */
	@SuppressWarnings("unchecked")
	private ComboBox<String>[] deviceLists = new ComboBox[PamConstants.MAX_CHANNELS];
	
	/**
	 * Possible voltage ranges of the channel
	 */
	@SuppressWarnings("unchecked")
	private ComboBox<String>[] rangeLists = new ComboBox[PamConstants.MAX_CHANNELS];
	
	private int nChannels;

	public NIChannelListPaneFX(NIDAQProcess niDProcess, AcquisitionControl daqControl) {
		this.niDaqProcess = niDProcess;
		niPanel= new PamGridPane();
		
		int gridy=0;
		
		//the text stating what the pane is for 
		Label label = new Label("Map Hardware (HW) to Software (SW) Channels"); 
		niPanel.add(label, 0, gridy);
		GridPane.setColumnSpan(label, 5);
		gridy++;
		
		//the column headings 
		niPanel.add(hiddenLabels[0] = new Label("Device"), 1, gridy);
		
		niPanel.add(hiddenLabels[1] = new Label("HW Chan"), 2, gridy);
		
		niPanel.add(hiddenLabels[2] = new Label("Range"), 0, gridy);
		gridy++;
		
//			PamDialog.addComponent(niPanel, hiddenLabels[0] = new JLabel("Device", SwingConstants.CENTER), c);
//			c.gridx++;
//			PamDialog.addComponent(niPanel, hiddenLabels[1] = new JLabel("HW Chan"), c);
//			c.gridx++;
//			PamDialog.addComponent(niPanel, hiddenLabels[2] = new JLabel("Range", SwingConstants.CENTER), c);
//			c.gridx++;
		
			
		ArrayList<NIDeviceInfo> niDevices = niDProcess.getNiDevices();
		int y0 = gridy;
		int x0 = 0;		
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
//			PamDialog.addComponent(niPanel, channelLabels[i] = 
//				new Label(String.format("Software Channel %d", i)), c);
//			c.gridx++;
//			PamDialog.addComponent(niPanel, deviceLists[i] = new ComboBox(), c);
//			deviceLists[i].setOnAction(new DeviceSelect(i));
//			c.gridx++;
//			PamDialog.addComponent(niPanel, channelLists[i] = new ComboBox(), c);
//			c.gridx++;
//			PamDialog.addComponent(niPanel, rangeLists[i] = new ComboBox(), c);
//			rangeLists[i].setOnAction(new RangeSelect(i));
			
			niPanel.add(new Label(String.format("Software Channel %d", i)), x0, gridy);
			niPanel.add(deviceLists[i] = new ComboBox<String>(), x0+1, ++gridy);
			deviceLists[i].setOnAction(new DeviceSelect(i));

			niPanel.add(channelLists[i] = new ComboBox<Integer>(), x0+2, ++gridy);
			niPanel.add(rangeLists[i] = new ComboBox<String>(), x0+3, ++gridy);
			rangeLists[i].setOnAction(new RangeSelect(i));
			
			
			for (int iC = 0; iC < PamConstants.MAX_CHANNELS; iC++) {
				channelLists[i].getItems().add(iC);
			}
			for (int iD = 0; iD < niDevices.size(); iD++) {
				deviceLists[i].getItems().add(niDevices.get(iD).toString());
			}
						
			if (i == 15) {
				gridy = y0;
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
			devList[i] = deviceLists[i].getSelectionModel().getSelectedIndex();
			devInfo = niDaqProcess.getDeviceInfo(devList[i]);
			rangeIndex = rangeLists[i].getSelectionModel().getSelectedIndex();
			aiRange = devInfo.getAIVoltageRange(rangeIndex);
			niDaqParams.setAIRange(i, aiRange);
		}
		niDaqProcess.setDeviceList(devList);

		int[] list = new int[nChannels];
		for (int i = 0; i < nChannels; i++) {
			list[i] = (Integer) channelLists[i].getSelectionModel().getSelectedItem();
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
		return null;
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
			channelLists[i].getSelectionModel().select(channelList[i]);
		}
		int[] devList = niDaqProcess.getDeviceList();
		int i = 0;
		int defaultDevice = niDaqProcess.getMasterDevice();
		if (devList != null) {
			for (i = 0; i < devList.length; i++) {
				if (deviceLists[i].getItems().size() > devList[i]) {
					deviceLists[i].getSelectionModel().select(devList[i]);
				}
			}
		}
		if (defaultDevice >= 0) {
			for (; i < deviceLists.length; i++) {
				if (deviceLists[i].getItems().size() > defaultDevice) {
					deviceLists[i].getSelectionModel().select(defaultDevice);
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
			deviceLists[i].getSelectionModel().select(boardNum);
		}
	}
	
	class DeviceSelect implements EventHandler<ActionEvent> {

		private int iSWChannel;
		
		public DeviceSelect(int iSWChannel) {
			super();
			this.iSWChannel = iSWChannel;
		}

		@Override
		public void handle(ActionEvent arg0) {
			setDeviceHoverInfo(iSWChannel);
			setDeviceRangeList(iSWChannel);
			if (iSWChannel == 0) {
				niDaqProcess.setMasterDevice(deviceLists[iSWChannel].getSelectionModel().getSelectedIndex());
			}
		}
		
	}
	
	private boolean setRange(int iSWChannel, double[] range) {
		int iDev = deviceLists[iSWChannel].getSelectionModel().getSelectedIndex();
		if (iDev < 0) {
			return false;
		}
		NIDeviceInfo devInfo = niDaqProcess.getDeviceInfo(iDev);
		int rangeIndex = devInfo.findAIRangeIndex(range);
		if (rangeIndex < 0) {
			return false;
		}
		if (rangeLists[iSWChannel].getItems().size()> rangeIndex){
			rangeLists[iSWChannel].getSelectionModel().select(rangeIndex);
			return true;
		}
		return false;
	}
	
	private double[] getRange(int iSWChannel) {
		int iDev = deviceLists[iSWChannel].getSelectionModel().getSelectedIndex();
		if (iDev < 0) {
			return null;
		}
		NIDeviceInfo devInfo = niDaqProcess.getDeviceInfo(iDev);
		int rangeIndex = rangeLists[iSWChannel].getSelectionModel().getSelectedIndex();
		if (rangeIndex < 0) {
			return null;
		}
		double[] range = devInfo.getAIVoltageRange(rangeIndex);
		return range;
	}
	
	private void setDeviceHoverInfo(int iDevice) {
		ComboBox deviceBox = deviceLists[iDevice];
		int dev = deviceBox.getSelectionModel().getSelectedIndex();
		if (dev < 0) {
			return;
		}
		ArrayList<NIDeviceInfo> niDevices = niDaqProcess.getNiDevices();
		NIDeviceInfo di = niDevices.get(dev);
		
		deviceBox.setTooltip(new Tooltip(di.gethoverInfo()));
	}
	
	private void setDeviceRangeList(int iSWChannel) {
		ComboBox<String> deviceBox = deviceLists[iSWChannel];
		ComboBox<String> rangeBox = rangeLists[iSWChannel];
		double[] currRange = getRange(iSWChannel);
		if (rangeBox == null || deviceBox == null) {
			return;
		}
		int dev = deviceBox.getSelectionModel().getSelectedIndex();
		NIDeviceInfo devInfo = niDaqProcess.getDeviceInfo(dev);
		if (devInfo == null) {
			return;
		}
		rangeBox.getItems().clear();
		int nRange = devInfo.getNumAIVoltageRanges();
		for (int i = 0; i < nRange; i++) {
			rangeBox.getItems().add(devInfo.getAIVoltageRangeString(i));
		}
		if (currRange != null) {
			setRange(iSWChannel, currRange);
		}
	}
	
	class RangeSelect  implements EventHandler<ActionEvent>  {

		private int iDevice;
		
		public RangeSelect(int iDevice) {
			super();
			this.iDevice = iDevice;
		}

		@Override
		public void handle(ActionEvent arg0) {
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
			if (deviceLists[i].getSelectionModel().getSelectedIndex() == master) {
				hasMaster = true;
			}
		}
		if (hasMaster == false) {
			PamDialogFX.showError(
					"At least one channel must be using the master device selected above!"); 
					
			return false;
		}
		
		// now check for repeat combinations. 
		for (int i = 0; i < nChannels-1; i++) {
			for (int j = i+1; j < nChannels; j++) {
				if (channelLists[i].getSelectionModel().getSelectedIndex() == channelLists[j].getSelectionModel().getSelectedIndex() &&
						deviceLists[i].getSelectionModel().getSelectedIndex() == deviceLists[j].getSelectionModel().getSelectedIndex()) {
					String w = String.format("Channel %d on device %s is used twice\n"+
							"only use each channel once on each device", channelLists[i].getSelectionModel().getSelectedIndex(),
							deviceLists[i].getSelectionModel().getSelectedItem());
					PamDialogFX.showError(
							w); 
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
			iDev = deviceLists[i].getSelectionModel().getSelectedIndex();
			isSame = true;
			for (int j = i+1; j < nChannels; j++) {
				jDev = deviceLists[j].getSelectionModel().getSelectedIndex();
				if (iDev != jDev) {
					isSame = false;
				}
				else if (!isSame && iDev == jDev) {
					// was different & is now the same again - error !
					String w = String.format("Channels on each device must be grouped together\n"+
							"Re-order channels for device: %s", deviceLists[i].getSelectionModel().getSelectedItem().toString());
					PamDialogFX.showError(
							w); 
					return false;
				}
			}
		}
		/**
		 * Now check that devices can support the requested sample rate.
		 */
		double sr = niDaqProcess.readSampleRate();
		if (sr <= 0) {					
			PamDialogFX.showError( "Invalid sample rate"); 
			return false;
		}
		int nDevices = deviceLists[0].getItems().size();
		int[] devChannels = new int[nDevices];
		NIDeviceInfo devInfo;
		for (int i = 0; i < nChannels; i++) {
			iDev = deviceLists[i].getSelectionModel().getSelectedIndex();
			if (iDev >= 0) {
				devChannels[iDev]++;
			}
		}
		for (int i = 0; i < nDevices; i++) {
			if (devChannels[i] > 0) {
				devInfo = niDaqProcess.getDeviceInfo(i);
				if (devInfo.isExists() == false) {
					String er2 = String.format("The device %s is not currently present, continue anyway ?", 
							devInfo);
					
					boolean ans  = PamDialogFX.showMessageDialog(null, "Warning", er2, new ButtonType("Continue"), ButtonType.CANCEL, AlertType.WARNING); 
					if (!ans) {
						return false;
					}
					else {
						continue; // skip the next stage !
					}
				}
				if (devInfo.canSample(sr, devChannels[i]) == false) {
					String err = String.format("The device %s cannot sample %d channels at %d Hz",
							devInfo.toString(), devChannels[i], (int) sr);
					PamDialogFX.showError(err);
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
			deviceLists[i].setDisable(!b);
//			rangeLists[i].setEnabled(b);
			if (!b){
				deviceLists[i].getSelectionModel().select(master);				
			}
		}
	}

	@Override
	public Node getNode() {
		return niPanel;
	}
	
	
	
}
