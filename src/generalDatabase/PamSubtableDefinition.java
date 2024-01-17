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



package generalDatabase;

import java.sql.Types;

/**
 * @author SCANS
 *
 */
public class PamSubtableDefinition extends PamTableDefinition {

	public static final String PARENTIDNAME = "parentID";
	public static final String PARENTUIDNAME = "parentUID";
	public static final String LONGDATANAME = "LongDataName";
	public static final String BINARYFILE = "BinaryFile";
	public static final int DATANAME_LENGTH = 80;
	public static final int BINARY_FILE_NAME_LENGTH = 80;
	
	private PamTableItem parentID, parentUID, longName, binaryFilename;

	/**
	 * @param tableName
	 * @param updatePolicy
	 */
	public PamSubtableDefinition(String tableName) {
		super(tableName, SQLLogging.UPDATE_POLICY_OVERWRITE);
		addTableItem(parentID = new PamTableItem(PARENTIDNAME, Types.INTEGER));
		addTableItem(parentUID = new PamTableItem(PARENTUIDNAME, Types.BIGINT));
		addTableItem(longName = new PamTableItem(LONGDATANAME, Types.CHAR, DATANAME_LENGTH));
		addTableItem(binaryFilename = new PamTableItem(BINARYFILE, Types.CHAR, BINARY_FILE_NAME_LENGTH));
		setUseCheatIndexing(false);
	}

	public PamTableItem getParentID() {
		return parentID;
	}

	public PamTableItem getParentUID() {
		return parentUID;
	}

	public PamTableItem getLongName() {
		return longName;
	}

	public PamTableItem getBinaryfile() {
		return binaryFilename;
	}

	

}
