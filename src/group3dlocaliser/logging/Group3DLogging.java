package group3dlocaliser.logging;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;

import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamUtils.LatLong;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamDetectionLogging;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLLoggingAddon;
import generalDatabase.SQLTypes;
import generalDatabase.SuperDetLogging;
import group3dlocaliser.Group3DDataBlock;
import group3dlocaliser.Group3DDataUnit;
import group3dlocaliser.Group3DLocaliserControl;

public class Group3DLogging extends SuperDetLogging {

	private Group3DLocaliserControl group3DControl;
	
	private PamTableItem masterChild, otherChilds;
//	superId;

	private SQLLoggingAddon currentSQLAddon;
	
	private PamTableItem locX, locY, locZ;

	public Group3DLogging(Group3DLocaliserControl group3dLocaliserControl, Group3DDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.group3DControl =group3dLocaliserControl;
		setTableDefinition(createBaseTableDef());
	}
	
	private PamTableDefinition createBaseTableDef() {
		PamTableDefinition tableDef = new PamTableDefinition(group3DControl.getUnitName(), UPDATE_POLICY_WRITENEW);
		locX = new PamTableItem("locX", Types.DOUBLE);
		locY = new PamTableItem("locY", Types.DOUBLE);
		locZ = new PamTableItem("locZ", Types.DOUBLE);
//		superId = new PamTableItem("SuperId", Types.INTEGER);
		/*
		 * Make a list of child UID's. UID numbers might be over 12 digits (yes, really, current UID at Meygen project
		 * is 11 digits and rising). May have to store up to 32, so a comma separated list would be 13*32 = 416 characters
		 * Store just the first, and then relative values which will likely to be within +/-10 of the first. This will 
		 * make the whole thing a lot smaller = 12 + 31 commas + 31+2 = 105 characters.
		 */
//		tableDef.addTableItem(superId);
		tableDef.addTableItem(masterChild = new PamTableItem("Parent UID", Types.BIGINT));
		tableDef.addTableItem(otherChilds = new PamTableItem("Child UIDs", Types.CHAR, 128));
		tableDef.addTableItem(locX);
		tableDef.addTableItem(locY);
		tableDef.addTableItem(locZ);
		return tableDef;
	}

	public void setLocalisationAddon(SQLLoggingAddon sqlLoggingAddon) {
		if (currentSQLAddon != null) {
			removeAddOn(currentSQLAddon);
		}
		// restore the base table at this point ...
		setTableDefinition(createBaseTableDef());
		if (sqlLoggingAddon != null) {
			addAddOn(sqlLoggingAddon);
		}
		this.currentSQLAddon = sqlLoggingAddon;
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		Group3DDataUnit g3dDataUnit = (Group3DDataUnit) pamDataUnit;
		int nChild = g3dDataUnit.getSubDetectionsCount();
		if (nChild > 0) {
			long[] uids = new long[nChild];
			for (int i = 0; i < nChild; i++) {
				uids[i] = g3dDataUnit.getSubDetection(i).getUID();
			}
			Arrays.sort(uids);
			masterChild.setValue(uids[0]);
			if (uids.length > 1) {
				String s = String.format("%d",uids[1]);
				for (int i = 2; i < nChild; i++) {
					s += String.format(",%d", uids[i]);
				}
				otherChilds.setValue(s);
			}
			else {
				otherChilds.setValue(null);
			}
		}
		else {
			masterChild.setValue(null);
			otherChilds.setValue(null);
		}
		/**
		 * Now work out x,y,z coordinates relative to the reference point
		 */
		AbstractLocalisation loc = pamDataUnit.getLocalisation();
//		superId.setValue(g3dDataUnit.getSuperDatabaseId());
		locX.setValue(null);
		locY.setValue(null);
		locZ.setValue(null);
		if (loc != null) {
			if (loc.hasLocContent(LocContents.HAS_LATLONG)) {
				LatLong latLong = loc.getLatLong(0);
				LatLong refLatLong = pamDataUnit.getOriginLatLong(false);
				if (latLong != null) {
					locX.setValue(refLatLong.distanceToMetresX(latLong));
					locY.setValue(refLatLong.distanceToMetresY(latLong));
					locZ.setValue(latLong.getHeight());
				}
			}
		}
	}
	
	/**
	 * Convert a UID list string back into a list of long values. 
	 * @param uidList UID list string (relative values)
	 * @return List of UId's. 
	 */
	private long[] getUIDList(String uidList) {
		if (uidList == null) {
			return null;
		}
		String[] splitStr = uidList.split(",");
		int n = splitStr.length;
		if (n == 0) {
			return null;
		}
		long[] l = new long[n];
		try {
			l[0] = Long.valueOf(splitStr[0]);
			for (int i = 1; i < n; i++) {
				l[i] = Long.valueOf(splitStr[i]) + l[i-1];
			}
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
		return l;
	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#createDataUnit(generalDatabase.SQLTypes, long, int)
	 */
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		Group3DDataUnit group3dDataUnit = new Group3DDataUnit(timeMilliseconds);
		Long masterUID = masterChild.getLongObject();
		String otherUID = otherChilds.getDeblankedStringValue();
		if (masterUID != null && otherUID != null) {
			String parents = String.format("%d,%s", masterUID, otherUID);
			group3dDataUnit.setParentUIDList(parents);
		}
		return group3dDataUnit;
	}

}
