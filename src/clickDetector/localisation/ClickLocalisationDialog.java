package clickDetector.localisation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import clickDetector.ClickControl;
import Localiser.ModelControlPanel;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;

/**
 * Dialog to allow users to select which type of localisation algorithm to use. 
 * @author Jamie Macaulay
 *
 */
public class ClickLocalisationDialog extends PamDialog  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	static private ClickLocalisationDialog singleInstance;

	private boolean okPressed;


	private ClickGroupLocaliser clickGroupLocaliser;
	private ClickLocDialogPanel clickLocDialogPanel;

	public ClickLocalisationDialog(Window parentFrame, ClickGroupLocaliser groupLocaliser) {
		super(parentFrame, "Click Localisation", false);
		this.clickGroupLocaliser = groupLocaliser;
		clickLocDialogPanel = new ClickLocDialogPanel(groupLocaliser);
		

		setHelpPoint("localisation.targetmotion.docs.targetmotion_overview");
//		super.setResizable(true);
		setDialogComponent(clickLocDialogPanel.getDialogComponent());
	}
	
	public static ClickLocParams showDialog(ClickControl clickControl, Window parentFrame, ClickGroupLocaliser clickGroupLocaliser){
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new ClickLocalisationDialog(parentFrame, clickGroupLocaliser);
		}
		singleInstance.clickGroupLocaliser = clickGroupLocaliser;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		if (singleInstance.okPressed) {
			return singleInstance.clickGroupLocaliser.getClickLocParams();
		}
		else {
			return null;
		}
	}

	private void setParams() {
		clickLocDialogPanel.setParams();
		this.pack();
	}

	@Override
	public boolean getParams() {
		okPressed = clickLocDialogPanel.getParams();
		return okPressed;
	}

	@Override
	public void cancelButtonPressed() {
		okPressed = false;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

	
	
	


}
