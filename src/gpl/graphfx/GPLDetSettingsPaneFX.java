package gpl.graphfx;

import dataPlotsFX.FXIconLoder;
import dataPlotsFX.layout.TDSettingsPane;
import gpl.GPLControlledUnit;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.symbol.FXSymbolOptionsPane;
import pamViewFX.symbol.StandardSymbolOptionPaneFX;
import pamViewFX.symbol.StandardSymbolOptionsPane;

public class GPLDetSettingsPaneFX extends PamBorderPane implements TDSettingsPane {

	private GPLControlledUnit gplControlledUnit;
	private GPLDetPlotinfo gplDetPlotInfo;
	private Canvas icon;
	
	public GPLDetSettingsPaneFX(gpl.GPLControlledUnit gPLControlledUnit, GPLDetPlotinfo gplDetPlotInfo) {
		super();
		gplControlledUnit = gPLControlledUnit;
		this.gplDetPlotInfo = gplDetPlotInfo;

		icon = FXIconLoder.createIcon("Resources/gplicon.png", 20, 20);
		
		FXSymbolOptionsPane fxPane = gplDetPlotInfo.getDataBlock().getPamSymbolManager().
				getFXOptionsPane(gplDetPlotInfo.getTDGraph().getUniqueName(), 
						gplDetPlotInfo.getTDGraph().getGraphProjector()); 
		
		StandardSymbolOptionsPane symbolOptionsPane = (StandardSymbolOptionsPane) fxPane;
		
//		symbolOptionsPane.addSettingsListener(()->{
//			gplDetPlotInfo.getWhistleSymbolChooser().notifySettingsChange(); 
//			gplDetPlotInfo.getTDGraph().repaint(0);
		this.setCenter(symbolOptionsPane.getContentNode());
//		this.setPrefWidth(250);
	}

	@Override
	public Node getHidingIcon() {
		return icon;
	}

	@Override
	public String getShowingName() {
		return gplControlledUnit.getUnitName();
	}

	@Override
	public Node getShowingIcon() {
		return icon;
	}

	@Override
	public Pane getPane() {
		return this;
	}

}
