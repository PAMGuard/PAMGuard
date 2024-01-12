package binaryFileStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import PamController.PSFXReadWriter;
import PamController.PamSettingsGroup;
import PamController.PamSettingsSource;
import PamUtils.PamCalendar;
import PamUtils.PamFileFilter;

/**
 * Manage storage and retrieval of PAMGUARD serialised settings storage in 
 * binary files. 
 * @author Doug
 *
 */
public class BinarySettingsStorage implements PamSettingsSource {

	private BinaryStore binaryStore;

	private static final String SETTINGSFILENAME = "PamguardSettings_";

	private static final String SETTINGSSTORE = "SettingsStore";

	private Vector<PamSettingsGroup> pamSettingsGroups;

	public BinarySettingsStorage(BinaryStore binaryStore) {
		super();
		this.binaryStore = binaryStore;
	}

//	/**
//	 * Set auto unpacking of psfx data. 
//	 * @param args
//	 */
//	public static void main(String[] args) {
//
//		File psfFile = new File("C:\\PamguardTest\\ClickBEarings\\bindata\\20150628\\PamguardSettings_20150628_190722.psfx");
//		BinarySettingsStorage bs = new BinarySettingsStorage(null);
//		PamSettingsGroup settings = bs.loadFileSettings(psfFile);
//		
//	}
	/**
	 * Save the settings in some vaguely sensible format. 
	 * This will be different to the main settings stores since
	 * a) they are crap 
	 * b) I want to include a list of modules in a format which can
	 * be read by anything, so that other software can at least work 
	 * out which modules were present without having to decode the 
	 * serialised java objects. 
	 * 
	 * 
	 * @param timeStamp current time in milliseconds
	 * @return true if saved correctly 
	 */
	@Override
	public boolean saveStartSettings(long timeStamp) {
		String filePath = binaryStore.getFolderName(timeStamp, true);
		filePath += PamCalendar.createFileName(timeStamp, 
				SETTINGSFILENAME, BinaryStore.settingsFileType);
		return PSFXReadWriter.getInstance().writePSFX(filePath, timeStamp);
/*
		// get an object containing everything we'll need to know. 
		PamSettingsGroup psg = PamSettingManager.getInstance().getCurrentSettingsGroup();
		// force the time stamp to be that given - might need to be exact !
		psg.setSettingsTime(timeStamp);
		BinaryHeader header = new BinaryHeader(SETTINGSSTORE, SETTINGSSTORE, SETTINGSSTORE);
		header.setAnalysisDate(timeStamp);
		header.setDataDate(timeStamp);

		BinaryFooter footer = new BinaryFooter(timeStamp, timeStamp, 2, 0);
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(new FileOutputStream(filePath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		header.writeHeader(dos);

		// write out a list of modules.
		ArrayList<UsedModuleInfo> umiList = psg.getUsedModuleInfo();
		UsedModuleInfo umi;
		ModuleNameObject moduleNameObject;
		int nMods = umiList.size();
		for (int i = 0; i < nMods; i++) {
			umi = umiList.get(i);
			moduleNameObject = new ModuleNameObject(umi.className, umi.getUnitType(), umi.unitName);
			writeData(dos, ModuleNameObject.typeId, moduleNameObject.createBinaryWriteObject());
		}

		// now write out the serialized settings. 
		nMods = psg.getUnitSettings().size();
		PamControlledUnitSettings pcsu;
		for (int i = 0; i < nMods; i++) {
			pcsu = psg.getUnitSettings(i);
			writeData(dos, 2, pcsu.getNamedSerialisedByteArray());
		}

		footer.writeFooter(dos);

		try {
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;*/
	}

	@Override
	public boolean saveEndSettings(long timeNow) {
		// do nothing at the end of a run with binary store. 
		return true;
	}

//	private boolean writeData(DataOutputStream dos, int objectId, byte[] data) {
//		int totalLen = data.length + 16;
//		int dataLen = data.length;
//		try {
//			dos.writeInt(totalLen);
//			dos.writeInt(objectId);
//			dos.writeInt(dataLen);
//			dos.write(data);
//		} catch (IOException e) {
//			e.printStackTrace();
//			return false;
//		}
//		return true;
//	}

	@Override
	public int getNumSettings() {
		if (pamSettingsGroups == null) {
			return 0;
		}
		return pamSettingsGroups.size();
	}

	@Override
	public PamSettingsGroup getSettings(int settingsIndex) {
		if (pamSettingsGroups == null) {
			return null;
		}
		return pamSettingsGroups.get(settingsIndex);
	}

	public void makeSettingsMap() {
		// find all psfx files and read thier contents. 
		ArrayList<File> fileList = listSettingsFiles();
	}

	private ArrayList<File> listSettingsFiles() {
		ArrayList<File> settingsFiles = new ArrayList<File>();
		PamFileFilter filter = new PamFileFilter("Settings files", BinaryStore.settingsFileType);
		filter.setAcceptFolders(true);
		if (binaryStore.binaryStoreSettings.getStoreLocation() == null) {
			return null;
		}
		addFiles(settingsFiles, new File(binaryStore.binaryStoreSettings.getStoreLocation()), filter);

		pamSettingsGroups = new Vector<PamSettingsGroup>();
		PSFXReadWriter psfxReadWriter = PSFXReadWriter.getInstance();
		for (int i = 0; i < settingsFiles.size(); i++) {
//			PamSettingsGroup psg = loadFileSettings(settingsFiles.get(i));
			PamSettingsGroup psg = psfxReadWriter.loadFileSettings(settingsFiles.get(i));
			if (psg != null) {
				pamSettingsGroups.add(psg);
			}
		}

		return null;
	}

	private void addFiles(ArrayList<File> files, File folder, PamFileFilter filter) {
		File[] newFiles = folder.listFiles(filter);
		if (newFiles == null) {
			return;
		}
		for (int i = 0; i < newFiles.length; i++) {
			if (newFiles[i].isDirectory()) {
				addFiles(files, newFiles[i].getAbsoluteFile(), filter);
			}
			else {
				files.add(newFiles[i]);
			}
		}		
	}

//	public PamSettingsGroup loadFileSettings(File file) {
//		DataInputStream dis = null;
//		try {
//			dis = new DataInputStream(new FileInputStream(file));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return null;
//		}
//		BinaryHeader bh = new BinaryHeader();
//		bh.readHeader(dis);
//		PamSettingsGroup psg = new PamSettingsGroup(bh.getDataDate());
//		/*
//		 * 
//			dos.writeInt(totalLen);
//			dos.writeInt(objectId);
//			dos.writeInt(dataLen);
//		 */
//		int totalLen;
//		int objectId;
//		int dataLen;
//		byte[] data = null;
//		while(true) {
//			try {
//				totalLen = dis.readInt();
//				objectId = dis.readInt();
//				dataLen = dis.readInt();
//				data = new byte[dataLen];
//				dis.read(data);
//				if (objectId == ModuleNameObject.typeId) {
//					ModuleNameObject mno = new ModuleNameObject(data);
//				}
//				else if (objectId == 2) {
//					PamControlledUnitSettings pcsu = PamControlledUnitSettings.createFromNamedByteArray(data);
//					if (pcsu != null) {
//						psg.addSettings(pcsu);
//					}
//				}
//			} catch (EOFException e) {
//				break;
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		if (pamSettingsGroups != null) {
//			pamSettingsGroups.add(psg);
//		}
//		return psg;
//	}
	// now read serialised data out of the data array

	@Override
	public String getSettingsSourceName() {
		// TODO Auto-generated method stub
		return null;
	}
}

