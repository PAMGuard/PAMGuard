package networkTransfer.send;

import PamModel.PamDependency;
import PamModel.PamPluginInterface;

public class MqttSendPlugin implements PamPluginInterface{
	
	private String jarFile;
	
	@Override
	public String getDefaultName() {
		// TODO Auto-generated method stub
		return "Network Sender";
	}

	@Override
	public String getHelpSetName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setJarFile(String jarFile) {
		this.jarFile = jarFile;
		
	}

	@Override
	public String getJarFile() {
		// TODO Auto-generated method stub
		return jarFile;
	}

	@Override
	public String getDeveloperName() {
		// TODO Auto-generated method stub
		return "Sam";
	}

	@Override
	public String getContactEmail() {
		// TODO Auto-generated method stub
		return "st@smruconsulting.com";
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "V1.0.0";
	}

	@Override
	public String getPamVerDevelopedOn() {
		// TODO Auto-generated method stub
		return "2.02.09c_APS";
	}

	@Override
	public String getPamVerTestedOn() {
		// TODO Auto-generated method stub
		return "2.02.09c_APS";
	}

	@Override
	public String getAboutText() {
		// TODO Auto-generated method stub
		return "Writes specific module new and updated data to a csv";
	}

	@Override
	public String getClassName() {
		// TODO Auto-generated method stub
		return NetworkSender.class.getName();
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMenuGroup() {
		// TODO Auto-generated method stub
		return "Utilities";
	}

	@Override
	public String getToolTip() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PamDependency getDependency() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMinNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNInstances() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isItHidden() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int allowedModes() {
		// TODO Auto-generated method stub
		return ALLMODES;
	}

}