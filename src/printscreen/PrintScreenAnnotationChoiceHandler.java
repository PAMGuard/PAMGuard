package printscreen;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import annotation.handler.AnnotationChoiceHandler;
import annotation.handler.AnnotationChoices;

public class PrintScreenAnnotationChoiceHandler extends AnnotationChoiceHandler {

	private PrintScreenControl printScreenControl;
	public PrintScreenAnnotationChoiceHandler(PrintScreenControl printScreenControl, PamDataBlock<PamDataUnit> pamDataBlock) {
		super(pamDataBlock);
		this.printScreenControl = printScreenControl;
	}

	@Override
	public AnnotationChoices getAnnotationChoices() {
		return printScreenControl.getPrintScreenParameters().getAnnotationChoices();
	}

}
