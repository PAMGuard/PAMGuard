package networkTransfer.receive;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Set;

import Acquisition.DaqStatusDataUnit;
import Array.ArrayManager;
import Array.Streamer;
import GPS.GPSDataBlock;
import GPS.GpsData;
import GPS.GpsDataUnit;
import PamUtils.LatLong;
import PamUtils.PamUtils;
import PamguardMVC.LastDataUnitStore;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Class for collecting data to do with individual buoys receiving data
 * over the network. 
 * @author Doug Gillespie
 *
 */
public class BuoyStatusDataUnit extends PamDataUnit {

	/**
	 * The first free channel number - incoming channel numbers will be shifted by this amount. 
	 */
	private BuoyStatusData buoyStatusData;
	private int totalPackets;
	private int unknownPackets;
	private int gpsCount;
	private NetworkReceiver networkReceiver;
	private LastDataUnitStore lastDataUnitStore = new LastDataUnitStore();
	private Socket socket;
	private GpsData gpsData;
	private int commandStatus = NetworkReceiver.NET_PAM_COMMAND_STOP;
	private Streamer hydrophoneStreamer;
	private GPSDataBlock gpsDataBlock;
	private double[] compassData;

//	private boolean genericStringPairsChanged = true;
	
	private static String[] compassNames = {"Head ", ", Pitch ",", Roll "};
	private static String[] shortCompassNames = {"H", "P", "R"};
	
//	private Double battVolts, chargeCurrent, loadCurrent;
	
	public BuoyStatusDataUnit(NetworkReceiver networkReceiver, int buoyId1, int buoyId2, int channelMap) {
		super(System.currentTimeMillis());
		buoyStatusData = new BuoyStatusData(buoyId1, buoyId2, channelMap);
		this.networkReceiver = networkReceiver;
		setChannelBitmap(channelMap);
		gpsDataBlock = new GPSDataBlock(networkReceiver.getNetworkReceiveProcess());
	}
	
	public BuoyStatusDataUnit(NetworkReceiver networkReceiver, BuoyStatusData buoyStatusData) {
		super(System.currentTimeMillis());
		this.buoyStatusData = buoyStatusData;
		this.networkReceiver = networkReceiver;
		super.setChannelBitmap(buoyStatusData.getChannelMap());
		gpsDataBlock = new GPSDataBlock(networkReceiver.getNetworkReceiveProcess());
	}
	
	public void newDataObject(PamDataBlock dataBlock, PamDataUnit dataUnit, int blockSeq, int receivedBytes) {
		buoyStatusData.setLastDataTime(dataUnit.getTimeMilliseconds());
		totalPackets++;
		lastDataUnitStore.addDataUnit(dataBlock, dataUnit, receivedBytes);
	}
	
	/**
	 * Find the last data unit from this buoy that belongs to the given data block. 
	 * @param unitClass last data block.
	 * @return last data unit from that block. 
	 */
	public PamDataUnit findLastDataUnit(Class unitClass) {
		return lastDataUnitStore.findLastDataUnit(unitClass);
	}
	
	/**
	 * Increment counter of unknown packets. 
	 */
	public void unknownPacket() {
		unknownPackets++;
	}
	
	/**
	 * Get a standard string name for a buoy. 
	 * @return a standard name in the form 'buoy xxx';
	 */
	public String getBuoyName() {
		return String.format("Buoy %03d", buoyStatusData.getBuoyId1());
	}

	/**
	 * @return the buoyId1
	 */
	public int getBuoyId1() {
		return buoyStatusData.getBuoyId1();
	}

	/**
	 * @return the buoyId2
	 */
	public int getBuoyId2() {
		return buoyStatusData.getBuoyId2();
	}

	/**
	 * The number of the first channel for data from this buoy - since
	 * all buoys will have the first channel as zero (I hope) this is basically
	 * used as a shift operator on the incoming channel numbers from the network.  
	 * @return the channel
	 */
	public int getLowestChannel() {
		return PamUtils.getLowestChannel(getChannelBitmap());
	}

