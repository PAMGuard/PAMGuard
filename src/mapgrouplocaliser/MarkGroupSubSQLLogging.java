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



package mapgrouplocaliser;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamSubtableDefinition;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

/**
 * @author SCANS
 *
 */
public class MarkGroupSubSQLLogging extends SQLLogging {

	MarkGroupSQLLogging parentLogger;
	
	/**
	 * @param pamDataBlock
	 */
	public MarkGroupSubSQLLogging(PamDataBlock pamDataBlock, MarkGroupSQLLogging parentLogger) {
		super(pamDataBlock);
		this.parentLogger = parentLogger;
		setTableDefinition(new PamSubtableDefinition(parentLogger.getMapGroupLocaliserControl().getUnitName()+"_Children"));
	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#setTableData(generalDatabase.SQLTypes, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		// nothing other than parentID and parentUID, already written in SQLLogging
	}	
}
