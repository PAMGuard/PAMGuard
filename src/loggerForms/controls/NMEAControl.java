package loggerForms.controls;

import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import NMEA.NMEADataBlock;
import NMEA.NMEADataUnit;
import PamController.PamController;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataBlock;
import loggerForms.LoggerForm;
import loggerForms.controlDescriptions.ControlDescription;

/**
 * Intermediate class for all NMEA data types. 
 * Contains functionality to find particular NMEA strings, correct 
 * string. Then extracts desired section of that string
 * searching multiple NMEA inputs if necessary to locate the 
 * @author Doug Gillespie
 *
 */
public abstract class NMEAControl extends SimpleControl {

	private NMEADataBlock nmeaDataBlock;

	public NMEAControl(ControlDescription controlDescription,
			LoggerForm loggerForm) {
		super(controlDescription, loggerForm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int autoUpdate() {
		return updateNMEAData();
	}

	/**
	 * Tries to find the appropriate NMEA string by searching 
	 * multiple data blocks if necessary. IF the previous call was 
	 * Successful, then it will just go straight to that same data
	 * block, otherwise, it will search all available datablocks. 
	 * @return
	 */
	public int updateNMEAData() {
		if (nmeaDataBlock == null) {
			nmeaDataBlock = findNMEADataBlock();
			if (nmeaDataBlock == null) {
				clear();
				return AUTO_UPDATE_FAIL;
			}
		}
		NMEADataUnit dataUnit = nmeaDataBlock.findNMEADataUnit(controlDescription.getNmeaString());
		if (dataUnit == null) {
			clear();
			return AUTO_UPDATE_FAIL;
		}
		
		return fillNMEAControlData(dataUnit);

	}
	
	public int fillNMEAControlData(NMEADataUnit dataUnit) {
		Integer nmeaPos = controlDescription.getNmeaPosition();
		if (nmeaPos == null) {
			clear();
			return AUTO_UPDATE_FAIL;
		}
		String subStr = NMEADataBlock.getSubString(dataUnit.getCharData(), nmeaPos);
		if (subStr == null) {
			clear();
			return AUTO_UPDATE_FAIL;
		}

		textField.setText(subStr);
		try {
			textField.commitEdit();
		} catch (ParseException e) {
			e.printStackTrace();
			return AUTO_UPDATE_FAIL;
		}
		//		textField.setValue(subStr);

		return AUTO_UPDATE_SUCCESS; 
	}

	/**
	 * Used when NMEA data is being updated in response to the arrival of 
	 * a new NMEA string (i.e. on forms which save all NMEA data from a 
	 * single string). An actual string is passed in, but there is just
	 * a chance that it will be the wrong type in which case the default 
	 * is used. 
	 * @param nmeaData
	 */
	public int updateNMEAData(NMEADataUnit nmeaData) {
		// TODO Auto-generated method stub
		StringBuffer nmeaString = nmeaData.getCharData();
		String stringId = nmeaData.getStringId();
		if (controlDescription.getNmeaString().equalsIgnoreCase(stringId) == false) {
			return updateNMEAData(); // use the one where itfinds its own string
		}
		return fillNMEAControlData(nmeaData);
		
	}

//	/**
//	 * Finds an NMEA data block which contains the string we're looking for
//	 * @return
//	 */
//	private NMEADataBlock findNMEADataBlock() {
//		ArrayList<PamDataBlock> nmeaDataBlocks = PamController.getInstance().getDataBlocks(NMEADataUnit.class, true);
//		NMEADataBlock aDataBlock;
//		for (int i = 0; i < nmeaDataBlocks.size(); i++) {
//			aDataBlock = (NMEADataBlock) nmeaDataBlocks.get(i);
//			if (aDataBlock.findNMEADataUnit(controlDescription.getNmeaString()) != null) {
//				return aDataBlock;
//			}
//		}
//		return null;
//	}
}
