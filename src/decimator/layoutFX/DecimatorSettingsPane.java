package decimator.layoutFX;

import java.awt.GridBagConstraints;

import org.controlsfx.control.PopOver;

import Acquisition.layoutFX.OfflineDAQPane;
import Filters.FilterBand;
import Filters.FilterDialog;
import Filters.FilterParams;
import Filters.FilterType;
import PamController.PamController;
import PamController.SettingsPane;
import PamDetection.RawDataUnit;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamDataBlock;
import apple.laf.JRSUIUtils.TabbedPane;
import dataMap.filemaps.OfflineFileDialogPanel;
import decimator.DecimatorControl;
import decimator.DecimatorParams;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.utilityPanes.FilterPaneFX;
import pamViewFX.fxNodes.utilityPanes.SourcePaneFX;

/**
 * 
 * Settings for the decimator.
 * 
 * @author Jamie Macaulay
 */
public class DecimatorSettingsPane extends SettingsPane<DecimatorParams> {
	
	private static DecimatorParams decimatorParams;
	
	private PamBorderPane mainPane;

	private DecimatorControl decimatorControl;

	private SourcePaneFX sourcePanel;

	private Label sourceSampleRate;

	private float inputSampleRate;

	private TextField newSampleRate;

	private PamButton filterButton;

	private PamButton defaultFilterButton;

	private Label filterInfo;

	private FilterPaneFX filterPaneFX;

	private ComboBox<String> interpolator;

	private boolean isViewer;

	public DecimatorSettingsPane(DecimatorControl aquisitionControl) {
		super(null);
		
		mainPane= new PamBorderPane();
		this.decimatorControl = aquisitionControl;

		mainPane.setCenter(createPane() );
		
	}
	
	private Pane createPane() {
		
		PamVBox mainPanel = new PamVBox();
		
		//		insets = new Insets(2,2,2,2);
		
		Label srcLabel = new Label("Decimator settings");
		PamGuiManagerFX.titleFont2style(srcLabel);
		mainPanel.getChildren().add(srcLabel);		
			
		sourcePanel = new SourcePaneFX( RawDataUnit.class, true, true);
		sourcePanel.addSelectionListener((obsval, oldVal, newVal)->{
			newDataSource();
		});
		
//		sourcePanel.addSourcePanelMonitor(new SPMonitor());
		mainPanel.getChildren().addAll(srcLabel, sourcePanel.getPane());		
		
		PamGridPane decimatorPanel = new PamGridPane();

		//Decimator Settings
		Label label = new Label("Decimator settings");
		PamGuiManagerFX.titleFont2style(label);
		mainPanel.getChildren().add(label);		

		int gridx = 0;
		int gridy = 0;
		decimatorPanel.add(new Label("Source sample rate "), gridx, gridy);
		gridx++;
		decimatorPanel.add(sourceSampleRate = new Label(" - Hz"),  gridx, gridy);
		gridx = 0;
		gridy ++;
		
		decimatorPanel.add(new Label("Output sample rate "),  gridx, gridy);
		gridx ++;
		decimatorPanel.add(newSampleRate = new TextField(),  gridx, gridy);
		gridx ++;
		decimatorPanel.add(new Label(" Hz"),  gridx, gridy);
		gridy ++;
		gridx = 0;
//		gridwidth = 1;
		
		filterPaneFX = new FilterPaneFX();
		decimatorPanel.add(filterButton = new PamButton("Filter settings"),  gridx, gridy);
		filterButton.setOnAction((action)->{
			selectFilters(filterButton);
		});
		
		
//		filterButton.addActionListener(new FilterButton());
		gridx = 1;
//		gridwidth = 2;
		
		
		decimatorPanel.add(defaultFilterButton = new PamButton("Default Filter"),  gridx, gridy);
		defaultFilterButton.setOnAction((action)->{
			 restoreDefaultSettings();
		});
		
		gridx = 0;
		gridy++;
		decimatorPanel.add(filterInfo = new Label("Filter: "),  gridx, gridy);
		PamGridPane.setColumnSpan(filterInfo, 3);

		gridx = 0;
//		gridwidth = 1;
		gridy++;
		
		Label interpLabel = new  Label("Interpolation: "); 

		decimatorPanel.add(interpLabel,  gridx, gridy);
		gridx++;
//		gridx += gridwidth;
//		gridwidth = 2;
		decimatorPanel.add(interpolator = new ComboBox<String>(),  gridx, gridy);
		interpolator.getItems().add("None");
		interpolator.getItems().add("Linear");
		interpolator.getItems().add("Quadratic");
//
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		if (isViewer) {
			TabPane tabbedPane = new TabPane();
			
			offlineDAQPaneFX= new OfflineDAQPane(acquisitionControl, this);

			offlineDAQDialogPanel = new OfflineFileDialogPanel(decimatorControl, this);
			tabbedPane.add("Offline Files", offlineDAQDialogPanel.getComponent());
			tabbedPane.add("Runtime Settings", mainPanel);
			setDialogComponent(tabbedPane);
		}
		else {
			setDialogComponent(mainPanel);
		}
//		
//		setHelpPoint("sound_processing.decimatorHelp.docs.decimator_decimator");
//		filterButton.setToolTipText("Manual adjustment of filter settings");
//		defaultFilterButton.setToolTipText("Set a default filter (6th order Butterworth low pass at Decimator Nyquist frequency)");
//		interpolator.setToolTipText("If Decimation / upsampling is not by an integer value, you should use interpolation to improve waveform reconstruction");
		
		return mainPanel;
	}
	
	private void selectFilters(PamButton button) {
		float filtSampleRate = Math.max(inputSampleRate, getOutputSampleRate());
		
		PopOver popOver = new PopOver(); 

		popOver.setContentNode(filterPaneFX.getContentNode());
		
		popOver.setOnHidden((e)->{
			filterPaneFX.setSampleRate(filtSampleRate);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParams(DecimatorParams input) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
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
