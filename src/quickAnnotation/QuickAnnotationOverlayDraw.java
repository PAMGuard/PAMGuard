package quickAnnotation;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import PamUtils.Coordinate3d;
import PamView.GeneralProjector;
import PamView.PamSymbol;
import PamguardMVC.PamDataUnit;
import annotation.string.StringAnnotation;
import annotationMark.MarkDataBlock;
import annotationMark.MarkDataUnit;
import annotationMark.MarkModule;
import annotationMark.MarkOverlayDraw;
import generalDatabase.lookupTables.LookupItem;

public class QuickAnnotationOverlayDraw extends MarkOverlayDraw {
	QuickAnnotationModule qam;
	
	public QuickAnnotationOverlayDraw(MarkDataBlock markDataBlock, MarkModule annotationModule) {
		super(markDataBlock, annotationModule);
		qam = (QuickAnnotationModule) annotationModule;
	}

	@Override
	public Rectangle drawDataUnit(Graphics g, PamDataUnit pamDataUnit,
			GeneralProjector generalProjector) {
		MarkDataUnit annotationDataUnit = (MarkDataUnit) pamDataUnit;
//		SpectrogramAnnotation specAnnotation = getSpecAnotation(pamDataUnit);
//		if (specAnnotation == null) {
//			return null;
//		}
		PamSymbol symbol = getDefaultSymbol();
		String label ="";
		StringAnnotation labelAnnotation = (StringAnnotation) pamDataUnit.findDataAnnotation(StringAnnotation.class,
				qam.getLabelAnnotationType().getAnnotationName());
		if (labelAnnotation != null){
			label = labelAnnotation.getString();
			if (label == null)
				label = "";
			
			LookupItem li = qam.getQuickAnnotationParameters().quickList.findSpeciesCode(label);
			if (li!=null)
				symbol = li.getSymbol();
			if (symbol == null) {
				symbol = getDefaultSymbol();
			}
		}
		
		
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setColor(symbol.getLineColor());
		g2d.setStroke(new BasicStroke(symbol.getLineThickness()));

		double[] frequency = annotationDataUnit.getFrequency();
		Coordinate3d topLeft = generalProjector.getCoord3d(pamDataUnit.getTimeMilliseconds(), 
				frequency[1], 0);
		Coordinate3d botRight = generalProjector.getCoord3d(pamDataUnit.getEndTimeInMilliseconds(), 
				frequency[0], 0);
		
		g2d.drawRect((int) topLeft.x, (int) topLeft.y, 
				(int) botRight.x - (int) topLeft.x, (int) botRight.y - (int) topLeft.y);

		if (generalProjector.isViewer()) {
			Coordinate3d middle = new Coordinate3d();
			middle.x = (topLeft.x + botRight.x)/2;
			middle.y = (topLeft.y + botRight.y)/2;
			middle.z = (topLeft.z + botRight.z)/2;
			generalProjector.addHoverData(middle, pamDataUnit);
		    java.awt.FontMetrics metrics = g.getFontMetrics(g2d.getFont());
		    // Determine the X coordinate for the text
		    int x = (int) ((middle.x - metrics.stringWidth(label)/ 2) );
		    if (x > 0)
			g2d.drawString(label, (int) x, (int) middle.y);
		}
		
		g2d.dispose();
		return new Rectangle((int) topLeft.x, (int) topLeft.y, 
				(int) botRight.x - (int) topLeft.x, (int) botRight.y - (int) topLeft.y);
	}

}
