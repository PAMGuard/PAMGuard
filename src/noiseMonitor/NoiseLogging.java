package noiseMonitor;

import java.sql.Types;

import PamController.PamController;
import PamUtils.PamUtils;
import PamguardMVC.PamDataUnit;
import generalDatabase.DBControl;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class NoiseLogging extends SQLLogging {

//	private NoiseProcess noiseProcess;
	
	private NoiseDataBlock noiseDataBlock;
	
	private PamTableDefinition tableDef;
	
	private PamTableItem channelNumber;
	
	private PamTableItem[] bandItems;
	
	
	public NoiseLogging(NoiseDataBlock noiseDataBlock) {
		super(noiseDataBlock);
		this.noiseDataBlock = noiseDataBlock;
//		this.noiseProcess = noiseProcess;
		createTableDef();
	}

//	@Override
//	public PamTableDefinition getTableDefinition() {
//		return tableDef;
//	}
	
	/**
	 * Called from the noise monitor process to recreate the table
	 * definition based on the noise measurements created by 
	 * the user and then check the table columns in the database
	 * @return true if all Ok. False if no database or an error creating a column.
	 */
	public boolean createAndCheckTable() {
		createTableDef();

		DBControl dbControl = (DBControl) PamController.getInstance().findControlledUnit(DBControl.getDbUnitType());
		if (dbControl == null) {
			return false;
		}
		
		return dbControl.getDbProcess().checkTable(tableDef);
		
	}
	
	/**
	 * Function to create the noise table definition will need to go back
	 * to the main process and controller to work out which columns are
	 * required
	 */
	private void createTableDef() {
		tableDef = new PamTableDefinition(getPamDataBlock().getDataName(), SQLLogging.UPDATE_POLICY_WRITENEW);
		tableDef.setUseCheatIndexing(true);
//		String[] dbColNames = noiseProcess.getDBColNames();
		String[] dbColNames = noiseDataBlock.getColumnNames();
		/**
		 * This stuff is really weird - the column names ARE the same, but this is saying 
		 * that are different. Something to do with unicode and whitespace ? 
		 */
//		for (int i = 0; i < dbColNames.length; i++) {
//			System.out.println(String.format("\"%s\" : \"%s\"", dbColNames[i], dbColNames[i]));
//			if (dbColNames[i].equalsIgnoreCase(dbColNames2[i]) == false) {
//				System.out.println(String.format("Error in new column name : (%d,%d)", dbColNames[i].length(), dbColNames2[i].length()));
//				for (int c = 0; c < Math.min(dbColNames[i].length(), dbColNames2[i].length()); c++) {
//					char c1 = dbColNames[c].charAt(c);
//					char c2 = dbColNames2[c].charAt(c);
////					if (c1 != c2) {
//						System.out.println(String.format("char at pos %d was %c is %c", c, c1, c2));
////					}
//				}
//			}
//		}
//		if (dbColNames == null) {
//			return;
//		}
		
		tableDef.addTableItem(channelNumber = new PamTableItem("Channel", Types.INTEGER));

		if (dbColNames != null) {
			bandItems = new PamTableItem[dbColNames.length];
			for (int i = 0; i < dbColNames.length; i++) {
				tableDef.addTableItem(bandItems[i] = new PamTableItem(dbColNames[i], Types.DOUBLE));
			}
		}

		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		NoiseDataUnit ndu = (NoiseDataUnit) pamDataUnit;
		int chan = PamUtils.getSingleChannel(ndu.getChannelBitmap());
		channelNumber.setValue(chan);
		int iBandItem = 0;
		/*
		 * loop over all band items. 
		 * output loop over bands, inner loop over the four measurement types. 
		 */
		double[][] bandData = ndu.getNoiseBandData();
		int nBands = bandData.length;
		int nMeasures = bandData[0].length;
		for (int iB = 0; iB < nBands; iB++) {
			for (int i = 0; i < nMeasures; i++) {
				bandItems[iBandItem++].setValue(bandData[iB][i]);
			}
		}
	}
	
	private long lastTime;

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		int chan = channelNumber.getIntegerValue();
		int nBands = noiseDataBlock.getBandLoEdges().length;
		int nMeasures = noiseDataBlock.getUsedMeasureNames().length;
		if (nMeasures * nBands != bandItems.length) {
			return null;
		}
		double[][] bandData = new double[nBands][nMeasures];
		for (int iBand = 0, iCol = 0; iBand < nBands; iBand++) {
			for (int iMeasure = 0; iMeasure < nMeasures; iMeasure++, iCol++) {
				bandData[iBand][iMeasure] = bandItems[iCol].getDoubleValue();
			}
		}

		NoiseDataUnit noiseDataUnit = new NoiseDataUnit(timeMilliseconds, 1<<chan, 0, 0);
		noiseDataUnit.setNoiseBandData(bandData);
		return noiseDataUnit;
	}

}
