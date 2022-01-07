package binaryFileStorage;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * Class to contain module specific information
 * which will be stored at the end of a binary
 * data file, just before the BinaryFooter. 
 * @author Doug Gillespie
 *
 */
abstract public class ModuleFooter implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
//	/**
//	 * Writes the module header to a DataOutputStream
//	 * @param dos Data output stream (generally a binary file)
//	 * @return true if successful
//	 */
//	public final boolean writeModuleFooter(DataOutputStream dos) {
//		byte[] data = getByteArray();
//		int dataLen = 3*4;
//		int objectLen = 0;
//		if (data != null) {
//			objectLen = data.length;
//		}
//		dataLen += objectLen;
//		try {
//			dos.writeInt(dataLen);
//			dos.writeInt(BinaryTypes.MODULE_FOOTER);
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
	/**
	 * Get module specific data to write to the footer. 
	 * Note that this is just the module data, not the 
	 * object identifier. 
	 * @return byte array or null
	 */
	public abstract byte[] getByteArray();

	/**
	 * Read data from a byte array to recreate a module header
	 * being read back from file. 
	 * <p>The BinaryHeader and ModuleHeader can be used
	 * to check version numbers if required. 
	 * @param binaryObjectData data read from file
	 * @param binaryHeader binary Header from data file
	 * @param moduleHeader module specific header from data file
	 * @return true if unpacked successfully. 
	 */
	public abstract boolean createFooter(BinaryObjectData binaryObjectData, BinaryHeader binaryHeader,
			ModuleHeader moduleHeader);

}
