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

import java.util.ArrayList;

/**
 * This class is used as temporary storage for subtable data of PamDataBlocks being loaded from a database.  The class has
 * two separate functions.  First, it will hold information about each subtable item including database index and UID of
 * the parent object, as well as PamDataBlock name and UID of the subtable data item.  Secondly, it also serves to hold
 * the data from all the individual rows of a subtable together in a single ArrayList.  Thus, if a subtable has 10 rows there
 * will be 10 PamSubtableData items created (one for each row, containing info about the row item) and an 11th PamSubtableData
 * object to group all of the previous 10 objects into a single ArrayList.<p>
 * 
 * @author SCANS
 *
 */
public class PamSubtableData {

	/**
	 * The database index of the parent PamDataUnit
	 */
	private int parentID;
	
	/**
	 * The UID of the parent PamDataUnit
	 */
	private long parentUID;
	
	/**
	 * The long name of the subdetection
	 */
	private String longName;
	
	/**
	 * The binary file where the PamDataUnit can be found
	 */
	private String binaryFilename;
	
	/**
	 * The UID of the subdetection
	 */
	private long childUID;
	
	/**
	 * Millisecond time of the child
	 */
	private long childUTC;
	

	/**
	 * List of all subtable items, packaged together in a nice little bundle
	 */
	private ArrayList<PamSubtableData> subtableItems;
	
	/**
	 * Database index of the subdetection (this is the database index in the subdetection table, not the database index
	 * from a detection table (e.g. if the subdetection is a click this value would be the index from the Offline_Clicks
	 * subdetection table, and not the index from the Click_Detector_Clicks table)
	 */
	private long dbIndex;
	
	/**
	 * 
	 */
	public PamSubtableData() {
	}

	public int getParentID() {
		return parentID;
	}

	public void setParentID(int parentID) {
		this.parentID = parentID;
	}

	public long getParentUID() {
		return parentUID;
	}

	public void setParentUID(long parentUID) {
		this.parentUID = parentUID;
	}

	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	public String getBinaryFilename() {
		return binaryFilename;
	}

	public void setBinaryFilename(String binaryFilename) {
		this.binaryFilename = binaryFilename;
	}

	public long getChildUID() {
		return childUID;
	}

	public void setChildUID(long unitUID) {
		this.childUID = unitUID;
	}

	public ArrayList<PamSubtableData> getSubtableItems() {
		return subtableItems;
	}

	public void addSubtableItemToList(PamSubtableData subtableItem) {
		this.subtableItems.add(subtableItem);
	}

	public long getDbIndex() {
		return dbIndex;
	}

	public void setDbIndex(long dbIndex) {
		this.dbIndex = dbIndex;
	}

	/**
	 * @return the childUTC
	 */
	public long getChildUTC() {
		return childUTC;
	}

	/**
	 * @param childUTC the childUTC to set
	 */
	public void setChildUTC(long childUTC) {
		this.childUTC = childUTC;
	}

	
}
