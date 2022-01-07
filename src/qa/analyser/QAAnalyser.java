package qa.analyser;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import Layout.PamAxis;
import PamController.PamControlledUnitSettings;
import PamController.PamFolderException;
import PamController.PamFolders;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamController.PamguardVersionInfo;
import PamUtils.FrequencyFormat;
import PamUtils.PamCalendar;
import PamView.PamColors;
import PamView.PamSymbolType;
import PamView.PamTable;
import PamView.chart.PamChartSeries;
import PamView.chart.PamChartSeriesException;
import PamView.chart.SeriesType;
import PamView.dialog.warn.WarnOnce;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamSimpleObserver;
import PamguardMVC.debug.Debug;
import Stats.LogRegWeka;
import generalDatabase.SQLLogging;
import generalDatabase.SuperDetLogging;
import generalDatabase.clauses.FixedClause;
import generalDatabase.clauses.UIDViewParameters;
import pamMaths.PamHistogram;
import pamMaths.PamLogHistogram;
import qa.ClusterParameters;
import qa.QAControl;
import qa.QADataProcess;
import qa.QAParameters;
import qa.QASequenceDataBlock;
import qa.QASequenceDataUnit;
import qa.QASoundDataUnit;
import qa.QATestDataUnit;
import qa.chart.QAPamChart;
import qa.chart.swing.QASwingChartRenderer;
import qa.generator.clusters.QACluster;
import qa.generator.sequence.QASequenceGenerator;
import qa.operations.QAOpsDataUnit;
import qa.resource.ReportTextBits;
import qa.swing.QAReportDialog;
import reportWriter.Report;
import reportWriter.ReportChart;
import reportWriter.ReportFactory;
import reportWriter.ReportSection;

/**
 * Used for loading in and analysing test data sets. 
 * Will have it's own set of datablocks and SQLLoggers which mirror those in the
 * generator so that data can be loaded entirely independently of what's going 
 * on in the generator / viewer. 
 * @author dg50
 *
 */
public class QAAnalyser extends QADataProcess implements PamSettings {

	private QAControl qaControl;

	private QueuedTaskDataBlock queuedTaskDataBlock;

	private TaskObserver taskObserver;

	private String reportVersion = "1.0";

	private QAReportOptions qaReportOptions = new QAReportOptions();

	public QAAnalyser(QAControl qaControl) {
		super(qaControl, false);
		this.qaControl = qaControl;
		getQaTestLogging().setSubTableClausePolicy(SuperDetLogging.SUBTABLECLAUSE_PARENTUID);
		getQaSequenceLogging().setSubTableClausePolicy(SuperDetLogging.SUBTABLECLAUSE_PARENTUID);
		queuedTaskDataBlock = new QueuedTaskDataBlock(this);
		taskObserver = new TaskObserver();
		queuedTaskDataBlock.addObserver(taskObserver, true);
		PamSettingManager.getInstance().registerSettings(this);
	}

	/**
	 * This is probably going to get called with a data unit from the
	 * generator datablock. If that is the case, then the local datablocks are
	 * cleared and the data unit and all it's sub sequences and sounds are loaded 
	 * into a local set of datablocks, separate from the main list. That way we're 
	 * able to play about with analysing data over a wide time period even when 
	 * data collection is ongoing ...
	 * @param testDataUnit
	 */
	public void analyseTest(QATestDataUnit testDataUnit) {
		QAReportOptions repOpts = QAReportDialog.showDialog(qaControl.getGuiFrame(), qaReportOptions);
		if (repOpts != null) { 
			qaReportOptions = repOpts;
			QAQueuedTask queuedTask = new QAQueuedTask(PamCalendar.getTimeInMillis(), repOpts.clone(), testDataUnit);
			queuedTaskDataBlock.addPamData(queuedTask);
		}

	}

	/**
	 * Call to process a whole load of tests, and if necessary, make a combined report with 
	 * results of many different tests. 
	 * @param testDataUnits List of test data units. 
	 */
	public void analyseTests(QATestDataUnit[] multiUnits) {
		analyseTests(Arrays.asList(multiUnits));
	}

	/**
	 * Call to process a whole load of tests, and if necessary, make a combined report with 
	 * results of many different tests. This will add the tests to a task list in a queue 
	 * and return immediately, so no need to worry about execution time. Will clone the list
	 * of data units so that the list object can be reused. 
	 * @param testDataUnits List of test data units. 
	 */
	public void analyseTests(List<QATestDataUnit> testDataUnits) {
		QAReportOptions repOpts = QAReportDialog.showDialog(qaControl.getGuiFrame(), qaReportOptions);
		if (repOpts != null) { 
			qaReportOptions = repOpts;
			ArrayList<QATestDataUnit> clonedList = new ArrayList<>();
			clonedList.addAll(testDataUnits);
			// that was only a 'shallow' clone. The main thing was to make sure the list wasn't overwritten or cleared. 
			QAQueuedTask queuedTask = new QAQueuedTask(PamCalendar.getTimeInMillis(), repOpts.clone(), clonedList);
			queuedTaskDataBlock.addPamData(queuedTask);
		}		
	}

	private class TaskObserver extends PamSimpleObserver {

		@Override
		public void addData(PamObservable observable, PamDataUnit pamDataUnit) {
			QAQueuedTask queudTask = (QAQueuedTask) pamDataUnit;
			processQueuedTask(queudTask);
		}

		@Override
		public String getObserverName() {
			return "QA Analysis Thread";
		}

	}

