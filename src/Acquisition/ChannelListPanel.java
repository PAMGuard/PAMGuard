package Acquisition;

import java.awt.Component;

import javafx.scene.Node;

/**
 * Interface allowing individual devices to provide their
 * own channel list panel. Some devices (such as sound cards)
 * don't use this at all. ASIO cards use a standard concrete 
 * implementation. NI cards use a more sophisticated one
 * whereby multiple devices may be used. 
 * @author Doug Gillespie
 *
 */
public interface ChannelListPanel {

	/**
	 * 
	 * @return  the component for the dialog panel. 
	 */
	public Component getComponent();
	
	/**
	 * 
	 * @return  the component for the dialog panel. 
	 */
	public Node getNode();
	
	/**
	 * Set the channel list. 
	 * @param channelList channel list
	 */
	public void setParams(int[] channelList);
	
	/**
	 * 
	 * @return the channel list
	 */
	public int[] getChannelList();
	
	/**
	 * 
	 * @return true if the configuration seems to be valid (i.e. no repeated channels, etc)
	 */
	public boolean isDataOk();
	
	/**
	 * Set the total number of channels to display. 
	 * @param nChannels the total number of channels to display. 
	 */
	public void setNumChannels(int nChannels);
	
}
