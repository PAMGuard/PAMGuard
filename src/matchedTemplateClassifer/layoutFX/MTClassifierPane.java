package matchedTemplateClassifer.layoutFX;

import java.io.File;
import java.util.ArrayList;

import PamController.SettingsPane;
import PamDetection.RawDataUnit;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.data.DDDataProvider;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.plots.WaveformPlot;
import detectionPlotFX.plots.WignerPlot;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import matchedTemplateClassifer.DefaultTemplates;
import matchedTemplateClassifer.ImportTemplateCSV;
import matchedTemplateClassifer.ImportTemplateMAT;
import matchedTemplateClassifer.MTClassifier;
import matchedTemplateClassifer.MTClassifierControl;
import matchedTemplateClassifer.MatchTemplate;
import matchedTemplateClassifer.TemplateImport;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamGridPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamSpinner;
import pamViewFX.fxNodes.PamStackPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.utilsFX.ColourArray.ColourArrayType;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;


/**
 * A pane with controls to set a single matched click classifier. There can be multiple 
 * classifiers for a single matched click classifier module. 
 * 
 * @author Jamie Macaulay
 *
 */
public class MTClassifierPane extends SettingsPane<MTClassifier> {

	/**
	 * Displays the click to match
	 */
	private MatchDetectionPlot detectionDisplayMatch;

	/*
	 * Displays a waveform to reject
	 */
	private MatchDetectionPlot detectionDisplayReject;

	/**
	 * Reference to the control
	 */
	private MTClassifierControl mtClassifierControl;

	/**
	 * MTClassifier pane
	 */
	private MTClassifier mtClassifier;

	/**
	 * The current data block. 
	 */
	private PamDataBlock currentParentBlock;

	/**
	 * The graph which shows the match template
	 */
	private TemplateDDDataInfo templateMatch;

	/*
	 * The graph which shows the reject template
	 */
	private TemplateDDDataInfo templateReject;

	/**
	 * PamSpinner 
	 */
	private PamSpinner<Double> spinner;

	/**
	 * File chooser. 
	 */
	private FileChooser fileChooser; 

	/**
	 * All types of importers. 
	 */
	private ArrayList<TemplateImport> templateImporters;  

	private PamBorderPane mainPane = new PamBorderPane();

	/**
	 * Spinner to choose the click type flag which is set. 
	 */
	private PamSpinner<Integer> typeSpinner;
	
	
	private Font font= Font.font(null, FontWeight.BOLD, 11);


	/**
	 * Generate a classifier pane. 
	 */
	public MTClassifierPane(MTClassifierControl mtClassifierControl, MTClassifier mtClassifier){
		super(null);
		this.mtClassifier=mtClassifier; 
		this.mtClassifierControl=mtClassifierControl; 

		fileChooser = new FileChooser();

		//create importers. 
		templateImporters= new ArrayList<TemplateImport>();
		templateImporters.add(new ImportTemplateCSV());
		templateImporters.add(new ImportTemplateMAT());


		mainPane.setTop(createBasicSettings());
		mainPane.setCenter(createTemplatePreviewPane()); 
		mainPane.setMaxWidth(Double.MAX_VALUE);
		
		
		//mainPane.setStyle("-fx-background-color: red");
		this.setParams(mtClassifier);
	}


	/**
	 * Set the current data block. 
	 */
	public void setParentDataBlock(PamDataBlock pamDataBlock){
		this.currentParentBlock=pamDataBlock;
	}

