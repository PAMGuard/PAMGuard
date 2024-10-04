package RightWhaleEdgeDetector;

import java.sql.Types;

import PamDetection.AbstractLocalisation;
import PamDetection.AcousticSQLLogging;
import PamUtils.PamUtils;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import generalDatabase.EmptyTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;

public class RWESQLLogging extends AcousticSQLLogging {

	private RWEControl rweControl;

	private PamTableItem score, signal, noise;
	private PamTableItem[] angles, angleErrors;
	private int maxAngles = 2;

	protected RWESQLLogging(RWEControl rweControl, PamDataBlock pamDataBlock) {
		super(pamDataBlock, rweControl.getUnitName());
		this.rweControl = rweControl;

		angles = new PamTableItem[maxAngles];
		angleErrors = new PamTableItem[maxAngles];
		
		EmptyTableDefinition tableDef = getTableDefinition();
		tableDef.addTableItem(score = new PamTableItem("score", Types.INTEGER)); 
		tableDef.addTableItem(signal = new PamTableItem("signal", Types.DOUBLE)); 
		tableDef.addTableItem(noise = new PamTableItem("noise", Types.DOUBLE));
		for (int i = 0; i < maxAngles; i++) {
			tableDef.addTableItem(angles[i] = new PamTableItem("Angle_"+i, Types.REAL));
			tableDef.addTableItem(angleErrors[i] = new PamTableItem("AngleError_"+i, Types.REAL));
		}

	}

	@Override
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		super.setTableData(sqlTypes, pamDataUnit);
		RWEDataUnit rweDataUnit = (RWEDataUnit) pamDataUnit;
		score.setValue(rweDataUnit.rweSound.soundType);
		signal.setValue(rweDataUnit.rweSound.signal);
		noise.setValue(rweDataUnit.rweSound.noise);
		AbstractLocalisation loc = rweDataUnit.getLocalisation();
		if (loc != null) {
			double[] angs = loc.getAngles();
			double[] errors = loc.getAngleErrors();
			int nAngs = Math.min(angs == null ? 0 :angs.length, maxAngles);
			int nErrs = Math.min(errors == null ? 0 : errors.length, maxAngles);
			for (int i = 0; i < nAngs; i++) {
				angles[i].setValue((float) PamUtils.constrainedAngleR(angs[i], Math.PI)); 
			}
			for (int i = nAngs; i < maxAngles; i++) {
				angles[i].setValue(null);
			}
			for (int i = 0; i < nErrs; i++) {
				angleErrors[i].setValue((float) errors[i]); 
			}
			for (int i = nErrs; i < maxAngles; i++) {
				angleErrors[i].setValue(null);
			}
		}
	}

	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int chanMap,
			long duration, double[] f) {
		// TODO Auto-generated method stub
		return null;
	}

}
