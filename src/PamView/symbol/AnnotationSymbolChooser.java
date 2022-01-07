package PamView.symbol;

import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;

public interface AnnotationSymbolChooser {

	public SymbolData getSymbolData(PamDataUnit dataUnit, SymbolData symbolData, DataAnnotation annotation, AnnotationSymbolOptions annotationSymbolOptions);
	
}
