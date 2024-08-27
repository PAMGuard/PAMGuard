package loc3d_Thode;

import java.sql.Types;

import PamDetection.LocContents;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamDetectionLogging;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class TowedArray3DSQLLogging extends PamDetectionLogging {

	TowedArray3DController towedArray3DController;
	
	
	PamTableItem dateItem, range_boat_Item, range_f_Item, range_r_Item, depthItem, azi_boat_Item,azi_f_Item, azi_r_Item, tdd_Item, 
		bearing_f_Item,bearing_r_Item, tds_f_Item, tds_r_Item, za_f_Item, za_r_Item;
	PamTableItem tilt_f_Item,tilt_r_Item,heading_f_Item,heading_r_Item;
	

	public TowedArray3DSQLLogging(TowedArray3DController towedArray3DController, PamDataBlock pamDataBlock) {
		// call the super constructor. 
		super(pamDataBlock, SQLLogging.UPDATE_POLICY_WRITENEW);

		setCanView(true);
		
		// hold a reference to the Controller. 
		this.towedArray3DController = towedArray3DController;
		
		// create the table definition. 
		PamTableDefinition tableDefinition = createTableDefinition();
	}
	
	public PamTableDefinition createTableDefinition() {
		
		
//		PamTableDefinition tableDef = new PamTableDefinition(towedArray3DController.getUnitName(), getUpdatePolicy());
		PamTableDefinition tableDef = super.getTableDefinition();
		tableDef.setUpdatePolicy(SQLLogging.UPDATE_POLICY_WRITENEW);
		
//		PamTableItem tableItem;
		// add table items. 
////		PamTableItem dateItem, durationItem, lowFreqItem, highFreqItem, energyItem;
//		tableDef.addTableItem(tableItem = new PamTableItem("GpsIndex", Types.INTEGER));
//		// this first item will automatically pick up a cross reference from the GPS data block . 
//		tableItem.setCrossReferenceItem("GpsData", "Id");
		
		//dateItem, range_f_Item, range_r_Item, depthItem, azi_f_Item, azi_r_Item, tdd_Item, 
		//bearing_f_Item,bearing_r_Item, tds_f_Item, tds_r_Item, z_f_Item, z_r_Item;
//		tableDef.addTableItem(dateItem = new PamTableItem("SystemDate", Types.TIMESTAMP));
		tableDef.addTableItem(range_f_Item = new PamTableItem("Forward Range m", Types.DOUBLE));
		tableDef.addTableItem(range_r_Item = new PamTableItem("Rear Range m", Types.DOUBLE));
		tableDef.addTableItem(azi_f_Item = new PamTableItem("Forward Azimuth deg", Types.DOUBLE));
		tableDef.addTableItem(azi_r_Item = new PamTableItem("Rear Azimuth deg", Types.DOUBLE));
		tableDef.addTableItem(depthItem = new PamTableItem("Source Depth m", Types.DOUBLE));
		tableDef.addTableItem(range_boat_Item = new PamTableItem("Boat Range m", Types.DOUBLE));
		tableDef.addTableItem(azi_boat_Item = new PamTableItem("Boat Azimuth deg", Types.DOUBLE));
		tableDef.addTableItem(tilt_f_Item = new PamTableItem("Forward tilt deg", Types.DOUBLE));
		tableDef.addTableItem(tilt_r_Item = new PamTableItem("Rear tilt deg", Types.DOUBLE));
		tableDef.addTableItem(heading_f_Item = new PamTableItem("Forward heading deg", Types.DOUBLE));
		tableDef.addTableItem(heading_r_Item = new PamTableItem("Rear heading deg", Types.DOUBLE));
		
		/*
		* 
		*tableDef.addTableItem(tdd_Item = new PamTableItem("tdd (msec)", Types.DOUBLE));
		*tableDef.addTableItem(bearing_f_Item = new PamTableItem("Forward Bearing (deg)", Types.DOUBLE));
		*tableDef.addTableItem(bearing_r_Item = new PamTableItem("Rear Bearing (deg)", Types.DOUBLE));
		*tableDef.addTableItem(tds_f_Item = new PamTableItem("Forward tds (msec)", Types.DOUBLE));
		*tableDef.addTableItem(tds_r_Item = new PamTableItem("Rear tds (msec)", Types.DOUBLE));
		*tableDef.addTableItem(za_f_Item = new PamTableItem("Forward Array Depth (m)", Types.DOUBLE));
		*tableDef.addTableItem(za_r_Item = new PamTableItem("Rear Array Depth (m)", Types.DOUBLE));
		*/
		
		

		setTableDefinition(tableDef);
		
		return tableDef;
	}

//	@Override
//	/**
//	 * This information will get used to automatically create an appropriate database
//	 * table and to generate SQL fetch and insert statements. 
//	 */
//	public PamTableDefinition getTableDefinition() {
//		/*
//		 * return the single instance of tableDefinition that was created in 
//		 * the constructor. This gets called quite often and we don't want 
//		 * to be creating a ne one every time. 
//		 */
//		return tableDefinition;
//	}

	@Override
	/*
	 * This gets called back from the database manager whenever a new dataunit is
	 * added to the datablock. All we have to do is set the data values for each 
	 * field and they will be inserted into the database. Note that we don't need 
	 * to set a value for the GpsIndex field since this will be cross referenced
	 * automatically. 
	 * If formats are incorrect, the SQL write statement is likely to fail !
	 */
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
//		PamTableItem dateItem, durationItem, lowFreqItem, highFreqItem, energyItem;
//		dateItem, range_f_Item, range_r_Item, depthItem, azi_f_Item, azi_r_Item, tdd_Item, 
		//bearing_f_Item,bearing_r_Item, tds_f_Item, tds_r_Item, z_f_Item, z_r_Item;
		
		super.setTableData(sqlTypes, pamDataUnit);
		TowedArray3DDataUnit towedArray3DDataUnit = (TowedArray3DDataUnit) pamDataUnit;
		
		TowedArray3DLocalization ldu = (TowedArray3DLocalization) towedArray3DDataUnit.getLocalisation();
		
//		dateItem.setValue(PamCalendar.getTimeStamp(ldu.getTimeMilliseconds()));
		range_f_Item.setValue( ldu.getRange(0));
		range_r_Item.setValue( ldu.getRange(1));
		range_boat_Item.setValue(ldu.getRange(2));
		azi_f_Item.setValue( ldu.getBearing(0));
		azi_r_Item.setValue( ldu.getBearing(1));
		azi_boat_Item.setValue(ldu.getBearing(2));
		depthItem.setValue(ldu.getHeight(0));
		tilt_f_Item.setValue( ldu.getTilt(0));
		tilt_r_Item.setValue( ldu.getTilt(1));
		heading_f_Item.setValue( ldu.getHeading(0));
		heading_r_Item.setValue( ldu.getHeading(1));
		
		
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		TowedArray3DDataUnit tadu = new TowedArray3DDataUnit(3);
		tadu.setDatabaseIndex(databaseIndex);
		tadu.setTimeMilliseconds(timeMilliseconds);
		TowedArray3DLocalization ldu = (TowedArray3DLocalization) tadu.getLocalisation();
		fillDataUnit(sqlTypes, tadu);
		double v;
		
		tadu.setRanges(v = (Double) range_f_Item.getValue(),0);
		tadu.setRanges((Double) range_r_Item.getValue(), 1);
		tadu.setRanges((Double) range_boat_Item.getValue(), 2);
		if (!Double.isNaN(v)) {
			tadu.setHasRanges(true);
			ldu.addLocContents(LocContents.HAS_RANGE);
		}
		tadu.setAngle(v = (Double) azi_f_Item.getValue(),0);
		tadu.setAngle((Double) azi_r_Item.getValue(),1);
		tadu.setAngle((Double) azi_boat_Item.getValue(),2);
		if (!Double.isNaN(v)) {
			tadu.setHasAngles(true);
			ldu.addLocContents(LocContents.HAS_BEARING);
		}
		tadu.setDepth(v = (Double) depthItem.getValue());
//		System.out.println("DEpth = " + v);
		if (!Double.isNaN(v)) {
			tadu.setHasDepth(true);
			ldu.addLocContents(LocContents.HAS_DEPTH);
		}
		tadu.setTilts(v = (Double) tilt_f_Item.getValue(), 0);
		tadu.setTilts(v = (Double) tilt_r_Item.getValue(), 1);
		tadu.setHeadings(v = (Double) heading_f_Item.getValue(), 0);
		tadu.setHeadings(v = (Double) heading_r_Item.getValue(), 1);
//		tadu.set(!Double.isNaN(v));
		

		getPamDataBlock().addPamData(tadu);
		
		return tadu;
	}

}
