package binaryFileStorage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Object which holds the type and name of a PAMGUARD module
 * and can read and or write it from file. 
 * @author Doug Gillespie. 
 *
 */
public class ModuleNameObject {

	public static int typeId = 1;
	
	private String unitType;
	
	private String unitName;

	private String className;

	public ModuleNameObject(String className, String unitType, String unitName) {
		super();
		this.className = className;
		this.unitType = unitType;
		this.unitName = unitName;
	}
	
	public ModuleNameObject(int version, byte[] binaryData) {
		ByteArrayInputStream bis = new ByteArrayInputStream(binaryData);
		DataInputStream dis = new DataInputStream(bis);
		int len;
		try {
			len = dis.readInt();
			className = dis.readUTF();
			unitType = dis.readUTF();
			unitName = dis.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ModuleNameObject(byte[] data) throws IOException {
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
		try {
			className = dis.readUTF();
			unitType = dis.readUTF();
			unitName = dis.readUTF();
			dis.close();
		} catch (IOException e) {
			throw(e); //better to throw this than to report it here. 
		}
	}

	public byte[] createBinaryWriteObject() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeUTF(className);
			dos.writeUTF(unitType);
			dos.writeUTF(unitName);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		byte[] outputData = bos.toByteArray();
		try {
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outputData;
	}
	
}
