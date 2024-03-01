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
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.utilityPanes.FilterPaneFX;
import pamViewFX.fxNodes.utilityPanes.SourcePaneFX;
import pamViewFX.validator.PamValidator;

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

	private PamValidator validator = new PamValidator();

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
			//need to validate the interpolate too as this depends on the input sample rate. 
			//validator.validate();
		});
		PamGuiManagerFX.titleFont2style(sourcePanel.getChannelLabel());

		//		sourcePanel.addSourcePanelMonitor(new SPMonitor());
		holder.getChildren().addAll(sourcePanel);		

		PamGridPane decimatorPane = new PamGridPane();
		decimatorPane.setVgap(5);
		decimatorPane.setHgap(5);

		//Decimator Settings
		Label label = new Label("Decimator settings");
		PamGuiManagerFX.titleFont2style(label);
		holder.getChildren().add(label);		

		int gridx = 0;
		int gridy = 0;
		decimatorPane.add(new Label("Source sample rate "), gridx, gridy);
		gridx++;
		decimatorPane.add(sourceSampleRate = new Label(" - Hz"),  gridx, gridy);
		sourceSampleRate.setPadding(new Insets(0,0,0,5));
		gridx = 0;
		gridy ++;

		decimatorPane.add(new Label("Output sample rate "),  gridx, gridy);
		gridx ++;
		decimatorPane.add(newSampleRate = new TextField(),  gridx, gridy);
		gridx ++;
		decimatorPane.add(new Label(" Hz"),  gridx, gridy);

		validator.createCheck()
		.dependsOn("new_sample_rate", newSampleRate.textProperty())
		.withMethod(c -> {
			try {
				String posVal = c.get("new_sample_rate");
				if (posVal.isEmpty() || Double.valueOf(posVal)==null) {
					c.error("The input for output sample rate is invalid");
				}
			}
			catch (Exception e) {
				c.error("The input for output sample rate is invalid");
			}
		})
		.decorates(newSampleRate).immediate();

		newSampleRate.textProperty().addListener((obsVal, oldVal, newval)->{
			//need to validate the interpolate too as this depends on the sample rate. 
			validator.validate();
		});


		gridy ++;
		gridx = 0;
		//		gridwidth = 1;

		filterPaneFX = new FilterPaneFX();
		decimatorPane.add(new Label("Anti-aliasing filter"),  gridx, gridy);

		gridx++;
		decimatorPane.add(filterButton = new PamButton("Settings"),  gridx, gridy);
		filterButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-chart-bell-curve-cumulative"));


		filterButton.setTooltip(new Tooltip("Set a custom anti-aliasing filter"));
		filterButton.setOnAction((action)->{
			selectFilters(filterButton);
		});

		//		filterButton.addActionListener(new FilterButton());
		gridx = 2;
		//		gridwidth = 2;


		decimatorPane.add(defaultFilterButton = new PamButton(),  gridx, gridy);
		defaultFilterButton.setGraphic(PamGlyphDude.createPamIcon("mdi2c-cog-refresh",PamGuiManagerFX.iconSize));
		defaultFilterButton.setTooltip(new Tooltip("Set the default anti-aliasing filter"));
		defaultFilterButton.setOnAction((action)->{
			restoreDefaultSettings();
		});

		gridx = 1;
		gridy++;
		decimatorPane.add(filterInfo = new Label(""),  gridx, gridy);
		PamGridPane.setColumnSpan(filterInfo, 3);

		gridx = 0;
		//		gridwidth = 1;
		gridy++;

		Label interpLabel = new  Label("Interpolation"); 

		decimatorPane.add(interpLabel,  gridx, gridy);
		gridx++;
		//		gridx += gridwidth;
		//		gridwidth = 2;
		decimatorPane.add(interpolator = new ComboBox<String>(),  gridx, gridy);
		interpolator.getItems().add("None");
		interpolator.getItems().add("Linear");
		interpolator.getItems().add("Quadratic");

		validator.createCheck()
		.dependsOn("new_sample_rate", interpolator.getSelectionModel().selectedIndexProperty())
		.withMethod(c -> {
			//the selected index
			int selectedIndex = c.get("new_sample_rate");
			try {
				float sampleRate = java.lang.Float.valueOf(newSampleRate.getText());
				String warning = decimatorInterpWarning(selectedIndex,  sampleRate);

				if (warning!=null) {
					c.warn(warning);
				}

			}
			catch (Exception e) {
				return;
			}
		})
		.decorates(interpolator).immediate();

		holder.getChildren().add(decimatorPane); 


		//the viewer mode. 
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

		filterPaneFX.setSampleRate(filtSampleRate);

		if (decimatorParams.filterParams==null) {
			restoreDefaultSettings();
		}

		popOver.setOnHidden((e)->{

			FilterParams newFP = filterPaneFX.getParams(decimatorParams.filterParams);

			if (newFP != null) {
				decimatorParams.filterParams = newFP.clone();
			}

			sayFilter();

		});

		filterPaneFX.setParams(decimatorParams.filterParams);
		//show the filter paramters
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
			filterInfo.setText(decimatorParams.filterParams.toString());
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

		//		boolean isInt = decimatorControl.isIntegerDecimation(sourcePanel.getSource().getSampleRate(), decimatorParams.newSampleRate);
		//		if (isInt && decimatorParams.interpolation > 0) {
		//			int ans = WarnOnce.showWarning("Decimator", "With in / out sample rate ratio equal to a whole number, there is no need to interpolate", WarnOnce.OK_CANCEL_OPTION);
		//			if (ans == WarnOnce.CANCEL_OPTION) {
		//				return null;
		//			}
		//			else {
		//				decimatorParams.interpolation = 0;
		//			}
		//		}
		//		if (!isInt && decimatorParams.interpolation == 0) {
		//			int ans = WarnOnce.showWarning("Decimator", "With in / out sample rate ratio NOT equal to a whole number, it is recommended that you use linear or quadratic interpolation", 
		//					WarnOnce.OK_CANCEL_OPTION);
		//			if (ans == WarnOnce.CANCEL_OPTION) {
		//				return null;
		//			}
		//			else {
		////				decimatorParams.interpolation = 0;
		//			}
		//		}

		return decimatorParams;
	}


	/**
	 * Check if the selected interpolator should be used. If not, returning a warning string. 
	 * @param interpSelection - the interpolator selection
	 * @param sampleRate - the sample rate. 
	 * @return a warning if one is needed. Null if not. 
	 */
	private String decimatorInterpWarning(int interpSelection, float sampleRate) {
		boolean isInt = decimatorControl.isIntegerDecimation(sourcePanel.getSource().getSampleRate(), sampleRate);
		String warningString = null;
		if (isInt && interpSelection > 0) {
			warningString = "With in / out sample rate ratio equal to a whole number, there is no need to interpolate";

		}
		if (!isInt && interpSelection == 0) {
			warningString = "With in / out sample rate ratio NOT equal to a whole number, it is recommended that you use linear or quadratic interpolation";

		}
		return warningString;
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
		validator.validate();

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
