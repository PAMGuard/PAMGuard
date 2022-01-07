package binaryFileStorage;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;

/**
 * Class to contain module specific information
 * which will be stored at the end of a binary
 * data file, just before the BinaryFooter. 
 * @author Doug Gillespie
 *
 */
abstract public class ModuleHeader implements Serializable, ManagedParameters {
	
	private static final long serialVersionUID = 1L;

	private int moduleVersion;
	
	/**
	 * @param moduleVersion
	 */
	public ModuleHeader(int moduleVersion) {
		super();
		this.moduleVersion = moduleVersion;
	}
//	/**
//	 * Writes the module header to a DataOutputStream
//	 * @param dos Data output stream (generally a binary file)
//	 * @return true if successful
//	 */
//	public final boolean writeModuleHeader(DataOutputStream dos) {
//		byte[] data = getByteArray();
//		int dataLen = 4*4;
//		int objectLen = 0;
//		if (data != null) {
//			objectLen = data.length;
//		}
//		dataLen += objectLen;
//		try {
//			dos.writeInt(dataLen);
//			dos.writeInt(BinaryTypes.MODULE_FOOTER);
//			dos.writeInt(moduleVersion); // added 4 May 2010
//			dos.writeInt(objectLen);
//			if (data != null) {
//				dos.write(data);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//			return false;
//		}
//		return true;
//	}
//	/**
//	 * Get module specific data to write to the footer. 
//	 * Note that this is just the module data, not the 
//	 * object identifier. 
//	 * @return byte array or null
//	 */
//	public abstract byte[] getByteArray();

	/**
	 * Read data from a byte array to recreate a module header
	 * being read back from file. 
	 * <p>The BinaryHeader and ModuleHeader can be used
	 * to check version numbers if required. 
	 * @param binaryObjectData data read from file
	 * @param binaryHeader binary Header from data file
	 * @return true if unpacked successfully. 
	 */
	public abstract boolean createHeader(BinaryObjectData binaryObjectData, BinaryHeader binaryHeader);
	
	/**
	 * @return the moduleVersion
	 */
	public int getModuleVersion() {
		return moduleVersion;
	}

}
