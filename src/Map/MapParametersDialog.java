package Map;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import Array.Hydrophone;
import Map.gridbaselayer.GridDialogPanel;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.panel.PamNorthPanel;



public class MapParametersDialog extends PamDialog {

	private static MapParametersDialog singleInstance;

	private MapParameters mapParameters;

	private JTextField trackShowtime, dataKeepTime, dataShowTime;
	
	private SpinnerNumberModel symbolSize;

	private FilePanel filePanel;
	
	private GridDialogPanel gridDialogPanel;
	
	private HydrophonePanel hydrophonePanel;

	private JCheckBox hydroCheckBox = (new JCheckBox("Show hydrophones", false));
	
	private JCheckBox shipCheckBox = new JCheckBox("Show ship");
	
	private JCheckBox colourByChannel = new JCheckBox("Colour by channel number");

	private JCheckBox keepShipOnMap = (new JCheckBox("Keep ship on map"));
	
	private JCheckBox keepShipCentred = new JCheckBox("Keep Ship Centred");
	
	private JCheckBox headingUp = new JCheckBox("Ship heading always up");
	
	private JCheckBox showSurface = new JCheckBox("Show sea surface");

	private MapFileManager mapFileManager;

	public JSpinner symbolSizeSpinner;
	
	private JCheckBox allow3D;

	public JComboBox<String> ringsType;

	public JTextField ringsRange;

	public JCheckBox showGrid;

	private SimpleMap simpleMap;

	private MapParametersDialog(java.awt.Window parentFrame, SimpleMap simpleMap) {

		super(parentFrame, "Map Options", true);
		
		this.simpleMap = simpleMap;

		OptionsPanel op = new OptionsPanel();
		hydrophonePanel = new HydrophonePanel();
		filePanel = new FilePanel(this);
		gridDialogPanel = new GridDialogPanel(parentFrame, simpleMap.getGridBaseControl());
		
		JPanel outerOptionsPanel = new JPanel();
		outerOptionsPanel.setLayout(new BoxLayout(outerOptionsPanel, BoxLayout.Y_AXIS));
		JPanel outerOptionsPanel2 = new JPanel(new BorderLayout());
		outerOptionsPanel.add(op);
		outerOptionsPanel.add(hydrophonePanel);
		outerOptionsPanel.add(new RotationPanel());
		outerOptionsPanel2.add(BorderLayout.NORTH, outerOptionsPanel);
				
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Options", outerOptionsPanel2);
		tabbedPane.addTab("Grid", new PamNorthPanel(new GridPanel()));
		tabbedPane.addTab("Raster File", new PamNorthPanel(gridDialogPanel.getDialogComponent()));
		tabbedPane.add("Contour File", filePanel);

//		JPanel  p = new JPanel(new BorderLayout());
//		p.add(BorderLayout.CENTER, t);
//		p.add(BorderLayout.SOUTH, );
		setDialogComponent(tabbedPane);
		setHelpPoint("mapping.mapHelp.docs.overview");
		//		this.enableHelpButton(true);

		//hydroCheckBox.get
	}
//	MapParameters oldParameters, MapFileManager mapFile

	public static MapParameters showDialog(java.awt.Window parentFrame, SimpleMap simpleMap) {
		if (singleInstance == null || singleInstance.simpleMap != simpleMap) {
			singleInstance = new MapParametersDialog(parentFrame, simpleMap);
		}
		singleInstance.mapParameters = simpleMap.mapParameters.clone();
		singleInstance.mapFileManager = simpleMap.mapFileManager;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.mapParameters;
	}



