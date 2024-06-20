package tethys.swing.export;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import PamUtils.PamCalendar;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import metadata.PamguardMetaData;
import nilus.Deployment;
import nilus.DeploymentRecoveryDetails;
import tethys.TethysTimeFuncs;
import tethys.tooltips.TethysTips;

public class DeploymentPeriodPanel {

	private JPanel mainPanel;
	private JTextField deploymentStart, deploymentEnd;
	private JRadioButton useThese, useAudio;
	private Window parentFrame;

	public DeploymentPeriodPanel(Window parentFrame) {
		super();
		this.parentFrame = parentFrame;
		mainPanel = new JPanel(new GridBagLayout());
		ButtonGroup bg = new ButtonGroup();
		useThese = new JRadioButton("Use fixed deployment and recovery times below");
		useAudio = new JRadioButton("Use start and end times of collected audio data");
		bg.add(useThese);
		bg.add(useAudio);
		useThese.setToolTipText("This is useful if recording started before a device was first deployed as is often the case for moored systems");
		deploymentStart = new JTextField(15);
		deploymentEnd = new JTextField(15);
		
		GridBagConstraints c = new PamGridBagContraints();
		c.gridwidth = 2;
		mainPanel.add(useAudio, c);
		c.gridy++;
		mainPanel.add(useThese, c);
		c.gridy++;
		c.gridwidth = 1;
		mainPanel.add(new JLabel("Deployment start ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(deploymentStart, c);
		c.gridy++;
		c.gridx = 0;
		mainPanel.add(new JLabel("Deployment end ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(deploymentEnd, c);
		
		useThese.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		});
		useAudio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableControls();
			}
		});

		deploymentStart.setToolTipText(TethysTips.findTip(DeploymentRecoveryDetails.class, "TimeStamp"));
		deploymentEnd.setToolTipText(TethysTips.findTip(DeploymentRecoveryDetails.class, "TimeStamp"));
	}

	protected void enableControls() {
		deploymentStart.setEditable(useThese.isSelected());
		deploymentEnd.setEditable(useThese.isSelected());
	}

	/**
	 * @return the mainPanel
	 */
	public JPanel getMainPanel() {
		return mainPanel;
	}
	
	public void setParams(PamguardMetaData metaData) {
		useThese.setSelected(metaData.useAudioForDeploymentTimes == false);
		useAudio.setSelected(metaData.useAudioForDeploymentTimes);
		
		enableControls();
		
		Deployment deployment = metaData.getDeployment();
		if (deployment == null) {
			return;
		}
		DeploymentRecoveryDetails drl = deployment.getDeploymentDetails();
		Long millis = TethysTimeFuncs.millisFromGregorianXML(drl.getTimeStamp());
		if (millis != null) {
			deploymentStart.setText(PamCalendar.formatDBDateTime(millis));
		}
		drl = deployment.getRecoveryDetails();
		millis = TethysTimeFuncs.millisFromGregorianXML(drl.getTimeStamp());
		if (millis != null) {
			deploymentEnd.setText(PamCalendar.formatDBDateTime(millis));
		}
	}
	
	public boolean getParams(PamguardMetaData metaData) {
		Deployment deployment = metaData.getDeployment();
		metaData.useAudioForDeploymentTimes = useAudio.isSelected();
		if (metaData.useAudioForDeploymentTimes) {
			return true;
		}
		Long millis = PamCalendar.millisFromDateString(deploymentStart.getText(), true);
		if (millis == null) {
			return PamDialog.showWarning(parentFrame, "Bad data string", "unable to read date strin for deployment start");
		}
		deployment.getDeploymentDetails().setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(millis));
		
		millis = PamCalendar.millisFromDateString(deploymentEnd.getText(), true);
		if (millis == null) {
			return PamDialog.showWarning(parentFrame, "Bad data string", "unable to read date strin for deployment end");
		}
		deployment.getRecoveryDetails().setTimeStamp(TethysTimeFuncs.xmlGregCalFromMillis(millis));
		
		return true;
	}
	
}
