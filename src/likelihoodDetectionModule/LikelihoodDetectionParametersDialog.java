package likelihoodDetectionModule;

import PamDetection.RawDataUnit;
import PamView.dialog.PamDialog;
import PamView.dialog.SourcePanel;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import java.util.ArrayList;
import javax.swing.table.DefaultTableColumnModel;

/**
 * The class LikelihoodDetectionParametersDialog is created by the LikelihoodDetectionUnit to
 * integrate configuration of the likelihood detection module into the configuration menus
 * of PamGuard.
 * 
 * @see PamView.dialog.PamDialog
 * @see LikelihoodDetectionUnit#createDetectionMenu
 */
public class LikelihoodDetectionParametersDialog extends PamDialog {

	/** The constant serialVersionUID required by PamDialog */
	static final long serialVersionUID = 559;
	
	/** The single instance of the dialog. Only one will be created and it will
	 * be managed via static methods.
	 */
	static private LikelihoodDetectionParametersDialog singleInstance;
	
	/** The accepted state of the dialog (i.e., whether OK has been selected). */
	public boolean accepted = false;
	
	/** A pointer to the persistent parameters for the likelihood detector.*/
	private LikelihoodDetectionParameters likelihoodParameters;
	
	/** The PamView construct that allows for the selection of an acquisition source and channels. */
	private SourcePanel sourcePanel;

	/** The custom TreeTable widget's model, that forms the main area for configuration of the
	 * detector.
	 */
	private TreeTableModel treeTableModel;
	
	/** The custom TreeTable widget, that forms the main area for configuration of the detector */
	private TreeTable treeTable;
	
	/**
	 * Instantiates a new likelihood detection parameters dialog.
	 * 
	 * @param parentFrame the parent frame
	 * @param The parameters that this dialog will display.
	 * @see LikelihoodDetectionParameters
	 */
	private LikelihoodDetectionParametersDialog( Frame parentFrame, LikelihoodDetectionParameters params ) {
		super( parentFrame, "Likelihood Detector Parameters", true );
		
		// This dialog is too complex for a simple button to restore defaults,
		// so it is removed from view.
		this.getDefaultButton().setVisible( false );
		
		// Set the help point for this dialog.
		this.setHelpPoint( "docs.LikelihoodDetector_Config" );
		
		this.likelihoodParameters = params;
		
		// Create the back-end model representation of the likelihood parameters.
		treeTableModel = new TreeTableModel( params.getAcquisitionSettings(), params.getChannelMap() );
		
		// Create the custom tree-table widget, using the back-end model.
		treeTable = new TreeTable( this.treeTableModel );

		// Create a main panel that will hold all of this dialog's
		// specific content.
		JPanel mainPanel = new JPanel();
		mainPanel.setBackground( Color.white );
		GridBagLayout mainLayout = new GridBagLayout();
		GridBagConstraints mainConstraints = new GridBagConstraints();
		mainPanel.setLayout( mainLayout );
		mainConstraints.gridx = 0;
		mainConstraints.gridy = 0;
		mainConstraints.anchor = GridBagConstraints.LINE_START;
		mainConstraints.fill = GridBagConstraints.HORIZONTAL;

		// Create a PamView.SourcePanel for selecting the aquisition source and
		// the channels.
		// DG Added this to SourcePanel constructor call. 
		sourcePanel = new SourcePanel(this, RawDataUnit.class, true, true );
		JPanel sourceSubPanel = new JPanel();
		sourceSubPanel.setLayout( new BorderLayout() );
		sourceSubPanel.setBorder( new TitledBorder("Raw Data Source") );
		sourceSubPanel.add( BorderLayout.CENTER, sourcePanel.getPanel() );
		
		sourcePanel.setSource( params.getSourceName() );
		sourcePanel.setChannelList( params.getChannelMap() );
		
		// Add the sourceSubPanel to the main panel's layout.
		mainConstraints.gridy++;
		((GridBagLayout)mainPanel.getLayout()).setConstraints( sourceSubPanel, mainConstraints );
		mainPanel.add( sourceSubPanel );

		JPanel treeTablePanel = new JPanel();
		treeTablePanel.setBackground( Color.white );
		GridBagLayout treeTableLayout = new GridBagLayout();
		GridBagConstraints treeTableConstraints = new GridBagConstraints();
		treeTableConstraints.gridx = 0;
		treeTableConstraints.gridy = 0;
		treeTableConstraints.anchor = GridBagConstraints.LINE_START;
		treeTableConstraints.fill = GridBagConstraints.BOTH;
		
		treeTablePanel.setLayout( treeTableLayout );
		JScrollPane treeScroller = new JScrollPane( treeTable );
		treeScroller.setBackground( Color.white );
		treeTablePanel.add( treeScroller );
		
		mainConstraints.gridy++;
		((GridBagLayout)mainPanel.getLayout()).setConstraints( treeTablePanel, mainConstraints );
		mainPanel.add( treeTablePanel );
		
		setDialogComponent( mainPanel );
		
		DefaultTableColumnModel columnModel = (DefaultTableColumnModel)treeTable.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth( params.getConfigurationDialogSettings().firstColumnWidth );
		columnModel.getColumn(1).setPreferredWidth( params.getConfigurationDialogSettings().secondColumnWidth );
		treeTable.restoreExpanded( params.getConfigurationDialogSettings().expandedState );
		
		// Call pack() here just to be sure the layout is correct.
		this.pack();
	}
	
