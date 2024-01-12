package AIS;

import java.io.Serializable;
import java.util.Calendar;

import NMEA.NMEABitArray;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamUtils.PamCalendar;

public class AISStaticData extends AISReport implements Serializable, ManagedParameters{

	public static final long serialVersionUID = 0;
	
	public int imoNumber;
	public String callSign;
	public String shipName;
	public int shipType;
	public int etaMonth;
	public int etaDay;
	public int etaHour;
	public int etaMinute;
	public double staticDraught;
	public String destination;
	public String dataClass = "A";
	/*
	 * Although these are int in the AIS data, use double so they are compatible with 
	 * hydrophone location information in rest of Pamguard
	 */
	public double dimA, dimB, dimC, dimD;
	public long etaMilliseconds;
	
	// some extras for aids to navigation data
	boolean offPosition;
	boolean virtualAidFlag;

	public int classBPart = -1;
	
	AISStaticData(int messageId, NMEABitArray bitData) {
		switch(messageId) {
		case 5: // standard static data
			unpackStandardStaticData(bitData);
			break;
		case 19:
			unpackClassBStaticData(bitData);
			break;
		case 21:
			unpackAidtoNavigationData(bitData);
			break;
		case 24:
			unpackClassBStaticData24(bitData);
			break;
		}
	}
	
	
	boolean unpackStandardStaticData(NMEABitArray bitData) {
		//07/08/2017 - this sometimes causes a comple PG freeze. Have used try catch to solve for now. 
		try {
		int dataSetIndicator = bitData.getUnsignedInteger(38, 39);
		switch (dataSetIndicator) {
		case 0:
			return unpackStandardStaticShipData(bitData);
		case 1:
			break;
		case 2:
			break;
		case 3:
			break;
		}
		return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false; 
		}
	}

	/**
	 * Constructor for use when reading back from database. 
	 * @param callSign callSign
	 * @param shipName shipName
	 * @param shipType shipType
	 * @param etaMillis eta Milliseconds
	 * @param draught draught
	 * @param destination destination
	 */
	public AISStaticData(String callSign, String shipName, int shipType, 
			long etaMillis, double draught, String destination) {
		dataClass = "";
		this.callSign = callSign;
		this.shipName = shipName;
		this.shipType = shipType;
		this.etaMilliseconds = etaMillis;
		this.staticDraught = draught;
		this.destination = destination;
	}

	private void unpackClassBStaticData(NMEABitArray bitData) {
		dataClass = "B";
		shipName = bitData.getString(143, 262);
		shipType = bitData.getUnsignedInteger(263, 270);
		dimA = bitData.getUnsignedInteger(271, 279);
		dimB = bitData.getUnsignedInteger(280, 288);
		dimC = bitData.getUnsignedInteger(289, 294);
		dimD = bitData.getUnsignedInteger(295, 300);
		
	}
	
	private void unpackClassBStaticData24(NMEABitArray bitData) {
		dataClass = "B";
		classBPart  = bitData.getUnsignedInteger(38, 39);
		switch(classBPart) {
		case 0:
			shipName = bitData.getString(40, 159);
			break;
		case 1:
			shipType = bitData.getUnsignedInteger(40, 47);
			callSign = bitData.getString(90, 131);
			dimA = bitData.getUnsignedInteger(132, 140);
			dimB = bitData.getUnsignedInteger(141, 149);
			dimC = bitData.getUnsignedInteger(150, 155);
			dimD = bitData.getUnsignedInteger(156, 161);
			break;
		}
		
		
	}
	private void unpackAidtoNavigationData(NMEABitArray bitData) {
		shipName = bitData.getString(43, 162);
		shipType = bitData.getUnsignedInteger(38, 42);
		dimA = bitData.getUnsignedInteger(219, 227);
		dimB = bitData.getUnsignedInteger(228, 236);
		dimC = bitData.getUnsignedInteger(237, 242);
		dimD = bitData.getUnsignedInteger(243, 248);
		offPosition = bitData.getUnsignedInteger(259, 259) == 1;
		virtualAidFlag = bitData.getUnsignedInteger(269, 269) == 1;
		
		int extensionLength = bitData.size() - 272;
		if (extensionLength > 0) {
			shipName += bitData.getString(272, bitData.size());
		}
		
	}


	public boolean unpackStandardStaticShipData(NMEABitArray bitData) {
//		mmsiNumber = bitData.getUnsignedInteger(8, 37);
		dataClass = "A";
		imoNumber = bitData.getUnsignedInteger(40, 69);
		callSign = bitData.getString(70, 111);
		shipName = bitData.getString(112, 231);
		shipType = bitData.getUnsignedInteger(232, 239);
		dimA = bitData.getUnsignedInteger(240, 248);
		dimB = bitData.getUnsignedInteger(249, 257);
		dimC = bitData.getUnsignedInteger(258, 263);
		dimD = bitData.getUnsignedInteger(264, 269);
		etaMonth = bitData.getUnsignedInteger(274, 277);
		etaDay = bitData.getUnsignedInteger(278, 282) - 1; // convert immediately to Java standard (base 0)
		etaHour = bitData.getUnsignedInteger(283, 287);
		etaMinute = bitData.getUnsignedInteger(288, 293);
		staticDraught = bitData.getUnsignedInteger(294, 301)/10.;
		destination = bitData.getString(302, 421);

		destination = destination.replace("@", " ");
		shipName = shipName.replace("@", " ");
		callSign = callSign.replace("@", " ");		
		/*
		 * Now set the eta in milliseconds - will be needed
		 */
		Calendar c = Calendar.getInstance();
		long now = PamCalendar.getTimeInMillis();
		int month = c.get(Calendar.MONTH);  // Jan is 0, Dec is 11
		int year = c.get(Calendar.YEAR);
//		int day = c.get(Calendar.DATE);
		if (etaMonth < month) year++; // it must be arriving next year
		c.set(year, etaMonth, etaDay, etaHour, etaMinute, 0);
		etaMilliseconds = c.getTimeInMillis();	
		
//		System.out.println(this);
		return true;
	}
	
