package zipUnpacker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import x3.X3JNIEncoder;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamUtils.FileFunctions;
import PamUtils.FileList;
import PamUtils.FileParts;

/**
 * Functions to unpack gzip files of PAMBuoy data. 
 * @author Doug Gillespie
 *
 */
public class ZipUnpacker implements PamSettings {

	private PamController pamController;
	
	private ZipSettings zipSettings = new ZipSettings();

	private x3.X3JNIEncoder x3Encoder;

	public ZipUnpacker(PamController pamController) {
		super();
		this.pamController = pamController;
		PamSettingManager.getInstance().registerSettings(this);
	}
	
	public void checkforFiles() {
		if (zipSettings.getArchiveFolder() == null || zipSettings.getDataFolder() == null) {
			return;
		}
		if (new File(zipSettings.getArchiveFolder()).exists() == false) {
			return;
		}
		if (new File(zipSettings.getDataFolder()).exists() == false) {
			return;
		}
		ArrayList<String> newDoneFiles = new ArrayList<>();
		FileList fileList = new FileList();
		String[] fileEnd = new String[1];
		fileEnd[0] = ".gz";
//		fileEnd[1] = ".zip";
		ArrayList<File> files = fileList.getFileList(zipSettings.getArchiveFolder(), fileEnd, true);
		for (File aFile:files) {
			FileParts fp = new FileParts(aFile);
			newDoneFiles.add(fp.getFileNameAndEnd());
			// should first try to see if it's already been unpacked. 
			if (zipSettings.isDoneFile(aFile)) {
				continue;
			}
			File tarFile = unpackGZFile(aFile);
			if (tarFile != null) {
				unpackTarFile(tarFile);
				tarFile.delete();
			}
		}
		zipSettings.setDoneFiles(newDoneFiles);
	}

	private void unpackTarFile(File tarFile) {
		if (tarFile == null) {
			return;
		}
		FileParts fp = new FileParts(tarFile);
		
		String name = fp.getFileName();
		String dataType = getDataType(name);
		String outFolderName = zipSettings.getDataFolder();
		if (dataType != null) {
			outFolderName += FileParts.getFileSeparator() + dataType;
		}
		File outFolder = new File(outFolderName);
		if (outFolder.exists() == false) {
			if (outFolder.mkdirs() == false) {
				System.err.println("Error creating data folder " + outFolderName);
			}
			else {
				FileFunctions.setNonIndexingBit(outFolder);
			}
		}
		TarArchiveEntry entry = null; 
		InputStream is = null;
		TarArchiveInputStream tarInputStream = null;		
		try {
			is = new FileInputStream(tarFile);
		} catch (FileNotFoundException e) {
			System.out.println("Unable to find tar archive file: " + tarFile);
			return;
		}
		try {
			tarInputStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", is);
		} catch (ArchiveException e) {
			System.out.println("Unable to open tar input stream from file " + tarFile);
		}
		try {
			while ((entry = (TarArchiveEntry)tarInputStream.getNextEntry()) != null) {
				File nextFile = new File(outFolder, entry.getName());
			    if (entry.isDirectory()) {
			    	System.out.println("New directory: " + nextFile.getAbsolutePath());
			    	nextFile.mkdirs();
			    	FileFunctions.setNonIndexingBit(nextFile);
			    	continue;
			    }
			    else {
			    	if (nextFile.exists()) {
				    	System.out.println("New file already exists. Won't overwrite: " + nextFile.getAbsolutePath());
			    		continue; // file already exists. 
			    	}
			    	System.out.println("Unpack tar file: " + nextFile.getAbsolutePath());
			    	/*
			    	 *  check the path on the file name since none of the files seem to 
			    	 *  be being recognised as folders so don't fire the above isDirectory() 
			    	 */
			    	String folderName = nextFile.getPath();
			    	int lastSep = folderName.lastIndexOf('\\');
			    	if (lastSep > 0) {
			    		File folder = new File(folderName.substring(0, lastSep));
			    		if (folder.exists() == false) {
			    			folder.mkdirs();
			    			FileFunctions.setNonIndexingBit(folder);
			    		}
			    	}
//			    	System.out.println("Extract file from tar archive: " + nextFile.getAbsolutePath());
			    	OutputStream fileStream = new FileOutputStream(nextFile);
			    	IOUtils.copy(tarInputStream, fileStream);
			    	fileStream.close();
			    	
			    	/*
			    	 * Now check to see if it's an x3 file, in which case
			    	 * we'll have to unpack it and then delete it. 
			    	 */
			    	FileParts fParts = new FileParts(nextFile);
			    	String fEnd = fParts.getFileEnd();
			    	if (fEnd != null && fEnd.equalsIgnoreCase("x3")) {
			    		if (x3Encoder == null) {
			    			x3Encoder = new X3JNIEncoder();
			    		}
			    		String x3Name = nextFile.getAbsolutePath();
			    		String wavName = x3Name.substring(0, x3Name.lastIndexOf(fEnd)) + "wav";
			    		x3Encoder.jniX3ToWav(x3Name, wavName);
			    		nextFile.delete();
			    	}
			    	
			    }
			}
			tarInputStream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	    try {
			tarInputStream.close();
		    if (is != null) is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Work out the type of data, which is encoded into gz file names
	 * e.g. smruDa-pb-030-02-binData-20130728023054
	 * @param name
	 * @return type of data which will be used in the output folder. 
	 */
	private String getDataType(String name) {
		if (name == null) {
			return null;
		}
		String[] nameBits = name.split("-");
		if (nameBits.length < 3) {
			return null;
		}
		return nameBits[nameBits.length-2];
	}

	/*
	 * Unpack a single file from the archive. 
	 * Will call back into itself recursively if files are 
	 * multiply packed e.g. tar gz files.
	 */
	private File unpackGZFile(File aFile) {
		FileParts fp = new FileParts(aFile);
		String outFileName = fp.getFolderName() + FileParts.getFileSeparator() + fp.getFileName();
		File outFile = new File(outFileName);
		try {
			GZIPInputStream is = new GZIPInputStream(new FileInputStream(aFile));
			FileOutputStream os = new FileOutputStream(outFile);
			IOUtils.copy(is, os);
		} catch (IOException e) {
			System.err.println("Error unpacking file " + aFile.getAbsolutePath());
			System.err.println("Error message: " + e.getMessage());
			return null;
		}
		return outFile;
	}

	@Override
	public String getUnitName() {
		return "Archive Unzipper";
	}

	@Override
	public String getUnitType() {
		return "Archive Unzipper";
	}

	@Override
	public Serializable getSettingsReference() {
		return zipSettings;
	}

	@Override
	public long getSettingsVersion() {
		return ZipSettings.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(
			PamControlledUnitSettings pamControlledUnitSettings) {
		zipSettings = ((ZipSettings) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}
	
}
