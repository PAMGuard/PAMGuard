package networkTransfer.emulator;

import java.awt.Frame;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JCheckBox;

import weka.core.pmml.Array;

import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.LatLong;
import PamguardMVC.PamDataBlock;

import networkTransfer.send.NetworkSender;

/**
 * 
 * @author Doug Gillespie
 * Class to control generation of pseudo station data. This will be 
 * used for development of displays and for training purposes. 
 * <p>Basic idea is to take some existing data from a single buoy and then 
 * to spit it out in many "channels" as though from multiple buoys, with each 
 * one starting at some random time within a data set so that individual channels 
 * light up with data at different times.   
 *
 */
public class NetworkEmulator implements PamSettings{

	private NetworkSender networkSender;
	
	protected EmulatorParams emulatorParams = new EmulatorParams();
	
	private EmulatorStream[] emulatorStreams;
	
	private boolean isStarted = false;
	
	public NetworkEmulator(NetworkSender networkSender) {
		this.networkSender = networkSender;
		PamSettingManager.getInstance().registerSettings(this);
		prepareEmulator();
	}

	public void showEmulateDialog(Frame parentFrame) {
		EmulateDialog.showDialog(this, parentFrame);
	}

	protected void prepareEmulator() {
		/*
		 * First generate all the emulator streams. Initialise each one with 
		 * it's own buoy id and lat long. 
		 */
		emulatorStreams = new EmulatorStream[emulatorParams.nBuoys];
		double ang = 360. / emulatorParams.nBuoys;
		for (int i = 0; i < emulatorParams.nBuoys; i++) {
			LatLong emLatLong = emulatorParams.gpsCentre.travelDistanceMeters(ang * i, emulatorParams.circleRadius);
			int buoyId = emulatorParams.firstBuoyId + i;
			emulatorStreams[i] = new EmulatorStream(this, buoyId, emLatLong);
		}
		
	}
	
	/**
	 * GEt a list of data blocks which have binary storage. 
	 * @return a list of data blocks which have binary storage.  
	 */
	protected ArrayList<PamDataBlock> getPotentialDataBlocks() {
		ArrayList<PamDataBlock> allDataBlocks = PamController.getInstance().getDataBlocks();
		ArrayList<PamDataBlock> dataBlocks = new ArrayList<PamDataBlock>();
		for (PamDataBlock aBlock:allDataBlocks) {
			if (aBlock.getBinaryDataSource() != null) {
				dataBlocks.add(aBlock);
			}
		}
		return dataBlocks;
	}

	/**
	 * 
	 * @return a list of used  / selected datablocks. 
	 */
	protected ArrayList<PamDataBlock> getUsedDataBlocks() {
		ArrayList<PamDataBlock> dataBlocks = getPotentialDataBlocks();
		ArrayList<PamDataBlock> usedBlocks = new ArrayList<PamDataBlock>();
		boolean[] selBlocks = emulatorParams.getUsedBlocks(dataBlocks.size());
		for (int i = 0; i < dataBlocks.size(); i++) {
			if (selBlocks[i]) {
				usedBlocks.add(dataBlocks.get(i));
			}
		}
		return usedBlocks;
	}
	
	private void startEmulator() {
		for (int i = 0; i < emulatorParams.nBuoys; i++) {
			emulatorStreams[i].start();
		}
	}
	
	private void stopEmulator() {
		for (int i = 0; i < emulatorParams.nBuoys; i++) {
			emulatorStreams[i].stop();
		}
	}

	/**
	 * Get the status data for a single buoy emulation. This should have 
	 * everything needed for the table in the display dialog. 
	 * @param iStream
	 * @return status data 
	 */
	protected EmBuoyStatus getStreamStatus(int iStream) {
		if (emulatorStreams == null || emulatorStreams.length <= iStream) {
			return null;
		}
		return emulatorStreams[iStream].getStatus();
	}
	
	/**
	 * @return the networkSender
	 */
	public NetworkSender getNetworkSender() {
		return networkSender;
	}

	@Override
	public Serializable getSettingsReference() {
		return emulatorParams;
	}

	@Override
	public long getSettingsVersion() {
		return EmulatorParams.serialVersionUID;
	}

	@Override
	public String getUnitName() {
		return networkSender.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "Network Emulator";
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		emulatorParams = ((EmulatorParams) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	/**
	 * @param isStarted the isStarted to set
	 */
	public void setStarted(boolean isStarted) {
		this.isStarted = isStarted;
	}

	/**
	 * @return the isStarted
	 */
	public boolean isStarted() {
		return isStarted;
	}

	public void start() {
		prepareEmulator();
		startEmulator();
		isStarted = true;
	}

	public void stop() {
		stopEmulator();
		isStarted = false;
	}

}