	/**
	 * this will get called back in a separate thread, using the queueing 
	 * and rethreading functionality of the PAMDataBlock
	 * @param queudTask
	 */
	public void processQueuedTask(QAQueuedTask queudTask) {
		/**
		 * Get the report options. Its just possible that these might be changed for multiple
		 * 
		 */
		QAReportOptions reportOptions = queudTask.getReportOptions();
		if (reportOptions == null) {
			// get the default (should be the same anyway)
			reportOptions = this.qaReportOptions;
		}
		if (reportOptions == null) {
			reportOptions = new QAReportOptions();
		}
		/*
		 *  should now be ready to write the report. Phew ! Will start with a general header, then a section for each 
		 *  sound type, with sub sections for single sounds and for sequence detections.  
		 */
		/**
		 * Make a report section title and header here, then 
		 * work through the species ...
		 */
		Report report = ReportFactory.createReport("PAMGuard Detection Efficiency report");
		
		ReportSection aSection = new ReportSection();
		aSection.addSectionText("Report Date: " + PamCalendar.formatDateTime(System.currentTimeMillis()));
		aSection.addSectionText("PAMGuard Version: " + PamguardVersionInfo.version + ", " + PamguardVersionInfo.date);
		aSection.addSectionText("Report Version: " + reportVersion);
		report.addSection(aSection);

		ReportSection preamble = new ReportSection("Introduction", 1);
		preamble.addSectionText(ReportTextBits.INTROTEXT);
		report.addSection(preamble);
		BufferedImage demoImage = ReportTextBits.loadImage("SampleReportDiagram.png");
		if (demoImage != null) {
			preamble.setImage(demoImage, "Example detection efficiency graph");
		}

		QASystemReport sysReport = new QASystemReport();
		report.addReport(sysReport.makeSystemSection(reportOptions));


		/**
		 * Possible that a task will have many different sound types in 
		 * it, so first break up by sound type ...
		 */
		ArrayList<QACluster> uniqueClusters = queudTask.getUniqueClusters();
		for (QACluster qaCluster : uniqueClusters) {
			ArrayList<QATestDataUnit> clusterTests = queudTask.getTestsForCluster(qaCluster);
			Report repSection = processClusterTests(qaCluster, clusterTests, reportOptions);
			report.addReport(repSection);
		}
		
		report.addReport(makeAcknowledgement());

		saveAndDisplay(report, "PAM Performance Report ", true);
	}

	private Report makeAcknowledgement() {
		Report report = new Report("");
		ReportSection section = new ReportSection("Acknowledgements", 1);
		section.addSectionText(ReportTextBits.weka1);
		BufferedImage wekIm = ReportTextBits.loadImage("WekaLogo.png");
		if (wekIm != null) {
			section.setImage(wekIm);
		}
		report.addSection(section);
		section = new ReportSection();
		section.addSectionText(ReportTextBits.weka2);
		report.addSection(section);
		return report;
	}

	/**
	 * Get a list of detectors in use with a given cluster. 
	 * @param qaCluster cluster
	 * @return List of used detectors. 
	 */
	private ArrayList<PamDataBlock> getClusterDetectors(QACluster qaCluster) {
		QAParameters params = qaControl.getQaParameters();
		ClusterParameters clusterParams = params.getClusterParameters(qaCluster);
		ArrayList<PamDataBlock> detectorList = qaControl.getQaMonitorProcess().getAllDetectors();
		ArrayList<PamDataBlock> usedDetectors = new ArrayList<>();
		for (PamDataBlock dataBlock : detectorList) {
			if (clusterParams.isSelectedDetector(dataBlock)) {
				usedDetectors.add(dataBlock);
			}
		}
		return usedDetectors;
	}

	/**
	 * 
	 * @param qaCluster Cluster being processed. 
	 * @param clusterTests a list of tests that should all be of the same cluster type. 
	 * @param reportOptions 
	 * @return 
	 */
	private Report processClusterTests(QACluster qaCluster, ArrayList<QATestDataUnit> clusterTests, QAReportOptions reportOptions) {

		QAParameters params = qaControl.getQaParameters();
		ClusterParameters clusterParams = params.getClusterParameters(qaCluster);

		ArrayList<PamDataBlock> usedDetectors = getClusterDetectors(qaCluster);

		Report report = ReportFactory.createReport("");
		/**
		 * Need to reload test data into separate data blocks. Will also want 
		 * to look at different aspects for each data unit, so just load the 
		 * all the results into a 2D array of result sets. 
		 */
		int nDets = usedDetectors.size();
		int nTests = clusterTests.size();
		if (nDets == 0) {
			Report clusterReport = makeClusterSummary(qaCluster, clusterParams, null, reportOptions);
			report.addReport(clusterReport);
			//			ReportSection section = new ReportSection();
			//			section.addSectionText("No detectors are available which can detect " + qaCluster.getName());
			//			report.addSection(section);
			return report;
		}
		if (nTests == 0) {
			return report; // nothing to do, should never happen, will probably crash before getting here. 
		}
		QATestResult[][] allReports = new QATestResult[nDets][nTests];

		for (int iTest = 0; iTest < nTests; iTest++) {
			/*
			 * Check the data unit - reload into the local datablock. 
			 * (particularly important during real time processing)
			 */
			QATestDataUnit reloadedTest = clusterTests.get(iTest);
			if (reloadedTest.getParentDataBlock() != this.getTestsDataBlock()) {
				reloadedTest = reloadTestData(reloadedTest);
			}
			/*
			 * For each detector, amalgamate all results from multiple tests. 
			 * i.e. loop over usedDetectors first, then get all data, then write a report
			 * sub-section. 
			 */
			for (int iDet = 0; iDet < usedDetectors.size(); iDet++) {
				QATestResult testReport = processTest(reloadedTest, usedDetectors.get(iDet));
				allReports[iDet][iTest] = testReport;
			}
		}
		/**
		 * Should now have a 2D array of all tests. Now aggregate these for each 
		 * detector into a one-D array. 
		 */
		QATestResult[] detectorReports = new QATestResult[nDets];
		for (int iDet = 0; iDet < nDets; iDet++) {
			try {
				detectorReports[iDet] = QATestResult.combineReports(allReports[iDet]);
			} catch (QAReportException e) {
				WarnOnce.showWarning(qaControl.getGuiFrame(), "QA Test Analysis", e.getMessage(), WarnOnce.CANCEL_OPTION);
				return null;
			}			
		}
		/**
		 * Now do the cluster report. Had to do here rather than earlier since
		 * information only gets loaded in reloadTestData(...)
		 */
		Report clusterReport = makeClusterSummary(qaCluster, clusterParams, allReports[0], reportOptions);
		report.addReport(clusterReport);
		double soundsPerSequence = getSoundsPerSequence(allReports[0]);
		/**
		 * We now have data for a single cluster for a list of detectors. 
		 * Now tell those reports to do their internal processing to make histograms and 
		 * regressions for both clusters and for single sounds. 
		 * 
		 * Work out sensible bins working in factors of sqrt2 from the nominal monitor range 
		 * until the min and max values of all ranges in the data are incorporated. All detectors
		 * are using the same data, so can take range limits from just the first one. 
		 */
		double monitorRange = clusterParams.monitorRange;
		double[] rangerange = detectorReports[0].getRangeRange();
		if (rangerange == null) {
			// there are no data, so will have to submit an empty report. 
			ReportSection sect = new ReportSection();
			sect.addSectionText("Warning: No data were generated for this test");
			report.addSection(sect);
			return report;
		}
		double[] binEdges = getHistoBinEdges(monitorRange, rangerange);
		boolean doSingleSounds = reportOptions.showSingleSoundResults || soundsPerSequence <= 1.;
		boolean doSequences = soundsPerSequence > 1.;
		for (int iDet = 0; iDet < nDets; iDet++) {
			if (doSequences) {
				detectorReports[iDet].setSequenceAnalysis(runSequenceAnalysys(detectorReports[iDet], clusterParams, binEdges));
			}
			if (doSingleSounds) {
				detectorReports[iDet].setSoundAnalysis(runSoundAnalysis(detectorReports[iDet], clusterParams, binEdges));
			}
		}

		report.addSection(getSoundSequenceText(qaCluster, clusterParams));
		
		if (doSingleSounds) {
			Report soundReport = makeEfficiencyReport(qaCluster, clusterParams, detectorReports, QATestResult.SOUND_ANAL_SET, reportOptions);
			report.addReport(soundReport);
		}
		if (doSequences) {
			Report seqReport = makeEfficiencyReport(qaCluster, clusterParams, detectorReports, QATestResult.SEQUENCE_ANAL_SET, reportOptions);
			report.addReport(seqReport);
		}

		return report;

	}

