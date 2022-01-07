package clickDetector.ClickClassifiers.basicSweep;

import javax.swing.AbstractSpinnerModel;

public class CodeSpinnerModel extends AbstractSpinnerModel {

	private CodeHost codehost;
	
	private SweepClassifier sweepClassifier;
	
	public CodeSpinnerModel(SweepClassifier sweepClassifier, CodeHost codehost) {
		super();
		this.sweepClassifier = sweepClassifier;
		this.codehost = codehost;
	}

	@Override
	public Object getNextValue() {
		return sweepClassifier.getNextFreeCode(codehost.getCode());
	}

	@Override
	public Object getPreviousValue() {
		return sweepClassifier.getPrevFreeCode(codehost.getCode());
	}

	@Override
	public Object getValue() {
		return codehost.getCode();
	}

	@Override
	public void setValue(Object value) {
		codehost.setCode((Integer) value);
	}
	
}

