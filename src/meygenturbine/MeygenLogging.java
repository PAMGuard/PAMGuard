package meygenturbine;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class MeygenLogging extends SQLLogging {

	private PamTableItem bearing, magnitude, depth;
	
	
	public MeygenLogging(PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		PamTableDefinition tableDef = new PamTableDefinition(pamDataBlock.getDataName());
		tableDef.addTableItem(bearing = new PamTableItem("Bearing", Types.DOUBLE));
		tableDef.addTableItem(magnitude = new PamTableItem("Magnitude", Types.DOUBLE));
		tableDef.addTableItem(depth = new PamTableItem("Depth", Types.DOUBLE));
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see generalDatabase.SQLLogging#createDataUnit(generalDatabase.SQLTypes, long, int)
	 */
	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		double b = bearing.getDoubleValue();
		double m = magnitude.getDoubleValue();
		double d = depth.getDoubleValue();
		return new MeygenDataUnit(timeMilliseconds, b, m, d);		
	}

}
