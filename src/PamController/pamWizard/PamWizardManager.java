package PamController.pamWizard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import PamController.PamController;
import PamController.soundMedium.GlobalMedium.SoundMedium;
import PamUtils.worker.filelist.FileListData;
import PamUtils.worker.filelist.WavFileType;
import PamUtils.worker.filelist.WavListUser;
import PamUtils.worker.filelist.WavListWorker;

/**
 * Manages the creation of automatic PAMGaurcd configurations. 
 * 
 * @author Jamie Macaulay
 */
public class PamWizardManager {
	
	public List<PamAutoConfig> autoConfigs;
	
	public FileListData<WavFileType> currentFiles; 
	
	/**
	 * Creates a list of wav files.
	 */
	WavListWorker wavListWorker = new WavListWorker(new WavListReceiver());

	private class WavListReceiver implements WavListUser {

		@Override
		public void newFileList(FileListData<WavFileType> fileListData) {
			newSoundFileList(fileListData);
		}
	}
	
	
	private void newSoundFileList(FileListData<WavFileType> fileListData) {
		currentFiles = fileListData; 
		
		System.out.println("PamWizardManager Found "+currentFiles.getFileCount()+" audio files.");
		
        // Create a few dummy PamAutoConfig implementations for demo
        List<PamAutoConfig> demo = new ArrayList<>();
        demo.add(new PamAutoConfig() {
            public boolean isConfigValid(PamFileImport importHandler) { return true; }
            public String getConfigDescription() { return "Air config for species A"; }
            public String[] getSpeciesList() { return new String[]{"Species A", "Species B"}; }
            public String getConfigName() { return "Air Config 1"; }
            public SoundMedium getGlobalMediumSettings() { return SoundMedium.Air; }
        });
        demo.add(new PamAutoConfig() {
            public boolean isConfigValid(PamFileImport importHandler) { return true; }
            public String getConfigDescription() { return "Water config only"; }
            public String[] getSpeciesList() { return new String[]{"Species C"}; }
            public String getConfigName() { return "Water Config"; }
            public SoundMedium getGlobalMediumSettings() { return SoundMedium.Water; }
        });
        demo.add(new PamAutoConfig() {
            public boolean isConfigValid(PamFileImport importHandler) { return true; }
            public String getConfigDescription() { return "Both media, many species"; }
            public String[] getSpeciesList() { return new String[]{"Species A", "Species C"}; }
            public String getConfigName() { return "Both Config"; }
            public SoundMedium getGlobalMediumSettings() { return null; }
        });

        SwingUtilities.invokeLater(() -> {
            PamAutoConfigDialog dlg = new PamAutoConfigDialog(null, demo);
            PamAutoConfig sel = dlg.showDialog();
            System.out.println("Selected: " + (sel == null ? "<none>" : sel.getConfigName()));
            System.exit(0);
        });
	}


	
	public PamWizardManager(PamController pamController) {
		createAutoConfigs(); 
	}
	
	
	
	private void createAutoConfigs() {
		/***Add automated configuration here***/
		
	}

	/**
	 * Called whenever new files are imported into PamGuard via drag and drop or other methods. 
	 * @param files - a folder or file. These should be checked to see if they are audio files 
	 * that can be used to create a PAMGuard configuration.
	 */
	public void newImportedFiles(List<File> files) {
		currentFiles = null;
		String[] rootList = filesToPathArray(files);
		
		//Swing way
		wavListWorker.startFileListProcess(PamController.getMainFrame(), rootList,
				true, true);
	
//		/**Now check each auto config to see if it can handle the imported files**/
//		ArrayList<PamAutoConfig> validConfigs = new ArrayList<PamAutoConfig>();
//		for (PamAutoConfig config : autoConfigs) {
//			if (config.isConfigValid(importHandler)) {
//				validConfigs.add(config);
//			}
//			
//		}
	}

	/**
	 * Convert a list of File objects to a String array of absolute paths.
	 * Null entries are skipped. If the input list is null or contains no valid
	 * files this returns an empty array.
	 * @param files list of File objects
	 * @return array of absolute path strings
	 */
	public static String[] filesToPathArray(List<File> files) {
		if (files == null || files.isEmpty()) return new String[0];
		List<String> paths = new ArrayList<>();
		for (File f : files) {
			if (f == null) continue;
			paths.add(f.getAbsolutePath());
		}
		return paths.toArray(new String[0]);
	}

}