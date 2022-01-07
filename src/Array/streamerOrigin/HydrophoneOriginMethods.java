package Array.streamerOrigin;

import java.util.ArrayList;

import Array.PamArray;
import Array.Streamer;


public class HydrophoneOriginMethods {

	private static HydrophoneOriginMethods singleInstance;
	
	private ArrayList<HydrophoneOriginSystem> systems = new ArrayList<HydrophoneOriginSystem>();
	
	private HydrophoneOriginMethods() {
		registerMethod(new GPSOriginSystem());
		registerMethod(new StaticOriginSystem());
	}
	
	/**
	 * @return a single instance of the register of hydrophone origin methods. 
	 */
	public static  HydrophoneOriginMethods getInstance() {
		if (singleInstance == null) {
			singleInstance = new HydrophoneOriginMethods();
		}
		return singleInstance;
	}
	
	/**
	 * 
	 * @return the number of registered methods. 
	 */
	public int getCount() {
		return systems.size();
	}
	
	/**
	 * Get a specific method
	 * @param iMethod method index
	 * @return origin method
	 */
	public HydrophoneOriginSystem getMethod(int iMethod) {
		if (iMethod >= systems.size()){
			return null;
		}
		return systems.get(iMethod);
	}
	/**
	 * Get a specific method
	 * @param methodClass method class
	 * @return origin method
	 */
	public HydrophoneOriginMethod getMethod(Class methodClass, PamArray pamArray, Streamer streamer ) {
		for (HydrophoneOriginSystem aSystem:systems) {
			if (aSystem.getMethodClass() == methodClass) {
				return aSystem.createMethod(pamArray, streamer);
			}
		}
		return null;
	}
	
	/**
	 * Get a method based on it's name. This is used when reading back from the 
	 * database 
	 * @param methodName name of the method as stored in the database. 
	 * @return method. 
	 */
	public HydrophoneOriginMethod getMethod(String methodName, PamArray pamArray, Streamer streamer) {
		for (HydrophoneOriginSystem aSystem:systems) {
			if (aSystem.getName().equals(methodName)) {
				return aSystem.createMethod(pamArray, streamer);
			}
		}
		return null;
	}
	
	public int indexOfClass(Class methodClass) {
		int i = 0;
		for (HydrophoneOriginSystem aMethod:systems) {
			if (aMethod.getMethodClass() == methodClass) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	/**
	 * Register a new hydrophone origin method
	 * @param method the method
	 */
	public void registerMethod(HydrophoneOriginSystem system) {
		systems.add(system);
	}
	
}
