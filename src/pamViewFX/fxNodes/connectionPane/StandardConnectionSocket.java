package pamViewFX.fxNodes.connectionPane;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.geometry.Orientation;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;

/**
 * A socket which allows connections to parent nodes. 
 * @author Jamie Macaulay
 *
 */
public class StandardConnectionSocket extends AbstractConnector {


	/**
	 * Socket which branches off a connection line. 
	 */
	boolean isBranch=false;

	/**
	 * Lines which connect socket to rectangle or a plug connection line. The first line in the list
	 * remains invisible unless the socket can be connected to a nearby plug in which case it connects the 
	 * plug and the socket. 
	 */
	private ArrayList<ConnectionLine> socketConnectionLines;

	/**
	 * This line appears if a plug comes close to the socket- connects the plug and the socket. 
	 */
	private Line canConnectLine;

	/**
	 * The plug connected to the connection line the socket is connected to. Note this is NOT the plug
	 * that the socket is connected to. Only used in branch socket.
	 */
	private StandardConnectionPlug connectionPlug;

	/**
	 * The line the branch socket is connected to. Only used in branch socket.
	 */
	private ConnectionLine connectionLine;

	private Color errorColour=Color.RED;

	@SuppressWarnings("unused")
	private boolean isError;



	/**
	 * Create a connection socket. 
	 * @param plugBodyWidth  - the width of the plug body
	 * @param plugEndWidth   - the width of the plug connection area- the bit which fits into the socket. 
	 * @param plugBodyHeight - the height of the main plug body.
	 * @param plugEndHeight  - the height of the plug connection area- the bit which fits into the socket. 
	 */
	public StandardConnectionSocket(StandardConnectionNode connectionNode, double plugBodyWidth, double plugEndWidth, double plugBodyHeight, double plugEndHeight, double cornerRadius, Orientation orientation){
		super(connectionNode, orientation);

		createSocket( plugBodyWidth, plugEndWidth, plugBodyHeight, plugEndHeight, cornerRadius,  orientation);

	}

	/**
	 * Create a branch connection socket. 
	 * @param socketBodyWidth  - the width of the socket body
	 * @param socketEndWidth   - the width of the socket connection area- the bit which fits into the socket. 
	 * @param socketBodyHeight - the height of the main socket body.
	 * @param socketEndHeight  - the height of the socket connection area- the bit which fits into the socket. 
	 * @param connectionLine - the parent connection line for the branch socket; 
	 */
	public StandardConnectionSocket(StandardConnectionNode connectionNode, double socketBodyWidth,
			double socketEndWidth, double socketBodyHeight,
			double socketEndHeight, double cornerRadius, 
			Orientation orientation, ConnectionLine connectionLine ) {
		super(connectionNode, orientation);

		createSocket( socketBodyWidth, socketEndWidth, socketBodyHeight, socketEndHeight, cornerRadius, orientation);
		isBranch=true; 

		this.connectionLine=connectionLine; 

		//find the plug
		for (int i=0; i<connectionNode.getConnectionPlugs().size(); i++){
			for (int j=0; j<connectionNode.getConnectionPlugs().get(i).getPlugConnectionLines().size(); j++){
				if (connectionNode.getConnectionPlugs().get(i).getPlugConnectionLines().get(j).equals(connectionLine)){
					connectionPlug=connectionNode.getConnectionPlugs().get(i);
					break; 
				}
			}
		}

	}

	/**
	 * Set the plug and lines to show an error
	 **/
	public void setPlugErrorAppearance(){
		this.setStroke(errorColour);
		this.setFill(errorColour);
		for (int i=0; i<this.getSocketConnectionLines().size(); i++){
			this.getSocketConnectionLines().get(i).setLineErrorAppearance();

		}		
	}

