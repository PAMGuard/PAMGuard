package whistleClassifier;

import java.io.File;
import java.util.ArrayList;

import PamUtils.FileParts;

/**
 * A collection of training data for multiple species.
 * <p>
 * Contains references to a list of several TrainingDataGroups (one per species)
 * <p>
 * each of which contains references to one or more TrainingDataSets (one per file / storage unit)  
 * @author Doug Gillespie
 * @see TrainingDataGroup
 * @see TrainingDataSet
 *
 */
public class TrainingDataCollection {

	private ArrayList<TrainingDataGroup> trainingDataGroups;
	
	private TrainingDataStore currentTrainingStore;
	
	private TrainingDataStore trainingStore = new FileTrainingStore();
	
	private WhistleClassifierControl whistleClassifierControl;
	
	public TrainingDataCollection(WhistleClassifierControl whistleClassifierControl) {
		this.whistleClassifierControl = whistleClassifierControl;
		trainingDataGroups = new ArrayList<TrainingDataGroup>();
	}
	
	public void clearStore() {
		trainingDataGroups.clear();
		currentTrainingStore = null;
	}
	
	/**
	 * Load all the training data for all species from a given store. 
	 * <p>
	 * This functionality should probably be put into another abstract class
	 * so that different types of store can be used. Maniana !
	 * 
	 * @param trainingDataStore
	 * @return true if loaded something successfully
	 */
	public boolean loadTrainingData(String folderName, boolean subFolders, boolean useFolderNames) {
		clearStore();
		TrainingFileList tfl = new TrainingFileList();
		ArrayList<File> trainingFiles = tfl.getFileList(folderName, subFolders);
		for (int i = 0; i < trainingFiles.size(); i++) {
			addFileData(trainingFiles.get(i), useFolderNames);
		}
		
		return true;
	}
	
	private boolean addFileData(File file, boolean useFolderNames) {
		TrainingDataSet dataSet = trainingStore.readData(file.getAbsolutePath());
		if (dataSet == null) {
			return false;
		}
		dataSet.setStorageSource(file.getName());
		String species = dataSet.getSpecies();
		if (useFolderNames) {
			FileParts fileParts = new FileParts(file);
			species = fileParts.getLastFolderPart();
		}
		TrainingDataGroup trainingDataGroup = findDataGroup(species);
		if (trainingDataGroup == null) {
			trainingDataGroup = createDataGroup(species);
		}
		trainingDataGroup.addDataSet(dataSet);
		
		return true;
	}
	
	private TrainingDataGroup findDataGroup(String species) {
		if (trainingDataGroups == null) {
			return null;
		}
		for (int i = 0; i < trainingDataGroups.size(); i++) {
			if (trainingDataGroups.get(i).species.equalsIgnoreCase(species)) {
				return trainingDataGroups.get(i);
			}
		}
		return null;
	}
	
	private TrainingDataGroup createDataGroup(String species) {
		if (trainingDataGroups == null) {
			trainingDataGroups = new ArrayList<TrainingDataGroup>();
		}
		TrainingDataGroup newGroup = new TrainingDataGroup(species);
		trainingDataGroups.add(newGroup);
		return newGroup;
	}
	
	public int getNumTrainingGroups() {
		if (trainingDataGroups == null) {
			return 0;
		}
		return trainingDataGroups.size();
	}
	
	public TrainingDataGroup getTrainingDataGroup(int iGroup) {
		return trainingDataGroups.get(iGroup);
	}
	
	public void dumpStoreContent() {
		System.out.println("Training store contains dat for " + trainingDataGroups.size() + " species:");
		TrainingDataGroup tdg;
		String species;
		int nContours;
		int nFragments;
		int nFiles;
		for (int i = 0; i < trainingDataGroups.size(); i++) {
			tdg = trainingDataGroups.get(i);
			species = tdg.species;
			nFiles = tdg.getNumDataSets();
			nContours = tdg.getNumContours();
			nFragments = tdg.getNumFragments(whistleClassifierControl.getWhistleFragmenter(), 8);
			System.out.println(String.format("  %s total %d files with %d contours and %d fragments (%d long)",
					species, nFiles, nContours, nFragments, 8));
		}
	}
}
