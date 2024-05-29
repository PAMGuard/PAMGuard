package Acquisition.filedate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.swing.FontIcon;

import PamUtils.PamCalendar;
import PamView.component.PamSettingsIconButton;
import PamView.dialog.PamGridBagContraints;

/**
 * Simpel dialog component which can interact with a file date. 
 * @author dg50
 *
 */
public class FileDateDialogStrip {

	private FileDate fileDate;
	
	private JPanel mainPanel;
	
	private JLabel formatLabel;
	
	private JTextField fileTime;

	private JButton settingsButton;

//	private ImageIcon settingsIcon = new ImageIcon(ClassLoader.getSystemResource("Resources/SettingsButtonSmall2.png"));
	public static FontIcon settingsIcon =  FontIcon.of(PamSettingsIconButton.SETTINGS_IKON, PamSettingsIconButton.NORMAL_SIZE, Color.DARK_GRAY);

	
	private Window parent;	
	
	private ArrayList<FileDateObserver> observers = new ArrayList<>();

	public FileDateDialogStrip(FileDate fileDate, Window parent) {
		this.fileDate = fileDate;
		this.parent = parent;
		settingsButton = new JButton(settingsIcon);
		formatLabel = new JLabel(" ");
		fileTime = new JTextField(20);
		
		mainPanel = new JPanel(new BorderLayout());
		JPanel centPanel = new JPanel(new BorderLayout());
		centPanel.add(BorderLayout.NORTH, formatLabel);
		centPanel.add(BorderLayout.SOUTH, fileTime);
		mainPanel.add(BorderLayout.CENTER, centPanel);
		mainPanel.add(BorderLayout.EAST, settingsButton);
		JPanel westPanel = new JPanel(new BorderLayout());
		westPanel.add(BorderLayout.NORTH, new JLabel("  "));
		westPanel.add(BorderLayout.CENTER, new JLabel("File date :  "));
		mainPanel.add(BorderLayout.WEST, westPanel);
		
//		mainPanel = new JPanel(new GridBagLayout());
//		GridBagConstraints c = new PamGridBagContraints();
//		c.gridx = 1;
//		mainPanel.add(formatLabel, c);
//		c.gridx = 0;
//		c.gridy ++;
//		mainPanel.add(new JLabel("File date :"), c);
//		c.gridx++;
//		mainPanel.add(fileTime, c);
//		fileTime.setEnabled(false);
//		c.gridx = 2;
//		c.gridy = 0;
//		c.gridheight = 2;
//		c.fill = GridBagConstraints.VERTICAL;
//		c.anchor = GridBagConstraints.BASELINE;
//		mainPanel.add(settingsButton, c);
		
		settingsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				settingsButton();
			}
		});
		settingsButton.setEnabled(fileDate.hasSettings());
		settingsButton.setToolTipText("File date settings and options");
	}
	

	private void settingsButton() {
		if (fileDate.doSettings(parent)) {
			notifyObservers();
		}
	}
	
	public Component getDialogComponent() {
		return mainPanel;
	}
	
	public void setDate(long fileDateMillis) {
		fileTime.setText(PamCalendar.formatDateTime(fileDateMillis));
	}
	
	public void setFormat(String format) {
		formatLabel.setText(format);
	}
	
	private void notifyObservers() {
		for (FileDateObserver obs:observers) {
			obs.fileDateChange(fileDate);
		}
	}
	
	public void addObserver(FileDateObserver observer) {
		observers.add(observer);
	}
	
	public boolean removeObserver(FileDateObserver observer) {
		return observers.remove(observer);
	}
}
