package nidaqdev.networkdaq;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import javax.swing.JComponent;
import javax.swing.Timer;

import org.pamguard.x3.x3.CRC16;
import org.pamguard.x3.x3.X3FrameDecode;
import org.pamguard.x3.x3.X3FrameHeader;

import networkTransfer.NetworkObject;
import networkTransfer.receive.BuoyStatusDataUnit;
import networkTransfer.receive.NetworkDataUser;
import networkTransfer.receive.NetworkReceiveThread;
import networkTransfer.receive.NetworkReceiver;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackSystem;
import warnings.PamWarning;
import warnings.WarningSystem;
import wavFiles.ByteConverter;
import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionDialog;
import Acquisition.AcquisitionProcess;
import Acquisition.DaqSystem;
import Acquisition.AudioDataQueue;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamUtils.LatLong;
import PamUtils.PamCalendar;
import PamView.dialog.PamDialog;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataUnit;
import generalDatabase.SQLLogging;

/**
 * DAQ system to receive data from remote NI Compact Rio chassis in compressed
 * or uncompressed data formats. Very prototype !
 * @author Doug Gillespie
 *
 */
public class NINetworkDaq extends DaqSystem implements NetworkDataUser, PamSettings {

	static public final int NET_AUDIO_HEADINFO = 1; // must match what's in the sender !
	static public final int NET_AUDIO_SOUND = 2;

	protected static final String systemName = "NI CRio Network DAQ";
	private AcquisitionControl acquisitionControl;
	private NetworkSoundDescription networkSoundDescription;

	private NINetworkDaqParams niNetParams = new NINetworkDaqParams();

	//	private X3Compressor x3JNI = new X3JNI();
	X3FrameDecode x3Decoder = new X3FrameDecode();

	private RawDataUnit[] prepDataUnits;

	private long totalSamples;
	private int duDuration = 40960;
	private int[] duCounters = new int[PamConstants.MAX_CHANNELS];

	NIUDPInterface niUdpInterface;
	private Socket rxSocket;
	private boolean keepRunning;
	private Thread captureThread;
	private NetworkReceiveThread networkReceiveThread;

	private CRioDialogPanel cRioDialogPanel;

	private LinuxSSHCommander sshCommander;
	private boolean isNormalMode;
	
	private Timer tempTimer;
	private PamWarning tempWarning = new PamWarning("CRio Chassis", "Temp Warning", 0);
	private double tempWarningLevels[] = {Double.NEGATIVE_INFINITY, 35, 50.};
	
	private long lastTempTime = 0;
	private double lastTemperature;
	
	private NIDaqLogging niDaqLogging;
	private long startTime;
	
	private NIDaqStatusComponent niDaqStatusComponent;

