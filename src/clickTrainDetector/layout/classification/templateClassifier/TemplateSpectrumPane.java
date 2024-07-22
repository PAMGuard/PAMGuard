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
import pamViewFX.fxNodes.PamStackPane;
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
		PamStackPane headerPane = new PamStackPane(); 
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

		SplitMenuButton splitMenuButton = new SplitMenuButton(); 
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
		tooltipText+="<b>Import a Spectrum Template</b>"; 
		tooltipText+="<p>";
		tooltipText+="Import a custom spectrum from file. Ideally the spectrum should be the same as the sample rate as the DAQ"
				+ "input into PAMGuard, however PAMGuard will attempt to interpolate if sample rates are different. There are "
				+ "two methods of importing, MATLAB and CSV";
		tooltipText+="<p><p>";
		tooltipText+="<b>MATLAB</b>";
		tooltipText+="A struct containing the fields <i>spectrum</i> with a 1D array of amplitude values between -1 and 1 and a field <i>sR</i> with "
				+ "a single number representing the sample rate in samples per second saved as a .mat file";
		tooltipText+="<p><p>";
		tooltipText+="<b>CSV</b>";
		tooltipText+="A comma delimitted CSV file were the first row is the amplitude values of the spectrum between 0 and 1 and the "
				+ "second column is a single value represenitng the sample rate in samples per second";
		return 	PamUtilsFX.htmlToNormal(tooltipText);
	}


	public void drawCurrentUnit() {
		templateDisplay.drawCurrentUnit();
	}


}
