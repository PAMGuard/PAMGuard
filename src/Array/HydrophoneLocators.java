package Array;

import java.util.ArrayList;

import PamController.PamController;

public class HydrophoneLocators {

	private ArrayList<HydrophoneLocatorSystem> locatorList = new ArrayList<HydrophoneLocatorSystem>();
			
	static HydrophoneLocators singleInstance;
	
//	private ArrayList<Class<HydrophoneLocator>> arrayLocators;
	
	private boolean isNetwork;
	private boolean isViewer; 
	
	private HydrophoneLocators() {
		isNetwork = (PamController.getInstance().getRunMode() == PamController.RUN_NETWORKRECEIVER);
		isViewer= (PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW);
//		RegisterLocator(new StaticHydrophoneLocatorSystem());
		RegisterLocator(new StraightHydrophoneLocatorSystem());
		RegisterLocator(new ThreadingHydrophoneLocatorSystem());
//		if (isNetwork || PamguardVersionInfo.getReleaseType() == PamguardVersionInfo.ReleaseType.SMRU) {
//			RegisterLocator(new NetworkHydrophoneLocatorSystem());
//		}
//		if (isViewer){
//			RegisterLocator(new ImportHydrophoneLocatorSystem());
//		}
		
		//need to specify which hydrophone locators can have changeable array locations; 
		
//		locatorNames.add("Terrella Hydrophone Locator");
	}
	
	
	
	public void RegisterLocator(HydrophoneLocatorSystem locatorSystem) {
		locatorList.add(locatorSystem);
	}
	
	
	
	public static HydrophoneLocators getInstance() {
		if (singleInstance == null) {
			singleInstance = new HydrophoneLocators();
		}
		return singleInstance;
	}
	
	/**
	 * Finds a locator system for a given locator class. 
	 * <p>N.B. The class type is of the HydrophoneLocator, not the HydrophoneLocatorSystem
	 * @param locatorClass hydrophone locator class
	 * @return HydrophoneLocatorSystem
	 */
	public HydrophoneLocatorSystem getLocatorSystem(Class locatorClass) {
		for (HydrophoneLocatorSystem system:locatorList) {
			if (system.getLocatorClass() == locatorClass) {
				return system;
			}
		}
		return locatorList.get(1);
	}

	public HydrophoneLocatorSystem getLocatorSystem(String locatorName) {
		for (HydrophoneLocatorSystem system:locatorList) {
			if (system.getName().equals(locatorName)) {
				return system;
			}
		}
		return locatorList.get(1);
	}
	
	/**
	 * Get teh index of a specified locator class. 
	 * @param locatorClass
	 * @return
	 */
	public int indexOfClass(Class locatorClass) {
		int i = 0;
		for (HydrophoneLocatorSystem system:locatorList) {
			if (system.getLocatorClass() == locatorClass) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
//	public HydrophoneLocator get(int i, PamArray pamArray) {
//		switch (i) {
//		case 0:
//			return new StraightHydrophoneLocator(pamArray);
//		case 1:
//			return new ThreadingHydrophoneLocator(pamArray);
//		case 2: // don't change this - see Array<anager.checkBuoyHydropneStreamer
//			return new NetworkHydrophoneLocator(pamArray);
//	//	case 2:
//	//		return new TerrellaHydrophoneLocator(pamArray);
//		}
//	}
	
	public HydrophoneLocator getLocator(PamArray pamArray, Streamer streamer) {
		HydrophoneLocatorSystem system = getLocatorSystem(streamer.getLocatorClass());
		if (system != null) {
			return system.getLocator(pamArray, streamer);
		}
//		switch (i) {
//		case 0:
//			return new StraightHydrophoneLocator(pamArray);
//		case 1:
//			return new ThreadingHydrophoneLocator(pamArray);
//		case 2:
//			return new NetworkHydrophoneLocator(pamArray);
////		case 2:
////			return new TerrellaHydrophoneLocator(pamArray);
//		}
		return null;
	}

	public int getCount() {
		return locatorList.size();
	}
	
	public HydrophoneLocatorSystem getSystem(int i) {
		return locatorList.get(i);
	}
	
}
