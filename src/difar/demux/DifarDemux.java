package difar.demux;

import java.awt.Component;
import java.awt.Window;

import difar.DemuxObserver;
import difar.DifarDataUnit;
import difar.DifarParameters;

public abstract class DifarDemux {

	public abstract boolean configDemux(DifarParameters difarParams, double sampleRate);
	
	public abstract DifarResult processClip(double[] difarClip, double sampleRate, int decimationFactor, DemuxObserver observer, 
			DifarDataUnit difarDataUnit);
	
	public abstract boolean hasOptions();
	
	public abstract boolean showOptions(Window window, DifarParameters difarParams);
	
	public abstract Component getDisplayComponent();
	
}
