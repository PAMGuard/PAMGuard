package qa;

import java.util.ListIterator;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamInstantProcess;
import qa.database.QASequenceLogging;
import qa.database.QASoundLogging;
import qa.database.QATestLogging;
import qa.operations.QAOpsDataBlock;
import qa.operations.QAOpsDataUnit;

public class QADataProcess extends PamInstantProcess {

	private QAControl qaControl;

	private QATestDataBlock testsDataBlock;

	private QASoundDataBlock soundsDataBlock;

	private QASequenceDataBlock sequenceDataBlock;
	
	private QASoundLogging qaLogging;

	private QASequenceLogging qaSequenceLogging;
	
	private QATestLogging qaTestLogging;

	private QAOpsDataBlock opsDataBlock;

	private boolean isGenerator;

	public QADataProcess(QAControl qaControl, boolean isGenerator) {
		super(qaControl);
		this.qaControl = qaControl;
		this.isGenerator = isGenerator;

		/**
		 * Put this first so that it's loaded before other data. 
		 */
		opsDataBlock = new QAOpsDataBlock(this);
		addOutputDataBlock(opsDataBlock);
		
		testsDataBlock = new QATestDataBlock("QA Tests", this, isGenerator);
		qaTestLogging = new QATestLogging(qaControl, testsDataBlock);
		testsDataBlock.SetLogging(qaTestLogging);
		addOutputDataBlock(testsDataBlock);	

		sequenceDataBlock = new QASequenceDataBlock(this, isGenerator);
		qaSequenceLogging = new QASequenceLogging(sequenceDataBlock);
		addOutputDataBlock(sequenceDataBlock);

		soundsDataBlock = new QASoundDataBlock(this, isGenerator);
		qaLogging = new QASoundLogging(qaControl, soundsDataBlock);
		addOutputDataBlock(soundsDataBlock);
		
		/*
		 * In normal mode need to leave logging of the sequence and sounds data 
		 * to the super detection or it all goes a bit wrong. 
		 * However in Viewer, seem to need these in place...
		 */
//		if (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW || isGenerator == false) {
//			sequenceDataBlock.SetLogging(qaSequenceLogging);
//			soundsDataBlock.SetLogging(qaLogging);
//		}

		qaTestLogging.setSubLogging(qaSequenceLogging);
		qaSequenceLogging.setSubLogging(qaLogging);
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
	 * @return the qaControl
	 */
	public QAControl getQaControl() {
		return qaControl;
	}

	/**
	 * @return the testsDataBlock
	 */
	public QATestDataBlock getTestsDataBlock() {
		return testsDataBlock;
	}

	/**
	 * @return the soundsDataBlock
	 */
	public QASoundDataBlock getSoundsDataBlock() {
		return soundsDataBlock;
	}

	/**
	 * @return the sequenceDataBlock
	 */
	public QASequenceDataBlock getSequenceDataBlock() {
		return sequenceDataBlock;
	}

	/**
	 * @return the qaLogging
	 */
	public QASoundLogging getQaLogging() {
		return qaLogging;
	}

	/**
	 * @return the qaSequenceLogging
	 */
	public QASequenceLogging getQaSequenceLogging() {
		return qaSequenceLogging;
	}

	/**
	 * @return the qaTestLogging
	 */
	public QATestLogging getQaTestLogging() {
		return qaTestLogging;
	}

	/**
	 * @return the opsDataBlock
	 */
	public QAOpsDataBlock getOpsDataBlock() {
		return opsDataBlock;
	}
	
	/**
	 * Get the ops data unit preceding the given time
	 * @param timeMilliseconds time to search
	 * @return preceding ops data unit. May be null
	 */
	public QAOpsDataUnit findOpsDataUnit(long timeMilliseconds) {
//		if (opsDataBlock.getUnitsCount() == 0) {
//		opsDataBlock.loadViewerData(0,  timeMilliseconds, null);	
//		}
		return opsDataBlock.getPreceedingUnit(timeMilliseconds);
	}

	/**
	 * Called back from load data in viewer mode to match up all data units with 
	 * their corresponding ops status. Ideally Tests, Sequences and Sounds will 
	 * all get an ops status. 
	 * @param qaDataBlock
	 */
	public void findOpsDataUnits(PamDataBlock qaDataBlock) {
		opsDataBlock.loadViewerData(0,  1, null);	
		synchronized(qaDataBlock.getSynchLock()) {
			ListIterator<PamDataUnit> it = qaDataBlock.getListIterator(0);
			while (it.hasNext()) {
				PamDataUnit dataUnit = it.next();
				if (dataUnit instanceof QADataUnit) {
					QADataUnit qaUnit = (QADataUnit) dataUnit;
					qaUnit.setQaOpsDataUnit(findOpsDataUnit(qaUnit.getTimeMilliseconds()));
				}
			}
		}
	}
}
