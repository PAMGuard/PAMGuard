package loc3d_Thode;


import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamDetection.PamDetection;
import PamView.dialog.PamDialog;
import PamView.dialog.SourcePanel;
//import PamguardMVC.DataType;
import PamguardMVC.PamDataBlock;
import warnings.PamWarning;
import warnings.WarningSystem;

public class TowedArray3DParametersDialog extends PamDialog {

	/*
	 * Make the dialog a singleton - saves time recreating it 
	 * every time it's used and will also leave the same tab showing
	 * for multi tab dialogs (doesn't really make any difference
	 * for this simple dialog)
	 */
	static private TowedArray3DParametersDialog singleInstance;
	
	/*
	 * local copy of parameters
	 */
	TowedArray3DProcessParameters towedArray3DProcessParameters;
	
	/*
	 * source panel is a handy utility for listing available data sources. 
	 */
	SourcePanel sourcePanel;
	
	/*
	 * alglist is a JcomboBox (pull down menu) of tracking algorithms
	 */
	JComboBox alglist;

	/*
	 * Warning message
	 */
	private static PamWarning locWarning = new PamWarning("Multipath 3D Localiser", "", 2);

	
	/*
	 * reference for data fields
	 *
	 */
	JTextField minIciNum, maxTimeDelay, maxTimeQueue, percentErrorAngle, percentErrorEchoTime;
	
	JCheckBox fine_tune_tdd_checkbox,fine_tune_tds_checkbox,yes_checkbox;
	
	/*
	 * Aaron, you want this too so that you can tell it no to try taking data from its own output data block,
	 * Doug.
	 */
	TowedArray3DController towedArray3DController;
	
