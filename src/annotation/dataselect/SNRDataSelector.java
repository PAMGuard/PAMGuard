package annotation.dataselect;

import PamguardMVC.PamDataBlock;
import annotation.DataAnnotationType;
import annotation.calcs.snr.SNRAnnotation;

public class SNRDataSelector extends ScalarDataSelector<SNRAnnotation> {

	public SNRDataSelector(DataAnnotationType<SNRAnnotation> annotationType, PamDataBlock pamDataBlock,
			String selectorName, boolean allowScores, int useMinMax) {
		super(annotationType, pamDataBlock, selectorName, allowScores, useMinMax);
	}

	@Override
	public double getScalarValue(SNRAnnotation annotation) {
		return annotation.getSnr();
	}

}
