package qa;

import java.util.ArrayList;
import java.util.ListIterator;

import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamViewParameters;
import PamUtils.PamCalendar;
import generalDatabase.DBControlUnit;
import generalDatabase.clauses.FixedClause;
import qa.database.QATestLogging;
import qa.generator.QAGeneratorProcess;
import qa.generator.clusters.QACluster;
import qa.generator.location.RandomLocationGenerator;
import qa.generator.testset.LongRandomTestSet;
import qa.generator.testset.QATestSet;
import qa.operations.OpsStatusParams;
import qa.operations.QAOpsDataUnit;

/**
 * Planning to keep one random test per cluster type. This will be re-extracted from the 
 * database at start up so that the count of clusters becomes the total for the database
 * and will continue to count up however many times PAMGuard restarts. 
 * @author dg50
 *
 */
public class RandomTestManager implements QANotifyable {

	private QAControl qaControl;
	private QATestDataBlock testsDataBlock;
	private QAGeneratorProcess generatorProcess;

	public RandomTestManager(QAControl qaControl) {
		this.qaControl = qaControl;
		this.testsDataBlock = qaControl.getQaGeneratorProcess().getTestsDataBlock();
		generatorProcess = qaControl.getQaGeneratorProcess();
		qaControl.addNotifyable(this);
	}
	
	/**
	 * Called whenever selected tests are changed and will remove
	 * or create an appropriate test for each selected cluster. 
	 * @return number of tests currently active. 
	 */
	public int manageTests() {
		int nTests = 0;
		boolean isRunning = qaControl.isRunning();
		boolean allowedopsState = isAllowedState();
		QAParameters params = qaControl.getQaParameters();
		ArrayList<QACluster> clusters = qaControl.getAvailableClusters();
		for (QACluster cluster:clusters) {
			ClusterParameters clusterParams = params.getClusterParameters(cluster);
			/*
			 * Try to find an active LongRandomTestSet for this cluster in the tests datablock. 
			 */
			boolean wantRun = isRunning && allowedopsState && clusterParams.runRandom;
			QATestDataUnit currentDataUnit = findTestDataUnit(cluster);
			if (wantRun && currentDataUnit != null) {
				// make sure that the test set is active. if it's already 
				//active don't need to do anything. 
				LongRandomTestSet lrts = (LongRandomTestSet) currentDataUnit.getQaTestSet();
				String currentState = lrts.getStatus();
				if (QATestSet.STATUS_ACTIVE.equalsIgnoreCase(currentState) == false) {
					lrts.setStatus(QATestSet.STATUS_ACTIVE);
					lrts.resetNextSample(generatorProcess.getCurrentSample(), generatorProcess.getSampleRate());
					testsDataBlock.updatePamData(currentDataUnit, PamCalendar.getTimeInMillis());
				}
			}
			else if (wantRun && currentDataUnit == null) {
				// want it, but don't have it, so make it
				startRandomTest(cluster);
			}
			else if (wantRun == false && currentDataUnit != null) {
				// have it but don't want it, so stop the test.
				stopRandomTest(currentDataUnit);
			}
			else if (wantRun == false && currentDataUnit == null) {
				// don't have it and don't want it. 
			}
			
		}
		
		return nTests;
	}
	
	/**
	 * Called at startup to read old tests back out of the database. 
	 */
	private void initialiseRandomTests() {
		loadOldTests();
		ArrayList<QACluster> clusters = qaControl.getAvailableClusters();
		for (QACluster cluster:clusters) {
			initialiseTests(cluster);
		}
	}

	private void initialiseTests(QACluster cluster) {
//		loadOldTest(cluster.getName());
	}

