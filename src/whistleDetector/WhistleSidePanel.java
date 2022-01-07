package whistleDetector;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import PamUtils.PamCalendar;
import PamView.PamColors;
import PamView.PamSidePanel;
import PamView.PamColors.PamColor;
import PamView.dialog.PamLabel;
import PamView.panel.PamBorderPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;

public class WhistleSidePanel extends PamObserverAdapter implements PamSidePanel {

	WhistleControl whistleControl;
	
	SidePanel sidePanel;
	
	TitledBorder titledBorder;
	
	JLabel timeLabel;
	JTextField eventCount, whistleCount;
	
	int countTime = 10; // time in minutes
	
	Timer timer;
	

	public WhistleSidePanel(WhistleControl whistleControl) {
		
		this.whistleControl = whistleControl;
		
		sidePanel = new SidePanel();
		
		timer = new Timer(2000, new TimerEvent());
		
		timer.start();
		
		whistleControl.whistleDetector.whistleDataBlock.addObserver(this);
		whistleControl.eventDetector.eventDataBlock.addObserver(this);
	}

	private class SidePanel extends PamBorderPanel {

		public SidePanel() {
			super();
			setBorder(titledBorder = new TitledBorder(whistleControl.getUnitName()));

			titledBorder.setTitleColor(PamColors.getInstance().getColor(PamColor.AXIS));

			GridBagLayout gb = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			setLayout(gb);
			
			timeLabel = new PamLabel(String.format("Detections in last %d minutes", countTime));
			eventCount = new JTextField(5);
			whistleCount = new JTextField(5);
			eventCount.setEditable(false);
			whistleCount.setEditable(false);
			
			c.anchor = GridBagConstraints.EAST;
			c.ipadx = 5;
			c.gridx = c.gridy = 0;
			c.gridwidth = 2;
			addComponent(this, timeLabel, c);
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy ++;
			addComponent(this, new PamLabel("Whistles"), c);
			c.gridx ++;
			addComponent(this, whistleCount, c);
			c.gridx = 0;
			c.gridy ++;
			addComponent(this, new PamLabel("Whistle Events"), c);
			c.gridx ++;
			addComponent(this, eventCount, c);
		}
		
		@Override
		public void setBackground(Color bg) {
			super.setBackground(bg);
			if (titledBorder != null) {
				titledBorder.setTitleColor(PamColors.getInstance().getColor(PamColor.AXIS));
			}
		}

		private void fillData() {
			long countStart = PamCalendar.getTimeInMillis() - countTime * 60000;
			PamDataBlock dataBlock = whistleControl.whistleDetector.whistleDataBlock;
			if (dataBlock != null) {
			  int nWhistles = dataBlock.getUnitsCountFromTime(countStart);
			  whistleCount.setText(String.format("%d", nWhistles));
			}
			dataBlock = whistleControl.eventDetector.eventDataBlock;
			if (dataBlock != null) {
				  int nEvents = dataBlock.getUnitsCountFromTime(countStart);
				  eventCount.setText(String.format("%d", nEvents));
			}
		}
	}

	public JComponent getPanel() {
		return sidePanel;
	}

	public void rename(String newName) {
		titledBorder.setTitle(newName);	
		sidePanel.repaint();		
	}
	
	class TimerEvent implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			sidePanel.fillData();	
		}
	}

	public String getObserverName() {
		return "Whistle side panel";
	}

	public long getRequiredDataHistory(PamObservable o, Object arg) {
		return countTime * 60000;
	}

	
}
