package userDisplayFX;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import javafx.embed.swing.SwingNode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import pamViewFX.fxNodes.internalNode.PamInternalPane;

/**
 * A user display which is based on a swing component. 
 * @author Jamie Macaulay
 *
 */
public class SwingDisplayNodeFX implements UserDisplayNodeFX{

	/**
	 * The name of the display
	 */
	private String name;
	
	/**
	 * The swing component to add.
	 */
	private JComponent jComponenet;

	/**
	 * The node holding the swing componenent
	 */
	private StackPane swingNode;


	public SwingDisplayNodeFX(JComponent jComponenet, String name){
		this.name=name;
		this.jComponenet=jComponenet;
		swingNode=createSwingNode( jComponenet);
		
		//need to tell the swing component to repaint when size changes. This has to be done on AWT thread
		swingNode.widthProperty().addListener((change)->{

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					jComponenet.repaint();
				}
			});
		});
		
		swingNode.heightProperty().addListener((change)->{

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					jComponenet.repaint();
				}
			});
		});
	}
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public Region getNode() {
		return swingNode;
	}

	@Override
	public void openNode() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isStaticDisplay() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isResizeableDisplay() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean requestNodeSettingsPane() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void closeNode() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyModelChanged(int changeType) {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
	 * Create the JavaFX node whihc holds the swing component. 
	 * @return JavFX node with swing inside....
	 */
	private StackPane createSwingNode(JComponent swingComponent){
		  final SwingNode swingNode = new SwingNode();

	        StackPane pane = new StackPane();
	        pane.getChildren().add(swingNode);
	        
	        SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                swingNode.setContent(swingComponent);
	            }
	        });
	        
	        return pane; 
	}

	@Override
	public boolean isMinorDisplay() {
		return false;
	}

	@Override
	public UserDisplayNodeParams getDisplayParams() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFrameHolder(PamInternalPane internalFrame) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public UserDisplayControlFX getUserDisplayControl() {
		// TODO Auto-generated method stub
		return null;
	}

}