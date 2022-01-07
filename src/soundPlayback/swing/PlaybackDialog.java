package soundPlayback.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamDetection.RawDataUnit;
import PamView.dialog.PamDialog;
import PamguardMVC.PamRawDataBlock;
import soundPlayback.PlaybackChangeObserver;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackParameters;
import soundPlayback.PlaybackSystem;

/**
 * Options for sound playback
 * @author Doug
 *
 */
public class PlaybackDialog extends PamDialog implements PlaybackChangeObserver{

	private static PlaybackDialog singleInstance;
	private PlaybackParameters playbackParameters;
	
	private PlaybackSourcePanel sourcePanel;
	
	private PlaybackControl playbackControl;
	
	private PlaybackSystem playbackSystem;
	
	private int maxChannels = 2;
	
	private JPanel dialogPanel;
	
	private Component dialogComponent;
	
	private PlaybackDialogComponent playbackDialogComponent;
	
	private PlaybackSideOptionsPanel sideOptionsPanel;
	
	private PlaybackDialog(Frame parentFrame, PlaybackControl playbackControl) {
		super(parentFrame, "Sound Playback Options", false);
		this.playbackControl = playbackControl;
		sourcePanel = new PlaybackSourcePanel(this, "Data source", RawDataUnit.class, true, true);
		dialogPanel = new JPanel();
		dialogPanel.setLayout(new BorderLayout());
		dialogPanel.add(BorderLayout.CENTER, sourcePanel.getPanel());
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Playback", dialogPanel);
		
		sideOptionsPanel = new PlaybackSideOptionsPanel(playbackControl);
		tabbedPane.addTab("Side Bar", sideOptionsPanel.getDialogComponent());
		
		setDialogComponent(tabbedPane);
		setHelpPoint("sound_processing.soundPlaybackHelp.docs.soundPlayback_soundPlayback");
		sourcePanel.addSelectionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sourceChanged();
			}
		});
		// TODO Auto-generated constructor stub
	}
	public static PlaybackParameters showDialog(Frame parentFrame, 
			PlaybackParameters playbackParameters, PlaybackControl playbackControl) {
		
		if (singleInstance == null || 
				parentFrame != singleInstance.getOwner() || 
				singleInstance.playbackControl != playbackControl) {
			singleInstance = new PlaybackDialog(parentFrame, playbackControl);
		}
		singleInstance.playbackParameters = playbackParameters.clone();
		singleInstance.playbackControl = playbackControl;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.playbackParameters;
	}
	
	private void setParams() {
		PamRawDataBlock pamRawDataBlock = PamController.getInstance().getRawDataBlock(playbackParameters.dataSource);
		sourcePanel.setSource(pamRawDataBlock);
		sourcePanel.setChannelList(playbackParameters.channelBitmap);
		if (playbackDialogComponent != null) {
			playbackDialogComponent.setParams(playbackParameters);
			playbackDialogComponent.setParentDialog(this);
		}
		sourceChanged();
		sideOptionsPanel.setParams(playbackParameters);
	}
	
	@Override
	public void cancelButtonPressed() {

		playbackParameters = null;

	}

	@Override
	public boolean getParams() {
		playbackParameters.channelBitmap = sourcePanel.getChannelList();
		playbackParameters.dataSource = sourcePanel.getSourceIndex();
		if (playbackDialogComponent != null) {
			PlaybackParameters nP = playbackDialogComponent.getParams(playbackParameters);
			if (nP != null) {
				playbackParameters = nP.clone();
			}
		}
		boolean ok = sideOptionsPanel.getParams(playbackParameters);
		return ok;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

	protected void selectionChanged(int channel) {
		
		enableChannelBoxes();
	}
	
	protected void sourceChanged() {
		playbackSystem = playbackControl.findPlaybackSystem(sourcePanel.getSource());
		if (playbackSystem != null) {
			playbackSystem.addChangeObserver(this);
			maxChannels = playbackSystem.getMaxChannels();
		}
//		maxChannels = playbackControl.getMaxPlaybackChannels(playbackSystem);
		enableChannelBoxes();
		sortDialogComponent();
		if (playbackDialogComponent != null) {
			playbackDialogComponent.dataSourceChanged(sourcePanel.getSource());
		}
	}
	
	private void sortDialogComponent() {
		Object oldComponent = dialogComponent;
		if (dialogComponent != null) {
			dialogPanel.remove(dialogComponent);
			dialogComponent = null;
		}
		if (playbackSystem != null) {
			playbackDialogComponent = playbackSystem.getDialogComponent();
			if (playbackDialogComponent == null) {
				playbackDialogComponent = new EmptyDialogComponent();
			}
			dialogComponent = playbackDialogComponent.getComponent();
			if (dialogComponent != null) {
				dialogPanel.add(BorderLayout.SOUTH, dialogComponent);
				playbackDialogComponent.setParams(playbackParameters);
			}

		}
		else {
			playbackDialogComponent = new EmptyDialogComponent();
			dialogPanel.add(BorderLayout.SOUTH, dialogComponent = playbackDialogComponent.getComponent());
		}
		//		if (oldComponent != dialogComponent) {
		invalidate();
		pack();
		//		}
	}
	
	private void enableChannelBoxes() {
		/*
		 * do two checks 1) that no more than max chnnels are checked 
		 * 2) uncheck any boxes that exceed this limit. 
		 */
		JCheckBox channelBoxes[] = sourcePanel.getChannelBoxes();
		int totalChecked = 0;
		for (int i = 0; i < channelBoxes.length; i++) {
			if (channelBoxes[i].isVisible() == false) {
				channelBoxes[i].setSelected(false);
			}
			else {
				if (channelBoxes[i].isSelected()) {
					if (totalChecked >= maxChannels) {
						channelBoxes[i].setSelected(false);
					}
					totalChecked++;
				}
			}
		}
		for (int i = 0; i < channelBoxes.length; i++) {
			if (channelBoxes[i].isVisible() == false) continue;
			if (totalChecked < maxChannels) {
				channelBoxes[i].setEnabled(true);
			}
			else if (channelBoxes[i].isSelected() == false) {
				channelBoxes[i].setEnabled(false);
			}
		}
		
	}
	
	private class EmptyDialogComponent extends PlaybackDialogComponent {

		JPanel panel;
		
		public EmptyDialogComponent() {
			super();

			panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.setBorder(new TitledBorder(""));
			if (playbackSystem == null) {
				panel.add(BorderLayout.CENTER, new JLabel(" Playback not possible with current input"));
			}
			else {
				panel.add(BorderLayout.CENTER, new JLabel(" Playback through " + playbackSystem.getName()));
			}
			
		}

		@Override
		Component getComponent() {
			return panel;
		}

		@Override
		PlaybackParameters getParams(PlaybackParameters playbackParameters) {
			return null;
		}

		@Override
		void setParams(PlaybackParameters playbackParameters) {
			
		}
		
	}

	@Override
	public void playbackChange() {
		if (playbackSystem != null) {
			maxChannels = playbackSystem.getMaxChannels();
			enableChannelBoxes();
		}
//		maxChannels = playbackControl.getMaxPlaybackChannels(playbackSystem);
	}

}
