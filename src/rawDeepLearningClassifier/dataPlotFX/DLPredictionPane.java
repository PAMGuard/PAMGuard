package rawDeepLearningClassifier.dataPlotFX;

import java.util.ArrayList;

import dataPlotsFX.layout.TDSettingsPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import pamViewFX.fxGlyphs.PamSVGIcon;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.PamToggleSwitch;
import rawDeepLearningClassifier.dlClassification.DLClassName;

/*
 * Symbol Options for the annotation pane
 */
public class DLPredictionPane extends PamBorderPane implements TDSettingsPane {
	
	/**
	 * The main holder pane. 
	 */
	private PamBorderPane mainPane; 
	
	/**
	 * Make the icon. 
	 */
	private Node icon = makeIcon(); 
	//private Node icon2 = makeIcon(); 
	
	/**
	 * Holds the panes with controls to change prediction colours for each class. 
	 */
	private ArrayList<PredictionColourPane> colourPanes = new ArrayList<PredictionColourPane>();

	/**
	 * Reference to the prediction data info
	 */
	private DLPredictionPlotInfoFX dlPredictionPlotInfoFX;

	/**
	 * Holds all the prediction colour controls. 
	 */
	private PamVBox predColHolder; 
	
	/**
	 * Stops the set params from triggering listeners in controls
	 * that call get params (all sorts of bad things happen if this occurs)
	 */
	private boolean setParams = false; 


	public DLPredictionPane(DLPredictionPlotInfoFX dlPredictionPlotInfoFX) {
		
		//System.out.println("HELLO PREDICTION PANE");
		
		this.dlPredictionPlotInfoFX=dlPredictionPlotInfoFX; 
		mainPane = new PamBorderPane(); 
		mainPane.setCenter(predColHolder = new PamVBox());
		predColHolder.setSpacing(5);
		predColHolder.setPadding(new Insets(5,0,0,0));

		this.setParams();

//		if (dlPredictionPlotInfoFX.getDlControl().getDLModel()!=null) {
//			layoutColourPanes( dlPredictionPlotInfoFX.getDlControl().getDLModel().getClassNames());
//		}
		
	}

	@Override
	public Node getHidingIcon() {
		return icon;
	}

	@Override
	public String getShowingName() {
		return "DL Prediction";
	}

	@Override
	public Node getShowingIcon() {
		return null;
	}

	@Override
	public Pane getPane() {
		return mainPane;
	}
	
	public void setParams() {
//		System.out.println("SET params"); 
		setParams= true;

		if (dlPredictionPlotInfoFX.getDlControl().getDLModel()!=null) {
			//populate the prediction pane. 
			DLClassName[] classNames = dlPredictionPlotInfoFX.getDlControl().getDLModel().getClassNames();
			
//			System.out.println("MAKE MY CLASS NAMES: " + dlPredictionPlotInfoFX.getDlControl().getDLModel().getClassNames());

			layoutColourPanes(classNames);
		}

		setParams=false; 
	}
	
	private void layoutColourPanes(DLClassName[] classNames){
		
		if (classNames==null) return;
		
		
		//System.out.println("Class name map: " +  dlPredictionPlotInfoFX.getDlControl().getDLParams().classNameMap);
			
		ArrayList<PredictionColourPane> colourPanes = new ArrayList<PredictionColourPane>();
		
		predColHolder.getChildren().clear();

		for (int i=0; i<classNames.length; i++) {
//			System.out.println("classNames: " + classNames[i].className + "   " 
//		+ this.colourPanes.size() + " LINE INFOS: " + this.dlPredictionPlotInfoFX.getDlPredParams().lineInfos);
			if (i<this.colourPanes.size()) {
				if (!classNames[i].className.equals(this.colourPanes.get(i).getName())) {
					colourPanes.add(new PredictionColourPane(classNames[i].className, true, Color.BLACK));
				}
				else {
					colourPanes.add(this.colourPanes.get(i));
				}
			}
			else {
				colourPanes.add(new PredictionColourPane(classNames[i].className, true, Color.BLACK));
			}
			
			//now check if we can recycle colours
			if (this.dlPredictionPlotInfoFX.getDlPredParams().lineInfos!=null && i<this.dlPredictionPlotInfoFX.getDlPredParams().lineInfos.length) {
				//probably the same
				//System.out.println("LINE INFOS: " + this.dlPredictionPlotInfoFX.getDlPredParams().lineInfos[i].color);
				colourPanes.get(i).setLineInfo(dlPredictionPlotInfoFX.getDlPredParams().lineInfos[i]);
			}
			
			predColHolder.getChildren().add(colourPanes.get(i));
		}
		
		this.colourPanes=colourPanes;
	}
	