	public NINetworkDaq(AcquisitionControl acquisitionControl) {
		this.acquisitionControl = acquisitionControl;
		isNormalMode = PamController.getInstance().getRunMode() == PamController.RUN_NORMAL;
		PamSettingManager.getInstance().registerSettings(this);
		niDaqStatusComponent = new NIDaqStatusComponent(this);
		if (isNormalMode) {
			niUdpInterface = new NIUDPInterface(this);
			sshCommander = new LinuxSSHCommander(this);
		}
		tempTimer = new Timer(2000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {	
				getChassisTemp();
			}
		});
	}

	protected void getChassisTemp() {
		String t = sendUDPCommand("temp");
//		System.out.println("Chassis temp: " + t);
		Double temp = readTempString(t);
		niDaqStatusComponent.setTemperature(temp);
		if (temp == null) {
			tempWarning.setWarningMessage(String.format("Invalid temperatue string from cRio: \"%s\"", t));
			tempWarning.setWarnignLevel(1);
			WarningSystem.getWarningSystem().addWarning(tempWarning);
		}
		else {
			lastTempTime = PamCalendar.getTimeInMillis();
			lastTemperature = temp;
			int lev = 0;
			for (int i = 1; i < tempWarningLevels.length; i++) {
				if (temp > tempWarningLevels[i]) {
					lev = i;
				}
			}
			if (lev == 0) {
				WarningSystem.getWarningSystem().removeWarning(tempWarning);
			}
			else {
				tempWarning.setWarningMessage(String.format("Temperature = %3.1f%s", temp, LatLong.deg));
				tempWarning.setWarningTip("Measured at " + PamCalendar.formatDateTime(System.currentTimeMillis()) + " UTC");
				tempWarning.setWarnignLevel(lev);
				WarningSystem.getWarningSystem().addWarning(tempWarning);
			}
		}
	}
	
	/**
	 * Read the temperature from the string as a double
	 * @param tStr string value
	 * @return temperature value or null. 
	 */
	private Double readTempString(String tStr) {
		if (tStr == null) return null;
		try {
			Double temp = Double.valueOf(tStr);
			return temp;
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	public Double getLastTemperature() {
		if (PamCalendar.getTimeInMillis() - lastTempTime > 5000) {
			return null;
		}
		else {
			return lastTemperature;
		}
	}

	@Override
	public String getSystemType() {
		return systemName;
	}

	@Override
	public String getSystemName() {
		return systemName;
	}

	@Override
	public JComponent getDaqSpecificDialogComponent(
			AcquisitionDialog acquisitionDialog) {
		getCrioDialogPanel(acquisitionDialog);
		cRioDialogPanel.setDialogParams(niNetParams);
		return cRioDialogPanel.getDialogPanel();
	}

	private CRioDialogPanel getCrioDialogPanel(AcquisitionDialog acquisitionDialog) {
		if (cRioDialogPanel == null) {
			cRioDialogPanel = new CRioDialogPanel(acquisitionDialog, this);
		}
		return cRioDialogPanel;
	}

	@Override
	public void dialogSetParams() {
		if (cRioDialogPanel == null) {
			return;
		}
		cRioDialogPanel.setDialogParams(niNetParams);
	}

	@Override
	public boolean dialogGetParams() {
		if (cRioDialogPanel == null) {
			return false;
		}
		boolean ok = cRioDialogPanel.getDialogParams(niNetParams);
		if (ok && sshCommander != null) {
			sshCommander.openShell(niNetParams.niAddress, niNetParams.getLinuxUser(), niNetParams.getLinuxPassword());
			if (sshCommander.getShellHost() == null) {
				String msg = String.format("Unable to connect to host %s with user %s", niNetParams.niAddress, niNetParams.getLinuxUser());
				PamDialog.showWarning(null, "NI Network Acquisition", msg);
			}
		}
		return ok;
	}

	@Override
	public int getMaxChannels() {
		return DaqSystem.PARAMETER_FIXED;
	}

	@Override
	public int getMaxSampleRate() {
		return DaqSystem.PARAMETER_FIXED; // will disable the built in sampel rate data. 
	}

	@Override
	public double getPeak2PeakVoltage(int swChannel) {
		return 20;
	}

	private boolean prepareLinuxApp() {
		// kill and restart the ni program.
		if (niNetParams.startFailures > 0 && niNetParams.startFailures % 10 == 0) {
			killLinuxApp();
		}
		// try to ping the app once and if can't then kill again and restart. 
		if (!pingLinuxApp()) {
			killLinuxApp();
			launchLinuxApp(); 
		}

		return true;
	}

	private boolean launchLinuxApp() {
//		String startCommand = String.format("%s udpport %d", niNetParams.getExeName(), niNetParams.niUDPPort);
		String startCommand = String.format("%s udpport %d", niNetParams.getExeName(), niNetParams.niUDPPort);
		sshCommander.writeCommand(startCommand);
		// now wait up to three seconds for the program to start before trying anything else
		for (int i = 0; i < 30; i++) {
			boolean pingOk = pingLinuxApp();
			if (pingOk) {
				return true;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.err.printf("Unable to ping software %s on Linux host %s\n", niNetParams.getExeName(), niNetParams.niAddress);
		return false;
	}

	private boolean pingLinuxApp() {
		String pingAns = sendUDPCommand("ping");
		if (pingAns != null && pingAns.equals("ping")) {
			return true;
		}
		else {
			return false;
		}
	}

	private void killLinuxApp() {
		String exeName = new String(niNetParams.getExeName());
		if (exeName.startsWith("./")) {
			exeName = exeName.substring(2);
		}
		sshCommander.writeCommand("killall " + exeName);
	}

	private boolean prepareShell() {
		String currentShellHost = sshCommander.getShellHost();
		if (currentShellHost == null || currentShellHost.equals(niNetParams.niAddress) == false) {
			sshCommander.openShell(niNetParams.niAddress, "admin", "");
		}
		sshCommander.writeCommand("cd " + niNetParams.getWorkingDir());

		currentShellHost = sshCommander.getShellHost();
		return (currentShellHost != null && currentShellHost.equals(niNetParams.niAddress));

	}

	private String sendUDPCommand(String command) {
		String str = niUdpInterface.sendCommand(command, niNetParams.niAddress, niNetParams.niUDPPort);
//		System.out.printf("%s port %d command \"%s\" replied \"%s\"\n",  niNetParams.niAddress, niNetParams.niUDPPort, command, str);
		return str;
	}

	@Override
	public boolean prepareSystem(AcquisitionControl daqControl) {
		long t1 = System.currentTimeMillis();
		/*
		 * Try to start the application on the cRio in it's own SSH Shell
		 */
		if (prepareShell() == false) {
			return false;
		}
		if (prepareLinuxApp() == false) {
			return false;
		}
		sendUDPCommand("verbose " + niNetParams.verboseLevel);
		sendUDPCommand("netsend destport " + niNetParams.niTCPPort);
		sendUDPCommand("netsend destip");
		sendUDPCommand("netsend clearqueue");
		sendUDPCommand("netsend enable true");
		String chanString = String.format("nchan %d", niNetParams.getNChannels());
		sendUDPCommand(chanString);
		if (niNetParams.chassisConfig != null) {
			String chassis = String.format("chassis %d", niNetParams.chassisConfig.getChassisId());
			sendUDPCommand(chassis);
		}
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			duCounters[i] = 0;
		}
		sendUDPCommand("prepare");	
		
		totalSamples = 0;
		dataCalls = 0;
		duDuration = 40960; // just under 1/10 s for 500ks data. 
		launchCapture();
		if (prepDataUnits != null) {
			prepDataUnits = null;
		}
		tempTimer.start();
		long t2 = System.currentTimeMillis();	
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		System.out.printf("Crio Prepare took %d millis\n", t2-t1);
		
		return true;
	}

	@Override
	public boolean startSystem(AcquisitionControl daqControl) {
//		long t1 = System.currentTimeMillis();

		sendUDPCommand("start");
		
		startTime = System.currentTimeMillis();
//		System.out.printf("Crio Start took %d millis\n", startTime-t1);
		return true;
	}

	@Override
	public void stopSystem(AcquisitionControl daqControl) {
		keepRunning = false;
		sendUDPCommand("stop");
//		sshCommander.writeCommand("exit");
		if (networkReceiveThread != null) {
			networkReceiveThread.stopThread();
			networkReceiveThread = null;
		}
		tempTimer.stop();
	}

	/**
	 * Launch the capture system. this will open a TCP socket on the
	 * remote device for listening and then launch a thread to 
	 * start to read from it. Similar to what's in NetworkReceiver, 
	 * but this end controls the socket, so shouldn't run into 
	 * firewall issues. 
	 */
	private boolean launchCapture() {
		keepRunning = true;
		try {
			rxSocket = new Socket(niNetParams.niAddress, niNetParams.niTCPPort);
		} catch (IOException e) {
			//			e.printStackTrace();
			System.err.printf("Network DAQ connection refused on host %s port %d\n", niNetParams.niAddress, niNetParams.niTCPPort);
			return false;
		}
		captureThread = new Thread(networkReceiveThread = new NetworkReceiveThread(rxSocket, this));
		captureThread.start();

		return true;
	}

	@Override
	public boolean isRealTime() {
		return true;
	}

	@Override
	public boolean canPlayBack(float sampleRate) {
		return true;
	}

	/* (non-Javadoc)
	 * @see Acquisition.DaqSystem#getPlaybackSystem(soundPlayback.PlaybackControl, Acquisition.DaqSystem)
	 */
	@Override
	public PlaybackSystem getPlaybackSystem(PlaybackControl playbackControl,
			DaqSystem daqSystem) {
		return playbackControl.getFilePlayback();
	}

	@Override
	public int getDataUnitSamples() {
		return duDuration;
	}

	@Override
	public void daqHasEnded() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDeviceName() {
		return systemName;
	}

	public boolean interpretData(Socket socket, short dataVersion2,
			short dataId1, int dataId2,
			int dataLen, byte[] duBuffer) {
		switch (dataId2) {
		case NET_AUDIO_HEADINFO:
			networkSoundDescription = interpretHeadInfo(dataLen, duBuffer);
			startAcquisition();
			return networkSoundDescription != null;
		case NET_AUDIO_SOUND:
			return interpretX3NetSound(dataLen, duBuffer);
			//			return interpretWavNetSound(dataLen, duBuffer);
		default:
			return false;
		}
	}

	private void startAcquisition() {
		if (PamController.getInstance().getPamStatus() != PamController.PAM_IDLE) {
			return;
		}
		totalSamples = 0;
		duDuration = 40960; // just under 1/10 s for 500ks data. 
		setStreamStatus(STREAM_RUNNING);
		//		PamController.getInstance().pamStop();
		//		PamController.getInstance().startLater();
	}

	private int rxHeadCount = 0;

	/**
	 * Called when Network header data arrive. This is an xml string in the 
	 * same format as sits in the header of x3a files and contains 
	 * everything we need to know about the forthcoming data stream. 
	 * @param buoyStatusDataUnit
	 * @param dataLen
	 * @param duBuffer
	 * @return true if decoded successfully. 
	 */
	public NetworkSoundDescription interpretHeadInfo(int dataLen, byte[] duBuffer) {
		String xmlString = null;
		if (rxHeadCount > 0) {
			System.out.println(String.format("Recevied sound header %d", rxHeadCount));
			//			System.exit(0);
		}
		rxHeadCount++;

		if (duBuffer == null) { // used for testing. 
			//			xmlString = "<X3ARCH PROG=\"x3new.m\" VERSION=\"2.0\">"
			//					+ "<CFG ID=\"0\" FTYPE=\"XML\">"
			//					+ "</CFG><CFG ID=\"1\" FTYPE=\"WAV\">"
			//					+ "<FS UNIT=\"Hz\">500000</FS>"
			//					+ "<SUFFIX>wav</SUFFIX>"
			//					+ "<CODEC TYPE=\"X3\" VERS=\"2\">"
			//					+ "<BLKLEN>20</BLKLEN>"
			//					+ "<CODES N=\"4\">RICE0,RICE1,RICE3,BFP</CODES>"
			//					+ "<FILTER>DIFF</FILTER>"
			//					+ "<NBITS>16</NBITS>"
			//					+ "<T N=\"3\">3,8,20</T>"
			//					+ "</CODEC></CFG>"
			//					+ "</X3ARCH>";
		}
		else {
			xmlString = new String(duBuffer);
		}
//		System.out.println(xmlString);

		NetworkSoundDescription nsd = new NetworkSoundDescription();
		nsd.blockSize = findXMLInteget(xmlString, "BLKLEN");
		nsd.sampleRate = findXMLInteget(xmlString, "FS");

		return nsd;
	}

	/**
	 * Pull out an integer value from an xml string for a specified tag. 
	 * @param xmlDoc Entire document as an xml string
	 * @param tagName tag name
	 * @return Intger value or null if tag not found or string not a a valid integer. 
	 */
	private Integer findXMLInteget(String xmlDoc, String tagName) {
		String str = findTag(xmlDoc, tagName);
		if (str == null) {
			return null;
		}
		try {
			return Integer.valueOf(str);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * find the first instance of a tag in an xml document 
	 * and extract the tag's string. 
	 * @param xmlDoc
	 * @param tagName
	 * @return Tag string or null
	 */
	String findTag(String xmlDoc, String tagName) {
		int tagStart = xmlDoc.indexOf(tagName);
		if (tagStart == -1) return null;
		tagStart = xmlDoc.indexOf('>', tagStart);
		if (tagStart == -1) return null;
		int tagEnd = xmlDoc.indexOf('<', tagStart);
		if (tagEnd == -1) return null;
		String elStr = xmlDoc.substring(tagStart+1, tagEnd);
		return elStr;
	}

	ByteConverter byteConverter;
	/*
	 * Not enough header information to do this properly !
	 */
	private boolean interpretWavNetSound(int dataLen, byte[] duBuffer) {
		// fudge an X3 header, then send to packer. 
		X3FrameHeader x3FrameHeader = new X3FrameHeader();
		x3FrameHeader.crcHeadOk = true;
		x3FrameHeader.setnChan(8);
		x3FrameHeader.setnSamples((short) (dataLen/16));

		short[] soundData = new short[dataLen/2];
		for (int i = 0, j = 0; i < dataLen; i+=2, j++) {
			soundData[j] = (short) ((duBuffer[i+1]&0xFF)<<8 | (duBuffer[i]&0xFF));
		}

		packChannelData(x3FrameHeader, soundData );

		return true;
	}
	
	private int dataCalls = 0;

	/**
	 * Called when Network sound data have arrived.This is in a compressed
	 * x3 format. The x3 packet contains it's own header in the first few bytes which
	 * must be unpacked before the rest of the data.   
	 * @param buoyStatusDataUnit
	 * @param dataLen
	 * @param duBuffer
	 * @return true if decoded successfully
	 */
	private boolean interpretX3NetSound(int dataLen, byte[] duBuffer) {
		/*
		 * Start by reading the header info off the start of the data.
		 */
		X3FrameHeader x3FrameHeader = new X3FrameHeader();
		boolean headOk = x3FrameHeader.readHeader(duBuffer);
		if (!headOk) {
			System.out.println("Error unpacking compressed data header");
			//			% pull the information out  of the frame header. Basically it's 10 shorts.
			System.out.printf("Frame head content = key %d, nCh %d, nSamp %d, nBytes %d, crcH %d, crcDat %d\n", 
					x3FrameHeader.getX3_key(), x3FrameHeader.getnChan(), x3FrameHeader.getnSamples(), 
					x3FrameHeader.getnBytes(), x3FrameHeader.getCrcHead(), x3FrameHeader.getCrcData());
			return false;
		}
		long now = PamCalendar.getTimeInMillis();
		if (dataCalls++ == 0) {
			System.out.printf("Time from Start to first %d samples of data = %d millis\n", x3FrameHeader.getnSamples(), System.currentTimeMillis()-startTime);
		}
		if (now - lastTimeCheck > 2000) {
			lastTimeCheck = now;
			int secs = x3FrameHeader.getTimeCode();
			int mics = x3FrameHeader.getTimeMicros();
			long x3Time = x3FrameHeader.getTimeCode() * 1000L + x3FrameHeader.getTimeMicros() / 1000L;
			//			System.out.println(String.format("Local time %s, Net data time %s, %d secs. %d mics", PamCalendar.formatDBDateTime(now), 
			//					PamCalendar.formatDBDateTime(x3Time), secs, mics));
		}

		short dataCRC = CRC16.getCRC16(duBuffer, x3FrameHeader.getnBytes(), X3FrameHeader.X3_HDRLEN);
		if (dataCRC != x3FrameHeader.getCrcData()) {
			System.out.println(String.format("CRC error unpacking compressed data header, expected 0x%X got 0x%X",
					x3FrameHeader.getCrcData(), dataCRC));
			return false;
		}
		short[] soundData = new short[x3FrameHeader.getnChan() * x3FrameHeader.getnSamples()];
		x3Decoder.unpackX3Frame(x3FrameHeader, duBuffer, X3FrameHeader.X3_HDRLEN, soundData, networkSoundDescription.blockSize);
		//		x3JNI.decompress(soundData, duBuffer, X3FrameHeader.X3_HDRLEN, x3FrameHeader.nChan, 
		//				x3FrameHeader.nSamples, dataLen-X3FrameHeader.X3_HDRLEN, networkSoundDescription.blockSize);
		packChannelData(x3FrameHeader, soundData);
		return dataCRC == x3FrameHeader.getCrcData();
	}
	long lastTimeCheck;

	/**
	 * Blocks of data arriving over the network are probably a lot smaller than the size of the 
	 * raw data units we want so will repack into larger arrays (converting to double at the same time). 
	 * @param x3FrameHeader
	 * @param soundData
	 */
	private synchronized void packChannelData(X3FrameHeader x3FrameHeader, short[] soundData) {
		long millis;
		int nChan = x3FrameHeader.getnChan();
		int nSamples = x3FrameHeader.getnSamples();
		AcquisitionProcess daqProcess = acquisitionControl.getDaqProcess();
		if (prepDataUnits == null || prepDataUnits.length != nChan) {
			prepDataUnits = new RawDataUnit[nChan];
			millis = daqProcess.absSamplesToMilliseconds(totalSamples);
			for (int i = 0; i < nChan; i++) {
				prepDataUnits[i] = new RawDataUnit(millis, 1<<i, 0, duDuration);
				prepDataUnits[i].setRawData(new double[duDuration], false);
			}
		}
		double scale = 1./32767.;
		double[] chData;
		AudioDataQueue newDataUnitList = daqProcess.getNewDataQueue();
		if (newDataUnitList == null) {
			return;
		}
		for (int iChan = 0; iChan < nChan; iChan++) {
			chData = prepDataUnits[iChan].getRawData();
			int sPos = iChan;
			for (int iSamp = 0; iSamp < nSamples; iSamp++, sPos += nChan) {
				chData[duCounters[iChan]++] = soundData[sPos] * scale;
				if (duCounters[iChan] == duDuration) {
					if (iChan == 0) {
						totalSamples += duDuration;
					}
					if (iChan == 6) {
						// dummy point to put in a break;
						chData[0] = chData[0] * 1.;
					}
					// complete and send off the data unit
					prepDataUnits[iChan].setRawData(chData, true); // force the amplitude calculation. 
					newDataUnitList.addNewData(prepDataUnits[iChan], iChan);
					//					daqProcess.getRawDataBlock().addPamData(prepDataUnits[iChan]);
					// then get the raw data unit ready fo the next loop round.  
					millis = daqProcess.absSamplesToMilliseconds(totalSamples);
					long sm = System.currentTimeMillis();
//					System.out.printf("Sys time %s sample time %s, diff %d\n", PamCalendar.formatTime(sm), 
//							PamCalendar.formatTime(millis), sm-millis);
					prepDataUnits[iChan] = new RawDataUnit(millis, 1<<iChan, totalSamples, duDuration);
					prepDataUnits[iChan].setRawData(new double[duDuration], false);
					duCounters[iChan] = 0;
				}
			}
		}

	}

	int nReceived = 0;
	int lastPackId = 0;
	@Override
	public NetworkObject interpretData(NetworkObject receivedData) {
		//		if (nReceived < 5 || nReceived%1000 == 0) {
		if (receivedData.getDataType1() != NetworkReceiver.NET_AUDIO_DATA) {
			System.out.printf("Unexpected network data in NI Network DAQ of type %d(%d)\n", 
					receivedData.getDataType1(), receivedData.getDataType2());
			return null;
		}

		int packId = receivedData.getBuoyId2();
		//		if (packId != (lastPackId+1)) {
		//			System.out.printf("Packet sequence error jump %d to %d\n", lastPackId, packId);
		//		}
		lastPackId = packId;
		//		if (nReceived < 5 || nReceived%1000 == 0) {
		//			System.out.printf("NI Rx data Version %d, buoy id %d(%d) dataId %d(%d), len %d\n", dataVersion2, buoyId1,
		//					buoyId2, dataId1, dataId2, dataLen);
		//		}
		interpretData(receivedData);

		nReceived++;
		return null;
	}

	@Override
	public void socketClosed(NetworkReceiveThread networkReceiveThread) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getUnitName() {
		return acquisitionControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return getDeviceName();
	}

	@Override
	public Serializable getSettingsReference() {
		return niNetParams;
	}

	@Override
	public long getSettingsVersion() {
		return NINetworkDaqParams.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		niNetParams = ((NINetworkDaqParams) pamControlledUnitSettings.getSettings()).clone();
		return niNetParams != null;
	}

	/* (non-Javadoc)
	 * @see Acquisition.DaqSystem#getStallCheckSeconds()
	 */
	@Override
	public long getStallCheckSeconds() {
		// increase to 10 since it's often pretty slow to get going. 
		return 5;
	}

	public void cRioCommandLineError(CRioErrorStrings cRioError) {
		System.out.printf("CRio Error %s count %d severity %d\n", cRioError.getName(), cRioError.getErrorCount(), cRioError.getSeverity());
		if (cRioError.getSeverity() >= 2) {
			System.out.println("Issuing a full PAMGuard restart from NINetworkDAQ.java");
//			PamController.getInstance().restartPamguard();
		}
	}

	/* (non-Javadoc)
	 * @see Acquisition.DaqSystem#setSelected(boolean)
	 */
	@Override
	public void setSelected(boolean select) {
		super.setSelected(select);
		if (select) {
			prepareLogging();
		}
	}

	private void prepareLogging() {
		if (niDaqLogging == null) {
			niDaqLogging = new NIDaqLogging(this);
		}
		SQLLogging lo = acquisitionControl.getDaqProcess().getDaqStatusDataBlock().getLogging();
		lo.addAddOn(niDaqLogging);
	}

	/* (non-Javadoc)
	 * @see Acquisition.DaqSystem#getStatusBarComponent()
	 */
	@Override
	public Component getStatusBarComponent() {
		return niDaqStatusComponent.getComponent();
	}

	/**
	 * @return the niNetParams
	 */
	public NINetworkDaqParams getNiNetParams() {
		return niNetParams;
	}

	@Override
	public void newReceivedDataUnit(BuoyStatusDataUnit buoyStatusDataUnit, PamDataUnit dataUnit) {
		// TODO Auto-generated method stub
		
	}


}
