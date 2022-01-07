package whistleClassifier.training;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import javax.swing.SwingWorker;

import classifier.Classifier;
import classifier.GroupMeans;

import Jama.Matrix;
import PamUtils.PamCalendar;
import whistleClassifier.FragmentClassifierParams;
import whistleClassifier.FragmentParameteriser;
import whistleClassifier.WhistleClassifierControl;

public class ClassifierTrainer {

	private WhistleClassifierControl whistleClassifierControl;

	private TrainingDataCollection trainingDataCollection;

	private TrainingObserver trainingObserver;

	public ClassifierTrainer(WhistleClassifierControl whistleClassifierControl,
			TrainingDataCollection trainingDataCollection,
			TrainingObserver trainingObserver) {
		super();
		this.whistleClassifierControl = whistleClassifierControl;
		this.trainingDataCollection = trainingDataCollection;
		this.trainingObserver = trainingObserver;
	}

	private BootstrapRunnable bootsRunnable;
	private int nBootstrap;
	private int sectionLength;
	private volatile boolean runningBootstrap;
	private volatile boolean stopBootstrap;
	private TrainingSelector trainingSelector;
	private FragmentParameteriser fragmentParameteriser;
	private FragmentClassifierParams fragmentClassifierParams;
	private boolean dumpTextFile;
	private BufferedWriter textFileWriter;

	private double minProbability;
	protected boolean startBootstrap(FragmentClassifierParams fragmentClassifierParams, boolean dumpTextFile) {
		this.fragmentClassifierParams = fragmentClassifierParams;
		this.nBootstrap = fragmentClassifierParams.getNBootstrap();
		this.sectionLength = fragmentClassifierParams.getSectionLength();
		this.minProbability = fragmentClassifierParams.getMinimumProbability();
		this.dumpTextFile = dumpTextFile;
		trainingObserver.setStatus(new ClassifierTrainingProgress("Starting Bootstrap"));
		bootsRunnable = new BootstrapRunnable();
		bootsRunnable.execute();

		return true;
	}

	protected void stop() {
		stopBootstrap = true;
	}
	private Matrix[] confusionMatrixes;
	private Matrix[] pairVariances, pairSTDs;
	private Matrix meanConfusion, stdConfusion, l95Confusion, u95Confusion;

