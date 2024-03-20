package tethys.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import PamView.dialog.NorthPanel;
import PamView.dialog.SettingsButton;
import PamView.help.PamHelp;
import tethys.Collection;
import tethys.CollectionHandler;
import tethys.TethysControl;

/**
 * Common panel used by Calibrations, Deployments and Detections to show an export button and other 
 * common components, such as help, a table, etc.. 
 * @author dg50
 *
 */
abstract public class TethysExportPanel extends TethysGUIPanel {
	
	private TippedButton exportButton;
	
	private JButton optionsButton, helpButton;
	
	private JPanel mainPanel, northPanel;
	
	private JLabel message;

	private CollectionHandler collectionHandler;

	private String helpPoint;

	private boolean showOptions;

	public TethysExportPanel(TethysControl tethysControl, CollectionHandler collectionHandler, boolean showOptions) {
		super(tethysControl);
		this.collectionHandler = collectionHandler;
		this.showOptions = showOptions;
		this.helpPoint = collectionHandler.getHelpPoint();
		
		mainPanel = new JPanel(new BorderLayout());
		northPanel = new JPanel(new BorderLayout());
		JPanel nwPanel = new JPanel();
		nwPanel.setLayout(new BoxLayout(nwPanel, BoxLayout.X_AXIS));
		JPanel nePanel = new JPanel();
		nePanel.setLayout(new BoxLayout(nePanel, BoxLayout.X_AXIS));
		northPanel.add(BorderLayout.CENTER, nwPanel);
		northPanel.add(BorderLayout.EAST, nePanel);
		mainPanel.add(BorderLayout.NORTH, northPanel);
		
		optionsButton = new SettingsButton();
		exportButton = new TippedButton("Export ...", "Export " + collectionHandler.collectionName() + " to Tethys");
		helpButton = new JButton("?");
		helpButton.setToolTipText("Show context sensitive help");
		JLabel space = new JLabel("  ");
		message = new JLabel (" ");
		
		nwPanel.add(optionsButton);
		nwPanel.add(exportButton);
		nwPanel.add(space);
		nwPanel.add(message);
		nePanel.add(helpButton);
		
		showAndHide();
		
		optionsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				optionsButtonPressed(e);
			}
		});
		exportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportButtonPressed(e);
			}
		});
		helpButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				helpButtonPressed(e);
			}
		});
	}

	/**
	 * Show the help. 
	 * @param e
	 */
	protected void helpButtonPressed(ActionEvent e) {
		if (helpPoint == null) {
			return;
		}
		PamHelp.getInstance().displayContextSensitiveHelp(helpPoint);
	}

	/**
	 * Export button has been pressed. 
	 * @param e
	 */
	protected abstract void exportButtonPressed(ActionEvent e);

	/**
	 * Options button has been pressed. 
	 * @param e
	 */
	protected abstract void optionsButtonPressed(ActionEvent e);

	private void showAndHide() {
		optionsButton.setVisible(showOptions);
		helpButton.setVisible(helpPoint != null);
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}

	/**
	 * @return the helpPoint
	 */
	public String getHelpPoint() {
		return helpPoint;
	}

	/**
	 * @param helpPoint the helpPoint to set
	 */
	public void setHelpPoint(String helpPoint) {
		this.helpPoint = helpPoint;
		showAndHide();
	}

	/**
	 * @return the showOptions
	 */
	public boolean isShowOptions() {
		return showOptions;
	}

	/**
	 * @param showOptions the showOptions to set
	 */
	public void setShowOptions(boolean showOptions) {
		this.showOptions = showOptions;
		showAndHide();
	}

	/**
	 * @return the exportButton
	 */
	public TippedButton getExportButton() {
		return exportButton;
	}

	/**
	 * @return the optionsButton
	 */
	public JButton getOptionsButton() {
		return optionsButton;
	}

	/**
	 * @return the helpButton
	 */
	public JButton getHelpButton() {
		return helpButton;
	}

	/**
	 * @return the mainPanel
	 */
	public JPanel getMainPanel() {
		return mainPanel;
	}

	/**
	 * @return the northPanel
	 */
	public JPanel getNorthPanel() {
		return northPanel;
	}

	/**
	 * @return the message
	 */
	public JLabel getMessage() {
		return message;
	}

	/**
	 * @return the collectionHandler
	 */
	public CollectionHandler getCollectionHandler() {
		return collectionHandler;
	}
	
	/**
	 * Enable or disable export button, leaving tool tips alone
	 * @param enable
	 */
	public void enableExport(boolean enable) {
		exportButton.setEnabled(enable);
		if (enable) {
			message.setText(null);
		}
	}
	
	/**
	 * Disable the export button and set the tooltip. 
	 * @param disabledTip
	 */
	public void disableExport(String disabledTip) {
		exportButton.disable(disabledTip);
		message.setText(disabledTip);
	}

}
