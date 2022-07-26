package clickDetector;


import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;

@Deprecated // - not used
public class ClickToolBar implements ActionListener{
	
	private JToolBar toolBar;
	private JRadioButton showBearing, showAmplitude, showICI;
	private static ClickToolBar clickToolBar; 
	private ClickTabPanelControl clickTabPanelControl;

	
	private ClickToolBar(ClickTabPanelControl clickTabPanelControl) {

		this.clickTabPanelControl = clickTabPanelControl;
		toolBar = new JToolBar();
		ButtonGroup g = new ButtonGroup();
		
		toolBar.add(new JLabel("Vertical Axis: "));
		showBearing = new JRadioButton("Bearing", true);
		showAmplitude = new JRadioButton("Amplitude", false);
		showICI = new JRadioButton("ICI", false);
		
		g.add(showBearing);
		g.add(showAmplitude);
		g.add(showICI);
		showBearing.addActionListener(this);
		showAmplitude.addActionListener(this);
		showICI.addActionListener(this);
		
		showBearing.setMargin(new Insets(0, 0, 0, 0));
		showAmplitude.setMargin(new Insets(0, 0, 0, 0));
		showICI.setMargin(new Insets(0, 0, 0, 0));
		
		toolBar.add(showBearing);
		toolBar.add(showAmplitude);
		toolBar.add(showICI);
	}
	
	public void actionPerformed(ActionEvent e) {
		// get settings out of tool bar and notify ClickTabPanelControl
		readToolBar();
	}
	
	public boolean readToolBar() {
		if (clickTabPanelControl == null) return false; // no point !
////		ClickParameters clickParameters = clickTabPanelControl.clickControl.clickParameters.clone();
//		BTDisplayParameters btDisplayParameters = 
//		clickParameters.VScale = getVScale();
//		clickTabPanelControl.toolBarNotify(clickParameters);
		return true;
	}

	int getVScale() {
		if (showBearing.isSelected()) return BTDisplayParameters.DISPLAY_BEARING;
		else if (showAmplitude.isSelected()) return BTDisplayParameters.DISPLAY_AMPLITUDE;
		else if (showICI.isSelected()) return BTDisplayParameters.DISPLAY_ICI;
		return BTDisplayParameters.DISPLAY_BEARING;
	}
	
	public void setControls(ClickParameters clickParameters) {
//		showBearing.setSelected(btDisplayParameters.VScale == BTDisplayParameters.DISPLAY_BEARING);
//		showAmplitude.setSelected(btDisplayParameters.VScale == BTDisplayParameters.DISPLAY_AMPLITUDE);
//		showICI.setSelected(btDisplayParameters.VScale == BTDisplayParameters.DISPLAY_ICI);
	}
	
	public JToolBar getToolBar() {
		return toolBar;
	}
}
