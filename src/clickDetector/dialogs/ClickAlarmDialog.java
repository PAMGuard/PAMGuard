/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package clickDetector.dialogs;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import clickDetector.ClickAlarm;
import clickDetector.ClickParameters;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileFilter;

import PamUtils.PamFileChooser;
import PamView.dialog.PamDialog;

public class ClickAlarmDialog extends PamDialog implements ActionListener {

    /**
     * The parameters object
     */
	private ClickParameters clickParameters;

    /**
     * Reference to the dialog
     */
	private static ClickAlarmDialog singleInstance;

    /**
     * Text field with the name of the user-selected alarm
     */
	JTextField alarmFileTxt;

    /**
     * Radio buttons allowing user to select default or other alarm
     */
    JRadioButton defaultAlarm, userAlarm;

    /**
     * button group holding the radio buttons
     */
    ButtonGroup buttonGroup;

    /**
     * Buttons
     */
	JButton browseFiles, renameAlarm, removeAlarm, addAlarm;
    
    /**
     * JFileChooser for selecting the user-defined sound file
     */
	JFileChooser fc;

    /**
     * Combo box for the alarm list
     */
    JComboBox alarmChooser;

    /**
     * Array list containing the alarms
     */
    String[] alarmList = { ClickAlarm.DEFAULTNAME };

    /**
     * current alarm in the combo box
     */
    ClickAlarm currentAlarm;

    /**
     * Default constructor
     *
     * @param parentFrame
     */
	private ClickAlarmDialog(Frame parentFrame) {
		
		super(parentFrame, "Audible Alarm Options", true);

        /* create the panel and add the components */
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(BorderLayout.CENTER, new AlarmPanel());
		setDialogComponent(p);

        // create the actionListeners
        browseFiles.addActionListener(this);
        renameAlarm.addActionListener(this);
        removeAlarm.addActionListener(this);
        addAlarm.addActionListener(this);
        defaultAlarm.addActionListener(this);
        userAlarm.addActionListener(this);
        alarmChooser.addActionListener(this);

	}

