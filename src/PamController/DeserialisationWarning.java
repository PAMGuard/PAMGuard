package PamController;

import java.awt.Window;
import java.util.ArrayList;

/**
 * Class to handle warnings from deserialisations which occurr when classes no longer exist. These warnings
 * are occurring more often because of the plugin system which means users are opening viewer databases and
 * psfx files containing classes that are not available in their configuration. This class will be 
 * creates at the start of reading settings, stack up a list of missing classes and then show a single 
 * warning at the end. 
 * @author dg50
 *
 */
public class DeserialisationWarning {

	private ArrayList<String> missingClasses = new ArrayList<>();
	
	private String configName;
	
	public DeserialisationWarning(String configName) {
		super();
		this.configName = configName;
	}

	public void addMissingClass(String message) {
		if (missingClasses.contains(message)) {
			return;
		}
		missingClasses.add(message);
	}
	
	public void showWarning(Window frame) {
		if (missingClasses.size() == 0) {
			return;
		}
		String str;
		if (missingClasses.size() > 1) {
			str = "<html>The following classes could not be loaded from your configuration in " + configName;
		}
		else {
			str = "<html>The following class could not be loaded from your configuration in " + configName;
		}
		str += "<br>";
		for (String mc : missingClasses) {
			str += "<br> - " + mc;
		}
		str += "<br>This may be because the configuration was using PAMGuard plugins, which have not been installed on your system, " +
		"or that you're using an updated PAMGuard version</html>";
		String title = "Missing Java classes in " + configName;
		System.out.println(str);
//		WarnOnce.showNamedWarning(title, frame, configName, str, WarnOnce.WARNING_MESSAGE);
	}
}
