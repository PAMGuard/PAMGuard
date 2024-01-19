package dataModelFX.connectionNodes;

import dataModelFX.DataModelStyle;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxGlyphs.PamSVGIcon;

/**
 * Handles module icons in PAMGuard.
 * <br>
 * This has two functions
 * 1) It keeps in JavaFX stuff out of the PamModel and in the GUI
 * 2) It means nodes can be created easily and copied for several GUI components. 
 * <br>
 * @author Jamie Macaulay
 *
 */
public class ModuleIconFactory {

	private static ModuleIconFactory instance; 

	private static int fontSize=20;

	/**
	 * Enum for module icons. 
	 * @author Jamie
	 *
	 */
	public enum ModuleIcon {
		DATAMAP, NMEA, GPS, MAP, SOUND_AQ, SOUND_OUTPUT, FFT, FILTER, CLICK, CLICK_TRAIN, RECORDER, WHISTLE_MOAN,
		NOISE_BAND, NOISE_FILT, DATABASE, BINARY, TIME_DISPLAY, DETECTION_DISPLAY, ARRAY, DEEP_LEARNING, MATCHED_CLICK_CLASSIFIER
	}

	/**
	 * Get the icon for a pamcontrolled unit. 
	 * @param icon - the enum of the controlled unit
	 * @return the icon for the controlled unit
	 */
	public  Node getModuleNode(ModuleIcon icon){
		long time1 = System.currentTimeMillis();
		Node iconNode =null;
		
		switch (icon){
		case ARRAY:
//			return new ImageView(new Image(getClass().getResourceAsStream("/Resources/modules/array_manager.png")));
			iconNode = getSVGIcon("/Resources/modules/Array Icon2.svg",Color.BLACK, 3);
			break;
		case BINARY:
//			return PamGlyphDude.createModuleGlyph(OctIcon.FILE_BINARY); 
			iconNode = PamGlyphDude.createModuleIcon("mdi2f-file-star-outline"); 
			break;
		case CLICK:
//			return new ImageView(new Image(getClass().getResourceAsStream("/Resources/modules/click.png")));
			iconNode = getSVGIcon("/Resources/modules/Click Detector Icon.svg", Color.BLACK, 2);
			break;
		case CLICK_TRAIN:
//			return new ImageView(new Image(getClass().getResourceAsStream("/Resources/modules/clicktrain.png")));
			iconNode = getSVGIcon("/Resources/modules/clicktrain.svg",Color.BLACK, 2);
			break;
		case DATABASE:
//			return PamGlyphDude.createModuleGlyph(FontAwesomeIcon.DATABASE);
			iconNode = PamGlyphDude.createModuleIcon("mdi2d-database");
			break;
		case DATAMAP:
			iconNode = new ImageView(new Image(getClass().getResourceAsStream("/Resources/modules/dataMap.png")));
			break;
		case FFT:
//			return new ImageView(new Image(getClass().getResourceAsStream("/Resources/modules/fft.png")));
			iconNode = getSVGIcon("/Resources/modules/fft.svg",Color.BLACK, 2);
			break;
		case FILTER:
//			return new ImageView(new Image(getClass().getResourceAsStream("/Resources/modules/filters.png")));
			iconNode = getSVGIcon("/Resources/modules/filters.svg",Color.BLACK, 2);
			break;
		case GPS:
//			return PamGlyphDude.createModuleGlyph(MaterialIcon.GPS_FIXED); 
			iconNode = PamGlyphDude.createModuleIcon("mdi2c-crosshairs-gps"); 
			break;
		case MAP:
//			return PamGlyphDude.createModuleGlyph(MaterialIcon.MAP);
			iconNode = PamGlyphDude.createModuleIcon("mdi2m-map");
			break;
		case NMEA:
			iconNode = createNMEASymbol();
		case NOISE_BAND:
			iconNode = new ImageView(new Image(getClass().getResourceAsStream("/Resources/modules/filterdNoiseMeasurementBank.png")));
			break;
		case NOISE_FILT:
			iconNode = new ImageView(new Image(getClass().getResourceAsStream("/Resources/modules/filterdNoiseMeasurement.png")));
			break;
		case RECORDER:
			iconNode = new ImageView(new Image(getClass().getResourceAsStream("/Resources/modules/recorder.png")));
			break;
		case SOUND_AQ:
			iconNode = getSVGIcon("/Resources/modules/noun_Soundwave_1786340.svg");
			break;
		case MATCHED_CLICK_CLASSIFIER:
			iconNode = getSVGIcon("/Resources/modules/matched_click_classifier.svg",Color.BLACK, 2);
			break;
			//return new ImageView(new Image(getClass().getResourceAsStream("/Resources/modules/aquisition.png")));
		case SOUND_OUTPUT:
//			return PamGlyphDude.createModuleGlyph(MaterialDesignIcon.HEADPHONES); 
			iconNode = PamGlyphDude.createModuleIcon("mdi2h-headphones"); 
			break;
//return new ImageView(new Image(getClass().getResourceAsStream("/Resources/modules/playback.png")));
		case WHISTLE_MOAN:
			iconNode = new ImageView(new Image(getClass().getResourceAsStream("/Resources/modules/whistles.png")));
			break;
		case TIME_DISPLAY:
			iconNode = new ImageView(new Image(getClass().getResourceAsStream("/Resources/modules/timeDisplay.png"))); 
			break;
		case DETECTION_DISPLAY: 
			iconNode = new ImageView(new Image(getClass().getResourceAsStream("/Resources/modules/detectionDisplay.png")));
			break;
		case DEEP_LEARNING:
			//System.out.println("------GET THE SVG ICON FOR DEEP LEARNING--------");
			iconNode = getSVGIcon("/Resources/modules/noun_Deep Learning_2486374.svg"); 
			break;
		default:
			break;
		}
		
		long time2 = System.currentTimeMillis();

		System.out.println("GET MODULE ICON: " + icon + " load time: " + (time2-time1));

		return iconNode;
	}; 
	
