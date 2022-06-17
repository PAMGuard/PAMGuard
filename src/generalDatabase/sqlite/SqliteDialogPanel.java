package generalDatabase.sqlite;

import generalDatabase.SystemDialogPanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamUtils.PamFileFilter;
import PamUtils.PamUtils;

public class SqliteDialogPanel implements SystemDialogPanel {
	
	private SqliteSystem sqliteSystem;
	
	private JPanel p;
	
	private JComboBox dbList;
	
	private JButton browseButton, newButton;
	
	private Component parent;
	
	public SqliteDialogPanel(Component parent, SqliteSystem sqliteSystem) {
		super();
		this.parent = parent;
		this.sqliteSystem = sqliteSystem;
		p = new JPanel();
		p.setBorder(new TitledBorder("Sqlite database file"));
		p.setLayout(new BorderLayout());
		p.add(BorderLayout.NORTH, dbList = new JComboBox());
		JPanel q = new JPanel();
		q.setLayout(new FlowLayout(FlowLayout.TRAILING));
		q.add(browseButton = new JButton("Browse / Create ..."));
//		q.add(newButton = new JButton("Create New ..."));
		p.add(BorderLayout.CENTER, q);		
		
		browseButton.addActionListener(new BrowseButtonAction());
	}
	
	public JPanel getPanel() {
		return p;
	}
	
	public boolean getParams() {
		// selected item may not be first in the list - so re-order the 
		// list to make sure that it is.
		int ind = dbList.getSelectedIndex();
		if (ind >= 0) {
			File selFile = (File) dbList.getSelectedItem();
			if (selFile.exists() == false) return false;
			sqliteSystem.getRecentDatabases().remove(selFile);
			sqliteSystem.getRecentDatabases().add(0, selFile);
			return true;
		}
		else {
			return false;
		}
	}
	
	public void setParams() {
		
		dbList.removeAllItems();
		for (int i = 0; i < sqliteSystem.getRecentDatabases().size(); i++) {
			dbList.addItem(sqliteSystem.getRecentDatabases().get(i));
		}
		
	}
	
	class BrowseButtonAction implements ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			
			String newDB = sqliteSystem.browseDatabases(parent);
			if (newDB != null) {
				sqliteSystem.setDatabaseName(newDB);

				setParams();
			}
			
		}

	}

	

}
