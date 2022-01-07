package pamViewFX.fxNodes.connectionPane.structures;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import pamViewFX.fxNodes.connectionPane.ConnectionNodeBody;
import pamViewFX.fxNodes.connectionPane.ConnectionPane;
import pamViewFX.fxNodes.connectionPane.StandardConnectionNode;
import pamViewFX.fxNodes.connectionPane.StandardConnectionPlug;
import pamViewFX.fxNodes.connectionPane.StandardConnectionSocket;

/**
 * An extension structure is a structure which allows users to clean up connection nodes. 
 * 
 * @author Jamie Macaulay
 *
 */
public class ExtensionSocketStructure extends StandardConnectionNode implements ConnectionStructure {
	
	
	public static double DEFAULT_BODY_WIDTH = 40;
	
	/**
	 * The colour of the extension socket body
	 */
	private static  Color bodyColour = Color.DODGERBLUE;
	
	/**
	 * Extension structure construction. 
	 */
	public ExtensionSocketStructure(ConnectionPane connectionPane) {
		super( connectionPane,  Orientation.HORIZONTAL); 
		this.plugYProperty().setValue(DEFAULT_BODY_WIDTH/2);
		this.plugXProperty().setValue(DEFAULT_BODY_WIDTH/2);
		
		this.socketYProperty().setValue(DEFAULT_BODY_WIDTH/2);
		this.socketXProperty().setValue(DEFAULT_BODY_WIDTH/2);

	}
	
	/**
	 * Create the connection plug. 
	 * @return the connection plug. 
	 */
	@Override
	public void initDefaultPlugLayout(StandardConnectionPlug plug) {
		//change plug so it sits inside the connection structure. 
		super.initDefaultPlugLayout(plug);
	
		plug.getPlugConnectionLines().get(0).startYProperty().bind(
				getConnectionNodeBody().layoutYProperty().add(getConnectionNodeBody().heightProperty().divide(2)));

	}
	
	/**
	 * Create the node body. This is just a simple circle. 
	 * @return the node body - the node body. 
	 */
	@Override
	public ConnectionNodeBody createNodeBody() {
		
		Circle circle  =  new Circle(DEFAULT_BODY_WIDTH/2,DEFAULT_BODY_WIDTH/2, DEFAULT_BODY_WIDTH/2);
		circle.setFill(bodyColour);
		
		ConnectionNodeBody connectionNodeBody = new ConnectionNodeBody(this); 
		
		connectionNodeBody.setBackground(new Background(
				new BackgroundFill(Color.TRANSPARENT,CornerRadii.EMPTY,Insets.EMPTY)));
		
		connectionNodeBody.getChildren().add(circle); 
		
		
		connectionNodeBody.setPrefHeight(DEFAULT_BODY_WIDTH);
		connectionNodeBody.setPrefWidth(DEFAULT_BODY_WIDTH);

		
		return connectionNodeBody; 
	}	
//	
//	/**
//	 * Create the connection plug. 
//	 * @return the connection plug. 
//	 */
//	public StandardConnectionPlug createPlug(Orientation orientation) {
//		return new StandardConnectionPlug(this, StandardConnectionNode.plugBodyWidth, StandardConnectionNode.plugEndWidth, 
//				StandardConnectionNode.plugBodyHeight, StandardConnectionNode.plugEndHeight,  StandardConnectionNode.cornerRadius, orientation);
//	}


	@Override
	public Group getConnectionGroup() {
		return this; 
	}

	
	@Override
	public Tooltip getToolTip() {
		return new Tooltip("Extension Socket for cleaning up plug lines");
	}

	@Override
	public ConnectionStructureType getStructureType() {
		return ConnectionStructureType.ExtensionSocket;
	}

	
	/**
	 * Get an icon for this node. 
	 * @param defaultWidth - the icon width
	 * @return the icon.
	 */
	public static Node getStructureIcon(double defaultWidth) {
		
		/***
		 * The icon is a little complex so build from different nodes
		 */
		
		Group group = new Group(); 
		
		double plugOffsetX = 5; 
		
		Circle circle  =  new Circle(DEFAULT_BODY_WIDTH/2,DEFAULT_BODY_WIDTH/2, DEFAULT_BODY_WIDTH/2);
		circle.setFill(bodyColour);
		
		Polygon plug = new Polygon(); 
		plug.getPoints().addAll(StandardConnectionPlug.getPlugPolygon(StandardConnectionNode.plugBodyWidth, StandardConnectionNode.plugEndWidth,
				StandardConnectionNode.plugBodyHeight, StandardConnectionNode.plugEndHeight, StandardConnectionNode.cornerRadius));
		
		plug.layoutXProperty().bind(circle.centerXProperty().add(circle.radiusProperty()).add(plugOffsetX).add(StandardConnectionNode.plugBodyWidth+StandardConnectionNode.plugEndWidth));
		plug.layoutYProperty().bind(circle.radiusProperty().divide(2).add(StandardConnectionNode.plugBodyHeight/2-3));
		plug.setFill(bodyColour);
		
		
		Polygon socket = new Polygon(); 
		socket.getPoints().addAll(StandardConnectionSocket.getSocketPolygon(StandardConnectionNode.plugBodyWidth, StandardConnectionNode.plugEndWidth,
				StandardConnectionNode.plugBodyHeight, StandardConnectionNode.plugEndHeight, StandardConnectionNode.cornerRadius));
		
		socket.layoutXProperty().bind(circle.centerXProperty().subtract(circle.radiusProperty()).subtract(plugOffsetX).subtract(StandardConnectionNode.plugBodyWidth+StandardConnectionNode.plugEndWidth));
		socket.layoutYProperty().bind(circle.radiusProperty().divide(2).add(StandardConnectionNode.plugBodyHeight/2-3));
		socket.setFill(bodyColour);
		
		Line line1 = new Line(); 
		line1.setStroke(bodyColour);
		line1.setStrokeWidth(3);
		line1.startXProperty().bind(circle.centerXProperty().add(circle.radiusProperty()));
		line1.startYProperty().bind(circle.centerYProperty());
		line1.endXProperty().bind(circle.centerXProperty().add(circle.radiusProperty()).add(plugOffsetX));
		line1.endYProperty().bind(circle.centerYProperty());

		
		Line line2 = new Line(); 
		line2.setStroke(bodyColour);
		line2.setStrokeWidth(3);
		line2.startXProperty().bind(circle.centerXProperty().subtract(circle.radiusProperty()));
		line2.startYProperty().bind(circle.centerYProperty());
		line2.endXProperty().bind(circle.centerXProperty().subtract(circle.radiusProperty()).subtract(plugOffsetX));
		line2.endYProperty().bind(circle.centerYProperty());

		group.getChildren().addAll(circle,plug, socket, line1, line2); 
		
		return group;
	}


}
