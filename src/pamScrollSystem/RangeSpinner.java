package pamScrollSystem;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractSpinnerModel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import PamUtils.TimeRangeFormatter;

/**
 * Spinner control used to set display ranges which can 
 * be added to a PamScroller. 
 * @author Doug
 *
 */
public class RangeSpinner  implements PamScrollObserver {

	private JPanel mainPanel;
	
	private RangeSpinnerModel spinnerModel;
	
	private JSpinner rangeSpinner;
	
	private JTextField rangeText;
	
	private double spinnerValue;
	
	private double defaultValue = 10;
	
	private double maxValue = Double.MAX_VALUE;
	
	private ArrayList<RangeSpinnerListener> rangeSpinnerListeners = new ArrayList<RangeSpinnerListener>();
	
	private double defaultTimeRanges[] = {1, 2, 5, 10, 20, 30, 60, 120,
			300, 600, 900, 1200, 1800, 2700, 3600, 2*3600, 6*3600, 12*3600, 24*3600,
			36*3600, 48*3600, 72*3600, 96*3600, 120*3600, 144*3600, 168*3600 };
	
	private double[] timeRanges;
	
	private boolean formatTimes = true;
	
	public RangeSpinner() {
		super();
		rangeText = new JTextField(5);
		rangeText.setHorizontalAlignment(SwingConstants.CENTER);
		rangeText.setToolTipText("Set the time range for the display in seconds");
		rangeText.addActionListener(new HitEnterListener());
		setTimeRanges(defaultTimeRanges);
		spinnerModel = new RangeSpinnerModel();
		rangeSpinner = new JSpinner(spinnerModel);
		rangeSpinner.setEditor(rangeText);
		rangeSpinner.addChangeListener(new SpinnerListener());
		mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(BorderLayout.CENTER, rangeSpinner);
		spinnerModel.setValue(new Double(defaultValue));
	}
	
