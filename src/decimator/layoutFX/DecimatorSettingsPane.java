package decimator.layoutFX;


import org.controlsfx.control.PopOver;

import Acquisition.layoutFX.OfflineDAQPane;
import Filters.FilterBand;
import Filters.FilterParams;
import Filters.FilterType;
import PamController.PamController;
import PamController.SettingsPane;
import PamDetection.RawDataUnit;
import PamView.dialog.warn.WarnOnce;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamRawDataBlock;
import dataMap.filemaps.OfflineFileParameters;
import decimator.DecimatorControl;
import decimator.DecimatorParams;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.utilityPanes.FilterPaneFX;
import pamViewFX.fxNodes.utilityPanes.SourcePaneFX;
import pamViewFX.fxStyles.PamStylesManagerFX;

/**
 * 
 * Settings for the decimator.
 * 
 * @author Jamie Macaulay 
 * @author Dougals Gillespie
 * 
 */
public class DecimatorSettingsPane extends SettingsPane<DecimatorParams> {
	
	private DecimatorParams decimatorParams;
	
	private PamBorderPane mainPane;

	/**
	 * Reference to the decimator control. 
	 */
	private DecimatorControl decimatorControl;

	/**
	 * Allows users to slect the input source. 
	 */
	private SourcePaneFX sourcePanel;

	/**
	 * Shows the sample rate of the source
	 */
	private Label sourceSampleRate;

	private float inputSampleRate;

	private TextField newSampleRate;

	private PamButton filterButton;

	private PamButton defaultFilterButton;

	private Label filterInfo;

	private FilterPaneFX filterPaneFX;

	private ComboBox<String> interpolator;

	private boolean isViewer;

	private OfflineDAQPane offlineDAQPaneFX;

	public DecimatorSettingsPane(DecimatorControl aquisitionControl) {
		super(null);
		
		mainPane= new PamBorderPane();
		this.decimatorControl = aquisitionControl;

		mainPane.setCenter(createPane() );
		
	}
	
	private Region createPane() {
		
		PamVBox holder = new PamVBox();
		holder.setSpacing(5);
		
		//		insets = new Insets(2,2,2,2);
		
		Label srcLabel = new Label("Decimator settings");
		
		PamGuiManagerFX.titleFont2style(srcLabel);
		holder.getChildren().add(srcLabel);		
			
		sourcePanel = new SourcePaneFX( RawDataUnit.class, true, true);
		sourcePanel.addSelectionListener((obsval, oldVal, newVal)->{
			newDataSource();
		});
		
//		sourcePanel.addSourcePanelMonitor(new SPMonitor());
		holder.getChildren().addAll(sourcePanel);		
		
		PamGridPane decimatorPane = new PamGridPane();

		//Decimator Settings
		Label label = new Label("Decimator settings");
		PamGuiManagerFX.titleFont2style(label);
		holder.getChildren().add(label);		

		int gridx = 0;
		int gridy = 0;
		decimatorPane.add(new Label("Source sample rate "), gridx, gridy);
		gridx++;
		decimatorPane.add(sourceSampleRate = new Label(" - Hz"),  gridx, gridy);
		gridx = 0;
		gridy ++;
		
		decimatorPane.add(new Label("Output sample rate "),  gridx, gridy);
		gridx ++;
		decimatorPane.add(newSampleRate = new TextField(),  gridx, gridy);
		gridx ++;
		decimatorPane.add(new Label(" Hz"),  gridx, gridy);
		gridy ++;
		gridx = 0;
//		gridwidth = 1;
		
		filterPaneFX = new FilterPaneFX();
		decimatorPane.add(filterButton = new PamButton("Filter settings"),  gridx, gridy);
		filterButton.setOnAction((action)->{
			selectFilters(filterButton);
		});
		
		
//		filterButton.addActionListener(new FilterButton());
		gridx = 1;
//		gridwidth = 2;
		
		
		decimatorPane.add(defaultFilterButton = new PamButton("Default Filter"),  gridx, gridy);
		defaultFilterButton.setOnAction((action)->{
			 restoreDefaultSettings();
		});
		
		gridx = 0;
		gridy++;
		decimatorPane.add(filterInfo = new Label("Filter: "),  gridx, gridy);
		PamGridPane.setColumnSpan(filterInfo, 3);

		gridx = 0;
//		gridwidth = 1;
		gridy++;
		
		Label interpLabel = new  Label("Interpolation: "); 

		decimatorPane.add(interpLabel,  gridx, gridy);
		gridx++;
//		gridx += gridwidth;
//		gridwidth = 2;
		decimatorPane.add(interpolator = new ComboBox<String>(),  gridx, gridy);
		interpolator.getItems().add("None");
		interpolator.getItems().add("Linear");
		interpolator.getItems().add("Quadratic");
		
		holder.getChildren().add(decimatorPane); 
//
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		Region mainPane;
		if (isViewer) {
			TabPane tabbedPane = new TabPane();
			
			offlineDAQPaneFX= new OfflineDAQPane(decimatorControl);

			tabbedPane.getTabs().add(new Tab("Offline Files",offlineDAQPaneFX.getContentNode()));
			tabbedPane.getTabs().add(new Tab("Decimator Settings", holder));
			mainPane = tabbedPane;
		}
		else {
			mainPane = holder;
		}
//		
//		setHelpPoint("sound_processing.decimatorHelp.docs.decimator_decimator");
//		filterButton.setToolTipText("Manual adjustment of filter settings");
//		defaultFilterButton.setToolTipText("Set a default filter (6th order Butterworth low pass at Decimator Nyquist frequency)");
//		interpolator.setToolTipText("If Decimation / upsampling is not by an integer value, you should use interpolation to improve waveform reconstruction");
		
		return mainPane;
	}
	