	/**
	 * Get the polygon point array for the plug.
	 * @param socketBodyWidth - the width of the plug body.
	 * @param socketEndWidth - the width of the end section of the plug (that fits into the socket).
	 * @param socketBodyHeight - the height of the plug body.
	 * @param socketEndHeight -  the height of the end section of the plug (that fits into the socket).
	 * @param cornerRadius - the corner radius of the plug 
	 * @return list of points that make a polygon of the plug shape. 
	 */
	public static ArrayList<Double> getSocketPolygon(double socketBodyWidth, double socketEndWidth, 
			double socketBodyHeight, double socketEndHeight,  double cornerRadius) {
	
		ArrayList<Double> points = new ArrayList<Double>();

		 points .addAll(Arrays.asList(new Double[]{
				0.0							, socketEndHeight/2.,
				0-socketEndWidth				, socketEndHeight/2.,
				0-socketEndWidth				, socketBodyHeight/2.,
				socketBodyWidth+socketEndWidth-cornerRadius	, socketBodyHeight/2,
		}));

		//bottom right corner
		double nVals=cornerRadius*2; 
		double circValx;
		double circValy; 
		for (double i=0; i<nVals; i++){
			circValx=cornerRadius*Math.sin((i/nVals)*(Math.PI/2));
			circValy=cornerRadius*Math.cos((i/nVals)*(Math.PI/2));
			 points .addAll(Arrays.asList(new Double[]{socketBodyWidth+socketEndWidth-(cornerRadius-circValx), socketBodyHeight/2-(cornerRadius-circValy)}));
		}

		 points .addAll(Arrays.asList(new Double[]{

				socketBodyWidth+socketEndWidth	, socketBodyHeight/2-cornerRadius,
				socketBodyWidth+socketEndWidth	, -socketBodyHeight/2+cornerRadius,
		}));

		//top right corner
		for (double i=0; i<nVals; i++){
			circValx=cornerRadius*Math.sin((i/nVals)*(Math.PI/2));
			circValy=cornerRadius*Math.cos((i/nVals)*(Math.PI/2));
			 points .addAll(Arrays.asList(new Double[]{socketBodyWidth+socketEndWidth-(cornerRadius-circValy), -socketBodyHeight/2.+(cornerRadius-circValx)}));
		}

		 points .addAll(Arrays.asList(new Double[]{
				socketBodyWidth+socketEndWidth-cornerRadius			, -socketBodyHeight/2., 
				0-socketEndWidth				, -socketBodyHeight/2., 
				0-socketEndWidth				, -socketEndHeight/2.,
				0.0							,-socketEndHeight/2.,
		}));	

		 return points;
		
	}


	private void createSocket(double socketBodyWidth,
			double socketEndWidth, double socketBodyHeight,
			double socketEndHeight, double cornerRadius, 
			Orientation orientation){		

		this.getPoints().addAll(getSocketPolygon( socketBodyWidth,  socketEndWidth, 
				 socketBodyHeight,  socketEndHeight,   cornerRadius));
		
		
		//rotate shape by 90 degrees about 0,0
		if (orientation==Orientation.VERTICAL) this.getTransforms().add(new Rotate(90,0,0));

		//		else {
		//			this.getPoints().addAll(new Double[]{
		//					plugEndHeight/2.,0.0,
		//					plugEndHeight/2.,0-plugEndWidth, 
		//					plugBodyHeight/2., 0-plugEndWidth, 
		//					plugBodyHeight/2,plugBodyWidth+plugEndWidth,
		//					-plugBodyHeight/2,plugBodyWidth+plugEndWidth, 
		//					-plugBodyHeight/2., 0-plugEndWidth,
		//					-plugEndHeight/2.,0-plugEndWidth,
		//					-plugEndHeight/2.,0.,
		//			});
		//		}

		//line which shows when plug is near but is invisible otherwise.
		canConnectLine= new ConnectionLine(getConnectionNode(), null);
		canConnectLine.startXProperty().bind(this.layoutXProperty()); 
		canConnectLine.startYProperty().bind(this.layoutYProperty());
		canConnectLine.setVisible(false);

	}

