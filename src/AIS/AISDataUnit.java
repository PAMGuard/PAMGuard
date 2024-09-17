package AIS;

import java.io.Serializable;
import java.util.ArrayList;

import NMEA.NMEABitArray;
import NMEA.NMEADataBlock;
import PamDetection.LocContents;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import warnings.PamWarning;
import warnings.WarningSystem;

public class AISDataUnit extends PamDataUnit implements Serializable, Cloneable {

	AISLocalisation aisLocalisation;
	
	private static PamWarning aisWarning = new PamWarning("AIS Decoder", "", 1);


	//	String latestString;

	/**
	 * Constructor used in data collection - doesn't do much
	 */
	public AISDataUnit(long timeMilliseconds) {
		super(timeMilliseconds);
		setLocalisation(aisLocalisation = new AISLocalisation(this));
		positionReports = new ArrayList<AISPositionReport>();
	}

	/**
	 * Constructor used when reading back from binary files. 
	 * @param timeMillis time millis
	 * @param charData 6 bit character data string
	 * @param fillBits fill bits to ignore at end of string
	 */
	public AISDataUnit(long timeMillis, String charData, int fillBits) {
		super(timeMillis);
		this.charData = charData;
		this.fillBits = fillBits;
		setLocalisation(aisLocalisation = new AISLocalisation(this));
		positionReports = new ArrayList<AISPositionReport>();
	}

	static final long serialVersionUID = 1;
	/**
	 * Flag used when a limit is placed on range. If it's clearly 
	 * out of range, then this flag is set and no more updates
	 * are made from static data. 
	 * <p>
	 * the flag can be set false again if new position data come 
	 * within range. 
	 */
	protected boolean isInRange = true;
	protected boolean newUnit = true;
	/**
	 * AIVDM, etc.
	 */
	protected String dataType;

	protected StationType stationType;
	/**
	 * Pulled out from the start of the AIVDM string. 
	 */
	protected int messageId;
	/**
	 * Total number of sentences needed to transfer the message
	 */
	protected int totalSentences;
	/**
	 * Message sentence number
	 */
	protected int sentenceNumber;

	public String getStationtypeString() {
		if (stationType == null) {
			return null;
		}
		return stationType.toString();
	}
	/**
	 * The "message sequential identifier" is a number from 0 to 9 that is sequentially assigned as needed. This
identifier is incremented for each new multi-sentence message. The count resets to 0, after 9 is used. For radio
broadcast messages requiring multiple sentences, each sentence of the message contains the same sequential
identification number. The purpose of this number is to link the separate sentences containing portions of the same
radio message. This allows for the possibility that other sentences might be interleaved with the message
sentences that contain the complete message contents. This number also links an ABK-sentence
acknowledgement to the appropriate BBM-sentence.
	 */
	protected int sequentialIdentifier = -1;
	/**
	 *When provided, the AIS channel is indicated as either "1" or "2." This channel indication is relative to the
operating conditions of the transponder when the packet is received. This field shall be null when the channel
identification is not provided.
	 */
	protected String aisChannel;
	/**
	 * Contents of the M.1371 radio message using the six-bit field type
	 * <p>
	 * The maximum string length of encapsulation is limited such that the total number of sentence characters
does not exceed 82. This field supports a maximum of 62 valid characters for messages transferred using multiple
sentences, and 63 valid characters for messages using a single sentence.
	 */
	protected String charData;
	/**
	 * Number of "fill-bits" added to complete the last six-bit character
	 * <p>
	 * Each character in the preceding six-bit coded character string represents six binary bits. This parameter
indicates the number of bits that were added to the end of the binary packet as a result of creating the last
character of the string. When the coding of the last six-bit character of the message packet does not create
additional "fill-bits," this value shall be set to zero. The value "0" indicates that no "fill-bits" were added. This field
may not be null.
	 */
	protected int fillBits;
	public int mmsiNumber;
	//	// data extracted from the 6bit binary data
	private ArrayList<AISPositionReport> positionReports;
	private AISStaticData staticData; 

	transient NMEABitArray bitData;

	static private final int MINIMUMBITS = 37;
	static private final int POSITIONREPORTBITS = 167;
	static private final int STATICDATABITS = 421;

