package generalDatabase;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamView.dialog.PamDialog;
import PamView.dialog.warn.WarnOnce;

public class DBDialog extends PamDialog {

	private DBParameters dbParameters;
	
	private DBControl dbControl;
	
	static DBDialog singleInstance;
	
	private JComboBox systemList;
	
	private SystemDialogPanel systemDialogPanel;
	
//	StandardDBPanel standardDBPanel;
	public static final String defaultTitle = "Database Selection";
	
	private JPanel dialogBottomPanel;
	
	private JCheckBox useAutoCommit;
	
	private DBDialog(DBControl dbControl, Frame parentFrame, String title) {
		
		super(parentFrame, title, false);
		
		this.dbControl = dbControl;
		
//		standardDBPanel = new StandardDBPanel();
//		
//		systemDialogPanel = standardDBPanel;
		
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		
		JPanel top = new JPanel();
		top.setBorder(new TitledBorder("Database system"));
		top.setLayout(new BorderLayout());
		top.add(BorderLayout.CENTER, systemList = new JComboBox());
		systemList.setPreferredSize(new Dimension(300, 20));
		systemList.addActionListener(new SelectDBSystem());
		
		p.add(BorderLayout.NORTH, top);
		
		dialogBottomPanel = new JPanel();
		dialogBottomPanel.setLayout(new BorderLayout());
//		dialogBottomPanel.add(BorderLayout.CENTER, standardDBPanel.getPanel());
		
		p.add(BorderLayout.CENTER, dialogBottomPanel);
		setDialogComponent(p);
		
		JPanel s = new JPanel(new BorderLayout());
		s.setBorder(new TitledBorder("Options"));
		s.add(BorderLayout.CENTER, useAutoCommit = new JCheckBox("Use AutoCommit"));
		p.add(BorderLayout.SOUTH, s);
		String tip = "<html>Auto Commit saves data immediately to the database. <p>"
				+ "Turning this off can improve performance since multiple records will be written together.</html>";
		useAutoCommit.setToolTipText(tip);
		useAutoCommit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
		        AbstractButton abstractButton = (AbstractButton) e.getSource();
		        boolean selected = abstractButton.getModel().isSelected();
		        if (selected) {
					String	msg = "<html>Note: AutoCommit may drastically slow down Pamguard processing.  Use with caution.</html>";
					int newAns = WarnOnce.showWarning(singleInstance, "Database AutoCommit", msg, WarnOnce.OK_OPTION);
		        }
			}
		});
		
		setHelpPoint("utilities.generalDatabaseHelp.docs.database_database");
	}
	
	
	
	public static DBParameters showDialog(DBControl dbControl, Frame parentFrame, DBParameters dbParameters, String selectTitle) {
		if (selectTitle == null) selectTitle = defaultTitle;
		if (singleInstance == null || singleInstance.getParent() != parentFrame || singleInstance.dbControl != dbControl) {
			singleInstance = new DBDialog(dbControl, parentFrame, selectTitle);
		}
		singleInstance.dbParameters = dbParameters;
		singleInstance.setParams();
		singleInstance.setVisible(true);		
		return singleInstance.dbParameters;
	}

	@Override
	public void cancelButtonPressed() {
		dbParameters = null;
	}
	
	private void setParams() {
		systemList.removeAllItems();
		ArrayList<DBSystem> dbSystems = dbControl.databaseSystems;
		for (int i = 0; i < dbSystems.size(); i++) {
			systemList.addItem(dbSystems.get(i).getSystemName());
		}
		if (dbParameters.getDatabaseSystem() < dbSystems.size()) {
			systemList.setSelectedIndex(dbParameters.getDatabaseSystem());
		}
		useAutoCommit.setSelected(dbParameters.getUseAutoCommit());
//		systemDialogPanel.setParams();
	}

	@Override
	public boolean getParams() {

		if (systemDialogPanel != null && !systemDialogPanel.getParams()) return false;
		
		dbParameters.setDatabaseSystem(systemList.getSelectedIndex());
		
		boolean oc = useAutoCommit.isSelected();
		if (oc) {
			String msg = "<html>Using Auto Commit will slow down database (and PAMGuard) performance. <p>"
					+ "We recommend that you don't use it. Press Cancel to uncheck or OK to continue";
			int ans = WarnOnce.showWarning(getOwner(), "Database Auto Commit", msg, WarnOnce.OK_CANCEL_OPTION);
			if (ans == WarnOnce.CANCEL_OPTION) {
				useAutoCommit.setSelected(false);
				return false;
			}
		}
		
		dbParameters.setUseAutoCommit(oc);
		
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}
	
	private void selectSystem() {

		int currenIndex = systemList.getSelectedIndex();
		dialogBottomPanel.removeAll();
		DBSystem currentSystem = dbControl.getSystem(currenIndex);
		if (currentSystem != null) {
			systemDialogPanel = currentSystem.getDialogPanel(this);
			if (systemDialogPanel != null) {
				dialogBottomPanel.add(BorderLayout.CENTER, systemDialogPanel.getPanel());
				systemDialogPanel.setParams();
			}
		}
		else {
			systemDialogPanel = new NullDialogPanel();
			dialogBottomPanel.add(BorderLayout.CENTER, systemDialogPanel.getPanel());
		}
		pack();
		
	}
	
	class SelectDBSystem implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			selectSystem();
			
		}
		
	}
/*
	class StandardDBPanel implements SystemDialogPanel {

		
		JTextField databaseName;
		
		JTextField userName;
		
		JPasswordField password;
		
		JButton browseButton;
		
		JPanel bot;
		
		
		public JPanel getPanel() {
			bot = new JPanel();
			bot.setBorder(new TitledBorder("Database selection"));
			GridBagConstraints c = new GridBagConstraints();
			bot.setLayout(new GridBagLayout());
			
			c.gridx = c.gridy = 0;
			addComponent(bot, new JLabel("Database"), c);
			c.gridy++;
			c.gridwidth = 3;
			addComponent(bot, databaseName = new JTextField(30), c);
			databaseName.setEditable(false);
			
			c.gridy++;
			c.gridwidth = 1;
			c.gridx = 2;
			c.anchor = GridBagConstraints.EAST;
			addComponent(bot, browseButton = new JButton("Browse"), c);
			browseButton.addActionListener(new BrowseDatabases());

			return bot;
		}

		public boolean getParams() {
//			dbParameters.databaseName = databaseName.getText();
			return true;
		}

		public void setParams() {
			
//			databaseName.setText(dbParameters.databaseName);
			
		}
		
		class BrowseDatabases implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				
				String newDatabase = dbControl.browseDatabases(bot);
				if (newDatabase != null) {
					databaseName.setText(newDatabase);
				}
				
			}
			
		}
		
	}
	*/
}
