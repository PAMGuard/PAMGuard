package group3dlocaliser.algorithm.crossedbearing;

import java.awt.Window;
import java.io.Serializable;

import Array.ArrayManager;
import Localiser.LocaliserPane;
import Localiser.detectionGroupLocaliser.DetectionGroupOptions;
import PamDetection.AbstractLocalisation;
import PamDetection.LocContents;
import PamDetection.LocalisationInfo;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import annotation.AnnotationSettingsDialog;
import annotation.localise.targetmotion.TMAnnotation;
import annotation.localise.targetmotion.TMAnnotationOptions;
import annotation.localise.targetmotion.TMAnnotationType;
import generalDatabase.SQLLoggingAddon;
import group3dlocaliser.Group3DLocaliserControl;
import group3dlocaliser.algorithm.LocaliserAlgorithm3D;
import group3dlocaliser.algorithm.LocaliserAlgorithmParams;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxGlyphs.PamGlyphDude;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamButton;
import pamViewFX.fxNodes.PamHBox;

public class CrossedBearingGroupLocaliser extends LocaliserAlgorithm3D {

	private double sampleRate;
	
	private TMAnnotationType tmAnnotationType;
	
	private LocContents locContents = new LocContents(LocContents.HAS_BEARING);

	private CrossedBearingPane crossedBearingPane;

	private Group3DLocaliserControl group3dLocaliserControl;

	public CrossedBearingGroupLocaliser(Group3DLocaliserControl group3dLocaliserControl) {
		this.group3dLocaliserControl = group3dLocaliserControl; 
		tmAnnotationType = new TMAnnotationType();
		TMAnnotationOptions tmAnnotationOptions = new TMAnnotationOptions("CrossedBearingGroupLocaliser");
		tmAnnotationOptions.getLocalisationParams().setIsSelected(0, false);
		tmAnnotationOptions.getLocalisationParams().setIsSelected(1, false);
		tmAnnotationOptions.getLocalisationParams().setIsSelected(2, true);
		tmAnnotationType.setAnnotationOptions(tmAnnotationOptions);
	}

	@Override
	public String getName() {
		return "Crossed Bearings";
	}

	@Override
	public boolean prepare(PamDataBlock sourceBlock) {
		this.sampleRate = sourceBlock.getSampleRate();
		return true;
	}

//	@Override
//	public GroupLocalisation runModel(List<PamDataUnit> dataUnits) {
	/* (non-Javadoc)
	 * @see Localiser.LocaliserModel#runModel(PamguardMVC.PamDataUnit, Localiser.detectionGroupLocaliser.DetectionGroupOptions, boolean)
	 */
	@Override
	public AbstractLocalisation runModel(PamDataUnit groupDataUnit, DetectionGroupOptions detectionGroupOptions,
			boolean addLoc) {
		TMAnnotation tmAnnotation = tmAnnotationType.autoAnnotate(groupDataUnit);
//		if (tmAnnotation != null) {
//			System.out.println(tmAnnotation.toString());
//		}
		if (tmAnnotation == null) {
			return null;
		}
		
		return tmAnnotation.getGroupLocalisation();
	}

	@Override
	public SQLLoggingAddon getSQLLoggingAddon(int arrayType) {
		switch (arrayType) {
		case ArrayManager.ARRAY_TYPE_NONE:
		case ArrayManager.ARRAY_TYPE_POINT:
			return null;
		case ArrayManager.ARRAY_TYPE_LINE:
		case ArrayManager.ARRAY_TYPE_PLANE:
			return new CrossedBearingSQLAddon(2);
		case ArrayManager.ARRAY_TYPE_VOLUME:
			return new CrossedBearingSQLAddon(1);
		}
		return null;
	}

	/**
	 * @return the tmAnnotationOptions
	 */
	public TMAnnotationOptions getTmAnnotationOptions() {
		return (TMAnnotationOptions) tmAnnotationType.getAnnotationOptions();
	}

	/**
	 * @param tmAnnotationOptions the tmAnnotationOptions to set
	 */
	public void setTmAnnotationOptions(TMAnnotationOptions tmAnnotationOptions) {
		tmAnnotationType.setAnnotationOptions(tmAnnotationOptions);
	}

	/**
	 * @return the tmAnnotationType
	 */
	public TMAnnotationType getTmAnnotationType() {
		return tmAnnotationType;
	}

	/* (non-Javadoc)
	 * @see Localiser.LocaliserModel#getToolTipText()
	 */
	@Override
	public String getToolTipText() {
		return "Localise using crossed bearings";
	}

	/* (non-Javadoc)
	 * @see Localiser.LocaliserModel#getLocContents()
	 */
	@Override
	public LocContents getLocContents() {
		return locContents;
	}

