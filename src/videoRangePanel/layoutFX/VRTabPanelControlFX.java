package videoRangePanel.layoutFX;

import java.awt.Frame;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import pamViewFX.fxStyles.PamStylesManagerFX;
import videoRangePanel.VRControl;
import videoRangePanel.VRPane;
import videoRangePanel.VRTabPane;

/**
 * Create a JavaFX GUI within the current swing framework. 
 * @author Jamie Macaulay
 */
public class VRTabPanelControlFX extends VRTabPane {
	
	/**
	 * 
	 */
	private JFXPanel jfxPanel;
	
	/**
	 * Reference to the main FX display pane. 
	 */
	private VRDisplayFX2AWT vRDisplay;

	/**
	 * 
	 * The vr control. 
	 */
	private VRControl vRControl; 
	
	/**
	 * Constructor for the tab panel.  
	 */
	public VRTabPanelControlFX(VRControl vRControl){
		this.vRControl= vRControl; 
		vRDisplay = new VRDisplayFX2AWT(vRControl); 
	}
	
	
	@Override
	public JMenu createMenu(Frame parentFrame) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				
			}
		});		
		return null; 
	}
	
	//initiate the JavaFX stuff. 
    private void initFX(JFXPanel fxPanel) {
    	
        //This method is invoked on JavaFX thread
        Scene scene = createScene();
        vRDisplay.getPane().prefWidthProperty().bind(scene.widthProperty()); 
        vRDisplay.getPane().prefHeightProperty().bind(scene.heightProperty()); 
        vRDisplay.getPane().bindTest(); 
        
//        //Add listener
//        vRDisplay.getPane().widthProperty().addListener((obs, oldval, newval)->{
//			System.out.println("Hello VRDisplayWidth:  "+ vRDisplay.getPane().getWidth() + 
//					" scene width: " + scene.getWidth() + " fxPanel: "+ fxPanel.getWidth() + 
//					" vr max width: " + vRDisplay.getPane().getMaxWidth());
//		});

        fxPanel.setScene(scene);
    }
    
    /**
     * Create the scene etc. for JavaFX 
     * @return
     */
    private Scene createScene(){
		Group root=  new  Group();
		Scene scene  =  new  Scene(root, Color.GRAY);
		scene.getStylesheets().clear();
		scene.getStylesheets().add(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getGUICSS()); 
		
		vRDisplay = new VRDisplayFX2AWT(vRControl);
		vRDisplay.createPane(); 
		root.getChildren().add(vRDisplay.getPane()); 
		
		return scene;
    }

    @Override
    public JComponent getPanel() {
    	if (jfxPanel==null){
    		jfxPanel = new JFXPanel();
    		Platform.runLater(new Runnable() {
    			@Override
    			public void run() {
    				initFX(jfxPanel);
    			}
    		});		
    	}
    	return  jfxPanel; 
    }

	@Override
	public JToolBar getToolBar() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public VRPane getVRPane() {
		return vRDisplay;
	}
	
	@Override
	public void update(int updateType) {
		super.update(updateType);
		vRDisplay.update(updateType); 
	}
	
	/**
	 * Open an image file (an image or video)
	 * @param file - the file to open. 
	 * @return true if the file has been opned successfully
	 */
	@Override
	public boolean openImageFile(File file) {
		return vRDisplay.openImageFile(file);
	}

}
