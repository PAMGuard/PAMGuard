package detectionPlotFX.plots.simple2d;

import javafx.geometry.Point2D;
import pamViewFX.fxNodes.PamSymbolFX;

public class SimpleSymbolData {
	public PamSymbolFX pamSymbol; 
	public Point2D pt;
	public SimpleSymbolData(PamSymbolFX pamSymbol, Point2D pt) {
		super();
		this.pamSymbol = pamSymbol;
		this.pt = pt;
	}
	
}
