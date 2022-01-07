package PamView;

import java.io.Serializable;

import PamModel.parametermanager.ManagedParameters;
import PamModel.parametermanager.PamParameterSet;
import PamUtils.PamUtils;
import PamView.dialog.GroupedSourcePanel;

/**
 * Specific parameters which always to with a GroupedSourcePanel
 * @author Doug Gillespie
 *
 */
public class GroupedSourceParameters implements Serializable, Cloneable, ManagedParameters {

	public static final long serialVersionUID = 1L;

	/**
	 * Name of data source
	 */
	private String dataSource;
	
	/**
	 * Bitmap of all channels used.  Note that this may be a sequence map and not a channel
	 * map, depending on the source being used
	 */
	private int channelBitmap;

	/**
	 * integer list of which group each channel belongs to
	 */
	private int[] channelGroups;

	/**
	 * Grouping selection
	 */
	private int groupingType = GroupedSourcePanel.GROUP_ALL;
	
	/**
	 * Create a grouped source parameters object. 
	 * @param dataSource data source
	 * @param channelBitmap channel bitmap
	 * @param channelGroups channel groups list
	 * @param groupingType grouping type. 
	 */
	public GroupedSourceParameters(String dataSource, int channelBitmap, int[] channelGroups, int groupingType) {
		super();
		this.dataSource = dataSource;
		this.channelBitmap = channelBitmap;
		this.channelGroups = channelGroups;
		this.groupingType = groupingType;
	}

	public GroupedSourceParameters() {
		super();
	}

	
	/**
	 * Get the group bitmap. i.e. the group numbers. This is not
	 * the channel bitmap. 
	 * @return the group bitmap. 
	 */
	public int getGroupMap() {
		return GroupedSourcePanel.getGroupMap(channelBitmap, channelGroups);
	}
	
	/**
	 * Get the total number of channel groups
	 * @return number of groups
	 */
	public int countChannelGroups() {
		return GroupedSourcePanel.countChannelGroups(channelBitmap, channelGroups);
	}
	
	/**
	 * Get the specific channels associated with a particular group. 
	 * @param iGroup group index (0, 1, 2, 3 whatever the actual group numbers are !)
	 * @return bitmap of group channels
	 */
	public int getGroupChannels(int iGroup) {
		return GroupedSourcePanel.getGroupChannels(iGroup, channelBitmap, channelGroups);
	}

	/**
	 * @return the dataSource
	 */
	public String getDataSource() {
		return dataSource;
	}

	/**
	 * @param dataSource the dataSource to set
	 */
	public void setDataSource(String dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Return the channel bitmap selected from the Source Pane.  Note that this may actually be the sequence
	 * bitmap and not the channel bitmap, depending on the source that has been selected
	 * @return the channelBitmap or sequenceBitmap
	 */
	public int getChanOrSeqBitmap() {
		return channelBitmap;
	}

	/**
	 * This method has been added so that the channelBitmap field will be included in XML output.
	 * @return
	 */
	public int getChannelBitmap() {
		return getChanOrSeqBitmap();
	}

	/**
	 * Set the channel or sequence bitmap (depending on the source that has been selected)
	 * @param channelBitmap the channelBitmap to set
	 */
	public void setChanOrSeqBitmap(int channelBitmap) {
		this.channelBitmap = channelBitmap;
	}

	/**
	 * @return the channelGroups
	 */
	public int[] getChannelGroups() {
		return channelGroups;
	}
	
//	/**
//	 * Get a bitmap of each channel group in an int array
//	 */
//	public void getChannelGroupBitMap() {
//		int channels = PamUtils.getNumChannels(this.getChannelBitmap()); 
//		int channelCount=0; 
//		int groupN=0; 
//		while (channelCount!=channels && channelCount<32) {
//			for (int i=0; i<channels; i++) {
//				if (channelGroups[i]==groupN) {
//					//channel is part of group
//					channelCount++; 
//				}
//			}
//			groupN++;
//		}
//	}

	/**
	 * @param channelGroups the channelGroups to set
	 */
	public void setChannelGroups(int[] channelGroups) {
		this.channelGroups = channelGroups;
	}

	/**
	 * @return the groupingType
	 */
	public int getGroupingType() {
		return groupingType;
	}

	/**
	 * @param groupingType the groupingType to set
	 */
	public void setGroupingType(int groupingType) {
		this.groupingType = groupingType;
	}
	
	/**
	 * 
	 * @return true if at least one group has multiple elements, so 
	 * might be able to calculate bearings. 
	 */
	public boolean mayHaveBearings() {
		int n = countChannelGroups();
		int groupMap;
		for (int i = 0 ; i< n; i++) {
			groupMap = getGroupChannels(i);
			if (PamUtils.getNumChannels(groupMap) > 1) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return true if mayHaveBearings is true and if there is more than one group
	 */
	public boolean mayHaveRange() {
		return (mayHaveBearings() && countChannelGroups() > 1); 
	}

	@Override
	public GroupedSourceParameters clone() {
		try {
			return (GroupedSourceParameters) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Note that all the fields have getters, so even though they are private they will still
	 * be included in the output so we don't need to explicitly add them here
	 */
	@Override
	public PamParameterSet getParameterSet() {
		PamParameterSet ps = PamParameterSet.autoGenerate(this);
		return ps;
	}

}
