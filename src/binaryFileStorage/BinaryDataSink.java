package binaryFileStorage;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

/**
 * Interface for users of binary data as it's being read from a file. 
 * Split off so that the main BinaryStore functions can be altered 
 * slightly for a file copy process. 
 * @author dg50
 *
 */
public interface BinaryDataSink {
	
	public void newFileHeader(BinaryHeader binaryHeader);
	
	public void newModuleHeader(BinaryObjectData binaryObjectData, ModuleHeader moduleHeader);
	
	public void newModuleFooter(BinaryObjectData binaryObjectData, ModuleFooter moduleFooter);
	
	public void newFileFooter(BinaryObjectData binaryObjectData, BinaryFooter binaryFooter);
	
	public boolean newDataUnit(BinaryObjectData binaryObjectData, PamDataBlock dataBlock, PamDataUnit dataUnit);

	public void newDatagram(BinaryObjectData binaryObjectData);

}
