package rawDeepLearningClassifier.dlClassification.delphinID;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import PamController.SettingsPane;
import javafx.scene.Node;
import javafx.stage.FileChooser.ExtensionFilter;
import rawDeepLearningClassifier.layoutFX.DLCLassiferModelUI;

/**
 * DelphinID user interface components. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DelphinUI implements DLCLassiferModelUI  {
	
	/**
	 * The icon. 
	 */
	private Node icon;
	
	/**
	 * Reference to the delphinID classifier. 
	 */
	private DelphinIDClassifier delphinUIClassifier;
	
	/**
	 * The delphinID settings pane. 
	 */
	private DelphinIDPane delphinIDSettings;

	/**
	 * List of extension filters for this type of model. 
	 */
	private ArrayList<ExtensionFilter> extensionFilters;

	public DelphinUI(DelphinIDClassifier delphinUIClassifier) {
		this.delphinUIClassifier=delphinUIClassifier;
		extensionFilters = new ArrayList<ExtensionFilter>(); 
		//import the settings holder
		extensionFilters.add(new ExtensionFilter("Zip Model", "*.zip")); 
	}

	@Override
	public SettingsPane<DelphinIDParams> getSettingsPane() {
		if (delphinIDSettings==null) {
			delphinIDSettings = new DelphinIDPane(delphinUIClassifier);
		}
		return delphinIDSettings;
	}

	@Override
	public void getParams() {
		DelphinIDParams params =  getSettingsPane().getParams(delphinUIClassifier.getDLParams()); 
		delphinUIClassifier.setDLParams(params); 
	}


	@Override
	public void setParams() {
		//		System.out.println("Set model params: " + genericModelClassifier.getGenericDLParams().dlTransfromParams.size()); 
		getSettingsPane().setParams(delphinUIClassifier.getDLParams());
	}

	@Override
	public List<ExtensionFilter> getModelFileExtensions() {
		//this is a bit redundant because zip files
		return extensionFilters;
	}

	@Override
	public JPanel getSidePanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getIcon() {
//		if (icon==null) {
//			PamSVGIcon iconMaker= new PamSVGIcon(); 
//			PamSVGIcon svgsprite;
//			try {
//				svgsprite = iconMaker.create(getClass().getResource("/Resources/delphinid_logo01.svg").toURI().toURL());
//				icon = svgsprite.getSpriteNode();
//			} catch (MalformedURLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (URISyntaxException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		return icon;
		return null;
	}
		
}
