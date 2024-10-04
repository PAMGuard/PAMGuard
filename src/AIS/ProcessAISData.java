package AIS;

import NMEA.NMEADataBlock;
import NMEA.NMEADataUnit;
import PamController.PamController;
import PamDetection.LocContents;
import PamUtils.PamCalendar;
import PamView.symbol.StandardSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;

/**
 * Process NMEA data to extract AIS information
 * @author Doug Gillespie
 *
 */
public class ProcessAISData extends PamProcess {


	AISControl aisControl;
	
	AISDataUnit aivdm = new AISDataUnit(PamCalendar.getTimeInMillis());
	
	AISDataBlock aisDataBlock;
	
	ProcessAISData(AISControl aisController) {

		super(aisController, null);
		
		aisControl = aisController;

		setProcessName("AIS Processing");

//		allNmeaData.addObserver(this);
		
		addOutputDataBlock(aisDataBlock = new AISDataBlock(aisControl, this));
		
		aisDataBlock.setOverlayDraw(new AISGraphics(aisController, aisDataBlock));
		aisDataBlock.setPamSymbolManager(new StandardSymbolManager(aisDataBlock, AISGraphics.defaultSymbol, true));
		
		aisDataBlock.setLocalisationContents(LocContents.HAS_BEARING | LocContents.HAS_DEPTH |
				LocContents.HAS_LATLONG | LocContents.HAS_RANGE);
		
		aisDataBlock.SetLogging(new AISLogger(aisDataBlock, this));
		aisDataBlock.setBinaryDataSource(new AISBinaryDataSource(aisController, aisDataBlock));
	}
	
	public AISDataBlock getOutputDataBlock() {
		return aisDataBlock;
	}

	boolean findNMEADataBlock() {
		
		PamDataBlock newDataBlock = null;
		newDataBlock = PamController.getInstance().getDataBlock(NMEADataUnit.class, aisControl.aisParameters.nmeaSource);
		if (newDataBlock == null) {
			newDataBlock = PamController.getInstance().getDataBlock(NMEADataUnit.class, 0);
		}
		if (newDataBlock == null)
			return false;
		
		aisControl.nmeaDataBlock = (NMEADataBlock) newDataBlock;
		
		setParentDataBlock(newDataBlock);
		
		return true;
	}
	
	@Override
	public void noteNewSettings() {
		findNMEADataBlock();
	}
	
	@Override
	public void newData(PamObservable o, PamDataUnit pamDataUnit) {

		NMEADataUnit nmeaDataUnit = (NMEADataUnit) pamDataUnit;
		StringBuffer nmeaString = nmeaDataUnit.getCharData();
		String stringId = NMEADataBlock.getSubString(nmeaString, 0);
		if (stringId.equals("!AIVDM")) {
			unpackAIVDM(nmeaDataUnit);
		}
	}
	
	private void unpackAIVDM(NMEADataUnit dataUnit) {
		StringBuffer dataString = dataUnit.getCharData();
//		System.out.println("Unpack AIVDM " + dataString);
		aivdm.dataType = NMEADataBlock.getSubString(dataString, 0);
		String subString;
		AISDataUnit newVDM = new AISDataUnit(dataUnit.getTimeMilliseconds());
//		newVDM.latestString = new String(dataString);
		//ByteBuffer byteBuffer;
		try {
			subString = NMEADataBlock.getSubString(dataString, 1);
			if (subString == null) return;
			newVDM.totalSentences = Integer.valueOf(subString);
			subString = NMEADataBlock.getSubString(dataString, 2);
			if (subString == null) return;
			newVDM.sentenceNumber = Integer.valueOf(subString);
			subString = NMEADataBlock.getSubString(dataString, 3);
			if (subString == null) return;
			if (subString.length() > 0) {
				newVDM.sequentialIdentifier = Integer.valueOf(subString);
			}
			subString = NMEADataBlock.getSubString(dataString, 4);
			if (subString == null) return;
			if (subString.length() > 0) {
				newVDM.aisChannel = new String(subString);
			}
			subString = NMEADataBlock.getSubString(dataString, 5);
			if (subString == null) return;
			newVDM.charData = new String(subString);
			subString = NMEADataBlock.getSubString(dataString, 6);
			if (subString == null) return;
			newVDM.fillBits = Integer.valueOf(subString);
			
		}
		catch (Exception Ex) {
			Ex.printStackTrace();
			return;
		}
		// see where we are in a sequence - if it's the first string, start 
		// a new AIVDM, if it's the last string, process it - most will
		// be both, but not all !. Stirngs may also be a bit out of synch
		// if we start up in the middle of a sequence. so cross check against
		// existing data in multi part data.
		if (newVDM.sentenceNumber == 1) {
			// start a new VDM
			aivdm = newVDM.clone();
		}
		else if (newVDM.sentenceNumber == aivdm.sentenceNumber + 1) {
			// update the VDM
			aivdm.sentenceNumber ++;
			aivdm.charData += newVDM.charData;
			aivdm.fillBits = newVDM.fillBits;
		}
		if (newVDM.sentenceNumber == newVDM.totalSentences) {
			/*
			 * Look in the AISDataBlock and see if it's an existing 
			 * ship (from it's MMSI number) and either update
			 * the existing unit, or create a new one.
			 * This is done by the AISDataBlock
			 * the first thing to do though is to get the MMSI number 
			 * from the string's header.
			 */
			boolean ok = aivdm.decodeMessage();
			aivdm.setTimeMilliseconds(PamCalendar.getTimeInMillis());
			// only add the data to the list if it's a useful type 
			// (i.e. not base station data)
			if (ok) {
				switch(aivdm.messageId){
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 18:
				case 19:
				case 21:
				case 24:
					aisDataBlock.addAISData(aivdm);
					break;
				default:
//					System.out.println(String.format("Bad AIVDM message type %d: %s", aivdm.messageId, aivdm.charData));
				}
			}
//			else {
//				System.out.println(String.format("Can't decode AIVDM message type %d: %s", aivdm.messageId, aivdm.charData));
//			}
			
		}
		
		
	}
	
	
	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearOldData() {
		//super.clearOldData();
	}

}
