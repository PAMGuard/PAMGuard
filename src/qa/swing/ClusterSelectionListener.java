package qa.swing;

import qa.generator.clusters.QACluster;

public interface ClusterSelectionListener {

	public void clusterSelected(QACluster qaCluster, int column, boolean selected);

}
