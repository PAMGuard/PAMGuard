package Map.gridbaselayer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import PamUtils.LatLong;
import PamView.PamGui;
import PamView.dialog.PamDialog;
import PamView.dialog.PamDialogPanel;
import PamView.dialog.PamFileBrowser;
import PamView.dialog.PamGridBagContraints;

public class GridDialogPanel implements PamDialogPanel{

	private JPanel mainPanel;
	private GridbaseControl gridbaseControl;

	private GridbaseParameters gridbaseParameters;

	private JTextField fileLabel;
	private JButton fileButton, clearButton;
	private Window ownerWindow;
	private JLabel latRange, lonRange, maxHeight, maxDepth;
	

	public GridDialogPanel(Window owner, GridbaseControl gridbaseControl) {
		this.ownerWindow = owner;
		this.gridbaseControl = gridbaseControl;
		mainPanel = new JPanel() ;
		mainPanel.setBorder(new TitledBorder("File Selection"));
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		fileButton = new JButton("Browse");
		clearButton = new JButton("Clear");
		//		c.gridx++;
		//		mainPanel.add();
		//		c.gridx = 0;
		c.gridwidth = 2;
		mainPanel.add(new JLabel("Select NETCDF File "), c);
		c.gridy++;
		c.gridwidth = 2;
		mainPanel.add(fileLabel = new JTextField(), c);
		fileLabel.setEditable(false);
		c.gridy++;
		JPanel buttonPanel = new JPanel(new BorderLayout());
		JPanel bPanel2 = new JPanel(new FlowLayout());
		bPanel2.add(clearButton);
		bPanel2.add(fileButton);
		buttonPanel.add(BorderLayout.EAST, bPanel2);
		mainPanel.add(buttonPanel, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		mainPanel.add(new JLabel("Latitude: ", JLabel.RIGHT),c);
		c.gridx++;
		mainPanel.add(latRange = new JLabel(" "), c); 
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		mainPanel.add(new JLabel("Longitude: ", JLabel.RIGHT),c);
		c.gridx++;
		mainPanel.add(lonRange = new JLabel(), c); 
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		mainPanel.add(new JLabel("Max land height: ", JLabel.RIGHT),c);
		c.gridx++;
		mainPanel.add(maxHeight = new JLabel(), c); 
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		mainPanel.add(new JLabel("Max water depth: ", JLabel.RIGHT),c);
		c.gridx++;
		mainPanel.add(maxDepth = new JLabel(), c); 
		
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		JLabel urlLabel;
		mainPanel.add(urlLabel = new JLabel("<html>Suitable files can be created at <br>" + GridbaseControl.bodcURL),c);
		urlLabel.setForeground(Color.BLUE);
		urlLabel.setToolTipText("Click to follow link to " + GridbaseControl.bodcURL);
		try {
			urlLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
		catch (Exception e) {}
		//		Font uFont = urlLabel.getFont();
		//		PamLabel
		//		uFont = new Font(name, style, size)
		urlLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					openURL();
				}
			}

		});
		fileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectFile();
			}
		});
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearFile();
			}
		});
	}

	protected void clearFile() {
		fileLabel.setText("");
	}

	private void selectFile() {
		String selFile = PamFileBrowser.fileBrowser(ownerWindow, gridbaseParameters.netCDFFile, PamFileBrowser.OPEN_FILE, ".nc");
		if (selFile != null) {
			fileLabel.setText(selFile);
			sayRange();
		}

	}
	
	private void sayRange() {
		String fn = fileLabel.getText();
		GebcoNETCDF gebfile = GebcoNETCDF.makeGebcoNCDFFile(fn);
		if (gebfile == null) {
			latRange.setText("-");
			lonRange.setText("-");
			maxHeight.setText("-");
			maxDepth.setText("-");
			return;
		}

		if (gebfile==null) return; 
		
		double[] lats = gebfile.getLatRange();
		int nLat = gebfile.getnLat();
		double[] lons = gebfile.getLonRange();
		int nLon = gebfile.getnLon();
		double[] ele = gebfile.getElevationRange();
		try {
			latRange.setText(String.format("%s - %s (%d points)", LatLong.formatLatitude(lats[0]), LatLong.formatLatitude(lats[1]), nLat));
			lonRange.setText(String.format("%s - %s (%d points)", LatLong.formatLongitude(lons[0]), LatLong.formatLongitude(lons[1]), nLon));
			maxDepth.setText(String.format("%3.0fm", -ele[0]));
			maxHeight.setText(String.format("%3.0fm", ele[1]));
		}
		catch (Exception e) {
			latRange.setText("Error in file");
			lonRange.setText("Error in file");
			maxHeight.setText("-");
			maxDepth.setText("-");
		}
	}

	protected void openURL() {
		PamGui.openURL(GridbaseControl.bodcURL);
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		gridbaseParameters = gridbaseControl.getGridbaseParameters().clone();
		fileLabel.setText(gridbaseParameters.netCDFFile);
		sayRange();
	}

	@Override
	public boolean getParams() {
		String fn = fileLabel.getText();
		//		if (fn == null || fn.length() == 0) {
		//			return PamDialog.showWarning(ownerWindow, "Map grid selection", "No NetCDF bathymetry File selected");
		//			return true; // it's OK to not have  afile. 
		//		}
		if (fn != null && fn.length() > 0) {
			File file = new File(fn);
			if (file.exists() == false) {
				return PamDialog.showWarning(ownerWindow, "Map file selection", "The ap raster file " + fn + " does not exist");
			}
		}
		gridbaseParameters.netCDFFile = fn;
		GebcoNETCDF ncFile = GebcoNETCDF.makeGebcoNCDFFile(fn);
		gridbaseControl.setGridbaseParameters(gridbaseParameters);
		return true;
	}

}
