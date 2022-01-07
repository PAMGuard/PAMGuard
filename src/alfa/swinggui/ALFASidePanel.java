package alfa.swinggui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamController.status.ModuleStatus;
import PamView.PamSidePanel;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.dialog.PamButton;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.panel.PamPanel;
import PamguardMVC.debug.Debug;
import alfa.ALFAControl;
import alfa.effortmonitor.AngleHistogram;
import alfa.status.swing.StatusButton;
import alfa.status.swing.StatusButtonSquare;

/**
 * The side pane holds status info and a big button to start and stop PAMGuard. 
 * @author Jamie Macaulay
 *
 */
public class ALFASidePanel implements PamSidePanel {

	private ALFAControl alfaControl;
	
	private JPanel mainPanel;
	
	private static String[] whaleButtonNames = {"Ahead", "Abeam", "Astern"};
	private StatusButton[] whaleStatus = new StatusButton[whaleButtonNames.length];
	private StatusButton systemStatus;

	private PamPanel mainHolder;

	/**
	 * The button to start and stop PAMGuard. 
	 */
	private JButton startStopButton;
	
	private static final int BUTTONSIZE = 40;
	
	private static final int BUTTONWIDTH = 220;
	

	public ALFASidePanel(ALFAControl alfaControl) {
		
		
		this.alfaControl = alfaControl;
		
		//button 
		mainHolder = new PamPanel(new BorderLayout()); 
		startStopButton = new JButton();
		startStopButton.setPreferredSize(new Dimension(BUTTONWIDTH, ALFAGUITransformer.TABHEIGHT));
		startStopButton.setHorizontalAlignment(SwingConstants.CENTER);

		startStopButton.addActionListener((action )->{
			if (PamController.getInstance().getPamStatus() == PamController.PAM_RUNNING) {
//				Debug.out.println("ALFA: PAMStop Pressed!!!--------------------");
				PamController.getInstance().toolBarStopButton(null);
			}
			else if (PamController.getInstance().getPamStatus() == PamController.PAM_IDLE) {
//				Debug.out.println("ALFA: PAMStart Pressed!!!--------------------");
				PamController.getInstance().pamStart();
			}
			//button decoration occurs on notification 
		});

		setStartStopStatus(false); 
		
		//the status pane
		Color clearColour = new Color(0,0,0,0);
		for (int i = 0; i < whaleButtonNames.length; i++) {
			whaleStatus[i] = new StatusButton(whaleButtonNames[i], new Dimension(BUTTONWIDTH,  ALFAGUITransformer.TABHEIGHT/2));
			whaleStatus[i].setColor(0, clearColour);
			whaleStatus[i].setStatus(true, new ModuleStatus(ModuleStatus.STATUS_OK));
		}
		
		
//		whaleStatus = new StatusButton("   Whales");
		systemStatus = new StatusButtonSquare("System Diagnostics",new Dimension(BUTTONWIDTH,  ALFAGUITransformer.TABHEIGHT));

		mainPanel = new PamPanel();
		mainPanel.setLayout(new GridBagLayout());
		//mainPanel.setBorder(new TitledBorder("Status"));
		GridBagConstraints c = new PamGridBagContraints();
		
//		mainPanel.add(new PamView.dialog.PamLabel("System", JLabel.CENTER),c);
//		c.gridx++;
//		mainPanel.add(new PamView.dialog.PamLabel("Whales", JLabel.CENTER),c);
//		c.gridx = 0;
//		c.gridy++;
		c.gridx = 0;
		c.gridwidth=3;
		c.gridheight=3; 
		if (!alfaControl.isViewer()) mainPanel.add(systemStatus.getComponent(),c);
		
		c.gridwidth=1;
		c.gridheight=1; 
		c.gridx = 2;
		c.gridy ++;
		c.gridy ++;
		mainPanel.add(new JLabel("Sperm Whale Detections"),c);
		c.gridy ++;
		mainPanel.add(whaleStatus[0].getComponent(),c);
		c.gridy ++;
		mainPanel.add(whaleStatus[1].getComponent(),c);
		c.gridy ++;
		mainPanel.add(whaleStatus[2].getComponent(),c);
		
		//advanced label
		c.gridy ++;
		c.gridwidth=3;
		mainPanel.add(new JSeparator(),c);
		
		PamLabel advLabel = new PamLabel("System Details", SwingConstants.CENTER); 
		//advLabel.setPreferredSize(new Dimension(BUTTONWIDTH, 2*BUTTONSIZE));
		advLabel.setFont(new Font(null, Font.BOLD, 20));
		advLabel.setVerticalTextPosition(JLabel.BOTTOM);
		
		if (!alfaControl.isViewer()) mainHolder.add(BorderLayout.NORTH, startStopButton); 
		mainHolder.add(BorderLayout.CENTER, mainPanel); 
		if (!alfaControl.isViewer()) mainHolder.add(BorderLayout.SOUTH, advLabel); 
	}
	
