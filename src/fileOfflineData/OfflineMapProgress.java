package fileOfflineData;

public class OfflineMapProgress {

	boolean stillCounting;
	
	int totalFiles;
	
	int fileIndex;

	public OfflineMapProgress(boolean stillCounting) {
		super();
		this.stillCounting = stillCounting;
	}

	public OfflineMapProgress(int totalFiles, int fileIndex) {
		super();
		stillCounting = false;
		this.totalFiles = totalFiles;
		this.fileIndex = fileIndex;
	}
	
	
}
