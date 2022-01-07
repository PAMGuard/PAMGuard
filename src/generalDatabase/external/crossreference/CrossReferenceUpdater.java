package generalDatabase.external.crossreference;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingWorker;

import PamController.PamController;
import PamguardMVC.PamDataBlock;
import generalDatabase.DBControl;
import generalDatabase.PamConnection;
import generalDatabase.SQLTypes;
import generalDatabase.external.CopyManager;

/**
 * Functions for cross reference updating. 
 * Got quite complicated, so got it's own class. 
 * @author Doug
 *
 */
public class CrossReferenceUpdater {

	private DBControl database;
	private List<CrossReference> xRefs;
	
	public CrossReferenceUpdater(DBControl destDatabase) {
		this.database = destDatabase;
	}
	
	/**
	 *  find and loop through multiple tables 
	 * @param crossReferenceMonitor
	 */
	public void run(CrossReferenceStatusMonitor crossReferenceMonitor) {
		xRefs = findCrossReferences();
		int i = 0;
		for (CrossReference x:xRefs) {
			updateCrossRefs(i++, x, crossReferenceMonitor);
		}
		crossReferenceMonitor.update(new CrossReferenceStatus(1, 1, 1, 1, "Cross reference update complete"));
		crossReferenceMonitor.update(null);
	}
	
	/**
	 * Update cross references for a single pair of database tables.
	 * @param i
	 * @param x
	 * @param crossReferenceMonitor 
	 */
	private void updateCrossRefs(int i, CrossReference x, CrossReferenceStatusMonitor crossReferenceMonitor) {
		
		String msg = String.format("Analysing tables %s and %s", x.getTableName1(), x.getTableName2());
		crossReferenceMonitor.update(new CrossReferenceStatus(xRefs.size(), i, 0, 0, msg));
		ArrayList<CrossReferenceSet> crSet = gatherCrossRefData(x);
		
//		System.out.printf("Created %d cross reference sets\n", crSet.size());
		if (crSet != null && crSet.size() > 0) {
			updateReferences(i, x, crSet, crossReferenceMonitor);
		}
	}
	
	/**
	 * Have a list of lists of indexes of data in the output table which 
	 * need updating. Get on with it and update records in groups or one at a time. 
	 * @param iTable table index
	 * @param x cross reference data
	 * @param crossReferenceMonitor 
	 * @param crSet list of lists of items to update. 
	 */
	private void updateReferences(int iTable, CrossReference x,
			ArrayList<CrossReferenceSet> crossRefSets, CrossReferenceStatusMonitor crossReferenceMonitor) {
		// count the total number of records. 
		int nRec = 0;
		int nDone = 0;
		for (CrossReferenceSet crSet:crossRefSets) {
			nRec += crSet.getSize();
		}
		//now work through the set of data. 
		String msg = String.format("Updating table %s column %s", x.getTableName2(), x.getColumnName2());
		crossReferenceMonitor.update(new CrossReferenceStatus(xRefs.size(), iTable, nRec, nDone, msg));
		for (CrossReferenceSet crSet:crossRefSets) {
			updateReferences(iTable, x, crSet);
			nDone += crSet.getSize();
			crossReferenceMonitor.update(new CrossReferenceStatus(xRefs.size(), iTable, nRec, nDone, msg));
		}
	}

