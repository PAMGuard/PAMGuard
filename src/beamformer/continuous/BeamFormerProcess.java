package beamformer.continuous;

import java.util.ArrayList;

import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import beamformer.BeamFormerBaseProcess;
import beamformer.BeamGroupProcess;

/**
 * Main beam forming process. Creates a number of channel group 
 * processes in case separate beam formers are running on separate channel groups. 
 * @author dg50
 *
 */
public class BeamFormerProcess extends BeamFormerBaseProcess {

	private BeamFormerControl beamFormerControl;

	public BeamFormerProcess(BeamFormerControl beamFormerControl) {
		super(beamFormerControl, true);
		this.beamFormerControl = beamFormerControl;
	}

	@Override
	public void prepareProcess() {
		super.prepareProcess();
		ArrayList<BeamGroupProcess> groupProcesses = this.getGroupProcesses();
		if (groupProcesses == null) {
			return;
		}
		for (BeamGroupProcess aProcess:groupProcesses) {
			aProcess.getBeamFormerAlgorithm().setKeepFrequencyInformation(true);
		}
		
	}

	@Override
	public void newData(PamObservable o, PamDataUnit pamDataUnit) {
		makeContinuousBeams(o, pamDataUnit);
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