	/**
	 * Summary info about the cluster and which tests were included in the analysis
	 * @param qaCluster
	 * @param clusterParams
	 * @param allReports 
	 * @param reportOptions 
	 * @param qaTestResults
	 * @return
	 */
	private Report makeClusterSummary(QACluster qaCluster, ClusterParameters clusterParams, 
			QATestResult[] allReports, QAReportOptions reportOptions) {
		Report report = ReportFactory.createReport("Rep section");
		String detType = "";
		String secTit = String.format("%s Detection Efficiency", qaCluster.getName());
		ReportSection section = new ReportSection(secTit, 1);
		report.addSection(section);
		String version = qaCluster.getVersion();
		double[] fRange = qaCluster.getSoundGenerator().getFrequencyRange();
		String txt = String.format("Sound generator version: %s, Nominal frequency range %s", 
				version, FrequencyFormat.formatFrequencyRange(fRange,  true));
		section.addSectionText(txt);

		if (allReports == null) {
			section.addSectionText("No detectors are available which can detect " + qaCluster.getName());
			return report;
		}

		int nTest = allReports.length;
		long[] UIDs = new long[nTest];
		long minT = Long.MAX_VALUE, maxT = 0;
		QATestDataUnit tdu = null;
		QATestResult testResult;
		for (int i = 0;i < nTest; i++ ) {
			testResult = allReports[i];
			tdu = testResult.getTestDataUnit();
			UIDs[i] = tdu.getUID();
			minT = Math.min(minT, tdu.getTimeMilliseconds());
			maxT = Math.max(maxT, tdu.getEndTimeInMilliseconds());
		}
		if (nTest == 1) {
			section.addSectionText(String.format("Test UID %d: %s to %s", UIDs[0], 
					PamCalendar.formatDateTime(minT, true), PamCalendar.formatDateTime(maxT, true)));
			section.addSectionText("Operations Status: " + getOpsStatusString(tdu));
		}
		else {
			section.addSectionText(String.format("Tests UIDs %s; %s to %s", Arrays.toString(UIDs), 
					PamCalendar.formatDateTime(minT, true), PamCalendar.formatDateTime(maxT, true)));
			ArrayList<String> opsStrings = getAllOpsStatusStrings(allReports);
			if (opsStrings.size() == 0) {

			}
			else if (opsStrings.size() == 1) {
				section.addSectionText("Operations Status: " + opsStrings.get(0));
			}
			else {
				section.addSectionText("Variable operations status: " + Arrays.toString(opsStrings.toArray()));
			}
		}
		double sps = getSoundsPerSequence(allReports);
		QASequenceGenerator seqGen = qaCluster.getSequenceGenerator();
		String seqTxt = String.format("Sequence generator: %s, mean sounds per sequence %3.1f", seqGen.getName(), sps);
		section.addSectionText(seqTxt);

		return report;
	}

	/**
	 * Get the mean number of sounds per sequence. This will inform whether
	 * we bother with the cluster analysis, or just do the single sounds
	 * @param allReports Array list of cluster tests
	 * @return mean number of sounds per cluster. 
	 */
	private double getSoundsPerSequence(QATestResult[] allReports) {
		if (allReports == null || allReports.length == 0) {
			return 0;
		}
		double nSounds = 0;
		double nSeqs = 0;
		for (int j = 0; j < allReports.length; j++) {
			int[] soundsPerSeq = allReports[j].getSequenceSummary().getSeqSounds();
			nSeqs += soundsPerSeq.length;
			for (int i = 0; i < soundsPerSeq.length; i++) {
				nSounds += soundsPerSeq[i];
			}
		}
		if (nSeqs == 0 || nSounds == 0) {
			return 0;
		}
		return Math.max(nSounds/nSeqs, 1.);
	}

	private ArrayList<String> getAllOpsStatusStrings(QATestResult[] allReports) {
		ArrayList<String> opsStrings = new ArrayList<>();
		for (int i = 0; i < allReports.length; i++) {
			String op = getOpsStatusString(allReports[i].getTestDataUnit());
			if (opsStrings.contains(op) == false) {
				opsStrings.add(op);
			}
		}
		return opsStrings;
	}

	private String getOpsStatusString(QATestDataUnit qaTestDataUnit) {
		if (qaTestDataUnit == null) {
			return "No data";
		}
		QAOpsDataUnit ops = qaTestDataUnit.getQaOpsDataUnit();
		if (ops == null) {
			return "Unknown";
		}
		else {
			return ops.getOpsStatusName();
		}
	}
	
	private String getDetectionTypeString(int soundAnalSet) {
		String detType = "";
		if (soundAnalSet == QATestResult.SEQUENCE_ANAL_SET) {
			detType = "Sound Sequences";
		}
		else if (soundAnalSet == QATestResult.SOUND_ANAL_SET) {
			detType = "Single Sounds";
		}
		return detType;
	}

