package difar.display;

import generalDatabase.lookupTables.LookupItem;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import Array.StreamerDataUnit;
import PamUtils.PamCalendar;
import PamUtils.PamUtils;
import PamView.dialog.PamButton;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import difar.DIFARMessage;
import difar.DifarControl;
import difar.DifarParameters;

/**
 * Sits at the top of the DIFAR queue panel 
 */
public class DifarActionsVesselPanel implements DIFARDisplayUnit{

	private JPanel vesselPanel;
//	public JCheckBox multiChannel;
//	public JComboBox selectedClassification;

	private DifarControl difarControl;
	private JButton deploy, settings, startCal;
	private JButton clearQueue;
	private JButton buoyEnd;
	
//	private final Object defaultItem= new Object(){
//		public String toString() {
//			return DifarParameters.Default;
//		};
//	};
	
	public DifarActionsVesselPanel(DifarControl difarControl) {
		this.difarControl=difarControl;

		vesselPanel = new PamPanel(new BorderLayout());
//		vesselPanel.setBorder(new TitledBorder("Actions"));
		JPanel westPanel = new PamPanel(new FlowLayout(FlowLayout.LEFT));
		vesselPanel.add(BorderLayout.WEST, westPanel);

		westPanel.add(new PamLabel("Actions "));
//		westPanel.add(settings = new PamButton("Settings"));
		westPanel.add(deploy = new PamButton("Deploy"));
		westPanel.add(startCal = new PamButton("Start Calib.."));
		westPanel.add(clearQueue = new PamButton("Clear Queue"));
		westPanel.add(buoyEnd = new PamButton("Buoy Finished"));
//		westPanel.add(new PamLabel("  Default Clip:"));
//		westPanel.add(selectedClassification = new JComboBox(difarControl.getDifarParameters().
//				getSpeciesList(difarControl).getSelectedList()));
//		selectedClassification.insertItemAt(defaultItem, 0);
//		Object selectedParam = difarControl.getDifarParameters().selectedClassification;
//		selectedParam = (selectedParam == null) ? defaultItem : selectedParam; 
//		selectedClassification.setSelectedItem(selectedParam);
//		multiChannel = new JCheckBox("Multi-Channel");
//		multiChannel.setHorizontalTextPosition(SwingConstants.LEFT);
//		multiChannel.setSelected(difarControl.getDifarParameters().multiChannelClips);
//		westPanel.add(multiChannel);

//		autoProcessNext = new PamCheckBox("Auto Process");
//		westPanel.add(autoProcessNext);
//		autoProcessNext.setSelected(difarControl.getDifarParameters().autoProcessNext);

		deploy.addActionListener(new Deploy());
//		settings.addActionListener(new Settings());
		startCal.addActionListener(new StartCalibration());
		clearQueue.addActionListener(new ClearQueue());
		buoyEnd.addActionListener(new BuoyEnd());
//		selectedClassification.addItemListener(new SelectedClassification());
//		multiChannel.addActionListener(new MultiChannelToggle());
		
		deploy.setToolTipText("Change the location or other information about a DIFAR buoy");
//		settings.setToolTipText("Setup the DIFAR system");
		startCal.setToolTipText("Start a DIFAR calibration run");
		clearQueue.setToolTipText("Remove all clips from the Queue");
		buoyEnd.setToolTipText("Mark the end time for this sonobuoy");
//		autoProcessNext.setToolTipText("Automatically take next unit from the queue for demux and DIFAR processing");

	}
	
	class ClearQueue implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			String msg = String
					.format("Are you sure you want to clear the queue?");
			msg += "All clips in the queue will be removed.";
			msg += "This action cannot be undone.";
			int ans = JOptionPane.showConfirmDialog(vesselPanel, msg,"Clear Queue", JOptionPane.OK_CANCEL_OPTION);
			if (ans == JOptionPane.CANCEL_OPTION) {
				return;
			}
			// User has been warned and really wants to clear the queue
			difarControl.getDifarQueue().clearQueuePanel();

		}
	}

	class Deploy implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
