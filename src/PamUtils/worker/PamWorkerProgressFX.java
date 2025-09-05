package PamUtils.worker;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 
 * Worker update message with property to allow FX bindings (e.g. binding to a progress bar)
 * 
 * @author Jamie Macaulay
 *
 */
public class PamWorkerProgressFX  {

	/**
	 * Double property 
	 */
	DoubleProperty progressProperty = new SimpleDoubleProperty(); 


	/**
	 * Message property
	 */
	StringProperty messageProperty  = new SimpleStringProperty();


	/**
	 * Get the progress property. 
	 * @return the progress property. 
	 */
	public DoubleProperty getProgressProperty() {
		return progressProperty;
	}


	/**
	 * Get the message property. for update messages. 
	 * @return the message property. 
	 */
	public StringProperty getMessageProperty() {
		return messageProperty;
	}


	/**
	 * Update with a swing worker message. 
	 * @param msg - the message. 
	 */
	public void update(PamWorkProgressMessage msg) {
		Platform.runLater(()->{
			if (msg==null )return; 
			if (msg.progress!=null) {
				//System.out.println("PamWorkerProgressFX: Progress: " + (msg.progress/100.0)); 
				progressProperty.setValue(msg.progress/100.0); 
			}
			if (msg.textLines!=null) {
				messageProperty.set(msg.textLines[0]);
			}
		});
	}

	/**
	 * Update progress between 0 and 1/ 
	 * @param d
	 */
	public void updateProgress(double d) {
		Platform.runLater(()->{
			progressProperty.setValue(d);
		}); 
	} 




}
