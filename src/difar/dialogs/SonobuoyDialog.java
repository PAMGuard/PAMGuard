package difar.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import Array.ArrayManager;
import Array.PamArray;
import Array.Streamer;
import Array.StreamerDataUnit;
import Array.StreamerDialog;
import PamUtils.PamCalendar;
import PamView.dialog.PamLabel;
import PamView.dialog.PamTextField;
import PamView.panel.PamBorder;
import PamView.panel.PamBorderPanel;
import annotation.string.StringAnnotation;
import annotation.timestamp.TimestampAnnotation;
import difar.DifarControl;
import difar.SonobuoyManager;

public class SonobuoyDialog extends StreamerDialog {

	private DifarControl difarControl;
	private SonobuoyManager buoyManager;
	private PamBorderPanel addonPanel;
	private PamTextField deployTime, endTime, action, calibration, calStdDev;
	private StreamerDataUnit streamerDataUnit;
	protected static SonobuoyDialog singleInstance;
	
	public SonobuoyDialog(Window parentFrame) {
		super(parentFrame);
		addonPanel = new PamBorderPanel(new GridBagLayout());
		addonPanel.setBorder(new TitledBorder("Sonobuoy deployment"));
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		
//		addonPanel.add(new JLabel("Action: ", SwingConstants.LEFT),c);
//		c.gridx++;
//		c.gridwidth = c.REMAINDER;
//		addonPanel.add(action = new JTextField(20),c);

		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		addonPanel.add(new PamLabel("Deploy time: ",SwingConstants.LEFT),c);
		c.gridx+=1;
		c.gridwidth = c.REMAINDER;
		addonPanel.add(deployTime = new PamTextField(20),c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		addonPanel.add(new PamLabel("End time: ",SwingConstants.LEFT),c);
		c.gridx+=1;
		c.gridwidth = c.REMAINDER;
		
		addonPanel.add(endTime = new PamTextField(20),c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
//		addonPanel.add(new JLabel("Calibration: ",SwingConstants.LEFT),c);
//		c.gridx++;
//		addonPanel.add(calibration = new JTextField(5),c);
//		c.gridx++;
//		addonPanel.add(new JLabel("° +/- ",SwingConstants.CENTER),c);
//		c.gridx++;
//		addonPanel.add(calStdDev= new JTextField(5),c);
//		c.gridx++;
//		addonPanel.add(new JLabel("°",SwingConstants.LEFT),c);
//		c.gridx++;
//		addonPanel.add(new JLabel(" ",SwingConstants.LEFT),c);
//		c.gridy++;
//		c.gridx = 0;
		getMainPanel().add(addonPanel);
		c.gridy++;
		
	}

	/**
	 * Dialog used when deploying a new sonobuoy 
	 * @param window
	 * @param currentArray
	 * @param streamer
	 * @param difarControl
	 * @return
	 */
	public static StreamerDataUnit showDialog(Window window, PamArray currentArray, StreamerDataUnit streamer, DifarControl difarControl) {
		if (singleInstance == null || singleInstance.getOwner() != window) {
			singleInstance = new SonobuoyDialog(window);
		}
		singleInstance.currentArray = currentArray;
		
		// Make a copy of the  StreamerDataUnits
//		singleInstance.streamerDataUnit = new StreamerDataUnit(streamer.getTimeMilliseconds(),streamer.getStreamerData()); 
//		for (int i = 0; i < streamer.getNumDataAnnotations(); i++){
//			singleInstance.streamerDataUnit.addDataAnnotation(streamer.getDataAnnotation(i));
//		}
		singleInstance.streamerDataUnit = streamer;
		singleInstance.streamerDataUnit.setDatabaseIndex(streamer.getDatabaseIndex());
		// Now we should have the full streamer data unit and annotations
		singleInstance.defaultStreamer = streamer.getStreamerData().clone();
		singleInstance.difarControl = difarControl;
		singleInstance.buoyManager = difarControl.sonobuoyManager;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.streamerDataUnit;
	}
	
	public void setParams() {
		super.setParams();
//		String actionString = "";
		String end = null;
		String compassCorrection = "";
		String compassStdDev = "";
		TimestampAnnotation endTimeAnnotation = (TimestampAnnotation) streamerDataUnit.findDataAnnotation(
				TimestampAnnotation.class, buoyManager.sonobuoyEndTimeAnnotation.getAnnotationName());
		if (endTimeAnnotation != null){
			end = PamCalendar.formatDBDateTime(endTimeAnnotation.getTimestamp());
		}
		
//		StringAnnotation actionAnnotation = (StringAnnotation) streamerDataUnit.findDataAnnotation(
//				StringAnnotation.class,	buoyManager.sonobuoyActionAnnotation.getAnnotationName());
//		if (actionAnnotation != null){
//			actionString = actionAnnotation.getString();
//		}
//		StringAnnotation correctionAnnotation = (StringAnnotation) streamerDataUnit.findDataAnnotation(
//				StringAnnotation.class, buoyManager.calMeanAnnotation.getAnnotationName());
//		if (correctionAnnotation != null){
//			compassCorrection = correctionAnnotation.getString();
//		}
//		StringAnnotation stdDevAnnotation = (StringAnnotation) streamerDataUnit.findDataAnnotation(
//				StringAnnotation.class, buoyManager.calStdDevAnnotation.getAnnotationName());
//		if (stdDevAnnotation != null){
//			compassStdDev = stdDevAnnotation.getString();
//		}
//		action.setText(actionString);
		deployTime.setText(PamCalendar.formatDBDateTime(streamerDataUnit.getTimeMilliseconds()));
		endTime.setText(end);
//		calibration.setText(String.format("%3.1f", Double.valueOf(compassCorrection)));
//		calStdDev.setText(String.format("%3.2f", Double.valueOf(compassStdDev)));
		
	}

	public boolean getParams() {
		
		boolean ok = super.getParams();
		streamerDataUnit.setStreamerData(defaultStreamer);
		Long deployMillis = PamCalendar.millisFromDateString(deployTime.getText(), false);
		if (deployMillis == null){
			return showWarning("Invalid deployment timestamp");
		}
		streamerDataUnit.setTimeMilliseconds(deployMillis);
		
		// End Time
		Long endMillis = PamCalendar.millisFromDateString(endTime.getText(), false);
		if (endMillis != null){
			buoyManager.addSonobuoyAnnotation(streamerDataUnit, 
					buoyManager.sonobuoyEndTimeAnnotation, endMillis);
		}
		
		//If heading has changed, issue a new calibration data unit 
		Double heading = streamerDataUnit.getStreamerData().getHeading();
		
//		buoyManager.addSonobuoyAnnotation(streamerDataUnit, 
//				buoyManager.sonobuoyActionAnnotation, action.getText());
//		buoyManager.addSonobuoyAnnotation(streamerDataUnit, 
//				buoyManager.calMeanAnnotation, calibration.getText());
//		buoyManager.addSonobuoyAnnotation(streamerDataUnit, buoyManager.calStdDevAnnotation, calStdDev.getText());
		return true;
		
	}
	
	@Override
	public void cancelButtonPressed() {
		super.cancelButtonPressed();
		streamerDataUnit = null;
	}
}
