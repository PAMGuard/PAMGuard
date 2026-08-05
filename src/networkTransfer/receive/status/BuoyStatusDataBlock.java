package networkTransfer.receive.status;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Set;

import GPS.GpsDataUnit;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import networkTransfer.receive.MqttNetReceiver;
import networkTransfer.receive.MqttReceiveThread;
import networkTransfer.receive.NetworkReceiver;

public class BuoyStatusDataBlock extends PamDataBlock<BuoyStatusDataUnit> {
	
	NetworkReceiver netReceiver;

	public BuoyStatusDataBlock(PamProcess parentProcess, NetworkReceiver networkReceiver) {
		super(BuoyStatusDataUnit.class, "Buoy Status Data", parentProcess, 0);
		super.setClearAtStart(false);
		this.netReceiver = networkReceiver;
	}


	@Override
	protected synchronized int removeOldUnitsS(long mastrClockSample) {
		return 0; // units are never removed. 
	}


	@Override
	protected synchronized int removeOldUnitsT(long currentTimeMS) {
		return 0; // units are never removed. 
	}


//	@Override
//	public synchronized void clearAll() {
//		// never delete !
//	}
//
//	/**
//	 * ClearAll is overridden to d onothing 
//	 */
//	public synchronized void clearList() {
//		super.clearAll();
//	}
//	@Override
//	public synchronized void addPamData(BuoyStatusDataUnit pamDataUnit) {
//		GpsDataUnit du = findDataUnit(pamDataUnit.getChannelBitmap());
////		if (du == null) {
////			super.addPamData(pamDataUnit);
////		}
////		else if (pamDataUnit != null){
////			du.setGpsData(pamDataUnit.getGpsData());
////		}
//	}
	
	
	public ArrayList<BuoyStatusDataUnit> getUniqueBuoyStatuses() {
		ArrayList<Integer> buoyIdsRegistered = new ArrayList<Integer>();
		ArrayList<BuoyStatusDataUnit> statuses = new ArrayList<BuoyStatusDataUnit>();
		for(BuoyStatusDataUnit unit:this.getDataCopy()) {
			if(!buoyIdsRegistered.contains(unit.getBuoyId1())){
				statuses.add(unit);
				buoyIdsRegistered.add(unit.getBuoyId1());
			}
		}
		return statuses;
	}
	
	public int getNBuoysRegistered() {
		return getUniqueBuoyStatuses().size();
	}
	
	public boolean isBuoySocketOpen(BuoyStatusDataUnit b) {
		if(this.netReceiver.connectionThread instanceof MqttNetReceiver) {
			MqttNetReceiver mqttThread =  (MqttNetReceiver) this.netReceiver.connectionThread;
			if(mqttThread == null) {
				return false;
			}
			MqttReceiveThread buoyReceiver = mqttThread.getBuoyReceiveThread(b.getBuoyId1());
			if(buoyReceiver==null) {
				return false;
			}else {
				return buoyReceiver.isAlive();
			}
		}else {
			return b.getSocket()!=null;
		}
		
	}
	
	public int getNBuoysConnectedAndRunning() {
		int count = 0;
		for(BuoyStatusDataUnit unit:getUniqueBuoyStatuses()) {
			long timeSinceLastData = System.currentTimeMillis()-unit.getBuoyStatusData().getLastDataTime();
			long tenMinutes = 1000L*60L*10L;
			if(timeSinceLastData>tenMinutes) {
				//System.out.println(unit.getBuoyName()+": Last buoy data time was "+unit.getLastDataTime()+" at "+PamCalendar.getTime()+". Milliseconds since last data: "+timeSinceLastData);
				continue;
			}
			if(unit.getBuoyStatusData()==null) {
				//System.out.println(unit.getBuoyName()+": buoy status data is null");
				continue;
			}
			if(!isBuoySocketOpen(unit)) {
				//System.out.println(unit.getBuoyName()+": buoy socket is not open");
				continue;
			}
			if(unit.getCommandStatus()!=NetworkReceiver.NET_PAM_COMMAND_START) {
				System.out.println(unit.getBuoyName()+": buoy pamguard is not running");
				continue;
			}
			count++;
		}
		return count;
	}
	
	public synchronized BuoyStatusDataUnit findDataUnit(int channelMap) {
		ListIterator<BuoyStatusDataUnit> li = getListIterator(0);
		BuoyStatusDataUnit du;
		while (li.hasNext()) {
			du = li.next();
			if ((du.getChannelBitmap() & channelMap) != 0) {
				return du;
			}
		}
		return null;
	}
	
	public BuoyStatusDataUnit findBuoyStatusData(int buoyId1, int buoyId2) {
		synchronized(this) {
			ListIterator<BuoyStatusDataUnit> li = getListIterator(0);
			BuoyStatusDataUnit du;
			while (li.hasNext()) {
				du = li.next();
				if (du.getBuoyId1() == buoyId1 && du.getBuoyId2() == buoyId2) {
					return du;
				}
			}
			return null;
		}
	}

	/**
	 * Find a buoy status data unit from it's internet address. 
	 * @param inetAddress internet address
	 * @return buoy unit or null
	 */
	public BuoyStatusDataUnit findBuoyStatusData(InetAddress inetAddress) {
		synchronized(this) {
			ListIterator<BuoyStatusDataUnit> li = getListIterator(0);
			BuoyStatusDataUnit du;
			if (inetAddress == null) {
				return null;
			}
			String wantAddr = inetAddress.getHostAddress();
			while (li.hasNext()) {
				du = li.next();
				String duAddr = du.getIPAddr();
				if (duAddr == null) {
					continue;
				}
				if (duAddr.equals(wantAddr)) {
					return du;
				}
			}
			return null;
		}
		
	}

//	public Set<String> getBuoyGenericDataSet(){
//		ListIterator<BuoyStatusDataUnit> it = getListIterator(0);
//		Set<String> masterKeys = null;
//		while (it.hasNext()){
//			BuoyStatusDataUnit thisUnit = it.next();
//			Set<String> keys = thisUnit.getPairKeys();
//			thisUnit.setGenericStringPairsChanged(false);
//			if (masterKeys==null || masterKeys.isEmpty()){
//				masterKeys=keys; //.clone()
//			}else{
//				masterKeys.addAll(keys);
//			}
//		}
//		return masterKeys;
//	}
//	public boolean getBuoyGenericDataChanged(){
//		ListIterator<BuoyStatusDataUnit> it = getListIterator(0);
//		boolean changed= false;
//		
//		
//		// TODO for now ignore new/deleted buoys but should fix this
//		
////		if (buoy added or removed){
////			, check all or return all
////		}else{
////			for each buoy{
////				if (buoy.changed==true){
////					changed=true;
////				}
////			}
////		}
//		
//		//check all	
//		while (it.hasNext()){
//			BuoyStatusDataUnit thisUnit = it.next();
//			changed = thisUnit.isGenericStringPairsChanged();
//			if (changed){
//				return true;
//			}
//		}
//		
//		
//		return changed;
//	}
//	
//	public String[] getBuoyGenericDataList(){ // somehow need to syncronise these between devices , tablename and value getting 
//		Set<String> masterKeys = getBuoyGenericDataSet();
//		String[] stringArray =null;
//		if (masterKeys!=null){
//			stringArray = Arrays.copyOf(masterKeys.toArray(), masterKeys.size(), String[].class);
//			Arrays.sort(stringArray);
//		}
//		return stringArray;
//	}
}
