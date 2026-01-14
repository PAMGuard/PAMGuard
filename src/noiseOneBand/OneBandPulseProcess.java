package noiseOneBand;

import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import noiseOneBand.offline.OneBandDatagramProvider;

public class OneBandPulseProcess extends PamProcess {

	private OneBandDataBlock pulseDataBlock;
	private OneBandControl dbhtControl;
	private PulseChannelProcess[] channelProcesses = new PulseChannelProcess[PamConstants.MAX_CHANNELS];
	
	public OneBandPulseProcess(OneBandControl dbhtControl) {
		super(dbhtControl, null);
		this.dbhtControl = dbhtControl;
		setParentDataBlock(dbhtControl.getOneBandProcess().getMeasureDataBlock());
		pulseDataBlock = new OneBandDataBlock(dbhtControl.getUnitName() + " pulses", dbhtControl, this, 0);
		pulseDataBlock.setBinaryDataSource(new OneBandDataSource(dbhtControl, pulseDataBlock, "Pulses"));
		pulseDataBlock.SetLogging(new OneBandLogging(dbhtControl, pulseDataBlock));
		pulseDataBlock.setDatagramProvider(new OneBandDatagramProvider(dbhtControl));
		addOutputDataBlock(pulseDataBlock);
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#prepareProcess()
	 */
	@Override
	public void prepareProcess() {
		OneBandDataBlock mBlock = dbhtControl.getOneBandProcess().getMeasureDataBlock();
		if (mBlock == null) return;
		int chanMap = mBlock.getChannelMap();
		pulseDataBlock.setChannelMap(chanMap);
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((1<<i & chanMap) == 0) {
				continue;
			}
			if (channelProcesses[i] == null) {
				channelProcesses[i] = new PulseChannelProcess(i);
			}
			channelProcesses[i].prepare();
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
	
	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#newData(PamguardMVC.PamObservable, PamguardMVC.PamDataUnit)
	 */
	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		OneBandDataUnit du = (OneBandDataUnit) arg;
		int chan = PamUtils.getSingleChannel(du.getChannelBitmap());
		if (channelProcesses[chan] != null) {
			channelProcesses[chan].newData(du);
		}
	}

	private class PulseChannelProcess {
		private int iChan;
		
		private OneBandDataUnit prevUnit;
		
		private double prevRms = Double.NaN;
		
		private boolean pulseOn = false;

		private double peakMax;

		private double pulseSEL;

		private double pulseP2P;

		private double pulse02P;

		private long pulseStart;

		private long pulseEnd;

		private long pulseStartSample;

		private int pulseCount;
		
		public PulseChannelProcess(int iChan) {
			super();
			this.iChan = iChan;
		}

		public void prepare() {
			prevUnit = null;
			prevRms = Double.NaN;
			pulseOn = false;
		}
		
		public void newData(OneBandDataUnit newUnit) {
			double rms = newUnit.getRms();
			if (prevRms == Double.NaN) {
				prevRms = rms;
				return;
			}
			if (!pulseOn) {
				if (rms-prevRms > dbhtControl.getParameters().singlePulseThreshold) {
					// start a pulse
					peakMax = rms;
					pulseOn = true;
					pulseSEL = Math.pow(10., rms/10.)*newUnit.getSampleDuration()/getSampleRate();
					pulseP2P = newUnit.getPeakPeak();
					pulse02P = newUnit.getZeroPeak();
					pulseEnd = pulseStart = newUnit.getTimeMilliseconds();
					pulseStartSample = newUnit.getStartSample();
					pulseCount = 1;
				}
			}
			else {
				if (peakMax-rms > dbhtControl.getParameters().singlePulseThreshold ||
						newUnit.getTimeMilliseconds()-pulseStart > dbhtControl.getParameters().maxPulseLength * 1000) {
					// end the pulse either because it's dropped back, or because it's got too long.
					OneBandDataUnit oneBandDataUnit = new OneBandDataUnit(pulseStart, 1<<iChan, pulseStartSample, 
							newUnit.getStartSample()-pulseStartSample);
					double pulseSecs = (newUnit.getTimeMilliseconds()-pulseStart)/1000;
					oneBandDataUnit.setRms(10*Math.log10(pulseSEL/pulseSecs));
					oneBandDataUnit.setZeroPeak(pulse02P);
					oneBandDataUnit.setPeakPeak(pulseP2P);
					oneBandDataUnit.setSEL(10.*Math.log10(pulseSEL), (int) pulseSecs);
					pulseDataBlock.addPamData(oneBandDataUnit);
					pulseOn = false;
//					System.out.println("Pulse ended length " + (int) ((newUnit.getTimeMilliseconds()-pulseStart)/1000));
				}
				else {
					// continue the pulse
					pulseSEL += Math.pow(10., rms/10.)*newUnit.getSampleDuration()/getSampleRate();
					pulseP2P = Math.max(pulseP2P, newUnit.getPeakPeak());
					pulse02P = Math.max(pulse02P, newUnit.getZeroPeak());
					pulseEnd = newUnit.getTimeMilliseconds();
					pulseCount++;
				}
			}
			prevRms = rms; // set so it doesn't immediately trigger again. 
			
		}
		
	}

	/**
	 * @return the pulseDataBlock
	 */
	public OneBandDataBlock getPulseDataBlock() {
		return pulseDataBlock;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.PamProcess#getProcessName()
	 */
	@Override
	public String getProcessName() {
		return "Pulse Detection";
	}

}
