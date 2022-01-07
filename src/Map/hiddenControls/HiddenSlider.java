package Map.hiddenControls;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import Map.MapParametersDialog;
import Map.SimpleMap;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamView.dialog.PamCheckBox;
import PamView.dialog.PamLabel;
import PamView.dialog.PamTextDisplay;
import PamView.hidingpanel.HidingDialog;
import PamView.hidingpanel.HidingDialogComponent;
import PamView.sliders.PamLogSlider;
import PamView.sliders.PamSliderLinearScale;
import PamView.sliders.PamSliderLogScale;
import PamView.sliders.TransparentPanel;

public class HiddenSlider extends HidingDialogComponent {

	private SimpleMap simpleMap;
	
	private JPanel controlPanel;
	
	private PamLogSlider timeSlider;
	
	private PamTextDisplay timeLabel;

	private PamTextDisplay maxTimeLabel;

	private PamTextDisplay minTimeLabel;

	private JCheckBox logBox;
	
	public HiddenSlider(SimpleMap simpleMap) {
		super();
		this.simpleMap = simpleMap;
		controlPanel = new TransparentPanel();
//		controlPanel.setBackground(PamColors.getInstance().getColor(PamColor.BACKGROUND_ALPHA));
		controlPanel.setBackground(new Color(0,0,0,0));
		controlPanel.setOpaque(false);
		controlPanel.setLayout(new BorderLayout());
		controlPanel.add(BorderLayout.CENTER, timeSlider = new PamLogSlider(JSlider.VERTICAL));
		timeSlider.setPamSliderScale(new PamSliderLogScale());
		timeSlider.setMinimum(0);
		timeSlider.setMaximum(900);
		timeSlider.setOpaque(false);
		timeLabel = new PamTextDisplay(1);
//		controlPanel.add(BorderLayout.SOUTH, timeLabel);
		JPanel labPanel = new JPanel(new BorderLayout());
		labPanel.setOpaque(false);
		labPanel.add(BorderLayout.NORTH, maxTimeLabel = new PamTextDisplay());
		labPanel.add(BorderLayout.SOUTH, minTimeLabel = new PamTextDisplay());
		maxTimeLabel.setText(" 900s");
		minTimeLabel.setText(" 0s");
		maxTimeLabel.setOpaque(false);
		minTimeLabel.setOpaque(false);
		maxTimeLabel.setHorizontalAlignment(JTextField.RIGHT);
		minTimeLabel.setHorizontalAlignment(JTextField.RIGHT);
		timeLabel.setHorizontalAlignment(JTextField.CENTER);
		timeLabel.setOpaque(false);
		timeLabel.setFixedTextColour(Color.WHITE);
		maxTimeLabel.setFixedTextColour(Color.WHITE);
		minTimeLabel.setFixedTextColour(Color.WHITE);
//		minTimeLabel.setBorder(null); removes tiny white border from around the number displayed. 
		controlPanel.add(BorderLayout.WEST, labPanel);


		timeLabel.setBorder(null);
		minTimeLabel.setBorder(null);
		maxTimeLabel.setBorder(null);
		
		timeLabel.setToolTipText("Current data display time");
		maxTimeLabel.setToolTipText("Maximum data display time");
		minTimeLabel.setToolTipText("Minimum data display time");

		JPanel scalePanel = new JPanel();
		scalePanel.setOpaque(false); 
		JPanel scaleNorthBox =new JPanel(new BorderLayout());
		scaleNorthBox.setOpaque(false);
		scaleNorthBox.add(BorderLayout.NORTH, scalePanel);
		labPanel.add(BorderLayout.CENTER, scaleNorthBox);
		
		scalePanel.setLayout(new BoxLayout(scalePanel, BoxLayout.Y_AXIS));
		scalePanel.add(logBox = new JCheckBox("log"));
		logBox.setOpaque(false);
		logBox.setForeground(Color.WHITE);
		logBox.setSelected(true);
		logBox.setToolTipText("Use Log time scale slider");
		logBox.addActionListener(new LogListener());
		scalePanel.add(timeLabel);
//		scalePanel.add(new PamLabel("scale"));
		
		
		timeSlider.addChangeListener(new SliderListener());
		timeSlider.setValue(900);
		controlPanel.setToolTipText("Set maximum display time for all detection data on map (overrides other settings)");
	}
	

	class SliderListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent arg0) {	
			sliderChanged();
		}
		
	}
	
	private void sliderChanged() {

		int val = (int) Math.round(timeSlider.getScaledValue());
		if (val == timeSlider.getMaximum()) {
			simpleMap.setHiddenSliderTime(null);
			timeLabel.setText("Show All");
		}
		else if (val == 0) {
			timeLabel.setText("None");
			simpleMap.setHiddenSliderTime(0);
		}
		else {
			simpleMap.setHiddenSliderTime(val);
			timeLabel.setText(String.format(" %ds",val));
		}
	
	}
	
	class LogListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			setLogScale(logBox.isSelected());
		}
	}
	
	public void setLogScale(boolean logScale) {
		if (logScale) {
			timeSlider.setPamSliderScale(new PamSliderLogScale());
		}
		else {
			timeSlider.setPamSliderScale(null);
		}
		sliderChanged();
	}
		

	/**
	 * Set the maximum time for the slider. 
	 * @param timeSeconds maximum time in seconds. 
	 */
	public void setMaxTimeSeconds(int timeSeconds) {
		maxTimeLabel.setText(String.format(" %ds", timeSeconds));
		timeSlider.setMaximum(timeSeconds);
	}

	@Override
	public JComponent getComponent() {
		return controlPanel;
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
		return "Map control";
	}

	/* (non-Javadoc)
	 * @see PamView.hidingpanel.HidingDialogComponent#hasMore()
	 */
	@Override
	public boolean hasMore() {
		return true;
	}

	/* (non-Javadoc)
	 * @see PamView.hidingpanel.HidingDialogComponent#showMore(PamView.hidingpanel.HidingDialog)
	 */
	@Override
	public boolean showMore(HidingDialog hidingDialog) {
		boolean ok = simpleMap.showParametersDialog(SwingUtilities.windowForComponent(getComponent()));
		return ok;
	}
}
