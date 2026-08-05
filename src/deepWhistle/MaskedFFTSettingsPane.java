package deepWhistle;

import javafx.geometry.Insets;
import javafx.scene.Node;
import pamViewFX.PamGuiManagerFX;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamHBox;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.utilityPanes.FreqResolutionPane;
import pamViewFX.fxNodes.utilityPanes.SourcePaneFX;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

import PamController.PamController;
import PamguardMVC.PamDataBlock;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;

/**
 * Simple JavaFX settings pane for DeepWhistleParameters.
 */
public class MaskedFFTSettingsPane extends DynamicSettingsPane<MaskedFFTParamters> {

    private MaskedFFTParamters params;
    private PamBorderPane main;
	private SourcePaneFX sourcePane;
	private FreqResolutionPane resolutionPane;

    public MaskedFFTSettingsPane(Object owner) {
        super(owner);
        createContent();
    }

    private void createContent() {
    	
    	PamVBox vBox=new PamVBox();
		vBox.setSpacing(5);

		sourcePane = new SourcePaneFX("Raw data source for FFT", FFTDataUnit.class, true, true);
		PamGuiManagerFX.titleFont2style(sourcePane.getTitleLabel());
		
		resolutionPane=new FreqResolutionPane(); 

		sourcePane.addSelectionListener((obsVal, newVal, oldVal) -> {
			//update the frequency resolution pane.
			resolutionPane.setParams((FFTDataBlock) sourcePane.getSource());
		});

		vBox.getChildren().add(sourcePane);
		
		vBox.getChildren().add(resolutionPane);
		
		main = new PamBorderPane();		
		main.setPadding(new Insets(5,5,5,5));
		main.setCenter(vBox);
		

    }

    @Override
    public MaskedFFTParamters getParams(MaskedFFTParamters currParams) {
        if (currParams == null) currParams = new MaskedFFTParamters();
			//			fftParameters.rawDataSource = sourceList.getSelectedItem().toString();
        currParams.dataSourceIndex = sourcePane.getSourceIndex();
        currParams.dataSourceName = sourcePane.getSourceLongName();
        
        currParams.channelMap = sourcePane.getChannelList();
        
        return currParams;
    }

    @Override
    public void setParams(MaskedFFTParamters input) {
        this.params = input;
        if (input == null) input = new MaskedFFTParamters();
        
		// and fill in the data source list (may have changed - or might in later versions)
		PamDataBlock  datablock = PamController.getInstance().getFFTDataBlock(params.dataSourceName);
//		System.out.println("Data block to set for FFT source: "+datablock.getDataName() + " FFT PARAMS: "+fftParameters.dataSourceName);
		//fft settings
		sourcePane.setSource(datablock);
		sourcePane.setChannelList(params.channelMap); //set selected channels
    }

    @Override
    public String getName() {
        return "Deep Whistle Mask Settings";
    }

    @Override
    public Node getContentNode() {
        return main;
    }

    @Override
    public void paneInitialized() {
        // nothing special
    }

}