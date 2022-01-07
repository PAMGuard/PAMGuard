package asiojni;

import Acquisition.AcquisitionControl;
import Acquisition.DaqSystem;
import Acquisition.DaqSystemInterface;

public class ASIOInterface implements DaqSystemInterface {

	String jarFile;
	
	
	@Override
	public DaqSystem createDAQControl(AcquisitionControl acquisitionControl) {
		DaqSystem asio = new ASIOSoundSystem(acquisitionControl);
		return asio;
	}

	@Override
	public String getHelpSetName() {
		return "asiojni/ASIOHelp.hs";
	}

	@Override
	public void setJarFile(String jarFile) {
		this.jarFile = jarFile;
	}

	@Override
	public String getJarFile() {
		return jarFile;
	}

	@Override
	public String getDeveloperName() {
		return "Doug Gillespie";
	}

	@Override
	public String getContactEmail() {
		return "support@pamguard.org";
	}

	@Override
	public String getVersion() {
		return "2.0.1";
	}

	@Override
	public String getPamVerDevelopedOn() {
		return "1.??.??";
	}

	@Override
	public String getPamVerTestedOn() {
		return "1.15.05";
	}

	@Override
	public String getAboutText() {
		String desc = "ASIO Sound Card driver";
		return desc;
	}

	@Override
	public String getDefaultName() {
		String desc = "ASIO Sound Card driver";
		return desc;
	}


}
