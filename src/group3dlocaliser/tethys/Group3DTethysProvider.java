package group3dlocaliser.tethys;

import PamguardMVC.PamDataBlock;
import group3dlocaliser.Group3DLocaliserControl;
import tethys.TethysControl;
import tethys.pamdata.AutoTethysProvider;

public class Group3DTethysProvider extends AutoTethysProvider {

	private Group3DLocaliserControl group3dLocaliserControl;

	public Group3DTethysProvider(TethysControl tethysControl, Group3DLocaliserControl group3dLocaliserControl, PamDataBlock pamDataBlock) {
		super(tethysControl, pamDataBlock);
		this.group3dLocaliserControl = group3dLocaliserControl;
	}

	@Override
	public boolean hasDetections() {
		return false;
	}

}
