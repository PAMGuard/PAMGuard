package clickTrainDetector.layout.classification.templateClassifier;

import java.io.File;
import java.util.ArrayList;

import clickTrainDetector.classification.templateClassifier.DefualtSpectrumTemplates;
import clickTrainDetector.classification.templateClassifier.SpectrumTemplateDataUnit;
import detectionPlotFX.data.DDDataInfo;
import detectionPlotFX.layout.DetectionPlotDisplay;
import detectionPlotFX.plots.SpectrumPlot;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import matchedTemplateClassifer.ImportTemplateCSV;
import matchedTemplateClassifer.ImportTemplateMAT;
import matchedTemplateClassifer.MatchTemplate;
import matchedTemplateClassifer.TemplateImport;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;
import pamViewFX.fxNodes.utilsFX.PamUtilsFX;

/**
 * Shows and allows users to import a spectrum template. 
 * 
 * @author Jamie Macaulay
 *
 */
public class TemplateSpectrumPane  extends PamBorderPane {
	
	/**
	 * Display for shwoing template spectrums. 
	 */
	private TemplateDetectionPlot templateDisplay;
	
	/**
	 * Data info for display spectrums on the display
	 */
	private TemplateDDDataInfo templateSpectrumInfo;

	/**
	 * The current spectrum
	 */
	private SpectrumTemplateDataUnit currentSpectrum;
	
	private double prefHeight =230; 
	
	/**
	 * All types of importers. 
	 */
	private ArrayList<TemplateImport> templateImporters;

	/**
	 * File chooser for selecting a template
	 */
	private FileChooser fileChooser;  

	/**
	 * The load/select template button. Its drop-down menu lists the default
	 * templates plus any extra items added with {@link #addMenuItem(MenuItem)}.
	 */
	private SplitMenuButton splitMenuButton;

	/**
	 * True once a separator has been added below the default templates, so that
	 * extra menu items are only separated from the defaults once.
	 */
	private boolean extraMenuItems = false;

	public TemplateSpectrumPane() {
		this.setCenter(createTheDisplay());
		//this.setStyle("-fx-background-color: red;");
		
		//create importers. 
		templateImporters= new ArrayList<TemplateImport>();
		templateImporters.add(new ImportTemplateCSV());
		templateImporters.add(new ImportTemplateMAT());
		
		fileChooser = new FileChooser();
		
	}
	
	
	private Pane createTheDisplay() {

		templateDisplay = new TemplateDetectionPlot(); 
		//set the default sample rate
		templateDisplay.setDataInfo(templateSpectrumInfo=new TemplateDDDataInfo(templateDisplay, 192000));
		templateDisplay.setPrefHeight(prefHeight);
		templateDisplay.setMaxHeight(prefHeight);
		//templateDisplay.setPrefWidth(500); //TODO - need to make this span the whole display...

		templateDisplay.getDataTypePane().notifyDataChange(); //need this to initialise options pane for different plots. 

		//now add the import button. 
		Pane spectrumTemplate = createTemplatePane(templateDisplay);
				
		
		return spectrumTemplate; 
	}
	
	/**
	 * Create a pane which allows a user to change template, load new templates and preview templates. 
	 * @return the template pane. 
	 */
	private Pane createTemplatePane(TemplateDetectionPlot detectionPlot) {

		SplitMenuButton splitMenuButtonReject = createLoadButton();
		//detectionPlot.setEnableSettingsButton(false); //disable settings button as too much clutter

		
		StackPane.setAlignment(splitMenuButtonReject, Pos.TOP_RIGHT);
		splitMenuButtonReject.setTranslateY(0);
		splitMenuButtonReject.setTranslateX(-20);

		detectionPlot.setMouseTransparent(true);
		detectionPlot.setMaxWidth(Double.MAX_VALUE);
		
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(detectionPlot); 
		stackPane.getChildren().add(splitMenuButtonReject); 
		stackPane.setPrefHeight(prefHeight);
		stackPane.setMaxHeight(prefHeight);
		stackPane.setMaxWidth(Double.MAX_VALUE);
		stackPane.setPrefWidth(500); //need this for some reason to make the plot resize.
	

		//stackPane.setStyle("-fx-background-color: red;");

		detectionPlot.prefWidthProperty().bind(stackPane.widthProperty());



		PamVBox holder = new PamVBox(); 
		holder.setSpacing(5);
		holder.getChildren().addAll(stackPane); 
		holder.setMaxWidth(Double.MAX_VALUE);
		
		//holder.setStyle("-fx-background-color: green;");


		holder.setPadding(new Insets(10,5,5,5));

		return holder;
	}
	
