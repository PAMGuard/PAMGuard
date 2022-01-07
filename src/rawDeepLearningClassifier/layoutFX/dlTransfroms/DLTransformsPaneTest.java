package rawDeepLearningClassifier.layoutFX.dlTransfroms;

import java.util.ArrayList;

import org.jamdev.jdl4pam.animalSpot.AnimalSpotParams;
import org.jamdev.jdl4pam.transforms.DLTransform;
import org.jamdev.jdl4pam.transforms.DLTransformsFactory;
import org.jamdev.jdl4pam.utils.DLUtils;
import org.jamdev.jpamutils.wavFiles.AudioData;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import pamViewFX.fxNodes.PamBorderPane;

/**
 * Test the dl transfroms pane. 
 * 
 * @author Jamie Macaulay
 *
 */
public class DLTransformsPaneTest extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		//test the DL transfroms pane. 


		String wavFilePath = "/Users/au671271/Google Drive/Aarhus_research/PAMGuard_bats_2020/deep_learning/BAT/example_wav/call_393_2019_S4U05619MOL2-20180917-051012_2525_2534.wav";

		//create the DL params. 
		AnimalSpotParams dlParams = new AnimalSpotParams();

		AudioData soundData = null; 
		//Open wav files. 
		try {
			soundData = DLUtils.loadWavFile(wavFilePath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ArrayList<DLTransform> transforms  = DLTransformsFactory.makeDLTransforms(dlParams.dlTransforms);

		//create the 
		DLImageTransformPane dlTarnsformsPane = new DLImageTransformPane();
		//dlTarnsformsPane.getStylesheets().add(PamStylesManagerFX.getPamStylesManagerFX().getCurStyle().getGUICSS());

		dlTarnsformsPane.setTransforms(transforms);
		dlTarnsformsPane.setPadding(new Insets(5,5,5,5));

		
		//hold everything together. 
		PamBorderPane borderPane = new PamBorderPane(); 
		borderPane.setCenter(dlTarnsformsPane);
		BorderPane.setMargin(dlTarnsformsPane, new Insets(10, 0,0,0));


		primaryStage.setScene(new Scene(borderPane, 890, 570));
		primaryStage.show();
	}


	public static void main(String args[]){           
		launch(args);      
	} 

	public class DLTransfromImagePane extends DLTransformImage {

		private DLTransformsPane dLTransformsPane;

		public DLTransfromImagePane(DLTransformsPane dLTransformsPane) {
			this.dLTransformsPane=dLTransformsPane; 
			this.newSettings();
		}
		
		@Override
		public ArrayList<DLTransform> getDLTransforms() {
			return dLTransformsPane.getDLTransforms();
		}

		
	}

}
