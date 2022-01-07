package PamView;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamController.PamController;
import PamController.PamViewParameters;
import PamUtils.PamCalendar;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamguardMVC.PamDataBlock;

public class ViewTimesDialog extends PamDialog {

	private static ViewTimesDialog singleInstance;
	
	private PamViewParameters viewParameters;

	JTextField startTime, endTime;
	JTextField analStartTime, analEndTime;
	JCheckBox useAnalTime;
	JCheckBox[] useBoxes;
	JButton selectAll, clearAll;
	
	private ViewTimesDialog(Frame parentFrame) {
		super(parentFrame, "Pamguard viewer", false);
		
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(BorderLayout.NORTH, makeTimesPanel());
		p.add(BorderLayout.CENTER, makeModulesPanel());
		setDialogComponent(p);
	}
	
	private JPanel makeTimesPanel() {
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Pamguard Viewer Times"));
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = c.gridy = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		addComponent(p, new JLabel("Times must be in the format \"YYYY-MM-DD HH:MM:SS\""), c);
		c.gridy++;
		c.gridwidth = 1;
		addComponent(p, new JLabel("Start "), c);
		c.gridx++;
		addComponent(p, startTime = new JTextField(20), c);
		c.gridy++;
		c.gridx = 0;
		addComponent(p, new JLabel("End "), c);
		c.gridx++;
		addComponent(p, endTime = new JTextField(20), c);
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		addComponent(p, useAnalTime = new JCheckBox("Also select by the analysis local time"), c);
		useAnalTime.addActionListener(new UseAnalTimeListener());
		c.gridy++;
		c.gridwidth = 1;
		addComponent(p, new JLabel("Start "), c);
		c.gridx++;
		addComponent(p, analStartTime = new JTextField(20), c);
		c.gridy++;
		c.gridx = 0;
		addComponent(p, new JLabel("End "), c);
		c.gridx++;
		addComponent(p, analEndTime = new JTextField(20), c);
		return p;
	}
	
	private JPanel makeModulesPanel() {
		JPanel p = new JPanel();
		p.setBorder(new TitledBorder("Pamguard Modules"));
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		c.gridx = c.gridy = 0;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		addComponent(p, new JLabel("Select data to load"), c);
		c.gridy++;
		c.gridwidth = 2;
		// loop through all data that have loadable logging. 
		ArrayList<PamDataBlock> dataBlocks;
		dataBlocks = PamController.getInstance().getDataBlocks();
		useBoxes = new JCheckBox[dataBlocks.size()];
		PamDataBlock dataBlock;
		for (int i = 0; i < dataBlocks.size(); i++) {
			dataBlock = dataBlocks.get(i);
			if (dataBlock.getLogging() != null && dataBlock.getLogging().isCanView()) {
				addComponent(p, useBoxes[i] = new JCheckBox(dataBlock.getDataName()), c);
				c.gridy++;
			}
		}
		c.gridwidth = 1;
		addComponent(p, selectAll = new JButton("Select all"), c);
		c.gridx ++;
		addComponent(p, clearAll = new JButton("Clear all"), c);
		selectAll.addActionListener(new SelectAll());
		clearAll.addActionListener(new ClearAll());
		
		return p;
	}
	
