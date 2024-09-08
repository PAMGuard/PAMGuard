package Map;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.kordamp.ikonli.swing.FontIcon;

import PamController.PamController;
import PamView.component.PamSettingsIconButton;
import PamView.dialog.PamDialog;
import PamguardMVC.PamDataBlock;
import PamguardMVC.dataSelector.DataSelector;

public class MapDetectionsDialog extends PamDialog {

	private MapDetectionsManager mapDetectionsManager;
	
	private MapDetectionsParameters mapDetectionsParameters;
	
	JCheckBox[] plotCheckBox;
	
	JTextField[] showTimes;
	
	JButton[] defaults;
	
	JCheckBox[] allAvailable;
	
	JCheckBox[] lookAhead;

	JCheckBox[] fades;
	
	JButton[] options;
	
	private MapDetectionsDialog(Frame parentFrame, MapDetectionsManager mapDetectionsManager) {
		
		super(parentFrame, "Data Overlay options", false);
		
		this.mapDetectionsManager = mapDetectionsManager;
		
		mapDetectionsParameters = mapDetectionsManager.getMapDetectionsParameters().clone();
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		JPanel dPanel = new JPanel();
		dPanel.setBorder(new TitledBorder("Data overlay options"));
		dPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.anchor = GridBagConstraints.CENTER;
		
		addComponent(dPanel, new JLabel("Data name"), c);
		c.gridx++;
		addComponent(dPanel, new JLabel(" Plot "), c);
		c.gridx++;
		addComponent(dPanel, new JLabel(" Time (s) "), c);
		c.gridx++;
		addComponent(dPanel, new JLabel(" Fade "), c);
		c.gridx++;
		addComponent(dPanel, new JLabel("  "), c);
		c.gridx++;
		addComponent(dPanel, new JLabel(" All "), c);
		c.gridx++;
		addComponent(dPanel, new JLabel(" Look Ahead "), c);

		
		int n = mapDetectionsParameters.mapDetectionDatas.size();
		plotCheckBox = new JCheckBox[n];
		showTimes = new JTextField[n];
		defaults = new JButton[n];
		allAvailable = new JCheckBox[n];
		lookAhead = new JCheckBox[n];
		options = new JButton[n];
		fades = new JCheckBox[n];
		
		
		MapDetectionData md;
//		ImageIcon settingsIcon = new ImageIcon(ClassLoader.getSystemResource("Resources/SettingsButtonSmall2.png"));
		FontIcon settingsIcon =  FontIcon.of(PamSettingsIconButton.SETTINGS_IKON, PamSettingsIconButton.NORMAL_SIZE, Color.DARK_GRAY);

		for (int i = 0; i < n; i++) {
			md = mapDetectionsParameters.mapDetectionDatas.get(i);
			if (md.dataBlock == null) {
				continue;
			}
			c.gridx = 0;
			c.gridy ++;
			c.anchor = GridBagConstraints.EAST;
			addComponent(dPanel, new JLabel(md.dataName), c);
			c.gridx++;
			c.anchor = GridBagConstraints.CENTER;
			addComponent(dPanel, plotCheckBox[i] = new JCheckBox(""), c);
			plotCheckBox[i].setSelected(md.select);
			plotCheckBox[i].addActionListener(new PlotEnabler(i));
			plotCheckBox[i].setToolTipText("Plot these data");
			c.gridx++;
			c.anchor = GridBagConstraints.CENTER;
			addComponent(dPanel, showTimes[i] = new JTextField(6), c);
			Double displaySeconds = (double) md.getDisplayMilliseconds()/1000.;
			DecimalFormat df = new DecimalFormat("#.##");
			showTimes[i].setText(df.format(displaySeconds));
			showTimes[i].setToolTipText("Enter the maximum time these data should display for");
			c.gridx++;
			addComponent(dPanel, fades[i] = new JCheckBox(""), c);
			fades[i].setSelected(md.fade);
//			fades[i].addActionListener(new AvailableEnabler(i));
			fades[i].setToolTipText("Fade lines over time");
			c.gridx++;
			addComponent(dPanel, defaults[i] = new JButton("default"), c);
			defaults[i].addActionListener(new DefaultAction(i));
			defaults[i].setToolTipText("Use the default map display time");
			c.gridx++;
			addComponent(dPanel, allAvailable[i] = new JCheckBox(""), c);
			allAvailable[i].setSelected(md.allAvailable);
			allAvailable[i].addActionListener(new AvailableEnabler(i));
			allAvailable[i].setToolTipText("Show all available data");
			c.gridx++;
			addComponent(dPanel, lookAhead[i] = new JCheckBox(""), c);
			lookAhead[i].setSelected(md.lookAhead);
			lookAhead[i].addActionListener(new AvailableEnabler(i));
			lookAhead[i].setToolTipText("Show data ahead of scrollbar (future data) instead of behind");
			c.gridx++;
			addComponent(dPanel, options[i] = new JButton(settingsIcon), c);
			options[i].addActionListener(new SelectSettings(i, md.dataBlock));
			if (md.dataBlock.getDataSelectCreator() != null) {
				options[i].setToolTipText("More data selection options");
			}
			
			
			
			enableRow(i);
		}
		
		mainPanel.add(BorderLayout.CENTER, dPanel);
		
		setDialogComponent(mainPanel);
	}
	
