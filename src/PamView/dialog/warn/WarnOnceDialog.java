package PamView.dialog.warn;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

/**
 * Part of a managed warning system. Would only be called from within WarnOnce
 * 
 * @author Doug Gillespie
 *
 */
@SuppressWarnings("serial")
public class WarnOnceDialog extends PamDialog {
	
	private JCheckBox dontShowAgain;
	private JCheckBox dontShowAgainThisSess;
	private int answer;
    final String html = "<html><body style='width: 350 px'>";
    int messageType;
    String message;
    Throwable error;

	protected WarnOnceDialog(Window parentFrame, String title, String message, int messageType, String helpPoint) {
		this(parentFrame, title, message, messageType, helpPoint, null);
	}

	protected WarnOnceDialog(Window parentFrame, String title, String message, int messageType, String helpPoint, Throwable error) {
		this(parentFrame, title, message, messageType, helpPoint, error, null, null);
	}

	/**
	 * Dialog with all the options
	 * 	
	 * @param parentFrame the frame to center this dialog in - can be null
	 * @param title the title of the dialog
	 * @param message the message to show in the dialog
	 * @param messageType the type of dialog (WarnOnce.OK_CANCEL_OPTION, WarnOnce.OK_Option or WarnOnce.WARNING_MESSAGE )
	 * @param helpPoint Pointer into help file for additional information
	 * @param error an error that may have caused this dialog to be displayed.  Can be null if there was no error
	 * @param okButtonText the text to use for the ok button instead of OK.  If null, the text will stay OK
	 * @param cancelButtonText the text to use for the cancel button instead of Cancel.  If null, the text will stay Cancel
	 */
	protected WarnOnceDialog(Window parentFrame, String title, String message, int messageType, String helpPoint, Throwable error, String okButtonText, String cancelButtonText) {
		super(parentFrame, title, false);
		this.message=message;
		this.messageType=messageType;
		this.error = error;
		JPanel p = new JPanel(new GridBagLayout());
		p.setBorder(new EmptyBorder(10, 10, 0, 10));
		GridBagConstraints c = new PamGridBagContraints();
		ImageIcon pgIcon = new ImageIcon(ClassLoader.getSystemResource("Resources/pamguardIcon.png"));
		p.add(new JLabel(pgIcon), c);
		c.gridx++;
		p.add(new JLabel("    "), c);
		c.gridx++;
		c.gridwidth = 2;
		p.add(new JLabel(html+message), c);
		c.gridy++;
		p.add(new JLabel("    "), c);
		c.gridy++;
		c.gridx = 2;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		p.add(dontShowAgainThisSess = new JCheckBox("Don't show this message again this session"), c);
		dontShowAgainThisSess.setToolTipText("Ticking this box will stop this warning showing again during this PAMGuard session");
		c.gridx++;
		p.add(dontShowAgain = new JCheckBox("Don't ever show this message again"), c);
		dontShowAgain.setToolTipText("Ticking this box will stop this warning showing again in the future");
		
		// special case - if this is a warning message, use the Cancel button as a copy-to-clipboard button instead
		// Note that this was implemented prior to adding the okButtonText and cancelButtonText fields.  I
		// just don't have time to go back through all the code and change it
		if (messageType == WarnOnce.WARNING_MESSAGE) {
//			getCancelButton().setVisible(false);
			getCancelButton().setText("Copy to Clipboard");
		}
		
		// if the message type is OK_OPTION, hide the cancel button
		if (messageType == WarnOnce.YES_NO_OPTION) {
			getOkButton().setText("Yes");
			getCancelButton().setText("No");
		}
		else if (messageType == WarnOnce.OK_OPTION) {
			getCancelButton().setVisible(false);
		} else {
			getCancelButton().setVisible(true);
		}
		
		
		// change the button text to custom text, if needed
		if (okButtonText!=null) {
			getOkButton().setText(okButtonText);
		}
		if (cancelButtonText!=null) {
			getCancelButton().setText(cancelButtonText);
		}
		
		setDialogComponent(p);
		if (helpPoint != null) {
			setHelpPoint(helpPoint);
		}
		setVisible(true);
	}
	

	@Override
	public boolean getParams() {
		answer = WarnOnce.OK_OPTION;
		return true;
	}

	@Override
	/**
	 * If the cancel button is pressed, check if this is a warning message.  If it is, then the cancel button is really
	 * the copy-to-clipboard button.  In that case, copy the message (and the error message, if available) to the clipboard
	 */
	public void cancelButtonPressed() {
		answer = WarnOnce.CANCEL_OPTION;
		if (this.messageType == WarnOnce.WARNING_MESSAGE) {
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

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	/**
	 * @return the answer
	 */
	public int getAnswer() {
		return answer;
	}
	
	public boolean isShowAgain() {
		return dontShowAgain.isSelected() == false;
	}

	/**
	 * @return
	 */
	public Boolean isShowThisSess() {
		return dontShowAgainThisSess.isSelected() == false;
	}

}
