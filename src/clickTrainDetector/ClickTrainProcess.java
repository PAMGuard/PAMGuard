package clickTrainDetector;

import PamController.PamController;
import PamController.status.ProcessCheck;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamInstantProcess;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import clickDetector.ClickDetection;
import clickTrainDetector.layout.CTDataUnitGraphics;
import clickTrainDetector.layout.UnconfirmedCTSymbolManager;
import cpod.CPODClick;
import pamScrollSystem.AbstractScrollManager;

/**
 * 
 * The core of the click train detector. Runs the click train detector on click detections.
 * 
 * @author Jamie Macaulay 
 *
 */
public class ClickTrainProcess extends PamProcess {

	/**
	 * The source data block 
	 */
	private PamDataBlock sourceDataBlock;

	/**
	 * Reference to the click train control. 
	 */
	private ClickTrainControl clickTrainControl;

	/**
	 * The click train data block ciontains all confirmed click trains but not classified click trains. 
	 */
	private ClickTrainDataBlock<CTDataUnit> clickTrainDataBlock;

	/**
	 * A temporary data block used in real time to store unconfirmed click trains. 
	 */
	private ClickTrainDataBlock<TempCTDataUnit> clickTrainTempBlock;

	/**
	 * Process check for the click train detector. 
	 */
	private CTProcessCheck processCheck;

	public ClickTrainProcess(ClickTrainControl pamControlledUnit) {
		super(pamControlledUnit, null);
		this.clickTrainControl=pamControlledUnit; 

		this.processCheck=new CTProcessCheck(this);


		//create the output data block
		clickTrainDataBlock= new ClickTrainDataBlock<CTDataUnit>(clickTrainControl, this, "Possible Click Trains", 0); 
		clickTrainDataBlock.setCanClipGenerate(true);


		//		if (pamControlledUnit.saveTrains) {
		//			clickTrainDataBlock.SetLogging(clickTrainDetLogging = new ClickTrainDetLogging(pamControlledUnit, clickTrainDataBlock));
		//			clickTrainDetLogging.setSubtable(new ClickTrainDetSubLogging(clickTrainDetLogging, clickTrainDataBlock));
		//		}

		clickTrainTempBlock=  new ClickTrainDataBlock<TempCTDataUnit>(clickTrainControl, this, "Unconfirmed Click Trains", 0); 
		//if (!clickTrainControl.isViewer()) {
		clickTrainTempBlock.setPamSymbolManager(new UnconfirmedCTSymbolManager(pamControlledUnit, clickTrainDataBlock));
		clickTrainTempBlock.setOverlayDraw(new CTDataUnitGraphics(clickTrainTempBlock));
		//}

		addOutputDataBlock(clickTrainTempBlock);
		addOutputDataBlock(clickTrainDataBlock);

	}

	@Override
	public void destroyProcess() {
		super.destroyProcess();
		AbstractScrollManager.getScrollManager().removeFromSpecialDatablock(clickTrainDataBlock);
	}

	@Override
	public void setupProcess() {
		super.setupProcess();

		//TODO - this is a bit messy. Ideally probably should have the datablock's long name in the settings but 
		//this would break previous configurations...
//		sourceDataBlock = PamController.getInstance().getDataBlock(ClickDetection.class, 
//				getClickTrainParams().dataSourceName);
		/*
		 * Identify by long name since that is unique, otherwise doesn't work with multiple click detectors. 
		 */
		sourceDataBlock = PamController.getInstance().getDataBlockByLongName(getClickTrainParams().dataSourceName);
		if (sourceDataBlock == null) {
			// otherwise find any click detector. 
			sourceDataBlock = PamController.getInstance().getDataBlock(ClickDetection.class, 0);
		}

		if (sourceDataBlock==null) {
			sourceDataBlock = PamController.getInstance().getDataBlock(CPODClick.class, 
					getClickTrainParams().dataSourceName);
		}
		
		//System.out.println("CPOD sample rate: " + sourceDataBlock.getSampleRate()); 
		//		if (sourceDataBlock!=null) System.out.println("Click train process: Source data" +   sourceDataBlock.getDataName());
		//		else System.out.println("The source data is null: " + getClickTrainParams().dataSourceName); 


		setParentDataBlock(sourceDataBlock);

		prepareProcess();
		
		//System.out.println("CLICK TRAIN sample rate: " + this.getSampleRate()); 

	}


