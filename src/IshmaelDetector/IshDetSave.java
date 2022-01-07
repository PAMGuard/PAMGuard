/**
 * 
 */
package IshmaelDetector;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;

public class IshDetSave extends PamObserverAdapter
{
	IshDetControl ishDetControl;
	public IshDetection inputData;
	PamDataBlock ishPeakDataBlock;
	float sampleRate;

	public IshDetSave(IshDetControl ishDetControl) {
		super();
		this.ishDetControl = ishDetControl;
		ishPeakDataBlock = ishDetControl.ishPeakProcess.outputDataBlock;
		ishPeakDataBlock.addObserver(this);	//call update() when unit added
	}

	public void addData(PamObservable o, PamDataUnit arg1) {
		IshDetection arg = (IshDetection)arg1; 
		//inputData = arg.detData;
		//Here we have to save these segments.  So open a 
		//an ascii file with the encoded date and time, and
		//save the positive detection with ten seconds of 
		//data to either side.
	}

	public void saveData() {
	}

	public String getObserverName() {
		return "File Saver for Energy Sum";
	}

	public void setSampleRate(float sampleRate, boolean notify) {
		this.sampleRate = sampleRate;
	}

	public void prepareForRun() {}
}