	/**
	 * Get an SVG icon.
	 * @param resourcePath - the path from the src folder
	 * @return a node for the SVG icon. 
	 */
	private Node getSVGIcon(String resourcePath) {
		return getSVGIcon( resourcePath, Color.BLACK, 1);
	}

	

	/**
	 * Get an SVG icon.
	 * @param resourcePath - the path from the src folder
	 * @return a node for the SVG icon. 
	 */
	private Node getSVGIcon(String resourcePath, Color colour, double lineWidth) {
		try {
			
			PamSVGIcon iconMaker= new PamSVGIcon(); 
			PamSVGIcon svgsprite = iconMaker.create(getClass().getResource(resourcePath).toURI().toURL(), colour, lineWidth);
//			svgsprite.getSpriteNode().setStyle("-fx-text-color: black");				
//			svgsprite.getSpriteNode().setStyle("-fx-fill: black");
			svgsprite.setFitHeight(DataModelStyle.iconSize-10);
			svgsprite.setFitWidth(DataModelStyle.iconSize-10);
		
			return svgsprite.getSpriteNode(); 
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null; 
	}


	private Node createNMEASymbol(){
		Font font = new Font(fontSize);
		Text label = new Text("NMEA");
		label.setFill(Color.BLACK);
		label.setFont(font);
		StackPane stackPane=new StackPane();
//		stackPane.getChildren().addAll(PamGlyphDude.createModuleGlyph(FontAwesomeIcon.FILE_ALT),label);
		stackPane.getChildren().addAll(PamGlyphDude.createModuleIcon("mdi2f-file-document-outline"),label);
		return stackPane;
	}

	/**
	 * Get instance of the ModuleFactory.
	 * @return
	 */
	public static ModuleIconFactory getInstance(){
		if (instance==null){
			instance=new ModuleIconFactory(); 
		}
		return instance; 
	}

	/**
	 * Get the icon for a pam controlled unit. 
	 * @param icon - the class name string of the controlled unit. 
	 * @return the icon for the controlled unit
	 */
	public Node getModuleNode(String className) {
		ModuleIcon icon = getModuleIcon(className); 
		if (icon==null) return null; 
		else return getModuleNode(icon); 
	}

	/**
	 * Get the module icon for a module class. 
	 * @param className - the class name. 
	 * @return the module icon enum
	 */
	public ModuleIcon getModuleIcon(String className) {
		ModuleIcon icon = null; 
		switch (className) {
		case "Acquisition.AcquisitionControl":
			icon=ModuleIcon.SOUND_AQ; 
			break; 
		case "ArrayManager":
			icon=ModuleIcon.ARRAY; 
			break; 
		case "fftManager.PamFFTControl":
			icon=ModuleIcon.FFT; 
			break; 
		case "Filters.FilterControl":
			icon=ModuleIcon.FILTER; 
			break; 
		case "binaryFileStorage.BinaryStore":
			icon=ModuleIcon.BINARY; 
			break; 
		case "generalDatabase.DBControlUnit":
			icon=ModuleIcon.DATABASE; 
			break; 
		case "whistlesAndMoans.WhistleMoanControl":
			icon=ModuleIcon.WHISTLE_MOAN; 
			break; 
		case "clickDetector.ClickControl":
			icon=ModuleIcon.CLICK; 
			break; 
		case "clickTrainDetector.ClickTrainControl":
			icon=ModuleIcon.CLICK_TRAIN; 
			break; 
		case "dataPlotsFX.TDDisplayController":
			icon=ModuleIcon.TIME_DISPLAY; 
			break; 
		case "detectionPlotFX.DetectionDisplayControl":
			icon=ModuleIcon.DETECTION_DISPLAY; 
			break; 
		case "dataMap.DataMapControl":
			icon=ModuleIcon.DATAMAP; 
			break; 
		case "soundPlayback.PlaybackControl":
			icon=ModuleIcon.SOUND_OUTPUT; 
			break; 
		case "rawDeepLearningClassifier.DLControl":
			icon=ModuleIcon.DEEP_LEARNING; 
			break; 
		case "matchedTemplateClassifer.MTClassifierControl":
			icon=ModuleIcon.MATCHED_CLICK_CLASSIFIER; 
			break; 
		}
		return icon;
	}

}