	@Override
	public void newData(PamObservable o, @SuppressWarnings("rawtypes") PamDataUnit arg) {
		if (o == sourceDataBlock) {
			newClickData((ClickDetection) arg);
			processCheck.newInput(o, arg);
		}
	}

	@Override
	public ProcessCheck getProcessCheck() {
		return processCheck;
	}

	/**
	 * New click detection has arrived. 
	 * @param click - the new click
	 */
	public void newClickData(PamDataUnit newClick) {
		if (!clickTrainControl.getClickTrainParams().useDataSelector || clickTrainControl.getDataSelector().scoreData(newClick)>0) {	
//			System.out.println("ClickTrainDetector2: " + newClick + 
//					" score: " + clickTrainControl.getDataSelector().scoreData(newClick) + 
//					" freq: " + newClick.getFrequency()[1]);	
			this.clickTrainControl.getCurrentCTAlgorithm().newDataUnit(newClick); 
		}
	}

	/**
	 * Get the click train parameters
	 * @return
	 */
	private ClickTrainParams getClickTrainParams() {
		return this.clickTrainControl.getClickTrainParams();
	}

	@Override
	public void pamStart() {
		if (!clickTrainControl.isViewer()) {
			clickTrainControl.update(ClickTrainControl.PROCESSING_START);
		}
	}

	@Override
	public void pamStop() {
		if (!clickTrainControl.isViewer()) {
			clickTrainControl.update(ClickTrainControl.PROCESSING_END);
		}
	}

	/**
	 * Get the main click train output data block
	 * @return - the click train output data block. 
	 */
	public ClickTrainDataBlock<CTDataUnit> getClickTrainDataBlock() {
		return this.clickTrainDataBlock;
	}


	/**
	 * Get the temporary click train data block. This is used to store unconfirmed tracks 
	 * for painting on displays. 
	 * @return the temporary click train data block. 
	 */
	public ClickTrainDataBlock<TempCTDataUnit> getUnconfirmedCTDataBlock() {
		return clickTrainTempBlock;
	}

	/**
	 * Get the clcik trian controller. 
	 * @return the click train controlle.r 
	 */
	public ClickTrainControl getClickTrainControl() {
		return this.clickTrainControl; 
	}
	
	
	@Override
	public void masterClockUpdate(long timeMilliseconds, long sampleNumber) {
		super.masterClockUpdate(timeMilliseconds, sampleNumber);
		this.clickTrainControl.getCurrentCTAlgorithm().update(ClickTrainControl.CLOCK_UPDATE, Long.valueOf(timeMilliseconds));
	}


	//	/**
	//	 * Get the temporary click train data block. This is used to store unconfirmed tracks 
	//	 * for painting on displays. 
	//	 * @return the temporary click train data block. 
	//	 */
	//	public ClickTrainDataBlock<TempCTDataUnit> getUnconfirmedCTDataBlock() {
	//		return getClickTrainTempBlock();
	//	}
	//	
	//
	//	/**
	//	 * Check all the SQL Logging additions are set up correctly. 
	//	 */
	//	protected void sortSQLLogging() {
	//		if (annotationHandler.addAnnotationSqlAddons(detectionGroupLogging) > 0) {
	//			// will have to recheck the table in the database. 
	//			DBControlUnit dbc = DBControlUnit.findDatabaseControl();
	//			if (dbc != null) {
	//				dbc.getDbProcess().checkTable(detectionGroupLogging.getTableDefinition());
	//			}
	//		}
	//
	//	}

}

