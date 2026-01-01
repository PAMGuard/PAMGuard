package PamUtils.worker.filelist;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import Acquisition.pamAudio.PamAudioFileFilter;
import PamUtils.worker.PamWorkProgressMessage;
import PamUtils.worker.PamWorker;
import clickDetector.WindowsFile;
import wavFiles.WavHeader;
import wavFiles.xwav.HarpCycle;
import wavFiles.xwav.HarpHeader;

/**
 * Worker which extracts acoustic files from a folder. 
 * @author Doug Gillespie
 *
 */
public class WavListWorker extends FileListWorker<WavFileType> {
	
	
	/**
	 * If true, load audio file information such as sample rate and number of channels when the file list is created. 
	 */
	public boolean loadAudioInfo = false;
	
	
	/**
	 * The wav file load listener
	 */
	private WavLoadListener loadListener = null;


	public WavListWorker(FileListUser<WavFileType> fileListUser) {
		super(fileListUser, new PamAudioFileFilter());
	}

//	public WavListWorker(FileListUser<WavFileType> fileListUser, String fileType) {
//		super(fileListUser, fileType);
//		// TODO Auto-generated constructor stub
//	}

	@Override
	public WavFileType createFile(File baseFile) {
		WavFileType type = new WavFileType(baseFile);
		
		if (loadAudioInfo) {
			type.getAudioInfo(loadListener);
		}
		return new WavFileType(baseFile); 
	}

	@Override
	public boolean eachFileTask(WavFileType aFile) {
		//Check this is not a hidden file. 
		if (aFile.isHidden()) return false;
		return super.eachFileTask(aFile);
	}

	@Override
	public boolean allFilesTask(FileListData<WavFileType> fileList) {
		if (fileList != null) {
			fileList.sortFileList();
		}
		return true;
	}

	

	@Override
	public void finaliseFileList(PamWorker<FileListData<WavFileType>> pamWorker,
			FileListData<WavFileType> fileList) {
		/*
		 *  get all files audio formats and split into chunks if it's xwav.
		 *  Will need to copy the list, empty it, and put everything back.  ??
		 */
		FileListData<WavFileType> fileListCopy = fileList.clone();
		ListIterator<WavFileType> it = fileListCopy.getFileIterator();
		// clear the original list
		fileList.clear();
		int n = fileListCopy.getFileCount();
		int ind = 0;
		while (it.hasNext()) {
			WavFileType aFile = it.next();
			// get the audio format. 
			String path = aFile.getAbsolutePath();
			ind++;
			if (path.toLowerCase().endsWith(".x.wav")) {
				try {
					WindowsFile windowsFile = new WindowsFile(new File(aFile.getAbsolutePath()), "r");
					WavHeader wavHeader = new WavHeader();
					if (wavHeader.readHeader(windowsFile) == false) {
						continue;
					}
					windowsFile.close();
					HarpHeader harpHeader = wavHeader.getHarpHeader();
					if (harpHeader != null) {
						/*
						 *  check for splits in the file and update accordingly
						 *  adding multiple chunks into the list. 
						 */
						int blockAlign = wavHeader.getBlockAlign();
						ArrayList<HarpCycle> cycles = harpHeader.harpCycles;
						for (HarpCycle cycle : cycles) {
							// make a new WavHEader object. 
							WavFileType harpFile = new WavFileType(aFile.getAbsoluteFile());
							harpFile.setSamplesOffset(cycle.getSamplesSkipped());
							harpFile.setStartMilliseconds(cycle.gettMillis());
							harpFile.setMaxSamples(cycle.getByteLength()/blockAlign);
							fileList.addFile(harpFile);
						}
						
					}
					else {
						fileList.addFile(aFile); // just put it back.
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
					continue;
				}
				pamWorker.update(new PamWorkProgressMessage(ind*100/n, path));
			}
			else {
				fileList.addFile(aFile); // just put it back. 
			}
		}
		
	}
	
	/**
	 * Should we load audio file information such as sample rate and number of channels when the file list is created?	
	 * @return - true if audio info will be loaded on every file.
	 */
	public boolean isLoadAudioInfo() {
		return loadAudioInfo;
	}

	/**
	 * Set whether audio file information such as sample rate and number of channels is loaded when the file list is created?	
	 * Default is false;
	 * @param loadAudioInfo - true to load audio info
	 */
	public void setLoadAudioInfo(boolean loadAudioInfo) {
		this.loadAudioInfo = loadAudioInfo;
	}

	public void setWavLoadListener(WavLoadListener laodListener) {
		this.loadListener = laodListener;
	
	}


}
