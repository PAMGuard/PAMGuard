package PamModel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class ThreadingDialog extends PamDialog {

	private PamModelSettings pamModelSettings;
	
	private JCheckBox multiThreading;
	
	private JTextField jitterMillis;
	
	private JTextArea infoArea;
	
	private JCheckBox enableGC;
	
	private JTextField gcInterval;
	
	private ThreadingDialog(Window parentFrame) {
		super(parentFrame, "Threading Model", true);
		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		p.setBorder(new TitledBorder("Threading Model"));
		c.gridwidth = 3;
		addComponent(p, multiThreading = new JCheckBox("Enable Multi-threading"), c);
		multiThreading.addActionListener(new CtrlEnabler());
		c.gridy++;
		c.gridwidth = 1;
		addComponent(p, new JLabel("Max inter thread jitter "), c);
		c.gridx++;
		addComponent(p, jitterMillis = new JTextField(5), c);
		c.gridx++;
		addComponent(p, new JLabel("milliseconds"), c);
		c.gridy++;
		c.gridwidth = 3;
		c.gridx = 0;
		String infoText = "Multithreading can speed up PAMGUARD execution" +
		"\nby utilising all cores on multi processor" +
		"\nmachines." +
		"\nIncreased jitter increases the decoupling" +
		"\nbetween threads which can improve performance" +
		"\nbut may cause a loss of synchronisation" +
		"\nbetween processes.";
		addComponent(p, infoArea = new JTextArea(infoText), c);
		infoArea.setEditable(false);
//		infoArea.setEnabled(false);
		infoArea.setBackground(p.getBackground());

		setHelpPoint("overview.PamMasterHelp.docs.multithreading");
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(p);
		
		JPanel gcPanel = new JPanel(new GridBagLayout());
		c = new PamGridBagContraints();
		mainPanel.add(gcPanel);
		gcPanel.setBorder(new TitledBorder("Garbage Collection"));
		c.gridwidth = 3;
		gcPanel.add(enableGC = new JCheckBox("Run additional garbage collection"), c);
		c.gridwidth = 1;
		c.gridy++;
		gcPanel.add(new JLabel("Interval ", JLabel.RIGHT), c);
		c.gridx++;
		gcPanel.add(gcInterval = new JTextField(5), c);
		c.gridx++;
		gcPanel.add(new JLabel(" seconds"), c);
		enableGC.addActionListener(new CtrlEnabler());
		
		
		
		setDialogComponent(mainPanel);
	}
	
	public static PamModelSettings showDialog(JFrame frame, PamModelSettings pamModelSettings) {
		ThreadingDialog td = new ThreadingDialog(frame);
		td.pamModelSettings = pamModelSettings.clone();
		td.setParams();
		td.setVisible(true);
		return td.pamModelSettings;
	}
	
	@Override
	public void cancelButtonPressed() {
		pamModelSettings = null;
	}
	
	private void setParams() {
		multiThreading.setSelected(pamModelSettings.multiThreading);
		jitterMillis.setText(String.format("%d", pamModelSettings.getThreadingJitterMillis()));
		
		gcInterval.setText(String.format("%d", pamModelSettings.gcInterval));
		enableGC.setSelected(pamModelSettings.enableGC);
		
		enableControls();
	}

	@Override
	public boolean getParams() {
		pamModelSettings.multiThreading = multiThreading.isSelected();
		int jm = 0;
		try {
			jm = Integer.valueOf(jitterMillis.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Inter thread jitter must be a positive integer");
		}
		pamModelSettings.setThreadingJitterMillis(jm);
		
		pamModelSettings.enableGC = enableGC.isSelected();
		if (enableGC.isSelected()) {
			try {
				pamModelSettings.gcInterval = Integer.valueOf(gcInterval.getText());	
			}
			catch (NumberFormatException e) {
				return showWarning("Garbage collection interval must be a positive integer");
			}
			if (pamModelSettings.gcInterval < 1) {
				return showWarning("Garbage collection interval must be a positive integer");
			}
		}
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		pamModelSettings = new PamModelSettings();
		setParams();
	}

	private void enableControls() {
		jitterMillis.setEnabled(multiThreading.isSelected());
		gcInterval.setEnabled(enableGC.isSelected());
	}

	class CtrlEnabler implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}

	}
}
