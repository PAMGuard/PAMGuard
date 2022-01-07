package dataPlotsFX.overlaymark;

import java.awt.Point;

import PamUtils.PamCoordinate;
import PamView.GeneralProjector;
import PamView.paneloverlay.overlaymark.OverlayMark;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

public class MarkPainterFX {

	private Color fillCol=Color.LIGHTGRAY;
	private Color lineCol=Color.LIGHTGRAY;
	
	private Color doneFillCol = Color.DARKTURQUOISE;
	private Color doneStrokeCol = Color.DARKTURQUOISE;
	
	private double intTransFill= 100./256.; 
	private double intTransStroke= 200./256.; 

	private double lineWidth=2;
	
	
	public MarkPainterFX() {
		
		fillCol=PamUtilsFX.addColorTransparancy(fillCol,  intTransFill); 
		lineCol=PamUtilsFX.addColorTransparancy(lineCol,  intTransStroke); 
		
		doneFillCol=PamUtilsFX.addColorTransparancy(doneFillCol,  intTransFill); 
		doneStrokeCol=PamUtilsFX.addColorTransparancy(doneStrokeCol,  intTransStroke); 

	}
	
	/**
	 * Draw the mark on the graph. 
	 * @param graphicsContext2D - the mark on the graph
	 * @param mark - the mark
	 * @param projector - the projector fro converting data values ot pixels
	 * @param isMarking  - true if the user is still marking 
	 */
	public void drawMark(GraphicsContext graphicsContext2D, OverlayMark mark,
			GeneralProjector<PamCoordinate> projector, boolean isMarking) {
		switch (mark.getMarkType()) {
		case RECTANGLE:
			drawRectange(graphicsContext2D, mark, projector, isMarking);
			break;
		case POLYGON:
			drawPolygon(graphicsContext2D, mark, projector, isMarking);
			break;
		}
	}

	private void drawRectange(GraphicsContext g2d, OverlayMark mark,
			GeneralProjector<PamCoordinate> projector, boolean isMarking) {
		
		if (mark.size() != 2) {
			return;
		}
	
		g2d.setLineWidth(2);
		g2d.setStroke(isMarking ? lineCol : doneStrokeCol);
		g2d.setFill(isMarking ? fillCol : doneFillCol);
		
		PamCoordinate m0 = mark.getCoordinate(0);
		PamCoordinate m1 = mark.getCoordinate(1);
		
		Point p0 = projector.getCoord3d(m0).getXYPoint();
		Point p1 = projector.getCoord3d(m1).getXYPoint();
		
//		g2d.strokeLine(p0.x, p0.y, p1.x, p0.y);
//		g2d.strokeLine(p1.x, p0.y, p1.x, p1.y);
//		g2d.strokeLine(p1.x, p1.y, p0.x, p1.y);
//		g2d.strokeLine(p0.x, p1.y, p0.x, p0.y);
		
		double x0, x1;
		double y0, y1; 
		
		x0=p0.x; x1=p1.x;
		y0=p0.y; y1=p1.y; 
		
//		System.out.println(String.format("start  x0 %3.1f x1 %3.1f y0 %3.1f y1 %3.1f", x0, x1, y0, y1));

		//make surew the rectangle paints both dragging left right up and down. 
		if (p1.x-p0.x<0){
			x0=p1.x; 
			x1=p0.x; 
		}
		
		if (p1.y-p0.y<0){
			y0=p1.y; 
			y1=p0.y; 
		}
		
//		System.out.println(String.format("finish x0 %3.1f x1 %3.1f y0 %3.1f y1 %3.1f", x0, x1, y0, y1));

		g2d.fillRect(x0, y0, x1-x0, y1-y0);
		g2d.strokeRect(x0, y0, x1-x0, y1-y0);
		
		//now add lines to show where the y axis and time axis limits are
		//TODO
		
	}

	private void drawPolygon(GraphicsContext g2d, OverlayMark mark,
			GeneralProjector<PamCoordinate> projector, boolean isMarking) {
		
		//set colour. 
		g2d.setLineWidth(2);
		g2d.setStroke(isMarking ? lineCol : doneStrokeCol);
		g2d.setFill(isMarking ? fillCol : doneFillCol);

		
		if (mark.size() < 1) {
			//			System.out.println("Dont draw mar size " + mark.size());
			return;
		}
		PamCoordinate c;
		Point p;
		int nPoints = mark.size();
		PamCoordinate currentMouse = mark.getCurrentMouseCoordinate();
		if (currentMouse != null) {
			nPoints++;
		}
		double[] xPoints = new double[nPoints];
		double[] yPoints = new double[nPoints];
		for (int i = 0; i < mark.size(); i++) {
			c = mark.getCoordinate(i);
			p = projector.getCoord3d(c).getXYPoint();
			xPoints[i] = p.x;
			yPoints[i] = p.y;
		}
		if (currentMouse != null) {
			p = projector.getCoord3d(currentMouse).getXYPoint();
			xPoints[nPoints-1] = p.x;
			yPoints[nPoints-1] = p.y;
		}
		
		g2d.setLineWidth(2);
		
		//stoke lines bar the one that's not yet joined up. 
		for (int i=0; i< xPoints.length-1; i++){
			g2d.strokeLine(xPoints[i], yPoints[i], xPoints[i+1], yPoints[i+1]);
		}
		
		//fill the polygon
		g2d.fillPolygon(xPoints, yPoints, nPoints);
		
		
		
//		Color fillCol = getFillCol();
//		if (fillCol != null) {
//			g2d.setColor(getFillCol());
//			g2d.fillPolygon(pgon);
//		}
//		// draw the line, but with dashes at the end, back to the start. 
//		g2d.setStroke(getLineStroke());
//		g2d.setColor(getLineCol());
//		for (int i = 1; i < nPoints; i++) {
//			g2d.drawLine(xPoints[i-1], yPoints[i-1], xPoints[i], yPoints[i]);
//		}
//		g2d.setStroke(getFinalLineStroke());
//		g2d.strokeLine(xPoints[0], yPoints[0], xPoints[nPoints-1], yPoints[nPoints-1]);

	}

}
