package beamformer;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamModel.parametermanager.PamParameterSet.ParameterSetType;
import PamModel.parametermanager.PrivatePamParameterData;
import PamUtils.DeepCloner;
import PamView.GroupedSourceParameters;

public class BeamFormerParams implements Cloneable, Serializable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	private GroupedSourceParameters groupedSourceParameters;
	/**
	 * The name of the FFT data source
	 */
//	public String dataSource;

	/**
	 * Bitmap of all possible channels.  If there are 4 channels,
	 * this would hold 00001111
	 */
//	public int channelBitmap;

	/**
	 * Array containing channel assignments.  Each index in the vector
	 * corresponds to a channel, and holds the group that the channel is
	 * in.  For example, if we have channels 0-3 (00001111), channels 0 and 1
	 * are in group 0 and channels 2 and 3 are in group 1, the array would
	 * contain:
	 * <ul>
	 * <li>channelGroups[0] = 0</li>
	 * <li>channelGroups[1] = 0</li>
	 * <li>channelGroups[2] = 1</li>
	 * <li>channelGroups[3] = 1</li>
	 * </ul>
	 * In general, channelGroups[chanNum]=groupNum
	 */
//	public int[] channelGroups;

	/**
	 * A Hashtable linking the algorithm name to the parameters object, so that we can pull up
	 * the algorithm-specific settings window.  The key can take two forms: groupNum_groupChannelMap, to
	 * determine the last algorithm the user selected for that particular group number and group channel
	 * map, and algorithmName_groupNum_groupChannelMap to return the parameters for a specific group number,
	 * group channel map and algorithm. 
	 */
	private HashMap<String, BeamAlgorithmParams> algorithmParamsTable = new HashMap<>();

	/**
	 * The type of grouping
	 */
