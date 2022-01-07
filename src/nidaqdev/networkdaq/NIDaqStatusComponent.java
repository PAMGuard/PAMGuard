package nidaqdev.networkdaq;

import java.awt.Component;

import PamUtils.LatLong;
import PamView.dialog.PamLabel;

public class NIDaqStatusComponent {

	private NINetworkDaq niNetworkDaq;
	
	private PamLabel tempLabel;

	public NIDaqStatusComponent(NINetworkDaq niNetworkDaq) {
		this.niNetworkDaq = niNetworkDaq;
		tempLabel = new PamLabel("CRio Temperature");
//		setTemperature(25.5);
	}
	
	public Component getComponent() {
		return tempLabel;
	}
	
	public void setTemperature(Double temp) {
		if (temp == null) {
			tempLabel.setText(null);
		}
		else {
			ChassisConfig chassis = niNetworkDaq.getNiNetParams().chassisConfig;
			if (chassis != null) {
				tempLabel.setText(String.format("%s %3.1f%sC", chassis.getDescription(), temp, LatLong.deg));
			}
		}
	}

}
