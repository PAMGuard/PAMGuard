package Layout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import PamView.dialog.GroupedSourcePanel;
import PamguardMVC.PamDataUnit;
import clickDetector.ClickDetection;

public abstract class DisplayChannelFilter {
	
	private int displayChannels = 0;
	protected int channels = 1;
	protected int[] channelGroups = new int[] {1};

	public boolean shouldPlot(PamDataUnit dataUnit) {
		if (displayChannels > 0 && (displayChannels & dataUnit.getChannelBitmap()) == 0) {
			return false;
		}
		return true;
	}
	
	public void noteNewSettings(int channels, int[] channelGroups) {
		this.channelGroups = channelGroups;
		this.channels = channels;
		if (GroupedSourcePanel.getGroupIndex(displayChannels,
				channels, channelGroups) < 0) {
			displayChannels = 0;
		}
	}
	
	public int getDisplayChannels() {
		return this.displayChannels;
	}
	
	protected abstract void repaintAsNeeded();
	
	public void setDisplayChannels(int displayChannels) {
		this.displayChannels = displayChannels;
		repaintAsNeeded();
		//Send signal to repaint?
	}
	
	public void addMenuSet(JComponent menu) {
		if(channelGroups==null) {
			return;
		}
		int nChannelGroups = GroupedSourcePanel.countChannelGroups(channels, channelGroups);
		if(menu instanceof JPopupMenu) {
			((JPopupMenu) menu).addSeparator();
		}
		JCheckBoxMenuItem channelMenu = new JCheckBoxMenuItem("Show all channel groups");
		channelMenu.addActionListener(new ChannelGroupAction(0));
		menu.add(channelMenu);
		if (getDisplayChannels() == 0) channelMenu.setSelected(true);
		String str;
		int groupChannels;
		if (nChannelGroups > 1) {		
			for (int i = 0; i < nChannelGroups; i++) {
				str = "Show channels " + GroupedSourcePanel.getGroupList(i, channels, channelGroups);
				channelMenu = new JCheckBoxMenuItem(str);
				groupChannels = GroupedSourcePanel.getGroupChannels(i, channels, channelGroups);
				channelMenu.addActionListener(new ChannelGroupAction(groupChannels));
				if (getDisplayChannels() == groupChannels) channelMenu.setSelected(true);
				menu.add(channelMenu);
			}
		}else {
			for (int i = 0; i < channelGroups.length; i++) {
				str = "Only show channel " + PamUtils.PamUtils.getLowestChannel(channelGroups[i]);
				channelMenu = new JCheckBoxMenuItem(str);
				groupChannels = channelGroups[i];
				channelMenu.addActionListener(new ChannelGroupAction(groupChannels));
				if (getDisplayChannels() == groupChannels) channelMenu.setSelected(true);
				menu.add(channelMenu);
			}
		}
	}
	
	
	class ChannelGroupAction implements ActionListener{
		int groupSelection;

		public ChannelGroupAction(int groupSelection) {
			super();
			this.groupSelection = groupSelection;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			setDisplayChannels(groupSelection);

		}

	}
	
	

	
	

}
