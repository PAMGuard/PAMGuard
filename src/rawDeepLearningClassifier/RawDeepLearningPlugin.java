package rawDeepLearningClassifier;

import PamDetection.RawDataUnit;
import PamModel.PamDependency;
import PamModel.PamPluginInterface;

/**
 * Making this a plugin. 
 * @author Jamie Macaulay
 *
 */
public class RawDeepLearningPlugin implements PamPluginInterface {

	private String jarFile;

	@Override
	public String getDefaultName() {
		return "Raw Deep Learning Classifier";
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
		return jarFile;
	}

	@Override
	public String getDeveloperName() {
		return "Jamie Macaulay";
	}

	@Override
	public String getContactEmail() {
		return "jamiemac@protonmail.com";
	}

	@Override
	public String getVersion() {
		return "0.0.90";
	}

	@Override
	public String getPamVerDevelopedOn() {
		return "2.01.05";
	}

	@Override
	public String getPamVerTestedOn() {
		return "2.01.05";
	}

	@Override
	public String getAboutText() {
		// TODO Auto-generated method stub
		return "Runs deep learning algorithms on raw acoustic data. \n"
				+ "https://github.com/macster110/PAMGuard_DeepLearningSegmenter";
	}

	@Override
	public String getClassName() {
		return "rawDeepLearningClassifier.DLControl";
	}

	@Override
	public String getDescription() {
		return "Raw Deep Learning Classifier";
	}

	@Override
	public String getMenuGroup() {
		return "Classifiers";
	}

	@Override
	public String getToolTip() {
		return "Runs deep learning models on raw acosutic data e.g. sound data, clicks, clips etc.";
	}

	@Override
	public PamDependency getDependency() {
		return new PamDependency(RawDataUnit.class, "Acquisition.AcquisitionControl");
	}

	@Override
	public int getMinNumber() {
		return 0;
	}

	@Override
	public int getMaxNumber() {
		return 0;
	}

	@Override
	public int getNInstances() {
		return 0;
	}

	@Override
	public boolean isItHidden() {
		return false;
	}

	@Override
	public int allowedModes() {
		return PamPluginInterface.ALLMODES;
	}

}
