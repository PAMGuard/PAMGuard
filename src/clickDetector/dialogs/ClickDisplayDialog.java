package clickDetector.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import Array.ArrayManager;
import Array.PamArray;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.warn.WarnOnce;
import clickDetector.BTDisplayParameters;
import clickDetector.ClickControl;
import clickDetector.ClickClassifiers.ClickIdentifier;

/**
 * Dialog for basic click display parameters
 * @author Doug
 *
 */
public class ClickDisplayDialog extends PamDialog implements ActionListener {

	static private ClickDisplayDialog singleInstance;
	
	private BTDisplayParameters btDisplayParameters;
	
	private ClickControl clickControl;
	
	private JRadioButton showBearing, showAmplitude, showICI, showSlant;
	private JTextField minICI, maxICI, amplitudeRange0, amplitudeRange1, hGridiLines;
	private JCheckBox trackedClickMarkers;
	private JCheckBox view360;
//	private JCheckBox bearingToArray;
//	private JCheckBox bearingToShip;
	private JCheckBox showEchoes, showUnassignedICI, logICIScale;
	
	private BTPanel btPanel;
	private SizePanel sizePanel;
	private SpeciesPanel speciesPanel;
	private JComboBox<String> angleTypes;
	
	private ClickDisplayDialog(Window  owner) {

		super(owner, "Click Display Parameters", true);

		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		p.setLayout(new BorderLayout());
		
		// put in  a tab control
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Axis", btPanel = new BTPanel());
		tabbedPane.addTab("Click Size", sizePanel = new SizePanel());
		tabbedPane.addTab("Species", speciesPanel = new SpeciesPanel());
		
		p.add(BorderLayout.CENTER, tabbedPane);

		showBearing.addActionListener(this);
		showAmplitude.addActionListener(this);
		showICI.addActionListener(this);
		showSlant.addActionListener(this);
		logICIScale.addActionListener(this);

		setDialogComponent(p);
		
		setHelpPoint("detectors.clickDetectorHelp.docs.ClickDetector_clickDetectorDisplays");

	}

	public static BTDisplayParameters showDialog(ClickControl clickControl, Window parentFrame, BTDisplayParameters btDisplayParameters) {
		if (singleInstance == null || singleInstance.getOwner() != parentFrame) {
			singleInstance = new ClickDisplayDialog(parentFrame);
		}
		singleInstance.clickControl = clickControl;
		singleInstance.btDisplayParameters = btDisplayParameters.clone();
		singleInstance.setParams(btDisplayParameters);
		singleInstance.setVisible(true);
		return singleInstance.btDisplayParameters;
	}

	@Override
	public void cancelButtonPressed() {
		btDisplayParameters = null;
	}
	
	private void setParams(BTDisplayParameters btDisplayParameters) {
		btPanel.setParams(btDisplayParameters);
		sizePanel.setParams(btDisplayParameters);
		speciesPanel.setParams(btDisplayParameters);
		pack();
	}
	
	@Override
	public boolean getParams() {

		if (btPanel.getParams() == false) return false;
		if (sizePanel.getParams() == false) return false;
		if (speciesPanel.getParams() == false) return false;
		
		return true;
	}
	
	@Override
	public void restoreDefaultSettings() {

		setParams(new BTDisplayParameters());
		
	}

	void setHorzGrid(int vScale)
	{
		int nG = 0;
		switch(vScale) {
		case BTDisplayParameters.DISPLAY_BEARING:
			nG = btDisplayParameters.nBearingGridLines;
			break;
		case BTDisplayParameters.DISPLAY_AMPLITUDE:
			nG = btDisplayParameters.nAmplitudeGridLines;
			break;
		case BTDisplayParameters.DISPLAY_ICI:
			nG = btDisplayParameters.nICIGridLines;
			break;
		}
		hGridiLines.setText(String.format("%d",nG));
	}
	private int getVScale() {
		if (showBearing.isSelected()) return BTDisplayParameters.DISPLAY_BEARING;
		else if (showAmplitude.isSelected()) return BTDisplayParameters.DISPLAY_AMPLITUDE;
		else if (showICI.isSelected()) return BTDisplayParameters.DISPLAY_ICI;
		else if (showSlant.isSelected()) return BTDisplayParameters.DISPLAY_SLANT;
		return BTDisplayParameters.DISPLAY_BEARING;
	}

