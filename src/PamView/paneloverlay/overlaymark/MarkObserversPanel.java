package PamView.paneloverlay.overlaymark;

import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import PamView.dialog.PamDialogPanel;

/**
 * Simple panel of checkboxes which can be used by an overlay marker 
 * (i.e. something that makes marks) to create a list of mark observers 
 * which want to subscribe to and use those marks. 
 * @author dg50
 *
 */
public class MarkObserversPanel implements PamDialogPanel {

	private JPanel mainPanel;
	
	private ArrayList<OverlayMarkObserver> markObservers;
	
	private JCheckBox[] checkBoxes;

	private MarkRelationships markRelationships;

	private OverlayMarker[] overlayMarkers;

	/**
	 * Construct a really simple swing panel to display a list of checkboxes for 
	 * all mark observers. 
	 * @param overlayMarker
	 */
	public MarkObserversPanel(OverlayMarker[] overlayMarkers) {
		this.overlayMarkers = overlayMarkers;

		this.markRelationships = MarkRelationships.getInstance();
		
		createSwingPanel();
	}
	
	private void createSwingPanel() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));		
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		mainPanel.removeAll();
		markObservers = OverlayMarkObservers.singleInstance().getMarkObservers();
		checkBoxes = new JCheckBox[markObservers.size()*overlayMarkers.length];
		int boxId = 0;
		for (int i = 0; i < markObservers.size(); i++) {
			OverlayMarkObserver markObserver = markObservers.get(i);
			for (OverlayMarker aMarker : overlayMarkers) {
				checkBoxes[boxId] = new JCheckBox(markObserver.getObserverName() + "_" + aMarker.getMarkerName());
				boolean link = markRelationships.getRelationship(aMarker, markObserver);
				checkBoxes[boxId].setSelected(link);
				mainPanel.add(checkBoxes[boxId]);
				boxId++;
			}
		}
	}

	@Override
	public boolean getParams() {
		if (markObservers == null) {
			return false;
		}
		int nObservers = 0;
		int boxId = 0;
		for (int i = 0; i < markObservers.size(); i++) {
			OverlayMarkObserver markObserver = markObservers.get(i);
			for (OverlayMarker aMarker : overlayMarkers) {
				markRelationships.setRelationship(aMarker, markObserver, checkBoxes[boxId].isSelected());
				if (checkBoxes[boxId].isSelected()) {
					nObservers++;
				}
				boxId++;
			}
		}		
		return (nObservers > 0);
	}

	public void setOverlayMarkers(OverlayMarker[] overlayMarkers) {
		this.overlayMarkers = overlayMarkers;
	}

}
