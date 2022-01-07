package clickDetector;

import PamDetection.RawDataUnit;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;

public class BinaryClickProcess extends PamProcess {

	private PamRawDataBlock binaryClickTimes;

	double[] binaryClickWave;

	int binaryPrepIndex = 0;

	private RawDataUnit binaryClickDataUnit;

	private int binaryClickRatio = 200;

	private ClickControl clickControl;

	private PamRawDataBlock rawBlock;

	private ClickDataBlock clickBlock;

	public BinaryClickProcess(ClickControl clickControl) {
		super(clickControl, null);
		this.clickControl = clickControl;

		binaryClickTimes = new PamRawDataBlock("Click Deltas", this, 1, 1);
		addOutputDataBlock(binaryClickTimes);

		prepareProcess();
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void newData(PamObservable o, PamDataUnit arg) {
		if (o == rawBlock) {
			newRawData((RawDataUnit) arg);
		}
		else if (o == clickBlock) {
			newClickData((ClickDetection) arg); 
		}
	}

	private void newClickData(ClickDetection newClick) {
		int s  = (int) ((newClick.getStartSample()+newClick.getSampleDuration())/binaryClickRatio - binaryClickDataUnit.getStartSample());
			
		if (s >= 0 && s < binaryClickWave.length) {
			binaryClickWave[s] += 1;
//			System.out.println("Add binary click data at sample " + s);
		}
		else {
			System.err.println("Can't Add binary click data at sample " + s);
		}
	}
	private void newRawData(RawDataUnit newRawData){
		if (newRawData.getChannelBitmap() == 1) {
			if (binaryPrepIndex++ % binaryClickRatio == 0) {
				if (binaryClickWave != null) {
					binaryClickDataUnit.setRawData(binaryClickWave, true);
					binaryClickTimes.addPamData(binaryClickDataUnit);
				}
				binaryClickWave = new double[newRawData.getSampleDuration().intValue()];
				binaryClickDataUnit = new RawDataUnit(newRawData.getTimeMilliseconds(), 1, 
						newRawData.getStartSample() / binaryClickRatio, binaryClickWave.length);

//				System.out.println("Made new block at raw sample " + newRawData.getStartSample() + 
//						" new smaple " + binaryClickDataUnit.getStartSample());
			}
		}
	}

	@Override
	public void setupProcess() {
		super.setupProcess();


		/**
		 * Subscribe to the parents raw data block and clicks
		 */
		ClickDetector clickDetector = clickControl.clickDetector;
		if (clickDetector == null) return;
		rawBlock = clickDetector.getRawSourceDataBlock(0);
		if (rawBlock != null) {
//			rawBlock.addObserver(this);
			setParentDataBlock(rawBlock);
		}

		clickBlock = clickDetector.getClickDataBlock();
		if (clickBlock != null) {
			clickBlock.addObserver(this);
		}

		//		binaryClickTimes.set
		this.setSampleRate(clickDetector.getSampleRate(), true);
	}

	@Override
	public void prepareProcess() {
		super.prepareProcess();		
		binaryPrepIndex = 0;
		binaryClickDataUnit = null;
		binaryClickWave = null;
		setupProcess();

	}

	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		sampleRate /= binaryClickRatio;
		super.setSampleRate(sampleRate, notify);
	}

}
