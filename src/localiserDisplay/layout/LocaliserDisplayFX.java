package localiserDisplay.layout;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import localiserDisplay.LoclaiserDisplayControl;
import map3D.layout.Pane3D;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.threeD.Utils3D;

/**
 * Main pane for the localiser display. The loclaiser display  
 * @author Jamie Macaulay 
 *
 */
public class LocaliserDisplayFX extends PamBorderPane {
	
	/**
	 * Reference to the localiser control. 
	 */
	private LoclaiserDisplayControl locDisplayControl;
	
	/**
	 * Localiser map 3D. 
	 */
	private Pane3D map3D;

	/**
	 * The loc info pane. Shows localiser specific pane. 
	 */
	private LocInfoPane locInfoPane;
	
	/**
	 * Constructor for the localiser display. 
	 * @param localiserDisplayControl - the loclaiser display. 
	 */
	public LocaliserDisplayFX(LoclaiserDisplayControl localiserDisplayControl) {
		this.locDisplayControl=localiserDisplayControl; 
		
		this.map3D= new Pane3D(); 
		
		this.locInfoPane = new LocInfoPane(); 
		
		this.setCenter(map3D); 
		
		createAxes(map3D.getDynamicGroup()); 
		
		//this.setCenter(new Label("Hello"));
		
	}
	
	/**
	 * Create the axis for the Map. Shows x, y and z directions. 
	 * @param sceneRoot
	 */
	private void createAxes(Group sceneRoot) {
		 Group axis = Utils3D.buildAxes(300.,Color.DARKRED, Color.RED,
				 Color.DARKGREEN, Color.GREEN,
				 Color.CYAN, Color.BLUE,
				 Color.WHITE); 
		 sceneRoot.getChildren().add(axis); 
	}
		

}
