package clickDetector;

import java.util.ListIterator;

import PamguardMVC.PamProcess;

@Deprecated
public class ClickTrainDataBlock extends ClickGroupDataBlock<ClickTrainDetection> {

	ClickControl clickControl;
	
	ClickTrainDetection lastShouldPlot;
	
	public ClickTrainDataBlock(ClickControl clickControl, String dataName, PamProcess parentProcess, int channelMap) {
		super(ClickTrainDetection.class, dataName, parentProcess, channelMap);
		this.clickControl = clickControl;
	}

	@Override
	synchronized public void addPamData(ClickTrainDetection clickTrainDetection) {
		setShouldPlot(clickTrainDetection, getUnitsCount()-1);
		super.addPamData(clickTrainDetection);
	}
	
	synchronized private void setShouldPlot(ClickTrainDetection clickTrainDetection, int prevDetection) {
		/*
		 * work back and see if anything has been plotted at a close enough angle in the
		 * set plot time 
		 */
		boolean should = true;
		ClickTrainDetection otherDetection;
		ListIterator<ClickTrainDetection> ctdIterator = getListIterator(prevDetection-1);
		while (ctdIterator.hasPrevious()) {
			otherDetection = ctdIterator.previous();
			if ((clickTrainDetection.getTimeMilliseconds() - otherDetection.getTimeMilliseconds()) / 1000 > 
				clickControl.clickParameters.minTimeSeparation) {
				break;
			}
			if (otherDetection.isShouldPlot() &&  
					Math.abs(clickTrainDetection.firstClickAngle - otherDetection.firstClickAngle) * 180 / Math.PI < 
					clickControl.clickParameters.minBearingSeparation) {
				should = false;
			}
		}
		setShouldPlot(clickTrainDetection, should);
		
		// work out if this click train is too close to an existing one to be worth plotting.
		// first work backwards through the click train list and see where the last one which 
//		// should be plotted is. 
//		if (lastShouldPlot == null) { // always plot the first one
//			setShouldPlot(clickTrainDetection, true);
//			return;
//		}
//		double angDiff = Math.abs(clickTrainDetection.firstClickAngle - lastShouldPlot.firstClickAngle) * 180 / Math.PI;
//		double timeDiff = (clickTrainDetection.getTimeMilliseconds() - lastShouldPlot.getTimeMilliseconds()) / 1000.;
//		boolean should = (angDiff > clickControl.clickParameters.minBearingSeparation || 
//				timeDiff > clickControl.clickParameters.minTimeSeparation);
//		setShouldPlot(clickTrainDetection, should);
		
	}
	
	private void setShouldPlot(ClickTrainDetection clickTrainDetection, boolean shouldPlot) {
		if (shouldPlot) {
			lastShouldPlot = clickTrainDetection;
		}
		clickTrainDetection.setShouldPlot(shouldPlot);
	}

	protected void resetShouldPlots() {
		// called when plot parameters have changed to rest flags in all click trains.
		lastShouldPlot = null;
		ListIterator<ClickTrainDetection> ctdIterator = getListIterator(0);
		ClickTrainDetection ctd;
		int ind = 0;
		while (ctdIterator.hasNext()) {
			ctd = ctdIterator.next();
			if (ind == 0) {
				setShouldPlot(ctd, true);
			}
			else {
				setShouldPlot(ctd, ind-1);
			}
			ind++;
		}
	}
}
