package binaryFileStorage;

/**
 * Use BinaryObjectData instead. this class has been effectively deleted
 * but turned into a subclass of BinaryObjectData so that existing code
 * will still work, but should be weeded from the system. 
 * @author Doug Gillespie
 *
 */
@Deprecated
public class PackedBinaryObject extends BinaryObjectData {

	public PackedBinaryObject(int objectType, byte[] byteArray) {
		super(objectType, byteArray);
	}

//	public byte[] data;
//	
//	public int objectId;
//
//	/**
//	 * @param objectId
//	 * @param data
//	 */
//	public PackedBinaryObject(int objectId, byte[] data) {
//		super();
//		this.objectId = objectId;
//		this.data = data;
//	}
//	
	
}
