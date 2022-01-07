package annotation.binary;

import annotation.DataAnnotationType;

public class AnnotationBinaryData {

	@Deprecated // Left in place in case a plug in uses it.  
	public AnnotationBinaryData(int binaryFileVersion, short annotationVersion,
			DataAnnotationType<?> dataAnnotationType, String shortIdCode, byte[] data) {
		super();
//		this.binaryFileVersion = binaryFileVersion;
		this.annotationVersion = annotationVersion;
		this.dataAnnotationType = dataAnnotationType;
		this.shortIdCode = shortIdCode;
		this.data = data;
	}
	
	public AnnotationBinaryData(short annotationVersion,
			DataAnnotationType<?> dataAnnotationType, String shortIdCode, byte[] data) {
		super();
//		this.binaryFileVersion = binaryFileVersion;
		this.annotationVersion = annotationVersion;
		this.dataAnnotationType = dataAnnotationType;
		this.shortIdCode = shortIdCode;
		this.data = data;
	}

	public byte[] data;
	
	public DataAnnotationType<?> dataAnnotationType;
	
	public short annotationVersion;
	
//	public int binaryFileVersion;

	public String shortIdCode;
	
}
