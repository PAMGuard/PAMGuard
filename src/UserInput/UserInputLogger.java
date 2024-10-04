/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package UserInput;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamConnection;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

/**
 * 
 * @author David J McLaren
 *
 */

public class UserInputLogger extends SQLLogging {
	
	PamTableDefinition tableDefinition;
	
//	int dateColumn, commentColumn;
//	PamTableItem dateItem, 
	PamTableItem commentItem;

	private static final int maxStringLength = 255;
	
	public UserInputLogger(PamDataBlock pamDataBlock) {
		
		super(pamDataBlock);

		setCanView(true);

		PamTableItem tableItem;
		tableDefinition = new PamTableDefinition("UserInput", UPDATE_POLICY_WRITENEW);
//		tableDefinition.addTableItem(tableItem = new PamTableItem("GpsIndex", Types.INTEGER));
//		tableItem.setCrossReferenceItem("GpsData", "Id");
//		tableDefinition.addTableItem(dateItem = new PamTableItem("SystemDate", Types.TIMESTAMP));
//		tableDefinition.addTableItem(commentItem = new PamTableItem("Comment", Types.VARCHAR, 
//				255));
		tableDefinition.addTableItem(commentItem = new PamTableItem("Comment", Types.CHAR, 
				maxStringLength));
//		tableDefinition.addTableItem(commentItem = new PamTableItem("Comment", Types.VARCHAR, 
//				UserInputController.maxCommentLength));

		setTableDefinition(tableDefinition);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {

		String comment = ((UserInputDataUnit) pamDataUnit).getUserString();
		
//		dateItem.setValue(PamCalendar.getTimeStamp(pamDataUnit.getTimeMilliseconds()));
		commentItem.setValue(comment);
		
	}

//	@Override
//	public PamTableDefinition getTableDefinition() {
//		return tableDefinition;
//	}

	@Override
	public synchronized boolean logData(PamConnection con, PamDataUnit dataUnit) {
		/**
		 * Override this since the string may have to be broken up into little bits in order
		 * to fit into 255 character chunks, becoming many smaller strings in the process.
		 * We'll do this by making new PAmDataUnits with shorter strings and pass them one by
		 * one back to the method on the super class.
		 */
		UserInputDataUnit userUnit = (UserInputDataUnit) dataUnit;
		UserInputDataUnit shortUnit = new UserInputDataUnit(dataUnit.getTimeMilliseconds(), "");
		String logString = new String(userUnit.getUserString());
		String subString, maxSubString;
		int lastSpace;
		while (logString != null) {
			if (logString.length() < maxStringLength) {
				shortUnit.setUserString(logString);
				return super.logData(con, shortUnit);
			}
			maxSubString = logString.substring(0, maxStringLength);
			lastSpace = Math.max(maxSubString.lastIndexOf(' '), maxSubString.lastIndexOf(','));
			lastSpace = Math.max(lastSpace, maxSubString.lastIndexOf('\n'));
			lastSpace = Math.max(lastSpace, maxSubString.lastIndexOf('\r'));
			if (lastSpace <= 0) {
				subString = maxSubString;
				lastSpace = maxStringLength;
			}
			else {
				subString = maxSubString.substring(0, lastSpace);
			}
			shortUnit.setUserString(subString);
			super.logData(con, shortUnit);
			logString = logString.substring(lastSpace+1);
		}
		
		return super.logData(con, dataUnit);
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {

		// first get the time of the data unit
//		Timestamp ts = (Timestamp) tableDefinition.getTimeStampItem().getValue();
//		long t = PamCalendar.millisFromTimeStamp(ts);
		String txt = commentItem.getDeblankedStringValue();
		// note that the data may well have been split between multiple database entries
		// if the string was too long, so try to find an existing unit
		// with exactly the same time. 
		UserInputDataUnit dataUnit = (UserInputDataUnit) getPamDataBlock().findDataUnit(timeMilliseconds, 0);
		if (dataUnit != null && dataUnit.getDatabaseIndex() != databaseIndex) {
			dataUnit.setDatabaseIndex(databaseIndex);
			dataUnit.setUserString(dataUnit.getUserString() + " " + txt);
			// don't call this next line, it causes the unit to get relogged. 
//			getPamDataBlock().updatePamData(dataUnit, timeMilliseconds);
			dataUnit.clearUpdateCount();
		}
		else {
			dataUnit = new UserInputDataUnit(timeMilliseconds, txt);
			dataUnit.setDatabaseIndex(databaseIndex);
			getPamDataBlock().addPamData(dataUnit);
		}
		
		return dataUnit;
	}


}
