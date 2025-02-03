package PamView.dialog.warn;

import java.awt.Window;
import java.io.Serializable;
import java.util.Hashtable;

import javax.swing.JOptionPane;

import PamController.PamController;
import PamController.PamGUIManager;
import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;
import javafx.scene.control.Alert.AlertType;
import pamguard.GlobalArguments;

/**
 * Show a dialog which can display a warning message and can be told to 
 * never show again.  Contains mostly static methods to create the 
 * dialog panels. 
 * 
 * @author Doug Gillepsie
 *
 */
public class WarnOnce implements PamSettings {

	public static final int OK_CANCEL_OPTION = JOptionPane.OK_CANCEL_OPTION;
	public static final int YES_NO_OPTION = JOptionPane.YES_NO_OPTION;
	public static final int WARNING_MESSAGE = JOptionPane.DEFAULT_OPTION;
	public static final int OK_OPTION = JOptionPane.OK_OPTION;
	public static final int CANCEL_OPTION = JOptionPane.CANCEL_OPTION;
	
	private WarnOnceList warnOnceList = new WarnOnceList();
	private Hashtable<String, Boolean> showThisSess = new Hashtable<>();
	private static WarnOnce singleInstance;
	
	static {
		singleInstance = new WarnOnce();
	}
	
	private WarnOnce() {
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	/**
	 * Clear the entire list of hidden warnings. All warnings will then be displayed. 
	 */
	public static void clearHiddenList(Window parent) {
		int ans = showWarning(parent, "Warning Messages", "Show all PAMGuard warning messages", WarnOnce.OK_CANCEL_OPTION);
		if (ans == WarnOnce.CANCEL_OPTION) return;
		singleInstance.warnOnceList.clearList();
		singleInstance.showThisSess.clear();
	}
	
	@Override
	public String getUnitName() {
		return "WarningList";
	}
	@Override
	public String getUnitType() {
		return "WarningList";
	}
	@Override
	public Serializable getSettingsReference() {
		return warnOnceList;
	}
	@Override
	public long getSettingsVersion() {
		return WarnOnceList.serialVersionUID;
	}
	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.warnOnceList = ((WarnOnceList) pamControlledUnitSettings.getSettings());
		return true;
	}
	
	/**
	 * Show a warning message in either a Swing or JavaFX dialog. The current GUI type automatically chooses
	 * the dialog type. 
	 * @param title title of warning.
	 * @param message warning message (use HTML for multi-line messages).
	 * @param messageType message type OK_CANCEL_OPTION or WARNING_MESSAGE. 
	 * @param helpPoint Pointer into help file for additional information.
	 * @param error an error that may have caused this dialog to be displayed.  Can be null if there was no error.
	 * @param okButtonText the text to use for the OK button instead of OK.  If null, the text will stay OK.
	 * @param cancelButtonText the text to use for the cancel button instead of Cancel.  If null, the text will stay Cancel
	 * otherwise OK_OPTION will always be returned. 
	 * @return if messageType is OK_CANCEl_OPTION this will return OK_OPTION or CANCEL_OPTION
	 * otherwise OK_OPTION will always be returned.  
	 */
	public static int showWarning(String title, String message, int messageType, String helpPoint, Throwable error, 
			String okButtonText, String cancelButtonText) {
		if (PamGUIManager.isSwing()) {
			return singleInstance.showWarningDialog(PamController.getMainFrame(), 
					title, message, messageType, helpPoint, error, okButtonText, cancelButtonText);
		}
		else if (PamGUIManager.isFX()) {
			return singleInstance.showWarningDialogFX(null, 
					title, message, getAlertType(messageType), helpPoint, error, okButtonText, cancelButtonText);
		}
		return -1; 
	}
	
	/**
	 * Show a warning message in either a Swing or JavaFX dialog. The current GUI type automatically chooses
	 * the dialog type. 
	 * @param title title of warning.
	 * @param message warning message (use HTML for multi-line messages).
	 * @param messageType message type OK_CANCEL_OPTION or WARNING_MESSAGE. 
	 * @return if messageType is OK_CANCEl_OPTION this will return OK_OPTION or CANCEL_OPTION 
	 * otherwise OK_OPTION will always be returned. 
	 */
	public static int showWarning(String title, String msg, int messageType) {
		return showWarning( title,  msg,  messageType,  null, null, null, null); 
	}
	
	
	/**
	 * Show a warning message in either a Swing or JavaFX dialog. The current GUI type automatically chooses
	 * the dialog type. 
	 * @param title title of warning.
	 * @param message warning message (use HTML for multi-line messages).
	 * @param messageType message type OK_CANCEL_OPTION or WARNING_MESSAGE. 
	 * @param helpPoint Pointer into help file for additional information.
	 * @return if messageType is OK_CANCEl_OPTION this will return OK_OPTION or CANCEL_OPTION 
	 * otherwise OK_OPTION will always be returned. 
	 */
	public static int showWarning(String title, String msg, int messageType, String helpPoint) {
		return showWarning( title,  msg,  messageType,  helpPoint, null, null, null); 
	}
	
