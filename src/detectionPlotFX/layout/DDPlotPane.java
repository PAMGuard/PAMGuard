package detectionPlotFX.layout;

import pamViewFX.fxPlotPanes.PlotPane;

/**
 * Extends the standard plot pane;
 * @author Jamie Macaulay
 *
 */
public class DDPlotPane extends PlotPane {
	
	public DDPlotPane(){
		super(); 
//		//neeed to redraw dataunit if the plot is resized. 
//		this.widthProperty().addListener((observable, oldval, newval)->{
//			if (currentDataInfo!=null){
//				drawDataUnit(lastDetection);
//			}
//		});
//		
//		this.heightProperty().addListener((observable, oldval, newval)->{
//			if (currentDataInfo!=null){
//				drawDataUnit(lastDetection);
//			}
//		});
		
		//if axis change size, i.e. due to animation the plot pane can reamin the same size but canvas chnages. So need width
		//and height listeners on canvas, not the plot pane. 
		this.getPlotCanvas().heightProperty().addListener((obsVal, oldVal, newVal)->{
			//drawCurrentUnit();
		});
		
		this.getPlotCanvas().widthProperty().addListener((obsVal, oldVal, newVal)->{
			//drawCurrentUnit();
		});
	}
	
	
}

