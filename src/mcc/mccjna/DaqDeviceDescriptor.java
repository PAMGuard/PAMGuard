package mcc.mccjna;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

/*
 * 
typedef struct 
{
	CHAR					ProductName[64];
	UINT					ProductID;			// product ID
	DaqDeviceInterface		InterfaceType;		// USB, BLUETOOTH, ...
	CHAR					DevString[64];
	CHAR					UniqueID[64];		// unique identifier for device. Serial number for USB deivces and MAC address for  bth and net devices
	ULONGLONG				NUID;				// numeric representation of uniqueID
	CHAR					Reserved[512];		// reserved for the future.
												
} DaqDeviceDescriptor;
 */

/**
 * DaqDeviceDescriptor class mirroring the C version for use with JNA. 
 * @author Doug Gillespie
 *
 */
public class DaqDeviceDescriptor extends Structure {

	/**
	 * Values for DaqDeviceDescritor Interfacetype (enum in C version, int here)
	 * Seems to be OK having these here, even though they are not 
	 * part of the original structure
	 */
	public static final int USB_IFC = 1 << 0;
	public static final int BLUETOOTH_IFC = 1 << 1;
	public static final int ETHERNET_IFC = 1 << 2;
	public static final int ANY_IFC = USB_IFC | BLUETOOTH_IFC | ETHERNET_IFC;

	
	public static class ByValue extends DaqDeviceDescriptor implements Structure.ByValue { }
	public byte[] ProductName = new byte[64];
	public int ProductID; // product ID
	public int InterfaceType; // USB, BLUETOOTH, ...
	public byte[] DevString = new byte[64];
	public byte[] UniqueID = new byte[64]; // unique identifier for device. Serial number for USB devices and MAC address
									// for bth and net devices
	public long NUID; // numeric representation of uniqueID
	public byte[] Reserved = new byte[512]; // reserved for the future.
	
	@Override
	protected List getFieldOrder() {
		return Arrays.asList(new String[] { "ProductName", "ProductID", "InterfaceType", "DevString", "UniqueID", "NUID", "Reserved"});
	}
	@Override
	public String toString() {
		return String.format("ProductName %s, ID %d, interface %d, DevString %s, UID %s, NUID %d", getProductName(),
				ProductID, InterfaceType, getDevString(), getUniqueID(), NUID);
	}
	/**
	 * @return the productName
	 */
	public String getProductName() {
		return new String(ProductName).trim();
	}
	/**
	 * @return the productID
	 */
	public int getProductID() {
		return ProductID;
	}
	/**
	 * @return the interfaceType
	 */
	public int getInterfaceType() {
		return InterfaceType;
	}
	/**
	 * @return the devString
	 */
	public String getDevString() {
		return new String(DevString).trim();
	}
	/**
	 * @return the uniqueID
	 */
	public String getUniqueID() {
		return new String(UniqueID).trim();
	}
	/**
	 * @return the nUID
	 */
	public long getNUID() {
		return NUID;
	}
	/**
	 * @return the reserved
	 */
	public byte[] getReserved() {
		return Reserved;
	}
	
	
	
	
}