	/**
	 * Show a warning message in either a Swing or JavaFX dialog. The current GUI type automatically chooses
	 * the dialog type. 
	 * @param title title of warning
	 * @param message warning message (use HTML for multiline messages)
	 * @param messageType message type OK_CANCEL_OPTION or WARNING_MESSAGE 
	 * @param helpPoint Pointer into help file for additional information
	 * @return if messageType is OK_CANCEN_OPTION this will return OK_OPTION or CANECL_OPTION, 
	 * @param error an error that may have caused this dialog to be displayed.  Can be null if there was no error
	 * otherwise OK_OPTION will always be returned. 
	 */
	public static int showWarning(String title, String message, int messageType, String helpPoint, Throwable error) {
		return showWarning( title,  message,  messageType,  helpPoint,  error, 
				  null,   null); 
	}
	
	/**
	 * Get the equivalent AlertType for a message flag. 
	 * @param message - the message flag. 
	 * @return the correpsonding alert type. 
	 */
	private static AlertType getAlertType(int message) {
		AlertType alertType = AlertType.WARNING;
		switch (message) {
		case OK_CANCEL_OPTION: 
			alertType=AlertType.CONFIRMATION;
			break;
		case WARNING_MESSAGE: 
			alertType=AlertType.WARNING;
			break;
		default:
			alertType=AlertType.CONFIRMATION;
		}
		return alertType; 
	}
	
	/**
	 * Show a warning message. 
	 * @param parent parent frame
	 * @param title title of warning
	 * @param message warning message (use html for multiline messages)
	 * @param messageType message type OK_CANCEL_OPTION or WARNING_MESSAGE 
	 * @return if messageType is OK_CANCEN_OPTION this will return OK_OPTION or CANECL_OPTION, 
	 * 
	 * otherwise OK_OPTION will always be returned. 
	 */
	public static int showWarning(Window parent, String title, String message, int messageType) {
		 return singleInstance.showWarningDialog(parent, title, message, messageType, null, null, null, null);
	}

	/**
	 * Show a warning message. This version of the call uses the additional 'name' parameter to determine if the 
	 * warning should be shown or not so that multiple similar messages can be turned off even though the title and 
	 * message may be slightly different.
	 * @param message name
	 * @param parent parent frame
	 * @param title title of warning
	 * @param message warning message (use html for multiline messages)
	 * @param messageType message type OK_CANCEL_OPTION or WARNING_MESSAGE 
	 * @return if messageType is OK_CANCEN_OPTION this will return OK_OPTION or CANECL_OPTION, 
	 * 
	 * otherwise OK_OPTION will always be returned. 
	 */
	public static int showNamedWarning(String name, Window parent, String title, String message, int messageType) {
		 return singleInstance.showWarningDialog(parent, title, message, name, messageType, null, null, null, null);
	}

	/**
	 * Show a warning message. If PAMGuard is being controlled through the network, display the error message in the console
	 * instead of a dialog box that requires user interaction
	 * 
	 * @param parent parent frame
	 * @param title title of warning
	 * @param message warning message (use html for multiline messages)
	 * @param messageType message type OK_CANCEL_OPTION or WARNING_MESSAGE 
	 * @param helpPoint Pointer into help file for additional information
	 * @return if messageType is OK_CANCEN_OPTION this will return OK_OPTION or CANECL_OPTION, 
	 * otherwise OK_OPTION will always be returned. 
	 */
	public static int showWarning(Window parent, String title, String message, int messageType, String helpPoint) {
		return singleInstance.showWarningDialog(parent, title, message, messageType, helpPoint, null, null, null);
	}
	
	/**
	 * Show a warning message. 
	 * @param parent parent frame
	 * @param title title of warning
	 * @param message warning message (use html for multiline messages)
	 * @param messageType message type OK_CANCEL_OPTION or WARNING_MESSAGE 
	 * @param helpPoint Pointer into help file for additional information
	 * @return if messageType is OK_CANCEN_OPTION this will return OK_OPTION or CANECL_OPTION, 
	 * otherwise OK_OPTION will always be returned. 
	 */
	public static int showWarning(Window parent, String title, String message, int messageType, String helpPoint, Throwable error) {
		if (canShowDialog()) {
			return singleInstance.showWarningDialog(parent, title, message, messageType, helpPoint, error, null, null);
		} else {
			System.out.println(" ");
			System.out.println("*** Warning: " + title);
			System.out.println(message);
			System.out.println(" ");
			return 0;
		}
	}
	