	private TowedArray3DParametersDialog(Frame parentFrame) {
		
		super(parentFrame, "3D localizer", true);
		
		/*
		 * Use the Java layout manager to constructs nesting panels 
		 * of all the parameters. 
		 */
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		/* 
		 * put a sourcePanel in the top of the dialog panel. 
		 * need to put it in an inner panel in order to add 
		 * a titled border (appearance is everything)
		 */
		//sourcePanel = new SourcePanel(DataType.DETEVENT, true);
		
		sourcePanel = new SourcePanel(this, PamDetection.class, true, true);
		
//		sourcePanel.setLocalisationRequirements(AbstractLocalisation.HAS_BEARING);
		
		JPanel sourceSubPanel = new JPanel();
		sourceSubPanel.setLayout(new BorderLayout());
		sourceSubPanel.setBorder(new TitledBorder("Detector source"));
		sourceSubPanel.add(BorderLayout.CENTER, sourcePanel.getPanel());
		mainPanel.add(BorderLayout.NORTH, sourceSubPanel);
		
		// make another panel for the rest of the parameters.
		JPanel detPanel = new JPanel();
		detPanel.setBorder(new TitledBorder("3D selection parameters"));
		// use the gridbaglaoyt - it's the most flexible
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		detPanel.setLayout(layout);
		constraints.anchor = GridBagConstraints.WEST;
		constraints.ipadx = 3;
		constraints.gridx = 0;
		constraints.gridy = 0;
		addComponent(detPanel, new JLabel("Minimum ICI needed for cross-channel match"), constraints);
		constraints.gridx++;
		addComponent(detPanel, minIciNum = new JTextField(4), constraints);
		constraints.gridx++;
		addComponent(detPanel, new JLabel(" integer"), constraints);
		
		constraints.gridx = 0;
		constraints.gridy ++;
		addComponent(detPanel, new JLabel("Maximum mulitpath time"), constraints);
		constraints.gridx++;
		addComponent(detPanel, maxTimeDelay = new JTextField(6), constraints);
		constraints.gridx++;
		addComponent(detPanel, new JLabel(" msec"), constraints);
		
		constraints.gridx=0;
		constraints.gridy++;
		addComponent(detPanel, new JLabel("Maximum Queue time"), constraints);
		constraints.gridx++;
		addComponent(detPanel, maxTimeQueue = new JTextField(6), constraints);
		constraints.gridx++;
		addComponent(detPanel, new JLabel(" msec"), constraints);
		
		constraints.gridx=0;
		constraints.gridy++;
		addComponent(detPanel, new JLabel("ICI estimation: percent angle error permitted"), constraints);
		constraints.gridx++;
		addComponent(detPanel, percentErrorAngle = new JTextField(6), constraints);
		constraints.gridx++;
		addComponent(detPanel, new JLabel(" percent"), constraints);
		
		constraints.gridx=0;
		constraints.gridy++;
		addComponent(detPanel, new JLabel("ICI estimation: percent echo time error permitted"), constraints);
		constraints.gridx++;
		addComponent(detPanel, percentErrorEchoTime = new JTextField(6), constraints);
		constraints.gridx++;
		addComponent(detPanel, new JLabel(" percent"), constraints);
		
		fine_tune_tdd_checkbox = new JCheckBox();
		constraints.gridx=0;
		constraints.gridy++;
		addComponent(detPanel, new JLabel("Use cross-correlation to refine direct-direct TOA"), constraints);
		constraints.gridx++;
		addComponent(detPanel, fine_tune_tdd_checkbox, constraints);
		constraints.gridx++;
		addComponent(detPanel, new JLabel(" "), constraints);
		fine_tune_tdd_checkbox.setSelected(false);
		
		fine_tune_tds_checkbox = new JCheckBox();
		constraints.gridx=0;
		constraints.gridy++;
		addComponent(detPanel, new JLabel("Use cross-correlation to refine direct-surface TOA"), constraints);
		constraints.gridx++;
		addComponent(detPanel, fine_tune_tds_checkbox, constraints);
		constraints.gridx++;
		addComponent(detPanel, new JLabel(" "), constraints);
		fine_tune_tds_checkbox.setSelected(false);
		
		yes_checkbox = new JCheckBox();
		constraints.gridx=0;
		constraints.gridy++;
		addComponent(detPanel, new JLabel("Generate 3D positions"), constraints);
		constraints.gridx++;
		addComponent(detPanel, yes_checkbox, constraints);
		constraints.gridx++;
		addComponent(detPanel, new JLabel(" "), constraints);
		yes_checkbox.setSelected(true);
		
		
		mainPanel.add(BorderLayout.CENTER, detPanel);

		//Aaron Add choice of algorithmn.  Demonstration on how to build a drop-down menu subpanel
		JPanel algPanel = new JPanel();
		algPanel.setLayout(new BorderLayout());
		algPanel.setBorder(new TitledBorder("Tracking method"));
		String[] algStrings = { "Cross bearings", "TOA only, simple TOA estimate", "TOA and rear bearing, simple TOA estimate",  
				"TOA only, estimate TOA using ICI", "TOA and rear bearing, estimate TOA using ICI" };
		algPanel.add(BorderLayout.CENTER, alglist = new JComboBox(algStrings));
//		Create the combo box, select item at index 4.
//		Indices start at 0, so 4 specifies the pig.
		//alglist.addActionListener(this);
		mainPanel.add(BorderLayout.SOUTH, algPanel);
		
		setDialogComponent(mainPanel);
	}
	
	public static TowedArray3DProcessParameters showDialog(Frame parentFrame, TowedArray3DController towedArray3DController, TowedArray3DProcessParameters towedArray3DProcessParameters) {
		if (singleInstance == null || singleInstance.getParent() != parentFrame) {
			singleInstance = new TowedArray3DParametersDialog(parentFrame);
		}
		singleInstance.towedArray3DController = towedArray3DController;
		singleInstance.towedArray3DProcessParameters = towedArray3DProcessParameters.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.towedArray3DProcessParameters;
	}

