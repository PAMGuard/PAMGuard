package qa.analyser;

import java.util.ArrayList;
import java.util.List;

import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;
import qa.QATestDataUnit;
import qa.generator.clusters.QACluster;

/**
 * Use a re-threaded data unit list to manage analysis tasks. 
 * @author dg50
 *
 */
public class QAQueuedTask extends PamDataUnit {

	private List<QATestDataUnit> qaTests;
	
	private QAReportOptions reportOptions;

	/**
	 * @param timeMilliseconds Current time of analysis
	 * @param qaReportOptions 
	 */
	public QAQueuedTask(long timeMilliseconds, QAReportOptions qaReportOptions, QATestDataUnit singleTest) {
		super(timeMilliseconds);
		this.reportOptions = qaReportOptions;
		qaTests = new ArrayList<QATestDataUnit>();
		qaTests.add(singleTest);
	}

	public QAQueuedTask(long timeMilliseconds, QAReportOptions qaReportOptions, List<QATestDataUnit> multipleTests) {
		super(timeMilliseconds);
		this.reportOptions = qaReportOptions;
		this.qaTests = multipleTests;
	}
	
	/**
	 * @return the number of tasks in this job,
	 */
	public int getNumTests() {
		if (qaTests == null) {
			return 0;
		}
		else {
			return qaTests.size();
		}
	}
	
	/**
	 * Get a list of unique clusters represented in these data
	 * @return 
	 */
	public ArrayList<QACluster> getUniqueClusters() {
		ArrayList<QACluster> uniqueClusters = new ArrayList<>();
		for (QATestDataUnit qaTest : qaTests) {
			QACluster testCluster = qaTest.getQaTestSet().getQaCluster();
			if (uniqueClusters.contains(testCluster)) {
				continue;
			}
			uniqueClusters.add(testCluster);
		}
		return uniqueClusters;
	}

	/**
	 * Get the tests associated with a particular cluster. 
	 * @param cluster cluster to search for
	 * @return array list of tests of that cluster. 
	 */
	public ArrayList<QATestDataUnit> getTestsForCluster(QACluster cluster) {
		ArrayList<QATestDataUnit> clusterTests = new ArrayList<>();
		for (QATestDataUnit qaTest : qaTests) {
			QACluster testCluster = qaTest.getQaTestSet().getQaCluster();
			if (testCluster == cluster) {
				clusterTests.add(qaTest);
			}
		}
		return clusterTests;
	}

	/**
	 * @return the reportOptions
	 */
	public QAReportOptions getReportOptions() {
		return reportOptions;
	}
}
