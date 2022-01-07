package PamView.paneloverlay.overlaymark;

import java.util.ArrayList;

import PamView.GeneralProjector;
import PamView.GeneralProjector.ParameterType;

/**
 * List of all available overlay mark providers. 
 * Contains a load of static list functions. 
 * @author Doug Gillespie
 *
 */
public class OverlayMarkProviders {

	private  ArrayList<OverlayMarker> markProviders;

	private static OverlayMarkProviders singleInstance;

	private OverlayMarkProviders() {
		markProviders = new ArrayList<>();
	}

	/**
	 * 
	 * @return single manager of overlay marks. 
	 */
	public static OverlayMarkProviders singleInstance() {
		if (singleInstance == null) {
			singleInstance = new OverlayMarkProviders();
		}
		return singleInstance;
	}

	/**
	 * Remove a provider of spectrogram marks. 
	 * @param overlayMarkProvider provider to remove
	 */
	public void addProvider(OverlayMarker overlayMarkProvider) {
		if (overlayMarkProvider == null) {
			return;
		}		
		// need to do a quick check - if a marker with this name already exists in the list, remove it first before
		// adding the new one
		String nameToAdd = overlayMarkProvider.getMarkerName();
		for (int i=0; i<markProviders.size(); i++) {
			OverlayMarker marker = markProviders.get(i);
			if (marker == null) {
				continue;
			}
			String existingName = marker.getMarkerName();
			if (existingName.equals(nameToAdd)) {
				markProviders.remove(i);
				break;
			}
		}
		markProviders.add(overlayMarkProvider);
	}

	/**
	 * Add a provider of spectrogram marks. 
	 * @param overlayMarkProvider provider to add
	 */
	public void removeProvider(OverlayMarker overlayMarkProvider) {
		markProviders.remove(overlayMarkProvider);
	}

	/**
	 * @return the full list of markProviders
	 */
	public ArrayList<OverlayMarker> getMarkProviders() {
		return markProviders;
	}

	/**
	 * Need another function to get a sub list of certain types of providers . 
	 */	
	public ArrayList<OverlayMarker> getMarkProviders(ParameterType[] parameterTypes) {
		ArrayList<OverlayMarker> someProviders = new ArrayList<>();
		for (OverlayMarker omp:markProviders) {
			if (canMark(omp.getProjector(), parameterTypes)) {
				someProviders.add(omp);
			}
		}
		return someProviders;
	}

	/**
	 * See if a particular projector can draw on a particular set of marks. 
	 * @param markProjector
	 * @param parameterTypes
	 * @return true or false. 
	 */
	private boolean canMark(GeneralProjector markProjector, ParameterType[] parameterTypes) {
		if (parameterTypes == null) {
			return true;
		}
		for (int i = 0; i < parameterTypes.length; i++) {
			if (parameterTypes[i] != markProjector.getParmeterType(i)) {
				return false;
			}
		}
		return true;
	}
}