	/**
	 * Create the load button. 
	 * @param detectionPlotDisplay - the detection display
	 * @return a split menu button
	 */
	private SplitMenuButton createLoadButton(){

		splitMenuButton = new SplitMenuButton(); 
		splitMenuButton.setOnAction((action)->{
			loadNewTemplate(); 
		});
		Tooltip toolTipe = new Tooltip(getTooltipText()); 
		splitMenuButton.setTooltip(toolTipe);

		MatchTemplate[] templates = DefualtSpectrumTemplates.getDefaultTemplates(); 

//		splitMenuButton.setGraphic(PamGlyphDude.createPamGlyph(MaterialIcon.FILE_DOWNLOAD, Color.GREY, PamGuiManagerFX.iconSize)); 
		splitMenuButton.setGraphic(PamGlyphDude.createPamIcon("mdi2d-download", Color.GREY, PamGuiManagerFX.iconSize)); 

		MenuItem menuItem;
		for (int i=0; i<templates.length; i++){
			menuItem= new MenuItem(templates[i].name); 
			splitMenuButton.getItems().add(menuItem); 
			final int ind=i; 
			menuItem.setOnAction((action)->{
				setNewTemplate(templates[ind]); 
			});
		}

		return splitMenuButton;
	}

	/**
	 * Add an item to the bottom of the template drop-down menu, below the list of
	 * default templates. Used for extra ways of setting a template, e.g. generating
	 * one from click events.
	 * @param menuItem - the menu item to add.
	 */
	public void addMenuItem(MenuItem menuItem) {
		if (!extraMenuItems) {
			extraMenuItems = true;
			splitMenuButton.getItems().add(new SeparatorMenuItem());
		}
		splitMenuButton.getItems().add(menuItem);
	}


	/**
	 * Set a new classification template
	 * @param spectrumTemplate - the spectrum template to set. 
	 */
	private void setNewTemplate(MatchTemplate spectrumTemplate) {

		// set a new template
		this.currentSpectrum = new SpectrumTemplateDataUnit(spectrumTemplate); 
		templateDisplay.setDataUnit(currentSpectrum);
		
		
	}
	
	/**
	 * Load a template from a file. 
	 * @param detectionPlotDisplay - the display to load the template to. 
	 */
	private void loadNewTemplate() {
		MatchTemplate matchTemplate = importFile(); 
		if (matchTemplate==null) return;
		setNewTemplate(matchTemplate);
	}