	/**
	 * Update references for a single changed index in a single table.
	 * @param iTable Table Index
	 * @param x Cross REference Data
	 * @param crSet Cross Reference Index information. 
	 */
	private boolean updateReferences(int iTable, CrossReference x,
			CrossReferenceSet crSet) {
		if (crSet.oldReference == crSet.newReference) {
			return true; // nothing to do if the reference hasn't changed. 
		}
		try {
			Statement stmt = database.getConnection().getConnection().createStatement();
			boolean result;
			for (Object updateRef:crSet.xItems) {
				String qStr = String.format("UPDATE %s SET %s=%s WHERE Id=%s", x.getTableName2(), 
						x.getColumnName2(), crSet.newReference, updateRef.toString());
//				System.out.println(qStr);
				result = stmt.execute(qStr);
			}
			database.getConnection().getConnection().commit();
		}
		catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private ArrayList<CrossReferenceSet> gatherCrossRefData(CrossReference x) {
		int nRec = CopyManager.countRecords(database, x.getTableName1());
		ArrayList<CrossReferenceSet> crossReferenceSets = new ArrayList<>();
		// now a similar query to the counting one, but with the IdData too. 
		/**
		 * got a problem though - say we had to flip two id's, 1 and 2. How can we do that, if we 
		 * relabel all 1's as 2's, then all 2's as 1's, we'll only have ones left !
		 * Could adopt a different strategy and make a massive memory map of ALL database Ids and then
		 * update them one at a time. Since all data have been copied into new tables, it should be practical 
		 * to allocate a single large lookup table which will contain all id's (or make a hash table to keep it
		 * more general). At end of day, even a million records won't require that much memory for the lookup 
		 * table. 
		 */
		SQLTypes sqlTypes = database.getConnection().getSqlTypes();
		String qStr = String.format("SELECT %s, %s FROM %s", sqlTypes.formatColumnName(x.getColumnName1()), 
				sqlTypes.formatColumnName(x.getOldColName1()),  x.getTableName1());
		PamConnection pCon = database.getConnection();
		Connection con = pCon.getConnection();
		try {
			Statement stmt = con.createStatement();
			ResultSet result = stmt.executeQuery(qStr);
			while (result.next()) {
				Object newId = result.getObject(1);
				Object oldId = result.getObject(2);
				CrossReferenceSet xRefSet = new CrossReferenceSet(oldId, newId);
				// now run a second query to get the indices of the affected records in second table using a second query. 
//				String udString = "UPDATE " + x.getTableName2() + " SET " + x.getColumnName2() + "=" + newId +
//						" WHERE "  + x.getColumnName2() + "=" + oldId;
				String str2 = String.format("SELECT Id FROM %s WHERE %s=%s", x.getTableName2(), 
						sqlTypes.formatColumnName(x.getColumnName2()), oldId.toString());
				Statement stmt2 = con.createStatement();
				ResultSet result2 = stmt2.executeQuery(str2);
				while (result2.next()) {
					xRefSet.addItem(result2.getObject(1));
				}
				result2.close();
				stmt2.close();
				crossReferenceSets.add(xRefSet);
				nRec++;
			}
			result.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return crossReferenceSets;
	}

	public void runInBackground(CrossReferenceStatusMonitor crossReferenceMonitor) {
			CrossReferenceWorker crw = new CrossReferenceWorker(crossReferenceMonitor);
			crw.execute();
		}

	private class CrossReferenceWorker extends SwingWorker<Integer, CrossReferenceStatus> implements CrossReferenceStatusMonitor{
		
		private List<CrossReference> xRefs;
		CrossReferenceStatusMonitor crossReferenceMonitor;
		
		/**
		 * @param destDatabase 
		 * @param xRefs2
		 * @param crossReferenceStatusMonitor
		 */
		public CrossReferenceWorker(CrossReferenceStatusMonitor crossReferenceMonitor) {
			super();
			this.crossReferenceMonitor = crossReferenceMonitor;
		}

		@Override
		protected Integer doInBackground() {
			CrossReferenceUpdater.this.run(this);
			return null;
		}

		@Override
		public void update(CrossReferenceStatus crossReferenceStatus) {
			publish(crossReferenceStatus);
		}

		/* (non-Javadoc)
		 * @see javax.swing.SwingWorker#process(java.util.List)
		 */
		@Override
		protected void process(List<CrossReferenceStatus> chunks) {
			for (CrossReferenceStatus crs:chunks) {
				crossReferenceMonitor.update(crs);
			}
		}
		
	}
	
	private List<CrossReference> findCrossReferences() {
		ArrayList<CrossReference> xRefs = new ArrayList<>();
		List<PamDataBlock> dataBlocks = PamController.getInstance().getDataBlocks();
		for (PamDataBlock aDataBlock:dataBlocks) {
			CrossReference xr = aDataBlock.getCrossReferenceInformation();
			if (xr != null) {
				xRefs.add(xr);
			}
		}
		return xRefs;
	}
}
