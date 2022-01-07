/**
 * 
 */
package analogarraysensor.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.border.TitledBorder;

import Array.sensors.swing.HeadingComponent;
import Array.sensors.swing.PitchRollComponent;
import PamView.PamSidePanel;
import PamView.panel.PamPanel;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import PamguardMVC.PamObserverAdapter;
import analogarraysensor.ArraySensorControl;
import analogarraysensor.AnalogArraySensorDataUnit;
import analoginput.AnalogSensorData;

/**
 * @author dg50
 *
 */
public class PitchRollSidePanel implements PamSidePanel {

	
	private ArraySensorControl arraySensorControl;	
	
	private PamPanel mainPanel;
	
	private PitchRollComponent pitchRollComponent;
	
	private HeadingComponent headingComponent;
	
	public PitchRollSidePanel(analogarraysensor.ArraySensorControl arraySensorControl) {
		super();
		this.arraySensorControl = arraySensorControl;
		mainPanel = new PamPanel(new BorderLayout());
		mainPanel.setBorder(new TitledBorder(arraySensorControl.getUnitName()));
		pitchRollComponent = new PitchRollComponent(arraySensorControl, 1);
		headingComponent = new HeadingComponent(arraySensorControl, 1);
		mainPanel.add(BorderLayout.NORTH, headingComponent);
		mainPanel.add(BorderLayout.CENTER, pitchRollComponent);
		mainPanel.setMinimumSize(new Dimension(100, 100));
		arraySensorControl.getAnalogSensorProcess().getSensorDataBlock().addObserver(new DataObserver());
	}

	@Override
	public JComponent getPanel() {
		return mainPanel;
	}

	@Override
	public void rename(String newName) {
		// TODO Auto-generated method stub

	}
	
	private class DataObserver extends PamObserverAdapter {

		@Override
		public void addData(PamObservable observable, PamDataUnit pamDataUnit) {
			AnalogArraySensorDataUnit asData = (AnalogArraySensorDataUnit) pamDataUnit;
			AnalogSensorData[] sensData = asData.getSensorData();
//			if (sensData[1] != null) {
//				pitchRollComponent.setCurrentPitch(0, sensData[1].getCalibratedValue());
//			}
//			if (sensData[2] != null) {
//				pitchRollComponent.setCurrentRoll(0, sensData[2].getCalibratedValue());
//			}
			pitchRollComponent.setSensorData(pamDataUnit.getChannelBitmap(), asData);
			headingComponent.setSensorData(pamDataUnit.getChannelBitmap(), asData);
		}

		@Override
		public String getObserverName() {
			return null;
		}

		@Override
		public PamObserver getObserverObject() {
			return null;
		}

	}

}