	/**
	 * Match template. The match template. 
	 * @return the imported match template. 
	 */
	private MatchTemplate importFile() {
		fileChooser.setTitle("Import a Template");
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
		try {
		MatchTemplate template = null;
		String extension = getFileExtension(file);
		for (int i=0; i< this.templateImporters.size(); i++) {
			for (int j=0; j<templateImporters.get(i).getExtension().length; j++) {
//				System.out.println(templateImporters.get(i).getExtension()[j] + " : " + extension);
				if (templateImporters.get(i).getExtension()[j].equals(extension)) {
//					System.out.println("Import using the extensions: " + extension);
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
		catch (Exception e) {
			e.printStackTrace();
			return null; 
		}
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
	 * Detection Display dataInfo for clicks. This deals with drawing spectrums.
	 * 
	 * @author Jamie Macaulay
	 *
	 */
	public class TemplateDDDataInfo extends DDDataInfo<SpectrumTemplateDataUnit> {

		public TemplateDDDataInfo(DetectionPlotDisplay dDPlot,
				float sampleRate) {
			super(dDPlot, sampleRate);

			this.addDetectionPlot(new SpectrumTemplatePlot(dDPlot));


			super.setCurrentDetectionPlot(0);
		}
	}
	
	/**
	 * Plot for a spectrum template. 
	 * @author Jamie Macaulay
	 *
	 */
	public class SpectrumTemplatePlot extends SpectrumPlot<SpectrumTemplateDataUnit>{

		public SpectrumTemplatePlot(DetectionPlotDisplay detectionPlotDisplay) {
			super(detectionPlotDisplay);
		}

		@Override
		public double[][] getPowerSpectrum(SpectrumTemplateDataUnit data, int start, int end) {
			return new double[][]{data.spectrumTemplate.waveform};
		}
		
	
		@Override
		public double getSampleRate(SpectrumTemplateDataUnit data) {
			//bit of a hack to get sample rate but works. 
//			System.out.println("Set sample rate: " + data.spectrumTemplate.sR);
			return data.spectrumTemplate.sR; 
		}

		@Override
		public double[][] getCepstrum(SpectrumTemplateDataUnit data, int start, int end) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	/**
	 * Simple sub class of the detection plot display which holds data on the match template to
	 * use. 
	 * @author Jamie Macaulay
	 *
	 */
	private class TemplateDetectionPlot extends DetectionPlotDisplay {

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
		 * Set the match template. 
		 * @param matchTemplate - the match template to set. 
		 */
		public void setMatchTemplate(MatchTemplate matchTemplate) {
			this.matchTemplate=matchTemplate; 
		}
	}

	/**
	 * Set the spectrum template. 
	 * @param template - the template to set
	 */
	public void setSpectrum(MatchTemplate template) {
		this.setNewTemplate(template);	
	}
	
	/**
	 * Get the spectrum parameters
	 * @return the spectrum paramters. 
	 */
	public MatchTemplate getSpectrum() {
		return this.currentSpectrum.spectrumTemplate; 
	}
	
	private String getTooltipText() {
		String tooltipText= "";
		tooltipText+="<b>Load a Spectrum Template</b>";
		tooltipText+="<p>";
		tooltipText+="Press the button to import a custom spectrum template from a file, or use the drop-down menu to "
				+ "select one of the default templates. Ideally the template should have the same sample rate as the DAQ "
				+ "input into PAMGuard; if the sample rates are different PAMGuard will interpolate the template. Two "
				+ "file formats can be imported, MATLAB (.mat) and CSV (.csv).";
		tooltipText+="<p><p>";
		tooltipText+="<b>MATLAB (.mat)</b>";
		tooltipText+="A .mat file containing a variable <i>spectrum</i>, a 1D array of linear spectrum amplitude values "
				+ "(usually normalised between 0 and 1), and a variable <i>sR</i>, a single number, the sample rate in "
				+ "samples per second.";
		tooltipText+="<p><p>";
		tooltipText+="<b>CSV (.csv)</b>";
		tooltipText+="A comma-delimited CSV file where the first row is the amplitude values of the spectrum (usually "
				+ "normalised between 0 and 1) and the first value of the second row is the sample rate in samples per second.";
		tooltipText+="<p><p>";
		tooltipText+="Templates generated from click events (the <i>Generate from click events…</i> item at the bottom "
				+ "of the drop-down menu) are saved in these same formats and so can be re-imported here.";
		return 	PamUtilsFX.htmlToNormal(tooltipText);
	}


	public void drawCurrentUnit() {
		templateDisplay.drawCurrentUnit();
	}


}
