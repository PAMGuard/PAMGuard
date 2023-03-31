package clickDetector.layoutFX.clickClassifiers;

import clickDetector.ClickClassifiers.ClickTypeCommonParams;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * A property wrapper for a basic click with JavaFX properties. This is used so JavaFX controls 
 * automatically change.
 * 
 * @author Jamie Macaulay
 *
 */
public class ClickTypeProperty {
	
	/**
	 * Params for a basic click type.
	 */
	private ClickTypeCommonParams commonClickParams;


	public ClickTypeProperty(ClickTypeCommonParams clickType){
		this.commonClickParams=clickType;
		name.setValue(clickType.getName());
		discardClassifier.setValue(clickType.getDiscard());
		code.setValue(clickType.getSpeciesCode());

	}

	public StringProperty name= new SimpleStringProperty("blank");
	
	public BooleanProperty enableClassifier= new SimpleBooleanProperty(true);
	
	public BooleanProperty discardClassifier= new SimpleBooleanProperty(true);

	public IntegerProperty code= new SimpleIntegerProperty(0);
	
	
	public ClickTypeCommonParams getClickType() {
		return commonClickParams;
	}

	public void setClickType(ClickTypeCommonParams clickType) {
		this.commonClickParams = clickType;
		//TODO-need to set properties;
		name.setValue(clickType.getName());
		enableClassifier.setValue(clickType.enable);
		discardClassifier.setValue(clickType.getDiscard());
		code.setValue(clickType.getSpeciesCode());
	}


}