	private Report makeEfficiencyReport(QACluster cluster, ClusterParameters clusterParams, QATestResult[] detectorReports, 
			int soundAnalSet, QAReportOptions reportOptions) {
		Report report = ReportFactory.createReport("Rep section");
		String detType = getDetectionTypeString(soundAnalSet);
		if (soundAnalSet == QATestResult.SEQUENCE_ANAL_SET) {
			report.addSection(getSequencyEfficiencyText(cluster, clusterParams));
		}
		else if (soundAnalSet == QATestResult.SOUND_ANAL_SET) {
			report.addSection(getSoundEfficiencyText(cluster, clusterParams));
		}
		// need to work out which, if any of the detectorReports is the primary detector for this cluster. 
		int[] detectorOrder = new int[detectorReports.length];
		int prefDet = -1;
		for (int i = 0; i < detectorReports.length; i++) {
			if (detectorReports[i].getTestDetector().getLongDataName().equalsIgnoreCase(clusterParams.primaryDetectionBlock)) {
				prefDet = i;
				detectorOrder[0] = i; // so the first reported will be this one. 
				// and swap it with the one that this is not ...
				detectorOrder[i] = 0;
			}
			else {
				detectorOrder[i] = i;
			}
		}
		QATestResult[] orderedReports = new QATestResult[detectorReports.length];
		for (int i = 0; i < detectorReports.length; i++) {
			orderedReports[i] = detectorReports[detectorOrder[i]];
		}
		

		// axes will be the same whether there is one or many graphs (I hope). 
//		double maxRange = detectorReports[0].getTestAnalysis(soundAnalSet).getHitHistogram().getMaxVal();
//		double minRange = detectorReports[0].getTestAnalysis(soundAnalSet).getHitHistogram().getMinVal();
		double[] histEdges = detectorReports[0].getTestAnalysis(soundAnalSet).getMissHistogram().getBinEdgeValues();
		double minRange = histEdges[0];
		double maxRange = histEdges[histEdges.length-1];

		/*
		 * Make the efficiency table. 
		 */
		PamTable pamTable = makeEfficiencyTable(cluster, histEdges, clusterParams, orderedReports, soundAnalSet, reportOptions);
		ReportSection tableSection = new ReportSection();
		String tableTit = String.format("Detection efficiency for %s %s", cluster.getName(), detType.toLowerCase());
		tableSection.setTable(pamTable, tableTit);
		report.addSection(tableSection);

		QAPamChart qaPamChart;
		if (qaReportOptions.showIndividualDetectors == false) {
			// reorder the reports so that the primary detector is first. 
			ReportSection[] chartSections = makeEfficiencyGraph(cluster, histEdges, clusterParams, orderedReports, soundAnalSet, reportOptions);
			report.addSections(chartSections);
		}
		else {
			for (int i = 0; i < orderedReports.length; i++) {
				QATestResult[] selReports = Arrays.copyOfRange(orderedReports, i, i+1);
				ReportSection[] chartSections = makeEfficiencyGraph(cluster, histEdges, clusterParams, selReports, soundAnalSet, reportOptions);
				report.addSections(chartSections);
			}
		}
//		report.addSection(section);
//		report.addSection(regFailures);// may be empty, but that shouldn't matter
		return report;
	}

