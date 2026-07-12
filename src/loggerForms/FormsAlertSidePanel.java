package loggerForms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

//import loggerForms.controls.LoggerControl.AutoUpdateAction;

import PamUtils.PamCalendar;
import PamView.PamSidePanel;
import PamView.dialog.PamLabel;
import PamView.panel.PamBorderPanel;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserverAdapter;
import loggerForms.network.LoggerNetworkManager;
import loggerForms.network.LoggerNetworkSystem;

public class FormsAlertSidePanel extends PamObserverAdapter implements PamSidePanel {
	
	
	private FormsControl formsControl;
//	private ArrayList<FormsDataBlock> formsDataBlocks;
	private ArrayList<FormDescription> formDescriptions;
	private FormsAlertPanel formsAlertPanel;
	private PamPanel outerPanel;
	
	private JComponent networkPanel;
	
	public static  Color warningColour = new Color(255, 117, 117);

	
	public FormsAlertSidePanel(FormsControl formsControl) {
		super();
		
		outerPanel = new PamPanel(new BorderLayout());
//
//		float[] col = Color.RGBtoHSB(255, 117, 117, null);		
//		warningColour = Color.getHSBColor(col[0], col[1], col[2]);
		
		this.formsControl = formsControl;
		formDescriptions= new ArrayList<FormDescription>();
//		formsDataBlocks=new ArrayList<FormsDataBlock>();
		formsAlertPanel=new FormsAlertPanel();
		
		outerPanel.add(BorderLayout.NORTH, formsAlertPanel);
		
		checkNetworkPanel();
		
		setupAutoUpdate();
	}
	
	/**
	 * Check to see if there is a panel from the Network system. 
	 */
	public void checkNetworkPanel() {
		JComponent netComponent =  null;
		LoggerNetworkManager netManager = LoggerNetworkSystem.getManager();
		if (netManager != null) {
			netComponent = netManager.getSideComponent();
		}
		if (netComponent == networkPanel) {
			return;
		}
		if (networkPanel != null) {
			outerPanel.remove(networkPanel);
		}
		networkPanel = netComponent;
		outerPanel.add(networkPanel, BorderLayout.SOUTH);
	}

	@Override
	public void addData(PamObservable o, PamDataUnit arg) {
		// TODO Auto-generated method stub
//		FormsDataUnit fdu=(FormsDataUnit)arg;
//		fdu.getTimeMilliseconds();
		
		for (int iForm=0;iForm<formsControl.getNumFormDescriptions();iForm++){
			
			formsControl.getFormDescription(iForm).setTimeOfNextSave();
		}
		
		formsAlertPanel.setTimesLeft();
		
	}
	
	/**
	 * Set up an autoupdate timer. 
	 */
	private void setupAutoUpdate() {
		int updateTime = 1000;
		Timer autoUpdateTimer = new Timer(updateTime, new AutoUpdateAction());
		autoUpdateTimer.start();
	}
	
	/**
	 * Class for auto update action listener. 
	 * @author Doug Gillespie
	 *
	 */
	private class AutoUpdateAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			formsAlertPanel.setTimesLeft();
		}
	}

	@Override
	public String getObserverName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public JComponent getPanel() {
//		if (){
//			
//		}
		return outerPanel;
	}

	@Override
	public void rename(String newName) {
		// TODO Auto-generated method stub

	}
	
	
	
	private void updateDataBlocks(){
		
		formDescriptions.clear();
		
		for (int iForm=0;iForm<formsControl.getNumFormDescriptions();iForm++){
			
			Integer autoalert = formsControl.getFormDescription(iForm).getAUTOALERT();
//			System.out.println(iForm+"th iform alert="+autoalert);
			if (!(autoalert==null)){
				FormDescription fd = formsControl.getFormDescription(iForm);
				formDescriptions.add(fd);
				FormsDataBlock datablock = fd.getFormsDataBlock();
				datablock.addObserver(this);
//				timesBetweenUpdates.add(autoalert);
			}
		}
		
		
		
	}
	
	class FormsAlertPanel extends PamBorderPanel{
		
		PamBorderPanel alertsPanel = new PamBorderPanel();
		TitledBorder titledBorder;
		JLabel[] formNameLabel;
		JTextField[] timesLeft;
		
		FormsAlertPanel(){
			super();
			setBorder(titledBorder = new TitledBorder("Forms Alerts"));
		}

		/**
		 * @return the timesLeft
		 */
		public JTextField[] getTimesLeft() {
			return timesLeft;
		}
		
		public void updateFormsShowing(){
			alertsPanel.removeAll();
			setLayout(new BorderLayout());
			alertsPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			updateDataBlocks();
			formNameLabel=new PamLabel[formDescriptions.size()];
			timesLeft = new JTextField[formDescriptions.size()];
			for (int pos=0;pos<formDescriptions.size();pos++){
				
				c.gridy=pos;
				c.gridx=0;
				String buttonName = formDescriptions.get(pos).getFormNiceName() + " ";
//				addComponent(alertsPanel, formNameLabel[pos]=new PamButton(buttonName), c);
				addComponent(alertsPanel, formNameLabel[pos]=new PamLabel(buttonName), c);
				c.gridx=1;
				addComponent(alertsPanel, timesLeft[pos]= new JTextField(8) , c);
				timesLeft[pos].setFocusable(false);
				
			}
			add(BorderLayout.NORTH, alertsPanel);
			invalidate();
			setVisible(formDescriptions.size() > 0);
		}

		/**
		 * @param timesLeft the timesLeft to set
		 */
		public void setTimesLeft() {
			long timeNow = PamCalendar.getTimeInMillis();
			for (int fd=0;fd<formDescriptions.size();fd++){
//				Long alertTime = (long) (fd.getAUTOALERT()*60*1000);
				long timeLeft = formDescriptions.get(fd).getTimeOfNextSave()-timeNow;
				
				if (timeLeft<0){
//					float[] col = Color.RGBtoHSB(255, 117, 117, null);
//					
//					timesLeft[fd].setBackground(Color.getHSBColor(col[0], col[1], col[2]));
					timesLeft[fd].setBackground(warningColour);
				}else{
					timesLeft[fd].setBackground(null);
				}
				
				timesLeft[fd].setText(PamCalendar.formatTime(Math.abs(timeLeft)));
				
			}
			
		}
		
	}

	/**
	 * @return the formsAlertPanel
	 */
	public FormsAlertPanel getFormsAlertPanel() {
		return formsAlertPanel;
	}

}
