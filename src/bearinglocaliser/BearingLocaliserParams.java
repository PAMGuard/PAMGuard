package bearinglocaliser;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import Localiser.controls.RawOrFFTParamsInterface;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import PamView.GroupedSourceParameters;
import bearinglocaliser.algorithms.BearingAlgorithmParams;

public class BearingLocaliserParams implements Serializable, Cloneable, RawOrFFTParamsInterface, ManagedParameters {

	public static final long serialVersionUID = 1L;

	private GroupedSourceParameters rawOrFFTSourceParameters = new GroupedSourceParameters();
	
	public String detectionSource;

	private int fftLength = 512;
	
	private int fftHop = fftLength / 2;
	
	private HashMap<String, BearingAlgorithmParams> algorithmParamsTable = new HashMap<>();
	
	/**
	 * Say to make additional data units and beam form ALL channel groups. 
	 */
	public boolean doAllGroups;
	
	private ArrayList<String> currentAlgorithmNames = new ArrayList<>();

	public BearingLocaliserParams() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected BearingLocaliserParams clone() {
		try {
			return (BearingLocaliserParams) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * Get a set of algorithm parameters for an algorithm of a specific name, a
	 * specific channel group and a specific channel map
	 * @param algorithmName algorithm name
	 * @param groupId channel group
	 * @param groupChanMap the channel map of the group 
	 * @return algorithm params, or null
	 */
	public BearingAlgorithmParams getAlgorithmParms(int groupId, int groupChanMap, String algorithmName) {
		if (algorithmName == null) {
			return null;
		}
		if (algorithmParamsTable == null) {
			return null;
		}
		return algorithmParamsTable.get(makeAlgoSearchName(algorithmName, groupId, groupChanMap));
	}

	/**
	 * Save a set of algorithm parameters for an algorithm of a specific name, a
	 * specific channel group and a specific channel map
	 * @param algorithmName algorithm name
	 * @param groupId channel group number
	 * @param groupChanMap channel map of the group
	 * @param algorithmParams algorithm parameters. 
	 */
	public void setAlgorithmParams(String algorithmName, int groupId, int groupChanMap, BearingAlgorithmParams algorithmParams) {
		if (algorithmParamsTable == null) {
			this.algorithmParamsTable = new HashMap<>();
		}
		this.algorithmParamsTable.put(makeAlgoSearchName(algorithmName, groupId, groupChanMap), algorithmParams);
	}



	/**
	 * Make a unique name from an algorithm name and group id to use with the parameters hash table. 
	 * @param baseName algorithm name
	 * @param groupId channel group
	 * @param chanMap channel map
	 * @return name (the base name and the group id number). 
	 */
	private String makeAlgoSearchName(String baseName, int groupId, int chanMap) {
		return String.format("%s_%d_%d", baseName, groupId, chanMap);
	}

	public String getDataSource() {
		return rawOrFFTSourceParameters.getDataSource();
	}

	public void setDataSource(String longDataName) {
		rawOrFFTSourceParameters.setDataSource(longDataName);
	}

	/**
	 * @return the rawOrFFTSourceParameters
	 */
	public GroupedSourceParameters getRawOrFFTSourceParameters() {
		return rawOrFFTSourceParameters;
	}

	/**
	 * @param rawOrFFTSourceParameters the rawOrFFTSourceParameters to set
	 */
	public void setRawOrFFTSourceParameters(GroupedSourceParameters rawOrFFTSourceParameters) {
		this.rawOrFFTSourceParameters = rawOrFFTSourceParameters;
	}

	/**
	 * Return the channel bitmap selected from the Source Pane.  Note that this may actually be the sequence
	 * bitmap and not the channel bitmap, depending on the source that has been selected
	 * @return the channelBitmap or sequenceBitmap
	 */
	public int getChannelBitmap() {
		return this.rawOrFFTSourceParameters.getChanOrSeqBitmap();
	}

	public int[] getChannelGroups() {
		return this.rawOrFFTSourceParameters.getChannelGroups();
	}

	public String getAlgorithmName(int i) {
		if (currentAlgorithmNames.size() > i) {
			return currentAlgorithmNames.get(i);
		}
		else {
			return null;
		}
	}

	public void addAlgorithmName(String algoName) {
		currentAlgorithmNames.add(algoName);
	}

	public void clearAlgorithmNames() {
		currentAlgorithmNames.clear();
	}

	@Override
	public String getSourceName() {
		return rawOrFFTSourceParameters.getDataSource();
	}

	@Override
	public void setSourceName(String sourceName) {
		rawOrFFTSourceParameters.setDataSource(sourceName);
	}

	@Override
	public int getFftLength() {
		return fftLength;
	}

	@Override
	public void setFftLength(int fftLength) {
		this.fftLength = fftLength;
	}

	@Override
	public int getFftHop() {
		return fftHop;
	}

	@Override
	public void setFftHop(int fftHop) {
		this.fftHop = fftHop;
	}

	@Override
	public int getWindowFunction() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setWindowFunction(int windowFunction) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this, ParameterSetType.DETECTOR);
		try {
			Field field = this.getClass().getDeclaredField("algorithmParamsTable");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return algorithmParamsTable;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		try {
			Field field = this.getClass().getDeclaredField("currentAlgorithmNames");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return currentAlgorithmNames;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		return ps;
	}

	

}
