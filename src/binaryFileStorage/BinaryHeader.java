package binaryFileStorage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;

import PamController.PamguardVersionInfo;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import PamUtils.PamCalendar;

public class BinaryHeader implements Serializable, ManagedParameters {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int headerLength = 0;
		
	private String pamguard = "PAMGUARDDATA";
	
	private String pamguardVersion = PamguardVersionInfo.version;
	
	private String pamguardBranch = PamguardVersionInfo.getReleaseType().toString();
	
	private long dataDate = PamCalendar.getTimeInMillis();
	
	private long analysisDate = System.currentTimeMillis();
	
	private long fileStartSample;
	
	private String moduleType;
	
	private String moduleName;
	
	private String streamName;
	
	private byte[] extraInfo;

	private int headerFormat;

	public BinaryHeader(String moduleType, String moduleName, String streamName, int fileFormat) {
		super();
		this.moduleType = moduleType;
		this.moduleName = moduleName;
		this.streamName = streamName;
		this.headerFormat = fileFormat;
	}
	
	public BinaryHeader() {
		super();
	}

	public byte[] getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(byte[] extraInfo) {
		this.extraInfo = extraInfo;
	}
	
	/**
	 * Write the header to the output stream
	 * @param dos opened DataOutputStream
	 * @return true if successful
	 */
	public boolean writeHeader(DataOutputStream dos) {
		// first work out the total number of bytes in the header. 
		int headLen = 12 + pamguard.length() + pamguardVersion.length() + 
		pamguardBranch.length() + 8 + 8 + 8 + // three 8's for the three times
		2 + moduleType.length() +
		2 + moduleName.length() + 
		2 + streamName.length() +
		4;
		if (extraInfo != null) {
			headLen += extraInfo.length;
		}
		byte[] bytes;
		try {
			dos.writeInt(headLen);
			dos.writeInt(BinaryTypes.FILE_HEADER);
			dos.writeInt(headerFormat);
			dos.write(pamguard.getBytes());
			dos.writeUTF(pamguardVersion);
			dos.writeUTF(pamguardBranch);
			dos.writeLong(dataDate);
			dos.writeLong(analysisDate);
			dos.writeLong(fileStartSample);
			dos.writeUTF(moduleType);
			dos.writeUTF(moduleName);
			dos.writeUTF(streamName);
			if (extraInfo == null) {
				dos.writeInt(0);
			}
			else {
				dos.writeInt(extraInfo.length);
				dos.write(extraInfo);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * Read binary header information
	 * @param dis data input stream
	 * @return true if read sucessfully. False otherwise
	 */
	public boolean readHeader(DataInputStream dis) {
		// first work out the total number of bytes in the header. 
//		int headLen = 12 + pamguard.length() + pamguardVersion.length() + 
//		pamguardBranch.length() + 8 + 8 + 
//		2 + moduleType.length() +
//		2 + moduleName.length() + 
//		2 + streamName.length() +
//		4;
//		if (extraInfo != null) {
//			headLen += extraInfo.length;
//		}
		int headLen = 0;
		int headId = 0;
		String PamString;
		byte[] nameBytes = new byte[pamguard.getBytes().length];
		int nNameBytes = 0;
		int extraInfoLen = 0;
		
		byte[] bytes;
		try {
			headLen = dis.readInt();
			headId = dis.readInt();
			headerFormat = dis.readInt();
			nNameBytes = dis.read(nameBytes);
			pamguardVersion = dis.readUTF();
			pamguardBranch = dis.readUTF();
			dataDate = dis.readLong();
			analysisDate = dis.readLong();
			fileStartSample = dis.readLong();
			moduleType = dis.readUTF();
			moduleName = dis.readUTF();
			streamName = dis.readUTF();
			extraInfoLen = dis.readInt();
			dis.skip(extraInfoLen);
		} catch (IOException e) {
//			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @return the headerLength
	 */
	public int getHeaderLength() {
		return headerLength;
	}

	/**
	 * @param headerLength the headerLength to set
	 */
	public void setHeaderLength(int headerLength) {
		this.headerLength = headerLength;
	}

	/**
	 * @return the headerFormat
	 */
	public int getHeaderFormat() {
		return headerFormat;
	}

	/**
	 * @param headerFormat the headerFormat to set
	 */
	public void setHeaderFormat(int headerFormat) {
		this.headerFormat = headerFormat;
	}

	/**
	 * @return the pamguardVersion
	 */
	public String getPamguardVersion() {
		return pamguardVersion;
	}

	/**
	 * @param pamguardVersion the pamguardVersion to set
	 */
	public void setPamguardVersion(String pamguardVersion) {
		this.pamguardVersion = pamguardVersion;
	}

	/**
	 * @return the pamguardBranch
	 */
	public String getPamguardBranch() {
		return pamguardBranch;
	}

	/**
	 * @param pamguardBranch the pamguardBranch to set
	 */
	public void setPamguardBranch(String pamguardBranch) {
		this.pamguardBranch = pamguardBranch;
	}

	/**
	 * @return the dataDate
	 */
	public long getDataDate() {
		return dataDate;
	}

	/**
	 * @param dataDate the dataDate to set
	 */
	public void setDataDate(long dataDate) {
		this.dataDate = dataDate;
	}

	public long getFileStartSample() {
		return fileStartSample;
	}

	public void setFileStartSample(long fileStartSample) {
		this.fileStartSample = fileStartSample;
	}

	/**
	 * @return the analysisDate
	 */
	public long getAnalysisDate() {
		return analysisDate;
	}

	/**
	 * @param analysisDate the analysisDate to set
	 */
	public void setAnalysisDate(long analysisDate) {
		this.analysisDate = analysisDate;
	}

	/**
	 * @return the moduleType
	 */
	public String getModuleType() {
		return moduleType;
	}

	/**
	 * @param moduleType the moduleType to set
	 */
	public void setModuleType(String moduleType) {
		this.moduleType = moduleType;
	}

	/**
	 * @return the moduleName
	 */
	public String getModuleName() {
		return moduleName;
	}

	/**
	 * @param moduleName the moduleName to set
	 */
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	/**
	 * @return the streamName
	 */
	public String getStreamName() {
		return streamName;
	}

	/**
	 * @param streamName the streamName to set
	 */
	public void setStreamName(String streamName) {
		this.streamName = streamName;
	}
	
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("pamguard");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return pamguard;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

	
}