	public static ClickParameters showDialog(Frame parentFrame, ClickParameters clickParameters) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new ClickAlarmDialog(parentFrame);
		}

		singleInstance.setParams(clickParameters);

		singleInstance.setVisible(true);

		return singleInstance.clickParameters;
	}

	@Override
	public void cancelButtonPressed() {
		clickParameters = null;
	}
	

    /**
     * Gets the values from ClickParameters and displays them in the GUI
     *
     * @param clickParameters the object holding the parameters to be displayed
     */
	public void setParams(ClickParameters clickParameters) {
		this.clickParameters = clickParameters.clone();

        /* set up the combo box values and make the first alarm in the alarm list
         * the current alarm */
        prepareComboBox();
        currentAlarm = clickParameters.clickAlarmList.get(0);
        alarmChooser.setSelectedIndex(0);
        alarmFileTxt.setText(currentAlarm.getUserAlarmFile());

        /* update the radio buttons to reflect the alarm selection */
        updateButtons();
    }

    /**
     * Sorts the ClickAlarmList arrayList by alarm name, removes all the previous
     * alarm names from the JComboBox and reloads the new list
     */
    public void prepareComboBox() {
        Collections.sort(clickParameters.clickAlarmList);
        alarmChooser.removeAllItems();
        for (int i=0 ; i<clickParameters.clickAlarmList.size() ; i++ ) {
            alarmChooser.addItem(clickParameters.clickAlarmList.get(i));
        }

    }

    /**
     * Enables/disables buttons in the GUI
     */
    private void updateButtons() {
        /* enable/disable the radio buttons depending on whether we're using
         * the default sound file or not
         */
        if (currentAlarm.useDefault()) {
            ButtonModel model = defaultAlarm.getModel();
            buttonGroup.setSelected(model, true);
            alarmFileTxt.setEnabled(false);
            browseFiles.setEnabled(false);
        } else {
            ButtonModel model = userAlarm.getModel();
            buttonGroup.setSelected(model, true);
    		alarmFileTxt.setText(currentAlarm.getUserAlarmFile());
            alarmFileTxt.setEnabled(true);
            browseFiles.setEnabled(true);
        }

        /* disable the 'removeAlarm' button if there's only 1 alarm in the list */
        if (clickParameters.clickAlarmList.size()==1) {
            removeAlarm.setEnabled(false);
        } else {
            removeAlarm.setEnabled(true);
        }
	}

    @Override
	public void restoreDefaultSettings() {
		setParams(new ClickParameters());
	}

    /**
     * ActionListener events for the GUI components
     * @param e
     */
	public void actionPerformed(ActionEvent e) {
        
        /* Default Alarm radio button */
        if (e.getSource() == defaultAlarm) {
            currentAlarm.selectDefault(true);
            currentAlarm.loadAlarm();

        /* User Alarm radio button */
        } else if (e.getSource() == userAlarm) {
            currentAlarm.selectDefault(false);
            browseFiles();

        /* Browse Files button */
        } else if (e.getSource() == browseFiles) {
            userAlarm.setEnabled(true);
            currentAlarm.selectDefault(false);
			browseFiles();

        /* create a new clickAlarm object */
        } else if (e.getSource() == addAlarm) {
            String alarmName = getNextAlarmName();
            currentAlarm = new ClickAlarm(alarmName);
            clickParameters.clickAlarmList.add(currentAlarm);
            prepareComboBox();
            alarmChooser.setSelectedItem(currentAlarm);

        /* rename the current alarm */
        } else if (e.getSource() == renameAlarm) {
            String newName = (String) JOptionPane.showInputDialog(
                    "Enter new alarm name", currentAlarm.getName());
            if (newName!=null && newName.length()>0) {
                currentAlarm.setName(newName);
            }

        /* delete current alarm */
        } else if (e.getSource() == removeAlarm) {
            clickParameters.clickAlarmList.remove(
                    clickParameters.clickAlarmList.indexOf(currentAlarm));
            prepareComboBox();
//            alarmChooser.removeItem(currentAlarm);
            currentAlarm = clickParameters.clickAlarmList.get(0);
            alarmChooser.setSelectedIndex(0);

        /* select an alarm from the drop-down menu */
        } else if (e.getSource() == alarmChooser) {
            if (alarmChooser.getItemCount()!=0) {
                currentAlarm = (ClickAlarm) alarmChooser.getSelectedItem();
            }
        }
        
        /* update the buttons */
        updateButtons();
    }

    /**
     * Looks through the current list of alarms and returns a unique alarm name
     *
     * @return a unique alarm name
     */
    public String getNextAlarmName() {
        String newName = null;
        int i=0;
        boolean duplicateName = true;
        while (duplicateName) {
            i++;
            newName = "Alarm " + i;
            duplicateName = false;
            for (int j=0; j<clickParameters.clickAlarmList.size(); j++) {
                if (clickParameters.clickAlarmList.get(j).getName().equals(newName)) {
                    duplicateName = true;
                    break;
                }
            }
        }
        return newName;
    }

    /** Allow the user to select a wav file */
	public void browseFiles() {
		if (fc == null) {
			fc = new PamFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		}

		fc.setCurrentDirectory(new File(alarmFileTxt.getText()));
        fc.setDialogTitle("Choose WAV file to use as alarm");
        fc.setFileFilter(new SoundFilter());

		int ans = fc.showOpenDialog(this);

		if (ans == JFileChooser.APPROVE_OPTION) {
			alarmFileTxt.setText(fc.getSelectedFile().toString());
            currentAlarm.setUserAlarmFile(fc.getSelectedFile().getAbsolutePath());
            currentAlarm.selectDefault(false);
		} else {
            currentAlarm.selectDefault(true);
        }
        currentAlarm.loadAlarm();
        updateButtons();
	}

    public class SoundFilter extends FileFilter {

        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            } else {
                String ext = f.getName().toLowerCase();
                if (ext.endsWith("wav")) {
                    return true;
                }
            }
            return false;
        }

        public String getDescription() {
            return "Wav files";
        }
    }

    /**
     * Takes the current values on the screen and loads them into the
     * ClickParameters object
     *
     * @return
     */
	@Override
	public boolean getParams() {
        if (buttonGroup.getSelection() == defaultAlarm.getModel()) {
            currentAlarm.selectDefault(true);

        } else {
            currentAlarm.selectDefault(false);
    		currentAlarm.setUserAlarmFile(alarmFileTxt.getText());
        }
        currentAlarm.loadAlarm();
		return true;
	}

    /**
     * Alarm Panel inner class constructor
     */
	private class AlarmPanel extends JPanel {

		public AlarmPanel() {
			super();
			this.setBorder(new TitledBorder("Alarm Options"));
			this.setLayout(new GridLayout(0,1));
			JPanel p = new JPanel();
			p.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();

            c.anchor = GridBagConstraints.FIRST_LINE_START;
            c.gridx = 0;
            c.gridy = 0;
            alarmChooser = new JComboBox();
            p.add(alarmChooser,c);

            c.gridy++;
            defaultAlarm = new JRadioButton("Windows Beep");
            p.add(defaultAlarm,c);

            c.gridy++;
            userAlarm = new JRadioButton("Selected File...");
            p.add(userAlarm,c);

            c.gridy++;
            alarmFileTxt = new JTextField(30);
			p.add(alarmFileTxt,c);
            c.insets = new Insets(0,10,0,0);
            c.gridy++;
            browseFiles = new JButton("Browse ...");
			p.add(browseFiles,c);

            c.insets.top = 10;
            c.insets.left = 0;
            c.gridy++;
            renameAlarm = new JButton("Rename alarm");
            p.add(renameAlarm, c);
            c.insets.top = 0;
            c.gridy++;
            removeAlarm = new JButton("Remove this alarm");
            p.add(removeAlarm, c);
            c.insets.top = 10;
            c.gridy++;
            addAlarm = new JButton("Add new alarm");
            p.add(addAlarm, c);

            /* set the buttons to the same size */
            Dimension buttonWidth = removeAlarm.getPreferredSize();
//            deleteSighting.setMinimumSize(buttonWidth);
//            deleteSighting.setMaximumSize(buttonWidth);
            renameAlarm.setPreferredSize(buttonWidth);
            renameAlarm.setMinimumSize(buttonWidth);
            renameAlarm.setMaximumSize(buttonWidth);
            removeAlarm.setPreferredSize(buttonWidth);
            removeAlarm.setMinimumSize(buttonWidth);
            removeAlarm.setMaximumSize(buttonWidth);
            addAlarm.setPreferredSize(buttonWidth);
            addAlarm.setMinimumSize(buttonWidth);
            addAlarm.setMaximumSize(buttonWidth);

            /* add subpanel to main panel */
			this.add(p);

            // create the button group
            buttonGroup = new ButtonGroup();
            buttonGroup.add(defaultAlarm);
            buttonGroup.add(userAlarm);
        }
	}
}
