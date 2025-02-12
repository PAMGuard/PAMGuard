package PamView.dialog.warn;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;

import PamView.help.PamHelp;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Window;
import pamViewFX.fxNodes.PamHBox;

/**
 * A warning dialog in JavaFX with a "do not show again" dialog box. 
 * @author Jamie Macaulay
 *
 */
public class WarnOnceDialogFX {

	/**
	 * The alert dialog. 
	 */
	private Alert alert;

	/**
	 * The answer from the dialog. 
	 */
	private int answer;

	/**
	 * The type of alert dialog. 
	 */
	private AlertType alertType;

	/**
	 * The throwable error
	 */
	private Throwable error;

	/**
	 * The warning message
	 */
	private String message;

	/**
	 * The opt out this session check box. 
	 */
	private CheckBox dontShowAgainThisSess;

	/**
	 * The opt out forever check box. 
	 */
	private CheckBox dontShowAgain;

	private HBox checkBoxPane;

	private ButtonType helpButton;

	private String helpPoint;  

	/**
	 * Constructor the warn once dialog. 
	 * @param parentStage - the parent window
	 * @param title
	 * @param message
	 * @param alertType
	 * @param helpPoint
	 * @param error
	 */
	public WarnOnceDialogFX(Window parentStage, String title, String message, AlertType alertType , String helpPoint, Throwable error) { 
		this(parentStage, title, message, alertType, helpPoint, error, null, null, false);
	}

	/**
	 * Constructor the warn once dialog. 
	 * @param parentStage - the parent window
	 * @param title - the title 
	 * @param message - the message 
	 * @param alertType - the aleret 
	 * @param helpPoint
	 * @param error
	 * @param oktext
	 * @param canceltext
	 */
	public WarnOnceDialogFX(Window parentStage, String title, String message, AlertType alertType , String helpPoint, Throwable error, String oktext, String canceltext) {
		this(parentStage, title, message, alertType, helpPoint, error, oktext, canceltext, false);
	}

	public WarnOnceDialogFX(Window parentStage, String title, String message, AlertType alertType , String helpPoint,
			Throwable error, String oktext, String canceltext, boolean disableCheckBoxes) {

		this.error=error; 
		this.message=message; 
		this.helpPoint=helpPoint; 
		
		ButtonType okButton = ButtonType.OK; 
		ButtonType cancelButton = ButtonType.CANCEL; 
		
		ArrayList<ButtonType> buttons = new ArrayList<ButtonType>(); 
	
		//figure out what the ok and cancel buttons should be
		switch (alertType) {
		case CONFIRMATION:
			okButton = ButtonType.YES;
			cancelButton = ButtonType.NO;
			break;
		case ERROR:
			break;
		case INFORMATION:
			cancelButton = null;
			break;
		case NONE:
			break;
		case WARNING:
			cancelButton = new ButtonType("Copy to clipboard"); 
			break;
		default:
			break;
		}
		
		//Override the cancel on OK buttons if set
		if (oktext!=null) { 
			okButton = new ButtonType(oktext); 
		}

		if (canceltext!=null) { 
			cancelButton = new ButtonType(canceltext); 
		}
		
		//add the buttons
		buttons.add(okButton);
		if (cancelButton!=null) buttons.add(cancelButton);
		
		//add additional buttons. 
		if (helpPoint!=null) {
			helpButton = new ButtonType("Help..."); 
			buttons.add(helpButton);
		}
		
//		System.out.println("Buttons: " + buttons.size());
		
		alert = createAlertWithOptOut(alertType, title, null, message, "Don't show this message again", buttons.toArray(new ButtonType[buttons.size()]));
		
		//System.out.println("CREATE ALERT: " + disableCheckBoxes); 
		this.disableCheckBoxes(disableCheckBoxes);
		dontShowAgainThisSess.setSelected(false);

	}
	


	/**
	 * Show the warning dialog. 
	 */
	public void showDialog() {
		//System.out.println("SHOW DIALOG: " +this); 
		
		Optional<ButtonType> type = alert.showAndWait();
		
		if (type.filter(t -> t == ButtonType.YES).isPresent()) {
			//System.out.println("OK_OPTION: " +this); 
			answer=WarnOnce.OK_OPTION; 
		}
		else if (type.filter(t -> t == helpButton).isPresent()) {
			//System.out.println("HELP: " +this); 
			answer = WarnOnce.OK_OPTION;
			helpButtonPressed(); 
		}
		else {
			//System.out.println("CANCEL_OPTION: " +this); 
			answer = WarnOnce.CANCEL_OPTION;
			cancelButtonPressed(); 
		}
	}



