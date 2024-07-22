package rawDeepLearningClassifier.dataSelector;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;

import PamView.dialog.PamDialogPanel;
import PamView.panel.PamPanel;
import javafx.scene.Node;
import pamViewFX.fxNodes.PamBorderPane;

/**
 * Swing panel for the deep learning data selector. 
 */
public class DLSelectPanel implements PamDialogPanel {

	private PamPanel mainPanel;
	
	private DLDataSelector dlDataSelector;
	
	private int currentIndex = 0; 

	public DLSelectPanel(DLDataSelector dlDataSelector) {
		super();
		this.dlDataSelector=dlDataSelector; 
		
		 mainPanel = new PamPanel(); 
		 mainPanel.setLayout(new BorderLayout());
		
	}

	@Override
	public JComponent getDialogComponent() {
		return mainPanel;
	}
	
	
	private void setDataFilterPane(int index) {
		DLDataFilter dlFilter = dlDataSelector.getDataSelectors().get(index);	
		mainPanel.add(dlFilter.getSettingsPanel().getDialogComponent(), BorderLayout.CENTER);
	}

	@Override
	public void setParams() {
		DLDataSelectorParams currParams = dlDataSelector.getParams();

		this.currentIndex = currParams.dataSelectorIndex; 
		
		//set the stored paramters for the deep learning filter
		dlDataSelector.getDataSelectors().get(currentIndex).setParams(currParams.dataSelectorParams[currentIndex]);
			
		//set the paramters in the dialog - note the dialog will have areference ot the filter and so can access the r
		//set params above. 
		dlDataSelector.getDataSelectors().get(currentIndex).getSettingsPanel().setParams();
		
		//set the 
		setDataFilterPane(currentIndex);
	}

	@Override
	public boolean getParams() {

		DLDataSelectorParams currParams = dlDataSelector.getParams();

		//dialog has a reference to the data filter and will change params. 
		boolean dataFilterOK = dlDataSelector.getDataSelectors().get(currentIndex).getSettingsPanel().getParams();

		if (dataFilterOK) {
			//TODO - maybe should grab settings from all filters or just the selected one?
			currParams.dataSelectorParams[currentIndex]  = dlDataSelector.getDataSelectors().get(currentIndex).getParams();

			dlDataSelector.setParams(currParams);

			return true;
		}
		else {
			return false;
		}
	}


}
