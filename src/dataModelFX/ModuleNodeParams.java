package dataModelFX;

import java.io.Serializable;
import java.util.UUID;

import PamController.UsedModuleInfo;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import dataModelFX.connectionNodes.ModuleConnectionNode;
import javafx.geometry.Point2D;

/**
 * Holds seriliazbale informations on a ModuleConnectionNode. 
 * 
 * @author Jamie Macaulay
 *
 */
public class ModuleNodeParams extends UsedModuleInfo implements ConnectionNodeParams, Serializable, ManagedParameters {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final double DEFAULT_WIDTH = 100; 
	
	
	public static final double DEFAULT_HEIGHT = 100; 

	/**
	 * The default colour of a module rectangle.
	 */
	public static final double[] DEFAULT_COLOUR = new double[] {0.65, 0.65, 0.65}; 
	
	/**
	 * The opacity to set disabled modules
	 */
	public final static double DISABLED_OPACITY=0.4; 
	

	/**
	 * The preferred width of the module connection node
	 */
	public double prefWidth = DEFAULT_WIDTH;
	
	/**
	 * The preferred height of the module connection node. 
	 */
	public double prefHeight = DEFAULT_HEIGHT; 
	
	/**
	 * The x position of the connection node in the data model pane. 
	 */
	private double layoutX;
	
	/**
	 * The y position of the connection node in the data model pane. 
	 */
	private double layoutY;


	/**
	 * A unique ID for the connection node. 
	 */
	private UUID id;
	
	private String unitType = ""; 

	/**
	 * Get the connection node type. 
	 */
	private PAMConnectionNodeType connectionNodeType = PAMConnectionNodeType.ModuleConnectionNode; 
	
	
	public ModuleNodeParams(ModuleConnectionNode moduleConnectionNode) {
		super(moduleConnectionNode.getPamControlledUnit().getClass().getName(), moduleConnectionNode.getPamControlledUnit().getUnitType(), 
				moduleConnectionNode.getPamControlledUnit().getUnitName());
		//System.out.println("Module x y: "+ moduleConnectionNode.getConnectionRectangle().getLayoutX() + " "+ moduleConnectionNode.getConnectionRectangle().getLayoutY());
		this.layoutX=moduleConnectionNode.getConnectionNodeBody().getLayoutX();
		this.layoutY=moduleConnectionNode.getConnectionNodeBody().getLayoutY();
		this.id = UUID.randomUUID(); 
		this.unitType = moduleConnectionNode.getPamControlledUnit().getUnitType(); 
//		this.settingsShowing=moduleConnectionNode.isSettingsShowing();
	}

	public ModuleNodeParams(String className, String unitType, String unitName, Point2D location, boolean settingsShowing) {
		super(className, unitType, unitName);
		this.layoutX=location.getX();
		this.layoutY=location.getY();
		this.id = UUID.randomUUID(); 
		this.unitType = unitType; 

//		this.settingsShowing=settingsShowing; 
	}
	

	@Override
	public double getLayoutX() {
		return layoutX;
	}

	@Override
	public double getLayoutY() {
		return layoutY;
	}
	
	public void setLayoutX(double layoutX) {
		this.layoutX=layoutX;
	}
	
	public void setLayoutY(double layoutY) {
		this.layoutY=layoutY;
	}


	public void setNodeType(PAMConnectionNodeType connectionnoNodeType) {
		this.connectionNodeType= connectionnoNodeType;
	}
	
	@Override
	public PAMConnectionNodeType getNodeType() {
		return connectionNodeType; 
	}


	@Override
	public UUID getID() {
		return id;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = super.getParameterSet();
		return ps;
	}

}
