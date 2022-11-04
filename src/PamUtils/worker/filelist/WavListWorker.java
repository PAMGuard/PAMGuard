package PamUtils.worker.filelist;

import java.io.File;

import Acquisition.pamAudio.PamAudioFileFilter;

/**
 * Worker which extracts acoustic files from a folder. 
 * @author Doug Gillespie
 *
 */
public class WavListWorker extends FileListWorker<WavFileType> {

	public WavListWorker(FileListUser<WavFileType> fileListUser) {
		super(fileListUser, new PamAudioFileFilter());
	}

//	public WavListWorker(FileListUser<WavFileType> fileListUser, String fileType) {
//		super(fileListUser, fileType);
//		// TODO Auto-generated constructor stub
//	}

	@Override
	public WavFileType createFile(File baseFile) {
		return new WavFileType(baseFile); 
	}

	@Override
	public boolean eachFileTask(WavFileType aFile) {
		return super.eachFileTask(aFile);
	}

	@Override
	public boolean allFilesTask(FileListData<WavFileType> fileList) {
		if (fileList != null) {
			fileList.sortFileList();
		}
		return true;
	}

}