	/**
	 * Show dialog the dialog.
	 * 
	 * @param parentFrame the parent frame
	 * @param likelihoodParameters the likelihood parameters
	 * 
	 * @return the likelihood detection parameters
	 */
	public static LikelihoodDetectionParameters 
	showDialog( Frame parentFrame, LikelihoodDetectionParameters likelihoodParameters ) {
		
		if (singleInstance == null || singleInstance.getParent() != parentFrame) {
			// COFU (create on first use)
			singleInstance = new LikelihoodDetectionParametersDialog(parentFrame, likelihoodParameters );
		}
		else {
			// Make sure the acquistion settings are passed on. They may have been changed.
			singleInstance.treeTableModel.setAcquisitionSettings( likelihoodParameters.getAcquisitionSettings() );
		}
		
		// The dialog has not yet been accepted.
		singleInstance.accepted = false;
		
		// From the persistent settings portion of the LikelihoodDetectionParameters (the dialog settings), 
		// restore the column sizes. Java does not give any elegant method of doing this.
		DefaultTableColumnModel columnModel = (DefaultTableColumnModel)singleInstance.treeTable.getColumnModel();
		columnModel.getColumn(0).setWidth( likelihoodParameters.getConfigurationDialogSettings().firstColumnWidth );
		columnModel.getColumn(1).setWidth( likelihoodParameters.getConfigurationDialogSettings().secondColumnWidth );
			
		// Tell the dialog what parameters it is now displaying (vice the ones it was constructed with on
		// first use).
		singleInstance.setParams( likelihoodParameters );
		
		// From the persistent settings portion of the LikelihoodDetectionParameters for the dialog,
		// restore the custom tree-table's expansion state as best possible. Java does not give any
		// elegant way of doing this. Note that this must be done after the call to setParams() so
		// that there are actual rows in the table to restore.
		singleInstance.treeTable.restoreExpanded( likelihoodParameters.getConfigurationDialogSettings().expandedState );
		
		// The call to pack() was added here, just before the call to setVisible() in order to
		// get around a race condition in Java where there was a thread trying to display the dialog
		// before a separate thread had actually laid out the widgets properly.
		singleInstance.setLocationRelativeTo(null);
		singleInstance.pack();
		singleInstance.setVisible( true );
				
		if ( singleInstance.accepted ) {
			// Create a new parameters object to return.
			LikelihoodDetectionParameters params = new LikelihoodDetectionParameters();
			
			// The DAQ number and the channel mask can change from the dialog, so we need to get it.
			AcquisitionSettings settings = singleInstance.likelihoodParameters.getAcquisitionSettings();
			settings.sourceNumber = singleInstance.sourcePanel.getSourceIndex();
			
			settings.sourceName = singleInstance.sourcePanel.getSource().getDataName();
			params.setAcquisitionSettings( settings );
			params.setSelectedChannels( singleInstance.sourcePanel.getChannelList() );
			params.setConfigurationDialogSettings( singleInstance.treeTableModel.getDialogSettings() );
			// Get the new target configurations.
			ArrayList<TargetConfiguration> configs = singleInstance.treeTableModel.getTargetConfigurations();
			params.targetConfigurations = configs;
			
			// All done.
			return params;
		}
		else {
			// Returning null signifies the cancel button was selected.
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see PamView.PamDialog#okButtonPressed()
	 */
	@Override
	protected void okButtonPressed() {
		// The dialog was accepted.
		accepted = true;
		
		// Now hide it.
		setVisible( false );
	}
	
	/**
	 * Populate the dialog with the values from the persistent parameters.
	 * 
	 * @param likelihoodParameters the new params
	 */
	public void setParams( LikelihoodDetectionParameters likelihoodParameters ) {	
		this.likelihoodParameters = likelihoodParameters.deepCopy();
		this.treeTableModel.setTargetConfigurations( this.likelihoodParameters.targetConfigurations );
	}
	
	/* (non-Javadoc)
	 * @see PamView.PamDialog#cancelButtonPressed()
	 */
	@Override
	public void cancelButtonPressed() {
		likelihoodParameters = null;		
	}
	
	/* (non-Javadoc)
	 * This function is over-ridden because it is forced to be, but it is not used.
	 * @see PamView.PamDialog#getParams()
	 */
	@Override
	public boolean getParams() {	
		return true;
	}

	/* (non-Javadoc)
	 * @see PamView.PamDialog#restoreDefaultSettings()
	 */
	@Override
	public void restoreDefaultSettings() {
		// Abstract function definition, but this is not required for
		// this class.
	}	
}