	public static MapDetectionsParameters showDialog(Frame parent, MapDetectionsManager mapDetectionsManager) {
		
		MapDetectionsDialog mapDetectionsDialog = new MapDetectionsDialog(parent, mapDetectionsManager);
		
		mapDetectionsDialog.setVisible(true); 
		
		return mapDetectionsDialog.mapDetectionsParameters;
	}

	@Override
	public void cancelButtonPressed() {

		mapDetectionsParameters = null;

	}

	@Override
	public boolean getParams() {
		
		int n = mapDetectionsParameters.mapDetectionDatas.size();
		
		MapDetectionData md;
		for (int i = 0; i < n; i++) {
			md = mapDetectionsParameters.mapDetectionDatas.get(i);
			if (md.dataBlock == null) {
				continue;
			}
			md.select = plotCheckBox[i].isSelected();
			try {
				double displaySeconds = Double.valueOf(showTimes[i].getText());
				md.setDisplayMilliseconds((long) (displaySeconds * 1000));
			}
			catch (NumberFormatException ex) {
				return false;
			}
			md.fade = fades[i].isSelected();
			md.allAvailable = allAvailable[i].isSelected();
			md.lookAhead = lookAhead[i].isSelected();
		}
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub

	}
	
	class PlotEnabler implements ActionListener {
		int iOverlay;

		public PlotEnabler(int overlay) {
			super();
			iOverlay = overlay;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			enableRow(iOverlay);
		}
	}
	
	private void enableRow(int iOverlay) {
		boolean e = plotCheckBox[iOverlay].isSelected();
		boolean e2 = allAvailable[iOverlay].isSelected();
		boolean viewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;
		showTimes[iOverlay].setEnabled(e && !e2);
		defaults[iOverlay].setEnabled(e && !e2);
		allAvailable[iOverlay].setEnabled(e);
		PamDataBlock pdb = 	mapDetectionsParameters.mapDetectionDatas.get(iOverlay).dataBlock;
		boolean e3 = e & pdb.getDataSelectCreator() != null;
		options[iOverlay].setEnabled(e3);
		options[iOverlay].setVisible(pdb.getDataSelectCreator() != null);
		fades[iOverlay].setEnabled(e && !e2);
		if (!fades[iOverlay].isEnabled()) {
			fades[iOverlay].setSelected(false);
		}
	}

	class DefaultAction implements ActionListener {
		int iOverlay;

		public DefaultAction(int overlay) {
			super();
			iOverlay = overlay;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			
			showTimes[iOverlay].setText(String.format("%d", mapDetectionsManager.getDefaultTime()));
			
		}
		
	}

	class AvailableEnabler implements ActionListener {
		int iOverlay;

		public AvailableEnabler(int overlay) {
			super();
			iOverlay = overlay;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			enableRow(iOverlay);
			
		}
		
	}
	
	class SelectSettings implements ActionListener {

		private int iBlock;
		private PamDataBlock dataBlock;

		public SelectSettings(int i, PamDataBlock dataBlock) {
			this.iBlock = i;
			this.dataBlock = dataBlock;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			String name = mapDetectionsManager.getUnitName();
			DataSelector ds = dataBlock.getDataSelector(name, false);
			if (ds == null) {
				return;
			}
			ds.showSelectDialog(getOwner());
		}
		
	}
}