	/**
	 * @return the creationTime
	 */
	public long getCreationTime() {
		return buoyStatusData.getCreationTime();
	}

	/**
	 * @return the lastDataTime
	 */
	public long getLastDataTime() {
		return buoyStatusData.getLastDataTime();
	}

	/**
	 * @return the totalPackets
	 */
	public int getTotalPackets() {
		return totalPackets;
	}
	
	public int getBlockPacketCount(PamDataBlock dataBlock) {
		return lastDataUnitStore.getDataBlockInfo(dataBlock).getnDataUnits();
	}

	
	public int getBlockPacketRXbytes(PamDataBlock dataBlock) {
		return lastDataUnitStore.getDataBlockInfo(dataBlock).getTotalBytesRX();
	}

	public void initialise() {
		lastDataUnitStore.clear();
	}

	public void setSocket(Socket socket) {
		this.socket = socket;		
		if (socket != null) {
			// use getHostAddress since it takes 0 time, calls to getHostName take several seconds. 
			buoyStatusData.setIpAddr(socket.getInetAddress().getHostAddress());
		}
	}
	
	/**
	 * @return the socket
	 */
	public Socket getSocket() {
		return socket;
	}
	
	

	public String getIPAddr() {
		if (socket == null) {
			return buoyStatusData.getIpAddr();
		}
		InetAddress inetAddr = socket.getInetAddress();
		// use getHostAddress since it takes 0 time, calls to getHostName take several seconds. 
//		System.out.printf("%s, \"%s\"\n", "inetAddr.getHostAddress()", inetAddr.getHostAddress());
//		System.out.printf("%s, \"%s\"\n", "inetAddr.getHostName()", inetAddr.getHostName());
//		System.out.printf("%s, \"%s\"\n", "inetAddr.toString()", inetAddr.toString());
		return inetAddr.getHostAddress();
	}
	
	public String getPort() {
		if (socket == null) {
			return null;
		}
		return new Integer(socket.getPort()).toString();
	}

	/**
	 * @return the gpsData
	 */
	public GpsData getGpsData() {
		if (gpsData == null) {
			return gpsData = getDefaultGpsData();
		}
		return gpsData;
	}

	/**
	 * Get a default location from the array manager. 
	 * @return a default location to be used in teh absence of any other gps data. 
	 */
	private GpsData getDefaultGpsData() {
//		ArrayManager arrayManager = ArrayManager.getArrayManager();
//		arrayManager.
		// for now return a default GPS position.
		return new GpsData();
	}

	/**
	 * @param gpsData the gpsData to set
	 */
	public void setGpsData(long timeMilliseconds, GpsData gpsData) {
		this.gpsData = gpsData;
		gpsCount++;
		gpsDataBlock.addPamData(new GpsDataUnit(timeMilliseconds, gpsData));
	}

	public void setCompassData(long timeInMillis, double[] compassData) {
		GpsData someData = gpsData;
		if (gpsData == null) {
			someData = getDefaultGpsData();
			gpsDataBlock.addPamData(new GpsDataUnit(timeInMillis, someData));
		}
		someData.setMagneticHeading(compassData[0]);
		this.compassData = compassData;
	}

	public double[] getCompassData() {
		return compassData;
	}

	public int getGpsCount() {
		return gpsCount;
	}
	
	public Object getPositionString() {
		String posString = "";
		String[] cn = compassNames;
		if (gpsData != null && gpsData.getLatitude() != 0 && gpsData.getLongitude() != 0) {
			posString = String.format("%s, %s ", LatLong.formatLatitude(gpsData.getLatitude()), LatLong.formatLongitude(gpsData.getLongitude()));
			cn = shortCompassNames;
		}
		if (compassData != null) {
			for (int i = 0; i < 3; i++) {
			posString += String.format("%s%3.1f%s", cn[i], compassData[i], LatLong.deg);
			}
		}
		return posString;
	}

