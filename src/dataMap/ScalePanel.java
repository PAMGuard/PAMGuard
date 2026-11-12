package dataMap;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import PamView.dialog.PamCheckBox;
import PamView.dialog.PamDialog;
import PamView.dialog.PamGridBagContraints;
import PamView.dialog.PamLabel;
import PamView.hidingpanel.HidingDialogComponent;
import PamView.panel.PamBorderPanel;

public class ScalePanel extends HidingDialogComponent {
	
	private DataMapControl dataMapControl;
	private DataMapPanel dataMapPanel;
	
	private JSlider hScrollBar;
	private JLabel hScaleText;
	private JComboBox<String> vDropDown;
	private JCheckBox vLogScale;
	private double[] hScaleChoices = DataMapParameters.hScaleChoices;
	private PamBorderPanel borderPanel;

	public ScalePanel(DataMapControl dataMapControl, DataMapPanel dataMapPanel) {
		borderPanel = new PamBorderPanel();
		borderPanel.setBorder(new TitledBorder("Scales"));
		this.dataMapControl = dataMapControl;
		this.dataMapPanel = dataMapPanel;
		borderPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		PamDialog.addComponent(borderPanel, new PamLabel("Horizontal ", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(borderPanel, hScrollBar = new MapSlider(0, hScaleChoices.length-1), c);
		c.gridx++;
		PamDialog.addComponent(borderPanel, hScaleText = new PamLabel(""), c);
		c.gridx = 0;
		c.gridy++;
		PamDialog.addComponent(borderPanel, new PamLabel("Vertical ", SwingConstants.RIGHT), c);
		c.gridx++;
		PamDialog.addComponent(borderPanel, vDropDown = new JComboBox<String>(), c);
		c.gridx++;
		PamDialog.addComponent(borderPanel, vLogScale = new PamCheckBox("Log Scale"), c);
		
		Dimension d = hScrollBar.getPreferredSize();
		d.width = 0;
		hScrollBar.setPreferredSize(d);
		hScrollBar.setToolTipText("Change horizontal scaling");
		
//		hScrollBar.addAdjustmentListener(new HScrollListener());
		hScrollBar.addChangeListener(new HScrollListener());
//		hScrollBar.add
		
		vDropDown.addActionListener(new VerticalListener());
		vDropDown.setToolTipText("Change vertical scaling");
		vLogScale.addActionListener(new LogScaleListener());
		vLogScale.setToolTipText("Use a log vertical scale");
//		hScrollBar.setMaximum(hScaleChoices.length);
//		hScrollBar.setBlockIncrement(1);
//		hScrollBar.setUnitIncrement(1);
//		hScrollBar.setVisibleAmount(1);
		
		vDropDown.addItem("No Scaling");
		vDropDown.addItem("per Second");
		vDropDown.addItem("per Minute");
		vDropDown.addItem("per Hour");
		vDropDown.addItem("per Day");
		
		setParams(dataMapControl.dataMapParameters);
		
	}
	
	// use setting flag to avoid immediate callback which overwrites changes 2 and 3 !
	boolean setting = false;
	public void setParams(DataMapParameters dataMapParameters) {
		setting = true;
		hScrollBar.setValue(dataMapParameters.hScaleChoice);
		vDropDown.setSelectedIndex(dataMapParameters.vScaleChoice);
		vLogScale.setSelected(dataMapParameters.vLogScale);
		setting = false;
	}
	
	public void getParams(DataMapParameters dataMapParameters) {
		if (setting) return;
		dataMapParameters.hScaleChoice = hScrollBar.getValue();
		dataMapParameters.vScaleChoice = vDropDown.getSelectedIndex();
		dataMapParameters.vLogScale = vLogScale.isSelected();
	}

	private void sayHScale() {
		int hChoice = hScrollBar.getValue();
		hScaleText.setText(String.format("%s pixs/hour", Double.valueOf(hScaleChoices[hChoice]).toString()));
	}
	
	private class MapSlider extends JSlider {

		private static final long serialVersionUID = 1L;

		public MapSlider(int min, int max) {
			super(min, max);
			setMinorTickSpacing(1);
			setMajorTickSpacing(1);
			setExtent(1);
//			ActionMap actionMap = getActionMap();
//			Object[] keys = actionMap.allKeys();
//			System.out.println(keys.toString());
//			for (int i = 0; i < keys.length; i++) {
//				System.out.println(keys[i]);
//			}
//			Action a;
//			try {
//				 a = actionMap.get("positiveBlockIncrement");
//				 System.out.println(a.getValue("scrollIncrement"));
////				 a.
//				a.putValue("scrollIncrement", 1);
//				actionMap.get("negativeBlockIncrement").putValue("scrollIncrement", 1);
//			}
//			catch (Exception e) {
//				
//			}
		}
//	    @Override
//	    public void scrollByBlock(int direction) {
//	        // Define your custom block increment value
//	        int blockIncrement = 15; 
//	        
//	        int oldValue = getValue();
//	        int newValue = oldValue + (direction * blockIncrement);
//	        setValue(newValue);
//	    }
		
	}
	
	class HScrollListener implements AdjustmentListener, ChangeListener {
		@Override
		public void adjustmentValueChanged(AdjustmentEvent arg0) {
			sayHScale();
			dataMapPanel.scaleChanged();
		}

		@Override
		public void stateChanged(ChangeEvent arg0) {
			sayHScale();
			hScrollBar.setExtent(1);
			//this is the one that gets called, not adjustmentValueChanged!?
			dataMapPanel.scaleChanged();
		}
	}
	
	class VerticalListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			dataMapPanel.scaleChanged();			
		}
	}
	class LogScaleListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			dataMapPanel.scaleChanged();
		}
	}
	@Override
	public JComponent getComponent() {
		return borderPanel;
	}

	@Override
	public boolean canHide() {
		return true;
	}

	@Override
	public void showComponent(boolean visible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return "Time Scale";
	}
		
}
