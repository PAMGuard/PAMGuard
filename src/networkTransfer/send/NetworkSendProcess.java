package networkTransfer.send;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import networkTransfer.receive.NetworkReceiver;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryObjectData;
import jsonStorage.JSONObjectData;
import jsonStorage.JSONObjectDataSource;
import PamController.PamControlledUnit;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;

public class NetworkSendProcess extends PamProcess {

	private BinaryDataSource binarySource;
	private JSONObjectDataSource jsonSource;
	private int quickId;
	private NetworkSender networkSender;
	private NetworkObjectPacker networkObjectPacker;
	private boolean commandProcess;
	private int outputFormat;

	public NetworkSendProcess(NetworkSender networkSender,
			PamDataBlock parentDataBlock, int sendingFormat) {
		super(networkSender, parentDataBlock);
		this.networkSender = networkSender;
		this.outputFormat = sendingFormat;
		if (parentDataBlock != null) {
			binarySource = parentDataBlock.getBinaryDataSource();
			quickId = parentDataBlock.getQuickId();
			jsonSource = parentDataBlock.getJSONDataSource();
		}
		networkObjectPacker = new NetworkObjectPacker();
	}


	@Override
	public void prepareProcess() {
//		outputFormat = networkSender.networkSendParams.sendingFormat;
		if (commandProcess && outputFormat==NetworkSendParams.NETWORKSEND_BYTEARRAY) {
			sendPamCommand(NetworkReceiver.NET_PAM_COMMAND_PREPARE);
		}
	}

	@Override
	public void pamStart() {
		if (commandProcess && outputFormat==NetworkSendParams.NETWORKSEND_BYTEARRAY) {
			sendPamCommand(NetworkReceiver.NET_PAM_COMMAND_START);
		}
	}

	@Override
	public void pamStop() {
		if (commandProcess && outputFormat==NetworkSendParams.NETWORKSEND_BYTEARRAY) {
			sendPamCommand(NetworkReceiver.NET_PAM_COMMAND_STOP);
		}
	}

	private void sendPamCommand(int command) {
		int id1 = networkSender.networkSendParams.stationId1;
		int id2 = networkSender.networkSendParams.stationId2;
		/*
		 * Need to pack the time that this command was sent so that the Network Receiver can know when to start
		 * even if there is a delay in sending the data.  
		 */
		ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] timeData = null;
		try {
			dos.writeLong(PamCalendar.getTimeInMillis());
			timeData = bos.toByteArray();
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] data = networkObjectPacker.packData(id1, id2, (short) NetworkReceiver.NET_PAM_COMMAND, command, timeData);
		NetworkQueuedObject qo = new NetworkQueuedObject(id1, id2, NetworkReceiver.NET_PAM_COMMAND, command, data);
		networkSender.queueDataObject(qo);
	}

	@Override
	public String getProcessName() {
		if (commandProcess) {
			return "Command";
		}
		if (getParentDataBlock() != null) {
			return getParentDataBlock().getDataName();
		}
		return null;
	}

	@Override
	public void newData(PamObservable dataBlock, PamDataUnit dataUnit) {
		
		NetworkQueuedObject qo = null;

		// pack the data into a byte array
		if (outputFormat==NetworkSendParams.NETWORKSEND_BYTEARRAY) {
			int id1 = networkSender.networkSendParams.stationId1;
			int id2 = networkSender.networkSendParams.stationId2;
			
			byte[] data = networkObjectPacker.packDataUnit(id1,id2, (PamDataBlock) dataBlock, dataUnit);
			qo = new NetworkQueuedObject(id1, id2, NetworkReceiver.NET_PAM_DATA, quickId, data);
		}

		// pack the data into a json string
		else if (outputFormat == NetworkSendParams.NETWORKSEND_JSON) {
			String jsonString = networkObjectPacker.packDataUnit((PamDataBlock) dataBlock, dataUnit);
			if (jsonString==null) {
				System.out.println("Error creating json string from " + dataBlock.getClass());
			} else {
				qo = new  NetworkQueuedObject(jsonString);
			}
//			System.out.print("***" + jsonString);
		}
		
		// Add to the output queue
		if (qo!=null) {
			networkSender.queueDataObject(qo);
		}
	}


	/**
	 * @param commandProcess the commandProcess to set
	 */
	public void setCommandProcess(boolean commandProcess) {
		this.commandProcess = commandProcess;
	}


	/**
	 * @return the commandProcess
	 */
	public boolean isCommandProcess() {
		return commandProcess;
	}


	public int getOutputFormat() {
		return outputFormat;
	}


	public void setOutputFormat(int outputFormat) {
		this.outputFormat = outputFormat;
	}

}
