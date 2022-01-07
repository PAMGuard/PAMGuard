package qa.generator.location;

import qa.ClusterParameters;
import qa.QAControl;
import qa.generator.clusters.QACluster;

/**
 * Manage location options for the different clusters and tests. 
 * @author dg50
 *
 */
public class LocationManager {

	private QAControl qaControl;
	
	public static final Class[] locatorTypes = {SetDistanceGenerator.class, SmartLocationGenerator.class, RandomLocationGenerator.class};
	public static final String[] locatorNames = {"Stepped Distances", "Smart Distances", "Random Distances"};
	public static final String testName = "kjl";

	public LocationManager(QAControl qaControl) {
		this.qaControl = qaControl;
	}
	
	public QALocationGenerator makeLocationGenerator(String locGenName, QACluster cluster, int totalSequences, double[] rangeLimits) {
		ClusterParameters clusterParams = qaControl.getQaParameters().getClusterParameters(cluster);
		double defaultRange = clusterParams.monitorRange;
		defaultRange = Math.max(1, defaultRange);
		if (locGenName.equals(locatorNames[0])) {
			return SetDistanceGenerator.makeSteppedGenerator(qaControl, cluster, totalSequences, rangeLimits);
		}
		if (locGenName.equals(locatorNames[1])) {
			return new SmartLocationGenerator(qaControl, cluster, totalSequences, rangeLimits);
		}
		if (locGenName.equals(locatorNames[2])) {
			return new RandomLocationGenerator(qaControl, cluster, totalSequences, rangeLimits);
		}
		return null;
	}
	

}
