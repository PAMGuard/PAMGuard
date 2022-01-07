package dbht.offline;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import dbht.DbHtParameters;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;

public class DbHtSummaryDialog extends PamDialog {

	private static DbHtSummaryDialog singleInstance;
	
	private DbHtSummaryParams dbHtSummaryParams;
	
	private JTextField exportInterval;
	
	private DbHtSummaryDialog(Window parentFrame) {
		super(parentFrame, "Data Export", false);
		// TODO Auto-generated constructor stub
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new TitledBorder("Data Export Options"));
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(mainPanel, new JLabel("Data export interval ", JLabel.RIGHT), c);
		c.gridx++;
		addComponent(mainPanel, exportInterval = new JTextField(6), c);
		c.gridx++;
		addComponent(mainPanel, new JLabel(" s", JLabel.LEFT), c);
		
		setDialogComponent(mainPanel);
	}
	
	public static DbHtSummaryParams showDialog(Window window, DbHtSummaryParams dbHtSummaryParams) {
		if (singleInstance == null || singleInstance.getOwner() != window) {
			singleInstance = new DbHtSummaryDialog(window);
		}
		singleInstance.dbHtSummaryParams= dbHtSummaryParams.clone();
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.dbHtSummaryParams;
	}

	private void setParams() {
		exportInterval.setText(String.format("%d", dbHtSummaryParams.intervalSeconds));
	}

	@Override
	public void cancelButtonPressed() {
		dbHtSummaryParams = null;
	}

	@Override
	public boolean getParams() {
		try {
			dbHtSummaryParams.intervalSeconds = Integer.valueOf(exportInterval.getText());
		}
		catch (NumberFormatException e) {
			return showWarning("Invalid interval (must be a positive integer)");
		}
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}

}