	/**
	 * If the help button is pressed then we send the user to a help point or a website
	 */
	private void helpButtonPressed() {
		
		if (check_URL(helpPoint)) {
			//go to a websitE
			try {
				java.awt.Desktop.getDesktop().browse( new URL(helpPoint).toURI());
			} catch (IOException | URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			//GO TO pamgUARD HELP,POINT
			PamHelp.getInstance().displayContextSensitiveHelp(helpPoint);
		}
	}
	
	  public static boolean check_URL(String str) {
		    try {
		      new URL(str).toURI();
		      return true;
		    } catch (Exception e) {
		      return false;
		    }
		  }

	/**
	 * If the cancel button is pressed, check if this is a warning message.  If it is, then the cancel button is really
	 * the copy-to-clip board button.  In that case, copy the message (and the error message, if available) to the clip board
	 */
	public void cancelButtonPressed() {
		if (alertType == AlertType.WARNING) {
			String fullString = message.replaceAll("<p>", "\n");
			if (error != null) {
				fullString += "\n" + error.getClass().getSimpleName() + "\n" + error.getMessage() + "\n";
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				error.printStackTrace(pw);
				fullString += sw.toString(); // stack trace as a string
			}
			StringSelection stringSelection = new StringSelection(fullString);
			Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			clpbrd.setContents(stringSelection, null);
		}
	}

	public boolean isShowAgain() {
		return dontShowAgain.isSelected() == false;
	}

	public Boolean isShowThisSess() {
		return dontShowAgainThisSess.isSelected() == false;
	}

	/**
	 * Create an alert dialog. 
	 * @param type - the type of alert. 
	 * @param title - title of the alert
	 * @param headerText - the header text of the warning
	 * @param message - the warning message
	 * @param optOutMessage - the message for the check box
	 * @param buttonTypes - the button type to use. 
	 * @return
	 */
	private Alert createAlertWithOptOut(AlertType type, String title, String headerText, 
			String message, String optOutMessage,
			ButtonType... buttonTypes) {

		Alert alert = new Alert(type);
		alert.initModality(Modality.WINDOW_MODAL);
		// Need to force the alert to layout in order to grab the graphic,
		// as we are replacing the dialog pane with a custom pane
		alert.getDialogPane().applyCss();
		Node graphic = alert.getDialogPane().getGraphic();

		// Create a new dialog pane that has a checkbox instead of the hide/show details button
		// Use the supplied callback for the action of the checkbox
		alert.setDialogPane(new DialogPane() {

			@Override
			protected Node createDetailsButton() {

				dontShowAgain = new CheckBox();
				dontShowAgain.setText(optOutMessage + " ever");

				dontShowAgainThisSess = new CheckBox();
				dontShowAgainThisSess.setText(" this session ");

				checkBoxPane = new PamHBox(); 
				checkBoxPane.getChildren().addAll(dontShowAgain, dontShowAgainThisSess); 
				checkBoxPane.setSpacing(5); 
				checkBoxPane.setAlignment(Pos.CENTER_LEFT);

				return checkBoxPane;
			}
		});
		
		alert.getDialogPane().getButtonTypes().addAll(buttonTypes);
		
		
		alert.getDialogPane().setContentText(message);
		// Fool the dialog into thinking there is some expandable content
		// a Group won't take up any space if it has no children
		alert.getDialogPane().setExpandableContent(new Group());
		alert.getDialogPane().setExpanded(true);
		// Reset the dialog graphic using the default style
		alert.getDialogPane().setGraphic(graphic);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		

		return alert;
	}

	/**
	 * Get the answer from the warning dialog. 
	 * @return the awarning answer. 
	 */
	public int getAnswer() {
		return answer;
	}


	public void disableCheckBoxes(boolean disable) {
		checkBoxPane.setDisable(disable);
		dontShowAgain.setDisable(disable);
		dontShowAgainThisSess.setDisable(disable);
	}



}
