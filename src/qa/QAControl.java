package qa;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import AirgunDisplay.AirgunControl;
import Array.ArrayManager;
import GPS.GPSControl;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.positionreference.PositionReference;
import PamController.positionreference.PositionReferenceFinder;
import PamView.PamSidePanel;
import PamguardMVC.PamDataBlock;
import qa.analyser.QAAnalyser;
import qa.database.QASoundLogging;
import qa.generator.QAGeneratorProcess;
import qa.generator.clusters.BeakedWhaleCluster;
import qa.generator.clusters.BlackFishCluster;
import qa.generator.clusters.DolphinClicksCluster;
import qa.generator.clusters.DolphinWhistlesCluster;
import qa.generator.clusters.HFBaleenCluster;
import qa.generator.clusters.LFBaleenCluster;
import qa.generator.clusters.PorpoiseCluster;
import qa.generator.clusters.QACluster;
import qa.generator.clusters.RightWhaleCluster;
import qa.generator.clusters.SpermWhaleCluster;
import qa.generator.location.LocationManager;
import qa.generator.location.QALocationGenerator;
import qa.generator.testset.QATestSet;
import qa.monitor.QAMonitorProcess;
import qa.operations.QAOperationsStatus;
import qa.swing.QADisplayProvider;
import qa.swing.QASidePanel;
import userDisplay.UserDisplayControl;

public class QAControl extends PamControlledUnit implements PamSettings, QANotifyable {

	public static final String quickTestName = "Quick";
//	public static final String randomTestName = "Random";
	public static final String randomTestName = "Random Drill";

	private static String unitType = "QA"; // leave so it doesn't break old configurations. 
	
	private QAParameters qaParameters = new QAParameters();
	
	private QAGeneratorProcess qaGeneratorProcess;
	
	private QAMonitorProcess qaMonitorProcess;
	
	private ArrayList<QACluster> availableClusters;
	
	private QAAnalyser qaAnalyser;
	
	private PositionReferenceFinder positionReferenceFinder;
	
	private PositionReference positionReference;
	
	private LocationManager locationManager;
	
	private QAOperationsStatus qaOperationsStatus;
	
	private ArrayList<QANotifyable> notifyables = new ArrayList<>();
	
	private RandomTestManager randomTestManager;
	
	private QASidePanel qaSidePanel;
	
	/**
	 * @return the positionReference
	 */
	public PositionReference getPositionReference() {
		return positionReference;
	}

	public QAControl(String unitName) {
		super(unitType, unitName);
		PamSettingManager.getInstance().registerSettings(this);
		
		qaGeneratorProcess = new QAGeneratorProcess(this);
		addPamProcess(qaGeneratorProcess);
		
		qaMonitorProcess = new QAMonitorProcess(this);
		addPamProcess(qaMonitorProcess);
		
		locationManager = new LocationManager(this);
		
		qaOperationsStatus = new QAOperationsStatus(this);
		
		availableClusters = new ArrayList<>();
		availableClusters.add(new LFBaleenCluster());
		availableClusters.add(new HFBaleenCluster());
		availableClusters.add(new RightWhaleCluster());
		availableClusters.add(new SpermWhaleCluster());
		availableClusters.add(new BeakedWhaleCluster());
		availableClusters.add(new DolphinWhistlesCluster());
		availableClusters.add(new DolphinClicksCluster());
		availableClusters.add(new BlackFishCluster());
		availableClusters.add(new PorpoiseCluster());
		
		qaAnalyser = new QAAnalyser(this);
		
		Class[] preferredPos = {AirgunControl.class, ArrayManager.class, GPSControl.class};
		positionReferenceFinder = new PositionReferenceFinder(preferredPos, true);
		
		randomTestManager = new RandomTestManager(this);
		
		qaSidePanel = new QASidePanel(this);
		
		UserDisplayControl.addUserDisplayProvider(new QADisplayProvider(this));
		
		addNotifyable(this); // get it's own notifications
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#notifyModelChanged(int)
	 */
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		switch (changeType) {
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			positionReference = positionReferenceFinder.findReference();
			break;
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
		case PamControllerInterface.REMOVE_CONTROLLEDUNIT:
			if (PamController.getInstance().isInitializationComplete()) {
				positionReference = positionReferenceFinder.findReference();
			}
		}
		
		tellNotifyables(changeType);
	}

	@Override
	public Serializable getSettingsReference() {
		return qaParameters;
	}

