package generalDatabase;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamUtils.PamCalendar;
import PamView.dialog.PamDialog;

public class MySQLDialogPanel implements SystemDialogPanel {

	ServerBasedSystem serverBasedSystem;
	
	Component parent;
	
	JPanel p;
	
	JTextField userName, ipAddress, portNumber;
	
	JPasswordField passWord;
	
	JButton connectServer, newDatabase;
	
	JLabel connectionStatus;
	
	JComboBox databaseList;
	
	public MySQLDialogPanel(Component parent, ServerBasedSystem serverBasedSystem) {
		super();
		this.parent = parent;
		this.serverBasedSystem = serverBasedSystem;
	}

	public JPanel getPanel() {
		if (p == null) {
			createPanel();
		}
		return p;
	}
	public JPanel createPanel() {
		p = new JPanel();
		p.setLayout(new BorderLayout());
		
		JPanel t = new JPanel();
		t.setBorder(new TitledBorder(serverBasedSystem.getSystemName() + " server settings"));
//		t.add(new JLabel("For MySQL databases use the old database module"));
		t.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;
		c.gridx = c.gridy = 0;
		PamDialog.addComponent(t, new JLabel("Server address "), c);
		c.gridx++;
		PamDialog.addComponent(t, ipAddress = new JTextField(20), c);
		
		c.gridy++;
		c.gridx = 0;
		PamDialog.addComponent(t, new JLabel("Port number "), c);
		c.gridx++;
		c.anchor = GridBagConstraints.WEST;
		PamDialog.addComponent(t, portNumber = new JTextField(10), c);

		c.anchor = GridBagConstraints.EAST;
		c.gridy++;
		c.gridx = 0;
		PamDialog.addComponent(t, new JLabel("User name "), c);
		c.gridx++;
		PamDialog.addComponent(t, userName = new JTextField(20), c);
		
		c.gridy++;
		c.gridx = 0;
		PamDialog.addComponent(t, new JLabel("Password "), c);
		c.gridx++;
		PamDialog.addComponent(t, passWord = new JPasswordField(20), c);
		
		c.gridy++;
		c.gridx = 0;
		PamDialog.addComponent(t, connectionStatus = new JLabel("no status "), c);
		c.gridx++;
		PamDialog.addComponent(t, connectServer = new JButton("Connect"), c);
		connectServer.addActionListener(new ConnectServer());
		
		
		JPanel b = new JPanel();
		b.setBorder(new TitledBorder(serverBasedSystem.getSystemName() + " Database"));
//		b.add(new JLabel("Only use the ODBC databases module for MS Access databases"));
		b.setLayout(new GridBagLayout());
		
		c.gridx = c.gridy = 0;
		PamDialog.addComponent(b, new JLabel("database "), c);
		c.gridx++;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 2;
		PamDialog.addComponent(b, databaseList = new JComboBox(), c);
		c.gridy++;
		c.gridwidth = 1;
		PamDialog.addComponent(b, new JLabel("                   "), c);
		c.gridx++;
		c.anchor = GridBagConstraints.WEST;
		PamDialog.addComponent(b, newDatabase = new JButton("Create new"), c);
		newDatabase.addActionListener(new CreateNewDatabase());
		
		
		p.add(BorderLayout.NORTH, t);
		p.add(BorderLayout.CENTER, b);
		
		return p;
	}

	MySQLParameters tempParams;
	public boolean getParams() {
		tempParams = serverBasedSystem.mySQLParameters.clone();
		
		if (getServerParams() == false) return false;
		
		if (getDbParams() == false) return false;
		
		// now if all ok, copy back.
		serverBasedSystem.mySQLParameters = tempParams.clone();
		return true;
	}
	
	public boolean getServerParams() {
		if (tempParams == null){
			tempParams = serverBasedSystem.mySQLParameters.clone();
		}
		try {
			tempParams.ipAddress = ipAddress.getText();
			tempParams.userName = userName.getText();
			tempParams.passWord = new String(passWord.getPassword());
			tempParams.portNumber = Integer.valueOf(portNumber.getText());
		}
		catch (Exception ex) {
			return false;
		}
		return true;
	}

	public void setParams() {
		ipAddress.setText(serverBasedSystem.mySQLParameters.ipAddress);
		userName.setText(serverBasedSystem.mySQLParameters.userName);
		passWord.setText(serverBasedSystem.mySQLParameters.passWord);
		portNumber.setText(String.format("%d",serverBasedSystem.mySQLParameters.portNumber));
		
		setDbParams(false);

		sayServerStatus();
	}
	
	public void setDbParams(boolean newList) {
		databaseList.removeAllItems();
		ArrayList<String> dbList = serverBasedSystem.getAvailableDatabases(newList);
		int selIndex = -1;
		if (dbList != null) for (int i = 0; i < dbList.size(); i++) {
			databaseList.addItem(dbList.get(i));
			if (dbList.get(i).equalsIgnoreCase(serverBasedSystem.mySQLParameters.databaseName)) {
				selIndex = i;
			}
		}
		databaseList.setSelectedIndex(selIndex);
	}
	public boolean getDbParams() {
		if (tempParams == null){
			tempParams = serverBasedSystem.mySQLParameters.clone();
		}
		tempParams.databaseName = (String) databaseList.getSelectedItem();
		return (tempParams.databaseName != null);
	}

	class ConnectServer implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			if (getServerParams() == false) return;
			serverBasedSystem.serverConnect(tempParams);
			sayServerStatus();
		}
		
	}
	
	private boolean lastStatus = false;
	void sayServerStatus() {
		if (serverBasedSystem.isServerConnected()) {
			connectionStatus.setText("Connected");
		}
		else  {
			connectionStatus.setText("No Connection");
		}
		if (lastStatus != serverBasedSystem.isServerConnected()) {
			setDbParams(true);
			lastStatus = serverBasedSystem.isServerConnected();
		}
		enableControls();
	}
	
	void enableControls() {
		boolean ok = serverBasedSystem.isServerConnected();
		newDatabase.setEnabled(ok);
		databaseList.setEnabled(ok);
	}
	
	class CreateNewDatabase implements ActionListener {

		public void actionPerformed(ActionEvent e) {

			String defaultName = null;
			long now = PamCalendar.getTimeInMillis();
			if (now == 0) {
				now = System.currentTimeMillis();
			}
			String newName = PamCalendar.createFileName(now, "PamDatabase_", "");
//			if (PamController.getInstance().getRunMode() != PamController.RUN_PAMVIEW) {
				defaultName = (String) JOptionPane.showInputDialog(null, "Enter a name for a new datase", 
						"Database selection", JOptionPane.QUESTION_MESSAGE, null, null, newName);
//			}
			if (defaultName == null || defaultName.length() == 0) {
				defaultName = PamCalendar.createFileName(now, "PamDatabase_", "");
			}
			// fill any blanks
			defaultName = defaultName.replace(" ", "_");
			if (serverBasedSystem.createNewDatabase(defaultName)) {
				setDbParams(true);
			}
		}
		
	}
}
