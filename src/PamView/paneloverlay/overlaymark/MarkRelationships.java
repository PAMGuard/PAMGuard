package PamView.paneloverlay.overlaymark;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenuItem;

import PamController.PamControlledUnitSettings;
import PamController.PamSettingManager;
import PamController.PamSettings;

/**
 * Singleton class to handle mark relationships. 
 * @author dg50
 *
 */
public class MarkRelationships implements PamSettings {
	
	private MarkRelationshipsData markRelationshipsData = new MarkRelationshipsData();
	
	private static MarkRelationships singleInstance;

	
	private MarkRelationships() {
		PamSettingManager.getInstance().registerSettings(this);
		subscribeAllMarkers();
	}
	
	/**
	 * Get a single instance of the class that handles ALL relationships 
	 * between the markers and the marked. 
	 * @return singleton mark relationships manager. 
	 */
	public static MarkRelationships getInstance() {
		if (singleInstance == null) {
			singleInstance = new MarkRelationships();
		}
		return singleInstance;
	}

	/**
	 * Set a link between a marker and an observer
	 * @param overlayMarker overlay marker
	 * @param markObserver mark observer
	 * @param linked linked true or false
	 */
	public void setRelationship(OverlayMarker overlayMarker, OverlayMarkObserver markObserver, boolean linked) {
		setRelationship(overlayMarker.getMarkerName(), markObserver.getObserverName(), linked);
	}
	
	/**
	 * Set a link between a marker and an observer. 
	 * @param markerName Marker name
	 * @param observerName Observer name
	 * @param linked linked true of false
	 */
	private void setRelationship(String markerName, String observerName, boolean linked) {
		markRelationshipsData.setRelationship(markerName, observerName, linked);
	}

	/**
	 * Get a link between a marker and an observer. 
	 * @param markerName Overlay Marker 
	 * @param observerName Mark Observer 
	 * @return linked true of false
	 */
	public boolean getRelationship(OverlayMarker overlayMarker, OverlayMarkObserver markObserver) {
		return getRelationship(overlayMarker.getMarkerName(), markObserver.getObserverName());
	}

	/**
	 * Get a link between a marker and an observer. 
	 * @param markerName Marker name
	 * @param observerName Observer name
	 * @return linked true of false
	 */
	private boolean getRelationship(String markerName, String observerName) {
		return markRelationshipsData.getRelationship(markerName, observerName);
	}

	@Override
	public String getUnitName() {
		return "Overlay mark relationships";
	}

	@Override
	public String getUnitType() {
		return "Overlay mark relationships";
	}

	@Override
	public Serializable getSettingsReference() {
		return markRelationshipsData;
	}

	@Override
	public long getSettingsVersion() {
		return MarkRelationshipsData.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		this.markRelationshipsData = (MarkRelationshipsData) pamControlledUnitSettings.getSettings();
		return true;
	}
	
	/**
	 * Subscribe all markers to all observers. 
	 * @return number of subsribed observers
	 */
	public int subscribeAllMarkers() {
		OverlayMarkProviders markProviders = OverlayMarkProviders.singleInstance();
		ArrayList<OverlayMarker> markerList = markProviders.getMarkProviders();
		int totalLinks = 0;
		for (OverlayMarker marker:markerList) {
			totalLinks += subscribeObservers(marker);
		}
		return totalLinks;
	}
	
	/**
	 * Subscribe observers to a single marker. 
	 * @param overlayMarker Overlay marker (source of marks)
	 * @return number of observers.
	 */
	public int subscribeObservers(OverlayMarker overlayMarker) {
		ArrayList<OverlayMarkObserver> observers = OverlayMarkObservers.singleInstance().getMarkObservers();
		int nObs = 0;
		for (OverlayMarkObserver observer:observers) {
			if (getRelationship(overlayMarker, observer)) {
				overlayMarker.addObserver(observer);
				nObs++;
			}
			else {
				overlayMarker.removeObserver(observer);
			}
		}
		return nObs;
	}

	/**
	 * Subscribe an observer to all things which might mark it. 
	 * @param markObserver Mark Observer
	 * @return number of markers marking this observer. 
	 */
	public int subcribeToMarkers(OverlayMarkObserver markObserver) {
		OverlayMarkProviders markProviders = OverlayMarkProviders.singleInstance();
		ArrayList<OverlayMarker> markerList = markProviders.getMarkProviders();
		int totalLinks = 0;
		for (OverlayMarker marker:markerList) {
			if (getRelationship(marker, markObserver)) {
				marker.addObserver(markObserver);
				totalLinks++;
			}
			else {
				marker.removeObserver(markObserver);
			}
		}
		return totalLinks;
	}
	
	/**
	 * @return a Java Swing menu item or setting mark relationship options. 
	 */
	public JMenuItem getSwingMenuItem(JFrame swingFrame) {
		JMenuItem menuItem = new JMenuItem("Display marks and observers ...");
		menuItem.setToolTipText("Control all relationships between displays which can draw mouse strokes and users of marked information");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				swingMenuAction(swingFrame);
			}
		});
		int nMarkers = OverlayMarkProviders.singleInstance().getMarkProviders().size();
		int nObs = OverlayMarkObservers.singleInstance().getMarkObservers().size();
		boolean has = (nMarkers > 0 && nObs > 0);
		if (has) {
			menuItem.setEnabled(has);
		}
		
		return menuItem;
	}
	
	private void swingMenuAction(JFrame swingFrame) {
		if (SwingRelationshipsDialog.showDialog(swingFrame)) {
			subscribeAllMarkers();
		}
	}

	/**
	 * @return the markRelationshipsData
	 */
	public MarkRelationshipsData getMarkRelationshipsData() {
		return markRelationshipsData;
	}
}
