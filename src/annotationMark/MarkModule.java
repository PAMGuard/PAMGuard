package annotationMark;

import annotation.DataAnnotationType;
import annotation.handler.AnnotationHandler;
import generalDatabase.SQLLoggingAddon;


public abstract class MarkModule extends PamController.PamControlledUnit {

	public static String unitType = "Annotations";
	
//	private DataAnnotationType soloAnnotationType;
	
	private MarkProcess annotationProcess;
		
	public MarkModule(String unitName) {
		super(unitType, unitName);
		addPamProcess(annotationProcess = new MarkProcess(this));
	}

	/**
	 * This must be called from the concrete super class constructor with a valid
	 * SoloannotationType to set up the module. would be nice to have done this 
	 * as an argument to the AnnotationModule constructor, but too many things
	 * to initialise, so has to be a call further down the constructor. 
	 * @param annotationType
	 */
	public void addAnnotationType(DataAnnotationType annotationType) {
		AnnotationHandler annotationHandler = annotationProcess.getMarkDataBlock().getAnnotationHandler();
		if (annotationHandler == null) {
			annotationProcess.getMarkDataBlock().setAnnotationHandler(annotationHandler = new AnnotationHandler(annotationProcess.getMarkDataBlock()));
		}
		annotationHandler.addAnnotationType(annotationType);
		SQLLoggingAddon sqlAddon = annotationType.getSQLLoggingAddon();
		if (sqlAddon != null) {
			annotationProcess.getAsql().addAddOn(sqlAddon);
		}
	}

	public MarkProcess getAnnotationProcess() {
		return annotationProcess;
	}

	public MarkDataBlock getAnnotationDataBlock() {
		return annotationProcess.getMarkDataBlock();
	}

}
