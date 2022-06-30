package effortmonitor.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamView.PamSidePanel;
import PamView.component.PamSettingsIconButton;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.dialog.PamTextField;
import PamView.panel.PamPanel;
import effortmonitor.EffortControl;
import effortmonitor.EffortObserver;
import effortmonitor.EffortParams;

public class EffortSidePanel implements PamSidePanel, EffortObserver {

	private PamPanel mainPanel;
	
	private PamTextField person;
	
	private PamCheckBox logging;
	
	private PamSettingsIconButton settingsButton;

	private EffortControl effortControl;
	
	public EffortSidePanel(EffortControl effortControl) {
		this.effortControl = effortControl;
		
		mainPanel = new PamPanel(new GridBagLayout());
		mainPanel.setBorder(new TitledBorder(effortControl.getUnitName()));
		GridBagConstraints c = new PamGridBagContraints();
		mainPanel.add(new PamLabel("Person ", JLabel.RIGHT), c);
		c.gridx++;
		mainPanel.add(person = new PamTextField(6), c);
		person.setEditable(false);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		mainPanel.add(logging = new PamCheckBox("Log effort"), c);
		c.gridx += c.gridwidth;
		mainPanel.add(settingsButton = new PamSettingsIconButton(), c);
		settingsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				settings();
			}
		});
		effortControl.addObserver(this);
		logging.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				loggingButton();
			}
		});
		
		updateSettings();
	}
	
	protected void loggingButton() {
		effortControl.setOnEffort(logging.isSelected());
	}

	protected void settings() {
		effortControl.showSettingsDialog(effortControl.getGuiFrame(), null);
	}
	
	public void updateSettings() {
		EffortParams params = effortControl.getEffortParams();
		person.setText(params.getObserver());
		logging.setSelected(params.isSet);
	}

	@Override
	public JComponent getPanel() {
		return mainPanel;
	}

	@Override
	public void rename(String newName) {
		mainPanel.setBorder(new TitledBorder(newName));
	}

	@Override
	public void statusChange() {
		updateSettings();
	}

}
