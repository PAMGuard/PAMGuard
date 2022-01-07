package whistleClassifier.training;

import java.io.File;
import java.util.ArrayList;

import whistleClassifier.WhistleClassifierControl;

import PamUtils.FileList;

public class TrainingFileList extends FileList {

	public ArrayList<File> getFileList(String folderName, boolean includeSubfolders) {
		String[] tFileEnds = new String[1];
		tFileEnds[0] = WhistleClassifierControl.trainingFileEnd;
		return super.getFileList(folderName, tFileEnds, includeSubfolders);
	}

}
