package clickTrainDetector;

import PamDetection.LocContents;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamInstantProcess;
import PamguardMVC.PamObservable;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.debug.Debug;
import annotation.localise.targetmotion.TMAnnotation;
import annotation.localise.targetmotion.TMAnnotationOptions;
import annotation.localise.targetmotion.TMAnnotationType;
import clickTrainDetector.localisation.CTLocalisation;
import clickTrainDetector.localisation.CTMAnntoationType;
import generalDatabase.SQLLogging;

/**
 * Process which, if conditions are met, attempts to localise a click train. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class CTLocalisationProcess extends PamInstantProcess {

	/**
	 * Reference click train control. 
	 */
	private ClickTrainControl clickTrainControl;

	/**
	 * Target motion annotation for the click train detector. 
	 */
	private TMAnnotationType tmAnnotationType;

	public CTLocalisationProcess(ClickTrainControl clickTrainControl) {
		super(clickTrainControl);

		this.clickTrainControl=clickTrainControl;
		this.setParentDataBlock(clickTrainControl.getClssfdClickTrainDataBlock());

		tmAnnotationType = new CTMAnntoationType(clickTrainControl);
		TMAnnotationOptions tmAnnotationOptions = clickTrainControl.getClickTrainParams().ctLocParams;
		tmAnnotationOptions.getLocalisationParams().setIsSelected(0, false);
		tmAnnotationOptions.getLocalisationParams().setIsSelected(1, false);
		tmAnnotationOptions.getLocalisationParams().setIsSelected(2, true);
		
//		SQLLogging locLogging = clickTrainControl.getClssfdClickTrainDataBlock().getLogging();
//		locLogging.addAddOn(tmAnnotationType.getSQLLoggingAddon());

		tmAnnotationType.setAnnotationOptions(tmAnnotationOptions);

		setProcessName("Click Train Localisation");
	}


	@Override
	public void newData(PamObservable o, @SuppressWarnings("rawtypes") PamDataUnit ctDataUnit) {
		//System.out.println("Hello new click train data: " + o + " " + clickTrainControl.getClickTrainDataBlock()); 
		if (o == clickTrainControl.getClssfdClickTrainDataBlock()) {
			newClickTrainData((CTDataUnit) ctDataUnit);
		}
	}

	private void newClickTrainData(CTDataUnit ctDataUnit) {
		
//		if (this.clickTrainControl.isViewer()) return;
		//shall we attempt to localise and annotate the data unit. 
		//do a few tests first. 
		if (clickTrainControl.getParentDataBlock().getLocalisationContents().hasLocContent(LocContents.HAS_BEARING) && clickTrainControl.getClickTrainParams().ctLocParams.shouldloc) {
			
			CTLocalisation ctLoc = new CTLocalisation(ctDataUnit, null, this.clickTrainControl.getClickTrainParams().ctLocParams); 

			int n = ctDataUnit.getSubDetectionsCount();
		
			if (n> clickTrainControl.getClickTrainParams().ctLocParams.minDataUnits && 
					ctDataUnit.getAngleRange()>= clickTrainControl.getClickTrainParams().ctLocParams.minAngleRange) {
				
				TMAnnotation annotation  = tmAnnotationType.autoAnnotate(ctDataUnit); 
								
				if (annotation!=null && annotation.getGroupLocalisation()!=null) {
					//merge the two localisations 
//					Debug.out.println("CTLocalisationProcess: !!!: " + annotation.getGroupLocalisation()); 
					ctLoc.setTargetMotionResult(annotation.getGroupLocalisation().getGroupLocaResult(0));
					for (int i=1; i<annotation.getGroupLocalisation().getGroupLocResults().length; i++) {
						ctLoc.addGroupLocaResult(annotation.getGroupLocalisation().getGroupLocResults()[i]); 
					}
//					Debug.out.println("HAS a LATLONG: " +ctLoc.hasLocContent(LocContents.HAS_LATLONG)); 
				}
			}
			else {
				Debug.out.println("CTLocalisationProcess: Click train NOT suitable for loc: angle range: " +
						Math.toDegrees(ctDataUnit.getAngleRange()) + " n units: " + n + 
						" min allowed angle: " + Math.toDegrees(clickTrainControl.getClickTrainParams().ctLocParams.minAngleRange)); 
			}

			ctDataUnit.setLocalisation(ctLoc);
			
			/**
			 * DO NOT call this. It's not needed since this is already in a callback from 
			 * the addData function to the datablock, so this will get called anyway before 
			 * the database get's called. 
			 */
			//update the data block to force annotation write. 
//			ctDataUnit.getParentDataBlock().updatePamData(ctDataUnit, PamCalendar.getTimeInMillis());
		}
	}


	@Override
	public void pamStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
	}
	

	/**
	 * Get the target motion type. 
	 * @return the target motion type annotation. 
	 */
	public TMAnnotationType getTmAnnotationType() {
		return tmAnnotationType;
	}

	/**
	 * Set the localisation parameters in the click train detector. 
	 * @param clickTrainParams - the localisation parameters. 
	 */
	public void setTMlocParams(ClickTrainParams clickTrainParams) {
		TMAnnotationOptions tmAnnotationOptions = clickTrainControl.getClickTrainParams().ctLocParams;
		tmAnnotationType.setAnnotationOptions(tmAnnotationOptions);
	}

}
