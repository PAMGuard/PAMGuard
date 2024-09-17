/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



package rockBlock;

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
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import PamUtils.PamCalendar;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.PamSidePanel;
import PamView.dialog.PamButton;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.panel.PamBorderPanel;

/**
 * @author mo55
 *
 */
public class RockBlockSidePanel implements PamSidePanel{

	
	private RockBlockControl rbc;
	private RockBlockStatus rbStatus;
	private SidePanel sidePanel;
	private TitledBorder titledBorder;
	private JTextField message = new JTextField(15);
	private JButton send = new PamButton("Send");
	private JButton resetRockBlock = new PamButton("Reset");
	private JLabel comPort = new PamLabel();
	private JLabel commReady = new PamLabel();
	private JLabel portError = new PamLabel();
	private JLabel signalStrength = new PamLabel();
	private JLabel numOutgoing = new PamLabel();
	private JLabel numIncoming = new PamLabel();
	private Timer commTimer;
	private Color normalColor;

	

	/**
	 * 
	 */
	public RockBlockSidePanel(RockBlockControl rbc) {
		this.rbc = rbc;
		this.rbStatus = rbc.getRockBlockStatus();
		sidePanel = new SidePanel();
		commTimer = new Timer(5000, new CommTimerAction());
		commTimer.start();
		
		normalColor = commReady.getForeground();
		
		message.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				messageAction(e);
			}
		});
	}

	protected void messageAction(ActionEvent e) {
//		System.out.println(e);;
		sendTheMessage();
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
	
	/**
	 * 
	 */
	protected void sendTheMessage() {
		/**
		 * Format in same way that we're sending the ALFA controls
		 */
		String msg = message.getText();
		if (msg == null || msg.length() == 0) {
			return;
		}
		long t = PamCalendar.getTimeInMillis();
		String str = String.format("$PGMSG,1,%s,%s,%s", 
				PamCalendar.formatDate2(t,false), PamCalendar.formatTime2(t, 0, false),
				msg);
		rbc.sendText(str);
		message.setText(null);
	}
	
	public class SidePanel extends PamBorderPanel {

		public SidePanel() {
			super();
			setBorder(titledBorder = new TitledBorder(rbc.getUnitName()));
			titledBorder.setTitleColor(PamColors.getInstance().getColor(PamColor.AXIS));

			this.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			c.gridwidth = 2;
			this.add(message, c);
			message.setAlignmentX(LEFT_ALIGNMENT);
			
			c.gridx = 0;
			c.gridwidth = 1;
			c.gridy++;
			this.add(send, c);
//			send.setHorizontalAlignment(SwingConstants.LEFT);
//			send.setAlignmentX(LEFT_ALIGNMENT);
			send.addActionListener(new ActionListener() {
                    @Override
					public void actionPerformed(ActionEvent evt) {
                        sendTheMessage();
                    }
			});
			c.gridx++;
			this.add(resetRockBlock, c);
//			checkSignal.setHorizontalAlignment(SwingConstants.LEFT);
//			resetRockBlock.setAlignmentX(LEFT_ALIGNMENT);
			resetRockBlock.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					resetRBCommObject();
				}
			});

			c.gridy++;
			c.gridx = 0;
			JLabel aLabel;
			this.add(aLabel = new JLabel("Com Port: ", SwingConstants.RIGHT),c);
			c.gridx++;
			this.add(comPort, c);

			c.gridy++;
			c.gridx = 0;
			add(aLabel = new JLabel("Com Ready: ", SwingConstants.RIGHT),c);
			c.gridx++;
			add(commReady,c);

			c.gridy++;
			c.gridx = 0;
			this.add(aLabel = new JLabel("Com Error: ", SwingConstants.RIGHT),c);
			c.gridx++;
			this.add(portError,c);

			c.gridy++;
			c.gridx = 0;
			this.add(aLabel = new JLabel("Signal Strength: ", SwingConstants.RIGHT),c);
			c.gridx++;
			this.add(signalStrength,c);

			c.gridy++;
			c.gridx = 0;
			this.add(aLabel = new JLabel("Num Outgoing Mess: ", SwingConstants.RIGHT),c);
			c.gridx++;
			this.add(numOutgoing,c);

			c.gridy++;
			c.gridx = 0;
			this.add(aLabel = new JLabel("Num Incoming Mess: ", SwingConstants.RIGHT),c);
			c.gridx++;
			this.add(numIncoming,c);
			
			this.repaint();
			
		}

		/**
		 * 
		 */
		protected void resetRBCommObject() {
			rbc.resetRockBlock();
		}


		
		protected void updateStatus() {
			comPort.setText(rbStatus.getComPort());
			if (rbStatus.isCommReady()) {
				commReady.setText("Yes");
				commReady.setForeground(normalColor);
				Font f = commReady.getFont();
				commReady.setFont(f.deriveFont(f.getStyle() & ~Font.BOLD));
			} else {
				commReady.setText("No");
				commReady.setForeground(Color.RED);
				Font f = commReady.getFont();
				commReady.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
			}
			String err = rbStatus.getPortError();
			if (err==null) {
				err = "None";
				portError.setForeground(normalColor);
			} else {
				err = err.substring(0, Math.min(err.length(), 20));
				portError.setForeground(Color.RED);
			}
			portError.setText(err);
			signalStrength.setText(String.valueOf(rbStatus.getSignalStrength()));
			numOutgoing.setText(String.valueOf(rbStatus.getNumOutgoingMess()));
			numIncoming.setText(String.valueOf(rbStatus.getNumIncomingMess()));
		}
	}

	/**
	 * Private timer class that performs status checks every so often
	 * 
	 * @author mo55
	 *
	 */
	private class CommTimerAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			sidePanel.updateStatus();
		}
	}


}