	class HitEnterListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			double val = getTextValue();
			spinnerModel.setValue(val);
		}
		
	}
	
	class SpinnerListener implements ChangeListener {
		
		private double lastValue = 0;

		@Override
		public void stateChanged(ChangeEvent arg0) {
			for (int i = 0; i < rangeSpinnerListeners.size(); i++) {
				rangeSpinnerListeners.get(i).valueChanged(lastValue, spinnerValue);
			}
			lastValue = spinnerValue;
		}
		
	}
	
	private double getTextValue() {
		if (formatTimes) {
			long millis = TimeRangeFormatter.readTime(rangeText.getText());
			return millis / 1000.;
		}
		else {
			try {
				return Double.valueOf(rangeText.getText());
			}
			catch (NumberFormatException e) {
				return spinnerValue;
			}
		}
	}

	public void setTimeRanges(double[] timeRanges) {
		this.timeRanges = timeRanges;
		Arrays.sort(this.timeRanges);
	}
	
	/**
	 * Add a single special time range, e.g. the length
	 * of a spectrogram display based on pixels
	 * @param specialRange special range.
	 */
	public void addSpecialTimeRange(double specialRange) {
		timeRanges = Arrays.copyOf(defaultTimeRanges, defaultTimeRanges.length+1);
		timeRanges[defaultTimeRanges.length] = specialRange;
		setTimeRanges(timeRanges);
	}
	
	/**
	 * Add some special time ranges, e.g. the length
	 * of a spectrogram display based on pixels
	 * @param specialRanges list of special ranges.
	 */
	public void addSpecialTimeRange(double[] specialRanges) {
		if (specialRanges == null) {
			setTimeRanges(defaultTimeRanges);
			return;
		}
		timeRanges = Arrays.copyOf(defaultTimeRanges, defaultTimeRanges.length+specialRanges.length);
		for (int i = 0; i < specialRanges.length; i++) {
			timeRanges[defaultTimeRanges.length+i] = specialRanges[i];
		}
		setTimeRanges(timeRanges);
	}
	
	/**
	 * Enable the controls
	 * @param e enable
	 */
	public void setEnabled(boolean e) {
		rangeText.setEnabled(e);
		rangeSpinner.setEnabled(e);
	}
	
	/**
	 * Determines whether or not the spinner is enabled
	 * @return true if enabled
	 */
	public boolean getEnabled() {
		return rangeSpinner.isEnabled();
	}
	
	public JPanel getComponent() {
		return mainPanel;
	}
	
	private class RangeSpinnerModel extends AbstractSpinnerModel {

		@Override
		public Object getNextValue() {
			Double currentValue = (Double) getValue();
			if (currentValue > maxValue) {
				return null;
			}
			for (int i = 0; i < timeRanges.length; i++) {
				if (timeRanges[i] > currentValue + 0.01) {
					return new Double(Math.min(timeRanges[i], maxValue));
				}
			}
			return null;
		}

		@Override
		public Object getPreviousValue() {
			Double currentValue = (Double) getValue();
			for (int i = timeRanges.length-1; i >= 0; i--) {
				if (timeRanges[i] < currentValue-0.01) {
					return new Double(timeRanges[i]);
				}
			}
			return null;
		}

		@Override
		public Object getValue() {
//			try {
//				return Double.valueOf(rangeText.getText());
//			}
//			catch (NumberFormatException e) {
//				return new Double(defaultValue);
//			}
			if (formatTimes) {
				long millis = TimeRangeFormatter.readTime(rangeText.getText());
				return new Double(millis / 1000.);
			}
			else {
				try {
					return Double.valueOf(rangeText.getText());
				}
				catch (NumberFormatException e) {
					return new Double(defaultValue);
				}
			}
		}

		/**
		 * Convenience function that calls 
		 * setValue(Object o) with the same 
		 * value wrapped as a DOuble
		 * @param val double value to set on spinner. 
		 */
		public void setValue(double val) {
			setValue(new Double(val));
		}
		
		@Override
		public void setValue(Object arg0) {
			spinnerValue = (Double) arg0;
			setValueText(spinnerValue);
//			rangeText.setText(arg0.toString());
			fireStateChanged();
		}
		
	}
	
	private void setValueText(double val) {
		if (formatTimes) {
			rangeText.setText(TimeRangeFormatter.formatTime((long) (val*1000.)));
		}
		else {
			if (val-Math.floor(val) < 1e-5) {
				rangeText.setText(String.format("%d", (int) val));
			}
			else {
				//			rangeText.setText((new Double(val)).toString());
				rangeText.setText(String.format("%3.2f", val));
			}
		}
//		String textStr = TimeRangeFormatter.formatTime((int) (val*1000));
//		System.out.println("Spinner time = " + textStr);
//		long newMillis = TimeRangeFormatter.readTime(textStr);
//		textStr = TimeRangeFormatter.formatTime(newMillis);
//		System.out.println("Regenerated time = " + textStr);
		
//
//		Duration duration = new DatatypeFactoryImpl().newDuration((long) (val*1000.));
//		System.out.println(duration.toString());
	}

	/**
	 * @return the defaultValue
	 */
	public double getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(double defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	/**
	 * @return the spinnerValue in seconds
	 */
	public double getSpinnerValue() {
		return spinnerValue;
	}

	/**
	 * @param spinnerValue the spinnerValue to set in seconds
	 */
	public void setSpinnerValue(double spinnerValue) {
		spinnerModel.setValue(spinnerValue);
	}

	/**
	 * Add a range spinner listener. 
	 * @param rangeSpinnerListener
	 */
	public void addRangeSpinnerListener(RangeSpinnerListener rangeSpinnerListener) {
		rangeSpinnerListeners.add(rangeSpinnerListener);
	}
	/**
	 * Remove a range spinner listener. 
	 * @param rangeSpinnerListener
	 */
	public void removeRangeSpinnerListener(RangeSpinnerListener rangeSpinnerListener) {
		rangeSpinnerListeners.remove(rangeSpinnerListener);
	}

	/**
	 * @return the maxValue
	 */
	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * @param maxValue the maxValue to set
	 */
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
		if (spinnerValue > maxValue && maxValue > 0) {
			setSpinnerValue(maxValue);
		}
	}

	@Override
	public void scrollRangeChanged(AbstractPamScroller pamScroller) {
		setMaxValue(pamScroller.getRangeMillis() / 1000.);
	}

	@Override
	public void scrollValueChanged(AbstractPamScroller pamScroller) {
		// TODO Auto-generated method stub
		
	}
	
}