	/**
	 * Make a report section containing an efficiency graph + appropriate diagnostic text if regressions fail. 
	 * @param cluster
	 * @param clusterParams
	 * @param detectorReports
	 * @param soundAnalSet
	 * @param reportOptions
	 * @return report section with a graph and a report section with regression failures. 
	 */
	private ReportSection[] makeEfficiencyGraph(QACluster cluster, double[] histEdges, ClusterParameters clusterParams, QATestResult[] detectorReports, 
			int soundAnalSet, QAReportOptions reportOptions) {
		double minRange = histEdges[0];
		double maxRange = histEdges[histEdges.length-1];
		PamAxis xAxis = new PamAxis(0, 0, 0, 0, 0, maxRange, PamAxis.BELOW_RIGHT, "Range (m)", PamAxis.LABEL_NEAR_CENTRE, "%d");
		PamAxis yAxis = new PamAxis(0,0,0,0,0,1.0,PamAxis.ABOVE_LEFT,"Probability",PamAxis.LABEL_NEAR_CENTRE, "%3.2f");
		yAxis.setInterval(.25);

		String detType = getDetectionTypeString(soundAnalSet);

		QAPamChart qaPamChart = new QAPamChart(xAxis, yAxis);
			PamChartSeries series;
			String tit = String.format("%s", detectorReports[0].getTestDetector().getLongDataName());
			series = qaPamChart.addMissHistogram(detectorReports[0].getTestAnalysis(soundAnalSet).getMissHistogram(), tit);
			series.setSymbolData(new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 1, 1, true, Color.LIGHT_GRAY, getSeriesColor(0)));
			//			tit = String.format("Detected (%s)", detectorReports[prefDet].getTestDetector().getLongDataName());
			series = qaPamChart.addHitHistogram(detectorReports[0].getTestAnalysis(soundAnalSet).getHitHistogram(), null);
			series.setSymbolData(new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 1, 1, true, Color.LIGHT_GRAY, getSeriesColor(0)));
		/*
		 * Now add efficiency lines for all detectors. 
		 */
		/*
		 * Will calculate 50 efficiency values over the range of ranges.
		 */
		double rStep = (maxRange-minRange)/50;
		double[] tstRange = new double[51];
		double[] tstInp = new double[1];
		for (int i = 0; i < tstRange.length; i++) {
			tstRange[i] = minRange + rStep*i;
		}
		ReportSection regFailures = new ReportSection();
		int nGoodRegressions = 0;
		int nBadRegressions = 0;
		for (int i = 0; i < detectorReports.length; i++) {
			QATestAnalysis analysis = detectorReports[i].getTestAnalysis(soundAnalSet);
			LogRegWeka lrw = analysis.getLogRegWeka();
			boolean lrOk = (lrw != null && lrw.getCoefficients() != null);
//			System.out.printf("Report %d %s, for %s is OK %s\n", i, detType,
//					detectorReports[i].getTestDetector().getLongDataName(), Boolean.valueOf(lrOk).toString());
			if (lrOk) {
				double[][] coeffs = lrw.getCoefficients();
				if (coeffs[0][0] > 0) {
					lrOk = false;
				}
				else {
					double[] prob = new double[tstRange.length];
					for (int j = 0; j < prob.length; j++) {
						tstInp[0] = tstRange[j];
						prob[j] = lrw.getPrediction(tstInp); 
					}
					PamChartSeries chartSeries;
					try {
						chartSeries = new PamChartSeries(SeriesType.LINE, detectorReports[i].getTestDetector().getLongDataName(), tstRange, prob);
						chartSeries.setLineColor(getSeriesColor(i));
					} catch (PamChartSeriesException e) {
						e.printStackTrace(); // should never happen
						continue;
					}
					qaPamChart.addSeries(chartSeries);
					nGoodRegressions++;
				}
			}
			if (!lrOk) {
				String lst = ReportTextBits.getRegressionFailureText(detType, detectorReports[i].getTestDetector().getLongDataName(), analysis);
//				System.out.println(lst);
				regFailures.addSectionText(lst);
				nBadRegressions++;
			}
		}
		String preamble = null;
		String sectionTitle;
		if (detectorReports.length > 1) {
			preamble = String.format("Detection efficiency using multiple detectors. " +
					"Histograms of 'hits' and 'misses' are shown only for the %s detector, regression lines are shown for %d detection method(s)"
					+ " that produced a valid Logistic regression result.",
					detectorReports[0].getTestDetector().getLongDataName(), nGoodRegressions);
			sectionTitle = "Detection efficiency for multiple detectors";
		}
		else {
			preamble = String.format("Detection efficiency using a single detection method: %s", 
					detectorReports[0].getTestDetector().getLongDataName());
			sectionTitle = String.format("%s", detectorReports[0].getTestDetector().getLongDataName());
		}

		ReportSection section = new ReportSection(sectionTitle, 3);
		section.addSectionText(preamble);
		QASwingChartRenderer chartRenderer = new QASwingChartRenderer(qaPamChart);
		tit = String.format("Detection efficiency for %s %s", cluster.getName(), detType.toLowerCase());
		
		section.setImage(chartRenderer.renderBufferedImage(600, 400), tit, true);

		ReportSection[] sections = new ReportSection[2];
		sections[0] = section;
		sections[1] = regFailures;
		return sections;
	}

	/**
	 * Make the efficiency table for all detectors. 
	 * @param cluster
	 * @param histEdges
	 * @param clusterParams
	 * @param detectorReports
	 * @param soundAnalSet
	 * @param reportOptions
	 * @return Efficiency table. 
	 */
	private PamTable makeEfficiencyTable(QACluster cluster, double[] histEdges, ClusterParameters clusterParams, QATestResult[] detectorReports, 
			int soundAnalSet, QAReportOptions reportOptions) {
		/*
		 * Make the results table for all detectors. 
		 */
		String[][] tableData = new String[histEdges.length-1][detectorReports.length+2];
		String[] colNames = new String[detectorReports.length+2];
		colNames[0] = "Range";
		colNames[1] = "n";
		for (int i = 0; i < histEdges.length-1; i++) {
			tableData[i][0] = String.format("%.0f-%.0fm", histEdges[i], histEdges[i+1]);
		}
		for (int i = 0; i < detectorReports.length; i++) {
			colNames[i+2] = detectorReports[i].getTestDetector().getLongDataName(); 
			PamHistogram hitHist = detectorReports[i].getTestAnalysis(soundAnalSet).getHitHistogram();
			PamHistogram missHist = detectorReports[i].getTestAnalysis(soundAnalSet).getMissHistogram();
			if (hitHist == null || missHist == null) {
				continue;
			}
			double[] hitData = hitHist.getData();
			double[] missData = missHist.getData();
			int nB = Math.min(histEdges.length-1, hitData.length);
			// now fill in the hist values, wherever there is data, leave null if 0,0
			for (int j = 0; j < nB; j++) {
				double m = missData[j];
				double h = hitData[j];
				if (m+h == 0) {
					tableData[j][i+2] = "-";
				}
				else {
					tableData[j][i+2] = String.format("%3.1f%%", h/(m+h)*100.);
				}
				if (i == 0) {
					// write the number of trials in column 1.
					tableData[j][1] = String.format("%d", (int) (m+h));
				}
			}
		}
		PamTable pamTable = new PamTable(tableData, colNames);
		return pamTable;
	}

	private Color getSeriesColor(int iDet) {
		return PamColors.getInstance().getWhaleColor(iDet+1);
	}
	
	private ReportSection getSoundSequenceText(QACluster cluster, ClusterParameters clusterParams) {
		ReportSection section = new ReportSection();
		if (cluster.getSequenceGenerator().getnSounds() == 1) {
			String txt = String.format(ReportTextBits.SINGLESOUNDSEQUNCETXT, cluster.getName());
			section.addSectionText(txt);
		}
		else {
			String txt = String.format(ReportTextBits.SOUNDSEQUNCETXT, cluster.getName(), cluster.getSequenceGenerator().getnSounds());
			section.addSectionText(txt);
		}
		return section;
	}

	private ReportSection getSequencyEfficiencyText(QACluster cluster, ClusterParameters clusterParams) {
		String tit = String.format("Detection efficiency for %s sound sequences", cluster.getName());
		ReportSection section = new ReportSection(tit, 2);
//		String txt = String.format(ReportTextBits.SOUNDSEQUNCETXT, cluster.getName(), cluster.getSequenceGenerator().getnSounds());
//		section.addSectionText(txt);
		return section;
	}

	private ReportSection getSoundEfficiencyText(QACluster cluster, ClusterParameters clusterParams) {
		String tit = String.format("Detection efficiency for single %s sounds", cluster.getName());
		ReportSection section = new ReportSection(tit, 2);
//		if (cluster.getSequenceGenerator().getnSounds() == 1) {
//			String txt = String.format(ReportTextBits.SINGLESOUNDSEQUNCETXT, cluster.getName());
//			section.addSectionText(txt);
//		}
//		else {
//			String txt = String.format(ReportTextBits.SINGLESOUNDTXT, cluster.getName(), cluster.getSequenceGenerator().getnSounds());
//			section.addSectionText(txt);
//		}
		return section;
	}