	/**
	 * Set the status of the go button. 
	 * @param running 
	 */
	public synchronized void setStartStopStatus(boolean running) {
		//Debug.out.println("ALFA:p Run: !!" + running);
		startStopButton.setFont(new Font(null, Font.BOLD, 20));
		if (!running) {
			startStopButton.setText("Press to Start");
//			startStopButton.setBackground(Color.GREEN);
//			startStopButton.setOpaque(true);
			//startStopButton.setBorderPainted(false);		
//			startStopButton.setIcon( new PamSymbol(PamSymbolType.SYMBOL_TRIANGLER, 25, 25, true, 
//					Color.BLACK, Color.BLACK));	
			
			startStopButton.setBorder(BorderFactory.createLineBorder(Color.GREEN, 5)); // Line Border + Thickness of the Border		}
			startStopButton.setIconTextGap(1);
			}
		else {
			startStopButton.setText("Press to Stop");
//			startStopButton.setOpaque(true);
//			startStopButton.setBackground(Color.GRAY);
//			startStopButton.setIcon( new PamSymbol(PamSymbolType.SYMBOL_SQUARE, 25, 25, true, 
//					Color.BLACK, Color.BLACK));
			startStopButton.setBorder(BorderFactory.createLineBorder(Color.GRAY, 5)); // Line Border + Thickness of the Border		}

		}
		startStopButton.repaint();
		startStopButton.validate();
		startStopButton.repaint();

	}

	@Override
	public JComponent getPanel() {
		return mainHolder;
	}

	@Override
	public void rename(String newName) {
		// TODO Auto-generated method stub
		
	}

	public void statusUpdate() {
		systemStatus.setStatus(true, alfaControl.getModuleStatus());
		
		AngleHistogram aHist = alfaControl.getStatusAngleHistogram();
		if (aHist != null) {
			double[] data = aHist.getData();
			double front = data[0];
			double aft = data[data.length-1];
			double abeam = aHist.getTotalContent()-front-aft;
			setButtonState(0, front);
			setButtonState(1, abeam);
			setButtonState(2, aft);
		}
//		whaleStatus[0].setStatus(true, alfaControl.getWhaleStatus());
	}

	/**
	 * Set the state of the button. 
	 * @param i - the state of the button. 
	 * @param nClickTrains - the number of click trains. 
	 */
	private void setButtonState(int i, double nClickTrains) {
		StatusButton button = whaleStatus[i];
		int state = ModuleStatus.STATUS_OK;
		String msg;
		if (nClickTrains >= 1) {
			state = ModuleStatus.STATUS_WARNING;
		}
		if (nClickTrains >= 3) {
			state = ModuleStatus.STATUS_ERROR;
		}
		msg = String.format("%d click trains detected %s", (int)nClickTrains, whaleButtonNames[i].toLowerCase());
		
		button.setStatus(true, new ModuleStatus(state, msg));
	}

	/**
	 * Notification passed from controller to side panel 
	 * @param changeType - the change type flag. 
	 */
	public synchronized void notifyModelChanged(int changeType) {
		//Debug.out.println("ALFA SIDE PANE: " + changeType);
		switch (changeType) {
		case PamController.PAM_RUNNING:
			setStartStopStatus(true); 
			break;
		case PamController.PAM_IDLE:
			setStartStopStatus(false); 
			break;
		case PamController.PAM_STALLED:
			setStartStopStatus(false); 
			break; 
		}
		
	}

}
