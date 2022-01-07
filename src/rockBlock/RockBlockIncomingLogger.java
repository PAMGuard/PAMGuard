/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
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



package rockBlock;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

/**
 * @author mo55
 *
 */
public class RockBlockIncomingLogger extends SQLLogging {

	PamTableItem message, read;
	
	private PamTableDefinition tableDefinition;

	public static final int STRING_LENGTH = 350;

	/**
	 * @param pamDataBlock
	 */
	protected RockBlockIncomingLogger(PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		setCanView(true);
		
		tableDefinition = new PamTableDefinition(pamDataBlock.getDataName(), UPDATE_POLICY_OVERWRITE);
		tableDefinition.addTableItem(message  = new PamTableItem("Message", Types.CHAR, STRING_LENGTH));
		tableDefinition.addTableItem(read = new PamTableItem("Sent", Types.BOOLEAN));
        setTableDefinition(tableDefinition);
	}


	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#setTableData(generalDatabase.SQLTypes, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		RockBlockIncomingMessage theMessage = (RockBlockIncomingMessage) pamDataUnit;
		message.setValue(theMessage.getMessage());
		read.setValue(theMessage.isMessageRead());
	}


	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		RockBlockIncomingMessage theMessage = new RockBlockIncomingMessage(timeMilliseconds, message.getStringValue());
		theMessage.setDatabaseIndex(databaseIndex);
		theMessage.setMessageRead(read.getBooleanValue());
		return theMessage;
	}
	
	

}