	private void enableControls(){
		int vS = getVScale();
//		logICIScale.setSelected(false);
//		logICIScale.setEnabled(false);
		boolean array2D = false;
		PamArray currentArray = ArrayManager.getArrayManager().getCurrentArray();
		if (currentArray != null) {
			array2D = currentArray.getArrayShape() >= ArrayManager.ARRAY_TYPE_PLANE;
		}
		
		maxICI.setEnabled(vS == BTDisplayParameters.DISPLAY_ICI);
		
		showUnassignedICI.setEnabled(vS == BTDisplayParameters.DISPLAY_ICI);
		minICI.setEnabled(logICIScale.isSelected());
		amplitudeRange0.setEnabled(vS == BTDisplayParameters.DISPLAY_AMPLITUDE);
		amplitudeRange1.setEnabled(vS == BTDisplayParameters.DISPLAY_AMPLITUDE);
		view360.setEnabled(vS == BTDisplayParameters.DISPLAY_BEARING && array2D);
		if (array2D == false) {
			view360.setSelected(false);
//			bearingToArray.setSelected(true);
//			bearingToShip.setSelected(false);
		}
		angleTypes.setEnabled(vS == BTDisplayParameters.DISPLAY_BEARING || vS == BTDisplayParameters.DISPLAY_SLANT);
//		bearingToArray.setEnabled(vS == BTDisplayParameters.DISPLAY_BEARING && array2D);
//		bearingToShip.setEnabled(vS == BTDisplayParameters.DISPLAY_BEARING && array2D);
		
	}
	
	class BTPanel extends JPanel {

