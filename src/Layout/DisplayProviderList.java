package Layout;

import java.util.ArrayList;


/**
 * DisplayProviderList is used to manage a list of possible providers
 * of display panels that may be incorporated into other displays.
 * <p>
 * Each DisplayPanelProvider is registered with DisplayProviderList and that
 * list can be accessed by various displays throughout Pamguard. 
 * An example is provided in SpectrogramParamsDialog.
 * 
 * @author Doug Gillespie
 * @see Spectrogram.SpectrogramParamsDialog
 *
 */
public class DisplayProviderList {

	static ArrayList<DisplayPanelProvider> displayPanelProviders = new ArrayList<DisplayPanelProvider>();
	
	public static void addDisplayPanelProvider(DisplayPanelProvider displayPanelProvider) {
		if (!displayPanelProviders.contains(displayPanelProvider)) {
			displayPanelProviders.add(displayPanelProvider);
		}
	}
	
	public static void removeDisplayPanelProvider(DisplayPanelProvider displayPanelProvider) {
		displayPanelProviders.remove(displayPanelProvider);
	}

	public static ArrayList<DisplayPanelProvider> getDisplayPanelProviders() {
		return displayPanelProviders;
	}
	
}
