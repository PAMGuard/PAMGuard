package generalDatabase;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamUtils.PamFileFilter;

public class MSAccessDialogPanel implements SystemDialogPanel {
	
	private BaseAccessSystem msAccessSystem;
	
	private JPanel p;
	
	private JComboBox dbList;
	
	private JButton browseButton, newButton;
	
	private Component parent;
	
	public MSAccessDialogPanel(Component parent, BaseAccessSystem msAccessSystem) {
		super();
		this.parent = parent;
		this.msAccessSystem = msAccessSystem;
		p = new JPanel();
		p.setBorder(new TitledBorder("MS Access database file"));
		p.setLayout(new BorderLayout());
		p.add(BorderLayout.NORTH, dbList = new JComboBox());
		JPanel q = new JPanel();
		q.setLayout(new FlowLayout(FlowLayout.TRAILING));
		q.add(browseButton = new JButton("Browse / Create ..."));
//		q.add(newButton = new JButton("Create New ..."));
		p.add(BorderLayout.CENTER, q);		
		
		browseButton.addActionListener(new BrowseButtonAction());
	}
	
	@Override
	public JPanel getPanel() {
		return p;
	}
	
	@Override
	public boolean getParams() {
		// selected item may not be first in the list - so re-order the 
		// list to make sure that it is.
		int ind = dbList.getSelectedIndex();
		if (ind >= 0) {
			File selFile = (File) dbList.getSelectedItem();
			if (!selFile.exists()) return false;
			msAccessSystem.getRecentDatabases().remove(selFile);
			msAccessSystem.getRecentDatabases().add(0, selFile);
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public void setParams() {
		
		dbList.removeAllItems();
		for (int i = 0; i < msAccessSystem.getRecentDatabases().size(); i++) {
			dbList.addItem(msAccessSystem.getRecentDatabases().get(i));
		}
		
	}
	
//	class ListAction implements ActionListener {
//	
//	public void actionPerformed(ActionEvent e) {
//	
//	int ind = dbList.getSelectedIndex();
//	if (ind > 0) {
//	File selFile = (File) dbList.getSelectedItem();
//	msAccessSystem.recentDatabases.remove(selFile);
//	msAccessSystem.recentDatabases.add(0, selFile);
//	dbList.remove(ind);
//	setParams();
//	}
//	}
//	
//	}
	
	class BrowseButtonAction implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			String newDB = msAccessSystem.browseDatabases(parent);
			if (newDB != null) {
				
				// see if this file exists in the list and if it does, remove it
				for (int i = 0; i < msAccessSystem.getRecentDatabases().size(); i++) {
					if (msAccessSystem.getRecentDatabases().get(i).toString().equalsIgnoreCase(newDB)) {
						msAccessSystem.getRecentDatabases().remove(i);
					}
				}
				// then insert the file at the top of the list.
				File newFile = new File(newDB);
				// if the file doesn't exit, consider creating it.
				if (!newFile.exists()) {
					newFile = createNewDatabase(newDB);
					if (newFile == null) {
						System.out.println("Unable to create "+newFile);
						return;
					}
					
				}
				
				msAccessSystem.getRecentDatabases().add(0, newFile);
				setParams();
				
			}
			
		}

	}

	public File createNewDatabase(String newDB) {
		String dummy = "BlankAccess._accdb";
		
		File cpdb=new File(dummy);
		if (!cpdb.exists()) {
			return null;
		}
		File newFile = new File(newDB);
//		String end = newFile.
		newFile = PamFileFilter.checkFileEnd(newFile, ".accdb", true);

		int ans = JOptionPane.showConfirmDialog(parent, "Create blank database " + newFile.getAbsolutePath() + " ?", "Microsoft Access", JOptionPane.OK_CANCEL_OPTION);
		if (ans == JOptionPane.CANCEL_OPTION) {
			return null;
		}
		
		try {
			Files.copy(cpdb.toPath(), newFile.toPath());
		}
		catch (Exception e) {
			return null;
		}
		return newFile;
	}

}
