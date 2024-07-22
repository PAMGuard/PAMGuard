package rawDeepLearningClassifier.dataSelector;

import javafx.scene.Node;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

/**
 * JavaFX pane for the deep learning data selector. This simply selects the rype
 * of filter to use and sets that as the controls. 
 * <p>
 * Note that at the moment this only implements one type of data filter and so 
 * essentially all controls etc. for changing filters are invisible to the user. 
 * 
 * @author Jamie Macaulay
 */
public class DLSelectPaneFX extends DynamicSettingsPane<Boolean>{
	
	private PamBorderPane mainPane;
	
	/**
	 * Reference to the deep learning data selector. 
	 */
	private DLDataSelector dlDataSelector;
	
	/**
	 * The current index selected by the user - not in the params. 
	 */
	private int currentIndex = 0;

	public DLSelectPaneFX(DLDataSelector dlDataSelector) {
		super(null);
		this.dlDataSelector=dlDataSelector;

		//there is currently one implemented fitler so no
		//need for a comboBox etc. to select. 
		 createPane();
		 
//		 mainPane.getChildren().add(dlDataSelector.getDataSelectors().getSettingsPane().getContentNode());
	
	}
	
	private void createPane() {
		mainPane = new PamBorderPane();
		
		//need to add a settings listener to the filter panes to pass on any notification this settings listener. 
		for (int i=0; i<dlDataSelector.getDataSelectors().size(); i++) {
			dlDataSelector.getDataSelectors().get(currentIndex).getSettingsPane().addSettingsListener(()->{
				//notify any listeners to this pane that a filter pane has changed. 
				notifySettingsListeners();
			});
		}
		
	}

	
	@Override
	public Boolean getParams(Boolean input) {
//		System.out.println("Get params DL data selector!"); 
		DLDataSelectorParams currParams = dlDataSelector.getParams();
		
		//TODO - maybe should grab settings from all filters or just the selected one?
		currParams.dataSelectorParams[currentIndex]  = dlDataSelector.getDataSelectors().get(currentIndex).getSettingsPane().getParams(currParams.dataSelectorParams[currentIndex]);
		
		dlDataSelector.setParams(currParams);

		return true;
	}

	@Override
	public void setParams(Boolean input) {
		DLDataSelectorParams currParams = dlDataSelector.getParams();

		this.currentIndex = currParams.dataSelectorIndex; 
		
		dlDataSelector.getDataSelectors().get(currentIndex).getSettingsPane().setParams(currParams.dataSelectorParams[currentIndex]);
		
		setDataFilterPane(currentIndex);
		
	}
	
	private void setDataFilterPane(int index) {
		DLDataFilter dlFilter = dlDataSelector.getDataSelectors().get(index);
		mainPane.setCenter(dlFilter.getSettingsPane().getContentNode());
	}

	@Override
	public String getName() {
		return "Filter by deep learning result";
	}

	@Override
	public Node getContentNode() {
		return mainPane; 
	}

	@Override
	public void paneInitialized() {
		// TODO Auto-generated method stub
		
	}

}
