/**
 * 
 */
package PamView.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import PamController.PamController;
import PamUtils.PamFileChooser;
import PamView.panel.PamPanel;

/**
 * @author Doug Gillespie
 *
 *has main functionality to give panel and and jfile chooser can be subclassed/altered 
 */
public class PamFilePanel extends JPanel {
		
	private JTextField tf;
	
	private String[] fileTypes;

	private String currentFile;

	private Window parentWindow;
	
	public PamFilePanel(Window parent, String fileType, String title){
		super();
		this.parentWindow = parent;
		if (title != null){
			setBorder(new TitledBorder(title));
		}
		setName(title);
		fileTypes = new String[1];
		fileTypes[0] = fileType;
		setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		
		tf=new JTextField();
		tf.setEditable(false);
		tf.setPreferredSize(new Dimension(400, tf.getPreferredSize().height)  );
		
		JButton browseButton;
		
		try{
			browseButton=new PamButton(new ImageIcon(ClassLoader.getSystemResource("Resources/searchIcon.png")));
		}catch (Exception e){
			browseButton = new PamButton("Browse");
		}
		
//		Border border = browseButton.getBorder();
		browseButton.setBorder(BorderFactory.createEmptyBorder(3, 4, 4, 4));
		browseButton.setToolTipText("Browse file system");
		browseButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				browseAction();
			}
		});
		
		add(tf, c);
		c.gridx++;
		add(browseButton, c);
		
	}
	
	
	private void browseAction() {
		String basePath = currentFile;
		String startPath = null;
		try {
			if (basePath == null) {
				basePath = PamController.getInstance().getPSFNameWithPath();
			}
			if (basePath != null) {
				File f = new File(basePath);
				File path = f.getParentFile();
				startPath = path.getAbsolutePath();
			}
		}
		catch (Exception e) {
			// catch everything since various things can go wrong here if a file doesn't exist, etc. 
		}
		
		String file = PamFileBrowser.fileBrowser(parentWindow, startPath, PamFileBrowser.OPEN_FILE, fileTypes);
		if (file != null) {
			setFile(file);
		}		
	}
	
	public void setFile(String filePath) {
		 currentFile = filePath;
		setFileLabel(filePath);
	}
	
	public File getFile(boolean mustExist) {
		if (currentFile == null || currentFile.length() == 0) {
			return null;
		}
		File f = new File(currentFile);
		if (mustExist) {
			if (f.exists() == false) {
				return null;
			}
		}
		return f;
	}
	
	private void setFileLabel(String filePath) {
		tf.setText(filePath);
	}


	/**
	 * @return the fileTypes
	 */
	public String[] getFileTypes() {
		return fileTypes;
	}


	/**
	 * @param fileTypes the fileTypes to set
	 */
	public void setFileTypes(String[] fileTypes) {
		this.fileTypes = fileTypes;
	}

}