	public void setParams() {
		/* 
		 * set the parameters in the source list. 
		 * including the channel list and the actual data source. 
		 */
		//ArrayList<PamDataBlock> detectorSources = PamController.getInstance().getDetectorDataBlocks();
		ArrayList<PamDataBlock> detectorSources = PamController.getInstance().getDataBlocks(PamDetection.class,true);
		
		// Aaron, I added these two lines to stop it trying to access it's own output data. Doug.
		sourcePanel.clearExcludeList();
		sourcePanel.excludeDataBlock(towedArray3DController.towedArray3DProcess.localizationDataBlock, true);
		
		sourcePanel.setSource(detectorSources.get(towedArray3DProcessParameters.detectorDataBlock));
		sourcePanel.setChannelList(towedArray3DProcessParameters.channelList);
		
		minIciNum.setText(String.format("%d", towedArray3DProcessParameters.minICINum));
		maxTimeDelay.setText(String.format("%d", towedArray3DProcessParameters.maxTimeDelay));
		maxTimeQueue.setText(String.format("%d", towedArray3DProcessParameters.maxTimeQueue));
		percentErrorAngle.setText(String.format("%d", towedArray3DProcessParameters.percentErrorAngle));
		percentErrorEchoTime.setText(String.format("%d", towedArray3DProcessParameters.percentICIMatchError));
		
		if(towedArray3DProcessParameters.fine_tune_tdd)
			fine_tune_tdd_checkbox.setSelected(true);
		else
			fine_tune_tdd_checkbox.setSelected(false);
		
		if(towedArray3DProcessParameters.fine_tune_tds)
			fine_tune_tds_checkbox.setSelected(true);
		else
			fine_tune_tds_checkbox.setSelected(false);
		
		if(towedArray3DProcessParameters.yes_process)
			yes_checkbox.setSelected(true);
		else
			yes_checkbox.setSelected(false);
		
		alglist.setSelectedIndex(towedArray3DProcessParameters.algchoice);
		
	}

	@Override
	public void cancelButtonPressed() {
		towedArray3DProcessParameters = null;		
	}

	@Override
	/**
	 * return true if all parameters are OK, otherwise, return false. 
	 */
	public boolean getParams() {

		// quick check - if the source has sequence numbers, we're not going to know which channels to use for localization.  Warn the user and exit
		PamDataBlock source = PamController.getInstance().getDataBlock(PamDetection.class, sourcePanel.getSourceName());
		if (source.getSequenceMapObject()!=null) {
			String err = "Error: the selected Source uses Beamformer output as a data source, and Beamformer output does not contain "
			+ "the link back to a single channel of raw audio data that is required for analysis.  Please either change the Source's "
			+ "data source, or select a different Source here.";
			locWarning.setWarningMessage(err);
			WarningSystem.getWarningSystem().addWarning(locWarning);
			return false;
		} else {
			WarningSystem.getWarningSystem().removeWarning(locWarning);
		}
		
		/*
		 * get the source parameters
		 */
		towedArray3DProcessParameters.detectorDataBlock = sourcePanel.getSourceIndex();
		towedArray3DProcessParameters.channelList = sourcePanel.getChannelList();
		if (towedArray3DProcessParameters.channelList == 0) {
			return false;
		}
		// will throw an exception if the number format of any of the parameters is invalid, 
		// so catch the exception and return false to prevent exit from the dialog. 
		try {
			towedArray3DProcessParameters.minICINum = Integer.valueOf(minIciNum.getText());
			towedArray3DProcessParameters.maxTimeDelay = Integer.valueOf(maxTimeDelay.getText());
			towedArray3DProcessParameters.maxTimeQueue = Integer.valueOf(maxTimeQueue.getText());
			towedArray3DProcessParameters.percentErrorAngle = Integer.valueOf(percentErrorAngle.getText());
			towedArray3DProcessParameters.percentICIMatchError = Integer.valueOf(percentErrorEchoTime.getText());
//			
//			towedArray3DProcessParameters.highFreq = Integer.valueOf(highFreq.getText());
//			towedArray3DProcessParameters.threshold = Double.valueOf(threshold.getText());
			towedArray3DProcessParameters.algchoice=Integer.valueOf(alglist.getSelectedIndex());
			
			if (fine_tune_tdd_checkbox.isSelected())
				towedArray3DProcessParameters.fine_tune_tdd=true;
			else
				towedArray3DProcessParameters.fine_tune_tdd=false;
			
			if (fine_tune_tds_checkbox.isSelected())
				towedArray3DProcessParameters.fine_tune_tds=true;
			else
				towedArray3DProcessParameters.fine_tune_tds=false;
			
			if (yes_checkbox.isSelected())
				towedArray3DProcessParameters.yes_process=true;
			else
				towedArray3DProcessParameters.yes_process=false;
			
		}
		catch (NumberFormatException ex) {
			return false;
		}
		
		return true;
	}

	@Override
	public void restoreDefaultSettings() {

		towedArray3DProcessParameters = new TowedArray3DProcessParameters();
		setParams();
		
	}

}