	/**
	 * Decode the binary data which are currently in a
	 * character string
	 * @return true if the message was decoded successfully
	 */
	public boolean decodeMessage() {
		// time to process the string ...
		//		More updated useful information at http://gpsd.berlios.de/AIVDM.html
		try {
			if (bitData == null){
				bitData = NMEADataBlock.six2eight(charData, fillBits);
			}
			if (bitData == null || bitData.size() < MINIMUMBITS) {
				return false;
			}
			messageId = bitData.getUnsignedInteger(0, 5);
			mmsiNumber = bitData.getUnsignedInteger(8, 37);
			AISPositionReport positionReport = null;

			switch (messageId) {
			case 1:
			case 2:
			case 3:
				if (bitData.size() < POSITIONREPORTBITS) {
					return false;
				}
				positionReports.add(new AISPositionReport(messageId, getTimeMilliseconds(), bitData));
				stationType = StationType.A;
				return true;
			case 4:
			case 11:
				//			System.out.println(String.format("AIS Base station report id %d", messageId));
				stationType = StationType.BASESTATION;
				return unpackBaseStationReport(messageId, bitData);
			case 5:
				if (bitData.size() < STATICDATABITS) {
					return false;
				}
				staticData = new AISStaticData(messageId, bitData);
				stationType = StationType.A;
				return true;
			case 7:
				// binary acknowledge - boring
				return true;
			case 8:
				// binary broadcast message - can't really use this !
				return true;
			case 18:
				stationType = StationType.B;
				positionReport = new AISPositionReport(messageId, getTimeMilliseconds(), bitData);
				if (positionReport.reportOk) {
					positionReports.add(positionReport);
					return true;
				}
				return false;
			case 19:
				stationType = StationType.B;
				positionReport = new AISPositionReport(messageId, getTimeMilliseconds(), bitData);
				if (positionReport.reportOk) {
					staticData = new AISStaticData(messageId, bitData);
					positionReports.add(positionReport);
					return true;
				}
				return false;
			case 20:
				// data link management message = boring
				return true;
			case 21: // aid to navigation
				stationType = StationType.NAVAID;
				positionReport = new AISPositionReport(messageId, getTimeMilliseconds(), bitData);
				if (positionReport.reportOk) {
					staticData = new AISStaticData(messageId, bitData);
					positionReports.add(positionReport);
					return true;
				}
				return false;
			case 24:
				stationType = StationType.B;
				staticData = new AISStaticData(messageId, bitData);
				return true;
			}
		}
		catch (Exception e) {
			String msg = "Error unpacking AIS Data: " + e.getMessage();
			System.out.println(msg);
			aisWarning.setEndOfLife(PamCalendar.getTimeInMillis() + 5000);
			aisWarning.setWarningMessage(msg);
			WarningSystem.getWarningSystem().addWarning(aisWarning);
		}
		return false;
	}

	private boolean unpackBaseStationReport(int messageId2,
			NMEABitArray bitData2) {
		//		staticData = new AISStaticData("", "Base Station", 0, 0, 0, null);
		positionReports.add(new AISPositionReport(messageId2, getTimeMilliseconds(), bitData2));
		return true;
	}

	public boolean isComplete() {
		return (positionReports.size() > 0 && staticData != null);
	}

	/**
	 * Update an existing AIS data unit with new data.
	 * @param newAISUnit
	 */
	public void update(AISDataUnit newAISUnit) {
		newUnit = false;
		if (newAISUnit.positionReports.size() != 0) {
			this.positionReports.add(newAISUnit.getPositionReport());
			aisLocalisation.getLocContents().setLocContent(LocContents.HAS_BEARING | LocContents.HAS_DEPTH |
					LocContents.HAS_LATLONG | LocContents.HAS_RANGE);
		}
		if (newAISUnit.staticData != null) {
			if (this.staticData == null) {
				this.staticData = newAISUnit.staticData;
			}
			else {
				mergeStaticData(newAISUnit.staticData);
			}
		}
		this.charData = newAISUnit.charData;
		this.bitData = newAISUnit.bitData;
		this.fillBits = newAISUnit.fillBits;
	}

	private void mergeStaticData(AISStaticData newStatic) {
		if (newStatic.classBPart < 0) {
			this.staticData = newStatic;
		}
		else if (newStatic.classBPart == 0) {
			this.staticData.shipName = newStatic.shipName;
		}
		else if (newStatic.classBPart == 1) {
			this.staticData.callSign = newStatic.callSign;
			this.staticData.shipType = newStatic.shipType;
			this.staticData.dimA = newStatic.dimA;
			this.staticData.dimB = newStatic.dimB;
			this.staticData.dimC = newStatic.dimC;
			this.staticData.dimD = newStatic.dimD;
		}

	}

	@Override
	protected AISDataUnit clone() {
		try {
			AISDataUnit newUnit = (AISDataUnit) super.clone();
			newUnit.aisLocalisation = new AISLocalisation(newUnit);
			//			if (latestString != null) {
			//				newUnit.latestString = new String(latestString); 
			//			}
			return newUnit;
		}
		catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
			return null;
		}
	}

	public AISPositionReport getPositionReport() {
		if (positionReports.size() == 0) {
			return null;
		}
		return positionReports.get(positionReports.size()-1);
	}

	public AISPositionReport findPositionReport(long timeMillis) {

		if (positionReports.size() == 0) {
			return null;
		}
		long bestT = Math.abs(positionReports.get(0).timeMilliseconds - timeMillis);
		long newT;
		int bestInd = 0;
		for (int i = 1; i < positionReports.size(); i++) {
			newT = Math.abs(positionReports.get(i).timeMilliseconds - timeMillis);
			if (newT < bestT) {
				bestT = newT;
				bestInd = i;
			}
		}
		return positionReports.get(bestInd);
	}

	public AISStaticData getStaticData() {
		return staticData;
	}

	public ArrayList<AISPositionReport> getPositionReports() {
		return positionReports;
	}

	public void addPositionReport(AISPositionReport positionReport) {
		if (positionReports == null) {
			positionReports = new ArrayList<AISPositionReport>();
		}
		positionReports.add(positionReport);
	}

	public void setStaticData(AISStaticData staticData) {
		this.staticData = staticData;
	}


}
