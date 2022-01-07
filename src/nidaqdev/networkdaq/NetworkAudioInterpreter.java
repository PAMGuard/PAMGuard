package nidaqdev.networkdaq;

import java.net.Socket;
import java.util.ArrayList;

import Acquisition.AcquisitionControl;
import Acquisition.DaqSystem;
import PamController.PamControlledUnit;
import PamController.PamController;
import networkTransfer.NetworkObject;
import networkTransfer.receive.BuoyStatusDataUnit;
import networkTransfer.receive.NetworkReceiver;

/**
 * Class of functions to help interpret incoming audio data - put
 * into a separate class to keep functions separate from rest of NetworkREciever class. 
 * @author Doug Gillespie
 *
 */
public class NetworkAudioInterpreter {

	private NINetworkDaq niNetworkDaq;
	private NetworkReceiver networkReceiver;
	int findTries = 0;
	
	public NetworkAudioInterpreter(NetworkReceiver networkReceiver) {
		this.networkReceiver = networkReceiver;
	}
	
	public NetworkObject interpretData(NetworkObject receivedObject, BuoyStatusDataUnit buoyStatusDataUnit) {
		NINetworkDaq niNetworkDaq = getNetworkDaq(true);
		if (niNetworkDaq == null) {
			return null;
		}

		return niNetworkDaq.interpretData(receivedObject);
	}
//	/**
//	 * Audio data need to be sent straight off to the Acquisition module, where they will be
//	 * unpacked and put into the system in the normal way. 
//	 * @param socket
//	 * @param dataVersion2
//	 * @param buoyStatusDataUnit
//	 * @param dataId1
//	 * @param dataId2
//	 * @param dataLen
//	 * @param duBuffer
//	 */
//	public boolean interpretData(Socket socket, short dataVersion2,
//			BuoyStatusDataUnit buoyStatusDataUnit, short dataId1, int dataId2,
//			int dataLen, byte[] duBuffer) {
//		NINetworkDaq niNetworkDaq = getNetworkDaq(true);
//		if (niNetworkDaq == null) {
//			return false;
//		}
//		return niNetworkDaq.interpretData(socket, dataVersion2, dataId1, dataId2, dataLen, duBuffer);
//	}

	/**
	 * Get the Network DAQ system to send data to. 
	 * Will only work with a single acquisition system ? 
	 * @param find search for it if it can't be found. 
	 * @return NetowrkDAQ sub system, or null. 
	 */
	NINetworkDaq getNetworkDaq(boolean find) {
		if (niNetworkDaq != null) {
			return niNetworkDaq;
		}
		if (find) {
			niNetworkDaq = findNetworkDaq();
		}
		return niNetworkDaq;
	}
	
	
	/**
	 * find the network Daq system
	 * @return
	 */
	NINetworkDaq findNetworkDaq() {
		// first find the Acquisition module. 
		ArrayList<PamControlledUnit> daqModules = PamController.getInstance().findControlledUnits(AcquisitionControl.unitType);
		for (PamControlledUnit aUnit:daqModules) {
			AcquisitionControl daqCtrl = (AcquisitionControl) aUnit;
			DaqSystem daqSystem = daqCtrl.findDaqSystem(null);
			if (daqSystem == null) {
				continue;
			}
			if (NINetworkDaq.class == daqSystem.getClass()) {
				return (NINetworkDaq) daqSystem;
			}
		}
		return null;
	}
}