	@Override
	public void setConnectionStatus(int type, ConnectorNode connectionShape) {
		switch (type){
		case CONNECTED:
			canConnectLine.setVisible(false);
			break;
		case POSSIBLE_CONNECTION:
			canConnectLine.setVisible(true);
			canConnectLine.endXProperty().bind(connectionShape.getShape().layoutXProperty());
			canConnectLine.endYProperty().bind(connectionShape.getShape().layoutYProperty());
			canConnectLine.setStroke(this.getHighLightColour());
			break;
		case NO_CONNECTION:
			canConnectLine.setVisible(false);
			canConnectLine.setStroke(Color.RED);
			//now if the socket is not the primary socket and receivers a no connection flag
			//then needs to delete itself. 
			if (isBranch){
				//System.out.println("Remove this branch socket!!!");
				getConnectionNode().removeConnectionSocket(StandardConnectionSocket.this);
			}
			break;
		}
		super.setConnectionStatus(type, connectionShape);
	}

	/**
	 * Line which connects a plug to a socket if the plug comes close. 
	 * @return line which appears when socket comes into contact with connection shape. 
	 */
	public Line getCanConnectLine(){
		return canConnectLine; 
	}

	/**
	 * Get all connection lines associated with a ConnectionSocket. 
	 * @return array of all connection lines associated with a ConnectionSocket. 
	 */
	public 	ArrayList<ConnectionLine> getSocketConnectionLines() {
		return socketConnectionLines;
	}

	/**
	 * Set all connection lines associated with a ConnectionSocket. A socket creates its' own connection lines by default.
	 * @return  array of all connection lines associated with a ConnectionSocket. 
	 */
	@SuppressWarnings("unlikely-arg-type")
	public void setSocketConnectionLines(ArrayList<ConnectionLine> connectionLines) {
		getConnectionNode().getChildren().remove(socketConnectionLines);
		socketConnectionLines=connectionLines;
	}

	@Override
	public ArrayList<ConnectorNode> getPossibleConnectionShapes() {
		return getConnectionNode().getConnectionPane().getConnectionPlugs(getConnectionNode()); //FIXME-round about way
	}


	/**
	 * Check whether the socket is a branch socket or not. 
	 * @return true if a branch socket. 
	 */
	public boolean isBranch() {
		return isBranch;
	}

	/**
	 * Set whether the socket is a branch socket or not. Branch sockets are connected to ConnectionLines rather than the socket of 
	 * a ConnectionNode.
	 * @param branch true of a branch socket. 
	 */
	public void setBranch(boolean branch) {
		this.isBranch=branch;
	}

	/*
	 * The connection line the branch socket is attached to. Note this will be null if not a branch socket. 
	 */
	public ConnectionLine getParentConnectionLine(){
		return connectionLine;
	}

	/**
	 * Get the connection plug that this branch socket is attached to. 
	 * Note :this is not the plug connected to the socket but the plug attached to the connection line which the socket attaches to. 
	 * Use this to trace the socket connection to other ConnectionNodes. 
	 * @return the plug the socket is attached (not connected) to. 
	 */
	public StandardConnectionPlug getParentConnectionPlug(){
		return connectionPlug;
	}

	/**
	 * Set the plug and lines to show an error.
	 **/
	private void setSocketErrorAppearance(){
		this.setStroke(errorColour.brighter());
		this.setFill(errorColour);	
	}

	/**
	 * Set the plug and lines to normal appearance. 
	 */
	private void setSocketNormalAppearance() {
		this.setStroke(getNormalColour().darker());
		this.setFill(getNormalColour());
	}

	@Override
	public boolean isError() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setError(boolean isError) {
		this.isError = isError;
		if (isError) setSocketErrorAppearance();
		else setSocketNormalAppearance();
		for (int i=0; i<this.getSocketConnectionLines().size(); i++){
			this.getSocketConnectionLines().get(i).setError(isError);
		}
	}

}