	private void loadOldTests() {
		QATestLogging testLogging = qaControl.getQaGeneratorProcess().getQaTestLogging();
		PamViewParameters pvp = new FixedClause("WHERE TRIM(Test_Type)='" + QAControl.randomTestName + "' ");
		testLogging.loadEarlyData(DBControlUnit.findConnection(), pvp);
		/*
		 * But now have to go through these, remove them and turn them into 
		 * LongRandomTestSets. What a bodge !
		 */
		ArrayList<LongRandomTestSet> newTests = new ArrayList<>();
		ListIterator<QATestDataUnit> it = testsDataBlock.getListIterator(0);
		while (it.hasNext()) {
			QATestDataUnit testDataUnit = it.next();
			QATestSet oldSet = testDataUnit.getQaTestSet();
			QACluster cluster = qaControl.findCluster(oldSet.getTestName());
			if (cluster == null) {
				continue;
			}
			LongRandomTestSet lrst = makeRandomTest(cluster);
			testDataUnit.setQaTestSet(lrst);
			lrst.setStatus(QATestSet.STATUS_IDLE);
			lrst.setnOldSequences(oldSet.getNumSequences());
			lrst.setTestDataUnit(testDataUnit);
			generatorProcess.addTestSet(testDataUnit);
		}
	}

	private LongRandomTestSet makeRandomTest(QACluster cluster) {
		ClusterParameters clusterParams = qaControl.getQaParameters().getClusterParameters(cluster);
		double[] rangeLims = qaControl.getRangeLimits(clusterParams.monitorRange);
		RandomLocationGenerator locationGenerator = new RandomLocationGenerator(qaControl, cluster, Integer.MAX_VALUE, rangeLims);
		LongRandomTestSet lrst = new LongRandomTestSet(qaControl, cluster.getName(), locationGenerator, cluster, 
				generatorProcess.getSampleRate(), generatorProcess.getCurrentSample());
		return lrst;
	}
	
	private void startRandomTest(QACluster cluster) {
		LongRandomTestSet lrst = makeRandomTest(cluster);
		QATestDataUnit dataUnit = new QATestDataUnit(PamCalendar.getTimeInMillis(), QAControl.randomTestName, lrst);
		generatorProcess.addTestSet(dataUnit);
	}

	private void stopRandomTest(QATestDataUnit testDataUnit) {
		long now = PamCalendar.getTimeInMillis();
//		testDataUnit.getQaTestSet().setEndTime(now);
		testDataUnit.getQaTestSet().setStatus(QATestSet.STATUS_IDLE);
		testsDataBlock.updatePamData(testDataUnit, now);
	}

	/**
	 * Find an existing ACTIVE test set data unit in the generator data block. 
	 * @param qaCluster
	 * @return found test or null
	 */
	private QATestDataUnit findTestDataUnit(QACluster qaCluster) {
		synchronized (testsDataBlock.getSynchLock()) {
			ListIterator<QATestDataUnit> it = testsDataBlock.getListIterator(0);
			while (it.hasNext()) {
				QATestDataUnit du = it.next();
				QATestSet testSet = du.getQaTestSet();
				if (testSet instanceof LongRandomTestSet && testSet.getQaCluster() == qaCluster) {
//					if (testSet.getStatus().equals(QATestSet.STATUS_ACTIVE)) {
						return du;
//					}
				}
			}
		}
		return null;
	}

	/**
	 * find if random tests are allowed while it's in this state. 
	 * @return true if random testing allowed for this ops state. 
	 */
	private boolean isAllowedState() {
		QAOpsDataUnit currentState = qaControl.getQaOperationsStatus().getCurrentStatus();
		if (currentState == null) {
			return false;
//			return WarnOnce.showWarning(qaControl.getGuiFrame(), title, message, messageType)
		}
		OpsStatusParams opsChoices = qaControl.getQaParameters().getOpsStatusParams(currentState.getOpsStatusCode());
		if (opsChoices == null) {
			return false;
		}
		return opsChoices.isAllowRandomTesting();
	}

	@Override
	public void qaNotify(int noteCode, Object noteObject) {
		switch (noteCode) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			initialiseRandomTests();
			break;
		case  QANotifyable.TEST_SELECT_CHANGED:
			if (qaControl.isRunning()) {
				manageTests();
			}
			break;
		case QANotifyable.PAM_START:
		case QANotifyable.PAM_STOP:
		case QANotifyable.OPS_STATUS_CHANGE:
		case QANotifyable.PARAMETER_CHANGE:
			manageTests();
			break;
		}
	}

}
