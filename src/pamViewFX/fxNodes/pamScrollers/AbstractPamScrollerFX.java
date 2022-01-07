package pamViewFX.fxNodes.pamScrollers;

import java.io.Serializable;
import java.util.Optional;

import javax.swing.SwingUtilities;

import PamController.PamController;
import pamScrollSystem.AbstractPamScroller;
import pamScrollSystem.LoadOptionsDialog;
import pamScrollSystem.PamScrollerData;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamSettingsDialogFX;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.layout.Pane;

/**
 * A scroller UI interfacing with PAMGUARD programmed using JavaFX.
 * @author Jamie Macaulay
 *
 */
public abstract class AbstractPamScrollerFX extends AbstractPamScroller implements Serializable {
		
	private static final long serialVersionUID = 1L;
	
	/**
	 * Button which pages backwards in time in viewer mode
	 */
	private PamButton pageForwardButton;
	
	/**
	 * Button which pages backwards in time in viewer mode
	 */
	private PamButton pageBackwardButton;
	
	/**
	 * Button which open up navigation dialog. 
	 */
	private PamButton settingsButton;

	/**
	 * Orientation of the scroll bar.
	 */
	protected Orientation orientation; 
	
	/**
	 * The navigation dialog. 
	 */
	private NavigationDialog navigationdialog;
	
	//private Color iconColor=Color.BLUE;

	
	public AbstractPamScrollerFX(String name, Orientation orientation, int stepSizeMillis, long defaultLoadTime, boolean hasMenu) {
		super(name, stepSizeMillis, stepSizeMillis, defaultLoadTime, hasMenu);
		this.orientation=orientation;
		createNavigationButtons();
	}
	
	
	/**
	 * Create navigation buttons. 
	 */
	private void createNavigationButtons(){
				
		settingsButton=new PamButton();
		//canvas=PamSymbolFX.createCanvas(PamSymbol.SYMBOL_TRIANGLED, Color.DARKSLATEGREY, Color.DARKSLATEGREY, 18, 18);//
//		settingsButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.MENU, PamGuiManagerFX.iconSize));
		settingsButton.setGraphic(PamGlyphDude.createPamIcon("mdi2m-menu", PamGuiManagerFX.iconSize));
		settingsButton.setOnAction(new EventHandler<ActionEvent>() {
		    @Override public void handle(ActionEvent e) {
		    	openTimeNavigationDialogAWT();
		    }
		});
	
		pageForwardButton=new PamButton();
		//Canvas canvas=PamSymbolFX.createCanvas(PamSymbol.SYMBOL_DOUBLETRIANGLER, Color.BLUE, Color.BLUE, 18, 18);
//		pageForwardButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.FAST_FORWARD, PamGuiManagerFX.iconSize));
		pageForwardButton.setGraphic(PamGlyphDude.createPamIcon("mdi2f-fast-forward", PamGuiManagerFX.iconSize));
		pageForwardButton.setOnAction((action)->{
			pageForward();
		});
		
		pageBackwardButton=new PamButton();
		//canvas=PamSymbolFX.createCanvas(PamSymbol.SYMBOL_DOUBLETRIANGLEL, Color.BLUE, Color.BLUE, 18, 18);//
//		pageBackwardButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.FAST_REWIND, PamGuiManagerFX.iconSize));
		pageBackwardButton.setGraphic(PamGlyphDude.createPamIcon("mdi2r-rewind", PamGuiManagerFX.iconSize));
		pageBackwardButton.setOnAction((action)->{
			pageBack();
		});
		
		pageForwardButton.getStyleClass().add("opaque-button-square"); 
		pageBackwardButton.getStyleClass().add("opaque-button-square"); 
		settingsButton.getStyleClass().add("opaque-button-square"); 
	}
	
	/**
	 * Create the navigation menu based on whether the scroll bar is being used in real time or for viewing data. Can override this to add more controls. 
	 * @param isViewer -whether in viewer mode or not
	 * @param Orientation orientation -orientation of the scroll bar
	 * @return pane holding controls for time navigation. 
	 */
	public Pane createNavigationPane(boolean isViewer, Orientation orientation){
			
		Pane holder;
		if (orientation==Orientation.HORIZONTAL){
			holder = new PamHBox();
			//make sure buttons resize properly with scroll pane
			pageForwardButton.prefHeightProperty().bind(holder.heightProperty());
			pageBackwardButton.prefHeightProperty().bind(holder.heightProperty());
			settingsButton.prefHeightProperty().bind(holder.heightProperty());

		}
		else {
			holder = new PamVBox();
			//make sure buttons resize properly with scroll pane
			pageForwardButton.prefWidthProperty().bind(holder.widthProperty());
			pageBackwardButton.prefWidthProperty().bind(holder.widthProperty());
			settingsButton.prefWidthProperty().bind(holder.widthProperty());
		}
		
		holder.getChildren().addAll(pageBackwardButton,settingsButton,pageForwardButton);
		
		return holder;
		
	}
	
	/**
	 * Opens the AWT time navigation dialog. 
	 */
	public void openTimeNavigationDialogAWT(){
		SwingUtilities.invokeLater(()-> {
			PamScrollerData newData = LoadOptionsDialog.showDialog(null, this, null);
			if (newData != null) {
				scrollerData = newData;
				rangesChangedF(getValueMillis());
			}
		});
	
	}
	

	/**
	 * Open the time navigation dialog. 
	 */
	public void openTimeNavigationDialogFX(){
		
		//the navigation dialog is really an internal pane. 
		if (this.navigationdialog==null){
			navigationdialog=new NavigationDialog(this);
		}
		
		navigationdialog.setParams(this.getScrollerData());
		PamSettingsDialogFX<?> settingsDialog=new PamSettingsDialogFX(navigationdialog); 
		settingsDialog.getDialogPane().getStylesheets().add(PamController.getInstance().getGuiManagerFX().getPamSettingsCSS());
		Optional<?> newData=settingsDialog.showAndWait();	

        //PamScrollerData newData=NavigationDialog.showDialog(null, false, StageStyle.UNDECORATED, this);
		if (newData != null && newData.isPresent()) {
			scrollerData = (PamScrollerData) newData.get();
			rangesChangedF(getValueMillis());
		}
	}

	/**
	 * Get the orientation of the scroll bar.
	 * @return orientation of the scroll bar. Either horizontal or vertical. 
	 */
	public Orientation getOrientation() {
		return orientation;
	}
	
	/**
	 * Get the settings button which opens the navigation dialog.
	 * @return the button which opens the navigation dialog. 
	 */
	public PamButton getSettingsButton() {
		return settingsButton;
	}
	
	/**
	 * Get the page forward button used in viewer mode. 
	 * @return
	 */
	public PamButton getPageForwardButton() {
		return pageForwardButton;
	}


	/**
	 * Get the page backward button
	 * @return
	 */
	public PamButton getPageBackwardButton() {
		return pageBackwardButton;
	}

	
	/**
	 * Get the node which contain all controls for scrolling system. 
	 * @return the FX component to go into the GUI. 
	 */
	public abstract Pane getNode();


	@Override
	public boolean isShowing() {
		Pane pane = getNode();
		if (pane == null) {
			return false;
		}
		else {
			return pane.isVisible();
		}
	}
	
}