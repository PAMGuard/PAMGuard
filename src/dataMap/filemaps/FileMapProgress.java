package dataMap.filemaps;

import javafx.scene.control.ProgressIndicator;
import pamViewFX.pamTask.PamTaskUpdate;

public class FileMapProgress extends PamTaskUpdate {
	
	public int countingState;
	
	public int totalFiles;
	
	public int openedFiles;
	
	public String fileName;
	
	public static final int STATE_LOADINGMAP = 1;
	public static final int STATE_SAVINGMAP = 2;
	public static final int STATE_COUNTINGFILES = 3;
	public static final int STATE_DONECOUNTINGFILES = 4;
	public static final int STATE_CHECKINGFILES = 5;
	public static final int STATE_MAPPINGFILES = 6;
	
	
	public FileMapProgress(int countingState, int totalFiles,
			int openedFiles, String fileName) {
		super();
		this.countingState = countingState;
		this.setStatus(countingState);
		this.totalFiles = totalFiles;
		this.openedFiles = openedFiles;
		this.fileName = fileName;
	}

	@Override
	public String getName() {
		return "Raw Sound File Map";
	}
	
	@Override
	public String getProgressString(){
		//filename is the update string
		String progressString=""; 
		switch (getStatus()) {
		case FileMapProgress.STATE_LOADINGMAP:
			progressString="Loading serialised file map";
			break;
		case FileMapProgress.STATE_SAVINGMAP:
			progressString="Loading serialised file map";
			break;
		case FileMapProgress.STATE_COUNTINGFILES:
			progressString=String.format("Counting files: %d", totalFiles);
			break;
		case FileMapProgress.STATE_DONECOUNTINGFILES:
			progressString="Finished Counting"; 
			break;
		case FileMapProgress.STATE_CHECKINGFILES:
			break;
		}
		return progressString; 
	}

	@Override
	public double getProgress() {
		double progress = ProgressIndicator.INDETERMINATE_PROGRESS ;
		switch (getStatus()) {
		case FileMapProgress.STATE_LOADINGMAP:
			progress=ProgressIndicator.INDETERMINATE_PROGRESS ;
			break;
		case FileMapProgress.STATE_SAVINGMAP:
			progress=ProgressIndicator.INDETERMINATE_PROGRESS ;
			break;
		case FileMapProgress.STATE_COUNTINGFILES:
			progress=ProgressIndicator.INDETERMINATE_PROGRESS ;
			break;
		case FileMapProgress.STATE_DONECOUNTINGFILES:
			progress=1; 
			break;
		case FileMapProgress.STATE_CHECKINGFILES:
			progress=((double) openedFiles)/totalFiles; 
			break;
		}
		return progress; 
	}

}
