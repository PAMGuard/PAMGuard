package clickDetector;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import PamUtils.PamCalendar;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.PamSidePanel;
import PamView.dialog.PamLabel;
import PamView.panel.PamBorderPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;

public class ClickSidePanel extends PamObserverAdapter implements PamSidePanel {

	ClickControl clickControl;
	
	SidePanel sidePanel;
	
	TitledBorder titledBorder;
	
	JLabel timeLabel, alarmTxt;
	JTextField eventCount, clickCount;
    JButton testAlarm;
	
	int countTime = 1; // time in minutes
	
	Timer timer;
	
	PamDataBlock clickDataBlock;
	
	PamDataBlock clickTrainDataBlock;

    public static final String NOALARMSOUND = "(No Alarm Sounding)";

	public ClickSidePanel(ClickControl clickControl) {
		
		this.clickControl = clickControl;
		
		sidePanel = new SidePanel();
		
		timer = new Timer(2, new TimerEvent());
		
		timer.start();

		noteNewSettings();
		
	}
	
    /**
     * The actual side panel.  Note that this inner class has been changed from
     * private to public so that ClickAlarmManager can access the alarmTxtAlert
     * method.  Can't use the ClickSidePanel.getPanel method to return an instance of the
     * SidePanel object because getPanel() returns a JComponent, not a SidePanel.
     */
	public class SidePanel extends PamBorderPanel implements ActionListener {

		public SidePanel() {
			super();
			setBorder(titledBorder = new TitledBorder(clickControl.getUnitName()));
			titledBorder.setTitleColor(PamColors.getInstance().getColor(PamColor.AXIS));

			GridBagLayout gb = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			setLayout(gb);
			
			timeLabel = new PamLabel(String.format("Detections in last %d minutes", countTime));
			eventCount = new JTextField(5);
			clickCount = new JTextField(5);
            alarmTxt = new PamLabel(NOALARMSOUND);
			eventCount.setEditable(false);
			clickCount.setEditable(false);
            testAlarm = new JButton("Test Alarm");
			
			c.anchor = GridBagConstraints.EAST;
			c.ipadx = 5;
			c.gridx = c.gridy = 0;
			c.gridwidth = 2;
			addComponent(this, timeLabel, c);
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy ++;
			addComponent(this, new PamLabel("Clicks"), c);
			c.gridx ++;
			addComponent(this, clickCount, c);
			c.gridx = 0;
			c.gridy ++;
			addComponent(this, new PamLabel("Click Events"), c);
			c.gridx ++;
			addComponent(this, eventCount, c);
//            c.gridx = 0;
//            c.gridy ++;
//            c.gridwidth = 2;
//            c.anchor = GridBagConstraints.LINE_START;
//            c.insets = new Insets(20, 0, 0, 0);
//            addComponent(this, testAlarm, c);
//            testAlarm.setEnabled(true);
//            testAlarm.addActionListener(this);
//            c.insets = new Insets(10, 0, 0, 0);
//            c.gridy++;
//            addComponent(this, alarmTxt, c);
//            alarmTxtAlert(null);
		}
		
        /**
         * Cycle through all available alarms, playing each one
         *
         * @param e
         */
        @Override
		public void actionPerformed(ActionEvent e) {
//            int[] codeList = clickControl.getClickIdentifier().getCodeList();
//            ClickTypeCommonParams commonParams = null;
//            for (int i=0; i<codeList.length; i++) {
//                commonParams = clickControl.getClickIdentifier().getCommonParams(codeList[i]);
//                alarmTxtAlert(commonParams. -- cant access name --- )
//            }
        	if (clickControl==null||clickControl.clickParameters==null||clickControl.clickParameters.clickAlarmList==null) return;
            for (ClickAlarm alarm : clickControl.clickParameters.clickAlarmList) {
               alarmTxtAlert(alarm);
               clickControl.clickAlarmManager.playIt(alarm);
               try {
                   Thread.sleep(1000);
               } catch (Exception ex) {
                   System.out.println("Error testing sounds");
               }
            }
            alarmTxtAlert(null);
        }

        /**
         * Changes the label below the 'test alarm' button to indicate which,
         * if any, alarm is currently sounding.
         *
         * @param clickAlarm the alarm being sounded.  Null indicates no alarm
         */
        public void alarmTxtAlert(ClickAlarm clickAlarm) {
            if (clickAlarm==null) {
                alarmTxt.setFont(alarmTxt.getFont().deriveFont(Font.ITALIC));
                alarmTxt.setForeground(Color.BLACK);
                alarmTxt.setText(NOALARMSOUND);
            } else {
                alarmTxt.setFont(alarmTxt.getFont().deriveFont(Font.PLAIN));
                alarmTxt.setForeground(Color.RED);
                alarmTxt.setText(clickAlarm.getName());
            }
           alarmTxt.paintImmediately(alarmTxt.getVisibleRect());
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
			if (clickDataBlock != null) {
			  int nClicks = clickDataBlock.getUnitsCountFromTime(countStart);
			  clickCount.setText(String.format("%d", nClicks));
			}

			if (clickTrainDataBlock != null) {
				  int nEvents = clickTrainDataBlock.getUnitsCountFromTime(countStart);
				  eventCount.setText(String.format("%d", nEvents));
			}
		}
	}

	@Override
	public JComponent getPanel() {
		return sidePanel;
	}

	@Override
	public void rename(String newName) {
		titledBorder.setTitle(newName);	
		sidePanel.repaint();		
	}
	
	class TimerEvent implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			sidePanel.fillData();	
		}
	}

	@Override
	public String getObserverName() {
		return clickControl.getUnitName() + " side panel";
	}

	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		return countTime * 60000;
	}

	@Override
	public void noteNewSettings() {
		clickDataBlock = clickControl.clickDetector.getClickDataBlock();
		clickDataBlock.addObserver(this);
		if (clickControl.clickTrainDetector != null) {
			if ((clickTrainDataBlock = clickControl.clickTrainDetector.getTrackedClickDataBlock()) != null) {
				clickControl.clickTrainDetector.getTrackedClickDataBlock().addObserver(this);
			}
		}
	}

}