		BTPanel() {
			
			setLayout(new BorderLayout());
			setBorder(new TitledBorder("Axis layout"));
			
			JPanel t = new JPanel();

			ButtonGroup g = new ButtonGroup();
			showBearing = new JRadioButton("Bearing");
			showAmplitude = new JRadioButton("Amplitude");
			showICI = new JRadioButton("ICI");
			showSlant = new JRadioButton("Slant Angle");
			angleTypes = new JComboBox<String>();
			g.add(showBearing);
			g.add(showAmplitude);
			g.add(showICI);
			g.add(showSlant);
			t.add(showBearing);
			t.add(showAmplitude);
			t.add(showICI);
			t.add(showSlant);
			
			JPanel p = new JPanel();
			p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			GridBagLayout layout = new GridBagLayout();
			GridBagConstraints c = new PamGridBagContraints();
			p.setLayout(layout);
			c.anchor = GridBagConstraints.WEST;
			
			c.gridy = 0;
			c.gridx = 0;
			addComponent(p, new JLabel("Bearing Range ", JLabel.RIGHT), c);
			c.gridx++;
			c.gridwidth = 4;
			addComponent(p, view360 = new JCheckBox("Full -180 to 180 degree view"), c);
			view360.setToolTipText("Applicable if you have a multi element array which gives " +
					"unambiguous bearings (planar and volumetric arrays only)");
			c.gridx = 0;
			c.gridwidth = 1;
			c.gridy++;
			p.add(new JLabel("Angle rotations ", JLabel.RIGHT), c);
			c.gridx++;
			c.gridwidth = 4;
			p.add(angleTypes, c);
			String[] at = BTDisplayParameters.angleTypeNames;
			for (int i = 0; i < at.length; i++) {
				angleTypes.addItem(at[i]);
			}
//			c.gridx = 0;
//			c.gridy++;
//			c.gridwidth = 1;
//			addComponent(p, new JLabel("Bearing Reference"), c);
//			c.gridx++;
//			addComponent(p, bearingToArray = new JCheckBox("To Array"), c);
//			c.gridx++;
//			c.gridwidth=2;
//			addComponent(p, bearingToShip = new JCheckBox("To Ship"), c);
//			bearingToArray.setToolTipText("Show bearings relative to the array (planar and volumetric arrays only)");
//			bearingToShip.setToolTipText("Show bearings relative to the ship or North for fixed systems (planar and volumetric arrays only)");
//			ButtonGroup bbg = new ButtonGroup();
//			bbg.add(bearingToArray);
//			bbg.add(bearingToShip);
			c.gridy++;
			c.gridx = 0;
			c.gridwidth = 3;
			addComponent(p, showUnassignedICI = new JCheckBox("Show ICI for unassigned clicks"), c);
			c.gridwidth = 1;
			
			
			c.gridwidth = 1;
			c.gridy++;
			c.gridx = 0;
			addComponent(p, new JLabel("ICI range "), c);
			c.gridx ++;
			addComponent(p, minICI = new JTextField(6), c);
			c.gridx ++;
			addComponent(p, new JLabel(" to "), c);
			c.gridx ++;
			addComponent(p, maxICI = new JTextField(6), c);
			c.gridx ++;
			addComponent(p, new JLabel(" s"), c);
			c.gridx++;
			addComponent(p, logICIScale = new JCheckBox("Log Scale"), c);
			
			c.gridy ++;
			c.gridx = 0;
			addComponent(p, new JLabel("Amplitude Range "), c);
			c.gridx = 1;
			addComponent(p, amplitudeRange0 = new JTextField(6), c);
			c.gridx = 2;
			addComponent(p, new JLabel(" to "), c);
			c.gridx ++;
			addComponent(p, amplitudeRange1 = new JTextField(6), c);
			c.gridx = 4;
			addComponent(p, new JLabel(" dB"), c);

			c.gridy ++;
			c.gridx = 0;
			addComponent(p, new JLabel("Grid lines "), c);
			c.gridx = 1;
			addComponent(p, hGridiLines = new JTextField(5), c);
			
			JPanel q = new JPanel();
			q.add(trackedClickMarkers = new JCheckBox("Show tracked click markers in border"));
			
			add(BorderLayout.NORTH, t);
			add(BorderLayout.CENTER, p);
			add(BorderLayout.SOUTH, q);
		}
		void addComponent(JPanel panel, Component p, GridBagConstraints constraints){
			((GridBagLayout) panel.getLayout()).setConstraints(p, constraints);
			panel.add(p);
		}
		
		void setParams(BTDisplayParameters btDisplayParameters){
			
			showBearing.setSelected(btDisplayParameters.VScale == BTDisplayParameters.DISPLAY_BEARING);
			showAmplitude.setSelected(btDisplayParameters.VScale == BTDisplayParameters.DISPLAY_AMPLITUDE);
			showICI.setSelected(btDisplayParameters.VScale == BTDisplayParameters.DISPLAY_ICI);
			showSlant.setSelected(btDisplayParameters.VScale == BTDisplayParameters.DISPLAY_SLANT);
			logICIScale.setSelected(btDisplayParameters.logICIScale);
			logICIScale.setToolTipText("Feature under development");
			showUnassignedICI.setSelected(btDisplayParameters.showUnassignedICI);
			//JTextField maxICI, amplitudeRange0, amplitudeRange1, hGridiLines;
			minICI.setText(String.format("%.3f", btDisplayParameters.minICI));
			maxICI.setText(String.format("%.3f", btDisplayParameters.maxICI));
			amplitudeRange0.setText(String.format("%.1f", btDisplayParameters.amplitudeRange[0]));
			amplitudeRange1.setText(String.format("%.1f", btDisplayParameters.amplitudeRange[1]));
			setHorzGrid(btDisplayParameters.VScale);
			trackedClickMarkers.setSelected(btDisplayParameters.trackedClickMarkers);
			view360.setSelected(btDisplayParameters.view360);
//			bearingToArray.setSelected(btDisplayParameters.bearingType == BTDisplayParameters.BEARING_FROMARRAY);
//			bearingToShip.setSelected(btDisplayParameters.bearingType == BTDisplayParameters.BEARING_FROMVESSEL);
			angleTypes.setSelectedIndex(btDisplayParameters.angleRotation);
			
			enableControls();
		}
		
