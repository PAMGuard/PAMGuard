package annotation.classifier;

import PamView.symbol.PamSymbolChooser;
import PamView.symbol.modifier.SymbolModifier;
import annotation.DataAnnotationType;
import annotation.binary.AnnotationBinaryHandler;
import annotation.dataselect.AnnotationDataSelCreator;

public class BaseClassificationAnnotationType extends DataAnnotationType<BaseClassificationAnnotation> {

	private String name = "Right Whale";
	
	private BaseClassificationBinary annotationBinary;

	private ClassifierDataSelCreator classificationSelCreator;
		
	public BaseClassificationAnnotationType(String methodName) {
		super();
		this.name = methodName;
		annotationBinary = new BaseClassificationBinary(this);
	}

	@Override
	public String getAnnotationName() {
		return name;
	}

	@Override
	public Class getAnnotationClass() {
		return BaseClassificationAnnotation.class;
	}

	@Override
	public boolean canAnnotate(Class dataUnitType) {
		return true;
	}

	@Override
	public String toString(BaseClassificationAnnotation dataAnnotation) {
		String ans = (name == null) ? "" : name + ", ";
		ans += String.format("%3.2f", dataAnnotation.getScore());
		return ans;
	}

	@Override
	public AnnotationBinaryHandler<BaseClassificationAnnotation> getBinaryHandler() {
		return annotationBinary;
	}

	@Override
	public String getShortIdCode() {
		return "BCLS";
	}

	@Override
	public SymbolModifier getSymbolModifier(PamSymbolChooser symbolChooser) {
		return new BaseClassSymbolModifier(this, name, symbolChooser);
	}

	@Override
	public AnnotationDataSelCreator getDataSelectCreator(String selectorName, boolean allowScores) {
		if (classificationSelCreator == null) {
			classificationSelCreator = new ClassifierDataSelCreator(this);
		}
		return classificationSelCreator;
	}



}
