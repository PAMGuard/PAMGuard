/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
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
package IshmaelDetector;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import Acquisition.pamAudio.PamAudioFileFilter;
import PamDetection.RawDataUnit;
import PamUtils.PamFileChooser;


public class MatchFiltParamsDialog extends IshDetParamsDialog implements ActionListener {

	static public final long serialVersionUID = 0;
	private static MatchFiltParamsDialog singleInstance;
	JComboBox kernelFilenameBox;
	JButton kernelFileButton;

	private MatchFiltParamsDialog(Frame parentFrame, Class inputDataClass) {
		super(parentFrame, "Matched Filter Parameters", inputDataClass);
		setHelpPoint("detectors.ishmael.docs.ishmael_matchedfilter");
	}

	public static MatchFiltParams showDialog2(Frame parentFrame, MatchFiltParams oldParams) 
	{
		//Create a new MatchFiltParamsDialog if needed.
		if (singleInstance == null || singleInstance.getOwner() != parentFrame)
			singleInstance = new MatchFiltParamsDialog(parentFrame, RawDataUnit.class);
		return (MatchFiltParams)showDialog3(parentFrame, oldParams, singleInstance);
	}
	
	@Override
	protected void addDetectorSpecificControls(JPanel g) {
		//Create the matched filter parameters panel.
		JPanel e = new JPanel();
		e.setBorder(BorderFactory.createTitledBorder("Matched filter"));
		e.setLayout(new BoxLayout(e, BoxLayout.Y_AXIS));
		JPanel f = new JPanel();
		f.setBorder(BorderFactory.createEmptyBorder());
		f.setLayout(new BoxLayout(f, BoxLayout.X_AXIS));
		f.add(new JLabel("Kernel sound file "));
		f.add(kernelFilenameBox = new JComboBox());
		e.add(f);
		
		e.add(kernelFileButton = new JButton("Select another file..."));
		kernelFileButton.addActionListener(this);
		
		g.add(e);
	}

	//Copy values from an MatchFiltParams to the dialog box.
	@Override
	void setParameters() {
		super.setParameters();
		//Set the values that are specific to matched filter.
		MatchFiltParams p = (MatchFiltParams)ishDetParams;
		kernelFilenameBox.removeAllItems();
		for (int i = 0; i < p.kernelFilenameList.size(); i++)
			kernelFilenameBox.addItem(p.kernelFilenameList.get(i));
		if (p.kernelFilenameList.size() > 0)
			kernelFilenameBox.setSelectedIndex(0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == kernelFileButton)		//user clicked "Select file"
			selectFile();
		
	}

	//User wants to pick a file.  Show a file-picker dialog box.
	//This was copied and modified from FileInputSystem.
	private void selectFile() {
		String currFile = (String)kernelFilenameBox.getSelectedItem();
		// seems to just support aif and wav files at the moment
//		Type[] audioTypes = AudioSystem.getAudioFileTypes();
//		for (int i = 0; i < audioTypes.length; i++) {
//			System.out.println(audioTypes[i]);
//		}
//		AudioStream audioStream = AudioSystem.getaudioin
		JFileChooser fileChooser = null;
		if (fileChooser == null) {
			fileChooser = new PamFileChooser();
			fileChooser.setFileFilter(new PamAudioFileFilter());
			fileChooser.setDialogTitle("Select matched filter kernel file...");
			//fileChooser.addChoosableFileFilter(filter);
			fileChooser.setFileHidingEnabled(true);
			fileChooser.setApproveButtonText("Select");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			javax.swing.filechooser.FileFilter[] filters = 
				fileChooser.getChoosableFileFilters();
			for (int i = 0; i < filters.length; i++) {
				fileChooser.removeChoosableFileFilter(filters[i]);
			}
			fileChooser.setFileFilter(new PamAudioFileFilter());
			
			if (currFile != null)
				fileChooser.setSelectedFile(new File(currFile));
		}
		int state = fileChooser.showOpenDialog(this);
		if (state == JFileChooser.APPROVE_OPTION) {
			currFile = fileChooser.getSelectedFile().getAbsolutePath();
			kernelFilenameBox.removeItem(currFile);
			kernelFilenameBox.insertItemAt(currFile, 0);
			kernelFilenameBox.setSelectedIndex(0);
		}
	}
	
	@Override
	//Read the values from the dialog box, parse and place into matchFiltParams.
	public boolean getParams() {
		MatchFiltParams p = (MatchFiltParams)ishDetParams;
		try {
			super.getParams();
			String fn = (String)kernelFilenameBox.getSelectedItem();
			if (fn == null)
				return false;
			p.kernelFilenameList.remove(fn);
			p.kernelFilenameList.add(0, fn);
			if (p.kernelFilenameList.size() > MatchFiltParams.MAX_FILENAME_LIST_SIZE)
				p.kernelFilenameList.remove(p.kernelFilenameList.size() - 1);
		} catch (Exception ex) {
			return false;
		}
		
		//Do error-checking here.
		//if (MatchFiltParams.isValidLength(matchFiltParams.fftLength)) {
		//	return true;
		//}
		return true;
	}
}
