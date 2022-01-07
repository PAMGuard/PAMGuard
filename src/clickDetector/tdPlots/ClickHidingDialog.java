package clickDetector.tdPlots;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import clickDetector.BTDisplayParameters;
import PamView.dialog.GroupedSourcePanel;
import PamView.dialog.PamButtonAlpha;
import PamView.hidingpanel.HidingDialog;
import PamView.hidingpanel.HidingDialogComponent;
import PamView.panel.PamPanel;

public class ClickHidingDialog extends HidingDialogComponent {

	private ClickPlotInfo clickPlotInfo;
	private JPanel mainPanel;
	private JRadioButton[] colourButtons;
	
	private ImageIcon tabIcon=new ImageIcon(ClassLoader
			.getSystemResource("Resources/reanalyseClicks.png"));
	private PamButtonAlpha channels;
	private PamPanel channelPanel;
	
	public ClickHidingDialog(ClickPlotInfo clickPlotInfo) {
		super();
		this.clickPlotInfo = clickPlotInfo;
		mainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c=new GridBagConstraints();
		c.anchor=GridBagConstraints.FIRST_LINE_START;
		c.gridx=0;
		c.gridy=0;
		c.insets=new Insets(10,0,10,0);//give everything a bit of space
		JPanel colourPanel = new JPanel(new GridLayout(0,1));
		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.add(BorderLayout.NORTH, colourPanel);
		colourPanel.setBorder(new TitledBorder("Click Colour"));
		mainPanel.add(northPanel,c);
		c.weighty=1;
		c.gridy++;

		String[] colNames = BTDisplayParameters.colourNames;
		colourButtons = new JRadioButton[colNames.length];
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < colNames.length; i++) {
			colourButtons[i] = new JRadioButton(colNames[i]);
			bg.add(colourButtons[i]);
			colourPanel.add(colourButtons[i]);
			colourButtons[i].addActionListener(new SelectColour(i));
			colourButtons[i].setOpaque(false);
		}
		
		channelPanel=new PamPanel(new BorderLayout());
		channels=new PamButtonAlpha("Channels");
//		channels.setBackground(PamColors.getInstance().getColor(PamColor.BACKGROUND_ALPHA));
		channelPanel.add(BorderLayout.NORTH, channels);
		channels.addActionListener(new OpenChannelMenu());
		mainPanel.add(channelPanel,c);
		
		c.weighty=1;
		channelPanel.setOpaque(false);
		colourPanel.setOpaque(false);
		mainPanel.setOpaque(false);
		northPanel.setOpaque(false);
		
	}
		
	class OpenChannelMenu implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {

			JPopupMenu menu=createClickList();
			menu.setLightWeightPopupEnabled(false);

			Point location=channels.getLocationOnScreen();
			location.y=channels.getLocationOnScreen().y+channels.getHeight();
			
			//need to do a bit of validate hockery pokery to solve a repaint problem? 
//			menu.invalidate();
			if (menu != null) {
				menu.show(channelPanel,0, channels.getHeight());
			}
//			menu.validate();
			menu.repaint();

			
		}
		
	}
	
	public JPopupMenu  createClickList(){
		JPopupMenu  menu=new JPopupMenu();
		// just do the stuff for selecting different channel groups for now
		int channels = clickPlotInfo.getClickControl().getClickParameters().getChannelBitmap();
		int[] channelGroups = clickPlotInfo.getClickControl().getClickParameters().getChannelGroups();
		int nChannelGroups = GroupedSourcePanel.countChannelGroups(channels, channelGroups);
		if (nChannelGroups > 1) {
			JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem("Show all channel groups");
			menuItem.addActionListener(new ChannelGroupAction(0));
			menu.add(menuItem);
			if (clickPlotInfo.getDisplayChannels() == 0) menuItem.setSelected(true);
			String str;
			int groupChannels;
			for (int i = 0; i < nChannelGroups; i++) {
				str = "Show channels " + GroupedSourcePanel.getGroupList(i, channels, channelGroups);
				menuItem = new JCheckBoxMenuItem(str);
				groupChannels = GroupedSourcePanel.getGroupChannels(i, channels, channelGroups);
				menuItem.addActionListener(new ChannelGroupAction(groupChannels));
				if (clickPlotInfo.getDisplayChannels() == groupChannels) menuItem.setSelected(true);
				menu.add(menuItem);
			}
		}
		return menu;

	}
	
	class ChannelGroupAction implements ActionListener{
		int groupSelection;

		public ChannelGroupAction(int groupSelection) {
			super();
			this.groupSelection = groupSelection;
		}

		public void actionPerformed(ActionEvent e) {
			
			clickPlotInfo.setDisplayChannels(groupSelection);

		}

	}

	
	
	
	private class SelectColour implements ActionListener {

		private int colourId;
		public SelectColour(int colourId) {
			super();
			this.colourId = colourId;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (colourButtons[colourId].isSelected()) {
				clickPlotInfo.selectColourType(colourId);
			}
		}
		
	}

	@Override
	public JComponent getComponent() {
		return mainPanel;
	}

	@Override
	public boolean canHide() {
		return true;
	}

	@Override
	public void showComponent(boolean visible) {
		selectColourButton();
	}


	public void selectColourButton() {
		int col = clickPlotInfo.btDisplayParams.colourScheme;
		for (int i = 0; i < colourButtons.length; i++) {
			colourButtons[i].setSelected(i == col);
		}
	}

	@Override
	public String getName() {
		return clickPlotInfo.getShortName();
	}

	/* (non-Javadoc)
	 * @see PamView.hidingpanel.HidingDialogComponent#hasMore()
	 */
	@Override
	public boolean hasMore() {
		return true;
	}

	/* (non-Javadoc)
	 * @see PamView.hidingpanel.HidingDialogComponent#showMore(PamView.hidingpanel.HidingDialog)
	 */
	@Override
	public boolean showMore(HidingDialog hidingDialog) {
		boolean ans = clickPlotInfo.editOptions(SwingUtilities.getWindowAncestor(clickPlotInfo.getTdGraph().getGraphOuterPanel()));
		if (ans) {
			selectColourButton();
		}
		return ans;
	}
	
	@Override
	public Icon getIcon(){
		return tabIcon; 
	}

}
