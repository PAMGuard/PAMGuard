package alfa.logger;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import PamController.PamController;
import PamUtils.PamCalendar;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import PamguardMVC.dataSelector.DataSelector;
import alfa.ALFAControl;
import loggerForms.FormDescription;
import loggerForms.FormsControl;
import loggerForms.FormsDataUnit;

public class LoggerMonitor extends PamProcess {

	
	private ALFAControl alfaControl;
	private DataSelector formsSelector;
	private FormsControl loggerControl;

	public LoggerMonitor(ALFAControl alfaControl) {
		super(alfaControl, null);
		this.alfaControl = alfaControl;
	}
	
	private boolean linkLogger() {
		loggerControl = (FormsControl) PamController.getInstance().findControlledUnit(FormsControl.class, null);
		if (loggerControl == null) {
			return false;
		}
		loggerControl.getFormsMonitor().addObserver(this, true);
		formsSelector = loggerControl.getFormsMonitor().getDataSelector(alfaControl.getUnitName());
		return false;
	}

	@Override
	public void pamStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pamStop() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);
		if (changeType == PamController.INITIALIZATION_COMPLETE) {
			linkLogger();
		}
	}

	public JMenuItem getMenuItem(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem("Logger forms ...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showOptionsMenu(parentFrame);				
			}
		});
		if (loggerControl == null) {
			menuItem.setEnabled(false);
			menuItem.setToolTipText("No logger forms available");
		}
		else {
			menuItem.setToolTipText("Select logger forms for satellite messaging");
		}
		return menuItem;
	}
	
	public boolean showOptionsMenu(Frame frame) {
		if (formsSelector == null) {
			return false;
		}
		return formsSelector.showSelectDialog(frame);
	}
	
	

	@Override
	public void newData(PamObservable o, PamDataUnit arg) {
		FormsDataUnit formsUnit = (FormsDataUnit) arg;
		if (formsSelector.scoreData(formsUnit) > 0) {
			newFormsData(formsUnit);
		}
	}

	private void newFormsData(FormsDataUnit formsUnit) {
//		Debug.out.println(formsUnit.getSummaryString());
		FormDescription desc = formsUnit.getLoggerForm().getFormDescription();
//		Debug.out.println(desc.getXMLData(formsUnit));
//		Debug.out.println(desc.getJSONData(formsUnit));
		String jsonString = desc.getStringData(formsUnit, alfaControl.getAlfaParameters().loggerFormFormat);
		if (jsonString != null) {
			LoggerCommsDataUnit lcdu = new LoggerCommsDataUnit(formsUnit.getTimeMilliseconds(), jsonString, alfaControl.getAlfaParameters().loggerFormFormat);
			alfaControl.getMessageProcess().newData(null, lcdu);
		}
	}

	/**
	 * @return the formsSelector
	 */
	public DataSelector getFormsSelector() {
		return formsSelector;
	}

}
