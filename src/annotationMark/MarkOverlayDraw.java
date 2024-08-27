package annotationMark;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Window;

import PamUtils.Coordinate3d;
import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;
import PamView.GeneralProjector.ParameterUnits;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamKeyItem;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataUnit;

public class MarkOverlayDraw extends PamDetectionOverlayGraphics {

//	private SpectrogramAnnotationType spectrogramAnnotationType;

	private MarkModule annotationModule;
	public static final SymbolData defaultSymbolData = new SymbolData(PamSymbolType.SYMBOL_SQUARE, 8, 8, false, Color.BLACK, Color.BLACK);

	public MarkOverlayDraw(MarkDataBlock markDataBlock, MarkModule annotationModule) {
		super(markDataBlock, new PamSymbol(defaultSymbolData));
		this.annotationModule = annotationModule;
	}

	@Override
	public Rectangle drawOnSpectrogram(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		MarkDataUnit annotationDataUnit = (MarkDataUnit) pamDataUnit;
//		SpectrogramAnnotation specAnnotation = getSpecAnotation(pamDataUnit);
//		if (specAnnotation == null) {
//			return null;
//		}
		PamSymbol symbol = getPamSymbol(pamDataUnit, generalProjector);
		if (symbol == null) {
			symbol = getDefaultSymbol();
		}
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setColor(symbol.getLineColor());
		g2d.setStroke(new BasicStroke(symbol.getLineThickness()));

		double[] frequency = annotationDataUnit.getFrequency();
		Coordinate3d topLeft = generalProjector.getCoord3d(pamDataUnit.getTimeMilliseconds(), 
				frequency[1], 0);
		Coordinate3d botRight = generalProjector.getCoord3d(pamDataUnit.getEndTimeInMilliseconds(), 
				frequency[0], 0);
		
		int x = (int) Math.min(topLeft.x, botRight.x);
		int w = (int) Math.abs(topLeft.x - botRight.x);
//		System.out.printf("Spec draw duration %3.0f millis, x=%d, wid=%d\n" , annotationDataUnit.getDurationInMilliseconds(), x, w);
		g2d.drawRect(x, (int) topLeft.y, w, (int) botRight.y - (int) topLeft.y);

		if (generalProjector.isViewer()) {
			Coordinate3d middle = new Coordinate3d();
			middle.x = (topLeft.x + botRight.x)/2;
			middle.y = (topLeft.y + botRight.y)/2;
			middle.z = (topLeft.z + botRight.z)/2;
			generalProjector.addHoverData(middle, pamDataUnit);
		}
		
		g2d.dispose();
		return new Rectangle((int) topLeft.x, (int) topLeft.y, 
				(int) botRight.x - (int) topLeft.x, (int) botRight.y - (int) topLeft.y);
	}
	
	@Override
	public boolean canDraw(ParameterType[] parameterTypes, ParameterUnits[] parameterUnits) {
		if (parameterTypes[0] == ParameterType.TIME
				&& parameterTypes[1] == ParameterType.FREQUENCY) {
			return true;
		}
		
		return super.canDraw(parameterTypes, parameterUnits);
	}

	@Override
	public PamKeyItem createKeyItem(GeneralProjector generalProjector,
			int keyType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHoverText(GeneralProjector generalProjector,
			PamDataUnit dataUnit, int iSide) {
		return dataUnit.getSummaryString();
	}

	@Override
	public boolean hasOptionsDialog(GeneralProjector generalProjector) {
		return super.hasOptionsDialog(generalProjector);
	}

	@Override
	public boolean showOptions(Window parentWindow,
			GeneralProjector generalProjector) {
		return super.showOptions(parentWindow, generalProjector);
	}
	
	@Override
	public PamSymbol getDefaultSymbol() {
		return new PamSymbol(defaultSymbolData);
	}


}
