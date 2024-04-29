package pamViewFX.fxNodes.pamDialogFX;

import java.util.Optional;

import dataPlotsFX.FXIconLoder;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;


/**
 * Creates a dialog with some useful PAMGUARD customisation.
 * 
 * @author Jamie Macaulay
 *
 */
public abstract class PamDialogFX<T> extends Dialog<T> {
	
	private Point2D openingLoc=null;
	
	/**
	 * Creates a standard dialog. This does not call getParams() on close. 
	 */
	public PamDialogFX() {
		super();
		setDefaultIcon();
		showingProperty().addListener((observable, oldValue, newValue) ->{
			if (openingLoc!=null){
//				System.out.println("X is "+openingLoc.getX()+" Y is: "+openingLoc.getY()); 
				this.setX(openingLoc.getX());
				this.setY(openingLoc.getY());
			}
		});
	}

	/**
	 * Creates a PAM settings dialog. On closing the dialog will call getParams(). If getParams()==null then the dialog assumes
	 * an error has occurred and will close. 
	 * @param window - the owner window. 
	 * @param title - title of dialog  
	 * @param style - style of dialog 
	 */
	public PamDialogFX(Window window, String title, StageStyle style) {
		super();
		
		//set title
		setTitle(title);
		setDefaultIcon();

		
		//set owner- in future java releases (likely 9+) this will be used for internal/lightweight dialogs 
		if (window!=null) initOwner(window);
		
		//create he dialog pane.
		DialogPane dialogPane=new DialogPane();
		dialogPane.getButtonTypes().addAll(ButtonType.OK,  ButtonType.CANCEL);
		
		showingProperty().addListener((observable, oldValue, newValue) ->{
			if (openingLoc!=null){
//				System.out.println("X is "+openingLoc.getX()+" Y is: "+openingLoc.getY()); 
				this.setX(openingLoc.getX());
				this.setY(openingLoc.getY());
			}
		});
		
		//set style
		this.initStyle(style);
		
		//set results converter
		this.setResultConverter(dialogButton -> {
		    if (dialogButton == ButtonType.OK) {
		        return getParams();
		    }
		    return null;
		});
	

		//set the dialog pane inside the dialog
		this.setDialogPane(dialogPane);

		//add listener to prevent close request if the dialog
		final Button btOk = (Button) this.getDialogPane().lookupButton(ButtonType.OK);
		btOk.addEventFilter(ActionEvent.ACTION, event -> {
			if (getParams()==null) {
				event.consume();
				showParamsWarning();
			}
		});

	}
	
	/**
	 * Set a default PAMguard icon on the dialog. 
	 */
	private void setDefaultIcon() {
		try {
			Image icon = FXIconLoder.createImage("Resources/pamguardIcon.png");
			Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
			stage.getIcons().add(icon);
		}
		catch (Exception e) {
			System.err.println("Unable to set dialog icon");
		}
	}
	
	/**
	 * 	Shows a warning message when getParams() returns null. Can be overriden to show specific error message
	 */
	public void showParamsWarning(){
		PamDialogFX.showWarning(null, "Dialog unable to close", "The dialog was unable to close due to an error");
	}
	

	/**
	 * Convenience class to show dialog 
	 * @param input-input class- usually contains params for the dialog.
	 * @param p2d - point on the screen in pixels to open dialog (not point within owner)
	 * @return
	 */
	public T showDialog(T input, Point2D p2d) {

		this.setOpeningLocation(p2d);
		this.setParams(input);

		//open dialog and wait for response
		Optional<T> result = this.showAndWait();

		if (result.isPresent()) return result.get();
		else return null; 

	}

	/**
	 * Optional function used to set controls in the dialog to the correct setting for input params.
	 * @param input- the input params. 
	 */
	public abstract void setParams(T input);
		
	
	/**
	 * This is called whenever the dialog is successfully closes. 
	 * @return the new params 
	 */
	public abstract T getParams();
	
	
	/**
	 * Convenience class to set dialog. et the content main content node. 
	 * @param content- content for main content node. 
	 */
	public void setContent(Node content){
		getDialogPane().setContent(content);		
	}
	
	/**
	 * Set expandable content. This lies in a hiding pane below the main content pane and can be expanded by user.
	 * @param content. Node to sit in expanded content node. 
	 */
	public void setExpandableContent(Node content){
		getDialogPane().setExpandableContent(content);		
	}
	
	/**
	 * Set the opening location of the dialog. 
	 * @param p2D. The opening location of the top left corner of the dialog in screen co-ordinates. Set null for default behaviour. 
	 */
	public void setOpeningLocation(Point2D p2d){
		this.openingLoc=p2d;
	}
	
	/**
	 * Show a message dialog
	 * @param title - title of message dialog 
	 * @param content - content of dialog
	 */
	public static boolean showMessageDialog(Window owner, String title, String content, ButtonType yesButton, ButtonType noButton, AlertType alertType){		
		Alert alert = new Alert(alertType, 
				content,
	             yesButton, 
	             noButton); 
		alert.initOwner(owner);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.showAndWait();
		return alert.getResult()==yesButton; 
	}
	
	
	/**
	 * Show a message dialog with default OK and Close buttons. 
	 * @param title - title of message dialog 
	 * @param content - content of dialog
	 * @param alertType - the alert type. 
	 */
	public static boolean showMessageDialog(Window owner, String title, String content, AlertType alertType){
		return showMessageDialog(owner, title, content,ButtonType.OK, ButtonType.CLOSE, alertType); 
	}
	
	/**
	 * Show a warning message dialog
	 * @param title - title of message dialog 
	 * @param content - content of dialog
	 */
	public static boolean showWarning(Stage guiFrame, String title, String content) {
		return showMessageDialog(guiFrame, title, content, AlertType.WARNING); 
	}
	
	/**
	 * Show a message dialog
	 * @param title - title of message dialog 
	 * @param content - content of dialog
	 */
	public static boolean showMessageDialog(Window owner, String title, String content){
		return showMessageDialog(owner, title, content, AlertType.INFORMATION); 
	}
	
	/**
	 * Show a message dialog
	 * @param title - title of message dialog 
	 * @param content - content of dialog
	 */
	public static boolean showMessageDialog(String title, String content){
		return showMessageDialog(null, title,  content);
	}
	
	/**
	 * Pack the dialog. This may not be necessary in future releases of fx and is a bit clumsy really. 
	 */
	public void pack(){
		//TODO- this is a bit CUMBERSOME and maybe fixed in new version of JavaFX
		//need to get stage and resize because new controls will have been added. 
		Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
		stage.sizeToScene();
	}
	
	public static boolean showWarning(String content) {
		return showWarning(null, "Warning",  content);
	}

	/**
	 * Show error dialog with default OK and CANCEL. 
	 * @param content - the error message. 
	 */
	public static boolean showError(String content) {
		return showMessageDialog(null, "Error", content, AlertType.ERROR); 
	}
	
	/**
	 * Show error dialog with default OK and CANCEL. 
	 * @param content - the error message. 
	 */
	public static boolean showError(String title, String content) {
		return showMessageDialog(null, title, content, AlertType.ERROR); 
	}

	public static boolean showMessageDialog(Window owner, String string, String string2, ButtonType yes,
			ButtonType cancel) {
		showMessageDialog(owner,  string, string2, yes, cancel, AlertType.CONFIRMATION); // temp until Jamie provides corrected code. 
		return false;
	}
	

}
