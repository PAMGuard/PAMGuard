package PamController;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import PamUtils.PamCalendar;
import PamView.dialog.warn.WarnOnce;
import binaryFileStorage.BinaryFooter;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryStore;
import binaryFileStorage.ModuleNameObject;

/**
 * Read and write psfx type configuration files. 
 * @author dg50
 *
 */
public class PSFXReadWriter {
	
	private static PSFXReadWriter singleInstance = null;

	private static final String SETTINGSSTORE = "SettingsStore";
	
//	private static final String TEMPNAME = "_tmp";
	private static final String TEMPEXT = ".tmp";

	private PSFXReadWriter() {
		// TODO Auto-generated constructor stub
	}

	public static PSFXReadWriter getInstance() {
		if (singleInstance == null) {
			singleInstance = new PSFXReadWriter();
		}
		return singleInstance;
	}
	
	/**
	 * Save settings to psfx file.  Note that this method first saves to a temp file
	 * (using the filename passed with 'tmp' appended to the end) and then renames to
	 * the correct filename at the end.  This way, if something happens and the file
	 * becomes corrupted during the save, the original will still be safe
	 * 
	 * @param fileName the name to use for the save file (full name, including path)
	 * @return true if successful, false otherwise
	 */
	public synchronized boolean writePSFX(String fileName) {
//		return writePSFX(fileName, PamCalendar.getTimeInMillis());
		String tempName = generateTempFilename(fileName);
		boolean success = writePSFX(tempName, PamCalendar.getTimeInMillis());
		if (success) {
			File origFile = new File(fileName);
			origFile.delete();
			File tempFile = new File(tempName);
			success = tempFile.renameTo(origFile);
		}
		return success;
	}
	
	/**
	 * Generate a temporary filename to save the settings to.
	 * @param fileName The original filename, with full path and including .psfx extension
	 * @return a String with the temporary filename
	 */
	private String generateTempFilename(String fileName) {
//		int idx = fileName.lastIndexOf(".");
//		String tempName = fileName.substring(0, idx) + TEMPNAME + fileName.substring(idx, fileName.length());
		String tempName = fileName + TEMPEXT;
		return tempName;
	}

	/**
	 * Write the settings to the file<br>
	 * Initially write to a temp file, then rename the temp file.  
	 * @param fileName the name to use for the save file (full name, including path)
	 * @param timeStamp time stamp in milliseconds
	 * @return
	 */
	public synchronized boolean writePSFX(String fileName, long timeStamp) {

		// get an object containing everything we'll need to know. 
		PamSettingsGroup psg = PamSettingManager.getInstance().getCurrentSettingsGroup();
		// force the time stamp to be that given - might need to be exact !
		psg.setSettingsTime(timeStamp);
		BinaryHeader header = new BinaryHeader(SETTINGSSTORE, SETTINGSSTORE, SETTINGSSTORE, 0);
		header.setAnalysisDate(timeStamp);
		header.setDataDate(timeStamp);

		BinaryFooter footer = new BinaryFooter(timeStamp, timeStamp, 2, BinaryStore.getCurrentFileFormat());
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(new FileOutputStream(fileName));
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

		footer.writeFooter(dos, BinaryStore.getCurrentFileFormat());

		try {
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean writeData(DataOutputStream dos, int objectId, byte[] data) {
		int totalLen = data.length + 16;
		int dataLen = data.length;
		try {
			dos.writeInt(totalLen);
			dos.writeInt(objectId);
			dos.writeInt(dataLen);
			dos.write(data);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	

	public PamSettingsGroup loadFileSettings(File file) {
		if (file == null) {
			return null;
		}
		if (!file.exists()) {
			return null;
		}
		
		// check for the existence of a temp file, and warn the user if one is found
		String tempName = generateTempFilename(file.getAbsolutePath());
		File tempFile = new File(tempName);
		if (tempFile.exists()) {
			String title = "Potential Problem with Settings File";
			String msg = "Pamguard has found the following temporary settings file:<br><br>" +
					tempFile.getAbsolutePath() + "<br><br>" +
					"Temporary files are created when the settings are saved, but are deleted if the save is "+
					"successful.  Since this settings file still exists, <em>there may have been a problem during the " +
					"last save</em>.  Please check your settings after Pamguard starts to make sure they are correct.<p>" +
					"<br>Note that you can also try to load the temporary file, in order to recover settings that may have " +
					"been lost.  If you want to do this, you should rename it now or else it may be overwritten when " +
					"Pamguard next shuts down.  Keep in mind that the temporary file may be corrupt, depending on what " +
					"kind of error occurred during the previous save.<br>";
			String help = null;
			/**
			 * Only open dialog warning if NOT under Network managed control so that it doesn't
			 * occurr when running under PAMDog control. 
			 */
			if (pamBuoyGlobals.getNetworkControlPort() == null) {
				int ans = WarnOnce.showWarning(PamController.getMainFrame(), title, msg, WarnOnce.WARNING_MESSAGE, help);
			}
			else {
				/*
				 * PAMDog control - just print the message. The psf won't have changed in any case. 
				 */
				System.out.println(title + " : " + msg);
			}
		}
		
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		BinaryHeader bh = new BinaryHeader();
		bh.readHeader(dis);
		PamSettingsGroup psg = new PamSettingsGroup(bh.getDataDate());
		ArrayList<ModuleNameObject> moduleNames = new ArrayList<ModuleNameObject>();
		/*
		 * 
			dos.writeInt(totalLen);
			dos.writeInt(objectId);
			dos.writeInt(dataLen);
		 */
		int totalLen;
		int objectId;
		int dataLen;
		byte[] data = null;
		while(true) {
			try {
				totalLen = dis.readInt();
				objectId = dis.readInt();
				dataLen = dis.readInt();
				data = new byte[dataLen];
				dis.read(data);
				if (objectId == ModuleNameObject.typeId) {
					ModuleNameObject mno = new ModuleNameObject(data);
					moduleNames.add(mno);
				}
				else if (objectId == 2) {
					PamControlledUnitSettings pcsu = PamControlledUnitSettings.createFromNamedByteArray(data);
					if (pcsu != null) {
						psg.addSettings(pcsu);
					}
				}
			} catch (EOFException e) {
				break;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// close the input stream and return
		try {
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return psg;
	}
}
