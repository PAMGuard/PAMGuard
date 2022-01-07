package Map.gridbaselayer;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import com.sun.mail.imap.protocol.BODYSTRUCTURE;

import PamUtils.PamFileChooser;
import PamView.PamGui;
import PamView.dialog.PamDialog;
import PamView.dialog.PamFileBrowser;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import ucar.nc2.dt.grid.gis.GridBoundariesExtractor;

public class GridbaseDialog extends PamDialog {

	private static GridbaseDialog singleInstance;
	private GridbaseControl gridbaseControl;
	private GridDialogPanel gridDialogPanel;
	private boolean answer = true;

	private GridbaseDialog(Window parentFrame, GridbaseControl gridbaseControl) {
		super(parentFrame, "Select Bathymetry Grid", false);
		this.gridbaseControl = gridbaseControl;
		gridDialogPanel = new GridDialogPanel(parentFrame, gridbaseControl);
		setDialogComponent(gridDialogPanel.getDialogComponent());
	}

	public static boolean showDialog(Window parentWindow, GridbaseControl gridbaseControl) {
		if (singleInstance == null || singleInstance.getOwner() != parentWindow || singleInstance.gridbaseControl != gridbaseControl) {
			singleInstance = new GridbaseDialog(parentWindow, gridbaseControl);
		}
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.answer;
	}

	private void setParams() {
		gridDialogPanel.setParams();
	}

	@Override
	public boolean getParams() {
		return gridDialogPanel.getParams();
	}

	@Override
	public void cancelButtonPressed() {
		answer = false;
	}


	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}


}