	/**
	 * Create basic settings pane. 
	 * @return the basic settings pane. 
	 */
	private Pane createBasicSettings(){


		PamVBox holder = new PamVBox(); 
		holder.setSpacing(5);

		Label label = new Label("Click Template Settings"); 
//		PamGuiManagerFX.titleFont2style(label);
//		label.setFont(PamGuiManagerFX.titleFontSize2);
		label.setFont(font);



		PamHBox threshHolder=new PamHBox(); 
		threshHolder.setSpacing(5);
		threshHolder.setAlignment(Pos.CENTER_LEFT); //make sure children are in center. 

		Label threshLabel = new Label("Match threshold "); 

		spinner = new PamSpinner<Double>(-5000.,5000.,0.03,0.01); 
		spinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
		spinner.getValueFactory().setConverter(PamSpinner.createStringConverter(5));
		spinner.setPrefWidth(100);
		spinner.setEditable(true);

		//FIXME- need to add this to register typing...
		spinner.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				spinner.increment(0); // won't change value, but will commit editor
			}
		});

		threshHolder.getChildren().addAll(threshLabel , spinner); 

		holder.getChildren().addAll(label, threshHolder);

		return holder; 
	}




	/**
	 * Create the preview frame.
	 * @return pane with controls to change templates. 
	 */
	private Pane createTemplatePreviewPane(){
		
		detectionDisplayMatch = new MatchDetectionPlot(); 
		//set the default sample rate
		detectionDisplayMatch.setDataInfo(templateMatch=new TemplateDDDataInfo(null , detectionDisplayMatch, 192000));
		//detectionDisplayMatch.setPrefHeight(300);
		detectionDisplayMatch.getDataTypePane().notifyDataChange(); //need this to initialise options pane for different plots. 
		
		detectionDisplayMatch.setAxisVisible(false, false, true, true);


		detectionDisplayReject = new MatchDetectionPlot();
		//set the default sample rate
		detectionDisplayReject.setDataInfo(templateReject=new TemplateDDDataInfo(null , detectionDisplayReject, 192000));
		//detectionDisplayReject.setPrefHeight(300);
		detectionDisplayReject.getDataTypePane().notifyDataChange(); 
		
		detectionDisplayReject.setAxisVisible(false, false, true, true);
		
		
		Pane matchTemplatePane	= createTemplatePane( "Match Template ",  detectionDisplayMatch); 
		Pane rejectTemplatePane	= createTemplatePane( "Reject Template ",  detectionDisplayReject, true); 
		//rejectTemplatePane.setStyle("-fx-background-color: blue");


		Label matchLabel= new Label("Click Templates");
		matchLabel.setFont(font);
		//PamGuiManagerFX.titleFont2style(matchLabel);

//		matchLabel.setFont(PamGuiManagerFX.titleFontSize2);

		PamGridPane gridPane = new PamGridPane();
		gridPane.setHgap(5);
		gridPane.setVgap(5);
		gridPane.add(matchLabel, 0, 0);
		GridPane.setColumnSpan(matchLabel, 2);
		

		gridPane.add(matchTemplatePane, 0, 1);
		gridPane.add(rejectTemplatePane, 1, 1);
		
		GridPane.setHgrow(matchTemplatePane, Priority.ALWAYS);
		GridPane.setHgrow(rejectTemplatePane, Priority.ALWAYS);
		
		GridPane.setVgrow(matchTemplatePane, Priority.ALWAYS);
		GridPane.setVgrow(rejectTemplatePane, Priority.ALWAYS);
		
//		GridPane gridpane = new GridPane();
//		ColumnConstraints column1 = new ColumnConstraints();
//		column1.setPercentWidth(50);
//		ColumnConstraints column2 = new ColumnConstraints();
//		column2.setPercentWidth(50);
//		gridpane.getColumnConstraints().addAll(column1, column2); // each get 50% of width

		gridPane.prefWidthProperty().bind(mainPane.widthProperty()); 
		gridPane.setPadding(new Insets(5,0,0,0));
		
		gridPane.setMaxWidth(Double.MAX_VALUE);
		//gridPane.setStyle("-fx-background-color: green");


		return gridPane; 
	}

	
	/**
	 * Create a pane which allows a user to change template, load new templates and preview templates. 
	 * @param label - the label for the pane. 
	 * @param detectionPlot - the detection plot.  
	 * @return the template pane. 
	 */
	private Pane createTemplatePane(String label, MatchDetectionPlot detectionPlot) {
		return createTemplatePane( label,  detectionPlot, false);
	}

	/**
	 * Create a pane which allows a user to change template, load new templates and preview templates.
	 * @param label - the label for the pane. 
	 * @param detectionPlot - the detection plot.  
	 * @param includeNone - include a none option for template. 
	 * @return the template pane. 
	 */
	private Pane createTemplatePane(String label, MatchDetectionPlot detectionPlot, boolean includeNone) {

		SplitMenuButton splitMenuButtonReject = createLoadButton(detectionPlot, includeNone);
		PamStackPane headerPane = new PamStackPane(); 
		//detectionPlot.setEnableSettingsButton(false); //disable settings button as too much clutter

		Label rejectLabel=new Label(label);
		rejectLabel.setStyle("-fx-font-weight: bold");		
		
		HBox.setHgrow(rejectLabel, Priority.ALWAYS);

		headerPane.getChildren().add(rejectLabel);
		StackPane.setAlignment(rejectLabel, Pos.CENTER);

		//rejectLabel.setFont(PamGuiManagerFX.titleFontSize2);
		headerPane.prefWidthProperty().bind(detectionPlot.widthProperty());


		StackPane.setAlignment(splitMenuButtonReject, Pos.TOP_RIGHT);
//		splitMenuButtonReject.setTranslateY(50);
//		splitMenuButtonReject.setTranslateX(-10);

		PamVBox holder = new PamVBox(); 
		
		//date pane box
		Pane dataPaneBox = detectionPlot.getDataTypePane(); 
		
		PamHBox importPane = new PamHBox();
		importPane.setSpacing(5);
		importPane.setAlignment(Pos.CENTER_LEFT);
		importPane.getChildren().add(new Label("Import")); 
		importPane.getChildren().add(splitMenuButtonReject); 
		//stackPane.getChildren().add(datatTypeBox); 
		importPane.setMaxWidth(Double.MAX_VALUE);
		//stackPane.prefWidthProperty().bind(holder.widthProperty());
		
		PamBorderPane controlPane = new PamBorderPane(); 
		controlPane.setLeft(dataPaneBox);
		controlPane.setRight(importPane);

		holder.setSpacing(5);
		holder.getChildren().addAll(headerPane, controlPane, detectionPlot); 
		holder.setMaxWidth(Double.MAX_VALUE);

		holder.setPadding(new Insets(10,5,5,5));

		return holder;
	}

	/**
	 * Load a template from a file. 
	 * @param detectionPlotDisplay - the display to load the template to. 
	 */
	private void loadNewTemplate(MatchDetectionPlot detectionPlotDisplay) {
		MatchTemplate matchTemplate = importFile(); 
		if (matchTemplate==null) return;
		setNewTemplate(matchTemplate,  detectionPlotDisplay);
	}


	/**
	 * Match template. The match template. 
	 * @return the imported match template. 
	 */
	private MatchTemplate importFile() {
		fileChooser.setTitle("Import a Match Template");
		File file = fileChooser.showOpenDialog(null);
		if (file != null) {
			return openFile(file);
		}
		else return null; 
	};


	/**
	 * Open the template file. 
	 * @param file - the file to open. 
	 */
	private MatchTemplate openFile(File file) {
		MatchTemplate template = null;
		String extension = getFileExtension(file);
		for (int i=0; i< this.templateImporters.size(); i++) {
			for (int j=0; j<templateImporters.get(i).getExtension().length; j++) {
				//System.out.println(templateImporters.get(i).getExtension()[j] + " : " + extension);
				if (templateImporters.get(i).getExtension()[j].equals(extension)) {
					//System.out.println("Import using the extensions: " + extension);
					template=templateImporters.get(i).importTemplate(file);
				}
			}
		}
		if (template==null) {
			PamDialogFX.showWarning("The file could not be loaded. Check the format and extension are correct"); 
			return null;
		}
		else return template; 
	}


	/**
	 * Get the file extension.
	 * @param file - the file again
	 * @return the string
	 */
	private String getFileExtension(File file) {
		String name = file.getName();
		try {
			return name.substring(name.lastIndexOf(".") + 1);
		} catch (Exception e) {
			return "";
		}
	}


	/**
	 * Create the load button. 
	 * @param detectionPlotDisplay - the detection display
	 * @param noneOption - true to include a no template option in the button default menu. 
	 * @return a split menu button
	 */
	private SplitMenuButton createLoadButton(MatchDetectionPlot detectionPlotDisplay, boolean noneOption){

		SplitMenuButton splitMenuButton = new SplitMenuButton(); 
		splitMenuButton.setOnAction((action)->{
			loadNewTemplate(detectionPlotDisplay); 
		});
		Tooltip toolTipe = new Tooltip(getTooltipText()); 
		splitMenuButton.setTooltip(toolTipe);

		MatchTemplate[] templates = DefaultTemplates.getDefaultTemplates(); 

//		splitMenuButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.FILE_DOWNLOAD, Color.GREY, PamGuiManagerFX.iconSize)); 
		splitMenuButton.setGraphic(PamGlyphDude.createPamIcon("mdi2d-download", Color.GREY, PamGuiManagerFX.iconSize)); 

		MenuItem menuItem;
		for (int i=0; i<templates.length; i++){
			if (templates[i].name.equals("None") && !noneOption) {
				continue; 
			}
			menuItem= new MenuItem(templates[i].name); 
			splitMenuButton.getItems().add(menuItem); 
			final int ind=i; 
			menuItem.setOnAction((action)->{
				setNewTemplate(templates[ind], detectionPlotDisplay); 
			});
		}

		return splitMenuButton;
	}


	/**
	 * Set a new template. 
	 * @param defaultTemplate
	 * @param detectionPlotDisplay
	 */
	private void setNewTemplate(MatchTemplate defaultTemplate, MatchDetectionPlot detectionPlotDisplay) {
		RawDataUnit newRawUnit = new RawDataUnit(0,1,0,defaultTemplate.waveform.length);
		detectionPlotDisplay.setMatchTemplate(defaultTemplate);
		newRawUnit.setRawData(defaultTemplate.waveform);
		detectionPlotDisplay.getCurrentDataInfo().setHardSampleRate(defaultTemplate.sR); //must set the sample rate. 
		detectionPlotDisplay.newDataUnit(newRawUnit);
	}


	@Override
	public MTClassifier getParams(MTClassifier p) {
		//the mt classifier
		mtClassifier.waveformMatch = this.detectionDisplayMatch.getMatchTemplate(); 
		mtClassifier.waveformReject = this.detectionDisplayReject.getMatchTemplate(); 

		//TODO need to fix spinner so that user input is read properly
		mtClassifier.thresholdToAccept = spinner.getValue(); 
		
		mtClassifier.reset(); 

		return mtClassifier;
	}
	
	


	@Override
	public void setParams(MTClassifier mtClassifier) {

		if (mtClassifier.waveformMatch==null) mtClassifier.waveformMatch=new MatchTemplate("BeakedWhale", DefaultTemplates.beakedWhale1, 192000);
		setNewTemplate(mtClassifier.waveformMatch, detectionDisplayMatch); 

		if (mtClassifier.waveformReject==null) mtClassifier.waveformReject=new MatchTemplate("Dolphin", DefaultTemplates.dolphin1, 192000);
		setNewTemplate(mtClassifier.waveformReject, detectionDisplayReject); 

		this.spinner.getValueFactory().setValue(mtClassifier.thresholdToAccept);

		detectionDisplayMatch.drawCurrentUnit();
		detectionDisplayReject.drawCurrentUnit();
	}


	@Override
	public String getName() {
		return "Matched Template Classifier";
	}

	@Override
	public Node getContentNode() {
		return mainPane;
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub

	}



	/**
	 * Detection Display dataInfo for clicks. This deals with drawing click waveforms, frequency, 
	 * wigner plots etc. 
	 * @author Jamie Macaulay
	 *
	 */
	public class TemplateDDDataInfo extends DDDataInfo<RawDataUnit> {

		public TemplateDDDataInfo(DDDataProvider dDDataProvider, DetectionPlotDisplay dDPlot,
				float sampleRate) {
			super(dDPlot, sampleRate);

			this.addDetectionPlot(new RawDataPlot(dDPlot));
			this.addDetectionPlot(new WignerTemplateData(dDPlot));

			super.setCurrentDetectionPlot(0);
		}
	}

	/**
	 * Shows a waveform on the graph.
	 * @author Jamie Macaulay
	 *
	 */
	public class RawDataPlot extends WaveformPlot<RawDataUnit> {

		public RawDataPlot(DetectionPlotDisplay detectionPlotDisplay) {
			super(detectionPlotDisplay);
			super.setupPlot();
			super.setShowChannels(false); 
		}


		@Override
		public void setupPlot() {
			//need to get rid of the right axis. 
			this.getDetectionPlotDisplay().setAxisVisible(false, false, true, true);
			//setup the scroll bar. 
		}
		
		@Override
		public String getName() {
			return "Template Waveform";
		}

		@Override
		public double[][] getWaveform(RawDataUnit pamDetection) {
			//just return the waveform. 
			//System.out.println("Getting the waveform for the waveform plot: " + pamDetection.getRawData().length);
			return new double[][]{pamDetection.getRawData()};
		}

		@Override
		public double[][] getEnvelope(RawDataUnit pamDetection) {
			return null; //no need for envelopes
		}
		
		@Override
		public Pane getSettingsPane() {
			return null; 
		}

	}

	/**
	 * Shows a waveform on the graph. 
	 * @author Jamie Macaulay
	 *
	 */
	public class WignerTemplateData extends WignerPlot<RawDataUnit> {

		public WignerTemplateData(DetectionPlotDisplay detectionPlotDisplay) {
			super(detectionPlotDisplay);
			super.setupPlot();
			super.getWignerParameters().colorArray=ColourArrayType.HOT; 
		}
		
		@Override
		public void setupPlot() {
			//need to get rid of the right axis. 
			this.getDetectionPlotDisplay().setAxisVisible(false, false, true, true);
			//setup the scroll bar. 
		}

		@Override
		public String getName() {
			return "Template Wigner";
		}

		@Override
		public double[] getWaveform(RawDataUnit pamDetection, int chan) {
			//just return the waveform. 
			//System.out.println("Getting the waveform for the wigner plot: " + pamDetection.getRawData().length);
			return pamDetection.getRawData();
		}
		
		@Override
		public Pane getSettingsPane() {
			return null; 
		}


	}

	/**
	 * Simple sub class of the detection plot display which holds data on the match template to
	 * use. 
	 * @author Jamie Macaulay
	 *
	 */
	private class MatchDetectionPlot extends DetectionPlotDisplay {

		
		public MatchDetectionPlot() {
			super(); 
		}
	
		/**
		 * Match template 
		 */
		private MatchTemplate matchTemplate; 

		/**
		 * Get match template.  
		 * @return the template. 
		 */
		public MatchTemplate getMatchTemplate() {
			return matchTemplate;
		}
		
		/**
		 * Bit of hack to get rid of scroll bar
		 */
		public void setupScrollBar(PamDataUnit newDataUnit){
			super.setupScrollBar(newDataUnit);
			this.setTop(null); 
		}


		/**
		 * Set the match template. 
		 * @param matchTemplate - the match template to set. 
		 */
		public void setMatchTemplate(MatchTemplate matchTemplate) {
			this.matchTemplate=matchTemplate; 
		}
	}

	private String getTooltipText() {
		String tooltipText= "";
		tooltipText+="<b>Import a waveform</b>"; 
		tooltipText+="<p>";
		tooltipText+="Import a custom waveform from file. The waveform should be the same as the sample rate as the DAQ"
				+ "input into PAMGuard, however PAMGuard will attempt to interpolate if sample rates are different. There are "
				+ "two methods of importing, MATLAB and CSV";
		tooltipText+="<p><p>";
		tooltipText+="<b>MATLAB</b>";
		tooltipText+="A struct containing the fields <i>waveform</i> with a 1D array of amplitude values between -1 and 1 and a field <i>sR</i> with "
				+ "a single number representing the sample rate in samples per second saved as a .mat file";
		tooltipText+="<p><p>";
		tooltipText+="<b>CSV</b>";
		tooltipText+="A comma delimitted CSV file were the first row is the amplitude values of the waveform between -1 and 1 and the "
				+ "second column is a single value represenitng the sample rate in samples per second";
		return 	PamUtilsFX.htmlToNormal(tooltipText);
	}

}
