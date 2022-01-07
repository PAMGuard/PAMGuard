package bearinglocaliser.beamformer;

import java.awt.Window;

import PamController.SettingsPane;
import beamformer.BeamAlgorithmParams;
import beamformer.BeamFormerParams;
import beamformer.BeamGroupProcess;
import beamformer.algorithms.BeamAlgorithmProvider;
import beamformer.algorithms.BeamFormerAlgorithm;
import bearinglocaliser.BearingLocaliserControl;
import bearinglocaliser.BearingLocaliserParams;
import bearinglocaliser.BearingProcess;
import bearinglocaliser.algorithms.BearingAlgorithm;
import bearinglocaliser.algorithms.BearingAlgorithmParams;
import bearinglocaliser.algorithms.BearingAlgorithmProvider;
import bearinglocaliser.algorithms.StaticAlgorithmProperties;
import bearinglocaliser.display.Bearing2DPlot;
import bearinglocaliser.display.BearingDataDisplay;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX2AWT;

public class WrappedBeamFormAlgorithmProvider extends BearingAlgorithmProvider {

	private BeamFormBearingWrapper beamFormBearingWrapper;
	private BeamAlgorithmProvider beamAlgorithmProvider;
	private WrappedStaticProperties wrappedStaticProperties;
	private Bearing2DPlot bearing2DPlot;
	
	public WrappedBeamFormAlgorithmProvider(BeamFormBearingWrapper beamFormBearingWrapper, BeamAlgorithmProvider beamAlgorithmProvider) {
		this.beamFormBearingWrapper = beamFormBearingWrapper;
		this.beamAlgorithmProvider = beamAlgorithmProvider;
		wrappedStaticProperties = new WrappedStaticProperties(beamAlgorithmProvider);
	}

	
	@Override
	public StaticAlgorithmProperties getStaticProperties() {
		return wrappedStaticProperties;
	}

	@Override
	public SettingsPane<?> getSettingsPane(Window awtWindow, BearingLocaliserParams blParams, BearingAlgorithmParams algoParams) {
//		BeamAlgorithmParams beamAlgoParams = null;
//		if (algoParams instanceof WrappedBeamFormParams) {
//			beamAlgoParams = ((WrappedBeamFormParams) algoParams).getBeamAlgorithmParams();
//		}
//		if (beamAlgoParams != null) {
//			beamAlgoParams.setCanBeam(false);
//			beamAlgoParams.setCanBeamogram(true);
//		}
//		BeamFormerParams bfParams = beamFormBearingWrapper.getBeamFormerParams();
//		bfParams.setGroupedSourceParameters(blParams.getRawOrFFTSourceParameters());
//		return beamAlgorithmProvider.getParamsDialog(bfParams, beamAlgoParams);
		return null;
	}
	
	public BeamAlgorithmParams getBeamAlgoParams(BearingAlgorithmParams bearingAlgoParams) {
		if (bearingAlgoParams instanceof WrappedBeamFormParams == false) {
			return null;
		}
		return ((WrappedBeamFormParams) bearingAlgoParams).getBeamAlgorithmParams();
	}

	@Override
	public BearingAlgorithm createAlgorithm(BearingProcess bearingProcess, BearingAlgorithmParams algorithmParams,
			int groupIndex) {
		beamFormBearingWrapper.sortBeamFormers(algorithmParams, groupIndex);

		BeamAlgorithmParams beamParams = getBeamAlgoParams(algorithmParams);
//		BeamFormerAlgorithm beamAlgorithm = beamAlgorithmProvider.makeAlgorithm(beamFormBearingWrapper.getBeamFormerProcess(), beamParams, 0, groupIndex);
		
		BeamGroupProcess beamGroupProcess = new BeamGroupProcess(beamFormBearingWrapper.getBeamFormerProcess(), beamAlgorithmProvider, 
				beamParams, beamParams.getChannelMap(), 0, groupIndex);
		WrappedBeamFormAlgorithm wbfa = new WrappedBeamFormAlgorithm(this, beamFormBearingWrapper.getWrappedBeamFormerProcess(), 
				beamGroupProcess, bearingProcess, algorithmParams, groupIndex);
		return wbfa;
	}

	@Override
	public BearingAlgorithmParams createNewParams(int groupNumber, int groupChanMap) {
		return new WrappedBeamFormParams(beamAlgorithmProvider.createNewParams(wrappedStaticProperties.getName(), groupNumber, groupChanMap));
	}

	public BearingAlgorithmParams showConfigDialog(Window awtWindow, BearingLocaliserParams params,
			BearingAlgorithmParams groupParams) {
		BeamAlgorithmParams beamParams = getBeamAlgoParams(groupParams);
		beamParams.setCanBeam(false);
		beamParams.setCanBeamogram(true);
		BeamFormerParams bfParams = beamFormBearingWrapper.getBeamFormerParams();
		bfParams.setGroupedSourceParameters(params.getRawOrFFTSourceParameters());
		SettingsPane<BeamAlgorithmParams> theSettingsPane = 
				(SettingsPane<BeamAlgorithmParams>) beamAlgorithmProvider.getParamsDialog(bfParams, beamParams);
		if (theSettingsPane == null) {
			System.out.println("No available settings pane for bearing algorithm " + getStaticProperties().getName());
			return null;
		}

		PamDialogFX2AWT<BeamAlgorithmParams> algDialog = new PamDialogFX2AWT<BeamAlgorithmParams>(awtWindow, theSettingsPane, false);
		BeamAlgorithmParams newParams = algDialog.showDialog(beamParams);
		if (newParams == null ) {
			return null;
		}
		return new WrappedBeamFormParams(newParams);
	}


}
