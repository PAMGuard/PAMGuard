package dataMap.layoutFX;

import java.util.ArrayList;

import PamController.OfflineDataStore;
import PamController.PamController;
import dataMap.DataMapControl;
import dataMap.OfflineDataMap;
import dataMap.OfflineDataMapPoint;
import javafx.scene.paint.Color;
import pamViewFX.fxNodes.pamScrollers.acousticScroller.ScrollBarPane;

/**
 * A scroll bar which shows a summary of the data. This allows users to scroll to different sections of the data, 
 * 
 */
public class DataMapScrollBar extends ScrollBarPane {

	private DataMapControl dataMapControl;

	public DataMapScrollBar(DataMapControl dataMapControl) {
		super();
		this.dataMapControl=dataMapControl;
		
		this.widthProperty().addListener((obsVal, oldval, newVal)->{
			paintDataSummary();
		});
		
		this.heightProperty().addListener((obsVal, oldval, newVal)->{
			paintDataSummary();
		});
		
		this.getScrollBox().setRangeButtonVisible(true);
	}

	/**
	 * Paint a summary of the data
	 */
	public void paintDataSummary() {
		
		this.getDrawCanvas().getGraphicsContext2D().clearRect(0, 0, this.getWidth(), this.getHeight());

		ArrayList<OfflineDataStore> offlineDataStores = PamController.getInstance().findOfflineDataStores();
		OfflineDataStore aSource;
		
		Color color = Color.DODGERBLUE;
		
		this.getDrawCanvas().getGraphicsContext2D().setStroke(color);
		this.getDrawCanvas().getGraphicsContext2D().setGlobalAlpha(0.3);

		for (int i = 0; i < offlineDataStores.size(); i++) {
			aSource = offlineDataStores.get(i);				
			paintOfflineDataSource(aSource);
		}
	}

	/**
	 * Paint the data on the canvas. 
	 * @param dataSource - the dta source. 
	 */
	private void paintOfflineDataSource(OfflineDataStore dataSource) {
		if (dataMapControl.getMappedDataBlocks()==null) return;

		OfflineDataMap aMap;		
		for (int i = 0; i < dataMapControl.getMappedDataBlocks().size(); i++) {
			aMap = dataMapControl.getMappedDataBlocks().get(i).getOfflineDataMap(dataSource);
			if (aMap == null) {
				continue;
			}
			long lastTime;
			OfflineDataMapPoint aPoint;
			for (int j=0; j<aMap.getMapPoints().size(); j++) {
				aPoint= (OfflineDataMapPoint) aMap.getMapPoints().get(j);

				//now have to paint the canvas. To be efficient find the start pixel, then iterate through all the 
				//the other pixels. 
				double startPixel = time2Pixel(aPoint.getStartTime());
				double endPixel = time2Pixel(aPoint.getEndTime());

				for (double k=startPixel; k<endPixel ; k++) {
					this.getDrawCanvas().getGraphicsContext2D().strokeLine(k, 0, k, this.getDrawCanvas().getHeight());
				}
			}
		}
	}

	/**
	 * Convert a time in millis to a pixel on the canvas. 
	 * @param time
	 * @return
	 */
	private double time2Pixel(long time) {
		double timeRangemillis  = this.getMaxVal() - this.getMinVal();

		double frac = ((double) (time - dataMapControl.getFirstTime()))/timeRangemillis;

		return frac*this.getDrawCanvas().getWidth();
	}

	
	private long pixel2Time(double pixel) {
		
		double frac = pixel/this.getDrawCanvas().getWidth();
	
		double timeRangemillis  = this.getMaxVal() - this.getMinVal();

		long timeMillis = (long) (frac*timeRangemillis) + dataMapControl.getFirstTime();

		return timeMillis;
	}

}


