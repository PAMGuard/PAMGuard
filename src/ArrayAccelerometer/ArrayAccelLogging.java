package ArrayAccelerometer;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLLogging;
import generalDatabase.SQLTypes;

public class ArrayAccelLogging extends SQLLogging {

	private PamTableItem[] v = new PamTableItem[ArrayAccelParams.NDIMENSIONS];
	private PamTableItem[] g = new PamTableItem[ArrayAccelParams.NDIMENSIONS];
	private PamTableItem pitch, roll;
	private ArrayAccelControl accelControl;
	public ArrayAccelLogging(ArrayAccelControl accelControl, PamDataBlock pamDataBlock) {
		super(pamDataBlock);
		this.accelControl = accelControl;
		PamTableDefinition tableDef = new PamTableDefinition(accelControl.getUnitName(), SQLLogging.UPDATE_POLICY_WRITENEW);
		for (int i = 0; i < ArrayAccelParams.NDIMENSIONS; i++) {
			tableDef.addTableItem(v[i] = new PamTableItem("Volts_"+i, Types.REAL));
			tableDef.addTableItem(g[i] = new PamTableItem("Accel_"+i, Types.REAL));
		}
		tableDef.addTableItem(pitch = new PamTableItem("Pitch", Types.REAL));
		tableDef.addTableItem(roll = new PamTableItem("Roll", Types.REAL));
		setTableDefinition(tableDef);
	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		ArrayAccelDataUnit du = (ArrayAccelDataUnit) pamDataUnit;
		Double[] vs = du.getVoltsRead();
		Double[] gs = du.getAcceleration();
		for (int i = 0; i < ArrayAccelParams.NDIMENSIONS; i++) {
			v[i].setValue(double2Float(vs[i]));
			g[i].setValue(double2Float(gs[i]));
		}
		pitch.setValue(double2Float(du.getPitch()));
		roll.setValue(double2Float(du.getRoll()));

	}

}