		boolean getParams(){

			try {
				btDisplayParameters.logICIScale = logICIScale.isSelected();
				if (btDisplayParameters.logICIScale) {
					btDisplayParameters.minICI = Double.valueOf(minICI.getText());
					if (btDisplayParameters.minICI <= 0) {
						btDisplayParameters.minICI = 0.001;
					}
				}
				else {
					btDisplayParameters.minICI = 0;
				}
				btDisplayParameters.VScale = getVScale();
				btDisplayParameters.maxICI = Double.valueOf(maxICI.getText());
				btDisplayParameters.amplitudeRange[0] = Double.valueOf(amplitudeRange0.getText());
				btDisplayParameters.amplitudeRange[1] = Double.valueOf(amplitudeRange1.getText());
				btDisplayParameters.angleRotation = angleTypes.getSelectedIndex();
				
				int nG = Integer.valueOf(hGridiLines.getText());
				switch(btDisplayParameters.VScale) {
				case BTDisplayParameters.DISPLAY_BEARING:
					btDisplayParameters.nBearingGridLines = nG;
					break;
				case BTDisplayParameters.DISPLAY_AMPLITUDE:
					btDisplayParameters.nAmplitudeGridLines = nG;
					break;
				case BTDisplayParameters.DISPLAY_ICI:
					btDisplayParameters.nICIGridLines = nG;
					break;
				
				}
				btDisplayParameters.trackedClickMarkers = trackedClickMarkers.isSelected();
				btDisplayParameters.view360 = view360.isSelected();
				btDisplayParameters.showUnassignedICI = showUnassignedICI.isSelected();
				
//				if (bearingToShip.isSelected()) {
//					btDisplayParameters.bearingType = BTDisplayParameters.BEARING_FROMVESSEL;
//				}
//				else {
//					btDisplayParameters.bearingType = BTDisplayParameters.BEARING_FROMARRAY;
//				}
			}
			catch (Exception Ex) {
				return false;
			}
			if (angleTypes.isEnabled() && btDisplayParameters.angleRotation != BTDisplayParameters.ROTATE_TOARRAY) {
				String msg = "Displaying angles relative to the vessel or North may require significant additional processing which might " +
						"slow down the click detector. For max performance display angles relative to the array.";
				int ans = WarnOnce.showWarning("Angle rotations", msg, WarnOnce.OK_CANCEL_OPTION);
				if (ans == WarnOnce.CANCEL_OPTION) {
					return false;
				}
			}
			return true;
		}
	}
	
	// tab panel for click size stuff
	class SizePanel extends JPanel{
		
		JTextField minLength, maxLength, minHeight, maxHeight;
		
		SizePanel() {
			super();
			JPanel northPanel = new JPanel();
			northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
			this.setLayout(new BorderLayout());
			add(BorderLayout.NORTH, northPanel);
			
			JPanel h = new JPanel();
//			h.setLayout(new BoxLayout(h, BoxLayout.X_AXIS));
			h.setLayout(new FlowLayout());
			h.setBorder(new TitledBorder("Click Length"));
			h.add(new JLabel("min"));
			h.add(minLength = new JTextField(3));
			h.add(new JLabel(" max"));
			h.add(maxLength = new JTextField(3));
			h.add(new JLabel(" pixels "));
			
			JPanel l = new JPanel();
//			l.setLayout(new BoxLayout(l, BoxLayout.X_AXIS));
			l.setLayout(new FlowLayout());
			l.setBorder(new TitledBorder("Click Height"));
			l.add(new JLabel("min"));
			l.add(minHeight = new JTextField(3));
			l.add(new JLabel(" max "));
			l.add(maxHeight = new JTextField(3));
			l.add(new JLabel(" pixels"));
			
			northPanel.add(h);
			northPanel.add(l);
		}
		
