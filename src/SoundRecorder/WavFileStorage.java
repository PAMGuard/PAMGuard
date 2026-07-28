package SoundRecorder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioFileFormat.Type;

import PamController.PamGUIManager;

import javax.sound.sampled.AudioFormat;

import PamUtils.FileFunctions;
import PamUtils.PamCalendar;
import PamUtils.PamFileFilter;
import wavFiles.WavFileWriter;

/**
 * Bespoke system for storing wav files. Am too sick of the complicated piped system 
 * required to use the standard Java libraries. 
 * @author dg50
 *
 */
public class WavFileStorage implements RecorderStorage {

	private RecorderControl recorderControl;
	private Type audioFileType;
	private AudioFormat audioFormat;
	private long fileStartMillis;
	private long lastDataTime;
	private WavFileWriter wavFile;

	public WavFileStorage(RecorderControl recorderControl) {
		super();
		this.recorderControl = recorderControl;
		if(PamGUIManager.getGUIType()==PamGUIManager.NOGUI) {
			//See note before 'createLock' (bottom of this .java file) for explanation
			
			removeOldLockFiles();
		}
	}

	@Override
	public boolean openStorage(Type fileType, long recordingStart, float sampleRate, int nChannels, int bitDepth) {

		closeStorage();

		//		this.sampleRate = sampleRate;
		this.audioFileType = fileType;
		//		this.nChannels = nChannels;
		//		this.bitDepth = bitDepth;
		//		this.fileType = fileType;

		boolean isBigendian = (fileType != Type.WAVE);
		

		audioFormat = new AudioFormat(sampleRate, bitDepth, nChannels, true, isBigendian);

		return openStorage(recordingStart);
	}


	private boolean openStorage(long recordingStart) {

		//		byteConverter = ByteConverter.createByteConverter(bitDepth/8, isBigendian, Encoding.PCM_SIGNED);

		//		totalFrames = 0;

		lastDataTime = fileStartMillis = recordingStart;

		//		fileBytes = 0;
		String fileExtension = "." + audioFileType.getExtension();
		File outFolder = FileFunctions.getStorageFileFolder(recorderControl.recorderSettings.outputFolder,
				recordingStart, recorderControl.recorderSettings.datedSubFolders, true);
		if (outFolder == null) {
			outFolder = new File(recorderControl.recorderSettings.outputFolder);
		}
		String fileName = PamCalendar.createFileNameMillis(recordingStart, outFolder.getAbsolutePath(), 
				recorderControl.recorderSettings.fileInitials+"_", fileExtension);
		/*
		 * Random access file will not replace, but overwrite, so need to delete an 
		 * existing file ? 
		 */
		File f = new File(fileName);
		if (f.exists()) {
			try {
				f.delete();
			}
			catch (Exception e) {
				System.out.println("Unable to delete existing wav file: " + e.getMessage());
			}
		}

		if(PamGUIManager.getGUIType()==PamGUIManager.NOGUI) {
			//See note before 'createLock'
			createLock(new File(fileName));
		}
		
		wavFile = new WavFileWriter(fileName, audioFormat);

		return true;
	}

	@Override
	public boolean reOpenStorage(long recordingStart) {	
		//		return openStorage(fileType, recordingStart, sampleRate, nChannels, bitDepth);
		closeStorage();
		return openStorage(recordingStart);
	}

	@Override
	public boolean addData(long dataTimeMillis, double[][] newData) {
		if (wavFile == null) {
			return false;
		}		
		lastDataTime = dataTimeMillis;
		return wavFile.write(newData);
	}

	@Override
	public boolean closeStorage() {
		if (wavFile == null) {
			return false;
		}
		if (PamGUIManager.getGUIType() == PamGUIManager.NOGUI) {
			//See note before 'createLock' for explanation
			deleteLock(new File(wavFile.getFileName()));
		}
		wavFile.close();
		wavFile = null;
		return true;
	}

	@Override
	public String getFileName() {
		if (wavFile == null) {
			return null;
		}
		return wavFile.getFileName();
	}

	@Override
	public long getFileSizeBytes() {
		if (wavFile == null) {
			return 0;
		}
		return wavFile.getWavHeader().getDataSize() + wavFile.getWavHeader().getHeaderSize();
	}

	@Override
	public long getFileFrames() {
		if (wavFile == null) {
			return 0;
		}
		return wavFile.getFileFrames();
	}

