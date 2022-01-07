package binaryFileStorage;

public class SecondaryBinaryStore extends BinaryStore {
	
	public static final String unitType = "Secondary Binary Store";

	public SecondaryBinaryStore(String unitName) {
		super(unitType, unitName);
//		getBinaryStoreSettings().channelShift = 4;
	}

	@Override
	public String getUnitType() {
		return unitType;
	}

}
