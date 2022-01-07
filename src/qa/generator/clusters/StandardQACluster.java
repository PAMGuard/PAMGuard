/**
 * 
 */
package qa.generator.clusters;

import qa.generator.QASoundGenerator;
import qa.generator.distributions.QADistribution;
import qa.generator.sequence.QASequenceGenerator;
import qa.generator.sounds.StandardSoundGenerator;

/**
 * @author dg50
 *
 */
public abstract class StandardQACluster implements QACluster {

	private String clusterName;

	private QASequenceGenerator qaSequenceGenerator;

	private QASoundGenerator qaSoundGenerator;

	private String version;

	/**
	 * @param clusterName name of the cluster
	 * @param qaSequenceGenerator sound sequence generator
	 * @param qaSoundGenerator sound generator
	 */
	public StandardQACluster(String clusterName, String version, 
			QASequenceGenerator qaSequenceGenerator, 
			StandardSoundGenerator qaSoundGenerator) {
		super();
		this.clusterName = clusterName;
		this.version = version;
		this.qaSequenceGenerator = qaSequenceGenerator;
		this.qaSoundGenerator = qaSoundGenerator;
	}

	/* (non-Javadoc)
	 * @see qa.generator.clusters.QACluster#getName()
	 */
	@Override
	public String getName() {
		return clusterName;
	}


	@Override
	public String getVersion() {
		return version;
	}

	public double[] getFrequencyRange() {
		return qaSoundGenerator.getFrequencyRange();
	}

	/**
	 * @param qaSequenceGenerator the qaSequenceGenerator to set
	 */
	public void setSequenceGenerator(QASequenceGenerator qaSequenceGenerator) {
		this.qaSequenceGenerator = qaSequenceGenerator;
	}

	/**
	 * @param qaSoundGenerator the qaSoundGenerator to set
	 */
	public void setSoundGenerator(QASoundGenerator qaSoundGenerator) {
		this.qaSoundGenerator = qaSoundGenerator;
	}

	/* (non-Javadoc)
	 * @see qa.generator.clusters.QACluster#getSoundGenerator()
	 */
	@Override
	public QASoundGenerator getSoundGenerator() {
		return qaSoundGenerator;
	}

	/* (non-Javadoc)
	 * @see qa.generator.clusters.QACluster#getSequenceGenerator()
	 */
	@Override
	public QASequenceGenerator getSequenceGenerator() {
		return qaSequenceGenerator;
	}

	@Override
	public QADistribution getDepthDistribution() {
		return null;
	}

	

}