	@Override
	public long getFileMilliSeconds() {
		return lastDataTime - fileStartMillis;
	}

	@Override
	public long getFileStartTime() {
		return fileStartMillis;
	}

	@Override
	public long getMaxFileSizeBytes() {
		return Integer.MAX_VALUE;
	}
	
	
	//ST July '26 (Implemented for SMRUC CAB Stuff)
	//Because Linux doesn't handle file locks well automatically
	//We will implement our own system of lock files to indicate that a file is being written to.
	//This is implemented in case a headless linux system has additional processes running that want to move and transfer recordings
	//We want to add a mechanism to ensure that the other process can check whether a file is actively writing.
	//If Pamguard crashes or closes unexpectedly, we want to remove any old lock files that may have been left over from a previous recording session.
	
	//The following methods are only called when PamGuiManager is NOGUI
	
	/**
	 * Locking file extension
	 */
	private static final String lockingFileExtension = "lck";
	
	/**
	 * For a given file, return the corresponding lock file object.
	 * @param outputFile the output file for which we want the lock file
	 * @return the lock file object
	 */
	private File getLockFile(File outputFile) {
		return new File(outputFile.toString() + "." + lockingFileExtension);
	}
	
	/**
	 * Create lock file to flag transfer that this file is actively being written to for APS.
	 * Linux does not handle file locks well, so explicitly building them in. 
	 * @param wavFile2
	 */
	private void createLock(File fileToLock) {
		File lockFile = getLockFile(fileToLock);
		try {
			lockFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Delete lock file to flag transfer that this file is not longer being written to for APS.
	 * Linux does not handle file locks well, so explicitly building them in. 
	 * @param wavFile2
	 */
	private void deleteLock(File fileToUnlock) {
		File wavFileLock = getLockFile(fileToUnlock);
		//Be very careful to only delete locking files.
		if (wavFileLock.exists() && wavFileLock.isFile() && wavFileLock.toString().endsWith("." + lockingFileExtension)) {
			wavFileLock.delete();
		}
	}
	
	/**
	 * Remove any old lock files that may have been left over from a previous recording session. 
	 * This is called at construction to ensure that any old lock files are removed before starting a new recording session.
	 */
	private void removeOldLockFiles() {
		ArrayList<File> lockFiles = listAllLockFiles();
		File currentLockFile = null;
		if (this.wavFile != null) {
			currentLockFile = getLockFile(new File(this.wavFile.getFileName()));
		}
		for (File lockFile : lockFiles) {
			if (currentLockFile != null && lockFile.equals(currentLockFile)) {
				//If for some reason the recorder is actively running, don't delete the lock file.
				//Because this is only called at construction, this should never happen, but just in case, don't delete the lock file if it is for the current recording.
				continue;
			}
			//Make certain that the file to be deleted is actually a lock file, and not some other file that happens to be in the output folder.
			if (lockFile.exists() && lockFile.isFile() && lockFile.toString().endsWith("." + lockingFileExtension)) {
				lockFile.delete();
			}
		}
	}
	
	/**
	 * List all lock files in the output folder and subfolders.
	 * 
	 * @return list of lock files
	 */
	private ArrayList<File> listAllLockFiles(){
		PamFileFilter lockFileFilter = new PamFileFilter("Recording File Locks", lockingFileExtension);
		lockFileFilter.setAcceptFolders(true);
        File recordingDirectory = new File(recorderControl.recorderSettings.outputFolder);
        ArrayList<File> lockFiles = new ArrayList<File>();
        listDataFiles(lockFiles, recordingDirectory, lockFileFilter);
        return lockFiles;
	}
	
	/**
	 * Copied directly from BinaryStore to prevent any confusion about the purpose/function here.
	 * 
	 * List all data files - get's called recursively
	 * @param fileList current fiel list - get's added to
	 * @param folder folder to search
	 * @param filter file filter
	 */
	private void listDataFiles(ArrayList<File> fileList, File folder, PamFileFilter filter) {
		File[] newFiles = folder.listFiles(filter);
		if (newFiles == null) {
			return;
		}
		for (int i = 0; i < newFiles.length; i++) {
			if (newFiles[i].isFile()) {
				fileList.add(newFiles[i]);
			}
			else if (newFiles[i].isDirectory()) {
				listDataFiles(fileList, newFiles[i].getAbsoluteFile(), filter);
			}
		}
	}

}
