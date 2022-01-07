package PamView.paneloverlay.overlaymark;

import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import PamView.GeneralProjector.ParameterType;
import PamView.dialog.PamDialogPanel;

/**
 * standard panel that can select mark providers 
 * of particular types. Very undecorated to include in
 * other more complex dialogs. This will need to know the 
 * name of the observed thing so that it can get data 
 * from the centralised MarkRelationships.  
 * @author dg50
 *
 */
public class MarkProvidersPanel implements PamDialogPanel {

	private ParameterType[] parameterTypes;
	
	private JPanel mainPanel;

	private ArrayList<OverlayMarker> markerList;
	
	private JCheckBox[] selectBoxes;

	private OverlayMarkObserver markObserver;

	private MarkRelationships markRelationships;

	/**
	 * @param parameterTypes
	 */
	public MarkProvidersPanel(OverlayMarkObserver markObserver, ParameterType[] parameterTypes) {
		super();
		this.markObserver = markObserver;
		this.parameterTypes = parameterTypes;
		
		this.markRelationships = MarkRelationships.getInstance();
		
		createSwingPanel();
	}
	
	private void createSwingPanel() {
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		// panel will be populated when params are set in case the list changes
		
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}

	@Override
	public void setParams() {
		mainPanel.removeAll();
		markerList = OverlayMarkProviders.singleInstance().getMarkProviders(parameterTypes);
		selectBoxes = new JCheckBox[markerList.size()];
		for (int i = 0; i < markerList.size(); i++) {
			selectBoxes[i] = new JCheckBox(markerList.get(i).getMarkerName());
			mainPanel.add(selectBoxes[i]);
//			MarkSelectionData markData = markerSelectionManager.getMarkerSelectionData(markerList.get(i).getMarkerName());
			boolean link = markRelationships.getRelationship(markerList.get(i), markObserver);
			selectBoxes[i].setSelected(link);
		}
	}

	@Override
	public boolean getParams() {
		int nMarks = 0;
		for (int i = 0; i < markerList.size(); i++) {
			markRelationships.setRelationship(markerList.get(i), markObserver, selectBoxes[i].isSelected());
			if (selectBoxes[i].isSelected()) {
				nMarks++;
			}
		}
		return (nMarks > 0);
	}
	
	
	
}