	private void setParams() {
		trackShowtime.setText(String.format("%d", mapParameters.trackShowTime));
		dataKeepTime.setText(String.format("%d", mapParameters.dataKeepTime));
		dataShowTime.setText(String.format("%d", mapParameters.dataShowTime));

		shipCheckBox.setSelected(!mapParameters.hideShip);
		hydroCheckBox.setSelected(mapParameters.showHydrophones);
		colourByChannel.setSelected(mapParameters.colourHydrophonesByChannel);
		symbolSize.setValue(Math.max(mapParameters.symbolSize, (Integer)symbolSize.getMinimum()));
		keepShipOnMap.setSelected(mapParameters.keepShipOnMap);
		keepShipCentred.setSelected(mapParameters.keepShipCentred);
		headingUp.setSelected(mapParameters.headingUp);
		showSurface.setSelected(mapParameters.hideSurface == false);

		filePanel.setMapFile(mapParameters.mapFile);
		
		allow3D.setSelected(mapParameters.allow3D);
		
		ringsType.setSelectedIndex(mapParameters.showRangeRings);
		ringsRange.setText(String.format("%3.1f", mapParameters.rangeRingDistance));
		
		showGrid.setSelected(!mapParameters.hideGrid);
		
		gridDialogPanel.setParams();

		enableControls();
	}

	@Override
	public boolean getParams() {
		try {
			mapParameters.trackShowTime = Integer.valueOf(trackShowtime.getText());
			mapParameters.dataKeepTime = Integer.valueOf(dataKeepTime.getText());
			mapParameters.dataShowTime = Integer.valueOf(dataShowTime.getText());
			mapParameters.hideShip = !shipCheckBox.isSelected();
			mapParameters.showHydrophones = hydroCheckBox.isSelected();
			mapParameters.colourHydrophonesByChannel = colourByChannel.isSelected();
			mapParameters.keepShipOnMap = keepShipOnMap.isSelected();
			mapParameters.keepShipCentred = keepShipCentred.isSelected();
			mapParameters.headingUp = headingUp.isSelected();
			mapParameters.hideSurface = showSurface.isSelected() == false;
			mapParameters.symbolSize = forceEven((Integer) symbolSize.getValue());
			mapParameters.showRangeRings = ringsType.getSelectedIndex();
			if (mapParameters.showRangeRings > 0) {
				mapParameters.rangeRingDistance = Double.valueOf(ringsRange.getText());
				if (mapParameters.rangeRingDistance <= 0.) {
					return showWarning("Range rings sepration must be > 0");
				}
			}
		}
		catch (Exception Ex) {
			return false;
		}
		mapParameters.mapFile = filePanel.getMapFile();
		mapParameters.mapContours = filePanel.getContoursList();
		mapParameters.allow3D = allow3D.isSelected();
		mapParameters.hideGrid = !showGrid.isSelected();
		if (gridDialogPanel.getParams() == false) {
			return false;
		}
		return true;
	}

	@Override
	public void cancelButtonPressed() {
		mapParameters = null;
	}

	@Override
	public void restoreDefaultSettings() {
		mapParameters = new MapParameters();
		setParams();

	}
	
