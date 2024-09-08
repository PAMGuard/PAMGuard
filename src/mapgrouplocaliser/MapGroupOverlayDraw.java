package mapgrouplocaliser;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import PamView.ManagedSymbolInfo;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.paneloverlay.overlaymark.MarkManager;
import PamView.paneloverlay.overlaymark.MarkOverlayDraw;
import PamView.symbol.SymbolData;


public class MapGroupOverlayDraw extends MarkOverlayDraw {

	private PamSymbol mapSymbol = null;
	private MapGroupLocaliserControl mapGroupLocaliserControl;
	private Stroke defaultStroke = new BasicStroke();
	
	public static final SymbolData defSymbol = new SymbolData(PamSymbolType.SYMBOL_SQUARE, 16, 16, true, Color.CYAN, Color.CYAN);

	public MapGroupOverlayDraw(MapGroupLocaliserControl mapGroupLocaliserControl, MarkManager markManager) {
		super(markManager);
		this.mapGroupLocaliserControl = mapGroupLocaliserControl;
//		PamOldSymbolManager.getInstance().addManagesSymbol(this);
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.overlaymark.MarkOverlayDraw#getFillCol()
	 */
	@Override
	public Color getFillCol() {
		if (!getPamSymbol().isFill()) {
			return null;
		}
		Color col = getPamSymbol().getFillColor();
		col = new Color(col.getRed(), col.getGreen(), col.getBlue(), 100);
		return col;
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.overlaymark.MarkOverlayDraw#getLineCol()
	 */
	@Override
	public Color getLineCol() {
		return getPamSymbol().getLineColor();
	}

	
	public PamSymbol getPamSymbol() {
		if (mapSymbol == null) {
			setPamSymbol(new PamSymbol(defSymbol));
		}
		return mapSymbol;
	}

	
	public void setPamSymbol(PamSymbol pamSymbol) {
		this.mapSymbol = pamSymbol;
		defaultStroke = new BasicStroke(pamSymbol.getLineThickness());
	}

	public ManagedSymbolInfo getSymbolInfo() {
		return new ManagedSymbolInfo(mapGroupLocaliserControl.getUnitName() + " overlays");
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.overlaymark.MarkOverlayDraw#getLineStroke()
	 */
	@Override
	public Stroke getLineStroke() {
		return defaultStroke ;
	}

	/* (non-Javadoc)
	 * @see PamView.paneloverlay.overlaymark.MarkOverlayDraw#getFinalLineStroke()
	 */
	@Override
	public Stroke getFinalLineStroke() {
		return defaultStroke;
	}


}
