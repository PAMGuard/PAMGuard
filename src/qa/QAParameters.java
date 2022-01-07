package qa;

import java.io.Serializable;
import java.util.Hashtable;

import PamController.PamFolderException;
import PamController.PamFolders;
import qa.generator.clusters.QACluster;
import qa.generator.location.LocationManager;
import qa.operations.OpsStatusParams;

public class QAParameters implements Cloneable, Serializable {

	public static final long serialVersionUID = 1L;
	
	public String rawDataSource;
	
	private Hashtable<String, ClusterParameters> clusterParametersTable;
	
	private Hashtable<String, OpsStatusParams> opsStatusParamsTable;
	
	private String quickTestLocationGeneratorName;
	
	private String randomTestLocationGeneratorName;
	
	private int nQuickTestSequences = 50;
	
	private int randomTestIntervalSeconds = 3600;
	
	private String reportOutputFolder;

	public boolean immediateQuickReport = true;

	private static final double DEFAULTRANGEFACTOR = 4.;
	/**
	 * how much nominal range for each cluster is multiplied by to determine
	 * min and max ranges for tests. 
	 */
	private Double rangeFactor = DEFAULTRANGEFACTOR;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected QAParameters clone() {
		try {
			return (QAParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get cluster specific parameters. will create a default object if one doesn't 
	 * exist so will never return null.
	 * @param qaCluster cluster
	 * @return cluster specific parameters
	 * @see ClusterParameters
	 */
	public ClusterParameters getClusterParameters(QACluster qaCluster) {
		if (clusterParametersTable == null) {
			clusterParametersTable = new Hashtable<>(); 
		}
		ClusterParameters clusterParams = clusterParametersTable.get(qaCluster.getName());
		if (clusterParams == null) {
			clusterParams = new ClusterParameters();
			setClusterParameters(qaCluster, clusterParams);
		}
		return clusterParams;
	}
	
	/**
	 * Set cluster specific parameters
	 * @param qaCluster cluster
	 * @param clusterParameters cluster specific parameters
	 * @see ClusterParameters
	 */
	public void setClusterParameters(QACluster qaCluster, ClusterParameters clusterParameters) {
		if (clusterParametersTable == null) {
			clusterParametersTable = new Hashtable<>(); 
		}
		clusterParametersTable.put(qaCluster.getName(), clusterParameters);
	}
	
	/**
	 * Get parameters specific to an operational status. <br>
	 * Will create a default object if one doesn't exist in the table. 
	 * @param opsCode code for operations status
	 * @return Status specific parameters. 
	 */
	public OpsStatusParams getOpsStatusParams(String opsCode) {
		if (opsStatusParamsTable == null) {
			opsStatusParamsTable = new Hashtable<>();
		}
		if (opsCode == null) {
			return null;
		}
		OpsStatusParams opsParams = opsStatusParamsTable.get(opsCode);
		if (opsParams == null) {
			opsParams = new OpsStatusParams(opsCode);
			opsStatusParamsTable.put(opsCode, opsParams);
		}
		return opsParams;
	}
	
	/**
	 * Set operations status parameters.
	 * @param opsStatusParams Operations status parameters. 
	 */
	public void setOpsStatusParams(OpsStatusParams opsStatusParams) {
		if (opsStatusParamsTable == null) {
			opsStatusParamsTable = new Hashtable<>();
		}
		opsStatusParamsTable.put(opsStatusParams.getStatusCode(), opsStatusParams);
	}

	/**
	 * This, for now at least, has been hard wired to Stepped. 
	 * @return the quickTestLocationGeneratorName
	 */
	public String getQuickTestLocationGeneratorName() {
//		if (quickTestLocationGeneratorName == null) {
//			quickTestLocationGeneratorName = LocationManager.locatorNames[0];
//		}
//		return quickTestLocationGeneratorName;
		return LocationManager.locatorNames[0];
	}

	/**
	 * @param quickTestLocationGeneratorName the quickTestLocationGeneratorName to set
	 */
	public void setQuickTestLocationGeneratorName(String quickTestLocationGeneratorName) {
		this.quickTestLocationGeneratorName = quickTestLocationGeneratorName;
	}

	/**
	 * @return the randomTestLocationGeneratorName
	 */
	public String getRandomTestLocationGeneratorName() {
		if (randomTestLocationGeneratorName == null) {
			randomTestLocationGeneratorName = LocationManager.locatorNames[2];
		}
		return randomTestLocationGeneratorName;
	}

	/**
	 * @param randomTestLocationGeneratorName the randomTestLocationGeneratorName to set
	 */
	public void setRandomTestLocationGeneratorName(String randomTestLocationGeneratorName) {
		this.randomTestLocationGeneratorName = randomTestLocationGeneratorName;
	}

	/**
	 * @return the nQuickTestSequences
	 */
	public int getnQuickTestSequences() {
		if (nQuickTestSequences == 0) {
			nQuickTestSequences = 50;
		}
		return nQuickTestSequences;
	}

	/**
	 * @param nQuickTestSequences the nQuickTestSequences to set
	 */
	public void setnQuickTestSequences(int nQuickTestSequences) {
		this.nQuickTestSequences = nQuickTestSequences;
	}

	/**
	 * @return the randomTestIntervalSeconds
	 */
	public int getRandomTestIntervalSeconds() {
		if (randomTestIntervalSeconds <= 0) {
			randomTestIntervalSeconds = 3600;
		}
		return randomTestIntervalSeconds;
	}

	/**
	 * @param randomTestIntervalSeconds the randomTestIntervalSeconds to set
	 */
	public void setRandomTestIntervalSeconds(int randomTestIntervalSeconds) {
		this.randomTestIntervalSeconds = randomTestIntervalSeconds;
	}

	/**
	 * @return the reportOutputFolder
	 */
	public String getReportOutputFolder() {
		reportOutputFolder = null;
		if (reportOutputFolder == null) {
			try {
				reportOutputFolder = PamFolders.getProjectFolder("QAReports", false);
			} catch (PamFolderException e) {
				/**
				 * Let it throw exception at this point, since user might just change it
				 * and never create a folder in the default location.  
				 * will do more complete checks on closure of dialog and before the folder
				 * gets used for any output
				 */
			}
		}
		return reportOutputFolder;
	}

	/**
	 * @param reportOutputFolder the reportOutputFolder to set
	 */
	public void setReportOutputFolder(String reportOutputFolder) {
		this.reportOutputFolder = reportOutputFolder;
	}

	/**
	 * Get how much nominal ranges for each cluster should be divided and multiplied
	 * by to determine the overall min and max ranges for testing. 
	 * @return the rangeFactor
	 */
	public double getRangeFactor() {
		if (rangeFactor == null) {
			rangeFactor = DEFAULTRANGEFACTOR;
		}
		return rangeFactor;
	}

	/**	 
	 * Set how much nominal ranges for each cluster should be divided and multiplied
	 * by to determine the overall min and max ranges for testing. 
	 * @param rangeFactor the rangeFactor to set
	 */
	public void setRangeFactor(double rangeFactor) {
		this.rangeFactor = rangeFactor;
	}
}