	class SelectAll implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			setAllBoxes(true);
		}
		
	}
	class ClearAll implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			setAllBoxes(false);			
		}
		
	}
	
	private void setAllBoxes(boolean what) {
		if (useBoxes == null) {
			return;
		}
		for (int i = 0; i < useBoxes.length; i++) {
			if (useBoxes[i] != null) {
				useBoxes[i].setSelected(what);
			}
		}
	}
	public static PamViewParameters showDialog(Frame frame, PamViewParameters guiParameters) {
//		if (singleInstance == null || singleInstance.getParent() != frame) {
			singleInstance = new ViewTimesDialog(frame);
//		}
		singleInstance.viewParameters = guiParameters.clone();
		singleInstance.setParams();
		
		singleInstance.setVisible(true);
		
		return singleInstance.viewParameters;
	}

	@Override
	public void cancelButtonPressed() {

		viewParameters = null;

	}

	public void setParams() {
//		sayTimes("setParams: ");
		startTime.setText(PamCalendar.formatDBDateTime(viewParameters.viewStartTime));
		endTime.setText(PamCalendar.formatDBDateTime(viewParameters.viewEndTime));
		useAnalTime.setSelected(viewParameters.useAnalysisTime);
		analStartTime.setText(PamCalendar.formatDBDateTime(viewParameters.analStartTime));
		analEndTime.setText(PamCalendar.formatDBDateTime(viewParameters.analEndTime));


		ArrayList<PamDataBlock> dataBlocks;
		dataBlocks = PamController.getInstance().getDataBlocks();
		checkUseBoxes(dataBlocks);
		if (useBoxes == null) {
			useBoxes = new JCheckBox[dataBlocks.size()];
		}
		int n = Math.min(dataBlocks.size(), useBoxes.length);
		for (int i = 0; i < n; i++) {
			if ((viewParameters.useModules == null || 
					i >= viewParameters.useModules.length ||
					viewParameters.useModules[i]) & useBoxes[i] != null) {
				useBoxes[i].setSelected(true);
			}
		}
		
		enableControls();
		
//		sayTimes("setParams: ");
	}
	
	@Override
	public boolean getParams() {
		long t;
		
		t = checkTime(startTime.getText());
		if (t < 0) return false;
		viewParameters.viewStartTime = t;
		
		t = checkTime(endTime.getText());
		if (t < 0) return false;
		viewParameters.viewEndTime = t;
		
		viewParameters.useAnalysisTime = useAnalTime.isSelected();
		if (viewParameters.useAnalysisTime) {
			t = checkTime(analStartTime.getText());
			if (t < 0) return false;
			viewParameters.analStartTime = t;
			
			t = checkTime(analEndTime.getText());
			if (t < 0) return false;
			viewParameters.analEndTime = t;
		}
		
//		sayTimes("getParams: ");

		// now work out which modules actually want to get loaded
		 
		ArrayList<PamDataBlock> dataBlocks;
		dataBlocks = PamController.getInstance().getDataBlocks();
		PamDataBlock dataBlock;
		viewParameters.useModules = new boolean[dataBlocks.size()];
//		useBoxes = checkUseBoxes(useBoxes, dataBlocks.size());
		checkUseBoxes(dataBlocks);
		for (int i = 0; i < dataBlocks.size(); i++) {
			dataBlock = dataBlocks.get(i);
			if (useBoxes[i] != null) {
				viewParameters.useModules[i] = useBoxes[i].isSelected();
				dataBlock.getLogging().setLoadViewData(viewParameters.useModules[i]);
			}
		}
		
		return true;
	}
	
	private void checkUseBoxes(ArrayList<PamDataBlock> dataBlocks) {
		// TODO Auto-generated method stub
		
	}


	private long checkTime(String timeString) {
		long t = PamCalendar.msFromDateString(timeString);
		if (t < 0) {
			JOptionPane.showMessageDialog(this, "Invalid data format " + timeString, "Error", JOptionPane.ERROR_MESSAGE);
		}
		return t;
	}
	
	private void sayTimes(String st) {
		System.out.println(st + PamCalendar.formatDateTime(viewParameters.viewStartTime) + " is " + 
				viewParameters.viewStartTime + " now = " + PamCalendar.formatDateTime(System.currentTimeMillis()) +
				" or " + PamCalendar.formatDBDateTime(viewParameters.viewStartTime));
	}
	class UseAnalTimeListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			enableControls();
		}
		
	}
	private void enableControls() {
		analStartTime.setEnabled(useAnalTime.isSelected());
		analEndTime.setEnabled(useAnalTime.isSelected());
	}
	
	
	private Date getDate(String dateStr) {
//		SimpleDateFormat df = new SimpleDateFormat(PamCalendar.getDBDateFormatString());
		DateFormat df;// = DateFormat.getDateTimeInstance();
		df = new SimpleDateFormat("y-M-d H:m:s");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		Date d = null;
		try {
			d = df.parse(dateStr);
		}
		catch (java.text.ParseException ex) {
			JOptionPane.showMessageDialog(this, "Invalid data format", "Error", JOptionPane.ERROR_MESSAGE);
//			ex.printStackTrace();
			return null;
		}
		return d;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
