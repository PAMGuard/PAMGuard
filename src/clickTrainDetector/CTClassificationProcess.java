package clickTrainDetector;

import PamguardMVC.PamDataUnit;
import PamguardMVC.PamInstantProcess;
import PamguardMVC.PamObservable;
import PamguardMVC.debug.Debug;
import clickTrainDetector.layout.CTDataUnitGraphics;
import clickTrainDetector.layout.ClickTrainSymbolManager;
import clickTrainDetector.logging.ClickTrainDetLogging;
import clickTrainDetector.logging.ClickTrainDetSubLogging;
import pamScrollSystem.AbstractScrollManager;

/**
 * Process which classifies click trains. 
 * 
 * @author Jamie Macaulay
 *
 */
@SuppressWarnings("rawtypes")
public class CTClassificationProcess extends PamInstantProcess {

	/**
	 * Reference to the click train control.
	 */
	private ClickTrainControl clickTrainControl;
	
	/**
	 * Classified click train data block. 
	 */
	private ClickTrainDataBlock<CTDataUnit> clssfdClickTrainDataBlock;

	/**
	 * Click train detection logging to SQLite database. 
	 */
	private ClickTrainDetLogging clickTrainDetLogging;
	
	/**
	 * The number of minutes to load the data block before and after the time limits of a scroller. 
	 */
	private static final long LOADMINS = 30;

	public CTClassificationProcess(ClickTrainControl pamControlledUnit) {
		super(pamControlledUnit);
		this.clickTrainControl=pamControlledUnit;
		this.setParentDataBlock(clickTrainControl.getClickTrainDataBlock());
		
		clssfdClickTrainDataBlock= new ClickTrainDataBlock<CTDataUnit>(clickTrainControl, this, "Click Trains", 0); 
		clssfdClickTrainDataBlock.setOverlayDraw(new CTDataUnitGraphics(clssfdClickTrainDataBlock));
		
		clssfdClickTrainDataBlock.setPamSymbolManager(new ClickTrainSymbolManager(clssfdClickTrainDataBlock));
		clssfdClickTrainDataBlock.setCanClipGenerate(true);

		AbstractScrollManager.getScrollManager().addToSpecialDatablock(clssfdClickTrainDataBlock, 1000L*60*LOADMINS, 1000L*60*LOADMINS);
		
		if (pamControlledUnit.saveTrains) {
			clssfdClickTrainDataBlock.SetLogging(clickTrainDetLogging = new ClickTrainDetLogging(pamControlledUnit, clssfdClickTrainDataBlock));
			clickTrainDetLogging.setSubLogging(new ClickTrainDetSubLogging(clickTrainDetLogging, clssfdClickTrainDataBlock));
			//It's important to have this here to ensure super detections from click trains load in viewer mode
			AbstractScrollManager.getScrollManager().addToSpecialDatablock(clssfdClickTrainDataBlock);
		}
	
		setProcessName("Click Train Classification");

		addOutputDataBlock(clssfdClickTrainDataBlock);

	}
	
	/**
	 * Get the classified click train data block. This holds click trains which have been classified to a species. 
	 * @return the classified click train datablock. 
	 */
	public ClickTrainDataBlock getClssfdClickTrainDataBlock() {
		return clssfdClickTrainDataBlock;
	}


	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		Debug.out.println("CTClassificationProcess: new click train classification: " + o + " " + clickTrainControl.getClickTrainDataBlock()); 
		if (o == clickTrainControl.getClickTrainDataBlock()) {
			newClickTrainData((CTDataUnit) arg);
		}
	}
	
	public void updateData(PamObservable o, PamDataUnit arg) {
		Debug.out.println("CTClassificationProcess: update click train classification: " + o + " " + clickTrainControl.getClickTrainDataBlock()); 
		if (o == clickTrainControl.getClickTrainDataBlock()) {
			newClickTrainData((CTDataUnit) arg);
		}
	}

	/**
	 * Classify a new click train.
	 * @param dataUnit the data unit. 
	 */
	@SuppressWarnings("unchecked")
	public void newClickTrainData(CTDataUnit dataUnit) {
		//there are two types of classification, add a classification object to the data unit or setting a junk
		//flag which indicates the data unit should be deleted. 
		dataUnit = this.clickTrainControl.getClassifierManager().classify(dataUnit);
		
		//remove the data unit from the click train data block - it will either be junked or passed to classified click train
		//data block now.
		clickTrainControl.getClickTrainDataBlock().remove(dataUnit);
		
		//the data unit is flagged as a junk train by the pre-classifier. 
		if (dataUnit.isJunkTrain()) {
			//remove any click train that has a junk flag. 
			Debug.out.println("CTClassificationProcess: Is a JUNK train: ");
			dataUnit.removeAllSubDetections(); //need this to remove the click trains, probs good just for references to stuff. 
		}
		else {
			dataUnit.setParentDataBlock(clssfdClickTrainDataBlock);
			clssfdClickTrainDataBlock.remove(dataUnit); //just in case 
			clssfdClickTrainDataBlock.addPamData(dataUnit);
		}
	}

	@Override
	public void pamStart() {

		System.out.println("-----||||| This parent block: " +  this.getParentDataBlock() + "  " + this.getParentProcess()); 
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
		
	}
	
}