//	public int groupingType = GroupedSourcePanel.GROUP_ALL;
	
	/**
	 * list of current algorithms used by each group. needed in order to 
	 * get rid of the muddle over getting algorithm parameters with or without a name 
	 * out of the algo parameters hash table. 
	 */
	private ArrayList<String> currentGroupAlgorithms = new ArrayList<>();
	
	/**
	 * Get the algorithm name for the n'th group.
	 * this is needed prior to calls to getalgorithmParams(...) 
	 * @param iGroup group index
	 * @return algorithm name. 
	 */
	public String getAlgorithmName(int iGroup) {
		if (currentGroupAlgorithms == null) return null;
		if (iGroup >= currentGroupAlgorithms.size()) return null;
		return currentGroupAlgorithms.get(iGroup);
	}
	
	public void addAlgorithmNames(String name) {
		if (currentGroupAlgorithms == null) {
			currentGroupAlgorithms = new ArrayList<>();
		}
		currentGroupAlgorithms.add(name);
	}
	
	public void clearAlgorithmNames() {
		if (currentGroupAlgorithms == null) return;
		currentGroupAlgorithms.clear();		
	}

	/**
	 * Main constructor
	 * 
	 */
	public BeamFormerParams() {
	}


	@Override
	/**
	 * Make a clone of this object.  Note that because of the HashTable, this
	 * is somewhat complicated.  Instead of going into each BeamAlgorithmParams extended
	 * object, serial/deserialize this object to make a deep copy.  See this page:
	 * http://javatechniques.com/blog/faster-deep-copies-of-java-objects/
	 * for complete details.
	 */
	public BeamFormerParams clone(){
		BeamFormerParams newOne = new BeamFormerParams();
//        try {
//            // Write the object out to a byte array
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            ObjectOutputStream out = new ObjectOutputStream(bos);
//            out.writeObject(this);
//            out.flush();
//            out.close();
//
//            // Make an input stream from the byte array and read
//            // a copy of the object back in.
//            ObjectInputStream in = new ObjectInputStream(
//                new ByteArrayInputStream(bos.toByteArray()));
//            newOne = (BeamFormerParams) in.readObject();
//        }
//        catch(IOException e) {
//            e.printStackTrace();
//        }
//        catch(ClassNotFoundException cnfe) {
//            cnfe.printStackTrace();
//        }
        
		try {
			newOne = (BeamFormerParams) DeepCloner.deepClone(this);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
        return newOne;		
	}

//	/**
//	 * Get a set of algorithm parameters for a specific channel group and a specific channel map
//	 * @param groupId channel group
//	 * @param groupChanMap the channel map of the group 
//	 * @return algorithm params, or null
//	 */
//	public BeamAlgorithmParams getAlgorithmParms(int groupId, int groupChanMap) {
//		if (algorithmParamsTable == null) {
//			algorithmParamsTable = new HashMap<>();
//		}
//		return algorithmParamsTable.get(makeAlgoSearchName(groupId, groupChanMap));
//	}

	/**
	 * Get a set of algorithm parameters for an algorithm of a specific name, a
	 * specific channel group and a specific channel map
	 * @param algorithmName algorithm name
	 * @param groupId channel group
	 * @param groupChanMap the channel map of the group 
	 * @return algorithm params, or null
	 */
	public BeamAlgorithmParams getAlgorithmParms(int groupId, int groupChanMap, String algorithmName) {
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
	public void setAlgorithmParams(String algorithmName, int groupId, int groupChanMap, BeamAlgorithmParams algorithmParams) {
		if (algorithmParamsTable == null) {
			this.algorithmParamsTable = new HashMap<>();
		}
		this.algorithmParamsTable.put(makeAlgoSearchName(algorithmName, groupId, groupChanMap), algorithmParams);
	}

//	/**
//	 * Save a set of algorithm parameters for a specific channel group and a specific channel map.  This is really just
//	 * used to return the algorithm name that the user has selected for a specific group and channel map, in order to
//	 * correctly populate the BeamformerSettingsPane Algorithm tab.
//	 *  
//	 * @param groupId channel group number
//	 * @param groupChanMap channel map of the group
//	 * @param algorithmParams algorithm parameters. 
//	 */
//	public void setAlgorithmParams(int groupId, int groupChanMap, BeamAlgorithmParams algorithmParams) {
//		if (algorithmParamsTable == null) {
//			this.algorithmParamsTable = new HashMap<>();
//		}
//		this.algorithmParamsTable.put(makeAlgoSearchName(groupId, groupChanMap), algorithmParams);
//	}
//
//	/**
//	 * Make a unique name from a group id and group channel map to use with the parameters hash table. 
//	 * @param groupId
//	 * @param groupChanMap
//	 * @return
//	 */
//	private String makeAlgoSearchName(int groupId, int groupChanMap) {
//		return String.format("%d_%d", groupId, groupChanMap);
//	}


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


	/**
	 * @return the groupedSourceParameters
	 */
	public GroupedSourceParameters getGroupedSourceParameters() {
		if (groupedSourceParameters == null) {
			groupedSourceParameters = new GroupedSourceParameters();
		}
		return groupedSourceParameters;
	}


	/**
	 * @param groupedSourceParameters the groupedSourceParameters to set
	 */
	public void setGroupedSourceParameters(GroupedSourceParameters groupedSourceParameters) {
		this.groupedSourceParameters = groupedSourceParameters;
	}
	/**
	 * Get the total number of channel groups
	 * @return number of groups
	 */
	public int countChannelGroups() {
		return getGroupedSourceParameters().countChannelGroups();
	}
	
	/**
	 * Get the specific channels associated with a particular group. 
	 * @param iGroup group index (0, 1, 2, 3 whatever the actual group numbers are !)
	 * @return bitmap of group channels
	 */
	public int getGroupChannels(int iGroup) {
		return getGroupedSourceParameters().getGroupChannels(iGroup);
	}

	/**
	 * Returns the primary dataSource for the beam former. This can 
	 * be either raw or fft data or possibly even a detector output or spectrogram mark. 
	 * Note that the <em><u>long</u> datablock name</em> is used here.
	 * @return the data source
	 */
	public String getDataSource() {
		return getGroupedSourceParameters().getDataSource();
	}

	/**
	 * Set the data source.  Note that this should be the <em><u>long</u> datablock name</em>, not just the
	 * data name.
	 * @param dataSource the dataSource to set
	 */
	public void setDataSource(String dataSource) {
		getGroupedSourceParameters().setDataSource(dataSource);
	}

	/**
	 * Return the channel bitmap selected from the Source Pane.
	 * @return the channelBitmap
	 */
	public int getChannelBitmap() {
		return getGroupedSourceParameters().getChanOrSeqBitmap();
	}

	/**
	 * @param channelBitmap the channelBitmap to set
	 */
	public void setChannelBitmap(int channelBitmap) {
		getGroupedSourceParameters().setChanOrSeqBitmap(channelBitmap);
	}

	/**
	 * @return the channelGroups
	 */
	public int[] getChannelGroups() {
		return getGroupedSourceParameters().getChannelGroups();
	}

	/**
	 * @param channelGroups the channelGroups to set
	 */
	public void setChannelGroups(int[] channelGroups) {
		getGroupedSourceParameters().setChannelGroups(channelGroups);
	}

	/**
	 * @return the groupingType
	 */
	public int getGroupingType() {
		return getGroupedSourceParameters().getGroupingType();
	}

	/**
	 * @param groupingType the groupingType to set
	 */
	public void setGroupingType(int groupingType) {
		getGroupedSourceParameters().setGroupingType(groupingType);	
	}

	/**
	 * Get the auto-generated parameter set, and then add in the fields that are not included
	 * because they are not public and do not have getters.
	 * Note: for each field, we search the current class and (if that fails) the superclass.  It's
	 * done this way because BeamFormerParams might be used directly (and thus the field would
	 * be found in the class) and it also might be used as a superclass to something else
	 * (e.g. BFLocaliserParams) in which case the field would only be found in the superclass.
	 */
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
			try {
				Field field = this.getClass().getSuperclass().getDeclaredField("algorithmParamsTable");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return algorithmParamsTable;
					}
				});
			} catch (NoSuchFieldException | SecurityException e2) {
				e2.printStackTrace();
			}
		}
			
		try {
			Field field = this.getClass().getDeclaredField("currentGroupAlgorithms");
			ps.put(new PrivatePamParameterData(this, field) {
				@Override
				public Object getData() throws IllegalArgumentException, IllegalAccessException {
					return currentGroupAlgorithms;
				}
			});
		} catch (NoSuchFieldException | SecurityException e) {
			try {
				Field field = this.getClass().getSuperclass().getDeclaredField("currentGroupAlgorithms");
				ps.put(new PrivatePamParameterData(this, field) {
					@Override
					public Object getData() throws IllegalArgumentException, IllegalAccessException {
						return currentGroupAlgorithms;
					}
				});
			} catch (NoSuchFieldException | SecurityException e2) {
				e2.printStackTrace();
			}
		}
		return ps;
	}


}
