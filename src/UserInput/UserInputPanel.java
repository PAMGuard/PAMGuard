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
package UserInput;

/**
 * 
 * @author David McLaren
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;

import pamScrollSystem.AbstractPamScrollerAWT;
import pamScrollSystem.ScrollPaneAddon;
import PamController.PamController;
import PamUtils.PamCalendar;
import PamView.PamTabPanel;
import PamView.dialog.PamButton;
import PamView.dialog.PamTextArea;
import PamView.panel.PamBorderPanel;
import PamguardMVC.PamDataBlock;

public class UserInputPanel implements PamTabPanel {

	UserInputController userInputController;

//	PamDataBlock<UserInputDataUnit> uiDataBlock;

	private UserInputLogger userInputLogger;

	public JFrame uiFrame;

	private javax.swing.JPanel uiPanel;

	private javax.swing.JPanel textEntryPanel;

	private javax.swing.JPanel emptyPanel;

	private javax.swing.JPanel textHistoryPanel;

	private JTextArea textInputField;

	// private javax.swing.JToolBar buttonBar;
	private javax.swing.JButton submitButton;
	
	private PamButton clearButton;

	// private GridLayout textEntryGrid;
	private JTextArea textOutputField;

	private JScrollPane scroller;

	private JEditorPane historyPane;

	private PamDataBlock userEntryDataBlock;
	
	private int runMode;

	/** Creates new form TestDataEntry */

	// public UserInputPanel(UserInputController userInputController) {
	public UserInputPanel(UserInputController userInputController) {
		this.userInputController = userInputController;
		// this.userInputController.getUserInputProcess().AddOutputDataBlock(
		// new PamDataBlock(DataType.USERENTRY,"User
		// Annotation",this.userInputController.getUserInputProcess(), 0));
//		userInputLogger = new UserInputLogger();

		runMode = PamController.getInstance().getRunMode();
		uiFrame = new JFrame();
		buildUiPanel();
	}

	private void buildUiPanel() {

		uiPanel = new PamBorderPanel();
		uiPanel.setLayout(new BorderLayout());
		textHistoryPanel = new PamBorderPanel();
		emptyPanel = new PamBorderPanel();
		emptyPanel.setPreferredSize(new Dimension(50, 50));

		historyPane = new JEditorPane();

		textInputField = new PamTextArea();
		textInputField.setWrapStyleWord(true);
		textInputField.setLineWrap(true);
		textInputField.addKeyListener(new TextListener());
//		textInputField.
//		textInputField.setAutoscrolls(true);
//		textInputField.setPreferredSize(new Dimension(600, 80));
		textInputField.setFont(new Font("Arial", Font.BOLD, 14));
		submitButton = new PamButton("Submit comment");
//		submitButton.setMaximumSize(new Dimension(60, 60));
		clearButton = new PamButton("Clear comment");
		textEntryPanel = new PamBorderPanel();
		textEntryPanel.setLayout(new BorderLayout());
		textEntryPanel.setBorder(new TitledBorder("Enter Comment"));

//		textInputField
//				.setBorder(new javax.swing.border.TitledBorder("Comment:"));
		textInputField.setFocusable(true);
		PamBorderPanel inputBorder = new PamBorderPanel();
//		inputBorder.setBorder(new javax.swing.border.TitledBorder("Comment:"));
//		inputBorder.setPreferredSize(new Dimension(600, 180));
		inputBorder.setLayout(new BorderLayout());
		JScrollPane inputScroller = new JScrollPane(textInputField);
		inputScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED); 
		inputScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		inputBorder.add(BorderLayout.CENTER, inputScroller);
		textEntryPanel.add(BorderLayout.CENTER, inputBorder);
		PamBorderPanel buttonPanel = new PamBorderPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(submitButton);
		buttonPanel.add(clearButton);
//		buttonPanel.add(BorderLayout.CENTER, submitButton);
//		buttonPanel.add(BorderLayout.CENTER, clearButton);
		textEntryPanel.add(BorderLayout.EAST, buttonPanel);

		textOutputField = new PamTextArea();
		textHistoryPanel.setLayout(new BorderLayout());
		textHistoryPanel.setBorder(new javax.swing.border.TitledBorder(
				"Entries:"));
		textOutputField.setBackground(new Color(230, 230, 230));
//		uiPanel.setLayout(new BoxLayout(uiPanel, BoxLayout.Y_AXIS));

		scroller = new JScrollPane(textOutputField);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		// scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
//		scroller.setPreferredSize(new Dimension(600, 300));
		textOutputField.setEditable(false);
		textOutputField.setWrapStyleWord(true);
		textOutputField.setLineWrap(true);
		textHistoryPanel.add(BorderLayout.CENTER, scroller);