	/* (non-Javadoc)
	 * @see Localiser.LocaliserModel#getSettingsPane()
	 */
	@Override
	public LocaliserPane<Serializable> getAlgorithmSettingsPane() {
		if (crossedBearingPane == null) {
			crossedBearingPane = new CrossedBearingPane(); 
		}
		return crossedBearingPane;
	}

	/* (non-Javadoc)
	 * @see Localiser.LocaliserModel#hasParams()
	 */
	@Override
	public boolean hasParams() {
		return true;
	}

	/* (non-Javadoc)
	 * @see Localiser.LocaliserModel#notifyModelProgress(double)
	 */
	@Override
	public void notifyModelProgress(double progress) {
		// TODO Auto-generated method stub
		
	}
	
	//TODO - make this full FX 
	/**
	 * Settings pane for the crossed bearing loclaiser. Note that this just shows a settings button which open a swing dialog.. 
	 * @author Jamie Macaulay
	 *
	 */
	public class CrossedBearingPane extends LocaliserPane<Serializable> {

		private PamBorderPane mainPane;
		
		private LocaliserAlgorithmParams algorithmPaams;

		public CrossedBearingPane() {
				PamHBox hBox = new PamHBox(); 
				hBox.setSpacing(5);
				hBox.setAlignment(Pos.CENTER);
				hBox.setPadding(new Insets(5,5,5,5));
				
				PamButton algoOptsButton = new PamButton("",PamGlyphDude.createPamIcon("mdi2c-cog", PamGuiManagerFX.iconSize));
				algoOptsButton.setOnAction((action)->{
					moreAlgorithmOptions() ;
				});
				algoOptsButton.setTooltip(new Tooltip("More Algorithm Options ..."));
				
				hBox.getChildren().addAll(new Label(getName() + " settings"), algoOptsButton); 
				
				
				mainPane = new PamBorderPane(); 
				mainPane.setCenter(hBox); 
		}
		
		/**
		 * Handle algorithm options ...
		 */
		protected void moreAlgorithmOptions() {
			String algoName  = getName();
			if (algoName == null) {
				return;
			}
			LocaliserAlgorithm3D localiserAlgorithm = group3dLocaliserControl.findAlgorithm(algoName);
			if (localiserAlgorithm == null) {
				return;
			}
			if (localiserAlgorithm.hasParams() == false) {
				return;
			}
			
			LocaliserAlgorithmParams algorithmPaams = group3dLocaliserControl.getLocaliserAlgorithmParams(localiserAlgorithm);
			
			
			this.algorithmPaams  = showAlgorithmDialog(getAWTWindow(), algorithmPaams);
			
			
//			if (algorithmPaams != null) {
//				group3dLocaliserControl.setAlgorithmParams(localiserAlgorithm, algorithmPaams);
//			}
		}
		
		
		/* (non-Javadoc)
		 * @see group3dlocaliser.algorithm.LocaliserAlgorithmProvider#showAlgorithmDialog(java.awt.Window, group3dlocaliser.algorithm.LocaliserAlgorithmParams)
		 */
		public LocaliserAlgorithmParams showAlgorithmDialog(Window parent, LocaliserAlgorithmParams currentParams) {
			
			if (currentParams != null && currentParams.getAlgorithmParameters() instanceof TMAnnotationOptions) {
				TMAnnotationOptions p = (TMAnnotationOptions) currentParams.getAlgorithmParameters();
				setTmAnnotationOptions(p);
			}
			
//			AnnotationSettingsPanel settingsPanel = cbLocaliser.getTmAnnotationType().getSettingsPanel();
			boolean asd = AnnotationSettingsDialog.showDialog(parent, getTmAnnotationType());
			
			
			if (asd) {
				return new LocaliserAlgorithmParams(getTmAnnotationOptions());
			}
			else {
				return null;
			}
		}

		@Override
		public Serializable getParams(Serializable currParams) {
			if (algorithmPaams!=null) {
				return this.algorithmPaams ; 
			}
			return currParams;
		}

		@Override
		public void setParams(Serializable input) {
			this.algorithmPaams = (LocaliserAlgorithmParams) input;
		}

		@Override
		public String getName() {
			return "Crossed Bearing Settings";
		}

		@Override
		public Node getContentNode() {
			return mainPane;
		}

		@Override
		public void paneInitialized() {
			// TODO Auto-generated method stub
			
		}
		
	}

	

	@Override
	public boolean canLocalise(PamDataBlock pamDataBlock) {
		if (pamDataBlock == null) {
			return false;
		}
		LocalisationInfo locCont = pamDataBlock.getLocalisationContents();
		return locCont.hasLocContent(LocContents.HAS_BEARING);
	}


}
