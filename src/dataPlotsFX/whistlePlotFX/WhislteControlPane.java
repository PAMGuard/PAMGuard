package dataPlotsFX.whistlePlotFX;

import dataPlotsFX.layout.TDSettingsPane;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.symbol.StandardSymbolOptionsPane;

/**
 * Controls the appearance of whistles on a TDGraphFX. 
 * 
 * @author Jamie Macaulay
 *
 */
public class WhislteControlPane extends PamBorderPane implements TDSettingsPane {
	
	private Image clickIcon=new Image(getClass().getResourceAsStream("/Resources/whistles.png"));

	
	private WhistlePlotInfoFX whistlePlotInfo;

	public WhislteControlPane(WhistlePlotInfoFX whistlePlotInfo) {
		super();
		this.whistlePlotInfo=whistlePlotInfo;
		
		StandardSymbolOptionsPane symbolOptionsPane= (StandardSymbolOptionsPane) whistlePlotInfo.getDataBlock().getPamSymbolManager().
				getFXOptionsPane(whistlePlotInfo.getTDGraph().getUniqueName(), 
						whistlePlotInfo.getTDGraph().getGraphProjector()); 
		
		symbolOptionsPane.addSettingsListener(()->{
			whistlePlotInfo.getWhistleSymbolChooser().notifySettingsChange(); 
			whistlePlotInfo.getTDGraph().repaint(0);
		});
		
		this.setCenter(symbolOptionsPane.getContentNode());
		this.setPrefWidth(250);
	}

	@Override
	public Node getHidingIcon() {
		return new ImageView(clickIcon);
	}

	@Override
	public String getShowingName() {
		return whistlePlotInfo.getShortName();
	}

	@Override
	public Node getShowingIcon() {
		return new ImageView(clickIcon);
	}

	@Override
	public Pane getPane() {
		return this;
	}
	
	

}
