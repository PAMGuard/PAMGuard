package clipgenerator;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class ClipGenSettingDialog extends PamDialog {

	private static ClipGenSettingDialog singleInstance;

	private ClipGenSetting clipGenSetting;

	private JTextField preSeconds, postSeconds;
	private JComboBox channelSel;

	private JRadioButton budgetOn, budgetOff;
	private JTextField budgetPeriod, dataBudget;
	
	private JTextField fileInitials;

	private ClipGenSettingDialog(Window parentFrame, String title) {
		super(parentFrame, title, false);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JPanel infoPanel = new JPanel();
		infoPanel.setBorder(new TitledBorder("Clip Generation"));
		infoPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(infoPanel, new JLabel("Channel selection ", SwingConstants.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		addComponent(infoPanel, channelSel = new JComboBox(ClipGenSetting.channelSelTypes),  c);
		c.gridx = 0;
		c.gridwidth = 1;
		c.gridy++;
		addComponent(infoPanel, new JLabel("Time before trigger ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(infoPanel, preSeconds = new JTextField(5), c);
		c.gridx++;
		addComponent(infoPanel, new JLabel(" seconds ", SwingConstants.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(infoPanel, new JLabel("Time after trigger ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(infoPanel, postSeconds = new JTextField(5), c);
		c.gridx++;
		addComponent(infoPanel, new JLabel(" seconds ", SwingConstants.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(infoPanel, new JLabel("File initials ", SwingConstants.RIGHT), c);
		c.gridx++;
		c.gridwidth = 2;
		addComponent(infoPanel, fileInitials = new JTextField(7), c);

		JPanel budgetPanel = new JPanel();
		budgetPanel.setBorder(new TitledBorder("Data Budget"));
		budgetPanel.setLayout(new GridBagLayout());
		c = new PamGridBagContraints();
		c.gridwidth = 1;
		addComponent(budgetPanel, budgetOff = new JRadioButton("Record everything"), c);
		c.gridx+=c.gridwidth;
		c.gridwidth = 2;
		addComponent(budgetPanel, budgetOn = new JRadioButton("Budget data"), c);
		c.gridx = 0;
		c.gridwidth = 1;
		c.gridy++;
		addComponent(budgetPanel, new JLabel("Data budget ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(budgetPanel, dataBudget = new JTextField(5), c);
		c.gridx++;
		addComponent(budgetPanel, new JLabel(" Megabytes ", SwingConstants.LEFT), c);
		c.gridx = 0;
		c.gridy++;
		addComponent(budgetPanel, new JLabel("Budget period ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(budgetPanel, budgetPeriod = new JTextField(5), c);
		c.gridx++;
		addComponent(budgetPanel, new JLabel(" Hours ", SwingConstants.LEFT), c);

		ButtonGroup budgetGroup = new ButtonGroup();
		budgetGroup.add(budgetOn);
		budgetGroup.add(budgetOff);
		BudgetButton bb = new BudgetButton();
		budgetOn.addActionListener(bb);
		budgetOff.addActionListener(bb);

		mainPanel.add(infoPanel);
		mainPanel.add(budgetPanel);

		setHelpPoint("sound_processing.ClipGenerator.docs.ClipGenerator");

		setDialogComponent(mainPanel);
	}

	public static ClipGenSetting showDialog(Window parent, ClipGenSetting clipGenSetting, boolean storeWavFile) {
		if (singleInstance == null || singleInstance.getOwner() != parent) {
			singleInstance = new ClipGenSettingDialog(parent, clipGenSetting.dataName);
		}
		singleInstance.setTitle(clipGenSetting.dataName);
		singleInstance.fileInitials.setEnabled(storeWavFile);
		singleInstance.clipGenSetting = clipGenSetting.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.clipGenSetting;
	}

	private class BudgetButton implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}

	}
	@Override
	public void cancelButtonPressed() {
		clipGenSetting = null;
	}

	public void enableControls() {
		boolean b = budgetOn.isSelected();
		dataBudget.setEnabled(b);
		budgetPeriod.setEnabled(b);
	}

	private void setParams() {
		preSeconds.setText(String.format("%3.2f", clipGenSetting.preSeconds));
		postSeconds.setText(String.format("%3.2f", clipGenSetting.postSeconds));
		channelSel.setSelectedIndex(clipGenSetting.channelSelection);
//		if (clipGenSetting.clipPrefix != null) {
			fileInitials.setText(clipGenSetting.clipPrefix);
//		}

		budgetOn.setSelected(clipGenSetting.useDataBudget);
		budgetOff.setSelected(!clipGenSetting.useDataBudget);
		dataBudget.setText(String.format("%3.1f", clipGenSetting.dataBudget/1024.));
		budgetPeriod.setText(String.format("%3.2f", clipGenSetting.budgetPeriodHours));
		enableControls();
	}

	@Override
	public boolean getParams() {
		try {
			clipGenSetting.preSeconds = Double.valueOf(preSeconds.getText());
			clipGenSetting.postSeconds = Double.valueOf(postSeconds.getText());
			clipGenSetting.channelSelection = channelSel.getSelectedIndex();
			clipGenSetting.clipPrefix = fileInitials.getText();
			if (clipGenSetting.clipPrefix == null) {
				return false;
			}

			clipGenSetting.useDataBudget = budgetOn.isSelected();
			if (clipGenSetting.useDataBudget) {
				clipGenSetting.dataBudget = (int) (Double.valueOf(dataBudget.getText()) * 1024.);
				clipGenSetting.budgetPeriodHours = Double.valueOf(budgetPeriod.getText());
			}
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid number ...");
		}
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
