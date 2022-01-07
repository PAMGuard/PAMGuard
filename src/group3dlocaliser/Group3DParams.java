package group3dlocaliser;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Hashtable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PrivatePamParameterData;
import PamView.GroupedSourceParameters;
import group3dlocaliser.algorithm.LocaliserAlgorithm3D;
import group3dlocaliser.algorithm.LocaliserAlgorithmParams;
import group3dlocaliser.grouper.DetectionGrouperParams;

public class Group3DParams implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;
	
	@Deprecated
	private GroupedSourceParameters groupedSourceParams;
	
	private String sourceName;
	
	private DetectionGrouperParams grouperParams;
	
	private String algorithmName;
	
	private Hashtable<String, LocaliserAlgorithmParams> algorithmSpecificParams; 
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Group3DParams clone() {
		try {
			return (Group3DParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

//	/**
//	 * @return the groupedSourceParams
//	 */
//	public GroupedSourceParameters getGroupedSourceParams() {
//		if (groupedSourceParams == null) {
//			groupedSourceParams = new GroupedSourceParameters();
//		}
//		return groupedSourceParams;
//	}
//
//	/**
//	 * @param groupedSourceParams the groupedSourceParams to set
//	 */
//	public void setGroupedSourceParams(GroupedSourceParameters groupedSourceParams) {
//		this.groupedSourceParams = groupedSourceParams;
//	}

	/**
	 * @return the grouperParams
	 */
	public DetectionGrouperParams getGrouperParams() {
		if (grouperParams == null) {
			grouperParams = new DetectionGrouperParams();
		}
		return grouperParams;
	}

	/**
	 * @param grouperParams the grouperParams to set
	 */
	public void setGrouperParams(DetectionGrouperParams grouperParams) {
		this.grouperParams = grouperParams;
	}

	/**
	 * @return the algorithmName
	 */
	public String getAlgorithmName() {
		return algorithmName;
	}

	/**
	 * @param algorithmName the algorithmName to set
	 */
	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}
	
	/**
	 * Get the parameters for a specific algorithm type
	 * @param algoProvider algorithm provider
	 * @return algorithm params or null if not yet set
	 */
	public LocaliserAlgorithmParams getAlgorithmParams(LocaliserAlgorithm3D algoProvider) {
		if (algorithmSpecificParams == null) {
			return null;
		}
		if (algoProvider == null) {
			return null;
		}
		return algorithmSpecificParams.get(algoProvider.getClass().getName());
	}
	
	/**
	 * Set the parameters for a specific algorithm type
	 * @param algorithmProvider algorithm provider
	 * @param localiserAlgorithmParams specific params, or null to remove them from the list. 
	 */
	public void setAlgorithmParams(LocaliserAlgorithm3D algorithmProvider, LocaliserAlgorithmParams localiserAlgorithmParams) {
		if (algorithmSpecificParams == null) {
			algorithmSpecificParams = new Hashtable<>();
		}
		if (algorithmProvider == null) {
			return;
		}
		String key = algorithmProvider.getClass().getName();
		if (localiserAlgorithmParams == null) {
			algorithmSpecificParams.remove(key);
		}
		else {
			algorithmSpecificParams.put(key, localiserAlgorithmParams);
		}
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		try {
			Field field = this.getClass().getDeclaredField("algorithmSpecificParams");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return algorithmSpecificParams;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

	/**
	 * @return the sourceName
	 */
	public String getSourceName() {
		if (sourceName == null && groupedSourceParams != null) {
			sourceName = groupedSourceParams.getDataSource();
		}
		return sourceName;
	}

	/**
	 * @param sourceName the sourceName to set
	 */
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	
}
