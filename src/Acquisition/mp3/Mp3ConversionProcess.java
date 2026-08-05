package Acquisition.mp3;

import PamController.PamControlledUnit;
import PamController.PamController;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.PamRawDataBlock;

public class Mp3ConversionProcess  extends PamProcess {
	
	private Mp3ConversionControl controller;
	private Mp3OutputBuffer outputThread;
	private CompressedRawDataBlock outputDataBlock;
	private Mp3ConversionParams params;
	private float sourceSampleRate;


	public Mp3ConversionProcess(Mp3ConversionControl pamControlledUnit) {
		super(pamControlledUnit, null);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void prepareProcess() {
		System.out.println("Calling prepare process on decimator");
		super.setupProcess();
		newSettings();
	}
	
	protected synchronized void newSettings() {
		if(this.controller!=null) {
			this.params = controller.converterParams;
		}
		PamRawDataBlock rawDataBlock = PamController.getInstance().
				getRawDataBlock(this.params.rawDataSource);
		if (rawDataBlock != getParentDataBlock()) {
			setParentDataBlock(rawDataBlock);
		}
		if (getParentDataBlock() != null) {
			sourceSampleRate = rawDataBlock.getSampleRate();
			if (sourceSampleRate == 0) {
				return;
			}
			this.params.channelMap &= getParentDataBlock().getChannelMap();
			outputDataBlock.setChannelMap(this.params.channelMap);
			this.setSampleRate(sourceSampleRate, true);
			//setupDecimator();
		}
	}
	
	
	private class Mp3OutputBuffer implements Runnable{
		
		@Override
		public void run() {
			
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

}