	private void selectFilters(PamButton button) {
		float filtSampleRate = Math.max(inputSampleRate, getOutputSampleRate());
		
		PopOver popOver = new PopOver(); 

		popOver.setContentNode(filterPaneFX.getContentNode());
		
		filterPaneFX.setParams(decimatorParams.filterParams);
		filterPaneFX.setSampleRate(filtSampleRate);
		
		popOver.setOnHidden((e)->{
			if (decimatorParams.filterParams==null) {
				restoreDefaultSettings();
			}
			
			FilterParams newFP = filterPaneFX.getParams(decimatorParams.filterParams);
		
			if (newFP != null) {
				decimatorParams.filterParams = newFP.clone();
			}
			
			sayFilter();

		});

		popOver.show(button);
	}

	
	/**
	 * display filter information
	 */
	private void sayFilter() {
		if (decimatorParams == null || decimatorParams.filterParams == null) {
			filterInfo.setText("No filter");
		}
		else {
			filterInfo.setText("Filter: " + decimatorParams.filterParams.toString());
		}		
	}
	
	
	private float getOutputSampleRate() {
		try {
			float fs = Float.valueOf(newSampleRate.getText());
			return fs;
		}
		catch (NumberFormatException e) {
			return inputSampleRate;
		}
	}
	
	
	public void restoreDefaultSettings() {
		/*
		 *  does not set the output sample rate, but does set sensible values for the 
		 *  filter.  
		 */
		float newFS = 0;
		try {
			newFS = java.lang.Float.valueOf(newSampleRate.getText());
		}
		catch (NumberFormatException e) {
		}
		PamDataBlock sourceblock = sourcePanel.getSource();
		if (sourceblock.getSampleRate() > 0) {
			newFS = Math.min(newFS, sourceblock.getSampleRate());
		}
		if (newFS <= 0) {
			PamDialogFX.showWarning("Invalid output samplerate : " + newSampleRate.getText());
			return;
		}
		decimatorParams.filterParams.lowPassFreq = newFS/2;
		decimatorParams.filterParams.filterType = FilterType.BUTTERWORTH;
		decimatorParams.filterParams.filterOrder = 6;
		decimatorParams.filterParams.filterBand = FilterBand.LOWPASS;
		
		sayFilter();
	}


	@Override
	public DecimatorParams getParams(DecimatorParams currParams) {
		try {
//			ArrayList<PamDataBlock> rawBlocks = PamController.getInstance().getRawDataBlocks();
			decimatorParams.rawDataSource =  sourcePanel.getSource().getDataName();
			decimatorParams.channelMap = sourcePanel.getChannelList();
			decimatorParams.newSampleRate = java.lang.Float.valueOf(newSampleRate.getText());
		}
		catch (Exception Ex) {
			PamDialogFX.showWarning("There is an unknown error - get in touch with PMAGuard support. ");
			return null;
		}
		
		if (decimatorParams.rawDataSource == null) {
			 PamDialogFX.showWarning("You must select a raw data source");
			 return null;
		}
		
		if (decimatorParams.channelMap == 0) {
			 PamDialogFX.showWarning("You must select at least one channel for decimation");
			 return null;
		}

		if (offlineDAQPaneFX != null) {
			OfflineFileParameters ofp = offlineDAQPaneFX.getParams();
			if (ofp == null) {
				return null;
			}
			decimatorControl.getOfflineFileServer().setOfflineFileParameters(ofp);
		}
		
		decimatorParams.interpolation = interpolator.getSelectionModel().getSelectedIndex();
		boolean isInt = decimatorControl.isIntegerDecimation(sourcePanel.getSource().getSampleRate(), decimatorParams.newSampleRate);
		if (isInt && decimatorParams.interpolation > 0) {
			int ans = WarnOnce.showWarning("Decimator", "With in / out sample rate ratio equal to a whole number, there is no need to interpolate", WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return null;
			}
			else {
				decimatorParams.interpolation = 0;
			}
		}
		if (!isInt && decimatorParams.interpolation == 0) {
			int ans = WarnOnce.showWarning("Decimator", "With in / out sample rate ratio NOT equal to a whole number, it is recommended that you use linear or quadratic interpolation", 
					WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				return null;
			}
			else {
//				decimatorParams.interpolation = 0;
			}
		}
		
		return decimatorParams;
	}

	@Override
	public void setParams(DecimatorParams input) {
		this.decimatorParams = input.clone();
		
		sourcePanel.excludeDataBlock(decimatorControl.getDecimatorProcess().getOutputDataBlock(0), true);
		sourcePanel.setSourceList();
		PamRawDataBlock currentBlock = PamController.getInstance().getRawDataBlock(decimatorParams.rawDataSource);
		sourcePanel.setSource(currentBlock);
		sourcePanel.setChannelList(decimatorParams.channelMap);
		newSampleRate.setText(String.format("%.1f", decimatorParams.newSampleRate));
		newDataSource();
		if (offlineDAQPaneFX != null) {
			offlineDAQPaneFX.setParams(decimatorControl.getOfflineFileServer().getOfflineFileParameters());
		}
		interpolator.getSelectionModel().select(decimatorParams.interpolation);
		sayFilter();
		
	}

	@Override
	public String getName() {
		return "Decimator Settings";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}
	
	private void newDataSource() {
		PamDataBlock block = sourcePanel.getSource();
		if (block != null) {
			sourceSampleRate.setText(String.format("%.1f Hz", 
					inputSampleRate = block.getSampleRate()));
		}
	}

}
