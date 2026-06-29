package backupmanager.network;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import backupmanager.BackupManager;

public class PamFtpParamsDialog extends PamDialog{

	private static PamFtpParamsDialog singleInstance;
	
	private FTPClientParams ftpParams = new FTPClientParams();
		
	private JTextField host;
	
	private JTextField user;

	private JPasswordField password;
	
	private BackupManager backupManager;
	
	private PamFtpParamsDialog(Window parentFrame, BackupManager backupManager) {
		super(parentFrame, "Ftp Client Settings", false);
		this.backupManager = backupManager;
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		JPanel southPanel = new JPanel(new GridBagLayout());
		southPanel.setBorder(new TitledBorder("Server Connection"));
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(southPanel, new JLabel("Host ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(southPanel, host = new JTextField(100), c);
		c.gridx=0;
		c.gridy++;
		addComponent(southPanel, new JLabel("Username ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(southPanel, user = new JTextField(50), c);
		c.gridx=0;
		c.gridy++;
		addComponent(southPanel, new JLabel("Password ", SwingConstants.RIGHT), c);
		c.gridx++;
		addComponent(southPanel, password = new JPasswordField(100), c);
		mainPanel.add(BorderLayout.SOUTH, southPanel);
		
		setDialogComponent(mainPanel);
	}
	
	public static FTPClientParams showDialog(Frame frame, BackupManager backupManager, FTPClientParams ftpParams) {
		if (singleInstance == null || singleInstance.getOwner() != frame) {
			singleInstance = new PamFtpParamsDialog(frame, backupManager);
		}
		singleInstance.ftpParams = ftpParams.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.ftpParams;
	}

	private void setParams() {
		host.setText(ftpParams.host);
		password.setText(ftpParams.password);
		user.setText(ftpParams.user);
	}

	@Override
	public boolean getParams() {
		ftpParams.host = host.getText();
		ftpParams.password = new String(password.getPassword());
		ftpParams.user = user.getText();
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		ftpParams = null;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	

}
