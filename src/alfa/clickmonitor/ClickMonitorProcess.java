package alfa.clickmonitor;

import javax.swing.JComponent;

import PamController.PamControlledUnit;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import alfa.ALFAControl;
import alfa.clickmonitor.eventaggregator.ClickAggregateDataBlock;
import alfa.clickmonitor.eventaggregator.ClickEventAggregate;
import alfa.clickmonitor.swing.ClickAggregatOverlayGraphics;
import alfa.clickmonitor.swing.ClickMonitorComponent;
import clickDetector.ClickControl;
import clickDetector.offlineFuncs.OfflineEventDataUnit;
import clickTrainDetector.ClickTrainControl;
import detectiongrouplocaliser.DetectionGroupDataUnit;

/**
 * PAM Process which will automatically subscribe to the click train detector output and prepare status messages for display and transmission
 * @author dg50
 *
 */
public class ClickMonitorProcess extends PamProcess {

	private ALFAControl bigBrotherControl;
//	private ClickControl clickControl;
	private ClickTrainControl clickTrainControl;
	
	private SimpleClickEventAggregator clickEventAggregator;
	
	private ClickAggregateDataBlock clickAggregateDataBlock;
	private ClickMonitorComponent clickMonitorComponent;

	public ClickMonitorProcess(ALFAControl bigBrotherControl) {
		super(bigBrotherControl, null);
		this.bigBrotherControl = bigBrotherControl;
		clickEventAggregator = new SimpleClickEventAggregator(1000e6);
		clickAggregateDataBlock = new ClickAggregateDataBlock("Sperm Whale Groups", this, 0);
		clickAggregateDataBlock.setNaturalLifetime(3600);
		clickAggregateDataBlock.setOverlayDraw(new ClickAggregatOverlayGraphics(clickAggregateDataBlock));
		addOutputDataBlock(clickAggregateDataBlock);
	}

	@Override
	public void pamStart() {
		clickEventAggregator.reset();
	}

	@Override
	public void pamStop() {
	}
	
	public void setClickTrainDetector(ClickTrainControl clickTrainControl) {
		if (this.clickTrainControl != clickTrainControl) {
			this.clickTrainControl = clickTrainControl;
			if (clickTrainControl != null) {
				setParentDataBlock(clickTrainControl.getClssfdClickTrainDataBlock());
			}
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#setupProcess()
	 */
	@Override
	public void setupProcess() {
		super.setupProcess();
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#newData(PamguardMVC.PamObservable, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		if (arg instanceof DetectionGroupDataUnit) {
			newClickEvent((DetectionGroupDataUnit) arg);
		}
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#updateData(PamguardMVC.PamObservable, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void updateData(PamObservable o, PamDataUnit arg) {
		if (arg instanceof DetectionGroupDataUnit) {
			newClickEvent((DetectionGroupDataUnit) arg);
		}
	}

	private void newClickEvent(DetectionGroupDataUnit detectionGroupDataUnit) {
		aggregateEvent(detectionGroupDataUnit);		
	}


	/**
	 * Bring click events together into larger aggregations to avoid transmitting to0 much stuff
	 * @param detectionGroupDataUnit. 
	 */
	private void aggregateEvent(DetectionGroupDataUnit detectionGroupDataUnit) {
		ClickEventAggregate aggregateEvent = clickEventAggregator.aggregateData(detectionGroupDataUnit);
		if (aggregateEvent == null) {
			bigBrotherControl.updateClickInformation(null, detectionGroupDataUnit);
			return;
		}
		if (aggregateEvent.getParentDataBlock() == null) {
			clickAggregateDataBlock.addPamData(aggregateEvent);
		}
		else {
			clickAggregateDataBlock.updatePamData(aggregateEvent, aggregateEvent.getLastUpdateTime());
		}
		bigBrotherControl.updateClickInformation(aggregateEvent, detectionGroupDataUnit);
	}

	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		return 1000*3600;
	}
	
	public JComponent getSwingComponent() {
		if (clickMonitorComponent == null) {
			clickMonitorComponent = new ClickMonitorComponent(this);
		}
		return clickMonitorComponent.getComponent();
	}

	/**
	 * @return the clickControl
	 */
	public ClickTrainControl getClickTrainControl() {
		return clickTrainControl;
	}

	/**
	 * @return the clickEventAggregator
	 */
	public SimpleClickEventAggregator getClickEventAggregator() {
		return clickEventAggregator;
	}

	/**
	 * @return the clickAggregateDataBlock
	 */
	public ClickAggregateDataBlock getClickAggregateDataBlock() {
		return clickAggregateDataBlock;
	}

}