	private class AnyAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			enableControls();
		}
	}

	class OptionsPanel extends JPanel {
		public OptionsPanel() {
			setBorder(new TitledBorder("Data Options"));
			GridBagLayout layout;
			setLayout(layout = new GridBagLayout());
			GridBagConstraints constraints = new PamGridBagContraints();
			//		t.setLayout(new GridLayout(4,3));
			constraints.anchor = GridBagConstraints.WEST;
			constraints.gridx = 0;
			constraints.gridy = 0;
			addComponent(this, new JLabel("Track Length "), constraints);
			constraints.gridx ++;
			addComponent(this,trackShowtime = new JTextField(7), constraints);
			constraints.gridx ++;
			addComponent(this,new JLabel(" s"), constraints);
			constraints.gridx = 0;
			constraints.gridy ++;
			addComponent(this,new JLabel("Data storage time "), constraints);
			constraints.gridx ++;
			addComponent(this,dataKeepTime = new JTextField(7), constraints);
			constraints.gridx ++;
			addComponent(this,new JLabel(" s"), constraints);
			constraints.gridx = 0;
			constraints.gridy ++;
			addComponent(this,new JLabel("Data display time "), constraints);
			constraints.gridx ++;
			addComponent(this,dataShowTime = new JTextField(7), constraints);
			constraints.gridx ++;
			addComponent(this,new JLabel(" s"), constraints);
			constraints.gridx = 0;
			constraints.gridy ++;
			constraints.gridwidth = 3;

			addComponent(this,shipCheckBox, constraints);
			constraints.gridy ++;
			addComponent(this,keepShipOnMap, constraints);
			constraints.gridx = 0;
			constraints.gridy ++;
			constraints.gridwidth = 3;
			addComponent(this,keepShipCentred, constraints);
			constraints.gridy ++;
			addComponent(this,headingUp, constraints);
			constraints.gridy ++;
			addComponent(this, showSurface, constraints);
		}
	}

	class HydrophonePanel extends JPanel {

		public HydrophonePanel() {
			super();
			setBorder(new TitledBorder("Hydrophone Options"));
			setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			c.gridx = 0;
			c.gridy ++;
			c.gridwidth = 3;
			addComponent(this,hydroCheckBox, c);
			c.gridy++;
			addComponent(this, colourByChannel, c);
			c.gridy++;
			c.gridwidth = 1;
			addComponent(this, new JLabel("Symbol size ",  JLabel.RIGHT), c);
			c.gridx++;
			symbolSize = new SpinnerNumberModel(Hydrophone.DefaultSymbolSize, 4, 30, 2);
			addComponent(this, symbolSizeSpinner = new JSpinner(symbolSize), c);
			
			hydroCheckBox.addActionListener(new AnyAction());
		}
		
	}
	
	class RotationPanel extends JPanel {
		public RotationPanel() {
			setBorder(new TitledBorder("3D Rotation"));
			setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			c.gridx = c.gridy = 0;
			this.add(allow3D = new JCheckBox("Enable 3D rotation"), c);
			allow3D.setToolTipText("To use 3D rotation, hold down the Shift key and drag the mouse across the map display");
		}
	}
	
	/**
	 * Panel for showing rings at fixed ranges. 
	 * @author dg50
	 *
	 */
	class GridPanel extends JPanel {
		private GridPanel() {
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			JPanel ringsPanel = new JPanel();
			ringsPanel.setBorder(new TitledBorder("Range Rings"));
			ringsPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			ringsPanel.add(new JLabel("Show ", JLabel.RIGHT), c);
			c.gridx++;
			ringsPanel.add(ringsType = new JComboBox<String>(), c);
			ringsType.addItem("No range rings");
			ringsType.addItem("Rings in Metres");
			ringsType.addItem("Rings in Kilometres");
			ringsType.addItem("Rings in Nautical miles");
			c.gridx = 0;
			c.gridy++;
			ringsPanel.add(new JLabel("Sepration ", JLabel.RIGHT), c);
			c.gridx++;
			ringsPanel.add(ringsRange = new JTextField(6), c);
			ringsType.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					enableControls();
				}
			});
			
			JPanel gridPanel = new JPanel(new GridBagLayout());
			gridPanel.setBorder(new TitledBorder("Grid"));
			showGrid = new JCheckBox("Show Latitude and Longitude Lines");
			c = new PamGridBagContraints();
			gridPanel.add(showGrid, c);
			
			add(gridPanel);
			add(ringsPanel);
		}
	}
	
	private int forceEven(int aNumber) {
		int ans = aNumber/2;
		return ans*2;
	}
	
	class FilePanel extends JPanel {
		private JTextField mapName;
		JButton browseButton, clearButton, allButton, noneButton;
		GetMapFile getMapFile = new GetMapFile();
		JPanel contourPanel;
		JCheckBox[] contourCheck;
		File mapFile;
		MapParametersDialog mapParametersDialog;

		FilePanel(MapParametersDialog mapParametersDialog) {
			this.mapParametersDialog = mapParametersDialog;
			this.setLayout(new BorderLayout());
			//			this.add(BorderLayout.NORTH, new JLabel("ASCII Map File ..."));
			JPanel top = new JPanel(new BorderLayout());
			top.setBorder(new TitledBorder("Gebco ASCII File "));
			top.add(BorderLayout.CENTER, mapName = new JTextField());

			JPanel p = new JPanel();
			JPanel p2 = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.RIGHT));
			p.add(clearButton = new JButton("Clear"));
			clearButton.addActionListener(new ClearButton());
			p.add(browseButton = new JButton("Browse"));
			browseButton.addActionListener(new BrowseButton());
			p2.setLayout(new BorderLayout());
			p2.add(BorderLayout.EAST, p);
			top.add(BorderLayout.SOUTH, p2);
			this.add(BorderLayout.NORTH, top);

			JPanel centPanel = new JPanel(new BorderLayout());
			centPanel.setBorder(new TitledBorder("Contours"));
			contourPanel = new JPanel();
			JScrollPane scrollPane = new JScrollPane(contourPanel);
			scrollPane.setPreferredSize(new Dimension(0, 200));
			//			p2.add(BorderLayout.CENTER, contourPanel = new JPanel());
			centPanel.add(BorderLayout.CENTER, scrollPane);
			JPanel centRight = new JPanel(new BorderLayout());
			centRight.setBorder(new EmptyBorder(new Insets(0,5,0,5)));
			JPanel centTopRight = new JPanel(new GridLayout(2,1));
			centTopRight.add(allButton = new JButton("Select All"));
			allButton.addActionListener(new AllButton());
			centTopRight.add(noneButton = new JButton("Select None"));
			noneButton.addActionListener(new NoneButton());
			centRight.add(BorderLayout.NORTH, centTopRight);
			centPanel.add(BorderLayout.EAST, centRight);
			this.add(BorderLayout.CENTER, centPanel);
		}

		public void setMapFile(File file) {
			if (file == null) return;
			mapFile = file;
			mapName.setText(file.getAbsolutePath());
			if (file.exists()) {
				mapFileManager.readFileData(file);
				fillContourPanel();
			}
		}
		public File getMapFile() {
			if (mapName.getText() == null || mapName.getText().length() == 0) {
				return null;	
			}
			else {
				return new File(mapName.getText());
			}
		}

		boolean[] getContoursList() {
			if (contourCheck == null) return null;
			boolean[] cl = new boolean[contourCheck.length];
			for (int i = 0; i < contourCheck.length; i++) {
				cl[i] = contourCheck[i].isSelected();
			}
			return cl;
		}

		private void fillContourPanel() {
			contourPanel.removeAll();
//			contourPanel.add(new JLabel("Contours"));
			contourPanel.setLayout(new BoxLayout(contourPanel, BoxLayout.Y_AXIS));
			Vector<Integer> availableContours = mapFileManager.getAvailableContours();
			if (availableContours == null) return;
			if (mapParameters.mapContours == null || mapParameters.mapContours.length <
					availableContours.size()) {
				mapParameters.mapContours = new boolean[availableContours.size()];
			}
			contourCheck = new JCheckBox[availableContours.size()];
			String name;
			for (int i = 0; i < availableContours.size(); i++) {
				name = String.format("%d m", availableContours.get(i));
				if (availableContours.get(i) == 0) {
					name += " (coast)";
				}
				if (availableContours.get(i) == -1){
					name += " (ice edge)";
				}
				contourPanel.add(contourCheck[i] = 
					new JCheckBox(name));
				contourCheck[i].setSelected(mapParameters.mapContours[i]);
			}
			mapParametersDialog.pack();
			//			invalidate();
		}
		private void browseMaps() {
			File newFile = mapFileManager.selectMapFile(getMapFile());
			if (newFile != null) {
				mapName.setText(newFile.getAbsolutePath());
				mapFileManager.readFileData(newFile);
				//				Vector<Double> availableContours = mapFile.getAvailableContours();
				fillContourPanel();
			}
		}
		private class BrowseButton implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				browseMaps();
			}
		}
		private class ClearButton implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				clearMap();
			}
		}
		private class AllButton implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				selectAllContours(true);
			}
		}
		private class NoneButton implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				selectAllContours(false);
			}

		}
		private void selectAllContours(boolean b) {
			if (contourCheck == null) {
				return;
			}
			for (int i = 0; i < contourCheck.length; i++) {
				contourCheck[i].setSelected(b);
			}
		}
		private void clearMap() {
			mapName.setText("");
			mapFileManager.clearFileData();
			fillContourPanel();
		}
	}
	public void enableControls() {
		colourByChannel.setEnabled(hydroCheckBox.isSelected());
		symbolSizeSpinner.setEnabled(hydroCheckBox.isSelected());
		ringsRange.setEnabled(ringsType.getSelectedIndex() > 0);
	}



}
