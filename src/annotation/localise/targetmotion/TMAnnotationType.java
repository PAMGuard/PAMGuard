package annotation.localise.targetmotion;

import Localiser.LocaliserModel;
import Localiser.detectionGroupLocaliser.GroupLocalisation;
import PamguardMVC.PamDataUnit;
import PamguardMVC.debug.Debug;
import PamguardMVC.superdet.SuperDetection;
import annotation.AnnotationDialogPanel;
import annotation.AnnotationSettingsPanel;
import annotation.DataAnnotationType;
import annotation.binary.AnnotationBinaryHandler;
import annotation.handler.AnnotationOptions;
import clickDetector.localisation.ClickLocParams;
import clickDetector.localisation.GeneralGroupLocaliser;
import generalDatabase.SQLLoggingAddon;

/**
 * Annotation type for target motion analysis. Target motion analysis requires a
 * parent data unit which has multiple sub units.
 * 
 * @author Jamie Macaulay
 *
 */
public class TMAnnotationType extends DataAnnotationType<TMAnnotation> {

	public static final String TMANNOTATIONNAME = "Target Motion";

	private static final String SHORTIDCODE = "TMAN";
	
	/**
	 * Target motion annotation options. 
	 */
	private TMAnnotationOptions tmAnnotationOptions;
	
	/**
	 * The dialog panel for target motion options., 
	 */
	protected TMSettingsPanel clickLocDialogPanel;

	/**
	 * The target motion localisation algorithm. 
	 */
	protected TMGroupLocaliser tmGroupLocaliser;
	
	/**
	 * SQL logging for the target motion algorithm.
	 */
	private TMAnnotationLogging tmAnnotationLogging;
	
	/**
	 * Binary logging for target motion localisation. 
	 */
	private TMAnnotationBinary tmAnnotationBinary;
	
	public TMAnnotationType() {
		super();
		tmAnnotationOptions = new TMAnnotationOptions(TMANNOTATIONNAME);
		tmGroupLocaliser = new TMGroupLocaliser();
		tmAnnotationLogging = new TMAnnotationLogging(this);
		tmAnnotationBinary = new TMAnnotationBinary(this);
//		tmAnnotationLogging.setTargetMotionLocaliser(tmGroupLocaliser);
	}

	@Override
	public String getAnnotationName() {
		return TMANNOTATIONNAME;
	}

	@Override
	public Class getAnnotationClass() {
		return TMAnnotation.class;
	}

	@Override
	public boolean canAnnotate(Class dataUnitType) {
		return true;
	}

	@Override
	public boolean canAutoAnnotate() {
		return true;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getShortIdCode()
	 */
	@Override
	public String getShortIdCode() {
		return SHORTIDCODE;
	}

	@Override
	public TMAnnotation autoAnnotate(PamDataUnit pamDataUnit) {
		if (pamDataUnit instanceof SuperDetection == false) {
			return null;
		}
		SuperDetection superDetection = (SuperDetection) pamDataUnit;
		if (superDetection.getSubDetectionsCount() < 2) {
			return null; // don't attempt if < 2 sub detections. 
		}
		
//		Debug.out.println("TMAnnotationType: Start Localising: " + pamDataUnit);
		GroupLocalisation newLocalisation =  tmGroupLocaliser.runModel(superDetection, tmAnnotationOptions);
		if (newLocalisation != null) {
			pamDataUnit.setLocalisation(newLocalisation);
			TMAnnotation tmAnnotation = new TMAnnotation(this, newLocalisation);
			pamDataUnit.addDataAnnotation(tmAnnotation);
			return tmAnnotation;
		}
		else {
			return null;
		}
	}

	@Override
	public SQLLoggingAddon getSQLLoggingAddon() {
		return tmAnnotationLogging;
	}

	/* (non-Javadoc)
	 * @see annotation.DataAnnotationType#getBinaryHandler()
	 */
	@Override
	public AnnotationBinaryHandler<TMAnnotation> getBinaryHandler() {
		return tmAnnotationBinary;
	}

	@Override
	public AnnotationDialogPanel getDialogPanel() {
		return super.getDialogPanel();
	}

	@Override
	public AnnotationSettingsPanel getSettingsPanel() {
		if (clickLocDialogPanel == null) {
			clickLocDialogPanel = new TMSettingsPanel(tmGroupLocaliser);
		}
		return clickLocDialogPanel;
	}

	@Override
	public boolean hasSettingsPanel() {
		return true;
	}

	@Override
	public AnnotationOptions getAnnotationOptions() {
		return tmAnnotationOptions;
	}

	@Override
	public void setAnnotationOptions(AnnotationOptions annotationOptions) {
		if (annotationOptions == null) {
			return;
		}
		super.setAnnotationOptions(annotationOptions);
		if (TMAnnotationOptions.class.isAssignableFrom(annotationOptions.getClass())) {
			this.tmAnnotationOptions = (TMAnnotationOptions) annotationOptions;
			if (tmAnnotationOptions.getLocalisationParams() == null) {
				tmAnnotationOptions.setLocalisationParams(new ClickLocParams());
			}
		}
		
	}

	private class TMGroupLocaliser extends GeneralGroupLocaliser {

		@Override
		public String getName() {
			return getAnnotationName();
		}

		@Override
		public ClickLocParams getClickLocParams() {	
			return tmAnnotationOptions.getLocalisationParams();
		}
		
	}

	public LocaliserModel findLocaliserModel(String modelName) {
		if (modelName == null) {
			return null;
		}
		return tmGroupLocaliser.findLocaliserAlgorithm(modelName);
	}
}
