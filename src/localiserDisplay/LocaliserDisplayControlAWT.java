package localiserDisplay;

import java.awt.Component;

import javax.swing.JComponent;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import localiserDisplay.layout.LocaliserDisplayFX;
import pamViewFX.fxStyles.PamStylesManagerFX;
import userDisplay.UserDisplayComponent;

/**
 * Control for the localiser display. The loclaiser display shows loclisation data on a 3D Map. 
 * It also allows users to localise 
 * @author macst
 *
 */
public class LocaliserDisplayControlAWT extends LoclaiserDisplayControl implements  UserDisplayComponent {


	public static final String unitType = "Map 3D";
	
	/**
	 * Loclaiser display provider
	 */
	private  LocaliserDisplayProvider localiserDsiplayProvider;

	private String uniqueDisplayName;
	
	/**
	 * The main javafx GUI Node 
	 */
	private LocaliserDisplayFX localiser;

	private JFXPanel jfxPanel;
	

	public LocaliserDisplayControlAWT(LocaliserDisplayProvider localiserDisplayPorivder, String uniqueDisplayName) {
		super(uniqueDisplayName); 
		this.localiserDsiplayProvider = localiserDisplayPorivder;
		setUniqueName(uniqueDisplayName);
	}


	public String getUniqueName() {
		return uniqueDisplayName;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueDisplayName = uniqueName;
	}


	@Override
	public Component getComponent() {
		return getPanel();
	}


	@Override
	public void openComponent() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void closeComponent() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void notifyModelChanged(int changeType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getFrameTitle() {
		return "Localiser Display";
	}
	

	//initiate the JavaFX stuff. 
    private void initFX(JFXPanel fxPanel) {
    	
        //This method is invoked on JavaFX thread
        Scene scene = createScene();
        localiser.prefWidthProperty().bind(scene.widthProperty()); 
        localiser.prefHeightProperty().bind(scene.heightProperty()); 

        fxPanel.setScene(scene);
    }
    
    /**
     * Create the scene etc. for JavaFX 
     * @return the scene for JFX
     */
    private Scene createScene(){
		Group root=  new  Group();
		Scene scene  =  new  Scene(root, Color.GRAY);
		scene.getStylesheets().clear();
		scene.getStylesheets().add(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getGUICSS()); 
		
		localiser = new LocaliserDisplayFX(this);
		root.getChildren().add(localiser); 
		
		return scene;
    }

    
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

}
