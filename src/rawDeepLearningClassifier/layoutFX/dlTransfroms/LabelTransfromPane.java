package rawDeepLearningClassifier.layoutFX.dlTransfroms;

import org.jamdev.jdl4pam.transforms.DLTransform;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

class LabelTransfromPane extends DLTransformPane {

	private DLTransform dlTransfrom;
	

	private String name; 

	public LabelTransfromPane(DLTransform dlTransfrom, String name) {
		super(); 
		this.name = name; 
		this.dlTransfrom = dlTransfrom; 
		Label label = new Label(name); 
		this.setLeft(label); 
		BorderPane.setAlignment(label, Pos.CENTER_LEFT);
		this.setStyle("-fx-background-color: rgba(205, 205, 205, 1);");

	}
	
	@Override
	public DLTransform getDLTransform() {
		return dlTransfrom;
	}

	@Override
	public DLTransform getParams(DLTransform dlTransform) {
		return dlTransform;
	}

	@Override
	public void setParams(DLTransform dlTransform) {
		// TODO Auto-generated method stub
	}

	
	
}