	private static boolean canShowDialog() {
		if (PamController.checkIfNetworkControlled()) {
			return false;
		}
		if (PamGUIManager.getGUIType() == PamGUIManager.NOGUI) {
			return false;
		}
		if (GlobalArguments.getParam(GlobalArguments.BATCHFLAG) != null) {
			return false;
		}
		return true;
	}
	
	/**
	 * Show a warning message. 
	 * @param parent parent frame
	 * @param title title of warning
	 * @param message warning message (use html for multiline messages)
	 * @param messageType message type OK_CANCEL_OPTION or WARNING_MESSAGE 
	 * @param helpPoint Pointer into help file for additional information
	 * @param error an error that may have caused this dialog to be displayed.  Can be null if there was no error
	 * @param okButtonText the text to use for the ok button instead of OK.  If null, the text will stay OK
	 * @param cancelButtonText the text to use for the cancel button instead of Cancel.  If null, the text will stay Cancel
	 * @return if messageType is OK_CANCEL_OPTION this will return OK_OPTION or CANCEL_OPTION, otherwise OK_OPTION will always be returned. 
	 */
	public static int showWarning(Window parent, String title, String message, int messageType, String helpPoint, Throwable error, String okButtonText, String cancelButtonText) {
		return singleInstance.showWarningDialog(parent, title, message, messageType, helpPoint, error, okButtonText, cancelButtonText);
	}
	
	/**
	 * Show a warning message in JavaFX dialog. 
	 * @param parent parent frame
	 * @param title title of warning
	 * @param message warning message (use html for multiline messages)
	 * @param messageType message type OK_CANCEL_OPTION or WARNING_MESSAGE 
	 * @return if messageType is OK_CANCEN_OPTION this will return OK_OPTION or CANECL_OPTION, 
	 * otherwise OK_OPTION will always be returned. 
	 */
	public static int showWarningFX(javafx.stage.Window parent, String title, String message, AlertType messageType) {
		 return singleInstance.showWarningDialogFX(parent, title, message, messageType, null, null, null, null);
	}

	/**
	 * Show a warning message in JavaFX dialog. If PAMGuard is being controlled through the network, display the error message in the console
	 * instead of a dialog box that requires user interaction
	 * 
	 * @param parent parent frame
	 * @param title title of warning
	 * @param message warning message (use html for multiline messages)
	 * @param messageType message type AlertType enum
	 * @param helpPoint Pointer into help file for additional information
	 * @return if messageType is OK_CANCEN_OPTION this will return OK_OPTION or CANECL_OPTION, 
	 * otherwise OK_OPTION will always be returned. 
	 */
	public static int showWarningFX(javafx.stage.Window parent, String title, String message, AlertType messageType, String helpPoint) {
		return singleInstance.showWarningDialogFX(parent, title, message, messageType, helpPoint, null, null, null);
	}
	
	/**
	 * Show a warning message in JavaFX dialog. 
	 * @param parent parent frame
	 * @param title title of warning
	 * @param message warning message (use html for multiline messages)
	 * @param messageType message type AlertType enum
	 * @param helpPoint Pointer into help file for additional information
	 * @return if messageType is OK_CANCEN_OPTION this will return OK_OPTION or CANECL_OPTION, 
	 * otherwise OK_OPTION will always be returned. 
	 */
	public static int showWarningFX(javafx.stage.Window parent, String title, String message, AlertType messageType, String helpPoint, Throwable error) {
		if (! PamController.checkIfNetworkControlled()) {
			return singleInstance.showWarningDialogFX(parent, title, message, messageType, helpPoint, error, null, null);
		} else {
			System.out.println(" ");
			System.out.println("*** Warning: " + title);
			System.out.println(message);
			System.out.println(" ");
			return 0;
		}
	}
	