	/**
	 * Create a text output file with a name based on the current date
	 * @return buffered writer for text output. 
	 */
	private BufferedWriter createTextOutputStream() {
		String folderName = this.whistleClassifierControl.getWhistleClassificationParameters().trainingDataFolder;
		String filePath = PamCalendar.createFileName(System.currentTimeMillis(), folderName, 
				"ClassifierTraining", ".txt");
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(filePath));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return bw;
	}

	/**
	 * Do a final training of the classifier with all data.
	 */
	private void trainWithAll() {

		
		bootsRunnable.forwardMessage(" ");
		bootsRunnable.forwardMessage("*** Perform final training with all data ***");
		createTrainTestData(1);
		String err;
		Classifier classifier = whistleClassifierControl.getFragmentClassifier(); 

		err = classifier.trainClassification(allTrainingData, trainingGroup);
		if (err != null) {
			bootsRunnable.forwardMessage(err);
		}
		bootsRunnable.forwardMessage("******************* DONE *******************");
	}


	public Matrix getMeanConfusion() {
		return meanConfusion;
	}

	public Matrix getSTDConfusion() {
		return stdConfusion;
	}

	private double[][] allTrainingData;
	private double[][][] allTestData;
	int[] trainingGroup;
	int[][] testGroup;

	/**
	 * Create randomly selected sets of training and test data. 
	 * @param trainingFraction Fraction of data to be used for training (Normally 2/3)
	 * @return true if >0 training and test sets created. 
	 */
	private boolean createTrainTestData(double trainingFraction) {

		int nSpecies = trainingDataCollection.getNumTrainingGroups();
		int[][] nTrainTestData = new int[nSpecies][];
		int totalTraining = 0;
		int totalTest = 0;
		//		whistleClassifierControl->minFreq;
		/*
		 * What a pain - each trainingSelect.createSetions call will
		 * do one species - need to accumulate output for all of these
		 * into a single matrix or array, but we won't know how many 
		 * there are for a long while yet - so will have to 
		 * accumulate everything in a list that can grow, 
		 * then turn it into a matrix later. 
//		 */
		int nTestSets = trainingSelector.getNumTestSets();
		double[][][] trainingData = new double[nSpecies][][];
		double[][][][] testData = new double[nTestSets][nSpecies][][]; 

		for (int iSpecies = 0; iSpecies < nSpecies; iSpecies++) {
			nTrainTestData[iSpecies] = trainingSelector.createSections(iSpecies, 
					trainingFraction, sectionLength, fragmentClassifierParams.getMinFrequency(),
					fragmentClassifierParams.getMaxFrequency(), fragmentClassifierParams.minimumContourLength);
			totalTraining += nTrainTestData[iSpecies][0];
			totalTest += nTrainTestData[iSpecies][1];
			String aString = String.format("Run %s with %d training and %d tests", 
					trainingDataCollection.getTrainingDataGroup(iSpecies).species, 
					nTrainTestData[iSpecies][0], nTrainTestData[iSpecies][1]);
			bootsRunnable.forwardMessage(aString);
			if (textFileWriter != null) {
				try {
					textFileWriter.newLine();
					textFileWriter.write(aString);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			trainingData[iSpecies] = trainingSelector.getParameterArray(0);
			for (int iTestSet = 0; iTestSet < nTestSets; iTestSet++) {
				testData[iTestSet][iSpecies] = trainingSelector.getParameterArray(iTestSet+1);
			}

		}

		allTrainingData = new double[totalTraining][];
		trainingGroup = new int[totalTraining];
		if (totalTest > 0) {
			allTestData = new double[nTestSets][totalTest][];
			testGroup = new int[nTestSets][totalTest];
		}
		int iTrain = 0;
		int[] iTest = new int[nTestSets];
		double[][] speciesTestData;
		double[][] speciesTrainingData;
		for (int iSpecies = 0; iSpecies < nSpecies; iSpecies++) {
			speciesTrainingData = trainingData[iSpecies];
			if (speciesTrainingData == null) {
				return false;
			}
			for (int i = 0; i < speciesTrainingData.length; i++) {
				allTrainingData[iTrain] = speciesTrainingData[i];
				trainingGroup[iTrain] = iSpecies;
				iTrain++;
			}
			if (testData != null) {
				for (int iT = 0; iT < nTestSets; iT++) {
					speciesTestData = testData[iT][iSpecies];
					if (speciesTestData != null) {
						for (int i = 0; i < speciesTestData.length; i++) {
							allTestData[iT][iTest[iT]] = speciesTestData[i];
							testGroup[iT][iTest[iT]] = iSpecies;
							iTest[iT]++;
						}
					}
				}
			}

		}
		return (totalTest > 0 && totalTraining > 0);
	}

	/**
	 * Perform a single training and testing cycle using some
	 * randomly selected fraction of the data. 
	 * @param iBoot
	 */
	private void oneBootstrap(int iBoot) {
		/** 
		 * now finally have test and training data for all species.
		 * So run the classifier.  
		 */

		int nTestSets = trainingSelector.getNumTestSets();
		
		if (createTrainTestData(2./3.) == false) {
			return;
		}

		String err;
		Classifier classifier = whistleClassifierControl.getFragmentClassifier(); 
		classifier.setMinimumProbability(minProbability);
		err = classifier.trainClassification(allTrainingData, trainingGroup);
		if (err != null) {
			bootsRunnable.forwardMessage(err);
		}

		int iConfusion;
		for (int iT = 0; iT < nTestSets; iT++) {
			iConfusion = iBoot*nTestSets + iT;
			int[] species = classifier.runClassification(allTestData[iT]);
			confusionMatrixes[iConfusion] = getConfusionMatrix(testGroup[iT], species);
			if (textFileWriter != null) {
				dumpToTextfile(String.format("Confusion Matrix %d.%d", iBoot+1, iT), confusionMatrixes[iConfusion]);
//				try {
//					Matrix m = confusionMatrixes[iConfusion];
//					//			dump the confusion matrix
//					int nCol = m.getColumnDimension();
//					int nRow = m.getRowDimension();
//					String str;
//					for (int iR = 0; iR < nRow; iR++) {
//						textFileWriter.newLine();
//						for (int iC = 0; iC < nCol; iC++) {
//							textFileWriter.write(String.format("%5.4f", m.get(iR, iC)));
//							if (iC < nCol-1) {
//								textFileWriter.write("; ");
//							}
//						}
//					}
//
//				} catch (IOException e) {
//					System.out.println("Error in classifier training bootstrap " + (iBoot+1));
//					e.printStackTrace();
//				}
			}
		}
		if (nTestSets > 1) {
			MatrixMean bsMean = new MatrixMean(confusionMatrixes, iBoot*nTestSets, nTestSets);
			Matrix m = bsMean.getMean();
			Matrix s = bsMean.getSTD();
			pairSTDs[iBoot] = s;
			pairVariances[iBoot] = s.arrayTimes(s);
			dumpMatrix(m, "Mean matrix for bootstrap " + (iBoot+1));
			dumpMatrix(s, "STD matrix for bootstrap " + (iBoot+1));
			if (textFileWriter != null) {
				dumpToTextfile("Mean matrix for bootstrap " + (iBoot+1), m);
				dumpToTextfile("STD matrix for bootstrap " + (iBoot+1), s);
			}
		}
	}

	private Matrix getConfusionMatrix(int[] truth, int[] result) {
		int[] uniqueGroups = GroupMeans.unique(truth);
		int nGroup = uniqueGroups.length;
		double[][] confusion = new double[nGroup][nGroup+1];
		int[] count = new int[nGroup];
		int[] truthInds = GroupMeans.getGroupIndex(uniqueGroups, truth);
		int[] resultInds = GroupMeans.getGroupIndex(uniqueGroups, result);
		for (int i = 0; i < result.length; i++) {
			count[truthInds[i]] ++;
			if (result[i] < 0) {
				confusion[truthInds[i]][nGroup] ++;
			}
			else {
				confusion[truthInds[i]][result[i]] ++;
			}
		}
		for (int iR = 0; iR < nGroup; iR++) {
			for (int iG = 0; iG < nGroup+1; iG++) {
				confusion[iR][iG] /= count[iR]; 
			}
		}
		return new Matrix(confusion);
	}
	protected void dumpToTextfile(String title, Matrix m) {
		if (textFileWriter == null) {
			return;
		}
		try {
			textFileWriter.newLine();
			textFileWriter.write(title);
			//			dump the confusion matrix
			int nCol = m.getColumnDimension();
			int nRow = m.getRowDimension();
			String str;
			for (int iR = 0; iR < nRow; iR++) {
				textFileWriter.newLine();
				for (int iC = 0; iC < nCol; iC++) {
					textFileWriter.write(String.format("%5.4f", m.get(iR, iC)));
					if (iC < nCol-1) {
						textFileWriter.write("; ");
					}
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	class BootstrapRunnable extends SwingWorker<Integer, ClassifierTrainingProgress> {

		boolean completed = false;
		@Override
		protected Integer doInBackground() throws Exception {
			try {
				completed = false;
				runningBootstrap = true;
				stopBootstrap = false;
				runBootstrap();
				runningBootstrap = false;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
		public void forwardMessage(String message) {
			publish(new ClassifierTrainingProgress(message));
		}

		/**
		 * Run the bootstrap - this gets called in a separate thread. 
		 * @param bootstrapRunnable 
		 */
		private void runBootstrap() {
			if (dumpTextFile) {
				textFileWriter = createTextOutputStream();
			}
//			publish(new ClassifierTrainingProgress(TrainingObserver.START, nBootstrap, 0));
			trainingSelector = new SequentialTrainingSelector();
//			trainingSelector = new PairedSequentialTrainingSelector();
			trainingSelector.setTrainingDataCollection(trainingDataCollection);
			trainingSelector.setWhistleFragmenter(whistleClassifierControl.getWhistleFragmenter());
			fragmentParameteriser = whistleClassifierControl.getFragmentParameteriser();
			confusionMatrixes = new Matrix[nBootstrap*trainingSelector.getNumTestSets()];
			pairVariances = new Matrix[nBootstrap];
			pairSTDs = new Matrix[nBootstrap];
			meanConfusion = null;
			stdConfusion = null;
			l95Confusion = u95Confusion = null;
			if (textFileWriter != null) {
				try {
					textFileWriter.write(String.format("Total Bootstraps %d", nBootstrap));
					String[] species = trainingDataCollection.getSpeciesList();
					textFileWriter.newLine();
					textFileWriter.write(String.format("Num Species %d", species.length));
					textFileWriter.newLine();
					textFileWriter.write(String.format("FragmentLength %d; Section Length %d", 
							fragmentClassifierParams.getFragmentLength(), fragmentClassifierParams.getSectionLength()));
					textFileWriter.newLine();
					for (int i = 0; i < species.length; i++) {
						textFileWriter.write(species[i]);
						if (i < species.length-1) {
							textFileWriter.write("; ");
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			for (int i = 0; i < nBootstrap; i++) {
				if (stopBootstrap) {
					break;
				}
				
				publish(new ClassifierTrainingProgress(ClassifierTrainingProgress.START_ONE, nBootstrap, i));

				if (textFileWriter != null) {
					try {
						textFileWriter.newLine();
						textFileWriter.write(String.format("Bootstrap %d", i));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				oneBootstrap(i);
				publish(new ClassifierTrainingProgress(ClassifierTrainingProgress.COMPLETE_ONE, 
						nBootstrap, i+1));
			}
			if (confusionMatrixes[confusionMatrixes.length-1] == null) {
				publish(new ClassifierTrainingProgress(ClassifierTrainingProgress.ABORT, 0, 0));
				return; // did not complete !
			}
			else {
				completed = true;
			}
//			publish(new ClassifierTrainingProgress(ClassifierTrainingProgress.COMPLETE_ALL, nBootstrap, nBootstrap));
			MatrixMean mm = new MatrixMean(confusionMatrixes);
			meanConfusion = mm.getMean();
			// now say the matrix - a bit clunky this, but who cares
			dumpMatrix(meanConfusion, "Mean Confusion Matrix (all bootstraps)");

			stdConfusion = mm.getSTD();
			//		Matrix cv = stdMatrix.arrayRightDivide(meanConfusion);
			dumpMatrix(stdConfusion, "STD Confusion Matrix (all bootstraps)");

			if (textFileWriter != null) {
				dumpToTextfile("Mean confusion", meanConfusion);
				dumpToTextfile("STD Confusion", stdConfusion);
			}

			Matrix l95, u95;
			if (nBootstrap > 20) {
				MatrixCI mci = new MatrixCI(confusionMatrixes);
				l95 = mci.getL95();
				u95 = mci.getU95();
				dumpMatrix(l95, "Lower 95% CI");
				dumpMatrix(u95, "Upper 95% CI");
				if (textFileWriter != null) {
					dumpToTextfile("Lower 95% CI", l95);
					dumpToTextfile("Upper 95% CI", u95);
				}
			}
			
			if (trainingSelector.getNumTestSets() > 1) {
				MatrixMean meanPairVar = new MatrixMean(pairVariances);
				Matrix varMean = meanPairVar.getMean();
				Matrix stdMean = varMean.copy();
				for (int iR = 0; iR < varMean.getRowDimension(); iR++) {
					for (int iC = 0; iC < varMean.getColumnDimension(); iC++) {
						stdMean.set(iR, iC, Math.sqrt(varMean.get(iR, iC)));
					}
				}
				dumpMatrix(stdMean, "RMS training pair STD");
				
				MatrixMean meanPairSTD = new MatrixMean(pairSTDs);
				Matrix meanSTD = meanPairSTD.getMean();
				dumpMatrix(meanSTD, "Mean training pair STD");

				if (textFileWriter != null) {
					dumpToTextfile("RMS training pair STD", stdMean);
					dumpToTextfile("Mean training pair STD", meanSTD);
				}
			}
			
			/*
			 * Finally, run the training with all data since in the future
			 * it will be run on different data to that just tested, so 
			 * might as well have best possible training. 
			 */
			trainWithAll();

			if (textFileWriter != null) {
				try {
					textFileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			trainingObserver.setStatus(new ClassifierTrainingProgress(ClassifierTrainingProgress.COMPLETE_ALL, nBootstrap, 0));
		}

	
		@Override
		protected void done() {
			if (completed == false) {
				return;
			}
			
		}


		@Override
		protected void process(List<ClassifierTrainingProgress> chunks) {
			for (int i = 0; i < chunks.size(); i++) {
				trainingObserver.setStatus(chunks.get(i));
			}
		}

	}

	private void dumpMatrix(Matrix m, String tit, String delimit) {
		dumpMatrix(m, tit, "");
	}

	private void dumpMatrix(Matrix m, String tit) {
		if (tit != null) {
			bootsRunnable.forwardMessage(tit);
		}
		String[] species = trainingDataCollection.getSpeciesList();
		int maxLen = 0;
		int nCol = m.getColumnDimension();
		int nRow = m.getRowDimension();
		int nspecies = species.length;
		for (int i = 0; i < nspecies; i++) {
			maxLen = Math.max(maxLen, species[i].length());
		}
		int colWid = maxLen+1;
		String str;
		str = "";
		for (int i = 0; i < colWid; i++) str += " ";
		for (int i = 0; i< nspecies; i++){
			str = addString(str, species[i], colWid);
		}
		str = addString(str, "??", colWid);
		bootsRunnable.forwardMessage(str);
		for (int iR = 0; iR < nRow; iR++) {
			str = addString("", species[iR], colWid);
			for (int iC = 0; iC < nCol; iC++) {
				str = addString(str, String.format("%d", (int)(m.get(iR, iC) * 100)), colWid);
			}
			bootsRunnable.forwardMessage(str);
		}
	}

	private String addString(String oldString, String newString, int minWidth) {
		String str = oldString + newString;
		for (int i = 0; i < (minWidth - newString.length()); i++) str += " ";
		return str;
	}


}
