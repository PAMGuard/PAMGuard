package clipgenerator.clipDisplay;

import PamUtils.Coordinate3d;
import PamUtils.PamCoordinate;
import PamView.GeneralProjector;
import Spectrogram.TimeFrequencyPoint;

public class ClipDataProjector extends GeneralProjector<TimeFrequencyPoint> {

	private ClipDisplayPanel clipDisplayPanel;
	
	private long clipStart;
	
	private ClipDisplayUnit currentClickedUnit;
	
//	private double minFrequency = 0;
//	private double maxFrequency = 1;
	
	public ClipDataProjector(ClipDisplayPanel clipDisplayPanel) {
		this.clipDisplayPanel = clipDisplayPanel;
		setParmeterType(0, GeneralProjector.ParameterType.TIME);
		setParmeterType(1, GeneralProjector.ParameterType.FREQUENCY);
	}
	
	public void setClipStart(long timeMillis) {
		this.clipStart = timeMillis;
	}


	@Override
	public Coordinate3d getCoord3d(TimeFrequencyPoint data) {
		return getCoord3d(data.getTimeMilliseconds(), data.getFrequency(), 0);
	}

	@Override
	public TimeFrequencyPoint getDataPosition(PamCoordinate screenPos) {
		ClipDisplayParameters clipParams = clipDisplayPanel.clipDisplayParameters;
		int halfFFTLen = 1<<(clipParams.getLogFFTLength()-1);
		int clipHeight = (int) (halfFFTLen * clipParams.imageVScale);
		double x = screenPos.getCoordinate(0);
		double y = screenPos.getCoordinate(1);
		double fMax = clipDisplayPanel.getSampleRate() / 2. * clipParams.frequencyScale;
		double tScale = clipDisplayPanel.getSampleRate()/1000./halfFFTLen * clipParams.imageHScale;
		double timeMillis = x/tScale+clipStart;
		double freqHz = (clipHeight-y)*fMax/clipHeight;
		return new TimeFrequencyPoint((long) timeMillis, freqHz);
	}

	@Override
	public Coordinate3d getCoord3d(double timeMillis, double freqHz, double d3) {
		ClipDisplayParameters clipParams = clipDisplayPanel.clipDisplayParameters;
		int halfFFTLen = 1<<(clipParams.getLogFFTLength()-1);
		int clipHeight = (int) (halfFFTLen * clipParams.imageVScale);
		double fMax = clipDisplayPanel.getSampleRate() / 2. * clipParams.frequencyScale;
		double fPix = clipHeight - freqHz/fMax*clipHeight;
		double tBins = (timeMillis-clipStart)*clipDisplayPanel.getSampleRate()/1000./halfFFTLen * clipParams.imageHScale;
		
		
		return new Coordinate3d(tBins, fPix);
	}

	/**
	 * @return the currentClickedUnit
	 */
	public ClipDisplayUnit getCurrentClickedUnit() {
		return currentClickedUnit;
	}

	/**
	 * @param currentClickedUnit the currentClickedUnit to set
	 */
	public void setCurrentClickedUnit(ClipDisplayUnit currentClickedUnit) {
		this.currentClickedUnit = currentClickedUnit;
	}

}