		void setParams(BTDisplayParameters btDisplayParameters) {
			minHeight.setText(String.format("%d", btDisplayParameters.minClickHeight));
			maxHeight.setText(String.format("%d", btDisplayParameters.maxClickHeight));
			minLength.setText(String.format("%d", btDisplayParameters.minClickLength));
			maxLength.setText(String.format("%d", btDisplayParameters.maxClickLength));
		}
		
		public boolean getParams() {
			try {
				btDisplayParameters.minClickHeight = Integer.valueOf(minHeight.getText());
				btDisplayParameters.maxClickHeight = Integer.valueOf(maxHeight.getText());
				btDisplayParameters.minClickLength = Integer.valueOf(minLength.getText());
				btDisplayParameters.maxClickLength = Integer.valueOf(maxLength.getText());
			}
			catch (Exception Ex) {
				return false;
			}
			return true;
		}
	}
	
	class SpeciesPanel extends JPanel {
		JCheckBox showAll;
		JCheckBox[] species;
		ClickIdentifier clickIdentifier;
		String[] speciesList;
		JPanel centralPanel = new JPanel();
		JPanel centralEastPanel = new JPanel();
		JPanel northPanel = new JPanel();
		JButton selectAll, clearAll;
//		JRadioButton andEvents, orEvents;
//		JRadioButton anyEvents, onlyEvents;
		private JComboBox<String> andOrSelection;
		private JCheckBox clicksInAnEvent;
		SpeciesPanel () {
			super();
			setLayout(new BorderLayout());
			northPanel = new JPanel();
			northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
			northPanel.setBorder(new TitledBorder("Echoes"));
			northPanel.add(showEchoes = new JCheckBox("Show Echoes"));
			add(BorderLayout.NORTH, northPanel);
			
			JPanel centralOuterPanel = new JPanel(new BorderLayout());
			centralPanel.setLayout(new BoxLayout(centralPanel, BoxLayout.Y_AXIS));
			centralOuterPanel.setBorder(new TitledBorder("Species Selection"));
			
			add(BorderLayout.CENTER, centralOuterPanel);
			centralOuterPanel.add(BorderLayout.CENTER, centralPanel);
			
			centralEastPanel.setLayout(new GridBagLayout());
			GridBagConstraints c = new PamGridBagContraints();
			addComponent(centralEastPanel, selectAll = new JButton("Select All"), c);
			c.gridx++;
			addComponent(centralEastPanel, clearAll = new JButton("Clear All"), c);
			selectAll.addActionListener(new AutoSelect(true));
			clearAll.addActionListener(new AutoSelect(false));
			centralOuterPanel.add(BorderLayout.SOUTH, centralEastPanel);
			
			JPanel southPanel = new JPanel();
			southPanel.setBorder(new TitledBorder("Event Selection"));
			southPanel.setLayout(new GridBagLayout());
			c = new PamGridBagContraints();
			southPanel.add(andOrSelection = new JComboBox<String>());
			andOrSelection.addItem("AND");
			andOrSelection.addItem("OR");
//			andOrSelection.addActionListener(showClicks);
			southPanel.add(clicksInAnEvent = new JCheckBox("Event clicks only"));
//			clicksInAnEvent.addActionListener(showClicks);
//			addComponent(southPanel, andEvents = new JRadioButton("AND"), c);
//			c.gridx++;
//			addComponent(southPanel, orEvents = new JRadioButton("OR"), c);
//			c.gridx = 0;
//			c.gridy++;
//			addComponent(southPanel, anyEvents = new JRadioButton("All clicks, whether part of an event or not"), c);
//			c.gridx++;
//			addComponent(southPanel, onlyEvents = new JRadioButton("Clicks which are part of an event"), c);
			add(BorderLayout.SOUTH, southPanel);
//			ButtonGroup bg = new ButtonGroup();
//			bg.add(andEvents);
//			bg.add(orEvents);
//			bg = new ButtonGroup();
//			bg.add(anyEvents);
//			bg.add(onlyEvents);
		}
		void setParams(BTDisplayParameters btDisplayParameters) {
			centralPanel.removeAll();
			species = null;
			speciesList = null;
			showAll = new JCheckBox("Show unclassified clicks");
			showAll.addActionListener(new AllSpeciesListener());
			centralPanel.add(showAll);
			clickIdentifier = clickControl.getClickIdentifier();
			if (clickIdentifier != null && clickIdentifier.getSpeciesList() != null) {
				speciesList = clickIdentifier.getSpeciesList();
				species = new JCheckBox[speciesList.length];
				if (speciesList != null) {
					for (int i = 0; i < speciesList.length; i++) {
						centralPanel.add(species[i] = new JCheckBox(speciesList[i]));
					}
				}
			}
			showEchoes.setSelected(btDisplayParameters.showEchoes);
			if (species == null) {
				showAll.setSelected(true);
			}
			else {
				showAll.setSelected(btDisplayParameters.getShowSpecies(0));
				for (int i = 0; i < species.length; i++) {
						species[i].setSelected(btDisplayParameters.getShowSpecies(i+1));
				}
//				if (btDisplayParameters.showSpeciesList != null) {
//					for (int i = 0; i < Math.min(species.length, btDisplayParameters.showSpeciesList.length);i++) {
//						species[i].setSelected(btDisplayParameters.showSpeciesList[i]);
//					}
//				}
			}

			clicksInAnEvent.setSelected(btDisplayParameters.showEventsOnly);
			andOrSelection.setSelectedIndex(btDisplayParameters.showANDEvents ? 0: 1);
//			orEvents.setSelected(!btDisplayParameters.showANDEvents);
//			andEvents.setSelected(btDisplayParameters.showANDEvents);
//			anyEvents.setSelected(!btDisplayParameters.showEventsOnly);
//			onlyEvents.setSelected(btDisplayParameters.showEventsOnly);
			
			enableButtons();
		}
		boolean getParams() {
			btDisplayParameters.showEchoes = showEchoes.isSelected();
			btDisplayParameters.setShowSpecies(0, showAll.isSelected());
			if (species != null) {
				for (int i = 0; i < species.length; i++) {
					btDisplayParameters.setShowSpecies(i+1, species[i].isSelected());
				}
				
			}

			btDisplayParameters.showEventsOnly = clicksInAnEvent.isSelected();
			btDisplayParameters.showANDEvents = (andOrSelection.getSelectedIndex() == 0);
//			btDisplayParameters.showANDEvents = andEvents.isSelected();
//			btDisplayParameters.showEventsOnly = onlyEvents.isSelected();
			return true;
		}
		class AllSpeciesListener implements ActionListener {

			public void actionPerformed(ActionEvent e) {
				enableButtons();				
			}
			
		}
		void enableButtons() {
			boolean someSpecies = (species != null && species.length > 0);
			showAll.setEnabled(someSpecies);
			if (!someSpecies) {
				showAll.setSelected(true);
			}
			selectAll.setEnabled(someSpecies);
			clearAll.setEnabled(someSpecies);
		}
		class AutoSelect implements ActionListener {

			boolean select;
			public AutoSelect(boolean select) {
				super();
				this.select = select;
			}
			public void actionPerformed(ActionEvent e) {
				showAll.setSelected(select);
				if (species == null) return;
				for (int i = 0; i < species.length; i++) {
					species[i].setSelected(select);
				}
				
			}
			
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == showBearing) {
			setHorzGrid(getVScale());
			enableControls();
		}
		else if (e.getSource() == showAmplitude) {
			setHorzGrid(getVScale());
			enableControls();
		}
		else if (e.getSource() == showICI) {
			setHorzGrid(getVScale());
			enableControls();
		}		
		else if (e.getSource() == showSlant) {
			setHorzGrid(getVScale());
			enableControls();
		}
		else if (e.getSource() == logICIScale) {
			enableControls();
		}
		
	}

}