	@Override
	public long getSettingsVersion() {
		return QAParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		qaParameters = ((QAParameters) pamControlledUnitSettings.getSettings()).clone();
		return true;
	}

	/**
	 * @return the qaParameters
	 */
	public QAParameters getQaParameters() {
		return qaParameters;
	}

	/**
	 * @return the qaGeneratorProcess
	 */
	public QAGeneratorProcess getQaGeneratorProcess() {
		return qaGeneratorProcess;
	}

	/**
	 * @return the qaMonitorProcess
	 */
	public QAMonitorProcess getQaMonitorProcess() {
		return qaMonitorProcess;
	}

	/**
	 * Called during setup to make sure that the database tables 
	 * are all in place. 
	 * @param allDetectors 
	 */
	public void checkDatabase(ArrayList<PamDataBlock> allDetectors) {
		QASoundLogging qaLogging = qaGeneratorProcess.getQaLogging();
		qaLogging.checkDetectorList(allDetectors);
		if (qaAnalyser != null) {
			qaLogging = qaAnalyser.getQaLogging();
			qaLogging.checkDetectorList(allDetectors);
		}
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#pamToStart()
	 */
	@Override
	public void pamToStart() {
		tellNotifyables(QANotifyable.PAM_START);
	}

	/* (non-Javadoc)
	 * @see PamController.PamControlledUnit#pamHasStopped()
	 */
	@Override
	public void pamHasStopped() {
		super.pamHasStopped();
		tellNotifyables(QANotifyable.PAM_STOP);
		qaGeneratorProcess.getQaLogging().flushBuffer();
	}
	
	/**
	 * Quick access to an is running function
	 * @return true if PAMGuard is running
	 */
	public boolean isRunning() {
		return PamController.getInstance().getPamStatus() == PamController.PAM_RUNNING;
	}

//	/* (non-Javadoc)
//	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
//	 */
//	@Override
//	public JMenuItem createDetectionMenu(Frame parentFrame) {
//		JMenu injectMenu = new JMenu("QA Inject");
//		for (QACluster cluster:availableClusters) {
//			JMenuItem menuItem = new JMenuItem(cluster.getName());
//			menuItem.addActionListener(new InjectAction(cluster));
//			injectMenu.add(menuItem);
//		}
//		return injectMenu;
//	}
	
	/**
	 * @return the qaAnalyser
	 */
	public QAAnalyser getQaAnalyser() {
		return qaAnalyser;
	}

	/**
	 * @return the availableClusters
	 */
	public ArrayList<QACluster> getAvailableClusters() {
		return availableClusters;
	}
	
	private ArrayList<QATestDataUnit> currentQuickTests = new ArrayList<>();

	/**
	 * Start a set of quick tests for all selected sequences. 
	 */
	public void startQuickTests() {
		/**
		 * go through all the available sequences and see which ones to include in testing
		 */
		synchronized (currentQuickTests) {
			currentQuickTests.clear(); // should already be empty. 
			String locator = qaParameters.getQuickTestLocationGeneratorName();
			int nSeq = qaParameters.getnQuickTestSequences();
			for (QACluster cluster:availableClusters) {
				ClusterParameters clusterParams = qaParameters.getClusterParameters(cluster);
				if (!clusterParams.runImmediate) {
					continue;
				}
				QALocationGenerator locGen = locationManager.makeLocationGenerator(locator, cluster, nSeq, getRangeLimits(clusterParams.monitorRange));
				QATestDataUnit newTest = getQaGeneratorProcess().addTestSet(quickTestName, cluster, locGen);
				if (newTest != null) {
					currentQuickTests.add(newTest);
				}
			}

		}
	}
	
	/**
	 * Get min and max testing ranges as factors below and above nominal range
	 * @param nominalRange nominal range for a cluster
	 * @return min and max ranges as 2 element array. 
	 */
	public double[] getRangeLimits(double nominalRange) {
		double[] lims = {nominalRange/qaParameters.getRangeFactor(), nominalRange*qaParameters.getRangeFactor()};
		Arrays.sort(lims);
		return lims;
	}
	
	/**
	 * Called whenever a test ends. Check the quick tests and see if we want to report. 
	 */
	public void checkCurrentQuickTests() {
		int nActive = 0;
		synchronized (currentQuickTests) {
			for (QATestDataUnit tdu : currentQuickTests) {
				if (QATestSet.STATUS_ACTIVE.equals(tdu.getQaTestSet().getStatus())) {
					nActive ++;
				}
			}
			if (currentQuickTests.size() > 0 && nActive == 0) {
				if (qaParameters.immediateQuickReport) {
					reportInSwingThread(currentQuickTests);
				}
			}
		}
	}

	/**
	 * Used when a set of quick tests end, since that notification comes through in a worker thread 
	 * but the dialog that is shown at the start of tests needs to be initiated in a the AWT
	 * thread to avoid lockup.   
	 * @param currentQuickTestsArray list of quick tests to process. 
	 */
	private void reportInSwingThread(ArrayList<QATestDataUnit> currentQuickTests) {
		ArrayList<QATestDataUnit> testsCopy = new ArrayList<QATestDataUnit>(currentQuickTests);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				qaAnalyser.analyseTests(currentQuickTests);
			}
		});
	}

	/**
	 * @return the qaOperationsStatus
	 */
	public QAOperationsStatus getQaOperationsStatus() {
		return qaOperationsStatus;
	}
	
	/**
	 * Add something that can receive notifications from QA Control 
	 * @param notifyable
	 */
	public void addNotifyable(QANotifyable notifyable) {
		notifyables.add(notifyable);
	}
	
	/**
	 * Add something that can receive notifications from QA Control 
	 * @param notifyable
	 * @return 
	 */
	public boolean removeNotifyable(QANotifyable notifyable) {
		return notifyables.remove(notifyable);
	}
	
	/**
	 * Send out notification messages to all registered components
	 * @param noteCode notification code, will include all standard PAMController ones, use -ve numbers for module specific ones. 
	 * @param noteObject Object to go with the code. 
	 */
	public void tellNotifyables(int noteCode, Object noteObject) {
		for (QANotifyable qan:notifyables) {
			qan.qaNotify(noteCode, noteObject);
		}
	}
	
	@Override
	public void qaNotify(int noteCode, Object noteObject) {
		if (noteCode == QANotifyable.TEST_END) {
			checkCurrentQuickTests();
		}
	}

	/**
	 * Send out notification messages to all registered components with a null object
	 * @param noteCode notification code, will include all standard PAMController ones, use -ve numbers for module specific ones. 
	 */
	public void tellNotifyables(int noteCode) {
		tellNotifyables(noteCode, null);
	}
		
	/**
	 * find a cluster from it's name. 
	 * @param clusterName
	 * @return found cluster or null. 
	 */
	public QACluster findCluster(String clusterName) {
		for (QACluster cluster:availableClusters) {
			if (cluster.getName().equals(clusterName)) {
				return cluster;
			}
		}
		return null;
	}

	@Override
	public PamSidePanel getSidePanel() {
		if (isViewer()) {
			return null;
		}
		else {
			return qaSidePanel;
		}
		
	}

	/**
	 * Get the primary detection data block for a cluster. Will try to use a 
	 * user defined one, otherwise try to find a default. 
	 * @param qaCluster cluster
	 * @return suitable primary datablock, or null. 
	 */
	public PamDataBlock getClusterPrimaryDetector(QACluster qaCluster) {
		ClusterParameters clusterParams = qaParameters.getClusterParameters(qaCluster);
		// try to find the selected data block
		if (clusterParams.primaryDetectionBlock != null) {
			PamDataBlock foundDataBlock = PamController.getInstance().getDataBlockByLongName(clusterParams.primaryDetectionBlock);
			if (foundDataBlock != null) {
				return foundDataBlock;
			}
		}
		// otherwise get a list of possible data blocks
		ArrayList<PamDataBlock> possDataBlocks = PamController.getInstance().getDataBlocks(qaCluster.getPrimaryDetectorType(), false);
		for (PamDataBlock dataBlock:possDataBlocks) {
			// take the first one that seems sensible for this cluster. 
			if (qaAnalyser.canDetectCluster(qaCluster, dataBlock)) {
				clusterParams.primaryDetectionBlock = dataBlock.getLoggingName();
				return dataBlock;
			}
		}
		return null;
	}

	/**
	 * Called when the range factor has been changed in the 
	 * main control panel. Need to put in params and also tell 
	 * all random tests to update their range limits. 
	 * @param newRangeFactor
	 */
	public void setNewRangeFactor(double newRangeFactor) {
		if (newRangeFactor == qaParameters.getRangeFactor()) {
			return;
		}
		qaParameters.setRangeFactor(newRangeFactor);
		qaGeneratorProcess.notifyNewRangeFactor();
	}
}
