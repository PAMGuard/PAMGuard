package dataMap;

import java.awt.Color;
import java.awt.Graphics;

import PamView.panel.KeyPanel;
import dataMap.DataStreamPanel.DataGraph;
import dataMap.layoutFX.DataStreamPaneFX.DataGraphFX;
import javafx.scene.canvas.GraphicsContext;

/**
 * Interface for module specific data map drawing. 
 * @author Doug Gillespie.
 *
 */
public interface DataMapDrawing {

	public void drawEffort(Graphics g, DataGraph dataGraph, OfflineDataMap map,
			Color haveDataColour);

	public void drawDataRate(Graphics g, DataGraph dataGraph, OfflineDataMap map,
			Color dataStreamColour);
	
	public void drawEffort(GraphicsContext g, DataGraph dataGraph, OfflineDataMap map,
			Color haveDataColour);

	public void drawDataRate(GraphicsContext g, DataGraph dataGraph, OfflineDataMap map,
			Color dataStreamColour);
	
	public KeyPanel getKeyPanel();

	public void drawEffort(GraphicsContext g, DataGraphFX dataGraphFX, OfflineDataMap aMap,
			javafx.scene.paint.Color haveDataColour);

	public void drawDataRate(GraphicsContext g, DataGraphFX dataGraphFX, OfflineDataMap aMap,
			javafx.scene.paint.Color dataStreamColour);
	

}
