package dataPlots.layout;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import dataPlots.TDControl;
import dataPlots.data.DataLineInfo;
import dataPlots.data.TDDataInfo;
import dataPlots.data.TDDataProvider;
import dataPlots.data.TDInfoMonitor;
import dataPlots.data.TDDataProviderRegister;
import PamView.dialog.PamButtonAlpha;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.hidingpanel.HidingDialogComponent;
import PamView.hidingpanel.HidingDialogPanel;
import PamView.hidingpanel.HidingGridBagContraints;

public class TDGraphHidingDialog extends HidingDialogComponent {

	private TDGraph tdGraph;
	
	private JPanel mainPanel;
	
	private JPanel axTypePanel;
	
	private JPanel dataEnablePanel;
		
	private JCheckBox yAutoScale;

	private TDControl tdControl;
	
	public JPopupMenu popMenu;
	
	private ArrayList<JRadioButton> axisCheckBoxes = new ArrayList<JRadioButton>();
	

	public TDGraphHidingDialog(TDControl tdControl, TDGraph tdGraph) {
		super();
		this.tdControl = tdControl;
		this.tdGraph = tdGraph;
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		
		GridBagConstraints c = new PamGridBagContraints();
		// stuff about the y axis. 
		JPanel yaxPanelBorder = new JPanel(new BorderLayout());
		JPanel yaxPanel = new JPanel();
		yaxPanelBorder.add(yaxPanel, BorderLayout.WEST);
		mainPanel.add(yaxPanelBorder);
		yaxPanelBorder.setBorder(new TitledBorder("Data Axis"));
		yaxPanel.setLayout(new GridBagLayout());
		c = new HidingGridBagContraints();
		axTypePanel = new JPanel();
//		axTypePanel.setLayout(new BoxLayout(axTypePanel, BoxLayout.Y_AXIS));
		axTypePanel.setLayout(new GridLayout(0, 1));
		yaxPanel.add(axTypePanel, c);
		c.gridy++;
		PamDialog.addComponent(yaxPanel, yAutoScale = new JCheckBox("Auto scale"), c);
		yAutoScale.setOpaque(false);
		yAutoScale.setMargin(HidingDialogPanel.getDefaultButtonInsets());
		yAutoScale.addActionListener(new YAutoScale());
				
		JPanel dataPanelBorder = new JPanel(new BorderLayout());
		dataEnablePanel = new JPanel(new GridBagLayout());
		dataPanelBorder.add(dataEnablePanel, BorderLayout.WEST);
		dataPanelBorder.setBorder(new TitledBorder("Show"));
		mainPanel.add(dataPanelBorder);
		
		remakeDialog();
		enableButtons();
	}
	
	/**
	 * Remake the dialog checkboxes after data have been added or removed from 
	 * it. 
	 */
	public void remakeDialog() {
		axTypePanel.removeAll();
		axisCheckBoxes.clear();
		ButtonGroup bg = new ButtonGroup();
		ArrayList<String> availData = tdGraph.getAvailableDataUnits();
		if (availData == null) {
			return;
		}
		for (int i = 0; i < availData.size(); i++) {
			JRadioButton checkBox = new JRadioButton(availData.get(i));
			String sources = tdGraph.getDataNamesForAxis(availData.get(i));
			axisCheckBoxes.add(checkBox);
			bg.add(checkBox);
			checkBox.addActionListener(new SelectDataSelected(checkBox, availData.get(i)));
			if (sources != null) {
				checkBox.setToolTipText(String.format("(%s)", sources));
			}
			else {
				checkBox.setEnabled(false);
			}
			checkBox.setOpaque(false);
			axTypePanel.add(checkBox);
		}
		checkAxisCheckBox();
		
		dataEnablePanel.removeAll();
		GridBagConstraints c = new PamGridBagContraints();
		for (TDDataInfo dataInfo:tdGraph.getDataList()) {
			JCheckBox dataCheckBox = new JCheckBox(dataInfo.getShortName());
			dataCheckBox.setOpaque(false);
			c.gridx = 0;
			dataEnablePanel.add(dataCheckBox, c);
			if (dataInfo.hasOptions()) {
				c.gridx++;
				JButton b = new PamButtonAlpha(CompoundHidingTabPane.settingsImage);
				dataEnablePanel.add(b, c);
				b.addActionListener(new DataSettings(dataInfo));
			}
			dataCheckBox.addActionListener(new DataEnable(dataCheckBox, dataInfo));
			dataCheckBox.setSelected(dataInfo.isShowing());
			c.gridy++;
		}
	}
	
	public void checkAxisCheckBox() {
		String currAx = tdGraph.graphParameters.currentAxisName;
		for (JRadioButton aCB:axisCheckBoxes) {
			aCB.setSelected(aCB.getText().equals(currAx));
		}	
	}
	
	

	public void enableButtons() {
	}
	
	@Override
	public JComponent getComponent() {
		return mainPanel;
	}


	@Override
	public boolean canHide() {
		if (popMenu != null && popMenu.isVisible()) {
			return false;
		}
		return true;
	}

	@Override
	public void showComponent(boolean visible) {
		yAutoScale.setSelected(tdGraph.graphParameters.autoScale);
	}

	
	/**
	 * Action for the auto scale check box
	 * @author Doug Gillespie
	 *
	 */
	private class YAutoScale implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			tdGraph.graphParameters.autoScale = yAutoScale.isSelected();
			tdGraph.checkAxis();
			tdControl.repaintAll();
		}
	}
	
	private class YAxisTypeSelection implements ActionListener {
		String axisName;
		public YAxisTypeSelection(String unitsType) {
			this.axisName = unitsType;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			tdGraph.setAxisName(axisName);
		}
	}

	private class SelectDataSelected implements ActionListener {

		private String axisName;
		private JRadioButton checkBox;

		public SelectDataSelected(JRadioButton checkBox2, String axisName) {
			this.axisName = axisName;
			this.checkBox = checkBox2;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (checkBox.isSelected()) {
				tdGraph.setAxisName(axisName);
				checkAxisCheckBox();
			}
		}
	}
	
	private class DataEnable implements ActionListener {
		private TDDataInfo dataInfo;
		private JCheckBox dataCheckBox;
		public DataEnable(JCheckBox dataCheckBox, TDDataInfo dataInfo) {
			super();
			this.dataCheckBox = dataCheckBox;
			this.dataInfo = dataInfo;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {	
			dataInfo.setShowing(dataCheckBox.isSelected());
		}
		
	}
	
	private class DataSettings implements ActionListener {

		private TDDataInfo dataInfo;

		public DataSettings(TDDataInfo dataInfo) {
			this.dataInfo = dataInfo;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			dataInfo.editOptions(SwingUtilities.getWindowAncestor(tdGraph.getGraphOuterPanel()));
		}
		
	}

	@Override
	public String getName() {
		return "Graph control";
	}
}
