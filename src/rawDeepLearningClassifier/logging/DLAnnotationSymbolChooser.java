package rawDeepLearningClassifier.logging;

import PamView.symbol.AnnotationSymbolChooser;
import PamView.symbol.AnnotationSymbolOptions;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;

/**
 * The DL annotation symbol chooser
 * @author Jamie Macaulay
 *
 */
public class DLAnnotationSymbolChooser implements AnnotationSymbolChooser {

	@Override
	public SymbolData getSymbolData(PamDataUnit dataUnit, SymbolData symbolData, DataAnnotation annotation,
			AnnotationSymbolOptions annotationSymbolOptions) {
		return new SymbolData();
	}

}
