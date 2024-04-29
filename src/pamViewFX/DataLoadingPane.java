package pamViewFX;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import pamScrollSystem.LoadQueueProgressData;
import pamViewFX.fxNodes.PamBorderPane;
import pamViewFX.fxNodes.PamVBox;
import pamViewFX.fxNodes.pamDialogFX.PamDialogFX;

/**
 * A pane which shows progress bars bars for various data load tasks in PAMGuard. 
 * Once data loading has completed the bars disappear and the hiding pane disappears. 
 * @author Jamie Macaulay 
 *
 */
public class DataLoadingPane extends PamBorderPane {
	
	/**
	 * Progress which shows progress in loading a particular datastream
	 */
	private ProgressBar streamProgress;
	
	/**
	 * Progress bar which shows all current progress in loading data.
	 */
	private ProgressBar allProgress;
	
	/**
	 * Label which shows the store name.
	 */
	private Label storeName;

	/**
	 * Label which indicates the current stream being loaded 
	 */
	private Label streamName;

	/**
	 * 
	 */
	private boolean emergencyStop; 
	
	/**
	 * Create the DataLoadingPane; 
	 * @param pamGuiManger
	 */
	public DataLoadingPane(PamGuiManagerFX pamGuiManger){
		this.setCenter(createPane());
	}
	
	/*
	 * Create the pane which holds various PAMGuard diagnostic and indicators. 
	 */
	private Pane createPane(){
		
		PamVBox holderPane=new PamVBox();
		holderPane.setSpacing(5);
		
		holderPane.getChildren().add(allProgress);
		
		holderPane.getChildren().add(streamProgress);

		return holderPane;
		
	}
	
	public void setData(LoadQueueProgressData progressData) {
		streamProgress.setProgress(-1);
		if (progressData.getStreamName() != null) {
			allProgress.setProgress(progressData.getTotalStreams()/progressData.getIStream());
//			storeName.setText(progressData.getStoreType());
			storeName.setText(String.format("Loading data block %d of %d", 
					progressData.getIStream(), progressData.getTotalStreams()));
			streamName.setText(progressData.getStreamName());
		}
		long interval = progressData.getLoadEnd() - progressData.getLoadStart();
		if (interval == 0) {
			streamProgress.setProgress(-1);
		}
		else {
			streamProgress.setProgress(-1);
			long done = progressData.getLoadCurrent() - progressData.getLoadStart();
			int percent = (int) (100L*done / interval);
			streamProgress.setProgress(percent);
		}
			
	}
	
	
	void stopPressed() {
		boolean ans = PamDialogFX.showMessageDialog("Do you want to stop loading data ?",
				"Data Loading");
		if (ans) {
			emergencyStop = true;
		}
	}
	
	public boolean shouldStop() {
		return emergencyStop;
	}
	
}
