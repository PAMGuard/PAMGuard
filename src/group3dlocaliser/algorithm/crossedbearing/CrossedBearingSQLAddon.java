package group3dlocaliser.algorithm.crossedbearing;

import targetMotionOld.TargetMotionSQLLogging;

public class CrossedBearingSQLAddon extends TargetMotionSQLLogging {

	
	private targetMotionModule.TargetMotionSQLLogging tml;
	public CrossedBearingSQLAddon(int nResults) {
		super(nResults, "");
//		hideColumns(getBeamLatitude());
//		hideColumns(getBeamLongitude());
		
	}
}