//			difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.Deploy));
			deployButton();
		}
	}
	class Settings implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.EditVesselBuoySettings));

		}
	}
	class StartCalibration implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			startCalibration();			
		}

	}
	class BuoyEnd implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
//			difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.Deploy));
			buoyEndButton();
		}
	}
	
//	class MultiChannelToggle implements ActionListener{
//		@Override
//		public void actionPerformed(ActionEvent e) {
//			difarControl.getDifarParameters().multiChannelClips
//					=!difarControl.getDifarParameters().multiChannelClips;
//		}
//	}
	
//	class SelectedClassification implements ItemListener{
//		@Override
//		public void itemStateChanged(ItemEvent ie) {
//			if (selectedClassification.getSelectedItem()==defaultItem)
//				difarControl.getDifarParameters().selectedClassification = null;
//			else
//				difarControl.getDifarParameters().selectedClassification = 
//						(LookupItem) selectedClassification.getSelectedItem();
//		}
//	}


	private void enableControls() {
		boolean isViewer = difarControl.isViewer();
	}

	public void deployButton() {
		// need to work out how many channels there are and pop up a menu if there are > 1
		int sourceChannelMap = 0;
		try {
			sourceChannelMap = difarControl.getDifarProcess().getParentDataBlock().getChannelMap();
		}
		catch (NullPointerException e) {
			return;
		}
		int nChan = PamUtils.getNumChannels(sourceChannelMap);
		Streamer streamer = null;
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		//		if (nChan == 0) {
//			ArrayManager.getArrayManager().showArrayDialog(difarControl.getGuiFrame());
//		}
//		else if (nChan == 1) {
//			difarControl.deploy(PamUtils.getSingleChannel(sourceChannelMap));
//		}
//		else {
			JPopupMenu popMenu = new JPopupMenu();
			JMenuItem menuItem = popMenu.add("Show array dialog");
			menuItem.addActionListener(new DeployChannel(-1));
			for (int i = 0; i < nChan; i++) {
				int aChan = PamUtils.getNthChannel(i, sourceChannelMap);
				int streamerInd = array.getStreamerForPhone(aChan);
				streamer = array.getStreamer(streamerInd);
				Double head = streamer.getHeading();
				StreamerDataUnit sdu = ArrayManager.getArrayManager().getStreamerDatabBlock().getLastUnit(1<<aChan);
				String name = sdu.getStreamerData().getStreamerName();
				String deployTime = PamCalendar.formatDateTime2(sdu.getTimeMilliseconds());
				String str = "Deploy channel " + aChan 
								+ " (replaces buoy: " + name  
								+ "@" + deployTime 
								+ " (" + sdu.getGpsData().toString() + ") "
								+ " Mag. Correction: " + head; 
				menuItem = popMenu.add(str);
				menuItem.addActionListener(new DeployChannel(aChan));
			}
			Point p = deploy.getMousePosition();
			if (p == null) {
				p = new Point(deploy.getWidth()/2, deploy.getHeight()/2);
			}
			popMenu.show(deploy, p.x, p.y);
//		}
		
	}
	
	public void buoyEndButton() {
		// need to work out how many channels there are and pop up a menu if there are > 1
		int sourceChannelMap = 0;
		try {
			sourceChannelMap = difarControl.getDifarProcess().getParentDataBlock().getChannelMap();
		}
		catch (NullPointerException e) {
			return;
		}
		int nChan = PamUtils.getNumChannels(sourceChannelMap);
		Streamer streamer = null;
		PamArray array = ArrayManager.getArrayManager().getCurrentArray();
		//		if (nChan == 0) {
//			ArrayManager.getArrayManager().showArrayDialog(difarControl.getGuiFrame());
//		}
//		else if (nChan == 1) {
//			difarControl.deploy(PamUtils.getSingleChannel(sourceChannelMap));
//		}
//		else {
			JPopupMenu popMenu = new JPopupMenu();
			JMenuItem menuItem = new JMenuItem();
//			JMenuItem menuItem = popMenu.add("Show array dialog");
//			menuItem.addActionListener(new DeployChannel(-1));
			for (int i = 0; i < nChan; i++) {
				int aChan = PamUtils.getNthChannel(i, sourceChannelMap);
				int streamerInd = array.getStreamerForPhone(aChan);
				streamer = array.getStreamer(streamerInd);
				StreamerDataUnit sdu = ArrayManager.getArrayManager().getStreamerDatabBlock().getLastUnit(1<<aChan);
				String name = sdu.getStreamerData().getStreamerName();
				if (sdu==null) return;
				String deployTime = PamCalendar.formatDateTime2(sdu.getTimeMilliseconds());
				String str = "End buoy: " + name 
								+ " (ch " + aChan 
								+ " delployed @ " + deployTime;
				menuItem = popMenu.add(str);
				menuItem.addActionListener(new EndBuoyChannel(aChan));
			}
			Point p = deploy.getMousePosition();
			if (p == null) {
				p = new Point(deploy.getWidth()/2, deploy.getHeight()/2);
			}
			popMenu.show(deploy, p.x, p.y);
//		}
		
	}
	
	private class DeployChannel implements ActionListener {

		private int channel;
		
		public DeployChannel(int channel) {
			super();
			this.channel = channel;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {	
			difarControl.sonobuoyManager.deployBuoy(channel);
		}
		
	}
	
	private class EndBuoyChannel implements ActionListener {

		private int channel;
		
		public EndBuoyChannel(int channel) {
			super();
			this.channel = channel;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {	
			difarControl.sonobuoyManager.endBuoy(channel, PamCalendar.getTimeInMillis(), true);
		}
		
	}
	private void startCalibration() {
		// need to work out how many channels there are and pop up a menu if there are > 1
		int sourceChannelMap = 0;
		try {
			sourceChannelMap = difarControl.getDifarProcess().getParentDataBlock().getChannelMap();
		}
		catch (NullPointerException e) {
			return;
		}
		int nChan = PamUtils.getNumChannels(sourceChannelMap);
		if (nChan <= 1) {
			difarControl.getDifarProcess().startBuoyCalibration(PamUtils.getSingleChannel(sourceChannelMap));
//			difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.StartBuoyCalibration));
		}
		else {
			JPopupMenu popupMenu = new JPopupMenu();
			for (int i = 0; i < nChan; i++) {
				JMenuItem menuItem = new JMenuItem("Channel " + PamUtils.getNthChannel(i, sourceChannelMap));
				menuItem.addActionListener(new ChannelCalibrationAction(PamUtils.getNthChannel(i, sourceChannelMap)));
				popupMenu.add(menuItem);
			}
			Point p = startCal.getMousePosition();
			if (p == null) {
				p = new Point(startCal.getWidth()/2, startCal.getHeight()/2);
			}
			popupMenu.show(startCal, p.x, p.y);
		}
	}

	private class ChannelCalibrationAction implements ActionListener {

		int channel;
		@Override
		public void actionPerformed(ActionEvent arg0) {
			difarControl.getDifarProcess().startBuoyCalibration(channel);
//			difarControl.sendDifarMessage(new DIFARMessage(DIFARMessage.StartBuoyCalibration));
		}
		public ChannelCalibrationAction(int channel) {
			super();
			this.channel = channel;
		}

	}

	@Override
	public String getName() {
		return "Difar Control Panel(Actions)";
	}

	@Override
	public Component getComponent() {
		return getComponent(Component.TOP_ALIGNMENT);
	}

	/**
	 * 
	 * @param Orientation
	 * @return component laid out for vertical(Component.LEFT_ALIGNMENT) or horizontal(Component.TOP_ALIGNMENT)
	 */
	public Component getComponent(float Orientation) {
		return vesselPanel;
	}

	@Override
	public int difarNotification(DIFARMessage difarMessage) {
		enableControls();
		return 0;
	}



}