	/**
	 * @param commandStatus the commandStatus to set
	 */
	public void setCommandStatus(int commandStatus) {
		this.commandStatus = commandStatus;
	}

	/**
	 * @return the commandStatus
	 */
	public int getCommandStatus() {
		return commandStatus;
	}

	/**
	 * @return the networkReceiver
	 */
	public NetworkReceiver getNetworkReceiver() {
		return networkReceiver;
	}

	/**
	 * @param networkReceiver the networkReceiver to set
	 */
	public void setNetworkReceiver(NetworkReceiver networkReceiver) {
		this.networkReceiver = networkReceiver;
	}

	/**
	 * Set the hydrophone streamer for the buoy. Also sets a
	 * reference to the buoy stats in the streamer. 
	 * @param hydrophoneStreamer the hydrophoneStreamer to set
	 */
	public void setHydrophoneStreamer(Streamer hydrophoneStreamer) {
		this.hydrophoneStreamer = hydrophoneStreamer;
//		hydrophoneStreamer.setBuoyStats(this);
	}

	/**
	 * @return the hydrophoneStreamer
	 */
	public Streamer getHydrophoneStreamer() {
		return hydrophoneStreamer;
	}

//	/**
//	 * @return the battVolts
//	 */
//	public Double getBattVolts() {
//		return battVolts;
//	}
//
//	/**
//	 * @param battVolts the battVolts to set
//	 */
//	public void setBattVolts(Double battVolts) {
//		this.battVolts = battVolts;
//	}
//
//	/**
//	 * @return the chargeCurrent
//	 */
//	public Double getChargeCurrent() {
//		return chargeCurrent;
//	}
//
//	/**
//	 * @param chargeCurrent the chargeCurrent to set
//	 */
//	public void setChargeCurrent(Double chargeCurrent) {
//		this.chargeCurrent = chargeCurrent;
//	}
//
//	/**
//	 * @return the loadCurrent
//	 */
//	public Double getLoadCurrent() {
//		return loadCurrent;
//	}
//
//	/**
//	 * @param loadCurrent the loadCurrent to set
//	 */
//	public void setLoadCurrent(Double loadCurrent) {
//		this.loadCurrent = loadCurrent;
//	}

	/**
	 * @return the unknownPackets
	 */
	public int getUnknownPackets() {
		return unknownPackets;
	}

	/**
	 * @param substring
	 * @param substring2
	 * @return whether a genericStringPair has been added or removed
	 */
	public void setPairData(String substring, Serializable dataObj) {
		if (dataObj==null){ 
			buoyStatusData.getGenericStringPairs().remove(substring);
		}else{
			// if put is null => change
			buoyStatusData.getGenericStringPairs().put(substring, new BuoyStatusValue(dataObj));
		}
	}
	
	public Hashtable<String, BuoyStatusValue> getPairDataAll(){
		return buoyStatusData.getGenericStringPairs();
	}
	
	public Set<String> getPairKeys(){
		return buoyStatusData.getGenericStringPairs().keySet();
	}
	
	public BuoyStatusValue getPairVal(String key){
		return buoyStatusData.getGenericStringPairs().get(key);
	}


	@Override
	public void setChannelBitmap(int channelBitmap) {
		super.setChannelBitmap(channelBitmap);
		buoyStatusData.setChannelMap(channelBitmap);
	}

	/**
	 * @return the buoyStatusData
	 */
	public BuoyStatusData getBuoyStatusData() {
		return buoyStatusData;
	}

//	/**
//	 * @return the genericStringPairsChanged
//	 */
//	public boolean isGenericStringPairsChanged() {
//		return genericStringPairsChanged;
//	}
//
//	/**
//	 * @param genericStringPairsChanged the genericStringPairsChanged to set
//	 */
//	public void setGenericStringPairsChanged(boolean genericStringPairsChanged) {
//		this.genericStringPairsChanged = genericStringPairsChanged;
//	}
	


}