//	@Deprecated
//	private Report writeReport(QATestDataUnit reloadedTest, ArrayList<QATestResult> testReports) {
//
//		//		and a really simple Report
//
//		Report myReport = ReportFactory.createReport("Detection Efficiency report");
//		String sectionName = String.format("%s Test UID %d" , reloadedTest.getQaTestSet().getTestName(), reloadedTest.getUID());
//		//				PamCalendar.formatDateTime(reloadedTest.getTimeMilliseconds()));
//		ReportSection aSection = new ReportSection(sectionName);
//		aSection.addSectionText("Test Date: " + PamCalendar.formatDateTime(reloadedTest.getTimeMilliseconds()));
//		myReport.addSection(aSection);
//
//		QASystemReport sysReport = new QASystemReport();
//		//		myReport.addSection(sysReport.makeSystemSection(null));
//
//		aSection = new ReportSection("Detection Efficiency");
//		String detectors = "Detectors Used: ";
//		ReportChart soundEffyChart = null;
//		ReportChart seqEffyChart = null;
//		if (testReports == null) {
//			detectors += "none";
//		}
//		else {
//			int iRep = 0;
//			soundEffyChart = new ReportChart("Single Sound Efficiency");
//			soundEffyChart.setAxisTitles("Range (m)", "Efficiency");
//			seqEffyChart = new ReportChart("Sequence Detection Efficiency");
//			seqEffyChart.setAxisTitles("Range (m)", "Efficiency");
//			for (QATestResult testReport:testReports) {
//				SequenceSummary seqSummary = testReport.getSequenceSummary();
//				double[] ranges = seqSummary.getSeqRanges();
//				double[] effy = seqSummary.getDetectionRate();
//				soundEffyChart.addSeries(testReport.getTestDetector().getLongDataName(), ranges, effy);
//				seqEffyChart.addSeries(testReport.getTestDetector().getLongDataName(), ranges, toDouble(seqSummary.getCountGtThan(1)));
//
//				detectors += testReport.getTestDetector().getLongDataName();
//				if (iRep < testReports.size()-1) {
//					detectors += "; ";
//				}
//			}
//		}
//		aSection.addSectionText(detectors);
//
//
//		if (soundEffyChart != null) {
//			aSection.setImage(soundEffyChart.getImage(),null,true);
//			aSection.addSectionText("Figure 1. Detection efficiency");
//		}
//		if (seqEffyChart != null) {
//			aSection  = new ReportSection();
//			aSection.addSectionText("Figure 2. Sequence Detection Efficiency");
//			aSection.setImage(seqEffyChart.getImage());
//			myReport.addSection(aSection);
//		}
//
//		/*
//		 * Now do logistic regression where possible. 
//		 */
//		int iRep = 0;
//		//		reportChart = new ReportChart("Single Sound Efficiency");
//		//		reportChart.setAxisTitles("Range (m)", "Efficiency");
//		for (QATestResult testReport:testReports) {
//			SequenceSummary seqSummary = testReport.getSequenceSummary();
//			//			double[] ranges = seqSummary.getSeqRanges();
//			//			double[] effy = seqSummary.getDetectionRate();
//			//			reportChart.addSeries(testReport.getTestDetector().getLongDataName(), ranges, effy);
//			//			
//			//			detectors += testReport.getTestDetector().getLongDataName();
//			//			if (iRep < testReports.size()-1) {
//			//				detectors += "; ";
//			//			}
//			PamHistogram hitHist = new PamHistogram(0, 2000, 10);
//			PamHistogram missHist = new PamHistogram(0, 2000, 10);
//			double[] ranges = seqSummary.getSeqRanges();
//			int[] hits = seqSummary.getSeqHits();
//			int[] misses = seqSummary.getMisses();
//			for (int i = 0; i < ranges.length; i++) {
//				hitHist.addData(ranges[i], hits[i]);
//				missHist.addData(ranges[i], misses[i]);
//			}
//
//			//			QAChart lrChart = new QAChart(testReport.getTestDetector().getLongDataName(), toInteger(hitHist.getBinCentreValues()), 
//			//					toInteger(hitHist.getData()), toInteger(missHist.getData()));
//			////			lrChart.getPrimaryChart().
//			//			lrChart.setAxisTitles("Range (m)", "Probability");
//			PamAxis xAxis = new PamAxis(0, 0, 0, 0, 0, 2000, PamAxis.BELOW_RIGHT, "Range (m)", PamAxis.LABEL_NEAR_CENTRE, "%d");
//			PamAxis yAxis = new PamAxis(0,0,0,0,0,1.0,PamAxis.ABOVE_LEFT,"Probability",PamAxis.LABEL_NEAR_CENTRE, "%3.2f");
//			yAxis.setInterval(.25);
//			QAPamChart qaPamChart = new QAPamChart(xAxis, yAxis);
//			qaPamChart.addMissHistogram(missHist);
//			qaPamChart.addHitHistogram(hitHist);
//
//			aSection = new ReportSection("Logistic regression for " + testReport.getTestDetector().getLongDataName());
//			LogRegWeka lrw = new LogRegWeka();
//			boolean regOk = lrw.setTrainingData(seqSummary.getSeqRanges(), seqSummary.getSeqHits());
//			if (regOk == false) {
//				aSection.addSectionText("Regression failed: " + lrw.getModelError());
//			}
//			else {
//				/**
//				 * Calculate a more fine scale regression curve to overlay on 
//				 * the plot. 
//				 * This will cover the hist range - need to be a bit cleverer about that
//				 * but not right now. 
//				 */
//				double minVal = 0;
//				double maxVal = 2000;
//				int nP = 100;
//				double step = (maxVal-minVal)/(nP-1);
//				double[] rangeDat = new double[nP];
//				double[] probDat = new double[nP];
//				double[] pRang = new double[1];
//				for (int i = 0; i < nP; i++) {
//					rangeDat[i] = minVal + step*i;
//					pRang[0] = rangeDat[i];
//					probDat[i] = lrw.getPrediction(pRang);
//				}
//				try {
//					PamChartSeries pSeries = new PamChartSeries(SeriesType.LINE, "", rangeDat, probDat);
//					pSeries.setLineColor(Color.RED);
//					pSeries.setLineWidth(3);
//					qaPamChart.addSeries(pSeries);
//				} catch (PamChartSeriesException e) {
//					e.printStackTrace();
//				}
//				//				lrChart.addSeries("", rangeDat, probDat);
//			}
//			double[][] singleSoundDat = seqSummary.getSingleSoundData();
//			LogRegWeka lrw2 = new LogRegWeka();
//			boolean reg2OK = lrw2.setTrainingData(singleSoundDat[0], singleSoundDat[1]);
//			if (reg2OK) {
//				double[][] coeffs = lrw2.getCoefficients();
//				for (int i = 0; i < coeffs.length; i++) {
//					double[] coef = coeffs[i];
//					Debug.out.println("Coeffs " + i + ": " + Arrays.toString(coef));
//				}
//				double minVal = 0;
//				double maxVal = 2000;
//				int nP = 100;
//				double step = (maxVal-minVal)/(nP-1);
//				double[] rangeDat = new double[nP];
//				double[] probDat = new double[nP];
//				double[] pRang = new double[1];
//				for (int i = 0; i < nP; i++) {
//					rangeDat[i] = minVal + step*i;
//					pRang[0] = rangeDat[i];
//					probDat[i] = lrw2.getPrediction(pRang);
//				}
//				try {
//					PamChartSeries pSeries = new PamChartSeries(SeriesType.SCATTER, "", rangeDat, probDat);
//					pSeries.setSymbolData(new SymbolData(PamSymbolType.SYMBOL_CIRCLE, 6, 6, true, Color.GREEN, Color.GREEN));
//					pSeries.setLineColor(Color.GREEN);
//					pSeries.setLineWidth(3);
//					qaPamChart.addSeries(pSeries);
//				} catch (PamChartSeriesException e) {
//					e.printStackTrace();
//				}
//			}
//
//			//			aSection.setImage(lrChart.getImage());
//			QASwingChartRenderer chartRenderer = new QASwingChartRenderer(qaPamChart);
//			aSection.setImage(chartRenderer.renderBufferedImage(600, 400));
//			myReport.addSection(aSection);
//			//			double[][] coeffs = lrw.getCoefficients();
//			//			Double range = lrw.getAttFromProb(0.5);
//		}
//
//
//
//		return myReport;
//
//
//	}

	/**
	 * Run regression analysis and make histograms of single sound data. 
	 * @param qaTestResult
	 * @param clusterParams
	 * @param binEdges
	 * @return
	 */
	private QATestAnalysis runSequenceAnalysys(QATestResult qaTestResult, ClusterParameters clusterParams, double[] binEdges) {
		/**
		 * Get an array of data for single sounds, second array is 1 or 0 (hit or miss)
		 */
		double[][] hitData =  qaTestResult.getSequenceSummary().getSequenceHitData(1);
		return runTestAnalysis(hitData, binEdges);
	}

	private QATestAnalysis runSoundAnalysis(QATestResult qaTestResult, ClusterParameters clusterParams, double[] binEdges) {
		/**
		 * Get an array of data for single sounds, second array is 1 or 0 (hit or miss)
		 */
		double[][] hitData =  qaTestResult.getSequenceSummary().getSingleSoundData();
		return runTestAnalysis(hitData, binEdges);
	}

	private QATestAnalysis runTestAnalysis(double[][] hitData, double[] binEdges) {
		int nBins = binEdges.length-1;
		PamLogHistogram hitHist = new PamLogHistogram(binEdges[0], binEdges[nBins], nBins);
		PamLogHistogram missHist = new PamLogHistogram(binEdges[0], binEdges[nBins], nBins);
		double[] ranges = hitData[0];
		double[] hit = hitData[1];
		double meanValue = 0;
		for (int i = 0; i < ranges.length; i++) {
			if (hit[i] > 0) {
				hitHist.addData(ranges[i]);
			}
			else {
				missHist.addData(ranges[i]);
			}
			meanValue += hit[i];
		}
		meanValue /= ranges.length; // might want this - tells us if the fit fails due to all being 1 or 0. 
		/**
		 * Now try to do a logistic regression.
		 */
		LogRegWeka lrw = new LogRegWeka();
		boolean regOk = lrw.setTrainingData(ranges, hit);
		if (regOk) {

		}
		return new QATestAnalysis(hitHist, missHist, meanValue, lrw);		
	}

	/**
	 * Get histogram bin edges covering the range limits and 
	 * making a bin edge right on the monitorValue. Current policy is a 
	 * logarithmic range scale, with a bin edge ratio of 1/sqrt(2);
	 * @param monitorRange
	 * @param rangelimits
	 * @return
	 */
	private double[] getHistoBinEdges(double monitorRange, double[] rangelimits) {
		int nUp = (int) Math.ceil(Math.log(rangelimits[1]/monitorRange)/Math.log(2.)*2);
		int nD = (int) Math.ceil(Math.log(monitorRange/rangelimits[0])/Math.log(2.)*2);
		int nB = nUp+nD+1;
		double rMin = monitorRange*Math.pow(2, -(double)nD/2.);
		double rMax = monitorRange*Math.pow(2, (double)nUp/2.);
		double[] edges = new double[nB];
		edges[0] = rMin;
		edges[nB-1] = rMax;
		double r = Math.sqrt(2);
		for (int i = 1; i < nB-1; i++) {
			edges[i] = edges[i-1]*r;
		}
		return edges;
	}

	private void saveAndDisplay(Report report, String reportName, boolean display) {
		String filePath = qaControl.getQaParameters().getReportOutputFolder();
		try {
			PamFolders.checkFolder(filePath, true);
		} catch (PamFolderException e) {
			WarnOnce.showWarning(qaControl.getGuiFrame(), "Report output folder", e.getMessage(), WarnOnce.CANCEL_OPTION);
			return;
		}
		String fileName = filePath + "/" +  PamCalendar.createFileName(System.currentTimeMillis(), reportName, ".docx"); 
		Debug.out.println("Writing report to " + fileName);
		ReportFactory.convertReportToDocx(report, fileName);
		ReportFactory.openReportInWordProcessor(fileName);
		//		PamReportViewer.viewWordDocument(fileName);
	}

	/**
	 * convert an array of integers to double precision
	 * @param intArray array of integers
	 * @return array of doubles
	 */
	private double[] toDouble(int[] intArray) {
		if (intArray == null) {
			return null;
		}
		double[] dArray = new double[intArray.length];
		for (int i = 0; i < dArray.length; i++) {
			dArray[i] = intArray[i];
		}
		return dArray;
	}

	/**
	 * convert an array of doubles to an array of integer values
	 * @param doubleArray
	 * @return integer array
	 */
	private int[] toInteger(double[] doubleArray) {
		if (doubleArray == null) {
			return null;
		}
		int[] intArray = new int[doubleArray.length];
		for (int i = 0; i < intArray.length; i++) {
			intArray[i] = (int) doubleArray[i];
		}
		return intArray;
	}

	public QATestResult processTest(QATestDataUnit testDataUnit, PamDataBlock aDetector) {
		Debug.out.printf("\n\nLoading and analysing event %d - %s - %s with %d sequences with detector %s\n", 
				testDataUnit.getUID(), testDataUnit.getQaTestSet().getTestName(),
				PamCalendar.formatTodaysTime(testDataUnit.getTimeMilliseconds()),
				testDataUnit.getSubDetectionsCount(), aDetector.getLongDataName());

		QATestResult testReport  = new QATestResult(testDataUnit, aDetector);

		int nSequences = testDataUnit.getSubDetectionsCount();
		double[] rangeData = new double[nSequences];
		int[] soundCount =  new int[nSequences];
		int[] detCount =  new int[nSequences];
		for (int i = 0; i < nSequences; i++) {
			QASequenceDataUnit seqDataUnit = testDataUnit.getSubDetection(i);
			QASequenceDataBlock seqDataBlock = (QASequenceDataBlock) seqDataUnit.getParentDataBlock();
			int nSubSub = 0;
			if (seqDataBlock.getSubtableData() != null) {
				nSubSub = seqDataBlock.getSubtableData().size();
			}
			//			System.out.printf("Sequence %d with %d sounds of %d\n", i, 
			//					seqDataUnit.getSubDetectionsCount(), nSubSub);
			soundCount[i] = seqDataUnit.getSubDetectionsCount();
			for (int s = 0; s < soundCount[i]; s++) {
				QASoundDataUnit soundDataUnit = seqDataUnit.getSubDetection(s);
				if (soundDataUnit == null) {
					continue;
				}
				if (soundDataUnit.getDetectorHit(aDetector) != 0) {
					detCount[i] ++;
				}
			}
			rangeData[i] = seqDataUnit.getDistanceToHydrophone();	
		}
		testReport.setSequenceSummary(new SequenceSummary(rangeData, soundCount, detCount));

		/*
		 * now make a report with a chart in it ...
		 */


		return testReport;




	}

	/*
	 * This is the clever bit - so that we can do all the analysis online while PAMGuard is still 
	 * running, we're going to try to reload the data into new structures. Will 
	 * this work or be a total disaster ? 
	 */
	private QATestDataUnit reloadTestData(QATestDataUnit testDataUnit) {
		getTestsDataBlock().clearAll();
		getSequenceDataBlock().clearAll();
		getSoundsDataBlock().clearAll();

		FixedClause clause = new FixedClause(String.format("WHERE UID=%d", testDataUnit.getUID()));
		boolean load = getQaTestLogging().loadViewData(clause, null);
		UIDViewParameters seqClause = UIDViewParameters.createUIDViewParameters(getTestsDataBlock());
		boolean loadSeq = getQaSequenceLogging().loadViewData(seqClause, null);
		getSequenceDataBlock().sortData();
		UIDViewParameters soundClause = UIDViewParameters.createUIDViewParameters(getSequenceDataBlock());
		boolean loadSounds = getQaLogging().loadViewData(soundClause, null);
		getSoundsDataBlock().sortData();

		System.out.printf("Numbers of test, seq and sound data = %d, %d, %d\n", 
				getTestsDataBlock().getUnitsCount(),getSequenceDataBlock().getUnitsCount(),
				getSoundsDataBlock().getUnitsCount());

		getTestsDataBlock().reattachSubdetections(null);
		getSequenceDataBlock().reattachSubdetections(null);
		//		getSoundsDataBlock().reattachSubdetections(null);

		QATestDataUnit testUnit = getTestsDataBlock().getFirstUnit();
		return testUnit;
	}

	@Override
	public void pamStart() { }

	@Override
	public void pamStop() {	}


	/**
	 * 
	 * @param qaCluster Sound type
	 * @param dataBlock detector output
	 * @return true if the cluster has a hope of being detected by the given detector output
	 */
	public boolean canDetectCluster(QACluster qaCluster, PamDataBlock dataBlock) {
		return (checkMatchFrequency(qaCluster, dataBlock) & checkMatchDuration(qaCluster, dataBlock));
	}

	private boolean checkMatchDuration(QACluster qaCluster, PamDataBlock dataBlock) {
		if (qaCluster == null || dataBlock == null) {
			return false;
		}
		double[] clusterDuration = qaCluster.getSoundGenerator().getDurationRange();
		double[] detectorDuration = dataBlock.getDurationRange();
		if (detectorDuration == null) {
			return true;
		}
		if (clusterDuration[0] > detectorDuration[1] || clusterDuration[1] < detectorDuration[0]) {
			return false;
		}
		else {
			return true;
		}
	}

	private boolean checkMatchFrequency(QACluster qaCluster, PamDataBlock dataBlock) {
		if (qaCluster == null || dataBlock == null) {
			return false;
		}
		double[] clusterFreq = qaCluster.getSoundGenerator().getFrequencyRange();
		double[] detectorFreq = dataBlock.getFrequencyRange();
		if (detectorFreq == null) {
			return true; // may be the case for things heard and for spectrgram annotatoins
		}
		if (detectorFreq[0] == 0 && detectorFreq[1] == 0) {
			return true;
		}
		// return false if no overlap
		if (clusterFreq[1] < detectorFreq[0] || clusterFreq[0] > detectorFreq[1]) {
			return false;
		}
		else {
			return true;
		}
	}

	@Override
	public String getUnitName() {
		return qaControl.getUnitName();
	}

	@Override
	public String getUnitType() {
		return "SIDE Analyser";
	}

	@Override
	public Serializable getSettingsReference() {
		return qaReportOptions;
	}

	@Override
	public long getSettingsVersion() {
		return QAReportOptions.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		qaReportOptions = (QAReportOptions) pamControlledUnitSettings.getSettings();
		return true;
	}


}
