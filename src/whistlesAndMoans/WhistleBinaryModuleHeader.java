package whistlesAndMoans;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleHeader;

public class WhistleBinaryModuleHeader extends ModuleHeader implements Serializable, ManagedParameters {
	
	private static final long serialVersionUID = 1L;
	
	public int delayScale = 1;

	public WhistleBinaryModuleHeader(int moduleVersion) {
		super(moduleVersion);
	}

	@Override
	public boolean createHeader(BinaryObjectData binaryObjectData,
			BinaryHeader binaryHeader) {			// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}
}
