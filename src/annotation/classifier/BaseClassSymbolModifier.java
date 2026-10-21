package annotation.classifier;

import java.awt.Color;


import PamView.ColourArray;
import PamView.GeneralProjector;
import PamView.PamSymbolType;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.legend.ColourBarSymbolData;
import PamView.symbol.legend.SymbolLegend;
import PamView.symbol.modifier.SymbolModType;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataUnit;
import annotation.DataAnnotation;
import annotation.DataAnnotationType;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;

public class BaseClassSymbolModifier extends SymbolModifier {
	
	private DataAnnotationType annotationType;

	public BaseClassSymbolModifier(DataAnnotationType annotationType, String name, PamSymbolChooser symbolChooser) {
		super(name, symbolChooser, SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR);
		this.annotationType =annotationType;
	}

	private static final int NCOL = 100;
	private ColourArray colArray = ColourArray.createRainbowArray(NCOL);
	private SymbolData symbolData;

//	@Override
//	public SymbolData getSymbolData(PamDataUnit dataUnit, SymbolData symbolData, DataAnnotation annotation, AnnotationSymbolOptions annotationSymbolOptions) {
//		if (annotation instanceof BaseClassificationAnnotation == false) {
//			return symbolData;
//		}
//		if (symbolData == null) {
//			symbolData = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, Color.BLACK, Color.BLACK);
//		}
//		BaseClassificationAnnotation bca = (BaseClassificationAnnotation) annotation;
//		double score = bca.getScore();
//		score = scaleTrans(score);
//		int iCol = (int) Math.round(score*(NCOL-1));
//		iCol = Math.max(0, Math.min(iCol, NCOL-1));
//		Color col = colArray.getColour(iCol);
//		if ((annotationSymbolOptions.changeChoice & AnnotationSymbolOptions.CHANGE_FILL_COLOUR) != 0) {
//			symbolData.setFillColor(col);
//		}
//		if ((annotationSymbolOptions.changeChoice & AnnotationSymbolOptions.CHANGE_LINE_COLOUR) != 0) {
//			symbolData.setLineColor(col);
//		}
//		return symbolData;
//	}
	
	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		DataAnnotation annotation = dataUnit.findDataAnnotation(annotationType.getAnnotationClass());
		if (annotation instanceof BaseClassificationAnnotation == false) {
			return null;
		}
		if (symbolData == null) {
			symbolData = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, Color.BLACK, Color.BLACK);
		}
		BaseClassificationAnnotation bca = (BaseClassificationAnnotation) annotation;
		double score = bca.getScore();
		score = scaleTrans(score);
		int iCol = (int) Math.round(score*(NCOL-1));
		iCol = Math.max(0, Math.min(iCol, NCOL-1));
		Color col = colArray.getColour(iCol);
		symbolData.setFillColor(col);
		symbolData.setLineColor(col);
		return symbolData;
	}

	/**
	 * Legend with a colour bar for the classification score. Scores are coloured on
	 * a log-odds scale from 0.5 to ~1. Only annotated data are modified.
	 */
	@Override
	public SymbolLegend getSymbolLegend() {
		SymbolData legendSymbol = new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 10, 10, true, Color.BLACK, Color.BLACK);
		// FX HOT is the same black-blue-cyan-green-orange-red map as the Swing rainbow array.
		return new SymbolLegend(getName(), new ColourBarSymbolData(legendSymbol, "Score (log-odds)",
				ColourArrayType.HOT, 0.5, 1.0, null));
	}

	private double scaleTrans(double score) {
		score = Math.max(0., Math.min(score, 1.));
		score = Math.log(score/(1-score))/6;
		score = Math.max(0., Math.min(score, 1.));
		return score;
	}
}
