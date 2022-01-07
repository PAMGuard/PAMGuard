package PamUtils.worker.filelist;

public interface WavListUser extends FileListUser<WavFileType> {

	@Override
	public void newFileList(FileListData<WavFileType> fileListData);

}