//		uiPanel.add(emptyPanel);
		if (runMode == PamController.RUN_PAMVIEW) {
			uiPanel.setLayout(new GridLayout(1,1));
			PamBorderPanel topPanel = new PamBorderPanel(new BorderLayout());
			ScrollPaneAddon sco = new ScrollPaneAddon(scroller, userInputController.getUnitName(),
					AbstractPamScrollerAWT.HORIZONTAL, 1000, 2*24*3600*1000, true);
			sco.addDataBlock(userInputController.userInputProcess.uiDataBlock);
			topPanel.add(BorderLayout.EAST, sco.getButtonPanel());
			textHistoryPanel.add(BorderLayout.NORTH, topPanel);
//			uiPanel.add(textEntryPanel);
			uiPanel.add(textHistoryPanel);
		}
		else {
			uiPanel.setLayout(new GridLayout(2,1));
			uiPanel.add(textEntryPanel);
			uiPanel.add(textHistoryPanel);
		}
		
		
		uiFrame.getContentPane().add(uiPanel); // ,
												// java.awt.BorderLayout.CENTER);
		uiPanel.getRootPane().setDefaultButton(submitButton);

//		submitButton.setText("Submit Comment");
		submitButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				submitActionPerformed(evt);
			}
		});
		clearButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				clearActionPerformed(evt);
			}
		});
		enableButtons();
		this.setFocusOnTextInput();
	}
	
	class TextListener implements KeyListener {

		public void keyPressed(KeyEvent e) {
			//if(e.getKeyText(e.getKeyCode())=="Enter"){
			if(e.getKeyCode()==KeyEvent.VK_ENTER){
			submitText();			
			}
		}

		public void keyReleased(KeyEvent e) {
			// TODO Auto-generated method stub
			if(e.getKeyCode()==KeyEvent.VK_ENTER){
				textInputField.setText(null);
				textInputField.setCaretPosition(0);
				}
			
		}

		public void keyTyped(KeyEvent e) {

			enableButtons();
			
		}
		
	}

	private void enableButtons() {
		boolean enab = (textInputField.getText().length() > 0);
		submitButton.setEnabled(enab);
		clearButton.setEnabled(enab);
	}
	
	private void submitText(){


		long timeMS = PamCalendar.getTimeInMillis();
		String dateStr = PamCalendar.formatDateTime(timeMS);
		appendStringToOutputField(textInputField.getText());
		
		// set data in user input process/model
		// may need to split the data up into several datablocks to fit in into 
		// the dataabse - max length for a line is UserInpuController.maxCommentLength
		String text = textInputField.getText();
		String subText;
		int lastSpace, nextSpace;
		while (text.length() > 0) {
			if (text.length() < UserInputController.maxCommentLength) {
				createDataUnit(timeMS, text);
				break;
			}
			// find the last space before the maximum character length
			lastSpace = 0;
			nextSpace = 0;
			int startChar;
			while (true) {
				nextSpace = text.indexOf(' ', lastSpace+1);
				if (nextSpace > 0 && nextSpace < UserInputController.maxCommentLength) {
					lastSpace = nextSpace;
					continue;
				}
				if (lastSpace <= 0){
					lastSpace = Math.min(UserInputController.maxCommentLength, text.length());
				}
				startChar = 0;
				while(text.charAt(startChar) == ' ') {
					startChar++;
				}
				subText = text.substring(startChar, lastSpace);
//				System.out.println(subText + " - " + subText.length());
				createDataUnit(timeMS, subText);
				text = text.substring(lastSpace);	
				text = text.trim();
				break;
			}
		}
		
		
		enableButtons();
	
	}
	
	public void appendStringToOutputField(String string){
		long timeMS = PamCalendar.getTimeInMillis();
		appendStringToOutputField(timeMS, string);	
	}

	public void appendStringToOutputField(long timeMS, String string){
		String dateStr = PamCalendar.formatDateTime(timeMS);
		textOutputField.append(dateStr + " :  " + string + "\n");
		textOutputField.setSelectionStart(textOutputField.getLineCount());		
	}
	
	private void submitActionPerformed(java.awt.event.ActionEvent evt) {
		submitText();
	}
	
	private void createDataUnit(long timeMS, String data) {
//		PamDataUnit pamDataUnit = userInputController.getUserInputProcess().uiDataBlock
//		.getNewUnit(0, 0, 1);
		UserInputDataUnit nd = new UserInputDataUnit(timeMS, data);
		userInputController.getUserInputProcess().uiDataBlock.addPamData(nd);
	}
	
	protected void refillHistory() {
		textOutputField.setText("");
		int n = userInputController.getUserInputProcess().uiDataBlock.getUnitsCount();
		UserInputDataUnit dataUnit;
		for (int i = 0; i < n; i++) {
			dataUnit = userInputController.getUserInputProcess().
			uiDataBlock.getDataUnit(i, PamDataBlock.REFERENCE_CURRENT);
			appendStringToOutputField(dataUnit.getTimeMilliseconds(), dataUnit.getUserString());
		}

	}
	
	private void clearActionPerformed(java.awt.event.ActionEvent evt) {
		textInputField.setText("");
		enableButtons();
	}

	public void setFocusOnTextInput() {
		textInputField.setText("");
		textInputField.requestFocus();
	}

	public JMenu createMenu(Frame parentFrame) {
		// TODO Auto-generated method stub
		return null;
	}

	public JComponent getPanel() {
		// TODO Auto-generated method stub
		setFocusOnTextInput();
		return uiPanel;
	}
	
	public JToolBar getToolBar() {
		// TODO Auto-generated method stub
		return null;
	}

}
