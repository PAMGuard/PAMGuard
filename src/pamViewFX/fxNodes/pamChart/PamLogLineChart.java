package pamViewFX.fxNodes.pamChart;

import javafx.scene.chart.NumberAxis;

/**
 * 
 * Convenience class with a log scale on the y axis.  
 * @author Jamie Macaulay
 *
 */
public class PamLogLineChart extends PamLineChart<Number, Number> {
	
	//private LogarithmicAxis logAxis=new LogarithmicAxis();  

	public PamLogLineChart() {
		super(new LogarithmicAxis(), new NumberAxis());
	}

}
