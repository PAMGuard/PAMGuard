package dataModelFX.connectionNodes;

import java.util.ArrayList;

import pamViewFX.fxNodes.connectionPane.StandardConnectionPlug;
import pamViewFX.fxNodes.connectionPane.StandardConnectionSocket;
import pamViewFX.fxNodes.connectionPane.ConnectionNode;
import pamViewFX.fxNodes.connectionPane.ConnectionPane;
import pamViewFX.fxNodes.connectionPane.ConnectorNode;
import userDisplayFX.UserDisplayControlFX;
import javafx.scene.paint.Color;
import PamController.PamControlledUnit;
import PamguardMVC.PamDataBlock;
import dataModelFX.DataModelConnectPane;
import dataModelFX.DataModelStyle;

/**
 * The display connection node changes the colour of connection lines which 
 * connect from parent nodes. It also enables branch sockets on some display.
 * 
 * @author Jamie Macaulay
 *
 */
public class DisplayConnectionNode extends ModuleConnectionNode {
	
	public StandardConnectionPlug lastConnectedPlug = null; 
	
	public boolean allowMultipleParent=false; //true to allow branch sockets. 

	public DisplayConnectionNode(DataModelConnectPane connectionPane) {
		super(connectionPane);
				
		//add a new connection listener 
		this.addConnectionListener((shape, foundShape, type)->{
			connectionListenerTriggered(shape, foundShape, type);
		});
		
		this.setAllowBranchSockets(false); //remember, it's the parent node plug line that has the branch sockets, not this node. 
		this.setAutoConnect(false); 
	}
	
	@Override
	protected void connectionListenerTriggered(ConnectorNode shape, ConnectorNode foundShape, int type){
//		System.out.println(" DisplayConnectionNode connection listener " +this.getPamControlledUnit()); 

		switch(type){
		case ConnectorNode.NO_CONNECTION:
			if (lastConnectedPlug!=null && lastConnectedPlug.getConnectedShape()==null){
				//want to remove any branch sockets which currently exist -otherwise have sockets which connect to nothing. 
				setLineDisplayColor(lastConnectedPlug, DataModelStyle.moduleLines);
				setAllowBranchSocket(lastConnectedPlug, false);
				lastConnectedPlug=null;
			}
			pamModeltoDataModel();
			break; 
		case ConnectorNode.POSSIBLE_CONNECTION:
			//System.out.println(" DisplayConnectionNode. POSSIBLE_CONNECTION " +shape+"  " +foundShape ); 
			break; 
		case ConnectorNode.CONNECTED:
			//System.out.println("DisplayConnectionNode. CONNECTED " + this.getPamControlledUnit()); 
			if (shape instanceof StandardConnectionSocket){
				if (!((StandardConnectionSocket) shape).isBranch()){
					lastConnectedPlug=(StandardConnectionPlug) shape.getConnectedShape();
					setAllowBranchSocket(lastConnectedPlug, allowMultipleParent);
				}
			}
			//printCompatibleDataUnits();
			checkLineColours(DataModelStyle.displayLines);
			pamModeltoDataModel();
			break; 
		}
		//BUG IN CHECK CONNECTIONS - LINE. 246 (Fixed but keep in mind if 'processor leak' type error occurs)
	}

	private void pamModeltoDataModel() {
		super.getDataModelConnectionPane().pamModeltoDataModel();
		
	}

	/**
	 * Set all lines connecting a plug to node to accept/reject branch sockets
	 * @param connectedPlug - the plug to which the lines belong
	 * @param allow - true to allow branch sockets. False to reject branch sockets. 
	 */
	public void setAllowBranchSocket(StandardConnectionPlug connectedPlug, boolean allow){
		if (!allow) connectedPlug.getConnectionNode().removeAllBranchSockets(); 
		//System.out.println("Plug connection lines size "+connectedPlug.getPlugConnectionLines().size());
		for (int i=0; i<connectedPlug.getPlugConnectionLines().size(); i++){
			connectedPlug.getPlugConnectionLines().get(i).setAllowBranchSockets(allow);
		}
	}
	
