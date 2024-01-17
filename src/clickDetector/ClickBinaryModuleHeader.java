package clickDetector;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleHeader;

public class ClickBinaryModuleHeader extends ModuleHeader implements ManagedParameters {

	public ClickBinaryModuleHeader(int moduleVersion) {
		super(moduleVersion);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean createHeader(BinaryObjectData binaryObjectData,
			BinaryHeader binaryHeader) {
		// TODO Auto-generated method stub
		return false;
	}

//	@Override
//	public byte[] getByteArray() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		return ps;
	}
}