	/**
	 * Show a warning message in JavaFX dialog. 
	 * @param parent parent frame
	 * @param title title of warning
	 * @param message warning message 
	 * @param messageType message type AlertType enum
	 * @param helpPoint Pointer into help file for additional information
	 * @param error an error that may have caused this dialog to be displayed.  Can be null if there was no error
	 * @param okButtonText the text to use for the ok button instead of OK.  If null, the text will stay OK
	 * @param cancelButtonText the text to use for the cancel button instead of Cancel.  If null, the text will stay Cancel
	 * @return if messageType is OK_CANCEL_OPTION this will return OK_OPTION or CANCEL_OPTION, otherwise OK_OPTION will always be returned. 
	 */
	public static int showWarningFX(javafx.stage.Window parent, String title, String message, AlertType messageType, String helpPoint, Throwable error, String okButtonText, String cancelButtonText) {
		return singleInstance.showWarningDialogFX(parent, title, message, messageType, helpPoint, error, okButtonText, cancelButtonText);
	}

	/**
	 * 
	 * Show the warning dialog and store whether or not to show it again in JavaFX dialog. 
	 * @param parent parent frame
	 * @param title title of warning
	 * @param message warning message (use html for multiline messages)
	 * @param messageType message type AlertType enum
	 * @param helpPoint  Pointer into help file for additional information
	 * @param error 
	 * @param okButtonText
	 * @return if messageType is OK_CANCEN_OPTION this will return OK_OPTION or CANECL_OPTION, 
	 * otherwise OK_OPTION will always be returned. 
	 */
	private int showWarningDialog(Window parent, String title, String message, int messageType, String helpPoint, Throwable error, String okButtonText, String cancelButtonText) {
		return showWarningDialog(parent, title, message, title+message, messageType,  helpPoint, error, okButtonText, cancelButtonText);
	}
	/**
	 * 
	 * Show the warning dialog and store whether or not to show it again in JavaFX dialog. 
	 * @param parent parent frame
	 * @param title title of warning
	 * @param message warning message (use html for multiline messages)
	 * @param messageType message type AlertType enum
	 * @param messageName Name of the message used to determine if it's shown again or not
	 * @param helpPoint  Pointer into help file for additional information
	 * @param error 
	 * @param okButtonText
	 * @return if messageType is OK_CANCEN_OPTION this will return OK_OPTION or CANECL_OPTION, 
	 * otherwise OK_OPTION will always be returned. 
	 */
	private int showWarningDialog(Window parent, String title, String message, String messageName, int messageType, String helpPoint, Throwable error, String okButtonText, String cancelButtonText) {

		// check if we should show the warning again this session
		Boolean showAgain = showThisSess.get(messageName);
		if (showAgain == null) {
			showThisSess.put(messageName, true);
		} else if (showAgain == false) {
			return -1;
		}
		
		// check if we should ever show the warning
		boolean showEver = warnOnceList.isShowWarning(messageName);
		if (showEver == false) {
			return -1;
		}
		
		if (parent == null) {
			parent = PamController.getMainFrame();
		}
		
		// show the warning
		if (canShowDialog()) {
			WarnOnceDialog wo = new WarnOnceDialog(parent, title, message, messageType, helpPoint, error, okButtonText, cancelButtonText);
			warnOnceList.setShowWarning(messageName, wo.isShowAgain());
			showThisSess.put(messageName, wo.isShowThisSess());
			return wo.getAnswer();
		}
		else {
			System.out.println(message);
			return OK_OPTION;
		}
	}
	

	/**
	 * 
	 * Show the warning dialog and store whether or not to show it again. 
	 * @param parent parent frame
	 * @param title title of warning
	 * @param message warning message (use html for multiline messages)
	 * @param messageType message type AlertType enum
	 * @param helpPoint  Pointer into help file for additional information
	 * @param error 
	 * @param okButtonText
	 * @return if messageType is OK_CANCEN_OPTION this will return OK_OPTION or CANECL_OPTION, 
	 * otherwise OK_OPTION will always be returned. 
	 */
	private int showWarningDialogFX(javafx.stage.Window parent, String title, String message, AlertType messageType, String helpPoint, Throwable error, String okButtonText, String cancelButtonText) {

		// check if we should show the warning again this session
		Boolean showAgain = showThisSess.get(title+message);
		if (showAgain == null) {
			showThisSess.put(title+message, true);
		} else if (showAgain == false) {
			return -1;
		}
		
		// check if we should ever show the warning
		boolean showEver = warnOnceList.isShowWarning(title+message);
		if (showEver == false) {
			return -1;
		}
		
		// show the warning
		WarnOnceDialogFX wo = new WarnOnceDialogFX(parent, title, message, messageType, helpPoint, error, okButtonText, cancelButtonText);
		wo.showDialog();
		warnOnceList.setShowWarning(title+message, wo.isShowAgain());
		showThisSess.put(title+message, wo.isShowThisSess());
		return wo.getAnswer();
	}



}
