package Spectrogram;

import java.util.ArrayList;

import PamView.paneloverlay.overlaymark.OverlayMarkObserver;
import PamView.paneloverlay.overlaymark.OverlayMarkObservers;

/**
 * Class for registration of classes that will observer
 * spectrgram displays. Only has static members.
 * @author Doug Gillespie
 *
 */
public class SpectrogramMarkObservers {
	
//	private static ArrayList<SpectrogramMarkObserver> observers =
//		new ArrayList<SpectrogramMarkObserver>();
	
	@Deprecated 
	public static void addSpectrogramMarkObserver(SpectrogramMarkObserver spectrogramMarkObserver) {
//		if (observers.contains(spectrogramMarkObserver) == false) {
//			observers.add(spectrogramMarkObserver);
//		}
		OverlayMarkObservers.singleInstance().addObserver(new SpectrogramMarkConverter(spectrogramMarkObserver));
	}
	
	@Deprecated
	public static void removeSpectrogramMarkObserver(SpectrogramMarkObserver spectrogramMarkObserver) {
//		observers.remove(spectrogramMarkObserver);
		// go through and try to find a marker which contains this observer.
		SpectrogramMarkConverter smc = findConverter(spectrogramMarkObserver);
		if (smc != null) {
			OverlayMarkObservers.singleInstance().removeObserver(smc);
		}
	}
	
	/**
	 * Find a converter which wraps a particulas old stype observer. 
	 * @param spectrogramMarkObserver
	 * @return
	 */
	public static SpectrogramMarkConverter findConverter(SpectrogramMarkObserver spectrogramMarkObserver) {
		ArrayList<OverlayMarkObserver> list = OverlayMarkObservers.singleInstance().getMarkObservers();
		for (int i = 0; i < list.size(); i++) {
			OverlayMarkObserver obs = list.get(i);
			if (!SpectrogramMarkConverter.class.isAssignableFrom(obs.getClass())) {
				continue;
			}
			SpectrogramMarkConverter smc = (SpectrogramMarkConverter) obs;
			if (smc.getSpectrogramMarkObserver() == spectrogramMarkObserver) {
				return smc;
			}
		}
		return null;
	}
	
//	public static ArrayList<SpectrogramMarkObserver> getSpectrogramMarkObservers() {
//		return observers;
//	}
	
	
}