	/**
	 * Displays have different input line colours than other modules. 
	 * Checks all lines in the ConnectionPane and colours accordingly
	 */
	protected void checkLineColours(Color displayColor){
		//now colour the lines connecting to the display node another colour.
		ArrayList<ConnectionNode> allNodes=this.getConnectionPane().getConnectionNodes();
		ArrayList<ConnectionNode> parents = this.getConnectionPane().getParentConnectionNodes(this);
		StandardConnectionPlug connectionPlug;
		
		for (int i=0; i<allNodes.size(); i++){
			this.getConnectionPane();
			connectionPlug=(StandardConnectionPlug) ConnectionPane.getConnectionPlug(this, allNodes.get(i), true, true);
			if (parents.contains(allNodes.get(i))){
				//set colour to display colour but don't want to override error appearance.
				setLineDisplayColor(connectionPlug, displayColor);
			}
			else{
				//otherwise set to the default colour
				setLineDisplayColor(connectionPlug, DataModelStyle.moduleLines);
			}
			
		}
	}
	
	
	private void setLineDisplayColor(StandardConnectionPlug connectedPlug, Color color){
		
		if (connectedPlug==null ){
			//System.out.println("DisplayConnectionNode: setLineDisplayColor: No connection plug found. ");
			return;
		}

		for (int i=0; i<connectedPlug.getPlugConnectionLines().size(); i++){
			if (!connectedPlug.getPlugConnectionLines().get(i).isError()) connectedPlug.getPlugConnectionLines().get(i).setNormalColor(color);
		}
		
		ArrayList<StandardConnectionSocket> branchSockets=connectedPlug.getBranchSockets();
		StandardConnectionPlug connectedShape;
		for (int i=0; i<branchSockets.size(); i++){
			
			//colour the branch socket lines
			for (int j=0; j<branchSockets.get(i).getSocketConnectionLines().size(); j++){
				if (!branchSockets.get(i).getSocketConnectionLines().get(j).isError()) branchSockets.get(i).getSocketConnectionLines().get(j).setNormalColor(color);
			}
					
			//colour the plug connecting to the branch socket
			connectedShape=(StandardConnectionPlug) branchSockets.get(i).getConnectedShape();
			if (connectedShape!=null){
				for (int j=0; j<connectedShape.getPlugConnectionLines().size(); j++){
					if (!connectedShape.getPlugConnectionLines().get(j).isError()) connectedShape.getPlugConnectionLines().get(j).setNormalColor(color);
				}
			}
		}
	}
	
	/**
	 * Print out compatible data units for the module. 
	 */
	public void printCompatibleDataUnits(){
		if (this.getPamControlledUnit()==null) System.out.println("DisplayConnectionNode: PamControlled unit is null");
		else {
			for (int i=0; i<this.getPamControlledUnit().getPamProcess(0).getCompatibleDataUnits().size(); i++){
				System.out.println(this.getPamControlledUnit().getPamProcess(0).getCompatibleDataUnits().get(i).getName());
			}
		}
	}
	
	@Override
	public PamDataBlock getPrefferedParent(PamControlledUnit parentControlledUnit) {
		/**
		 * This is quite important for the display node as it accepts pretty much every
		 * type of data. So, for example, the whistle and moan detector has an FFT noise
		 * free data output. In general a user probably would probably want the
		 * connected region data block as the default parent. Thus the preferred data
		 * block should be the connected region data block.
		 */
//		if (parentControlledUnit instanceof WhistleMoanControl){
//			return ((WhistleMoanControl) parentControlledUnit).getWhistleToneProcess().getOutputData();
//		}
		return null;
	}
	
	@Override
	public boolean hasInput(PamControlledUnit pamControlledUnit){
		return true; 
	}
	
	@Override
	public void setPamControlledUnit(PamControlledUnit pamControlledUnit){
		super.setPamControlledUnit(pamControlledUnit);
		//also create reference to the display
		if (pamControlledUnit instanceof UserDisplayControlFX){
			if (((UserDisplayControlFX) pamControlledUnit).isMultiParent()){
				this.allowMultipleParent=true;
			}

		}
	}
	
	/**
	 * Check whether the display node can accept more than one parent node at once. 
	 * @return true if more than one parent node is allowed 
	 */
	public boolean isAllowMultipleParent() {
		return allowMultipleParent;
	}

	/**
	 * Set whether the display node can accept more than one parent node at once. 
	 * @param allowMultipleParent true if more than one parent node is to be allowed. 
	 */
	public void setAllowMultipleParent(boolean allowMultipleParent) {
		this.allowMultipleParent = allowMultipleParent;
	}
	


}
