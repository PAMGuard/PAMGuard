package pamViewFX.fxNodes.tooltip;

import java.util.concurrent.CountDownLatch;

import javafx.application.Platform;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Tooltip with some extra functionality, such as setting HTML text
 * @author Jamie Macaulay
 *
 */
public class PamToolTip extends Tooltip {

	private WebEngine webEngine;

	private WebView web;


	public PamToolTip() {
		//eeek...nasty
		final CountDownLatch latch = new CountDownLatch(1);
		Platform.runLater(()->{
			web = new WebView();
			webEngine = web.getEngine();
	        latch.countDown();
		}); 
		try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Set HTML in the tool tip. 
	 * @param htmlText - HTML text to set.
	 */
	public void setHTML(String htmlText) {

		Platform.runLater(()->{
		webEngine.loadContent(htmlText);
		this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		this.setGraphic(web);
		});
	}


}