	@Override
	public String toString() {
		return new String("; Ship : " + imoNumber + " ; call sign: " + callSign + 
				"; type: " + getVesselTypeAndCargo(shipType) + " ; name: " + shipName + 
				" ; dest - "  + destination + "; draught - " + staticDraught + 
				"; ETA: " + String.format("%2d-%2d %2d:%2d",etaMonth, etaDay, etaHour, etaMinute) +
				String.format("; Dimension ABCD %d:%d:%d:%d", dimA, dimB, dimC, dimD)); 
	}
	public String shipTypeAndCargo() {
		return getVesselTypeAndCargo(shipType);
	}
	public String getStationTypeString(StationType stationType, int type) {
		if (stationType == null) {
			return null;
		}
		switch(stationType) {
		case BASESTATION:
			return stationType.BASESTATION.toString();
		case A:
		case B:
			return getVesselTypeAndCargo(type);
		case NAVAID:
			return getNavAidType(type);
		case UNKNOWN:
			return null;
		}
		return null;
	}
	private String getNavAidType(int type) {
		switch (type) {
		case 0:
			return "Default, Type of Aid to Navigation not specified";
		case 2:
			return "RACON (radar transponder marking a navigation hazard)";
		case 3:
			return "Fixed structure off shore (oil, wind, etc)";
		case 4:
			return "Spare, Reserved for future use.";
		case 5:
			return "Light, without sectors";
		case 6:
			return "Light, with sectors";
		case 7:
			return "Leading Light Front";
		case 8:
			return "Leading Light Rear";
		case 9:
			return "Beacon, Cardinal N";
		case 10:
			return "Beacon, Cardinal E";
		case 11:
			return "Beacon, Cardinal S";
		case 12:
			return "Beacon, Cardinal W";
		case 13:
			return "Beacon, Port hand";
		case 14:
			return "Beacon, Starboard hand";
		case 15:
			return "Beacon, Preferred Channel port hand";
		case 16:
			return "Beacon, Preferred Channel starboard hand";
		case 17:
			return "Beacon, Isolated danger";
		case 18:
			return "Beacon, Safe water";
		case 19:
			return "Beacon, Special mark";
		case 20:
			return "Cardinal Mark N";
		case 21:
			return "Cardinal Mark E";
		case 22:
			return "Cardinal Mark S";
		case 23:
			return "Cardinal Mark W";
		case 24:
			return "Port hand Mark";
		case 25:
			return "Starboard hand Mark";
		case 26:
			return "Preferred Channel Port hand";
		case 27:
			return "Preferred Channel Starboard hand";
		case 28:
			return "Isolated danger";
		case 29:
			return "Safe Water";
		case 30:
			return "Special Mark";
		case 31:
			return "Light Vessel / LANBY / Rigs";
		}
		return "Navaid type " + type;
	}


	public String getVesselTypeAndCargo(int type) {
		// first and second digits are interpreted differently
		// so best to combine these in single string in one go.
		int dig1 = (int) Math.floor(type / 10);
		int dig2 = type % 10;
		String string = null;
		switch (dig1) {
		case 1:
			return "Unknown vessel type";
		case 2:
			string = new String("WIG");
			break;
		case 3:
			string = new String("Vessel");
			switch (dig2) {
			case 0:
				string = new String("Fishing Vessel");
				break;
			case 1:
				string += " - towing";
				break;
			case 2:
				string += " - towing (L>200m or W>25m)";
				break;
			case 3:
				string += " - dredging / uw ops";
				break;
			case 4:
				string += " - diving";
				break;
			case 5:
				string = new String("Military Vessel");
				break;
			case 6:
				string = new String("Sailing Vessel");
				break;
			case 7:
				string = new String("Pleaseure craft");
				break;
			}
			return string;
		case 4:
			string = new String("HSC");
			break;
		case 5:
			switch (dig2) {
			case 0:
				return new String("Pilot Vessel");
			case 1:
				return new String("Search and Rescue Vessel");
			case 2:
				return new String("Tug");
			case 3:
				return new String("Port Tender");
			case 4:
				return new String("Pollution Control Vessel");
			case 5:
				return new String("Law Enforcement Vessel");
			case 6:
				return new String("Unknown Vessel");
			case 7:
				return new String("Unknown Vessel");
			case 8:
				return new String("Medical Transport Vessel");
			case 9:
				return new String("Resolution 18 Vessel");
			}
		case 6:
			string =  new String("Passenger Ship");
			break;
		case 7:
			string =  new String("Cargo Ship");
			break;
		case 8:
			string =  new String("Tanker");
			break;
		case 9:
			string =  new String("Ship");
			break;
		}
		// then append the cargo type (types 5 and 3 have already retured)
		switch (dig2) {
		case 1:
			string += " carrying hazardous material category A";
			break;
		case 2:
			string += " carrying hazardous material category B";
			break;
		case 3:
			string += " carrying hazardous material category C";
			break;
		case 4:
			string += " carrying hazardous material category D";
			break;
		case 5:
		case 6:
		case 7:
		case 8:
			string += " carrying unknown materials";
			break;
		}
		
		return string;
	}
	
	public double getLength() {
		return dimA + dimB;
	}
	
	public double getWidth() {
		return dimC + dimD;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DISPLAY);
		return ps;
	}

}
