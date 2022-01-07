package rawDeepLearningClassifier.dlClassification.genericModel;

import java.io.File;
import java.util.ArrayList;

import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.transforms.DLTransfromParams;

import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;

/** 
 * 
 * Import and export settings files for the generic classifier
 * 
 * @author Jamie Macaulay
 *
 */
public class GenericImportExportPane extends ImportExportPane {


	/**
	 * the generic advanced pane. 
	 */
	private GenericAdvPane genericAdvPane;

	/**
	 * The generic model parser. 
	 */
	private GenericModelParser genericParser; 


	public GenericImportExportPane(GenericAdvPane genericAdvPane) {
		this.genericAdvPane=genericAdvPane;

	}


	@Override
	public void exportSettings(File file) {

		//OK so this is a little complicated. First get parameters with all relevant settings form the pane. 
		//Then modify those parameters with the current 
		try {

			//set the default segment length so it saves prope;rly.
			double sR = genericAdvPane.getDLControl().getSettingsPane().getSelectedParentDataBlock().getSampleRate(); 

			int defaultsamples = genericAdvPane.getDLControl().getSettingsPane().getSegmentLenSpinner().getValue(); 

			genericAdvPane.getCurrentParams().defaultSegmentLen = 1000.*defaultsamples/sR; 

			boolean writeOK = GenericModelParser.writeGenericModelParams(file, genericAdvPane.getParams(genericAdvPane.getCurrentParams()));
			if (!writeOK) {
				PamDialogFX.showError("Error: Could not export the settings file");
				return;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			PamDialogFX.showError("Error: Could not export the settings file");
			return; 
		}
	}


	@Override
	public void importSettingFile(File file) {

		//OK so this is a little complicated. First get parameters with all relevant settings form the pane. 
		//Then modify those parameters with 
		GenericModelParams genericModelParams = null;

		try {
			genericModelParams = GenericModelParser.readGenericModelParams(file, 
					genericAdvPane.getParams(genericAdvPane.getCurrentParams()), genericAdvPane.getDLControl().getClassNameManager());

			//set the default segment length if available.
			if (genericModelParams.defaultSegmentLen!=null) {
				double sR = genericAdvPane.getDLControl().getSettingsPane().getSelectedParentDataBlock().getSampleRate(); 
				//automatically set the default segment length. 
				genericAdvPane.getDLControl().getSettingsPane().getSegmentLenSpinner().getValueFactory().setValue((int) (sR*genericModelParams.defaultSegmentLen/1000.));
			}

			if (genericModelParams==null) {
				PamDialogFX.showError("Error: Could not import the settings file");
				return;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			PamDialogFX.showError("Error: Could not import the settings file");
			return; 
		}

		genericModelParams.dlTransfroms = DLTransformsFactory.makeDLTransforms((ArrayList<DLTransfromParams>) genericModelParams.dlTransfromParams); 
		//System.out.println("genericAdvPane: setParams()");
		//System.out.println(genericModelParams.toString());

		this.genericAdvPane.setParams(genericModelParams);
	}


}
