package ravendata;

import PamguardMVC.PamProcess;
import ravendata.swing.RavenGraphics;

public class RavenProcess extends PamProcess {

	private RavenControl ravenControl;
	
	private RavenDataBlock ravenDataBlock;
	
	private RavenLogging ravenLogging;

	public RavenProcess(RavenControl pamControlledUnit) {
		super(pamControlledUnit, null);
		this.ravenControl = pamControlledUnit;
		ravenDataBlock = new RavenDataBlock(this, 0);
		addOutputDataBlock(ravenDataBlock);
		ravenLogging = new RavenLogging(pamControlledUnit, ravenDataBlock);
		ravenDataBlock.SetLogging(ravenLogging);
		ravenDataBlock.setOverlayDraw(new RavenGraphics(ravenDataBlock));
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
