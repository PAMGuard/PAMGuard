package IshmaelDetector;

import java.io.File;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;

/**
 * Beginning of a binary data source for the Ishameal detector to save the 
 * Ishmael data so new peaks can be picked out. 
 * TODO
 * @author Jamie Macaulay
 *
 */
public class IshmaelBinaryDataSource extends BinaryDataSource {
	
	/**
	 * Stream name
	 */
	private String streamName;

	/**
	 * The Ishmael detector control. 
	 */
	private IshDetFnProcess ishDetControl; 
	
	
	private static final int currentVersion = 1;


	public IshmaelBinaryDataSource(IshDetFnProcess ishDetFnProcess, PamDataBlock sisterDataBlock, String streamName) {
		super(sisterDataBlock);
		this.ishDetControl=ishDetFnProcess; 
		this.streamName = streamName; 
	}

	@Override
	public String getStreamName() {
		return streamName;
	}

	@Override
	public int getStreamVersion() {
		return 1;
	}

	@Override
	public int getModuleVersion() {
		return currentVersion;
	}

	@Override
	public byte[] getModuleHeaderData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData, BinaryHeader bh, int moduleVersion) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData, BinaryHeader bh) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData, BinaryHeader bh,
			ModuleHeader moduleHeader) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void newFileOpened(File outputFile) {
		// TODO Auto-generated method stub
	}

}