	/**
	 * Get the parameters.
	 */
	private void getParams() {
//		System.out.println("GET params"); 
		if (setParams) return; 

		this.dlPredictionPlotInfoFX.getDlPredParams().lineInfos = new LineInfo[colourPanes.size()];
		for (int i=0; i<colourPanes.size(); i++) {
			this.dlPredictionPlotInfoFX.getDlPredParams().lineInfos[i] = new LineInfo(
					colourPanes.get(i).toggleSwitch.isSelected(), colourPanes.get(i).colourPicker.getValue());
		}
	}
	
	
	/**
	 * There are new settings. Repaints the graph. 
	 */
	private void newSettings() {
		newSettings(0);
	}

	/**
	 * There are new settings. Repaints the graph. 
	 * @param milliswait
	 */
	private void newSettings(long milliswait) {
		getParams();
		this.dlPredictionPlotInfoFX.getTDGraph().repaint(milliswait);
	}

	private Node makeIcon() {
		String resourcePath = "/Resources/modules/noun_Deep Learning_2486374.svg"; 
		try {
			PamSVGIcon iconMaker= new PamSVGIcon(); 
			//PamSVGIcon svgsprite = iconMaker.create(new File(getClass().getResource(resourcePath).toURI()), Color.WHITE);
			PamSVGIcon svgsprite = iconMaker.create(getClass().getResource(resourcePath).toURI().toURL(), Color.DODGERBLUE, 1);
			 
//			svgsprite.getSpriteNode().setStyle("-fx-text-color: white");				
//			svgsprite.getSpriteNode().setStyle("-fx-fill: white");
			svgsprite.setFitHeight(20);
			svgsprite.setFitWidth(20);
			return svgsprite.getSpriteNode(); 
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null; 
	}
	
	
	/**
	 * Pane for showing prediciton and colour.
	 * @author Jamie Macaulay
	 *
	 */
	private class PredictionColourPane extends PamHBox {
		
		/**
		 * The toggle switch
		 */
		private PamToggleSwitch toggleSwitch;
		
		/**
		 * The colour picker
		 */
		private ColorPicker colourPicker;

		private String name;

		public PredictionColourPane(String name, boolean enabled, Color color) {
			
			this.setSpacing(5);
			this.setAlignment(Pos.CENTER_LEFT);
			
			this.toggleSwitch = new PamToggleSwitch(name); 
			this.toggleSwitch.selectedProperty().addListener((obsVal, oldVal, newVal)->{
				newSettings();
			});
			toggleSwitch.setPrefWidth(120);
			toggleSwitch.setTooltip(new Tooltip(name));
			
			this.colourPicker = new ColorPicker();
			this.colourPicker.valueProperty().addListener((obsVal, oldVal, newval)->{
				newSettings();
			});
			
			this.colourPicker.setPrefWidth(80);
			
			
			this.name =name;
			
			this.getChildren().addAll(toggleSwitch, colourPicker);
			
			
			setParams(name, enabled, color);
			
		}
		
		public void setLineInfo(LineInfo lineInfo) {
			this.colourPicker.setValue(lineInfo.color);
			
		}

		public String getName() {
			return name;
		}

		public void setParams(String name, boolean enabled, Color color) {
			this.toggleSwitch.setLabel(new Label(name));
			this.toggleSwitch.setSelected(enabled);
			this.colourPicker.setValue(color);
		}
		
	}

	
}